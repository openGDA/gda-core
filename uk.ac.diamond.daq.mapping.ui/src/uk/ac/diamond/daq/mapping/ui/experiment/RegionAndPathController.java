/*-
 * Copyright © 2019 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingRegionManager;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.region.CentredRectangleMappingRegion;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.osgi.OsgiService;



/**
 * Controller to manage shared access to the Scan Region object of the Mapping Experiment bean. This should be used by
 * ALL views that modify the Scan Region and ALL related access to the bean should be via this object which is available
 * as a service. Views must provide a viewUpdater function which has the responsibility of managing the bindings to the
 * regionShape/scanPathModel properties of the Scan Region object and to the relevant point number field(s) on the
 * scanPathModel object. In addition a status update handler function should be provided to display error messages from
 * the point calculation job. The detach methods for each of these {@link Consumer}s MUST be called when a linked
 * view is disposed to release the references to the originally supplied functions.
 *
 * @since GDA 9.13
 */
@OsgiService(RegionAndPathController.class)
public class RegionAndPathController extends AbstractMappingController {
	/**
	 * This listener must handle changes to the region shape and cascade the effect down to cause rebinding and redraw
	 * of associated controls in the client view. It does this by firing an array of handler functions injected by each
	 * of the client views when calling the initialise method on the main class, which is mandatory
	 */
	public class RegionSelectorListener implements ISelectionChangedListener, IValueChangeListener<IMappingScanRegionShape> {

		private final PropertyChangeListener regionBeanPropertyChangeListener;
		private final AtomicBoolean inUpdate = new AtomicBoolean(false);

		public RegionSelectorListener() {
			this.regionBeanPropertyChangeListener = evt -> {
				updatePlotRegion();
				updatePoints();
			};
		}

		/**
		 * Handler for controls that return objects implementing {@link ISelection} e.g. combo box
		 */
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (inUpdate.compareAndSet(false, true)) {
				try {
					logger.debug("Region selection event setting region to: {}", event.getSelection().toString());

					// Get the new selection.
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					IMappingScanRegionShape selectedRegion = (IMappingScanRegionShape) selection.getFirstElement();

					changeRegion(selectedRegion);
				} finally {
					inUpdate.set(false);
				}
			}
		}

		/**
		 * Handler for controls that return objects implementing {@link IObservableValue} e.g. a set of radio buttons
		 */
		@Override
		public void handleValueChange(ValueChangeEvent<? extends IMappingScanRegionShape> event) {
			if (inUpdate.compareAndSet(false, true)) {
				try {
					logger.debug("Region selection event setting region to : {}",
								event.getObservableValue().getValue().getName());
					changeRegion(event.getObservableValue().getValue());

				} finally {
					inUpdate.set(false);
				}
			}
		}

