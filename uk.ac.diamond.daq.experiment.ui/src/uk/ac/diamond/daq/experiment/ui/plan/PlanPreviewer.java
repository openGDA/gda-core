package uk.ac.diamond.daq.experiment.ui.plan;

import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.STRETCH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation.LineStyle;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.ExperimentException;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

/**
 * Given a driver profile, we may be able to predict the execution of an experiment plan's segments and triggers.
 * This can only occur when segments and triggers depend on the demanded signals of the driver (or the plan is purely
 * time-based).
 * <p>   
 * Aside from offering a visual confirmation to the user that they have defined their plan correctly,
 * this prediction gives us an opportunity to do further validation of the plan. We are now able to mark e.g. a trigger
 * whose position-based condition will not be met within the duration of its parent segment.
 */
public class PlanPreviewer {
	
	private static final Logger logger = LoggerFactory.getLogger(PlanPreviewer.class);
	
	private IPlottingSystem<Composite> plot;
	private ExperimentPlanBean planBean;
	
	private ExperimentService experimentService;
	
	private Composite preview;
	
	private Dataset timeDataset;
	private Dataset yDataset;
	
	private Label warning;
	
	private List<IAnnotation> segmentAnnotations = new ArrayList<>();
	
	public PlanPreviewer(ExperimentService experimentService, ExperimentPlanBean bean, Composite composite) {
		this.experimentService = experimentService;
		this.planBean = bean;
		
		preview = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(preview);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(preview);
		
		createPlottingSystem();
		
		warning = new Label(preview, SWT.NONE);
		STRETCH.applyTo(warning);
		
		// For now, we have a button to manually trigger a whole new prediction. 
		Button updateButton = new Button(preview, SWT.NONE);
		updateButton.setText("Update preview");
		STRETCH.applyTo(updateButton);
		updateButton.addListener(SWT.Selection, event -> update());
	}
	
	public void update() {
		warning.setVisible(false);
		plot.clear();
		prepareBaseDatasets();
		plotSegments();
		plot.setTitle("Predicted outcome");
	}
	
	private void createPlottingSystem() {
		try {
			plot = PlottingFactory.createPlottingSystem();
			plot.createPlotPart(preview, "Preview", null, PlotType.XY, null);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(plot.getPlotComposite());
		} catch (Exception e) {
			new Label(preview, SWT.NONE).setText("Preview cannot be displayed");
		}
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
	}
	
	private void plotBaseDatasets(Dataset x, Dataset y) {
		x.setName("Time (min)");
		timeDataset = x;
		yDataset = y;
		plot.createPlot1D(timeDataset, Arrays.asList(yDataset), null);
	}
	
	private void plotDriverModel() {
		DriverModel model = experimentService.getDriverProfile(planBean.getExperimentDriverName(), planBean.getExperimentDriverProfile(), "");
		
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
		Dataset pseudoSecondDimension = DatasetFactory.createFromObject(new double[] {0., 0.});
		pseudoSecondDimension.setName("Time");
		plotBaseDatasets(x, pseudoSecondDimension);
		IAxis pseudoAxis = plot.getSelectedYAxis();
		pseudoAxis.setVisible(false);
	}
	
	/**
	 * although they may be described in terms of POSITION as well as TIME,
	 * segments divide the plan sequentially i.e. in TIME
	 * 
	 * we place a vertical annotation at the time when we expect the segment to end 
	 */
	private void plotSegments() {
		
		if (!planBean.getSegments().stream().allMatch(this::canBePreviewed)) {
			logger.info("Plan cannot be previewed");
			return;
		}
		
		segmentAnnotations.forEach(annot -> Display.getDefault().syncExec(() -> plot.removeAnnotation(annot)));
		plot.repaint();
		segmentAnnotations.clear();
		double nextSegmentStartTime = 0;
		
		for (SegmentRequest segment : planBean.getSegments()) {
			nextSegmentStartTime = preview(segment, nextSegmentStartTime);
		}
		
		plot.repaint();
	}
	
