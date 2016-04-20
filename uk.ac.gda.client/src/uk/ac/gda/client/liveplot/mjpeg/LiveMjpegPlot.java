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

package uk.ac.gda.client.liveplot.mjpeg;

import gda.factory.Finder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.DataEvent;
import org.eclipse.dawnsci.analysis.api.dataset.IDataListener;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.IRemoteDataset;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.plotting.api.IPlotActionSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A RCP view for connecting to and displaying a live MJPEG stream. The intension is to provide a easy way for camera to be integrated into GDA.
 *
 * @author James Mudd
 */
public class LiveMjpegPlot extends ViewPart {

	public static final String ID = "uk.ac.gda.client.liveplot.mjpeg.LiveMJPEGView";

	private static final Logger logger = LoggerFactory.getLogger(LiveMjpegPlot.class);

	private static final long DEFAULT_SLEEP_TIME = 50; // ms i.e. 20 fps
	private static final int DEFAULT_CACHE_SIZE = 3; // frames

	private static IPlottingService plottingService;
	private static IRemoteDatasetService remoteDatasetService;

	public synchronized void setPlottingService(IPlottingService plottingService) {
		logger.debug("Plotting service set by call to: {}", this);
		LiveMjpegPlot.plottingService = plottingService;
	}

	public synchronized void setRemoteDatasetService(IRemoteDatasetService remoteDatasetService) {
		logger.debug("Remote Dataset service set by call to: {}", this);
		LiveMjpegPlot.remoteDatasetService = remoteDatasetService;
	}

	private IPlottingSystem<Composite> plottingSystem;

	private IRemoteDataset stream;
	private IDataListener shapeListener;

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
			// Find all the implemented cameras
			List<CameraConfiguration> cameras = Finder.getInstance().listFindablesOfType(CameraConfiguration.class);
			final Map<String, CameraConfiguration> cameraMap = new HashMap<String, CameraConfiguration>();
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
				parent.setLayout(new GridLayout(3, false));
				parent.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

				Label cameraSelectorLabel = new Label(parent, SWT.NONE);
				cameraSelectorLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
				cameraSelectorLabel.setText("Select camera:");

				final org.eclipse.swt.widgets.List cameraSelector = new org.eclipse.swt.widgets.List(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
				cameraSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				cameraSelector.setItems(cameraMap.keySet().toArray(new String[0]));
				cameraSelector.setSelection(0);

				Button connectButton = new Button(parent, SWT.DEFAULT);
				connectButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
				connectButton.setText("Connect");
				connectButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						// Get the cameras id for the secondary id
						reopenViewWithSecondaryId(cameraMap.get(cameraSelector.getItem(cameraSelector.getSelectionIndex())).getName());
					}
				});

			} else { // No cameras found
				displayAndLogError(parent, "No cameras were found");
			}
			return;
		}
	}

	/**
	 * This is the method that actually creates a MJPEG stream and sets up the plotting system.
	 * <p>
	 * To get here the secondary id of the view need to be set (hopefully to a valid camera id)
	 *
	 * @param parent
	 *            Composite to draw on
	 * @param cameraId
	 *            The name of the camera to use
	 */
	private void createLivePlot(Composite parent, String cameraId) {

		// Get the camera config from the finder
		CameraConfiguration camConfig = Finder.getInstance().find(cameraId);

		if (camConfig == null) {
			displayAndLogError(parent, "Camera configuration could not be found for the specified camera ID");
			return;
		}

		URL url;
		try {
			url = new URL(camConfig.getUrl());
		} catch (MalformedURLException e) {
			displayAndLogError(parent, "Malformed URL check camera configuration", e);
			return;
		}

		// If sleepTime or cacheSize are set use them else use the defaults
		long sleepTime = camConfig.getSleepTime() != 0 ? camConfig.getSleepTime() : DEFAULT_SLEEP_TIME; // ms
		int cacheSize = camConfig.getCacheSize() != 0 ? camConfig.getCacheSize() : DEFAULT_CACHE_SIZE; // frames

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
		plottingSystem.setTitle(cameraId);

		// Add useful plotting system actions
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		IPlotActionSystem plotActionSystem = plottingSystem.getPlotActionSystem();
		plotActionSystem.fillToolActions(toolBarManager, ToolPageRole.ROLE_2D);
		plotActionSystem.fillZoomActions(toolBarManager);
		plotActionSystem.fillPrintActions(toolBarManager);
		getViewSite().getActionBars().updateActionBars();

		// Try and make the stream run faster
		ITrace trace = plottingSystem.createPlot2D((IDataset) stream, null, null);
		final IImageTrace iTrace = (IImageTrace) trace;
		iTrace.setDownsampleType(DownsampleType.POINT);
		iTrace.setRescaleHistogram(false);
		// Fix the aspect ratio as is typically required for visible cameras
		plottingSystem.setKeepAspect(true);
		// Disable auto rescale as the live stream is constantly refreshing
		plottingSystem.setRescale(false);

		// Do some things to make the UI a bit more friendly
		if (camConfig.getDisplayName() != null) {
			setPartName(camConfig.getDisplayName());
		} else {
			setPartName(camConfig.getName());
		}

		shapeListener = new IDataListener() {
			int[] oldShape;
			@Override
			public void dataChangePerformed(DataEvent evt) {
				if (!Arrays.equals(evt.getShape(), oldShape)) {
					oldShape = evt.getShape();
					// Need to be in the UI thread to do rescaling
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							plottingSystem.autoscaleAxes();
							iTrace.rehistogram();
						}
					});
				}
			}
		};
		stream.addDataListener(shapeListener);
	}

	@Override
	public void setFocus() {
		if (plottingSystem != null) {
			plottingSystem.setFocus();
		}
	}

	private void displayAndLogError(final Composite parent, final String errorMessage) {
		Label errorLabel = new Label(parent, SWT.NONE);
		errorLabel.setText(errorMessage);
		parent.layout(true);
		logger.error(errorMessage);
	}

	private void displayAndLogError(final Composite parent, final String errorMessage, final Exception exception) {
		Label errorLabel = new Label(parent, SWT.NONE);
		errorLabel.setText(errorMessage);
		parent.layout(true);
		logger.error(errorMessage, exception);
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
					shapeListener = null;
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
			page.showView(LiveMjpegPlot.ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			logger.error("Error activating Live MJPEG view with secondary ID {}", secondaryId, e);
		}
	}
}
