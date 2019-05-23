package uk.ac.diamond.daq.experiment.plan;

import static gda.configuration.properties.LocalProperties.GDA_DATAWRITER_DIR;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Paths;

import org.eclipse.scanning.api.event.IEventService;
import org.junit.Before;
import org.junit.Test;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.api.driver.DriverState;
import uk.ac.diamond.daq.experiment.api.driver.SingleAxisLinearSeries;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
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

		plan.setFactory(new TestFactory(sev));

		plan.addSEV(()->0.0); // TestFactory::addSEV returns our MockSEV

		// OSGi would usually set the following
		IEventService eventService = mock(IEventService.class);
		ExperimentRecord experimentRecord = new ExperimentRecord("");
		experimentRecord.setEventService(eventService);

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
		assertThat(plan.getExperimentRecord().getSegmentRecords().size(), is(3));
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
		ep.setFactory(new TestFactory(sev));
		ep.addSEV(()->0.0); // TestFactory doesn't give a monkeys

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

		NoImplDriver experimentDriver = new NoImplDriver();
		experimentDriver.setModel(new SingleAxisLinearSeries());
		assertThat(experimentDriver.getState(), is(DriverState.IDLE));
		plan.setDriver(experimentDriver);
		plan.addSegment(SEGMENT1_NAME, sev -> false);

		plan.start();

		assertThat(experimentDriver.hasRun(), is(true));
	}

	/**
	 * A named {@link Triggerable} for readability. Does nothing.
	 */
	private Object someJob() {
		return null;
	}
}