	private double preview(SegmentRequest segment, double segmentStart) {
		double segmentEnd = 0;
		switch (segment.getSignalSource()) {
		case TIME:
			segmentEnd = segmentStart + segment.getDuration() / 60.0;
			break;
		
		case POSITION:
			int startingIndex = getStartingIndex(segmentStart);
			segmentEnd = segmentEndTime(segmentStart, startingIndex, segment);

			break;

		default:
			break;
		}
		
		annotateSegmentEnd(segment.getName(), segmentEnd);
		plotTriggerPoints(segment.getTriggerRequests(), segmentStart, segmentEnd);
		
		return segmentEnd;
	}
	
	private void annotateSegmentEnd(String segmentName, double xPos) {
		try {
			IAnnotation segmentAnnotation = plot.createAnnotation("Segment end: " + segmentName);
			segmentAnnotation.setLocation(xPos, 0);
			segmentAnnotation.setShowName(true);
			segmentAnnotation.setShowPosition(false);
			segmentAnnotation.setLineStyle(LineStyle.UP_DOWN);
			Display.getDefault().syncExec(()->plot.addAnnotation(segmentAnnotation));
			segmentAnnotations.add(segmentAnnotation);
			
		} catch (Exception e) {
			throw new ExperimentException(e);
		}
	}
	
	private Object[] getTriggerCoordinates(TriggerRequest trigger, double segmentStartTime, double segmentEndTime) {
		switch (trigger.getSignalSource()) {
		case POSITION:
			return getPositionBasedTriggerCoordinates(trigger, segmentStartTime, segmentEndTime);
		case TIME:
			return getTimeBasedTriggerCoordinates(trigger, segmentStartTime, segmentEndTime);
		default:
			throw new IllegalArgumentException("Unknown signal source");
		}
	}
	
	private Object[] getTimeBasedTriggerCoordinates(TriggerRequest trigger, double segmentStartTime, double segmentEndTime) {
		switch (trigger.getExecutionPolicy()) {
		case SINGLE:
			double x = segmentStartTime + trigger.getTarget() / 60.0;
			if (x > segmentEndTime) {
				reportProblem(getTriggerOutsideSegmentMessage(trigger.getName()));
			}
			int secondEnclosingIndex = getStartingIndex(x);
			double y = getInterpolatedY(secondEnclosingIndex, x);
			return new Object[] {x, y};
		
		case REPEATING:
			int numberOfTriggers = (int) Math.floor((segmentEndTime-segmentStartTime)*60/trigger.getInterval());
			
			double[] xs = new double[numberOfTriggers];
			double[] ys = new double[numberOfTriggers];
			for (int index = 0 ; index < numberOfTriggers ; index ++) {
				xs[index] = segmentStartTime + (index + 1) * trigger.getInterval() / 60.0;
				ys[index] = getInterpolatedY(getStartingIndex(xs[index]), xs[index]);
			}
			return new Object[] {xs, ys};
			
		default:
			throw new IllegalArgumentException("Trigger has unknown execution policy");	
		}
	}
	
	private String getTriggerOutsideSegmentMessage(String triggerName) {
		return "Trigger " + triggerName + " would occur outside its containing segment; will not trigger";
	}

