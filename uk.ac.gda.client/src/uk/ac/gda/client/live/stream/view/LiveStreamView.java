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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.plotting.api.IPlotActionSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.scanning.connector.epicsv3.EpicsV3DynamicDatasetConnector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.DropDownAction;

/**
 * A RCP view for connecting to and displaying a live MJPEG stream. The intension is to provide a easy way for cameras
 * to be integrated into GDA, with minimal Spring configuration.
 *
 * @author James Mudd
 */
public class LiveStreamView extends ViewPart {

	public static final String ID = "uk.ac.gda.client.live.stream.view.LiveStreamView";

	private static final Logger logger = LoggerFactory.getLogger(LiveStreamView.class);

	private static final long MJPEG_DEFAULT_SLEEP_TIME = 50; // ms i.e. 20 fps
	private static final int MJPEG_DEFAULT_CACHE_SIZE = 3; // frames

	private static IPlottingService plottingService;
	private static IRemoteDatasetService remoteDatasetService;

	public static synchronized void setPlottingService(IPlottingService plottingService) {
		logger.debug("Plotting service set to: {}", plottingService);
		LiveStreamView.plottingService = plottingService;
	}

	public static synchronized void setRemoteDatasetService(IRemoteDatasetService remoteDatasetService) {
		logger.debug("Remote Dataset service set to: {}", remoteDatasetService);
		LiveStreamView.remoteDatasetService = remoteDatasetService;
	}