		/**
		 * Handles the change of region hsape and fires each of the registered view update handlers.
		 *
		 * @param newRegion The new region shape that has been chosen
		 */
		private void changeRegion(IMappingScanRegionShape newRegion) {
			// We're going to replace the scan region with a new one
			// so, if the existing one is non-null, remove the property change listener from it
			if (scanRegionShape != null) {
				scanRegionShape.removePropertyChangeListener(regionBeanPropertyChangeListener);
			}

			// Set the new scan region in the local cache and the mapping bean
			scanRegionShape = newRegion;
			logger.debug("Setting mapping bean region to : {}", newRegion);
			getMappingBean().getScanDefinition().getMappingScanRegion().setRegion(scanRegionShape);

			// Update the path control(s) with paths valid for the new region type in each client view
			// (The path control listener will take care of propagating the change appropriately, and updating the GUI)
			// Do this before starting drawing the region (+ path ) with the plotting system because changing path
			// afterwards breaks the region drawing
			for (Consumer<RegionPathState> viewUpdater : viewUpdaters) {
				viewUpdater.accept(new RegionPathState(
						scanRegionShape, getRegionListAndLinkRegion(), scanPathModel, getScanPathListAndLinkPath()));
			}

			// If new scan region is non-null, add it to the plot and add the property change listener
			if (scanRegionShape != null) {
				plotter.createNewPlotRegion(scanRegionShape);
				scanRegionShape.addPropertyChangeListener(regionBeanPropertyChangeListener);
			}
		}
	}

	/**
	 * DTO style class to simplify the passing of the required state to the handler functions
	 */
	public final class RegionPathState {
		private final IMappingScanRegionShape scanRegionShape;
		private final IScanPathModel scanPathModel;
		private final List<IMappingScanRegionShape> scanRegionShapeList;
		private final List<IScanPathModel> scanPathList;

		RegionPathState(final IMappingScanRegionShape shape, final List<IMappingScanRegionShape> shapeList,
						final IScanPathModel path, final List<IScanPathModel> pathList) {
			scanRegionShape = shape;
			scanRegionShapeList = shapeList;
			scanPathModel = path;
			scanPathList = pathList;
		}

		public final IMappingScanRegionShape scanRegionShape() {
			return scanRegionShape;
		}

		public final IScanPathModel scanPathModel() {
			return scanPathModel;
		}

		public final List<IMappingScanRegionShape> scanRegionList() {
			return scanRegionShapeList;
		}

		public final List<IScanPathModel> scanPathList() {
			return scanPathList;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(RegionAndPathController.class);
	private static final String E4_CONTEXT_DATA = "org.eclipse.e4.data";

	private PropertyChangeListener pathBeanPropertyChangeListener;	// propagates changes in paths to the plotting system
	private PlottingController plotter;
	private BeamPositionPlotter beamPositionPlotter;
	private IMappingRegionManager mappingRegionManager;
	private Set<Class<?>> scanRegionClasses;
	private PathInfoCalculatorJob pathCalculationJob;
	private IMappingScanRegionShape scanRegionShape = null;
	private IScanPathModel scanPathModel = null;

	private List<Consumer<RegionPathState>> viewUpdaters = new ArrayList<>();
	private List<Consumer<String>> statusMessageConsumers = new ArrayList<>();

	private RegionSelectorListener listener;

	public RegionAndPathController() {
		logger.debug("Created RegionAndPathController");
	}

	/**
	 * This method MUST be called by all views that make use of the controller to obtain associated services, set up the
	 * scan path calculation job and register their path update and status message consumer functions if they exist.
	 * These will be fired for all associated views when appropriate events occur. Additionally this registers the event
	 * handler for path plotting events which cause the scan path to be drawn within the region. When passing the two
	 * consumers it is necessary to maintain references to the supplied functions (i.e. the lambda instances created by
	 * the compiler at that time) in the calling class. This is so that they can be passed to the associated detachXXX
	 * methods when the calling class is disposed resulting in them being removed from the active list of consumers.
	 * Failure to do this will result in characteristic NullPointerExceptions when the controller tries to call the no
	 * longer available functions.
	 *
	 * @param viewUpdater An {@link Optional} handler function to initialise the revised list of path in the client view
	 * @param statusMessageConsumer An {@link Optional} handler function to receive path calculation job status messages
	 */
	public void initialise(Optional<Consumer<RegionPathState>> viewUpdater,
			Optional<Consumer<String>> statusMessageConsumer) {
		super.initialise();
		if (viewUpdater.isPresent()) {
			viewUpdaters.add(viewUpdater.get());
		}
		if (statusMessageConsumer.isPresent()) {
			statusMessageConsumers.add(statusMessageConsumer.get());
		}
	}

	/**
	 * Called by the super class's doOneTimeInitialisation method having set its {@link AtomicBoolean} initialised flag
	 * to ensure that this code is only called once. Retrieves the required services and initalises them and also sets
	 * up the listeners for changes in scan path and region
	 */
	@Override
	protected void oneTimeInitialisation() {
		beamPositionPlotter = getService(BeamPositionPlotter.class);
		beamPositionPlotter.init();
		plotter = getService(PlottingController.class);
		registerPathPlotEventHandler();

		mappingRegionManager = getService(IMappingRegionManager.class);
		initialiseScanRegionClasses(mappingRegionManager);
		pathCalculationJob = createPathCalculationJob();
		setScanRegionShape(getScanRegionFromBean().getRegion());	// Initialize the shape and
		scanPathModel = getScanRegionFromBean().getScanPath();		// path to default values
		pathBeanPropertyChangeListener = evt -> updatePoints();
		listener = new RegionSelectorListener();
	}

	public void detachViewUpdater(Consumer<RegionPathState> viewUpdater) {
		viewUpdaters.remove(viewUpdater);
	}

	public void detachStatusMessageConsumer(Consumer<String> statusMessageConsumer) {
		statusMessageConsumers.remove(statusMessageConsumer);
	}

	/**
	 * Supplies the listener that will be trigger when the region shape is changed to allow the client view to respond.
	 * This will be the same for all views as it is created by the {@link #initialise} method
	 *
	 * @return An {@link ISelectionChangedListener} triggered by region shape selection
	 */
	public RegionSelectorListener getRegionSelectorListener() {
		checkInitialised();
		return listener;
	}

	/**
	 * Returns the current value of {@link IMappingScanRegionShape} held by the controller as the scanRegion resulting
	 * from all previous updates.
	 *
	 * @return	The current scanRegion
	 */
	public final IMappingScanRegionShape getScanRegionShape() {
		checkInitialised();
		return scanRegionShape;
	}

	/**
	 * Returns the current value of {@link IScanPathModel} held by the controller as the scanPathModel resulting
	 * from all previous updates.
	 *
	 * @return	The current scanPathModel
	 */
	public final IScanPathModel getScanPathModel() {
		checkInitialised();
		return scanPathModel;
	}

	/**
	 * Provides the list of available {@link IMappingScanRegionShape}s (scanRegions) and also ensures that if a
	 * scanRegion has been set previously, the corresponding object reference in the list of regions is a reference to
	 * that object. If no scanRegion has been set it sets it to refer to the first object in the list.
	 *
	 * @return The list of available region shapes.
	 */
	public List<IMappingScanRegionShape>getRegionListAndLinkRegion() {
		checkInitialised();
		List<IMappingScanRegionShape> regionList = getTemplateRegions();
		if (scanRegionShape == null) {
			scanRegionShape = regionList.get(0);
		} else {
			for (int i = 0; i < regionList.size(); i++) {
				if (regionList.get(i).getClass().equals(scanRegionShape.getClass())) {
					regionList.set(i, scanRegionShape);
				}
			}
		}
		return regionList;
	}

	/**
	 * Set {@link #scanRegionShape} from the input value, if a compatible editor exists.
	 * <p>
	 * In many cases, {@link #scanRegionShape} will be set to the input shape directly.<br>
	 * The special case is that a {@link CentredRectangleMappingRegion} and {@link RectangularMappingRegion} are
	 * semantically identical and can be used interchangeably, so convert one to the other if the only one of the two
	 * editors is available.
	 *
	 * @param regionShape
	 *            the region shape to set (subject to possible conversion noted above)
	 */
	private void setScanRegionShape(IMappingScanRegionShape regionShape) {
		if (regionShape == null) {
			scanRegionShape = null;
			return;
		}
		final Class<?> regionShapeClass = regionShape.getClass();
		if (scanRegionClasses.contains(regionShapeClass)) {
			scanRegionShape = regionShape;
		} else if (regionShapeClass.equals(RectangularMappingRegion.class) && scanRegionClasses.contains(CentredRectangleMappingRegion.class)) {
			scanRegionShape = new CentredRectangleMappingRegion();
			scanRegionShape.updateFromROI(regionShape.toROI());
		} else if (regionShapeClass.equals(CentredRectangleMappingRegion.class) && scanRegionClasses.contains(RectangularMappingRegion.class)) {
			scanRegionShape = new RectangularMappingRegion();
			scanRegionShape.updateFromROI(regionShape.toROI());
		} else {
			logger.error("Cannot set mapping scan region {}: no compatible editor found", regionShape.getName());
		}
	}

	/**
	 * Provides the list of available {@link IScanPathModel}s (scan paths) and also ensures that if a scanPathModel
	 * has been set previously, the corresponding object reference in the list of paths is a reference to
	 * that object. If no scanPathModel has been set it sets it to refer to the first object in the list.
	 *
	 * @return The list of available scan paths.
	 */
	public List<IScanPathModel> getScanPathListAndLinkPath() {
		checkInitialised();
		List<IScanPathModel> scanPathList = getValidPathsList();
		if (scanPathModel == null) {
			scanPathModel = scanPathList.get(0);
		} else {
			for (int i = 0; i < scanPathList.size(); i++) {
				if (scanPathList.get(i).getClass().equals(scanPathModel.getClass())) {
					scanPathList.set(i, scanPathModel);
				}
			}
		}
		return scanPathList;
	}

	/**
	 * Provides the list of available {@link IMappingScanRegionShape}s (scanRegions) as set in the Spring config N.B.
	 * The objects in this list may not correspond to the currently selected instance of their individual types; to get
	 * the same list but referring to the active instance use {@link #getRegionListAndLinkRegion()}
	 */
	public final List<IMappingScanRegionShape> getTemplateRegions() {
		return mappingRegionManager.getTemplateRegions();
	}

	/**
	 * Provides the list of available {@link IScanPathModel}s (scan paths) for the current region shape as set in the
	 * Spring config. N.B. The objects in this list may not correspond to the currently selected instance of their
	 * individual types; to get the same list but referring to the active instance use
	 * {@link #getScanPathListAndLinkPath()}
	 */
	public List<IScanPathModel> getValidPathsList() {
		return getValidPathsList(scanRegionShape);
	}

	/**
	 * Provides the list of available {@link IScanPathModel}s (scan paths) for the specified region shape as set in the
	 * Spring config. N.B. The objects in this list may not correspond to the currently selected instance of their
	 * individual types; to get the same list but referring to the active instance use
	 * {@link #getScanPathListAndLinkPath()}
	 */
	public List<IScanPathModel> getValidPathsList(final IMappingScanRegionShape scanRegionShape) {
		return mappingRegionManager.getValidPaths(scanRegionShape);
	}

	/**
	 * Creates as default sized region of the currently selected type at the current position of the mapped stage. The
	 * dimensions of the default region are obtained from the region definition in the Spring config and the local and
	 * mapping bean for the region are updated to reference that object instead of he current one.
	 */
	public void createDefaultRegionAtStagePosition() {
		checkInitialised();
		final MappingStageInfo mappingStage = getService(MappingStageInfo.class);
		final double xAxisPosition = getAxisPosition(mappingStage.getPlotXAxisName());
		final double yAxisPosition = getAxisPosition(mappingStage.getPlotYAxisName());

		scanRegionShape = mappingRegionManager.getTemplateRegion(scanRegionShape.getClass());
		scanRegionShape.centre(xAxisPosition, yAxisPosition);
		getMappingBean().getScanDefinition().getMappingScanRegion().setRegion(scanRegionShape);
	}

	/**
	 * Moves the current shape and path combination so that it is centred on the supplied coordinates. This will
	 * automatically trigger recalculation of the associated scan points. The referenced {@link IMappingScanRegionShape}
	 * does not change but its fields are updated propagating the change to listeners.
	 *
	 * @param x		The new horizontal centre of the region
	 * @param y		The new vertical centre of the region
	 */
	public void createRegionWithCurrentRegionValuesAt(double x, double y) {
		checkInitialised();
		scanRegionShape.centre(x, y);
	}

	public void setMappingBean(IMappingExperimentBean bean) {
		checkInitialised();
		mappingExperimentBeanProvider.setMappingExperimentBean(bean);
		mappingExperimentBeanProvider.setSetByView(true);
	}

	/**
	 * Updates the local caches of scan region and path from the mappingExperimentBean
	 */
	public void refreshFromMappingBean() {
		checkInitialised();
		IMappingScanRegion mappingScanRegion = getScanRegionFromBean();
		setScanRegionShape(mappingScanRegion.getRegion());
		scanPathModel = mappingScanRegion.getScanPath();
	}

	/**
	 * Updates both the mapping bean's {#link IMappingScanRegion} object and the corresponding local values of region
	 * shape and path model
	 * @param newRegionShapeValue	The region shape to be stored
	 * @param newPathValue			The path model to be stored
	 */
	public void updateMappingBeanScanRegion(final IMappingScanRegionShape newRegionShapeValue,
			final IScanPathModel newPathValue) {
		checkInitialised();
		IMappingScanRegion mappingScanRegion = getScanRegionFromBean();
		mappingScanRegion.setRegion(newRegionShapeValue);
		mappingScanRegion.setScanPath(newPathValue);
		refreshFromMappingBean();
	}

	/**
	 * Retrives the current {#link IMappingScanRegion} object from the mapping bean
	 *
	 * @return	The mapping bean's current {#link IMappingScanRegion}
	 */
	public IMappingScanRegion getScanRegionFromBean() {
		checkInitialised();
		return getMappingBean().getScanDefinition().getMappingScanRegion();
	}

	/**
	 * Triggers an update of the plotted region
	 */
	public void updatePlotRegion() {
		checkInitialised();
		plotter.updatePlotRegionFrom(scanRegionShape);
	}

	/**
	 * Schedules a recalculation of the current scan path, cancelling any  calculations currently in progress
	 */
	public void updatePoints() {
		checkInitialised();
		pathCalculationJob.cancel();
		if (scanPathModel != null && scanRegionShape != null) {
			pathCalculationJob.setScanPathModel(scanPathModel);
			pathCalculationJob.setScanRegion(scanRegionShape);
			pathCalculationJob.schedule();
		}
	}

	/**
	 *  Updates the current scan path - this method should be called by the view updater function of client methods to
	 *  manage the plotter bindings and trigger recalculation of the set of points when a new scan path is chosen.
	 *
	 * @param newPath The new path to be set.
	 */
	public void changePath(IScanPathModel newPath) {
		checkInitialised();
		logger.debug("Changing path to {}", newPath);

		// We're going to replace the scan path with a new one
		// If the existing one is non-null, remove the property change listener from it
		if (scanPathModel != null) {
			scanPathModel.removePropertyChangeListener(pathBeanPropertyChangeListener);
		}

		// Set the new scan path. If non-null, add the property change listener
		scanPathModel = newPath;
		getScanRegionFromBean().setScanPath(newPath);
		if (scanPathModel != null) {
			scanPathModel.addPropertyChangeListener(pathBeanPropertyChangeListener);
		}

		// Update the GUI to reflect the path changes
		updatePoints();
	}

	public void triggerRegionUpdate(final SelectObservableValue<IMappingScanRegionShape> observableValue) {
		checkInitialised();
		getRegionSelectorListener().handleValueChange(
				new ValueChangeEvent<IMappingScanRegionShape>(observableValue, null));
	}

	/**
	 * Create the job in the Eclipse infrastructure to calculate the points for the cvurrent region shape and scan path
	 *
	 * @return	A reference to the created {@link Job}
	 */
	private PathInfoCalculatorJob createPathCalculationJob() {
		PathInfoCalculatorJob job = ContextInjectionFactory.make(PathInfoCalculatorJob.class, getService(IEclipseContext.class));
		UISynchronize uiSync = getService(UISynchronize.class);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void running(IJobChangeEvent event) {
				uiSync.asyncExec(() -> {
					publishStatusMessage("Scan path calculation in progress");
					plotter.removePath();
				});
			}
			@Override
			public void done(final IJobChangeEvent event) {
				uiSync.asyncExec(() -> {
					IStatus result = event.getResult();
					if (result.getSeverity() == IStatus.CANCEL) {
						publishStatusMessage("Scan path calculation was cancelled");
					} else if (!result.isOK()) {
						publishStatusMessage("Error in scan path calculation - see log for details");
						logger.warn("Error in scan path calculation", result.getException());
					}
					// else, calculation completed normally and the status text will be updated from the new PathInfo
				});
			}
		});
		return job;
	}

	/**
	 * Initialise the set of available scan regions
	 * <p>
	 * The {@link #mappingRegionManager} must be set before classing this function
	 */
	private void initialiseScanRegionClasses(IMappingRegionManager mappingRegionManager) {
		final List<IMappingScanRegionShape> scanRegions = mappingRegionManager.getTemplateRegions();
		scanRegionClasses = new HashSet<>(scanRegions.size());
		for (IMappingScanRegionShape region : scanRegions) {
			scanRegionClasses.add(region.getClass());
		}
	}

	private double getAxisPosition(String axisName) {
		Scannable axis = Finder.getInstance().find(axisName);
		try {
			return (double) axis.getPosition();
		} catch (DeviceException e) {
			logger.error("Could not get position of axis {}", axisName, e);
			return 0;
		}
	}

	/**
	 * Triggers all status message handler functions passed in by client vies during initialisation
	 * @param message
	 */
	private void publishStatusMessage(final String message) {
		for (Consumer<String> messageConsumer : statusMessageConsumers) {
			messageConsumer.accept(message);
		}
	}

	/**
	 * Provide a handler for event caused by the {@link PathInfoCalculatorJob} submitting path data to its Path
	 * Calculation Topic. The handler must run in the UI thread for the path to be plotted.
	 */
	private void registerPathPlotEventHandler() {
		BundleContext ctx = FrameworkUtil.getBundle(RegionAndPathController.class).getBundleContext();
		EventHandler handler = new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				// switch to UI thread is not already in it passing this method to it AND THEN RETURNING!
				if (Display.getCurrent() == null) {
					Display.getDefault().asyncExec(() -> handleEvent(event));
					return;
				}
				Optional<PathInfo> pathInfo = Optional.ofNullable((PathInfo)event.getProperty(E4_CONTEXT_DATA));
				if (pathInfo.isPresent()) {
					plotter.plotPath(pathInfo.get());
				} else {
					logger.error("Could not plot path - no Path Info data present in UI Event");
				}
			}
		};

		Dictionary<String, String> props = new Hashtable<>();
		props.put(org.osgi.service.event.EventConstants.EVENT_TOPIC,PathInfoCalculatorJob.PATH_CALCULATION_TOPIC);
		ctx.registerService(EventHandler.class, handler, props);
	}
}
