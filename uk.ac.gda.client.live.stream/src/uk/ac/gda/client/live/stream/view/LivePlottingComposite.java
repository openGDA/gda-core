/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.dawnsci.analysis.dataset.roi.XAxisLineBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import uk.ac.diamond.scisoft.analysis.plotclient.ScriptingConnection;
import uk.ac.gda.client.event.RootCompositeAware;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnection.IAxisChangeListener;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.event.ListenToConnectionEvent;
import uk.ac.gda.client.live.stream.event.PlottingSystemUpdateEvent;
import uk.ac.gda.client.live.stream.event.StopListenToConnectionEvent;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Manages an {@link IPlottingSystem} to display a live stream from a camera
 *
 * Publishes {@link PlottingSystemUpdateEvent} when starts listening at the inner stream
 */
public class LivePlottingComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(LivePlottingComposite.class);

	private static final String LIVE_CAMERA_STREAM = "Live camera stream";

	private LiveStreamConnection liveStreamConnection;
	private IPlottingSystem<Composite> plottingSystem;

	private ScriptingConnection scriptingConnection;
	private IImageTrace iTrace;
	private IDatasetConnector dataset;
	private AtomicLong frameCounter = new AtomicLong();

	private boolean showAxes;
	private boolean showTitle;
	private boolean connected;

	private final IAxisChangeListener axisChangeListener = this::updateAxes;
	private IDataListener dataShapeChangeListener;
	private IDataListener titleUpdateListener;

	/**
	 * Simple constructor for use in wizard pages etc. where there are no action bars or workspace part
	 * <p>
	 * For a description of parameters, see
	 * {@link #LivePlottingComposite(Composite parent, int style, String plotName, IActionBars actionBars, IWorkbenchPart part)}
	 *
	 * @throws GDAClientException
	 */
	public LivePlottingComposite(Composite parent, int style, String plotName,
			LiveStreamConnection liveStreamConnection) throws GDAClientException {
		this(parent, style, liveStreamConnection);
		preparePlottingSystem(plotName, null, null);
	}

	public LivePlottingComposite(Composite parent, int style, String plotName, IActionBars actionBars,
			LiveStreamConnection liveStreamConnection, IWorkbenchPart workbenchPart) throws GDAClientException {
		this(parent, style, liveStreamConnection);
		preparePlottingSystem(plotName, actionBars, workbenchPart);
	}

	/**
	 * Create LivePlottingComposite
	 *
	 * @param parent
	 *            a widget which will be the parent of the new instance (cannot be null)
	 * @param style
	 *            style the style of widget to construct
	 * @param plotName
	 *            name of plot (passed to plotting system
	 * @param actionBars
	 *            of the parent: can be null (e.g. if the parent is not a ViewPart)
	 * @param part
	 *            parent ViewPart: can be null (e.g. if the parent is not a ViewPart)
	 * @throws GDAClientException
	 *             when cannot comunicate with {@link SpringApplicationContextProxy} or when cannot prepare
	 *             {@link IPlottingSystem}
	 */
	public LivePlottingComposite(Composite parent, int style, String plotName, IActionBars actionBars,
			IWorkbenchPart part) throws GDAClientException {
		this(parent, style, null);
		preparePlottingSystem(plotName, actionBars, part);
	}

	private LivePlottingComposite(Composite parent, int style, LiveStreamConnection liveStreamConnection)
			throws GDAClientException {
		super(parent, style);
		this.liveStreamConnection = liveStreamConnection;
		setLayout(new FillLayout());
		SpringApplicationContextProxy.addApplicationListener(openConnectionListener);
		SpringApplicationContextProxy.addApplicationListener(closeConnectionListener);
		addDisposeListener(e -> {
			this.dispose();
		});
	}

	private void preparePlottingSystem(String plotName, IActionBars actionBars, IWorkbenchPart part)
			throws GDAClientException {
		try {
			final IPlottingService plottingService = PlatformUI.getWorkbench().getService(IPlottingService.class);
			plottingSystem = plottingService.createPlottingSystem();
			plottingSystem.createPlotPart(this, plotName, actionBars, PlotType.IMAGE, part);
			createScriptingConnection(plotName);

			// Fix the aspect ratio as is typically required for visible cameras
			plottingSystem.setKeepAspect(true); // Setting this to false will
			// result in the toolbar button initially showing the wrong state.

			// Disable auto rescale as the live stream is constantly refreshing
			plottingSystem.setRescale(false);
			if (this.liveStreamConnection != null && this.liveStreamConnection.isConnected()) {
				activatePlottingSystem();
			}
		} catch (Exception e) {
			throw new GDAClientException(e);
		}
		GridDataFactory.fillDefaults().grab(true, true).applyTo(this);
	}

	private void updateAxesVisibility() {
		for (IAxis axis : plottingSystem.getAxes()) {
			axis.setVisible(showAxes);
		}
	}

	private void updateTitleVisibility() {
		try {
			if (showTitle) {
				final CameraConfiguration camConfig = liveStreamConnection.getCameraConfig();
				final StreamType streamType = liveStreamConnection.getStreamType();
				// Use the full camera name from the camera configuration, if available, for the plot title as it should
				// better describe the camera and we should have plenty of space for it.
				plottingSystem.setTitle(camConfig.getName() + ": " + streamType + " - No data yet");
				liveStreamConnection.addDataListenerToStream(updateTitleUpdateListener());
			} else {
				plottingSystem.setTitle("");
				Optional.ofNullable(titleUpdateListener).ifPresent(liveStreamConnection::removeDataListenerFromStream);
			}
		} catch (LiveStreamException e) {
			// This will probably never happen, as getStream() should not fail once the stream is connected
			logger.error("Error showing/hiding title");
		}
	}

	/**
	 * Connect trace to plotting system.
	 *
	 * @throws LiveStreamException
	 */
	public void activatePlottingSystem() throws LiveStreamException {
		if (connected) {
			return;
		}

		logger.debug("Connecting to plot {}", plottingSystem.getPlotName());

		// Create a new trace.
		iTrace = plottingSystem.createImageTrace(LIVE_CAMERA_STREAM);

		// Connect the stream to the trace
		if (liveStreamConnection.isConnected()) {
			dataset = liveStreamConnection.getStream();
		} else {
			dataset = liveStreamConnection.connect();
		}

		iTrace.setDynamicData(dataset);

		// Add the axes to the trace
		final List<IDataset> axes = liveStreamConnection.getAxes();
		if (liveStreamConnection.hasAxesProvider() || (axes != null && axes.size() == 2)) {
			iTrace.setAxes(liveStreamConnection.getAxes(), false);
			liveStreamConnection.addAxisMoveListener(axisChangeListener);
		}
		setShowAxes(liveStreamConnection.getCameraConfig().getCalibratedAxesProvider() != null);
		updateAxesVisibility();
		updateTitleVisibility();

		// Add the listener for updating the frame counter
		dataset.addDataListener(updateDataShapeChangeListener());

		// Reset the frame counter
		frameCounter.set(0);

		// Try and make the stream run faster
		iTrace.setDownsampleType(DownsampleType.POINT);
		iTrace.setRescaleHistogram(false);

		// Plot the new trace.
		plottingSystem.addTrace(iTrace);
		ClientSWTElements.findParentUUID(getParent()).ifPresent(uuidRoot -> SpringApplicationContextProxy
				.publishEvent(new PlottingSystemUpdateEvent(LivePlottingComposite.this, uuidRoot, plottingSystem)));
		connected = true;
	}

	private void createScriptingConnection(String partName) {
		Objects.requireNonNull(plottingSystem, "Plotting system is not initialised");
		scriptingConnection = new ScriptingConnection(partName);
		scriptingConnection.setPlottingSystem(plottingSystem);
	}

	private void updateAxes() {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		display.asyncExec(() -> iTrace.setAxes(liveStreamConnection.getAxes(), false));
	}

	public void setCrosshairs(double xPos, double yPos) throws Exception {
		if (connected) {
			final int[] maxShape = dataset.getMaxShape();
			if (maxShape != null && maxShape.length > 1) {
				final Color crosshairColour = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

				// X-axis crosshair
				final IRegion xRegion = plottingSystem
						.createRegion(RegionUtils.getUniqueName("Crosshair X", plottingSystem), RegionType.XAXIS_LINE);
				xRegion.setRegionColor(crosshairColour);
				xRegion.setROI(new XAxisLineBoxROI(xPos, 0, 6, maxShape[1], 0));
				plottingSystem.addRegion(xRegion);

				// Y-axis crosshair
				final IRegion yRegion = plottingSystem
						.createRegion(RegionUtils.getUniqueName("Crosshair Y", plottingSystem), RegionType.YAXIS_LINE);
				yRegion.setRegionColor(crosshairColour);
				yRegion.setROI(new YAxisBoxROI(0, yPos, maxShape[0], 6, 0));
				plottingSystem.addRegion(yRegion);
			}
		}
	}

	public void disconnect() {
		if (connected) {
			// Disconnect from the plotting system
			// Note that this does not disconnect the live stream connection, as this may be shared with other views.
			logger.debug("Disconnecting from plot {}", plottingSystem.getPlotName());
			plottingSystem.removeTrace(iTrace);
			if (dataset != null) {
				dataset.removeDataListener(dataShapeChangeListener);
				dataShapeChangeListener = null;
				dataset.removeDataListener(titleUpdateListener);
				titleUpdateListener = null;
				dataset = null;
			}
			liveStreamConnection.removeAxisMoveListener(axisChangeListener);
			iTrace.dispose();
			connected = false;
		}
	}

	@Override
	public void dispose() {
		disconnect();

		if (plottingSystem != null) {
			plottingSystem.dispose();
		}
		if (scriptingConnection != null) {
			scriptingConnection.dispose();
			scriptingConnection = null;
		}
	}

	public IImageTrace getITrace() {
		return iTrace;
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}

	public boolean isShowAxes() {
		return showAxes;
	}

	public void setShowAxes(boolean showAxes) {
		this.showAxes = showAxes;
		if (connected) {
			updateAxesVisibility();
		}
	}

	/**
	 * See comment on {@link #setShowTitle(boolean)}
	 *
	 * @return {@code true} if title is shown, {@code false} if it is not shown
	 */
	public boolean isShowTitle() {
		return showTitle;
	}

	/**
	 * TODO: Consider whether we should remove this function and specify in the constructor whether the title should be
	 * shown.<br>
	 * There is probably no reason to allow the title to be added or removed after the view is shown.
	 *
	 * @param showTitle
	 *            {@code true} to show the title, {@code false} to hide it
	 */
	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
		if (connected) {
			updateTitleVisibility();
		}
	}

	private boolean processEvent(RootCompositeAware event, UUID uuidRoot) {
			return uuidRoot.equals(event.getRootComposite());
	}

	private IDataListener updateDataShapeChangeListener() {
		dataShapeChangeListener = new IDataListener() {
			private int[] oldShape;

			@Override
			public void dataChangePerformed(DataEvent evt) {
				final Display display = PlatformUI.getWorkbench().getDisplay();
				// Check if the shape has changed, if so rescale
				if (!Arrays.equals(evt.getShape(), oldShape)) {
					oldShape = evt.getShape();
					// Need to be in the UI thread to do rescaling
					display.asyncExec(() -> {
						if (plottingSystem != null && !plottingSystem.isDisposed()) {
							plottingSystem.autoscaleAxes();
							iTrace.rehistogram();
						}
					});
					updateAxes();
				}
			}
		};
		return dataShapeChangeListener;
	}

	private IDataListener updateTitleUpdateListener() {
		titleUpdateListener = new IDataListener() {
			private long lastUpdateTime = System.nanoTime(); // Initialise
																// to
																// make
																// first
																// call
																// to
																// getFps
																// more
																// reasonable

			@Override
			public void dataChangePerformed(DataEvent evt) {
				// Build the new title while not in the UI thread
				final String newTitle = buildTitle();
				// Update the plot title in the UI thread
				Display.getDefault().asyncExec(() -> {
					if (plottingSystem != null && !plottingSystem.isDisposed()) {
						plottingSystem.setTitle(newTitle);
					}
				});
			}

			private String buildTitle() {
				final double fps = getFps();
				final String cameraName = liveStreamConnection.getCameraConfig().getName();
				return String.format("%s: %s - Frame: %d (%.1f fps)", cameraName, liveStreamConnection.getStreamType(),
						frameCounter.incrementAndGet(), fps);
			}

			private double getFps() {
				final long now = System.nanoTime();
				final long timeDiff = now - lastUpdateTime;
				lastUpdateTime = now; // Cache
										// for
										// next
										// frame
				return 1 / (timeDiff * 1e-9);
			}
		};
		return titleUpdateListener;
	}

	ApplicationListener<ListenToConnectionEvent> openConnectionListener = new ApplicationListener<ListenToConnectionEvent>() {
		@Override
		public void onApplicationEvent(ListenToConnectionEvent event) {
			if (!ClientSWTElements.findParentUUID(getParent()).map(uuid -> processEvent(event, uuid)).orElse(false)) {
				return;
			}
			if (LiveStreamConnection.class.isAssignableFrom(event.getSource().getClass())) {
				liveStreamConnection = LiveStreamConnection.class.cast(event.getSource());
				try {
					activatePlottingSystem();
				} catch (LiveStreamException e) {
					logger.error("Canot activate Plotting", e);
				}
			}
		}
	};

	ApplicationListener<StopListenToConnectionEvent> closeConnectionListener = new ApplicationListener<StopListenToConnectionEvent>() {
		@Override
		public void onApplicationEvent(StopListenToConnectionEvent event) {
			// Avoids disposed widget
			if (isDisposed() || getParent().isDisposed()) {
				return;
			}
			if (!ClientSWTElements.findParentUUID(getParent()).map(uuid -> processEvent(event, uuid)).orElse(false)) {
				return;
			}
			if (LiveStreamConnection.class.isAssignableFrom(event.getSource().getClass())) {
				disconnect();
			}
		}
	};
}
