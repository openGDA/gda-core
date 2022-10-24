package uk.ac.diamond.daq.experiment.ui.plan;

import static uk.ac.diamond.daq.experiment.api.Services.getExperimentService;
import static uk.ac.diamond.daq.experiment.api.remote.EventConstants.EXPERIMENT_PLAN_TOPIC;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.event.PlanStatusBean;
import uk.ac.diamond.daq.experiment.api.plan.event.SegmentRecord;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerRecord;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;

public class PlanProgressPlotView extends ViewPart {

	public static final String ID = "uk.ac.diamond.daq.experiment.ui.plan.PlanProgressPlotView";
	private static final Logger logger = LoggerFactory.getLogger(PlanProgressPlotView.class);

	private ISubscriber<IBeanListener<PlanStatusBean>> subscriber;

	private PlanStatusBean activePlan;

	private IPlottingSystem<Composite> plottingSystem;

	private Scannable signalSource;
	private Future<?> trajectoryJob;
	private DynamicTraceMaintainer trajectory;

	private List<IAnnotation> segmentAnnotations;
	private Map<String, DynamicTraceMaintainer> triggerPlots;
	private Map<Scannable, DynamicTraceMaintainer> sevPlots;

	@Override
	public void createPartControl(Composite parent) {
		try {
			createSubscriber();
		} catch (Exception e) {
			logger.error("Could not create subscriber, rendering this view useless. Giving up...", e);
			return;
		}

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
	}

	private void createSubscriber() throws URISyntaxException, EventException {
		ClientRemoteServices remoteServices = SpringApplicationContextFacade.getBean(ClientRemoteServices.class);
		subscriber = remoteServices.createSubscriber(EXPERIMENT_PLAN_TOPIC);
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

		initialisePlanPlottingComponents();

		if (bean.getDriverName() != null && bean.getDriverProfile() != null) {
			initialiseDriverPlottingComponents();
		}

		setupSevPlotting();

		startTrajectoryJob();
	}


	private void initialisePlanPlottingComponents() {
		triggerPlots = new HashMap<>();
		segmentAnnotations = new ArrayList<>();
	}

	private void setupSevPlotting() {
		sevPlots = new HashMap<>();
		addAnyNewSevs();
	}

	private void addAnyNewSevs() {
		for (var name : knownSevs()) {
			Scannable sev = Finder.find(name);
			if (sev != null) {
				sevPlots.computeIfAbsent(sev, scannable -> new DynamicTraceMaintainer(scannable, true));
			}
		}
	}

	private Set<String> knownSevs() {
		if (activePlan == null) return Collections.emptySet();

		var sevs = activePlan.getSegments().stream()
					.map(SegmentRecord::getSampleEnvironmentName)
					.collect(Collectors.toSet());

		sevs.addAll(activePlan.getTriggers().stream()
					.map(TriggerRecord::getSampleEnvironmentName)
					.collect(Collectors.toSet()));

		return sevs;
	}

	private void initialiseDriverPlottingComponents() {
		plotDriverProfile();

		IExperimentDriver<? extends DriverModel> driver = Finder.find(activePlan.getDriverName());
		signalSource = Finder.find(driver.getMainReadoutName());
		trajectory = new DynamicTraceMaintainer(signalSource, true);
	}

	/**
	 * Records the position of the driver's main readout every 500ms and adds to plot
	 */
	private void startTrajectoryJob() {
		trajectoryJob = Async.scheduleAtFixedRate(this::addPointToTrajectory, 0, 500, TimeUnit.MILLISECONDS);
	}

	private void updateView() {
		plotSegments();
		plotTriggers();
		addAnyNewSevs();
		updatePlotTitleAndAxisLabel(activePlan.getName() + ": " + activePlan.getStatus().toString(), "Time (min)");
		if (activePlan.getStatus() == Status.COMPLETE && trajectoryJob != null && !trajectoryJob.isDone()) {
			trajectoryJob.cancel(true);
		}
	}

	private void updatePlotTitleAndAxisLabel(String title, String xAxisLabel) {
		Display.getDefault().asyncExec(() -> {
			plottingSystem.setTitle(title);
			plottingSystem.getSelectedXAxis().setTitle(xAxisLabel);
		});
	}

	private void addPointToTrajectory() {
		if (trajectory != null) {
			trajectory.addPoint(Instant.now().toEpochMilli());
			trajectory.updateTrace();
		}
		sevPlots.values().forEach(trace -> {
			trace.addPoint(Instant.now().toEpochMilli());
			trace.updateTrace();
		});
	}

