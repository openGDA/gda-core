/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view;

import static uk.ac.gda.client.live.stream.view.StreamViewUtility.displayAndLogError;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.nxdetector.roi.ImutableRectangularIntegerROI;
import gda.factory.Finder;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.api.ILiveStreamConnectionService;
import uk.ac.gda.client.live.stream.handlers.SnapshotData;
import uk.ac.gda.client.live.stream.view.customui.LiveStreamViewCustomUi;

/**
 * A RCP view for connecting to and displaying a live MJPEG stream. The intention is to provide a easy way for cameras
 * to be integrated into GDA, with minimal Spring configuration.
 * <p>
 * Can be extended by overriding {@link #createLivePlot(Composite, String)}.
 * <p>
 * To setup this view in Spring create a {@link CameraConfiguration} in client Spring
 * <p>
 * For additional docs see <a href="http://confluence.diamond.ac.uk/x/1wWKAg">Setup Live Stream Camera View</a>
 *
 * @author James Mudd
 */
public class LiveStreamView extends ViewPart {

	public static final String ID = "uk.ac.gda.client.live.stream.view.LiveStreamView";

	private static final Logger logger = LoggerFactory.getLogger(LiveStreamView.class);

	private CameraConfiguration camConfig;
	private LiveStreamConnection liveStreamConnection;
	private LivePlottingComposite plottingComposite;
	private IActionBars actionBars;

	@Override
	public void createPartControl(final Composite parent) {
		if (PlatformUI.getWorkbench().getService(IRemoteDatasetService.class) == null) {
			displayAndLogError(logger, parent, "Cannot create Live Stream: no remote dataset service is available");
			return;
		}

		if (PlatformUI.getWorkbench().getService(IPlottingService.class) == null) {
			displayAndLogError(logger, parent, "Cannot create Live Stream: no plotting service is available");
			return;
		}

		if (PlatformUI.getWorkbench().getService(ILiveStreamConnectionService.class) == null) {
			displayAndLogError(logger, parent, "Cannot create Live Stream: no connection service is available");
			return;
		}

		// Check if the secondary id is set if so open the view else ask the user to choose a camera
		if (getViewSite().getSecondaryId() != null) {
			createLivePlot(parent, getViewSite().getSecondaryId());
		} else {
			createCameraSelector(parent);
		}
	}

	private void createCameraSelector(final Composite parent) {
		// Find all the implemented cameras. This is currently using the finder but could use OSGi instead.
		final List<CameraConfiguration> cameras = Finder.getInstance().listLocalFindablesOfType(CameraConfiguration.class);
		final Map<String, CameraConfiguration> cameraMap = new TreeMap<>();
		for (CameraConfiguration cam : cameras) {
			if (cam.getDisplayName() != null) {
				cameraMap.put(cam.getDisplayName(), cam);
			} else {
				logger.warn("No display name was set for camera id: {}. Using id instead", cam.getName());
				cameraMap.put(cam.getName(), cam);
			}
		}
		if (!cameraMap.isEmpty()) {
			logger.debug("Found {} cameras", cameras.size());

			// Setup composite layout
			parent.setLayout(new GridLayout(1, false));
			parent.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

			final Label cameraSelectorLabel = new Label(parent, SWT.NONE);
			cameraSelectorLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
			cameraSelectorLabel.setText("Select camera:");

			final org.eclipse.swt.widgets.List cameraSelector = new org.eclipse.swt.widgets.List(parent,
					SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
			cameraSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			cameraSelector.setItems(cameraMap.keySet().toArray(new String[0]));
			cameraSelector.setSelection(0);
			cameraSelector.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					openViewWithSecondaryId(
							cameraMap.get(cameraSelector.getItem(cameraSelector.getSelectionIndex())).getName(), false);
				}
			});

