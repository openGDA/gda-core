package uk.ac.diamond.daq.experiment.ui.plan.preview;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.diamond.daq.experiment.api.remote.Inequality.GREATER_THAN;
import static uk.ac.diamond.daq.experiment.api.remote.Inequality.LESS_THAN;
import static uk.ac.diamond.daq.experiment.api.remote.SignalSource.POSITION;
import static uk.ac.diamond.daq.experiment.api.remote.SignalSource.TIME;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gda.factory.Factory;
import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.plan.DriverBean;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.Inequality;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;

public class PlanPreviewerTest {

	private static final String SEGMENT_ONE_NAME = "seg1";
	private static final String SEGMENT_TWO_NAME = "seg2";
	private static final String SEGMENT_THREE_NAME = "seg3";

	private static final String TRIGGER_ONE_NAME = "trig1";

	private static final String DRIVER_NAME = "driver";
	private static final String DRIVER_PROFILE_NAME = "profile";
	private static final String SIGNAL_NAME = "calories";


	private static final double ERROR = 1e-9;

	@Mock
	private PlotController plotController;

	private static ExperimentService experimentService;

	// for trigger tests
	private ArgumentCaptor<Object> xCoordinates = ArgumentCaptor.forClass(Object.class);
	private ArgumentCaptor<Object> yCoordinates = ArgumentCaptor.forClass(Object.class);

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void prepareFinder() {
    	experimentService = mock(ExperimentService.class);

    	Map<String, ExperimentService> map = new HashMap<>();
    	map.put("experimentService", experimentService);

    	Factory testFactory = mock(Factory.class);
    	when(testFactory.getFindablesOfType(ExperimentService.class)).thenReturn(map);

    	Finder.getInstance().addFactory(testFactory);
    }

    @AfterClass
    public static void cleanupFinder() {
    	Finder.getInstance().removeAllFactories();
    }

    /* SEGMENT TESTS */

    @Test
    public void oneUnplottableSegmentPreventsPreviewOfAny() {
    	Dataset x = linearSpace(0, 1, 3);
    	Dataset y = discreteData(0.0, 1.0, 0.0);
    	prepareDriverProfile(x, y);

    	SegmentDescriptor segment1 = getSegment(SEGMENT_ONE_NAME, 20);

    	// unplottable because the driver profile isn't aware
    	// of signal 'Unknown', which this segment depends on
    	SegmentDescriptor segment2 = getSegment(SEGMENT_TWO_NAME, GREATER_THAN, 0.45);
    	segment2.setSampleEnvironmentVariableName("Unknown");

    	TriggerDescriptor trigger = getTrigger(TRIGGER_ONE_NAME, TIME, 2);
    	SegmentDescriptor segment3 = getSegment(SEGMENT_THREE_NAME, 15);
    	segment3.setTriggers(asList(trigger));

    	getPlanPreview(segment1, segment2, segment3).update();

    	// cannot mark segments
    	verify(plotController, never()).markSegmentEnd(anyString(), anyDouble());

    	// and therefore cannot calculate trigger positions
    	verify(plotController, never()).plotTriggerPoints(anyString(), any(), any());
    }

	@Test
	public void timeBasedSegments() {

		double duration1InSec = 10;
		double duration1InMin = duration1InSec / 60.0;
		SegmentDescriptor segment1 = getSegment(SEGMENT_ONE_NAME, duration1InSec);

		double duration2InSec = 15;
		double duration2InMin = duration2InSec / 60.0;
		SegmentDescriptor segment2 = getSegment(SEGMENT_TWO_NAME, duration2InSec);

		getPlanPreview(false, segment1, segment2).update();

		verify(plotController).markSegmentEnd(SEGMENT_ONE_NAME, duration1InMin);
		verify(plotController).markSegmentEnd(SEGMENT_TWO_NAME, duration1InMin + duration2InMin);
	}

