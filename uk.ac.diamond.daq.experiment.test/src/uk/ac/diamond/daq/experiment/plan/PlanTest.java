package uk.ac.diamond.daq.experiment.plan;

import static gda.configuration.properties.LocalProperties.GDA_DATAWRITER_DIR;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.api.driver.DriverState;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.ISegmentAccount;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.ITriggerAccount;
import uk.ac.diamond.daq.experiment.api.plan.ITriggerEvent;
import uk.ac.diamond.daq.experiment.api.plan.SignalSource;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;
import uk.ac.diamond.daq.experiment.driver.NoImplDriver;

public class PlanTest {

	private static final String EXPERIMENT_NAME = "TheExperiment";
	private static final String SEGMENT1_NAME = "First";
	private static final String SEGMENT2_NAME = "Second";
	private static final String SEGMENT3_NAME = "Third";
	private static final String TRIGGER1_NAME = "T1";
	private static final String TRIGGER2_NAME = "T2";
	private static final String TRIGGER3_NAME = "T3";

	private Plan plan;
	private MockSEV sev;

	@Before
	public void setupBasicplan() throws Exception {

		plan = new Plan(EXPERIMENT_NAME);
		sev = new MockSEV();

		plan.setFactory(new TestFactory());

		plan.addSEV(null); // TestFactory::addSEV returns our MockSEV

		TestHelpers.setUpTest(PlanTest.class, "DontCare", true);
	}

	@Test
	public void segmentsRunInCreationOrder() {
		ISegment segment1 = plan.addSegment(SEGMENT1_NAME, signal -> signal > 12);
		ISegment segment2 = plan.addSegment(SEGMENT2_NAME, signal -> signal < 0);

		plan.start();

		assertThat(segment1.isActivated(), is(true));
		assertThat(segment2.isActivated(), is(false));

		sev.broadcast(24);

		assertThat(segment1.isActivated(), is(false));
		assertThat(segment2.isActivated(), is(true));

		sev.broadcast(-5);

		assertThat(plan.isRunning(), is(false));
	}

	@Test (expected = IllegalArgumentException.class)
	public void convenienceMethodsThrowIfNoSEVFound() {
		Plan badPlan = new Plan("Ill-defined");
		badPlan.addSegment("ShouldFail", x -> x > 5);
	}

	@Test
	public void experimentplanWillSkipToCorrectInitialSegment() throws InterruptedException {
		plan.addSegment(SEGMENT1_NAME, x->x>5);
		plan.addSegment(SEGMENT2_NAME, x->x>10);
		ISegment s3 = plan.addSegment(SEGMENT3_NAME, x->x>15);

		sev.broadcast(13);

		// starting ep will activate s1, but we want it to quickly skip to s3
		plan.start();
		assertTrue(s3.isActivated());

		sev.broadcast(16);

		// but all segments run
		assertThat(plan.getExperimentRecord().getSegmentAccounts().size(), is(3));
	}

	@Test
	public void segmentActivationAndCompletionTimestamped() {
		plan.addSegment(SEGMENT1_NAME, x->x>5);
		plan.addSegment(SEGMENT2_NAME, x->x>10);
		plan.addSegment(SEGMENT3_NAME, x->x>15);

		plan.start();

		sev.ramp(16, 1);

		List<ISegmentAccount> segmentAccounts = plan.getExperimentRecord().getSegmentAccounts();
		assertThat(segmentAccounts.size(), is(3));

		List<String> accountNames = segmentAccounts.stream().map(ISegmentAccount::getSegmentName).collect(toList());
		assertThat(accountNames, is(equalTo(Arrays.asList(SEGMENT1_NAME, SEGMENT2_NAME, SEGMENT3_NAME))));

		segmentAccounts.forEach(accnt -> {
			assertThat(accnt.getStartTime(), is(not(after(accnt.getEndTime()))));
			// i.e. start time is before OR equal to end time (for quick tests!)
		});
	}

	private static Matcher<ChronoZonedDateTime<?>> after(ZonedDateTime other) {
		return greaterThan(other);
	}

	@Test (expected = IllegalArgumentException.class)
	public void invalidSegmentAccountRequested() {
		plan.addSegment(SEGMENT1_NAME, x -> true);
		plan.start();
		plan.getExperimentRecord().getSegmentAccount(SEGMENT2_NAME);
	}

	@Test (expected = IllegalArgumentException.class)
	public void invalidTriggerAccountRequested() {
		plan.addSegment(SEGMENT1_NAME, x -> true);
		plan.start();
		plan.getExperimentRecord().getTriggerAccount(TRIGGER1_NAME);
	}

	@Test (expected=IllegalStateException.class)
	public void getRecordWhileRunningThrows() {
		plan.addSegment(SEGMENT1_NAME, sev, x -> false);
		plan.start();
		assertTrue(plan.isRunning());
		plan.getExperimentRecord();
	}

