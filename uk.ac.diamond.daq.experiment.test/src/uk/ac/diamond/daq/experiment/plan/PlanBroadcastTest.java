package uk.ac.diamond.daq.experiment.plan;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static uk.ac.diamond.daq.experiment.api.plan.event.EventConstants.EXPERIMENT_PLAN_TOPIC;

import java.io.PrintStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import gda.TestHelpers;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.event.PlanStatusBean;
import uk.ac.diamond.daq.experiment.api.plan.event.SegmentRecord;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerRecord;
import uk.ac.diamond.daq.experiment.driver.NoImplDriver;

/**
 * Tests related to the recording and broadcasting of events by the experiment plan
 */
public class PlanBroadcastTest {

	private static final String PLAN_NAME = "Plan B";
	private static final String SEV_NAME = "displacement";
	private static final String FIRST_SEGMENT_NAME = "Merl";
	private static final String SECOND_SEGMENT_NAME = "Lenny";
	private static final String THIRD_SEGMENT_NAME = "Terrance";
	private static final String TRIGGER_ONE_NAME = "Joyce";
	private static final String TRIGGER_TWO_NAME = "Nelson";
	private static final String DRIVER_NAME = "Dorothy";
	private static final String DRIVER_PROFILE_NAME = "Nigel";

	private PlanStatusBean bean;

	private Plan plan;
	private MockSEV sev;

	private DummyPublisher publisher;

	@Before
	public void setUp() throws Exception {
		TestHelpers.setUpTest(PlanBroadcastTest.class, "DontCare", true);
		mockOsgiInjection();
		initPlan();
	}

	@Test
	public void planNameIsBroadcasted() {
		plan.addSegment(FIRST_SEGMENT_NAME, s -> true);
		plan.start();

		assertThat(bean.getName(), is(PLAN_NAME));
	}

	@Test
	public void statusIsBroadcasted() {
		plan.addSegment(FIRST_SEGMENT_NAME, s -> s > 1);
		plan.start();

		assertThat(bean.getStatus(), is(Status.RUNNING));

		sev.broadcast(2);

		assertThat(bean.getStatus(), is(Status.COMPLETE));
	}

	private SegmentRecord getLastSegment() {
		return bean.getSegments().get(bean.getSegments().size()-1);
	}

	private TriggerRecord getLastTrigger() {
		return bean.getTriggers().get(bean.getTriggers().size()-1);
	}

	@Test
	public void segmentsRecords() {
		plan.addSegment(FIRST_SEGMENT_NAME, s -> s > 0);
		plan.addSegment(SECOND_SEGMENT_NAME, s -> s < 0);
		plan.start();

		SegmentRecord s1Record = getLastSegment();
		assertThat(s1Record.getSegmentName(), is(FIRST_SEGMENT_NAME));

		sev.broadcast(1);

		SegmentRecord s2Record = getLastSegment();
		assertThat(s2Record.getSegmentName(), is(SECOND_SEGMENT_NAME));

		sev.broadcast(-1);

	}

	@Test
	public void triggersAreBroadcasted() {
		plan.addSegment(FIRST_SEGMENT_NAME, s -> s >= 5,
				plan.addTrigger(TRIGGER_ONE_NAME, this::work, 1),
				plan.addTrigger(TRIGGER_TWO_NAME, this::work, 0.3));

		plan.start();

		sev.broadcast(0.3);

		assertThat(bean.getLastTrigger(), is(TRIGGER_TWO_NAME));

		sev.broadcast(0.9);

		assertThat(bean.getLastTrigger(), is(TRIGGER_TWO_NAME));

		sev.broadcast(1);

		assertThat(bean.getLastTrigger(), is(TRIGGER_ONE_NAME));
	}

	@Test
	public void experimentDriverNameAndProfile() {
		IExperimentDriver driver = new NoImplDriver();
		driver.setName(DRIVER_NAME);
		driver.getModel().setName(DRIVER_PROFILE_NAME);
		plan.setDriver(driver);

		plan.addSegment(FIRST_SEGMENT_NAME, x -> true);
		plan.start();

		assertThat(bean.getDriverName(), is(DRIVER_NAME));
		assertThat(bean.getDriverProfile(), is(DRIVER_PROFILE_NAME));
	}


	@Test
	public void runningMultipleTimesOverwritesRecord() {
		plan.addSegment(FIRST_SEGMENT_NAME, sev, x->true);
		plan.addSegment(SECOND_SEGMENT_NAME, sev, x->true);

		plan.start(); // finishes immediately
		plan.start();
		assertThat(plan.getExperimentRecord().getSegmentRecords().size(), is(2));
	}