	@Test
	public void positionBasedSegments() {
		Dataset x = linearSpace(0.0, 1.0, 3);
		Dataset y = discreteData(0.0, 1.0, 0.0);
		prepareDriverProfile(x, y);

		double yEndSeg1 = 0.7;
		SegmentDescriptor segment1 = getSegment(SEGMENT_ONE_NAME, GREATER_THAN, yEndSeg1);

		/* when will segment1 to end?
		 * sometime between the first two points: (0, 0), (0.5, 1)
		 *
		 * gradient of the line = (1-0)/(0.5-0) = 2
		 * intercept = y - mx = 0 - 2 * 0 = 0
		 *
		 * therefore xEndSeg1 = (yEndSeg1 + 0) / 2
		 */

		double xEndSeg1 = yEndSeg1 / 2.0;

		double yEndSeg2 = 0.2;
		SegmentDescriptor segment2 = getSegment(SEGMENT_TWO_NAME, LESS_THAN, yEndSeg2);


		/* when will segment2 end?
		 * y < 0.2 sometime between the last 2 points: (0.5, 1), (1.0, 0.0)
		 *
		 * gradient = (0 - 1) / (1 - 0.5) = -2
		 * intercept = y - mx = 0 - (-2) * 1 = 2
		 *
		 * xEndSeg2 = (yEndSeg2 - 2) / -2
		 */

		double xEndSeg2 = (yEndSeg2 - 2) / -2.0;

		getPlanPreview(segment1, segment2).update();

		verify(plotController).markSegmentEnd(SEGMENT_ONE_NAME, xEndSeg1);
		verify(plotController).markSegmentEnd(SEGMENT_TWO_NAME, xEndSeg2);
	}

	@Test
	public void mixedModeSegments() {
		Dataset x = linearSpace(0.0, 1.0, 3);
		Dataset y = discreteData(0.0, 1.0, 0.0);
		prepareDriverProfile(x, y);

		double seg1DurationSec = 35;
		SegmentDescriptor seg1 = getSegment(SEGMENT_ONE_NAME, seg1DurationSec);

		double seg2Ref = 0.14;
		SegmentDescriptor seg2 = getSegment(SEGMENT_TWO_NAME, LESS_THAN, seg2Ref);
		/* expected end: interpolate last two points, where m = -2; c = 2 */
		double seg2End = (seg2Ref - 2) / -2.0;

		double seg3DurationSec = 25;
		SegmentDescriptor seg3 = getSegment(SEGMENT_THREE_NAME, seg3DurationSec);

		getPlanPreview(seg1, seg2, seg3).update();

		verify(plotController).markSegmentEnd(SEGMENT_ONE_NAME, seg1DurationSec / 60.0);
		verify(plotController).markSegmentEnd(SEGMENT_TWO_NAME, seg2End);
		verify(plotController).markSegmentEnd(SEGMENT_THREE_NAME, seg2End + seg3DurationSec / 60.0);
	}


	/* TRIGGER TESTS */

	@Test
	public void singleTimedTriggerInTimedSegment() {

		double targetInSec = 12;
		TriggerDescriptor trigger = getTrigger(TRIGGER_ONE_NAME, TIME, targetInSec, 0);

		double segmentDuration = 20;
		SegmentDescriptor segment = getSegment(SEGMENT_ONE_NAME, segmentDuration);

		segment.setTriggers(asList(trigger));

		getPlanPreview(false, segment).update();

		verify(plotController).plotTriggerPoints(TRIGGER_ONE_NAME, targetInSec / 60.0, 0.0);
	}

	@Test
	public void singleTimedTriggerInPositionBasedSegment() {

		prepareDriverProfile(linearSpace(0.0, 1.0, 3), discreteData(0.2, 0.7, 0.8));

		double targetInSec = 12;
		TriggerDescriptor trigger = getTrigger(TRIGGER_ONE_NAME, TIME, targetInSec, 0);

		SegmentDescriptor waitSegment = getSegment(SEGMENT_ONE_NAME, GREATER_THAN, 0.4);
		SegmentDescriptor activeSegment = getSegment(SEGMENT_TWO_NAME, GREATER_THAN, 0.79);
		activeSegment.setTriggers(asList(trigger));

		getPlanPreview(waitSegment, activeSegment).update();

		/* Q: What are the coordinates of the trigger?
		 * A:
		 *  x is activeSegment's start time + targetInSec
		 *  activeSegment starts sometime between the first two data points
		 *  this line has:
		 *  	m = 0.5 / 0.5 = 1.0;
		 *  	c = y - mx = 0.7 - 1 * 0.5 = 0.2
		 *  so activeSegment's start time = (0.4-0.2)/1 = 0.2
		 *  so x = 0.2 + targetInSec / 60.0
		 */
		double triggerX = 0.4;

		/*
		 * we are still in the same line
		 * so triggerY = triggerX + 0.2
		 */
		double triggerY = 0.6;

		verify(plotController).plotTriggerPoints(TRIGGER_ONE_NAME, triggerX, triggerY);
	}