	@Test (expected=IllegalStateException.class)
	public void segmentNamesShouldBeUnique() {
		plan.addSegment(SEGMENT1_NAME, s->false);
		plan.addSegment(SEGMENT1_NAME, s->true);
		plan.start();
	}

	@Test (expected=IllegalStateException.class)
	public void triggerNamesShouldBeUnique() {
		plan.addSegment(SEGMENT1_NAME, s -> s > 5,
				plan.addTrigger(TRIGGER1_NAME, this::someJob, 0),
				plan.addTrigger(TRIGGER1_NAME, this::someJob, 5));
		plan.start();
	}

	@Test
	public void runningMultipleTimesOverwritesRecord() {
		plan.addSegment(SEGMENT1_NAME, sev, x->true);
		plan.addSegment(SEGMENT2_NAME, sev, x->true);

		plan.start(); // finishes immediately
		plan.start();
		assertThat(plan.getExperimentRecord().getSegmentAccounts().size(), is(2));
	}

	@Test
	public void allTriggersRecorded() {

		DummySEVTrigger t1 = (DummySEVTrigger) plan.addTrigger(TRIGGER1_NAME, this::someJob, 4);
		DummySEVTrigger t2 = (DummySEVTrigger) plan.addTrigger(TRIGGER2_NAME, this::someJob, 1);

		plan.addSegment(SEGMENT1_NAME, x -> x > 12, t1);
		plan.addSegment(SEGMENT2_NAME, x -> x > 30, t2);

		plan.start();

		sev.ramp(31, 1);

		ITriggerAccount t1account = plan.getExperimentRecord().getTriggerAccount(TRIGGER1_NAME);
		ITriggerAccount t2account = plan.getExperimentRecord().getTriggerAccount(TRIGGER2_NAME);
		assertThat(t1account.getEvents().size(), is(t1.getTriggerCount()));
		assertThat(t2account.getEvents().size(), is(t2.getTriggerCount()));
	}

	@Test
	public void significantSEVSignalsRecorded() {
		/* we need to record sev signals which:
		 * - cause segment transition
		 * - trigger a trigger's triggerableoperation
		 */

		plan.addSegment(SEGMENT1_NAME, x -> x >= 10, // a signal of 10 or higher would cause this segment to terminate
				plan.addTrigger(TRIGGER1_NAME, this::someJob, 2.5)); // this trigger fires in sev signal intervals of 2.5 (or greater)

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

		ITriggerAccount triggerAccount = plan.getExperimentRecord().getTriggerAccount(TRIGGER1_NAME);
		List<ITriggerEvent> counts = triggerAccount.getEvents();

		assertThat(counts.size(), is(3));

		List<Double> triggeringSignals = counts.stream().map(ITriggerEvent::getTriggeringSignal).collect(toList());
		assertThat(triggeringSignals, is(equalTo(Arrays.asList(3.0, 6.0, 9.0))));

		ISegmentAccount segmentAccount = plan.getExperimentRecord().getSegmentAccount(SEGMENT1_NAME);
		assertThat(segmentAccount.getTerminationSignal(), is(10.5));
	}

	@Test
	public void planSetsCorrectDatawriterProperty() {

		// When a trigger fires, we want any files created as a consequence
		// to be saved in {data directory}/{plan name}/{segment name}/{trigger name}

		// This is achieved by setting the GDA_DATAWRITER_DIR property

		plan.addSegment(SEGMENT1_NAME, s -> s >= 10.0,
				plan.addTrigger(TRIGGER1_NAME, this::someJob, 3.0),
				plan.addTrigger(TRIGGER2_NAME, this::someJob, 4.0));

		plan.addSegment(SEGMENT2_NAME, s -> s < 0,
				plan.addTrigger(TRIGGER3_NAME, this::someJob, 2.0));

		final String defaultDir = LocalProperties.get(GDA_DATAWRITER_DIR);

		plan.start();

		final String experimentDir = Paths.get(defaultDir, EXPERIMENT_NAME).toString();
		assertThat(LocalProperties.get(GDA_DATAWRITER_DIR), is(experimentDir));

		sev.broadcast(3.0); // triggers trigger1

		final String s1t1Dir = Paths.get(experimentDir, SEGMENT1_NAME, TRIGGER1_NAME).toString();
		assertThat(LocalProperties.get(GDA_DATAWRITER_DIR), is(s1t1Dir));

		sev.broadcast(5.0); // triggers trigger2

		final String s1t2Dir = Paths.get(experimentDir, SEGMENT1_NAME, TRIGGER2_NAME).toString();
		assertThat(LocalProperties.get(GDA_DATAWRITER_DIR), is(s1t2Dir));

		sev.broadcast(10); // end of segment1, segment2 starts

		sev.ramp(2, -1);

		final String s2t3Dir = Paths.get(experimentDir, SEGMENT2_NAME, TRIGGER3_NAME).toString();
		assertThat(LocalProperties.get(GDA_DATAWRITER_DIR), is(s2t3Dir));

		sev.broadcast(-0.3); // end of segment2 & no further segments -> end of experiment

		assertThat(LocalProperties.get(GDA_DATAWRITER_DIR), is(defaultDir));
	}