	@Test
	public void allTriggersRecorded() {

		DummySEVTrigger t1 = (DummySEVTrigger) plan.addTrigger(TRIGGER_ONE_NAME, this::work, 4);
		DummySEVTrigger t2 = (DummySEVTrigger) plan.addTrigger(TRIGGER_TWO_NAME, this::work, 1);

		plan.addSegment(FIRST_SEGMENT_NAME, x -> x > 12, t1);
		plan.addSegment(SECOND_SEGMENT_NAME, x -> x > 30, t2);

		plan.start();

		sev.ramp(31, 1);

		TriggerRecord t1account = plan.getExperimentRecord().getTriggerRecord(TRIGGER_ONE_NAME);
		TriggerRecord t2account = plan.getExperimentRecord().getTriggerRecord(TRIGGER_TWO_NAME);
		assertThat(t1account.getEvents().size(), is(t1.getTriggerCount()));
		assertThat(t2account.getEvents().size(), is(t2.getTriggerCount()));
	}

	@Test
	public void triggerSuccessRecorded() throws Exception {
		plan.addSegment(FIRST_SEGMENT_NAME, x -> x == 1,
			plan.addTrigger(TRIGGER_ONE_NAME, this::badJob, 0.3, 0.01),
			plan.addTrigger(TRIGGER_TWO_NAME, this::work, 0.4, 0.01));

		// We are expecting 6 broadcasts in total:
		// 1 segment start
		// 2 trigger one start
		// 3 trigger one complete
		// 4 trigger two start
		// 5 trigger two complete
		// 6 experiment end
		publisher.useCounter(6);

		plan.start();

		sev.broadcast(0.3); // trigger work() (succeeds)
		sev.broadcast(0.4); // trigger badJob() (fails)
		sev.broadcast(1);   // terminate segment

		publisher.await();

		TriggerEvent triggerEvent1 = plan.getExperimentRecord().getTriggerRecord(TRIGGER_ONE_NAME).getEvents().iterator().next();
		assertThat(triggerEvent1.isSuccessful(), is(false));

		TriggerEvent triggerEvent2 = plan.getExperimentRecord().getTriggerRecord(TRIGGER_TWO_NAME).getEvents().iterator().next();
		assertThat(triggerEvent2.isSuccessful(), is(true));
	}

	@Test
	public void BroadcastSevNamesForSegmentsAndTriggers() {
		plan.addSegment(FIRST_SEGMENT_NAME, s -> s > 1,
				plan.addTrigger(TRIGGER_ONE_NAME, this::work, 0.5));

		plan.start();

		sev.broadcast(0.5);

		assertThat(getLastTrigger().getSampleEnvironmentName(), is(SEV_NAME));
		assertThat(getLastSegment().getSampleEnvironmentName(), is(SEV_NAME));
	}

	private void badJob() {
		throw new RuntimeException("I'm afraid I cannot do that, Dave");
	}

	@Test
	public void significantSEVSignalsRecorded() {
		/* we need to record sev signals which:
		 * - cause segment transition
		 * - trigger a trigger's triggerableoperation
		 */

		plan.addSegment(FIRST_SEGMENT_NAME, x -> x >= 10, // a signal of 10 or higher would cause this segment to terminate
				plan.addTrigger(TRIGGER_ONE_NAME, this::work, 2.5)); // this trigger fires in sev signal intervals of 2.5 (or greater)

		plan.start();

		/*
		 * The broadcasted signals from simulated SEV sequence:
		 * 0 (signal at plan.start())
		 * 1.5
		 * 3 -> trigger fires
		 * 4.5
		 * 6 -> trigger fires
		 * 7.5
		 * 9 -> trigger fires
		 * 10.5 -> segment ends
		 * 12
		 */
		sev.ramp(11, 1.5);

		TriggerRecord triggerAccount = plan.getExperimentRecord().getTriggerRecord(TRIGGER_ONE_NAME);
		List<TriggerEvent> counts = triggerAccount.getEvents();

		assertThat(counts.size(), is(3));

		List<Double> triggeringSignals = counts.stream().map(TriggerEvent::getTriggeringSignal).collect(toList());
		assertThat(triggeringSignals, is(equalTo(Arrays.asList(3.0, 6.0, 9.0))));

		SegmentRecord segmentAccount = plan.getExperimentRecord().getSegmentRecord(FIRST_SEGMENT_NAME);
		assertThat(segmentAccount.getTerminationSignal(), is(10.5));
	}