	@Test
	public void repeatingTimedTriggerInPositionBasedSegment() {
		prepareDriverProfile(linearSpace(0.0, 1.0, 3), discreteData(0.2, 0.7, 0.8));

		double interval = 12;
		TriggerDescriptor trigger = getTrigger(TRIGGER_ONE_NAME, TIME, interval);

		SegmentDescriptor waitSegment = getSegment(SEGMENT_ONE_NAME, GREATER_THAN, 0.4);
		SegmentDescriptor activeSegment = getSegment(SEGMENT_TWO_NAME, GREATER_THAN, 0.79);
		activeSegment.setTriggers(asList(trigger));

		double[] expectedX = new double[3];
		double[] expectedY = new double[3];

		/* the first point is as the test above */
		expectedX[0] = 0.4;
		expectedY[0] = 0.6;

		/* since this is just repeating time, x[1], x[2] are trivial */
		expectedX[1] = 0.6;
		expectedX[2] = 0.8;

		/* These lie on the second line, which has
		 * m = (0.8-0.7)/(1-0.5) = 0.2; c = 0.8 - 0.2 * 1 = 0.6
		 *
		 * y[1] = 0.2 * x[1] + 0.6 = 0.72
		 * y[2] = 0.2 * x[2] + 0.6 = 0.76
		 */

		expectedY[1] = 0.72;
		expectedY[2] = 0.76;

		getPlanPreview(waitSegment, activeSegment).update();

		verify(plotController).plotTriggerPoints(eq(TRIGGER_ONE_NAME), xCoordinates.capture(), yCoordinates.capture());

		@SuppressWarnings("unchecked")
		List<Double> x = (List<Double>) xCoordinates.getValue();
		@SuppressWarnings("unchecked")
		List<Double> y = (List<Double>) yCoordinates.getValue();

		assertThat(x.size(), is(3));

		for (int value = 0; value < 3; value ++) {
			assertThat(x.get(value), is(closeTo(expectedX[value], ERROR)));
			assertThat(y.get(value), is(closeTo(expectedY[value], ERROR)));
		}

	}

	/*
	 * For single position-based triggers, we need to consider several scenarios.
	 * we should mark target - tolerance for increasing signals
	 * and target + tolerance on decreasing signals
	 */

	@Test
	public void singlePositionTriggerIncreasingSignal() {

		// easy maths: single line with m = 1, c = 0
		prepareDriverProfile(discreteData(0.0, 1.0), discreteData(0.0, 1.0));

		double target = 0.4;
		double tolerance = 0.01;

		TriggerDescriptor trigger = getTrigger(TRIGGER_ONE_NAME, POSITION, target, tolerance);
		SegmentDescriptor segment = getSegment(SEGMENT_ONE_NAME, 60);
		segment.setTriggers(asList(trigger));

		getPlanPreview(segment).update();

		verify(plotController).plotTriggerPoints(TRIGGER_ONE_NAME, target-tolerance, target-tolerance);
	}