	private IPlottingSystem<Composite> plottingSystem;
	private IDatasetConnector stream;
	private StreamType streamType;
	private IImageTrace iTrace;
	private CameraConfiguration camConfig;
	private Composite parent;
	private Text errorText;
	private String cameraName;
	private long frameCounter = 0;
	private final IDataListener shapeListener = new IDataListener() {
		int[] oldShape;

		@Override
		public void dataChangePerformed(DataEvent evt) {
			final Display display = PlatformUI.getWorkbench().getDisplay();
			// Check if the shape has changed, if so rescale
			if (!Arrays.equals(evt.getShape(), oldShape)) {
				oldShape = evt.getShape();
				// Need to be in the UI thread to do rescaling
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						plottingSystem.autoscaleAxes();
						iTrace.rehistogram();
					}
				});
			}
			// Update the frame count in the UI thread
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					plottingSystem.setTitle(cameraName + ": " + streamType + " - Frame: " + Long.toString(frameCounter++));
				}
			});
		}
	};

	@Override
	public void createPartControl(final Composite parent) {

		if (remoteDatasetService == null) {
			displayAndLogError(parent, "Cannot connect to MJPEG stream: no remote dataset service is available");
			return;
		}

		if (plottingService == null) {
			displayAndLogError(parent, "Cannot connect to MJPEG stream: no plotting service is available");
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
			List<CameraConfiguration> cameras = Finder.getInstance().listLocalFindablesOfType(CameraConfiguration.class);
			final Map<String, CameraConfiguration> cameraMap = new TreeMap<String, CameraConfiguration>();
			for (CameraConfiguration camConfig : cameras) {
				if (camConfig.getDisplayName() != null) {
					cameraMap.put(camConfig.getDisplayName(), camConfig);
				} else {
					logger.warn("No display name was set for camera id: {}. Using id instead", camConfig.getName());
					cameraMap.put(camConfig.getName(), camConfig);
				}
			}
			if (!cameraMap.isEmpty()) {
				logger.debug("Found {} cameras", cameras.size());

				// Setup composite layout
				parent.setLayout(new GridLayout(1, false));
				parent.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

				Label cameraSelectorLabel = new Label(parent, SWT.NONE);
				cameraSelectorLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
				cameraSelectorLabel.setText("Select camera:");

				final org.eclipse.swt.widgets.List cameraSelector = new org.eclipse.swt.widgets.List(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
				cameraSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				cameraSelector.setItems(cameraMap.keySet().toArray(new String[0]));
				cameraSelector.setSelection(0);
				cameraSelector.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDoubleClick(MouseEvent e) {
						reopenViewWithSecondaryId(cameraMap.get(cameraSelector.getItem(cameraSelector.getSelectionIndex())).getName());
					}
				});

				Button connectButton = new Button(parent, SWT.DEFAULT);
				connectButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
				connectButton.setText("Connect");
				connectButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						// Get the cameras ID for the secondary ID
						reopenViewWithSecondaryId(cameraMap.get(cameraSelector.getItem(cameraSelector.getSelectionIndex())).getName());
					}
				});

			} else { // No cameras found
				displayAndLogError(parent, "No cameras were found");
			}
			return;
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
	 * This is the method that actually creates a MJPEG stream and sets up the plotting system.
	 * <p>
	 * To get here the secondary id of the view need to be set (hopefully to a valid camera id)
	 *
	 * @param parent
	 *            Composite to draw on
	 * @param secondaryId
	 *            The name of the camera to use and type of stream to display
	 */
	private void createLivePlot(final Composite parent, final String secondaryId) {
		this.parent = parent;

		String cameraId = cameraIdFromSecondaryId(secondaryId);
		StreamType streamType = streamTypeFromSecondaryId(secondaryId);

		// Get the camera config from the finder
		camConfig = Finder.getInstance().find(cameraId);

		if (camConfig == null) {
			displayAndLogError(parent, "Camera configuration could not be found for the specified camera ID");
			return;
		}

		if (streamType == null) {
			streamType = camConfig.getUrl() == null ? StreamType.EPICS_ARRAY : StreamType.MJPEG;
		}

		// Do some things to make the UI a bit more friendly
		if (camConfig.getDisplayName() != null) {
			setPartName(camConfig.getDisplayName());
		} else {
			setPartName(camConfig.getName());
		}

		// Setup the plotting system
		try {
			plottingSystem = plottingService.createPlottingSystem();
			plottingSystem.createPlotPart(parent, camConfig.getUrl(), null, PlotType.IMAGE, this);
		} catch (Exception e) {
			displayAndLogError(parent, "Could not create plotting system", e);
			return;
		}

		for (IAxis axis : plottingSystem.getAxes()) {
			axis.setVisible(false);
		}

		// Get the camera name to use for the GUI
		if (camConfig.getDisplayName() != null) {
			cameraName = camConfig.getDisplayName();
		} else {
			cameraName = cameraId;
		}
		plottingSystem.setTitle(cameraName);

		// Add useful plotting system actions
		configureToolbar();

		// Fix the aspect ratio as is typically required for visible cameras
		plottingSystem.setKeepAspect(true);
		// Disable auto rescale as the live stream is constantly refreshing
		plottingSystem.setRescale(false);

		// Create a new trace.
		iTrace = plottingSystem.createImageTrace("Live camera stream");

		// Attach the IDatasetConnector of the MJPEG stream to the trace.
		if (streamType == StreamType.MJPEG && camConfig.getUrl() == null) {
			displayAndLogError(parent, "MJPEG stream requested but no url defined for " + cameraName);
		}
		if (streamType == StreamType.EPICS_ARRAY && camConfig.getArrayPv() == null) {
			displayAndLogError(parent, "EPICS stream requested but no array PV defined for " + cameraName);
		}
		setupStream(streamType);

		// Try and make the stream run faster
		iTrace.setDownsampleType(DownsampleType.POINT);
		iTrace.setRescaleHistogram(false);
		// Plot the new trace.
		plottingSystem.addTrace(iTrace);
	}

	private void configureToolbar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		// Add the menu for switching between stream types
		toolBarManager.add(new SwitchStreamTypeAction(this));
		// Setup the plotting system toolbar options
		IPlotActionSystem plotActionSystem = plottingSystem.getPlotActionSystem();
		plotActionSystem.fillToolActions(toolBarManager, ToolPageRole.ROLE_2D);
		plotActionSystem.fillZoomActions(toolBarManager);
		plotActionSystem.fillPrintActions(toolBarManager);
		getViewSite().getActionBars().updateActionBars();
	}

	@Override
	public void setFocus() {
		if (plottingSystem != null) {
			plottingSystem.setFocus();
		}
	}

	private void displayAndLogError(final Composite parent, final String errorMessage) {
		displayAndLogError(parent, errorMessage, null);
	}

	private void displayAndLogError(final Composite parent, final String errorMessage, final Exception exception) {
		logger.error(errorMessage, exception);
		if (errorText == null) {
			errorText = new Text(parent, SWT.LEFT | SWT.WRAP | SWT.BORDER);
			errorText.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					errorText.dispose();
					parent.layout(true);
					errorText=null;
				}
			});
			errorText.setToolTipText("Double click this message to remove it.");
			parent.layout(true);
		}
		StringBuilder s = new StringBuilder(errorText.getText());
		s.append("\n").append(errorMessage);
		if (exception != null) { s.append("\n\t").append(exception.getMessage()); }
		errorText.setText(s.toString());
	}

	@Override
	// This method is required for the plotting tools to work.
	public <T> T getAdapter(final Class<T> clazz) {
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
		if (plottingSystem != null) {
			plottingSystem.dispose();
			plottingSystem = null;
		}
		if (stream != null) {
			try {
				if (shapeListener != null) {
					stream.removeDataListener(shapeListener);
				}
				stream.disconnect();
				stream = null;
			} catch (Exception e) {
				logger.error("Error disconnecting remote data stream", e);
			}
		}
		super.dispose();
	}

	/**
	 * Close this view and open again with the secondary ID specified
	 */
	private void reopenViewWithSecondaryId(final String secondaryId) {
		IWorkbenchPage page = getSite().getPage();
		page.hideView(this);
		try {
			page.showView(LiveStreamView.ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			logger.error("Error activating Live MJPEG view with secondary ID {}", secondaryId, e);
		}
	}

	/**
	 * This inner class is just used to provide the toolbar action enabling reseting and switching the stream type.
	 */
	class SwitchStreamTypeAction extends DropDownAction {

		public SwitchStreamTypeAction(final LiveStreamView liveMjpegPlot) {
			// Need to have a default action. Here I chose to have a reset button.
			super(new Action() {
				@Override
				public void run() {
					liveMjpegPlot.reset();
				}
				@Override
				public String getText() {
					return "Reset";
				}
			});

			// Go through all the known stream types and if they are configured options add them
			for (final StreamType type : StreamType.values()) {
				switch (type) {
				case MJPEG:
					if (camConfig.getUrl() == null) continue;
					break;
				case EPICS_ARRAY:
					if (camConfig.getArrayPv() == null) continue;
					break;
				default:
					logger.warn("Building menu encounterd an unrecognised stream type");
					// Any unrecognised new types add them all
					break;
				}
				// If you got here your about to add an additional stream option.
				this.add(new Action(type.displayName, IAction.AS_PUSH_BUTTON) {
					@Override
					public void run() {
						liveMjpegPlot.reopenViewWithSecondaryId(cameraIdFromSecondaryId(getViewSite().getSecondaryId()) + type.secondaryIdSuffix());
					}
				});
			}

			this.setToolTipText("Choose the type of stream to display from this camera");
		}
	}

	/**
	 * Enum containing all the known stream types
	 */
	private enum StreamType {
		MJPEG("MJPEG"),
		EPICS_ARRAY("EPICS Array");

		private final String displayName;

		StreamType(String displayName) {
			this.displayName = displayName;
		}

		/**
		 * Note: If this is changed, views referenced in user workspaces will no longer be valid.
		 *
		 * @return suffix used to denote which stream is associated with a view.
		 */
		public String secondaryIdSuffix() {
			return "#" + name();
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	/**
	 * This should be called when starting a new stream. It also takes care of disconnecting old streams (if any)
	 *
	 * @param streamType The type of stream to use
	 */
	private void setupStream(StreamType streamType) {
		// Update the current stream type field.
		this.streamType = streamType;

		// Disconnect the existing stream
		if (stream != null) { // Will be null the first time
			try {
				stream.disconnect();
			} catch (DatasetException e) {
				displayAndLogError(parent, "Error disconnecting from stream", e);
				return; // Should not continue and create an additional stream
			}
		}

		// Switch on the type of stream to setup
		switch (streamType) {
		case MJPEG:

			final URL url;
			try {
				url = new URL(camConfig.getUrl());
			} catch (MalformedURLException e) {
				displayAndLogError(parent, "Malformed URL check camera configuration", e);
				return;
			}

			// If sleepTime or cacheSize are set use them, else use the defaults
			long sleepTime = camConfig.getSleepTime() != 0 ? camConfig.getSleepTime() : MJPEG_DEFAULT_SLEEP_TIME; // ms
			int cacheSize = camConfig.getCacheSize() != 0 ? camConfig.getCacheSize() : MJPEG_DEFAULT_CACHE_SIZE; // frames

			try {
				if (camConfig.isRgb()) {
					stream = remoteDatasetService.createMJPGDataset(url, sleepTime, cacheSize);
				} else {
					stream = remoteDatasetService.createGrayScaleMJPGDataset(url, sleepTime, cacheSize);
				}
				stream.connect();
			} catch (Exception e) {
				displayAndLogError(parent, "Could not connect to MJPEG Stream at: " + url, e);
				return;
			}
			break;

		case EPICS_ARRAY:
			stream = new EpicsV3DynamicDatasetConnector(camConfig.getArrayPv());
			try {
				stream.connect();
			} catch (DatasetException e) {
				displayAndLogError(parent, "Could not connect to EPICS Array Stream PV: " + camConfig.getArrayPv() + ":ArrayData" , e);
				return;
			}
			break;

		default:
			String message = "Stream type '" + streamType + "' not supported";
			displayAndLogError(parent, message);
			throw new RuntimeException(message);
		}

		// Connect the stream to the trace
		iTrace.setDynamicData(stream);

		// Add the listener for updating the frame counter
		stream.addDataListener(shapeListener);

		// Reset the frame counter
		frameCounter = 0;
	}

	private void reset() {
		// Call setupStream again with the current stream type. Creates a new stream resetting it.
		setupStream(streamType);
	}

}