	@Test
	public void segmentActivationAndCompletionTimestamped() {
		plan.addSegment(FIRST_SEGMENT_NAME, x->x>5);
		plan.addSegment(SECOND_SEGMENT_NAME, x->x>10);
		plan.addSegment(THIRD_SEGMENT_NAME, x->x>15);

		plan.start();

		sev.ramp(16, 1);

		List<SegmentRecord> segmentAccounts = plan.getExperimentRecord().getSegmentRecords();
		assertThat(segmentAccounts.size(), is(3));

		List<String> accountNames = segmentAccounts.stream().map(SegmentRecord::getSegmentName).collect(toList());
		assertThat(accountNames, is(equalTo(Arrays.asList(FIRST_SEGMENT_NAME, SECOND_SEGMENT_NAME, THIRD_SEGMENT_NAME))));

		segmentAccounts.forEach(accnt -> {
			assertThat(accnt.getStartTime(), is(not(after(accnt.getEndTime()))));
			// i.e. start time is before OR equal to end time (for quick tests!)
		});
	}

	private static Matcher<Long> after(long timestamp) {
		return greaterThan(timestamp);
	}

	@Test (expected = IllegalArgumentException.class)
	public void invalidSegmentAccountRequested() {
		plan.addSegment(FIRST_SEGMENT_NAME, x -> true);
		plan.start();
		plan.getExperimentRecord().getSegmentRecord(SECOND_SEGMENT_NAME);
	}

	@Test (expected = IllegalArgumentException.class)
	public void invalidTriggerAccountRequested() {
		plan.addSegment(FIRST_SEGMENT_NAME, x -> true);
		plan.start();
		plan.getExperimentRecord().getTriggerRecord(TRIGGER_ONE_NAME);
	}

	@Test (expected=IllegalStateException.class)
	public void getRecordWhileRunningThrows() {
		plan.addSegment(FIRST_SEGMENT_NAME, sev, x -> false);
		plan.start();
		assertTrue(plan.isRunning());
		plan.getExperimentRecord();
	}

	private void initPlan() {
		plan = new Plan(PLAN_NAME);
		sev = new MockSEV();
		sev.setName(SEV_NAME);
		plan.setFactory(new TestFactory(sev));
		plan.addSEV(()->0.0); // doesn't matter
	}

	/**
	 * Give the plan an event service which creates our dummy publisher
	 */
	private void mockOsgiInjection() {
		IEventService eventService = mock(IEventService.class);
		publisher = new DummyPublisher();
		doReturn(publisher).when(eventService).createPublisher(any(), eq(EXPERIMENT_PLAN_TOPIC));

		ExperimentRecord experimentRecord = new ExperimentRecord("doesn't matter");
		experimentRecord.setEventService(eventService);
	}

	private void work() { /* a Triggerable with a more meaningful name */ }

	/**
	 * Simply assigns {@code this.broadcastedBean}
	 * when {@link #broadcast(PlanBean)} is called.
	 *
	 * Can also initiate a countdown latch to wait
	 * for a desired number of broadcast events:
	 * <pre>
	 * {@code
	 * publisher.useCounter(3);
	 * ...
	 * boolean capturedThreeEvents = publisher.await();
	 * }
	 * </pre>
	 */
	class DummyPublisher implements IPublisher<PlanStatusBean> {

		private CountDownLatch latch;

		void useCounter(int desiredCounts) {
			latch = new CountDownLatch(desiredCounts);
		}

		boolean await() throws InterruptedException {
			return latch.await(50, TimeUnit.MILLISECONDS);
		}

		@Override
		public void broadcast(PlanStatusBean bean) throws EventException {
			PlanBroadcastTest.this.bean = bean;
			if (latch != null) {
				latch.countDown();
			}
		}

		@Override
		public String getTopicName() {
			return EXPERIMENT_PLAN_TOPIC;
		}

		@Override
		public void disconnect() throws EventException {
			// no-op
		}

		@Override
		public URI getUri() {
			// no-op
			return null;
		}

		@Override
		public IEventConnectorService getConnectorService() {
			// no-op
			return null;
		}

		@Override
		public boolean isConnected() {
			// no-op
			return true;
		}

		@Override
		public String getStatusSetName() {
			// no-op
			return null;
		}

		@Override
		public void setStatusSetName(String queueName) {
			// no-op
		}

		@Override
		public void setStatusSetAddRequired(boolean isRequired) {
			// no-op
		}

		@Override
		public void setLoggingStream(PrintStream stream) {
			// no-op
		}

	}
}