	@Test
	public void singlePositionTriggerDecreasingSignal() {

		// easy maths: single line with m = -1, c = 1
		prepareDriverProfile(discreteData(0.0, 1.0), discreteData(1.0, 0.0));
		double m = -1.0;
		double c = 1.0;

		double target = 0.4;
		double tolerance = 0.01;

		TriggerDescriptor trigger = getTrigger(TRIGGER_ONE_NAME, POSITION, target, tolerance);
		SegmentDescriptor segment = getSegment(SEGMENT_ONE_NAME, 60);
		segment.setTriggers(asList(trigger));

		getPlanPreview(segment).update();

		verify(plotController).plotTriggerPoints(eq(TRIGGER_ONE_NAME), xCoordinates.capture(), yCoordinates.capture());

		assertThat((double) xCoordinates.getValue(), is(closeTo((target+tolerance - c)/m, ERROR)));
		assertThat((double) yCoordinates.getValue(), is(closeTo(target+tolerance, ERROR)));
	}

	@Test
	public void singlePositionBasedTriggerDecreasingSignal() {

		prepareDriverProfile(linearSpace(0.0, 1.0, 3), discreteData(4.0, 3.4, -1.2));

		double target = 0.5;
		double tolerance = 0.005;
		TriggerDescriptor trigger = getTrigger(TRIGGER_ONE_NAME, POSITION, target, tolerance);

		SegmentDescriptor segment = getSegment(SEGMENT_ONE_NAME, 60);
		segment.setTriggers(asList(trigger));

		getPlanPreview(segment).update();

		// we're approaching our range from a more positive value,
		// so first hit should be target + tolerance
		double expectedY = target + tolerance;

		/* find trigger's x
		 * must interpolate line between 2nd & 3rd points, characterised by: */
		double m = (-1.2 - 3.4) / 0.5;
		double c = -1.2 - m;

		// so then
		double expectedX = (expectedY - c) / m;
		verify(plotController).plotTriggerPoints(eq(TRIGGER_ONE_NAME), xCoordinates.capture(), yCoordinates.capture());
		assertThat((double) xCoordinates.getValue(), is(closeTo(expectedX, ERROR)));
		assertThat((double) yCoordinates.getValue(), is(closeTo(expectedY, ERROR)));
	}

	@Test
	public void singlePositionBasedTriggerInSegmentBetweenDataPoints() {

		prepareDriverProfile(linearSpace(0.0, 0.6, 4), discreteData(0.0, 1.1, 2.0, 0.2));

		// we want a single trigger when signal = 1.45, between 2nd and 3rd points
		double target = 1.45;
		double tolerance = 0.005;
		TriggerDescriptor trigger = getTrigger(TRIGGER_ONE_NAME, POSITION, target, tolerance);

		SegmentDescriptor innactive = getSegment(SEGMENT_ONE_NAME, GREATER_THAN, 1.2);
		SegmentDescriptor active = getSegment(SEGMENT_TWO_NAME, LESS_THAN, 0.201); // whatever, until the end
		active.setTriggers(asList(trigger));


		// the line in question is characterised by:
		double m = (2-1.1)/0.2;
		double c = 2-m*0.4;

		// increasing signal so we expect to find (target - tolerance) first
		double expectedY = target - tolerance;
		// so expectedX:
		double expectedX = (expectedY - c) / m;

		getPlanPreview(innactive, active).update();

		verify(plotController).plotTriggerPoints(eq(TRIGGER_ONE_NAME), xCoordinates.capture(), yCoordinates.capture());
		assertThat((double) xCoordinates.getValue(), is(closeTo(expectedX, ERROR)));
		assertThat((double) yCoordinates.getValue(), is(closeTo(expectedY, ERROR)));
	}

	/* VALIDATION */

	@Test
	public void timeBasedSegmentWithZeroWidth() {
		SegmentDescriptor segment = getSegment(SEGMENT_ONE_NAME, 0.0);
		getPlanPreview(false, segment).update();

		verify(plotController).flag(SEGMENT_ONE_NAME, PlanPreviewer.ZERO_WIDTH_SEGMENT_MSG, 0.0);
	}

	@Test
	public void positionBasedSegmentWithZeroWidth() {
		prepareDriverProfile(discreteData(0.0, 1.0), discreteData(0.0, 1.0));

		double waitInSec = 30;
		SegmentDescriptor spacer = getSegment(SEGMENT_ONE_NAME, waitInSec);
		SegmentDescriptor zeroWidthSegment = getSegment(SEGMENT_TWO_NAME, LESS_THAN, 1.0);

		getPlanPreview(spacer, zeroWidthSegment).update();

		verify(plotController).flag(SEGMENT_TWO_NAME, PlanPreviewer.ZERO_WIDTH_SEGMENT_MSG, waitInSec / 60.0);
	}