	private void plotDriverProfile() {
		List<Dataset> plottableDatasets = getExperimentService().getDriverProfile(activePlan.getDriverName(), activePlan.getDriverProfile(), activePlan.getName()).getPlottableDatasets();

		final Dataset xDataset = plottableDatasets.get(0);
		final Dataset yDataset = plottableDatasets.get(1);

		ILineTrace trace = plottingSystem.createLineTrace("driver profile");
		trace.setData(xDataset, yDataset);

		Display.getDefault().asyncExec(()->plottingSystem.addTrace(trace));
	}


	private void plotSegments() {
		for (int i = segmentAnnotations.size(); i < activePlan.getSegments().size(); i++) {
			// we plot an annotation to visualise the start of every segment
			SegmentRecord segmentRecord = activePlan.getSegments().get(i);
			plotAnnotation("Segment: '" + segmentRecord.getSegmentName() + "'",
					getRelativeTimeInMinutes(segmentRecord.getStartTime()), getYPosition());
		}
		if (activePlan.getStatus() == Status.COMPLETE) {
			// we need a final annotation to represent the end of the experiment
			plotAnnotation("Plan end", getRelativeTimeInMinutes(activePlan.getSegments()
					.get(activePlan.getSegments().size()-1).getEndTime()), getYPosition());
		}
	}

	/**
	 * Plotted as a vertical line at the specified xPosition to visualise segment limits.
	 */
	private void plotAnnotation(String name, double xPosition, double yPosition) {
		try {
			IAnnotation annotation = plottingSystem.createAnnotation(name);
			annotation.setLocation(xPosition, yPosition);
			annotation.setShowName(true);
			annotation.setLineStyle(LineStyle.UP_DOWN);
			annotation.setShowPosition(false);
			segmentAnnotations.add(annotation);
			Display.getDefault().syncExec(() -> plottingSystem.addAnnotation(annotation));
			plottingSystem.repaint();
		} catch (Exception e) {
			logger.error("Error plotting annotation '{}'", name, e);
		}
	}

	private double getRelativeTimeInMinutes(long timestamp) {
		return (timestamp-activePlan.getStartTime()) / 1000.0 / 60.0;
	}

	private double getYPosition() {
		if (signalSource != null) {
			try {
				return (double) signalSource.getPosition();
			} catch (DeviceException e) {
				logger.error("Error reading scannable", e);
			}
		}
		return 0.0;
	}

	private class DynamicTraceMaintainer {
		private List<Double> x = new ArrayList<>();
		private List<Double> y = new ArrayList<>();

		private final ILineTrace trace;

		private final Optional<Scannable> scannable;

		/**
		 * Use this constructor for when the trace name is not scannable name
		 */
		public DynamicTraceMaintainer(String traceName, Scannable scannable, boolean line) {
			this.scannable = Optional.ofNullable(scannable);
			trace = plottingSystem.createLineTrace(traceName);
			if (line) {
				trace.setLineWidth(2);
			} else {
				trace.setLineWidth(-10);
				trace.setPointSize(8);
				trace.setPointStyle(PointStyle.FILLED_CIRCLE);
			}
			Display.getDefault().asyncExec(() -> plottingSystem.addTrace(trace));
		}

		/**
		 * @param scannable scannable whose position we track
		 * @param line true to plot the line, false plots individual points instead
		 */
		public DynamicTraceMaintainer(Scannable scannable, boolean line) {
			this(scannable.getName(), scannable, line);
		}

		public void updateTrace() {
			trace.setData(DatasetFactory.createFromObject(x), DatasetFactory.createFromObject(y));
			plottingSystem.repaint();
		}

		public void addPoint(long timestamp) {
			x.add(getRelativeTimeInMinutes(timestamp));
			double position = scannable.map(this::getScannablePosition).orElse(0.0);
			y.add(position);
		}

		private double getScannablePosition(Scannable scannable) {
			try {
				return (double) scannable.getPosition();
			} catch (Exception e) {
				logger.error("Error reading {} position", scannable.getName(), e);
				return 0.0;
			}
		}

		public int getSize() {
			return x.size();
		}
	}

	private void plotTriggers() {
		for (TriggerRecord trigRec : activePlan.getTriggers()) {
			String triggerName = trigRec.getTriggerName();
			DynamicTraceMaintainer tp = triggerPlots.computeIfAbsent(triggerName, name -> new DynamicTraceMaintainer(name, Finder.find(trigRec.getSampleEnvironmentName()), false));
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

}
