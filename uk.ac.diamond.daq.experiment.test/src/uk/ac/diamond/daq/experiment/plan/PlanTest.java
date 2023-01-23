package uk.ac.diamond.daq.experiment.plan;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.server.servlet.Services;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gda.TestHelpers;
import uk.ac.diamond.daq.experiment.api.EventConstants;
import uk.ac.diamond.daq.experiment.api.driver.DriverState;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.driver.SingleAxisLinearSeries;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.Payload;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.experiment.driver.NoImplDriver;
import uk.ac.diamond.daq.experiment.plan.trigger.DummySEVTrigger;

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

	private Payload payload;

	@Before
	public void setupBasicPlan() throws Exception {

		// OSGi would usually set the following
		IEventService eventService = mock(IEventService.class);
		ExperimentRecord experimentRecord = new ExperimentRecord("");
		experimentRecord.setEventService(eventService);

		// mock subscriber to experiment controller
		doReturn(mock(ISubscriber.class)).when(eventService).createSubscriber(any(), eq(EventConstants.EXPERIMENT_CONTROLLER_TOPIC));
		new Services().setEventService(eventService);

		plan = new Plan(EXPERIMENT_NAME);
		sev = new MockSEV();

		plan.setFactory(new TestFactory(sev));

		plan.experimentController = mock(ExperimentController.class);

		plan.addSEV(()->0.0); // TestFactory::addSEV returns our MockSEV



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
	public void experimentplanWillSkipToCorrectInitialSegment() {
		plan.addSegment(SEGMENT1_NAME, x->x>5);
		plan.addSegment(SEGMENT2_NAME, x->x>10);
		ISegment s3 = plan.addSegment(SEGMENT3_NAME, x->x>15);

		sev.broadcast(13);

		// starting ep will activate s1, but we want it to quickly skip to s3
		plan.start();
		assertThat(s3.isActivated(), is(true));

		sev.broadcast(16);

		// but all segments run
		assertThat(plan.getExperimentRecord().getSegmentRecords().size(), is(3));
	}

	@Test
	public void segmentNamesShouldBeUnique() {
		plan.addSegment(SEGMENT1_NAME, s->false);
		plan.addSegment(SEGMENT1_NAME, s->true);
		plan.start();
		assertThat(plan.isRunning(), is(false));
	}

	@Test
	public void triggerNamesShouldBeUnique() {
		plan.addSegment(SEGMENT1_NAME, s -> s > 5,
				plan.addTrigger(TRIGGER1_NAME, payload, 0),
				plan.addTrigger(TRIGGER1_NAME, payload, 5));
		plan.start();
		assertThat(plan.isRunning(), is(false));
	}

	@Test
	public void planUsesExperimentController() throws ExperimentControllerException {

		plan.addSegment(SEGMENT1_NAME, s -> s >= 10.0,
				plan.addTrigger(TRIGGER1_NAME, payload, 3.0),
				plan.addTrigger(TRIGGER2_NAME, payload, 4.0));

		plan.addSegment(SEGMENT2_NAME, s -> s < 0,
				plan.addTrigger(TRIGGER3_NAME, payload, 2.0));

		plan.start();

		sev.broadcast(10); // end of segment1, segment2 starts
		sev.ramp(2, -1);
		sev.broadcast(-0.3); // end of segment2 & no further segments -> end of experiment

		ExperimentController controller = plan.experimentController;

		Mockito.verify(controller).startExperiment(EXPERIMENT_NAME);
		Mockito.verify(controller).startMultipartAcquisition(SEGMENT1_NAME);
		Mockito.verify(controller).startMultipartAcquisition(SEGMENT2_NAME);
		Mockito.verify(controller, Mockito.times(2)).stopMultipartAcquisition();
		Mockito.verify(controller).isExperimentInProgress();
		Mockito.verifyNoMoreInteractions(controller);

		/*
		 * But what about experiment.prepareAcquisition() ?
		 * That is the responsibility of the particular trigger/trigger processor
		 */
	}

	@Test
	public void segmentsShouldAssessSEVSignalsBeforeTriggers() {
		// let's say the active segment reaches its limit when sev signal > 10
		// the segment enables a single trigger - disabled in next segment - which triggers in sev signal intervals of 5
		// if the next sev signal to be broadcast could both cause a segment transition *and* trigger activation,
		// the segment should terminate first which means the trigger should *not* fire

		DummySEVTrigger trigger = (DummySEVTrigger) plan.addTrigger(TRIGGER1_NAME, payload, sev, 5);
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
		experimentDriver.setModel(new SingleAxisLinearSeries("Displacement"));
		assertThat(experimentDriver.getState(), is(DriverState.IDLE));
		plan.setDriver(experimentDriver);
		plan.addSegment(SEGMENT1_NAME, this::neverEnding);

		plan.start();

		assertThat(experimentDriver.hasRun(), is(true));
	}

	@Test
	public void planAborted() {
		plan.addSegment(SEGMENT1_NAME, this::neverEnding);
		plan.start();
		plan.abort();

		assertThat(plan.isRunning(), is(false));
	}

	@Test
	public void nothingTriggeredAfterAbortion() {
		var trigger = (DummySEVTrigger) plan.addTrigger(TRIGGER1_NAME, payload, sev, 5);
		plan.addSegment(SEGMENT1_NAME, this::neverEnding, trigger);
		plan.start();
		plan.abort();

		sev.ramp(50, 1);

		assertThat(trigger.getTriggerCount(), is(0)); // would expect 10 events if plan were still running
	}

	@Test
	public void driverStoppedOnAbort() {
		@SuppressWarnings("unchecked")
		IExperimentDriver<SingleAxisLinearSeries> driver = mock(IExperimentDriver.class);
		var model = new SingleAxisLinearSeries();
		model.setAxisName("model");
		Mockito.when(driver.getName()).thenReturn("driver");
		Mockito.when(driver.getModel()).thenReturn(model);
		plan.setDriver(driver);
		plan.addSegment(SEGMENT1_NAME, this::neverEnding);
		plan.start();
		plan.abort();

		Mockito.verify(driver).abort();
	}

	@Test
	public void experimentStoppedAfterAbortion() throws ExperimentControllerException {
		ExperimentController controller = plan.experimentController;

		plan.addSegment(SEGMENT1_NAME, this::neverEnding);
		plan.start();

		when(controller.isExperimentInProgress()).thenReturn(true);
		plan.abort();

		Mockito.verify(controller).startExperiment(EXPERIMENT_NAME);
		Mockito.verify(controller).startMultipartAcquisition(SEGMENT1_NAME);
		Mockito.verify(controller).stopMultipartAcquisition();
		Mockito.verify(controller).stopExperiment();
		Mockito.verify(controller, Mockito.times(2)).isExperimentInProgress(); // twice? yes: one before stopping multipart acquisition, one before stopping experiment
		Mockito.verifyNoMoreInteractions(controller);
	}

	/**
	 * A limit condition for never ending segments
	 */
	private boolean neverEnding(double ignored) {
		return false;
	}
}
