package uk.ac.diamond.daq.experiment.ui.plan.preview;

import static uk.ac.diamond.daq.experiment.api.Services.getExperimentService;

import java.util.List;
import java.util.Optional;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

/**
 * Given a driver profile, we may be able to predict the execution of an experiment plan's segments and triggers.
 * This can only occur when segments and triggers depend on the demanded signals of the driver (or the plan is purely
 * time-based).
 * <p>   
 * Aside from offering a visual confirmation to the user that they have defined their plan correctly,
 * this prediction gives us an opportunity to do further validation of the plan. We are now able to mark e.g. a segment
 * whose position-based condition will not be met according to the driver profile
 */
public class PlanPreviewer {
	
	public static final String ZERO_WIDTH_SEGMENT_MSG = "This segment has zero width";
	public static final String INFINITE_SEGMENT_MSG = "This segment has no predicted end";
	
	private static final Logger logger = LoggerFactory.getLogger(PlanPreviewer.class);	
	
	private PlotController plotController;
	private ExperimentPlanBean planBean;
	
	private Dataset timeDataset;
	private Dataset yDataset;
	private LinearInterpolator interpolator;
	
	private TriggerLocatorFactory triggerLocatorFactory;
	
	public PlanPreviewer(ExperimentPlanBean planBean, PlotController plotter) {
		this.plotController = plotter;
		this.planBean = planBean;
	}
	
	public void update() {
		plotController.clear();
		prepareBaseDatasets();
		plotSegments();
	}
	
	/**
	 * What are base datasets? either:
	 *  driver profile, or
	 *  a 2-point dataset if purely time-based
	 */
	private void prepareBaseDatasets() {
		if (planBean.isDriverUsed()) {
			plotDriverModel();
		} else if (planBean.getSegments().stream().allMatch(segment -> segment.getSignalSource() == SignalSource.TIME)) {
			plotDurationBase();
		}
		
		triggerLocatorFactory = new TriggerLocatorFactory(timeDataset, yDataset);
	}
	
	private void plotBaseDatasets(Dataset x, Dataset y) {
		x.setName("Time (min)");
		timeDataset = x;
		if (y != null) {
			yDataset = y;
			plotController.createPlot(timeDataset, yDataset);
		} else {
			yDataset = DatasetFactory.createFromObject(new double[] {0, 0});
			plotController.createPlot(timeDataset);
		}
		interpolator = new LinearInterpolator(timeDataset, yDataset);
	}
	
	private void plotDriverModel() {
		DriverModel model = getExperimentService().getDriverProfile(planBean.getDriverBean().getDriver(), planBean.getDriverBean().getProfile(), "");
		List<Dataset> data = model.getPlottableDatasets();
		plotBaseDatasets(data.get(0), data.get(1));
	}
	
	/**
	 * If the plan is purely time-based, then we plot time vs 0,
	 * hacking the plot so it looks 1D...
	 */
	private void plotDurationBase() {
		double duration = planBean.getSegments().stream().mapToDouble(SegmentRequest::getDuration).sum() / 60.0;
		Dataset x = DatasetFactory.createFromObject(new double[] {0.0, duration});
		plotBaseDatasets(x, null);
	}
	
	private void plotSegments() {
		if (!planBean.getSegments().stream().allMatch(this::canBePreviewed)) {
			logger.info("Plan cannot be previewed");
			return;
		}
		
		double nextSegmentStartTime = 0;
		
		for (SegmentRequest segment : planBean.getSegments()) {
			nextSegmentStartTime = preview(segment, nextSegmentStartTime);
		}
	}
	
	private double preview(SegmentRequest segment, double segmentStart) {
		Optional<Double> segmentEnd;
		switch (segment.getSignalSource()) {
		case TIME:
			if (segment.getDuration() == 0.0) {
				plotController.flag(segment.getName(), ZERO_WIDTH_SEGMENT_MSG, segmentStart);
			}
			segmentEnd = Optional.of(segmentStart + segment.getDuration() / 60.0);
			break;
		
		case POSITION:
			int startingIndex = getStartingIndex(segmentStart);
			segmentEnd = segmentEndTime(segmentStart, startingIndex, segment);
			break;

		default:
			segmentEnd = Optional.empty();
			break;
		}
		
		if (segmentEnd.isPresent()) {
			plotController.markSegmentEnd(segment.getName(), segmentEnd.get());
			plotTriggerPoints(segment.getTriggerRequests(), segmentStart, segmentEnd.get());
			return segmentEnd.get();
		}
		
		return segmentStart;
	}
	
	/**
	 * Returns an optional containing the time when a segment's LimitCondition will be met
	 * 
	 * Tests start time first, then iterates through yDataset
	 * to find the line around the inequality argument, which is interpolated.
	 */
	private Optional<Double> segmentEndTime(double segmentStart, int startingIndex, SegmentRequest segment) {
		LimitCondition condition = segment.getInequality().getLimitCondition(segment.getInequalityArgument()); 
		
		// first of all test our current point.
		double signalAtStartingPoint = interpolator.getY(segmentStart);
		if (condition.limitReached(signalAtStartingPoint)) {
			plotController.flag(segment.getName(), ZERO_WIDTH_SEGMENT_MSG, segmentStart);
			return Optional.of(segmentStart);
		}
		
		for (int index = startingIndex + 1; index < yDataset.getSize(); index ++) {
			if (condition.limitReached(yDataset.getElementDoubleAbs(index)))  {
				return Optional.of(interpolator.getX(segment.getInequalityArgument(), index-1));
			}
		}
		
		plotController.flag(segment.getName(), INFINITE_SEGMENT_MSG, segmentStart);
		return Optional.empty();
	}
	
	private void plotTriggerPoints(List<TriggerRequest> triggers, double startTime, double endTime) {
		
		for (TriggerRequest trigger : triggers) {
			if (!canBePreviewed(trigger)) continue;
			
			TriggerLocator<?> locator = triggerLocatorFactory.getTriggerLocator(trigger);
			locator.search(startTime, endTime);
			
			locator.getX().ifPresent(x -> 
				locator.getY().ifPresent(y -> 
					plotController.plotTriggerPoints(trigger.getName(), x, y)
				)
			);
		}
	}

	/**
	 * Find the index of the X data point just below the given argument
	 */
	private int getStartingIndex(double target) {
		return DatasetUtils.findIndexGreaterThan(timeDataset, target) - 1;
	}
	
	private boolean canBePreviewed(SegmentRequest segment) {
		if (segment.getSignalSource() == SignalSource.TIME) return true;
		return yDataset.getName().equalsIgnoreCase(segment.getSampleEnvironmentVariableName());
	}
	
	/**
	 * It is assumed that the containing segment can be previewed
	 */
	private boolean canBePreviewed(TriggerRequest trigger) {
		if (trigger.getSignalSource() == SignalSource.TIME) return true;
		return yDataset.getName().equalsIgnoreCase(trigger.getSampleEnvironmentVariableName());
	}

}