	private Object[] getPositionBasedTriggerCoordinates(TriggerRequest trigger, double startTime, double endTime) {
		
		int firstRelevantIndex = getStartingIndex(startTime);
		
		if (trigger.getExecutionPolicy() == ExecutionPolicy.SINGLE) {
			
			/** We need to find the two encapsulating data points and interpolate */
			
			Double previous = yDataset.getElementDoubleAbs(firstRelevantIndex);
			
			for (int index = firstRelevantIndex+1; index < yDataset.getSize(); index ++) {
				double currentPoint = yDataset.getElementDoubleAbs(index);
				if (triggerTargetLiesInLine(trigger, previous, currentPoint)) {

					double tx = new Line(index-1).getX(trigger.getTarget());
					if (tx < startTime || tx > endTime) {
						reportProblem(getTriggerOutsideSegmentMessage(trigger.getName()));
					}
					return new Object[] {tx, trigger.getTarget()};
				}
				previous = currentPoint;
			}
		
		} else if (trigger.getExecutionPolicy() == ExecutionPolicy.REPEATING) {
			
			/*** 
			 * 
			 *  We need to traverse yDataset from interpolated value at segment start,
			 *  to the interpolated value at segment end.
			 *  
			 *  At each point we ask: is the difference between our current position and
			 *  the next point equal to or greater than the trigger interval? if so,
			 *  we increase/decrease by the trigger interval (depending on whether the signal
			 *  is increasing or decreasing) until we get to the next point in the dataset 
			 *  
			 ***/
			
			List<Double> x = new ArrayList<>();
			List<Double> y = new ArrayList<>();
			
			// what's the signal at the start of the segment?
			double signal = getInterpolatedY(startTime);
			
			int finalRelevantIndex = getStartingIndex(endTime);
			
			RepeatedTriggerLocator locator = new RepeatedTriggerLocator(trigger.getInterval());
			
			for (int index = firstRelevantIndex+1; index < finalRelevantIndex + 1; index ++) {
				locator.findInLine(signal, yDataset.getElementDoubleAbs(index), index - 1);
				x.addAll(locator.getXCoordinates());
				y.addAll(locator.getYCoordinates());
				signal = locator.getLastKnownTrigger();				
			}
			
			// what's the signal at the end of the segment?
			double finalSignal = getInterpolatedY(endTime);
			
			// now interpolate the section between y(finalRelevantIndex) and finalSignal
			locator.findInLine(signal, finalSignal, finalRelevantIndex);
			x.addAll(locator.getXCoordinates());
			y.addAll(locator.getYCoordinates());

			return new Object[] {x, y};
		}

		throw new IllegalArgumentException("Trigger has unknown execution policy");

	}
	
	/**
	 * Does the given trigger's target +/- tolerance lie somewhere between the two given signals?
	 */
	private boolean triggerTargetLiesInLine(TriggerRequest trigger, double previous, double currentPoint) {
		double lo = trigger.getTarget() - trigger.getTolerance();
		double hi = trigger.getTarget() + trigger.getTolerance();
		return (previous < currentPoint && previous < lo && currentPoint > lo) ||
				(previous > currentPoint && previous > hi && currentPoint < hi);
	}

	private class RepeatedTriggerLocator {
		private List<Double> x;
		private List<Double> y;
		private double lastKnownTrigger;
		private double triggerInterval;
		
		RepeatedTriggerLocator(double triggerInterval) {
			this.triggerInterval = triggerInterval;
		}
		
		/**
		 * find the coordinates of the predicted trigger points from lastKnownTrigger to finalSignal,
		 * interpolating linearly from data[firstIndex] to data[firstIndex+1]
		 */
		void findInLine(double lastKnownTrigger, double finalSignal, int firstIndex) {
			
			x = new ArrayList<>();
			y = new ArrayList<>();
			int direction = lastKnownTrigger < finalSignal ? 1 : -1;
			
			Line line = new Line(firstIndex);
			
			while (Math.abs(finalSignal - lastKnownTrigger) >= triggerInterval) {
				 lastKnownTrigger += triggerInterval * direction;
				 y.add(lastKnownTrigger);
				 x.add(line.getX(lastKnownTrigger));
			}
			
			this.lastKnownTrigger = lastKnownTrigger;
		}
		
		List<Double> getXCoordinates() {
			return x;
		}
		
		List<Double> getYCoordinates() {
			return y;
		}
		
		double getLastKnownTrigger() {
			return lastKnownTrigger;
		}
	}

	private void plotTriggerPoints(List<TriggerRequest> triggers, double startTime, double endTime) {
		for (TriggerRequest trigger : triggers) {
			if (!canBePreviewed(trigger)) continue;
			Object[] triggerCoordinates = getTriggerCoordinates(trigger, startTime, endTime);
			plotTriggerData(trigger.getName(), triggerCoordinates[0], triggerCoordinates[1]);
		}
	}