	@Test
	public void infiniteSegment() {
		prepareDriverProfile(discreteData(0.0, 1.0), discreteData(0.0, 1.0));
		double delayInSec = 45;
		SegmentDescriptor delaySegment = getSegment(SEGMENT_ONE_NAME, delayInSec);
		SegmentDescriptor infiniteSegment = getSegment(SEGMENT_ONE_NAME, GREATER_THAN, 5.0);
		getPlanPreview(delaySegment, infiniteSegment).update();
		verify(plotController).flag(SEGMENT_ONE_NAME, PlanPreviewer.INFINITE_SEGMENT_MSG, delayInSec / 60.0);
	}

	private Dataset linearSpace(double start, double stop, int length) {
		return DatasetFactory.createLinearSpace(DoubleDataset.class, start, stop, length);
	}

	private Dataset discreteData(double... values) {
		return DatasetFactory.createFromObject(values);
	}

	private void prepareDriverProfile(Dataset x, Dataset y) {
		DriverModel model = mock(DriverModel.class);
		x.setName("time");
		y.setName(SIGNAL_NAME);
		when(model.getPlottableDatasets()).thenReturn(asList(x, y));
		when(experimentService.getDriverProfile(eq(DRIVER_NAME), eq(DRIVER_PROFILE_NAME), any()))
			.thenReturn(model);
	}

	private TriggerDescriptor getTrigger(String name, SignalSource source, double target, double tolerance) {
		TriggerDescriptor trigger = getTrigger(name, source);
		trigger.setExecutionPolicy(ExecutionPolicy.SINGLE);
		trigger.setTarget(target);
		trigger.setTolerance(tolerance);
		return trigger;
	}

	private TriggerDescriptor getTrigger(String name, SignalSource source, double interval) {
		TriggerDescriptor trigger = getTrigger(name, source);
		trigger.setExecutionPolicy(ExecutionPolicy.REPEATING);
		trigger.setInterval(interval);
		return trigger;
	}

	private TriggerDescriptor getTrigger(String name, SignalSource source) {
		TriggerDescriptor trigger = new TriggerDescriptor();
		trigger.setName(name);
		trigger.setSignalSource(source);
		trigger.setSampleEnvironmentVariableName(SIGNAL_NAME);
		return trigger;
	}

	/**
	 * Create time-based segment; duration in seconds
	 */
	private SegmentDescriptor getSegment(String name, double duration) {
		SegmentDescriptor segment = new SegmentDescriptor();
		segment.setName(name);
		segment.setSignalSource(TIME);
		segment.setDuration(duration);
		return segment;
	}

	/**
	 * Create position-based segment
	 */
	private SegmentDescriptor getSegment(String name, Inequality inequality, double inequalityArgument) {
		SegmentDescriptor segment = new SegmentDescriptor();
		segment.setName(name);
		segment.setSignalSource(POSITION);
		segment.setSampleEnvironmentVariableName(SIGNAL_NAME);
		segment.setInequality(inequality);
		segment.setInequalityArgument(inequalityArgument);
		return segment;
	}

	private ExperimentPlanBean getPlanBean(boolean useDriver, SegmentDescriptor... segments) {
		ExperimentPlanBean planBean = new ExperimentPlanBean();
		if (useDriver) {
			DriverBean driverBean = new DriverBean();
			driverBean.setDriver(DRIVER_NAME);
			driverBean.setProfile(DRIVER_PROFILE_NAME);
			planBean.setDriverBean(driverBean);
		}
		planBean.setSegments(asList(segments));
		return planBean;
	}

	private PlanPreviewer getPlanPreview(SegmentDescriptor... segments) {
		return getPlanPreview(true, segments);
	}

	private PlanPreviewer getPlanPreview(boolean useDriver, SegmentDescriptor... segments) {
		return new PlanPreviewer(getPlanBean(useDriver, segments), plotController);
	}


}
