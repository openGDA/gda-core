package uk.ac.diamond.daq.experiment.ui.plan;

import static uk.ac.diamond.daq.experiment.api.plan.event.EventConstants.EXPERIMENT_PLAN_TOPIC;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation.LineStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.event.PlanStatusBean;
import uk.ac.diamond.daq.experiment.api.plan.event.SegmentRecord;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerRecord;

public class PlanProgressPlotView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(PlanProgressPlotView.class);
	
	private ISubscriber<IBeanListener<PlanStatusBean>> subscriber;
	private static IEventService eventService;
	private ExperimentService experimentService;

	private PlanStatusBean activePlan;
	
	private IPlottingSystem<Composite> plottingSystem;
	private Text status;
	
	private Scannable signalSource;
	private Future<?> trajectoryJob;
	private DynamicTraceMaintainer trajectory;
	
	private List<IAnnotation> segmentAnnotations;
	private Map<String, DynamicTraceMaintainer> triggerPlots;
	
	@Override
	public void createPartControl(Composite parent) {
		try {
			createSubscriber();
		} catch (Exception e) {
			logger.error("Could not create subscriber, rendering this view useless. Giving up...", e);
			return;
		}
		
		experimentService = Finder.getInstance().findSingleton(ExperimentService.class);
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(composite);
		GridLayoutFactory.fillDefaults().applyTo(composite);
		
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Error creating plotting system", e);
			return;
		}
		
		plottingSystem.createPlotPart(composite, "Experiment plan", null, PlotType.XY, null);
		plottingSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		status = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER);
		status.setText("\n"); // initialise with enough height for two lines
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(status);
	}

	private void createSubscriber() throws URISyntaxException, EventException {
		Objects.requireNonNull(eventService);
		URI activeMqUri = new URI(LocalProperties.getActiveMQBrokerURI());
		subscriber = eventService.createSubscriber(activeMqUri, EXPERIMENT_PLAN_TOPIC);
		subscriber.addListener(event -> {
			final PlanStatusBean bean = event.getBean();
			if (activePlan == null || !activePlan.getUniqueId().equals(bean.getUniqueId())) {
				setNewPlanBean(bean);
			} else {
				activePlan.merge(bean);
			}
			updateView();
		});
	}
	
	private void setNewPlanBean(PlanStatusBean bean) {
		activePlan = bean;
		
		Display.getDefault().asyncExec(() -> {
			plottingSystem.clearAnnotations();
			plottingSystem.clearTraces();
		});
		
		if (bean.getDriverName() != null && bean.getDriverProfile() != null) {
			plotDriverProfile();
			initialiseAllThePlottingStuff();
			startTrajectoryJob();
		}
	}
	
	/**
	 * Records the position of the driver's main readout every 500ms and adds to plot
	 */
	private void startTrajectoryJob() {
		trajectoryJob = Async.scheduleAtFixedRate(this::addPointToTrajectory, 0, 500, TimeUnit.MILLISECONDS);
	}

	private void updateView() {
		if (activePlan.getStatus() == Status.RUNNING) {
			plotSegments();
		}
		plotTriggers();
		updatePlotTitleAndAxisLabel(activePlan.getName() + ": " + activePlan.getStatus().toString(), "Time (min)");
		if (activePlan.getStatus() == Status.COMPLETE && trajectoryJob != null && !trajectoryJob.isDone()) {
			trajectoryJob.cancel(true);
			try {
				IAnnotation planEndAnnotation = plottingSystem.createAnnotation("Plan end");
				planEndAnnotation.setLocation(getRelativeTimeInMinutes(activePlan.getSegments().get(activePlan.getSegments().size()-1).getEndTime()), getYPosition());
				planEndAnnotation.setLineStyle(LineStyle.UP_DOWN);
				planEndAnnotation.setShowPosition(false);
				Display.getDefault().syncExec(()->plottingSystem.addAnnotation(planEndAnnotation));
			} catch (Exception e) {
				logger.error("Blame Jake", e);
			}
		}
		updateStatus();
	}
	
	private void updateStatus() {
		if (activePlan != null) {
			String planInfo = "Plan: '" + activePlan.getName() + "'; started: " + formatTimestamp(activePlan.getSegments().get(0).getStartTime());
			String other = "";
			if (activePlan.getStatus() == Status.RUNNING) {
				SegmentRecord runningSegment = activePlan.getSegments().get(activePlan.getSegments().size()-1);
				other = "\nSegment: '" + runningSegment.getSegmentName() + "'; started: " + formatTimestamp(runningSegment.getStartTime());
			} else if (activePlan.getStatus() == Status.COMPLETE) {
				other = "; completed: " + formatTimestamp(activePlan.getSegments().get(activePlan.getSegments().size()-1).getEndTime());
			}
			final String updateText = planInfo + other;
			Display.getDefault().asyncExec(()->status.setText(updateText));
		}
	}
	
	private String formatTimestamp(long timestamp) {
		return DateFormat.getDateTimeInstance().format(new Date(timestamp));
	}
	
	private void updatePlotTitleAndAxisLabel(String title, String xAxisLabel) {
		Display.getDefault().asyncExec(() -> {
			plottingSystem.setTitle(title);
			plottingSystem.getSelectedXAxis().setTitle(xAxisLabel);
		});
	}
	
	private void initialiseAllThePlottingStuff() {
		IExperimentDriver driver = Finder.getInstance().find(activePlan.getDriverName());
		signalSource = Finder.getInstance().find(driver.getMainReadoutName());
		trajectory = new DynamicTraceMaintainer("actual trajectory", true);
		triggerPlots = new HashMap<>();
		segmentAnnotations = new ArrayList<>();
	}
	
	private void addPointToTrajectory() {
		trajectory.addPoint(Instant.now().toEpochMilli());
		trajectory.updateTrace();
	}
	
	private void plotDriverProfile() {
		List<DriverProfileSection> sections = experimentService.getDriverProfile(activePlan.getDriverName(),
										activePlan.getDriverProfile(), activePlan.getName()).getProfile();
		if (sections.isEmpty()) return;
		
		double[] x = new double[sections.size()+1];
		double[] y = new double[sections.size()+1];
		
		x[0] = 0;
		y[0] = sections.get(0).getStart();
		
		for (int i = 0; i < sections.size(); i++) {
			x[i+1] = sections.get(i).getDuration() + x[i];
			y[i+1] = sections.get(i).getStop();
		}
		
		final Dataset xDataset = DatasetFactory.createFromObject(x);
		final Dataset yDataset = DatasetFactory.createFromObject(y);
		
		ILineTrace trace = plottingSystem.createLineTrace("driver profile");
		trace.setData(xDataset, yDataset);
		
		Display.getDefault().asyncExec(()->plottingSystem.addTrace(trace));
	}

	
	private void plotSegments() {
		for (int i = segmentAnnotations.size(); i < activePlan.getSegments().size(); i++) {
			SegmentRecord segmentRecord = activePlan.getSegments().get(i);
			try {
				IAnnotation annotation = plottingSystem.createAnnotation("Segment: '" + segmentRecord.getSegmentName() + "'");
				annotation.setLocation(getRelativeTimeInMinutes(segmentRecord.getStartTime()), getYPosition());
				annotation.setShowName(true);
				annotation.setLineStyle(LineStyle.UP_DOWN);
				annotation.setShowPosition(false);
				segmentAnnotations.add(annotation);
				Display.getDefault().syncExec(()->plottingSystem.addAnnotation(annotation));
				plottingSystem.repaint();
			} catch (Exception e) {
				logger.error("Messed up the annotations", e);
			}
		}
	}
	
	private double getRelativeTimeInMinutes(long timestamp) {
		return (timestamp-activePlan.getStartTime()) / 1000.0 / 60.0;
	}
	
	private double getYPosition() {
		try {
			return (double) signalSource.getPosition();
		} catch (DeviceException e) {
			logger.error("Error reading scannable", e);
			return 0; // lol
		}
	}

	private class DynamicTraceMaintainer {
		private List<Double> x = new ArrayList<>();
		private List<Double> y = new ArrayList<>();
		
		private final ILineTrace trace;
		
		/**
		 * @param traceName
		 * @param line true to plot the line, false plots individual points instead
		 */
		public DynamicTraceMaintainer(String traceName, boolean line) {
			trace = plottingSystem.createLineTrace(traceName);
			if (line) {
				trace.setLineWidth(2);
			} else {
				trace.setLineWidth(-10);
				trace.setPointSize(8);
				trace.setPointStyle(PointStyle.FILLED_CIRCLE);
			}
			Display.getDefault().asyncExec(()->plottingSystem.addTrace(trace));
		}
		
		public void updateTrace() {
			trace.setData(DatasetFactory.createFromObject(x), DatasetFactory.createFromObject(y));
			plottingSystem.repaint();
		}
		
		public void addPoint(long timestamp) {
			x.add(getRelativeTimeInMinutes(timestamp));
			y.add(getYPosition());
		}
		
		public int getSize() {
			return x.size();
		}
	}
	
	private void plotTriggers() {
		for (TriggerRecord trigRec : activePlan.getTriggers()) {
			String name = trigRec.getTriggerName();
			if (!triggerPlots.containsKey(name)) {
				triggerPlots.put(name, new DynamicTraceMaintainer(name, false));
			}
			DynamicTraceMaintainer tp = triggerPlots.get(name);
			
			List<TriggerEvent> events = trigRec.getEvents();
			for (int i = tp.getSize(); i < events.size(); i++) {
				tp.addPoint(events.get(i).getTimestamp());
			}
			tp.updateTrace();
		}
	}

	@Override
	public void setFocus() {
		// leave focus well alone thank you
	}
	
	@Override
	public void dispose() {
		if (subscriber != null) {
			try {
				subscriber.disconnect();
			} catch (EventException e) {
				logger.error("Error disconnecting subscriber", e);
			}
		}
		if (trajectoryJob != null) {
			trajectoryJob.cancel(true);
		}
		super.dispose();
	}
	
	// OSGi use only!
	public void setEventService(IEventService eventService) {
		PlanProgressPlotView.eventService = eventService; // NOSONAR used by OSGi only (I hope...)
	}

}