	private void plotTriggerData(String triggerName, Object x, Object y) {
		// If trigger conditions never met, x or y could be null
		if (x == null || y == null) {
			return;
		}
		ILineTrace trace = plot.createLineTrace(triggerName);
		trace.setData(DatasetFactory.createFromObject(x), DatasetFactory.createFromObject(y));
		trace.setLineWidth(-10);
		trace.setPointSize(8);
		trace.setPointStyle(PointStyle.FILLED_CIRCLE);
		plot.repaint();
		Display.getDefault().syncExec(() -> plot.addTrace(trace));
	}
	
	/**
	 * Returns the time when a segment's LimitCondition will be met
	 * 
	 * Tests start time first, then iterates through yDataset
	 * to find the line around the inequality argument, which is interpolated.
	 */
	private double segmentEndTime(double segmentStart, int startingIndex, SegmentRequest segment) {
		LimitCondition condition = segment.getInequality().getLimitCondition(segment.getInequalityArgument()); 
		
		// first of all test our current point.
		double signalAtStartingPoint = new Line(startingIndex).getY(segmentStart);
		if (condition.limitReached(signalAtStartingPoint)) {
			reportProblem("Segment '" + segment.getName() + "' predicted to have zero duration!");
			return segmentStart;
		}
		
		for (int index = startingIndex + 1; index < yDataset.getSize(); index ++) {
			if (condition.limitReached(yDataset.getElementDoubleAbs(index)))  {
				return new Line(index - 1).getX(segment.getInequalityArgument());
			}
		}
		
		String infiniteSegmentMessage = "Segment '" + segment.getName() + "' predicted to run indefinitely!";
		reportProblem(infiniteSegmentMessage);
		throw new IllegalArgumentException(infiniteSegmentMessage);
	}
	
	private double getInterpolatedY(int index, double tx) {
		
		// do we need to interpolate?
		int i = DatasetUtils.findIndexEqualTo(timeDataset, tx);
		if (i < timeDataset.getSize()) {
			// no we don't
			return yDataset.getElementDoubleAbs(i);
		}
		
		// yes we do (most likely)
		return new Line(index).getY(tx);
	}
	
	/**
	 * This class linearly interpolates two data points
	 * to get some intermediate value
	 */
	private class Line {
		
		private final double x0;
		private final double x1;
		private final double y0;
		private final double y1;
		
		private final double m;
		private final double c;
		
		public Line(double x0, double x1, double y0, double y1) {
			this.x0 = x0;
			this.x1 = x1;
			this.y0 = y0;
			this.y1 = y1;
			
			m = calculateGradient();
			c = calculateIntercept();
		}
		
		/**
		 * The line will be constructed from (x[i0], y[i0]) to (x[i0+1], y[i0+1]).
		 */
		public Line(int i0) {
			this(timeDataset.getElementDoubleAbs(i0),
				timeDataset.getElementDoubleAbs(i0+1),
				yDataset.getElementDoubleAbs(i0),
				yDataset.getElementDoubleAbs(i0+1));
		}
		
		private double calculateGradient() {
			return (y1-y0) / (x1-x0);
		}
		
		private double calculateIntercept() {
			return y0 - m * x0;
		}
		
		/**
		 * Get interpolated Y given X
		 */
		double getY(double x) {
			return m * x + c;
		}
		
		/**
		 * Get interpolated X given Y
		 */
		double getX(double y) {
			return (y - c) / m;
		}
	}
	
	/**
	 * get the y value at a given x
	 */
	private double getInterpolatedY(double xc) {
		int index = DatasetUtils.findIndexEqualTo(timeDataset, xc);
		if (index < timeDataset.getSize()) {
			return yDataset.getElementDoubleAbs(index);
		}
		return getInterpolatedY(getStartingIndex(xc), xc);
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
	
	
	private void reportProblem(String problem) {
		warning.setText(problem);
		warning.setVisible(true);
	}

}