	@Test
	public void nonAlphanumericsInNamesBecomeUnderscores() {
		// actually '-' and '.' are also allowed
		Plan ep = new Plan("My$Experiment");
		ep.setFactory(new TestFactory());
		ep.addSEV(null);

		ep.addSegment(" ", x -> x >= 5, ep.addTrigger("My.Trigger&", this::someJob, 2.5));

		String expectedDataDirectory = Paths.get(LocalProperties.get(GDA_DATAWRITER_DIR),
											"My_Experiment", "_", "My.Trigger_").toString();

		ep.start();

		sev.broadcast(3);
		assertThat(LocalProperties.get(GDA_DATAWRITER_DIR), is(expectedDataDirectory));
	}

	@Test
	public void segmentsShouldAssessSEVSignalsBeforeTriggers() {
		// let's say the active segment reaches its limit when sev signal > 10;
		// the segment enables a single trigger - disabled in next segment - which triggers in sev signal intervals of 5
		// if the next sev signal to be broadcast could both cause a segment transition *and* trigger activation,
		// the segment should terminate first which means the trigger should *not* fire

		DummySEVTrigger trigger = (DummySEVTrigger) plan.addTrigger(TRIGGER1_NAME, this::someJob, sev, 5);
		plan.addSegment(SEGMENT1_NAME, x -> x >= 10, trigger);

		plan.start();

		sev.ramp(10, 0.5);

		assertThat(plan.isRunning(), is(false));
		assertThat(trigger.getTriggerCount(), is(1));
	}

	@Test
	public void startingPlanShouldStartDriverIfIncluded() {
		// An experimental plan is a passive or reactive system
		// i.e. it responds to signals but does not drive them (natively).
		// The driving is done by the ExperimentDriver.
		// If one is included, calling start() on the plan
		// should also start the driver.

		IExperimentDriver experimentDriver = getExperimentDriver();
		assertThat(experimentDriver.getState(), is(DriverState.IDLE));
		plan.setDriver(experimentDriver);
		plan.addSegment(SEGMENT1_NAME, sev -> false);

		plan.start();

		assertThat(experimentDriver.getState(), is(DriverState.RUNNING));
	}

	private IExperimentDriver getExperimentDriver() {
		return new NoImplDriver();
	}

	/**
	 * Works like PositionTrigger but without executor service.
	 * No triggerable job but triggering signals increment trigger count (getTriggerCount())
	 */
	class DummySEVTrigger extends TriggerBase {

		private DummySEVTrigger(String name, IPlanRegistrar registrar, double positionInterval) {
			super(registrar, () -> {}, sev);
			setName(name);
			this.thesev = sev;
			this.positionInterval = positionInterval;
			this.registrar = registrar;
		}

		private final IPlanRegistrar registrar;
		private final ISampleEnvironmentVariable thesev;
		private final double positionInterval;

		private double previousTrigger;

		private int triggerCount;

		@Override
		public void signalChanged(double signal) {
			if (!isEnabled()) return;
			if (BigDecimal.valueOf(signal).subtract(BigDecimal.valueOf(previousTrigger)).abs().divide(BigDecimal.valueOf(positionInterval), 5, RoundingMode.HALF_UP).compareTo(BigDecimal.ONE) < 0) {
				return;
			}

			previousTrigger = signal;
			registrar.triggerOccurred(this, signal);
			triggerCount++;
		}

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			if (enabled) {
				previousTrigger = thesev.read();
				thesev.addListener(this);
			} else {
				thesev.removeListener(this);
			}
		}

		@Override
		protected void enable() {
			// nothing else
		}

		@Override
		protected void disable() {
			// nothing else
		}

		public int getTriggerCount() {
			return triggerCount;
		}

		@Override
		protected boolean evaluateTriggerCondition(double signal) {
			return false;
		}

	}

	/**
	 * Factory that injects our mock implementations
	 */
	class TestFactory extends PlanFactory {

		@Override
		public ISampleEnvironmentVariable addSEV(SignalSource signalProvider) {
			return sev;
		}

		@Override
		public ITrigger addTrigger(String name, Triggerable triggerable,ISampleEnvironmentVariable sev,
				double triggerInterval) {
			ITrigger trigger = new DummySEVTrigger(name, getRegistrar(), triggerInterval);
			trigger.setName(name);
			return trigger;
		}
	}

	private void someJob() {
		// do nothing: to avoid passing in null to dummy trigger for readability
	}

}