			final Button connectButton = new Button(parent, SWT.NONE);
			connectButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
			connectButton.setText("Connect");
			connectButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Get the cameras ID for the secondary ID
					openViewWithSecondaryId(
							cameraMap.get(cameraSelector.getItem(cameraSelector.getSelectionIndex())).getName(), false);
				}
			});

			final Button connectCloseButton = new Button(parent, SWT.NONE);
			connectCloseButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
			connectCloseButton.setText("Connect and close");
			connectCloseButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Get the cameras ID for the secondary ID
					openViewWithSecondaryId(
							cameraMap.get(cameraSelector.getItem(cameraSelector.getSelectionIndex())).getName(), true);
				}
			});

		} else { // No cameras found
			displayAndLogError(logger, parent, "No cameras were found");
		}
	}

	private String cameraIdFromSecondaryId(String secondaryId) {
		if (secondaryId.endsWith(StreamType.MJPEG.secondaryIdSuffix())) {
			return secondaryId.substring(0, secondaryId.lastIndexOf(StreamType.MJPEG.secondaryIdSuffix()));
		} else if (secondaryId.endsWith(StreamType.EPICS_ARRAY.secondaryIdSuffix())) {
			return secondaryId.substring(0, secondaryId.lastIndexOf(StreamType.EPICS_ARRAY.secondaryIdSuffix()));
		} else {
			return secondaryId;
		}
	}

	private StreamType streamTypeFromSecondaryId(String secondaryId) {
		if (secondaryId.endsWith(StreamType.MJPEG.secondaryIdSuffix())) {
			return StreamType.MJPEG;
		} else if (secondaryId.endsWith(StreamType.EPICS_ARRAY.secondaryIdSuffix())) {
			return StreamType.EPICS_ARRAY;
		} else {
			return null;
		}
	}

	/**
	 * This is the method that actually creates a MJPEG stream and sets up the plotting composite.
	 * <p>
	 * To get here the secondary id of the view need to be set (hopefully to a valid camera id)
	 * <p>
	 * When extending this class it is necessary to call this method with the composite the plot
	 * should be drawn on.
	 *
	 * @param parent
	 *            Composite to draw on
	 * @param secondaryId
	 *            The name of the camera to use and type of stream to display
	 */
	protected void createLivePlot(final Composite parent, final String secondaryId) {
		logger.debug("Creating live stream plot with secondary ID: {}", secondaryId);
		final String cameraId = cameraIdFromSecondaryId(secondaryId);
		StreamType streamType = streamTypeFromSecondaryId(secondaryId);

		// Get the camera config from the finder
		camConfig = Finder.getInstance().find(cameraId);

		if (camConfig == null) {
			displayAndLogError(logger, parent, "Camera configuration could not be found for camera ID " + cameraId);
			logger.info("Check that 'client.xml' contains a '<bean class=\"gda.spring.FindableNameSetterPostProcessor\" />' line");
			return;
		}

		if (streamType == null) {
			streamType = camConfig.getUrl() == null ? StreamType.EPICS_ARRAY : StreamType.MJPEG;
		}

		// Use camera ID (, i.e. camera device name) and stream type for the tab text, to keep it short
		setPartName(cameraId + ": " + streamType);

		actionBars = getViewSite().getActionBars();

		// Use a single column to support top custom UI, main stream view and bottom custom UI.
		parent.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).create());

		// Create the plotting view
		try {
			ILiveStreamConnectionService connectionService = PlatformUI.getWorkbench().getService(ILiveStreamConnectionService.class);
			liveStreamConnection = connectionService.getSharedLiveStreamConnection(camConfig, streamType);
			plottingComposite = new LivePlottingComposite(parent, SWT.NONE, getPartName(), liveStreamConnection, actionBars, this);
			plottingComposite.setShowAxes(camConfig.getCalibratedAxesProvider() != null);
			plottingComposite.setShowTitle(true);
			plottingComposite.connect();
			GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingComposite);
			setupRoiProvider();
		} catch (Exception e) {
			displayAndLogError(logger, parent, "Could not create plotting view", e);
			return;
		}

		// Add useful plotting system actions
		configureActionBars(actionBars);

		createCustomUi(parent);

	}

	/**
	 * This method sets up custom UI if defined in the {@link CameraConfiguration}.
	 *
	 * @param parent The parent to hold the custom UI
	 */
	private void createCustomUi(final Composite parent) {
		if(camConfig.getTopUi() != null) {
			// New composite to hold the custom UI. This insulates this class from what the custom UI does.
			Composite topComposite = new Composite(parent, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(topComposite);

			GridLayoutFactory.fillDefaults().applyTo(topComposite);
			createCustomUiSection(topComposite, camConfig.getTopUi());

			topComposite.moveAbove(plottingComposite); // Put the top composite above the plot
		}


		if(camConfig.getBottomUi() != null) {
			// New composite to hold the custom UI. This insulates this class from what the custom UI does.
			Composite bottomComposite = new Composite(parent, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(bottomComposite);

			GridLayoutFactory.fillDefaults().applyTo(bottomComposite);
			createCustomUiSection(bottomComposite, camConfig.getBottomUi());
		}

		// This redraws the top composite above the plot
		parent.redraw();
	}


	/**
	 * This method sets up a custom UI by providing it with the {@link IPlottingSystem} and {@link LiveStreamConnection}
	 * then calling LiveStreamViewCustomUi#createUi(Composite) method to draw. At this point the custom UI can use the
	 * plotting system or stream if required.
	 *
	 * @param parent
	 *            the composite onto which the the custom UI will be drawn
	 * @param customUi
	 *            The custom Ui to draw
	 */
	private void createCustomUiSection(Composite parent, LiveStreamViewCustomUi customUi) {
		customUi.setPlottingSystem(plottingComposite.getPlottingSystem());
		customUi.setLiveStreamConnection(liveStreamConnection);
		customUi.setImageTrace(plottingComposite.getITrace());
		customUi.setActionBars(actionBars);
		customUi.createUi(parent);
		logger.debug("Using custom UI class '{}'", customUi.getClass().getCanonicalName());
	}

	private void configureActionBars(IActionBars actionBars) {
		final IToolBarManager toolBarManager = actionBars.getToolBarManager();

		// Setup the plotting system toolbar options
		final List<String> requiredToolBarIds = Arrays.asList(
				"org.csstudio.swt.xygraph.autoscale",
				"org.dawb.common.ui.plot.tool",
				"org.dawb.workbench.plotting.histo",
				"org.dawnsci.plotting.system.preference.export",
				"org.eclipse.nebula.visualization.xygraph.figures.ZoomType",
				"org.dawb.workbench.ui.editors.plotting.swtxy.addRegions",
				"org.dawb.workbench.ui.editors.plotting.swtxy.removeRegions");

		// Remove all ToolBar contributions with Ids which are either undefined or not required
		Arrays.stream(toolBarManager.getItems())
			.filter(ci -> ci.getId() == null || requiredToolBarIds.stream().noneMatch(ci.getId()::contains))
			.forEach(toolBarManager::remove);
			// If getId() returns null then the match will not be performed as the || short circuits it, this
			// also prevents the NPE which would result from trying to match on a null Id.

		// Remove all Menu contributions
		actionBars.getMenuManager().removeAll();

		// Add the Reset button to restart the view
		toolBarManager.insertBefore(toolBarManager.getItems()[0].getId(), new Action() {
			@Override
			public void run() {
				openViewWithSecondaryId(getViewSite().getSecondaryId(), true);
			}
			@Override
			public  ImageDescriptor getImageDescriptor() {
				return AbstractUIPlugin.imageDescriptorFromPlugin("uk.ac.gda.client.live.stream", "icons/reset_view.png");
			}
			@Override
			public  String getToolTipText() {
				return "Reset live stream view";
			}
		});

		actionBars.updateActionBars();
	}

	@Override
	public void setFocus() {
		final IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
		if (plottingSystem != null) {
			plottingSystem.setFocus();
		}
	}

	@Override
	// This method is required for the plotting tools to work.
	public <T> T getAdapter(final Class<T> clazz) {
		final IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
		if (plottingSystem != null) {
			T adapter = plottingSystem.getAdapter(clazz);
			if (adapter != null) {
				return adapter;
			}
		}
		return super.getAdapter(clazz);
	}

	@Override
	public void dispose() {
		if (camConfig != null) {
			if (camConfig.getTopUi() !=  null) {
				camConfig.getTopUi().dispose();
			}
			if (camConfig.getBottomUi() !=  null) {
				camConfig.getBottomUi().dispose();
			}
		}

		if (plottingComposite != null) {
			plottingComposite.dispose();
		}
		if (liveStreamConnection != null) {
			try {
				liveStreamConnection.disconnect();
			} catch (LiveStreamException e) {
				logger.error("Error disconnecting live stream", e);
			}
		}
		super.dispose();
	}

	/**
	 * Open a live stream view with the secondary ID specified
	 *
	 * @param secondaryId
	 * @param closeExistingView close the existing view first
	 */
	protected void openViewWithSecondaryId(final String secondaryId, boolean closeExistingView) {
		final IWorkbenchPage page = getSite().getPage();
		if (closeExistingView) {
			page.hideView(this);
		}
		try {
			page.showView(LiveStreamView.ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			logger.error("Error activating Live MJPEG view with secondary ID {}", secondaryId, e);
		}
	}

	/**
	 * Set up ROI provider
	 */
	private void setupRoiProvider() {
		// Setup the ROI provider if configured
		if (camConfig.getRoiProvider() != null) {

			final IROIListener roiListener = new IROIListener.Stub() {
				@Override
				public void roiChanged(ROIEvent evt) {
					updateServerRois();
				}
			};

			final IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
			if (plottingSystem == null) {
				logger.warn("ROI provider not set: plotting system is null");
				return;
			}
			plottingSystem.addRegionListener(new IRegionListener.Stub() {

				// Note regionS method
				@Override
				public void regionsRemoved(RegionEvent evt) {
					evt.getRegions().stream().forEach(region -> region.removeROIListener(roiListener));
					updateServerRois();
				}

				@Override
				public void regionRemoved(RegionEvent evt) {
					evt.getRegion().removeROIListener(roiListener);
					updateServerRois();
				}

				@Override
				public void regionAdded(RegionEvent evt) {
					evt.getRegion().addROIListener(roiListener);
					updateServerRois();
				}

				@Override
				public void regionNameChanged(RegionEvent evt, String oldName) {
					updateServerRois();
				}
			});
		}
	}

	private void updateServerRois() {
		final IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
		if (plottingSystem == null) {
			logger.warn("Cannot update server ROIS: plotting system is null");
			return;
		}
		final Collection<IRegion> regions = plottingSystem.getRegions();

		// Check if any regions are non rectangular and warn if not
		if (regions.stream()
				.map(IRegion::getROI)
				.anyMatch(roi -> !(roi instanceof RectangularROI))) {
			logger.warn("{} contains non rectangular regions", camConfig.getDisplayName());
		}

		// Get the rectangular ROIs
		final List<gda.device.detector.nxdetector.roi.RectangularROI<Integer>> rois = regions.stream()
				.map(IRegion::getROI)
				.filter(RectangularROI.class::isInstance) // Only use rectangular ROIs
				.map(RectangularROI.class::cast) // Cast to RectangularROI
				.map(ImutableRectangularIntegerROI::valueOf) // Create ImutableRectangularIntegerROI
				.collect(Collectors.toList());

		// Send the new ROIs to the server
		camConfig.getRoiProvider().updateRois(rois);
	}

	public CameraConfiguration getActiveCameraConfiguration() {
		return camConfig;
	}

	public StreamType getActiveStreamType() {
		if (liveStreamConnection != null) {
			return liveStreamConnection.getStreamType();
		}
		return null;
	}

	public SnapshotData getSnapshot() throws LiveStreamException {
		final IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
		if (plottingSystem == null) {
			throw new LiveStreamException("Cannot get snapshot: plotting system is null");
		}
		final IImageTrace iTrace = getITrace();
		if (iTrace == null) {
			throw new LiveStreamException("Cannot get snapshot: image trace is null");
		}
		final SnapshotData snapshotData = new SnapshotData(plottingSystem.getTitle(), iTrace.getData().clone());
		final List<IDataset> axes = iTrace.getAxes();
		if (axes != null && !axes.isEmpty()) {
			snapshotData.setxAxis(axes.get(0));
			snapshotData.setyAxis(axes.get(1));
		}

		return snapshotData;
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		if (plottingComposite == null) {
			return null;
		}
		return plottingComposite.getPlottingSystem();
	}

	protected IImageTrace getITrace() {
		if (plottingComposite == null) {
			return null;
		}
		return plottingComposite.getITrace();
	}
}
