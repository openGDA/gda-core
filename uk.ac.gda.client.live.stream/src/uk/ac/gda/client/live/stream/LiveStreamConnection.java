/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.epics.connector.EpicsV3DynamicDatasetConnector;
import uk.ac.gda.client.live.stream.calibration.CalibratedAxesProvider;
import uk.ac.gda.client.live.stream.event.OpenConnectionEvent;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * An instance of this class encapsulates a connection to a live stream, i.e. a camera, as defined by a
 * {@link CameraConfiguration} and a {@link StreamType}.
 *
 * Publishes {@link OpenConnectionEvent} when a connection is created
 *
 * @author Matthew Dickie
 * @author Maurizio Nagni
 */
public class LiveStreamConnection implements IConnection, ILiveStreamConnection {

	/**
	 * The Connection Universal Unique ID UUID, Type 4 ,pseudo randomly generated
	 *
	 * @see "https://www.ietf.org/rfc/rfc4122.txt"
	 */
	private final UUID id;

	@FunctionalInterface
	public interface IAxisChangeListener {
		public void axisChanged();
	}

	private static final long MJPEG_DEFAULT_SLEEP_TIME = 50; // ms i.e. 20 fps
	private static final int MJPEG_DEFAULT_CACHE_SIZE = 3; // frames

	private final CameraConfiguration cameraConfig;

	private final StreamType streamType;

	private IDatasetConnector stream;

	private boolean connected;

	private IDataset xAxisDataset;

	private IDataset yAxisDataset;

	private IDataListener axesUpdater;

	private final Logger logger = LoggerFactory.getLogger(LiveStreamConnection.class);

	private final Set<IAxisChangeListener> axisChangeListeners = new HashSet<>(4);

	private AtomicInteger connectionCount = new AtomicInteger(0);

	public LiveStreamConnection(CameraConfiguration cameraConfig, StreamType streamType) {
		this.cameraConfig = cameraConfig;
		this.streamType = streamType;
		this.id = UUID.randomUUID();
	}

	/**
	 * Returns this connection unique id
	 *
	 * @return the UUID associated with this connection
	 */
	@Override
	public UUID getId() {
		return id;
	}

	public synchronized IDatasetConnector connect(IDatasetConnector stream) throws LiveStreamException {
		if (getStream() != null) {
			increaseConnectionCount();
			return stream;
		}



		// Attach the IDatasetConnector of the MJPEG stream to the trace.
		logger.debug("Connecting to live stream");
		setStream(stream);
		setupAxes();
		connected = true;
		increaseConnectionCount();
		return stream;
	}

	public synchronized IDatasetConnector connect() throws LiveStreamException {
		if (getStream() != null) {
			increaseConnectionCount();
			return stream;
		}

		if (streamType == StreamType.MJPEG && getCameraConfig().getUrl() == null) {
			throw new LiveStreamException(
					"MJPEG stream requested but no url defined for " + getCameraConfig().getName());
		}
		if (streamType == StreamType.EPICS_ARRAY && getCameraConfig().getArrayPv() == null) {
			throw new LiveStreamException(
					"EPICS stream requested but no array PV defined for " + getCameraConfig().getName());
		}

		// Attach the IDatasetConnector of the MJPEG stream to the trace.
		logger.debug("Connecting to live stream");
		switch (streamType) {
		case MJPEG:
			setStream(setupMpegStream());
			break;
		case EPICS_ARRAY:
			setStream(setupEpicsArrayStream());
			break;
		default:
			throw new LiveStreamException("Stream type '" + streamType + "' not supported");
		}

		setupAxes();
		connected = true;
		increaseConnectionCount();
		SpringApplicationContextProxy.publishEvent(new OpenConnectionEvent(this));
		return stream;
	}

	@Override
	public synchronized void disconnect() throws LiveStreamException {
		decreaseConnectionCount();
		//if (getConnectionCount() == 0) {
			if (getStream() != null) { // Will be null the first time
				try {
					CalibratedAxesProvider calibratedAxesProvider = getCameraConfig().getCalibratedAxesProvider();
					if (calibratedAxesProvider != null) {
						calibratedAxesProvider.disconnect();
						stream.removeDataListener(axesUpdater);
					}
					getStream().disconnect();
				} catch (Exception e) {
					throw new LiveStreamException("Error disconnecting from live stream", e);
				} finally {
					stream = null;
				}
			}

			xAxisDataset = null;
			yAxisDataset = null;
			connected = false;
		//}
	}

	@Override
	public void addAxisMoveListener(IAxisChangeListener axisMoveListener) {
		axisChangeListeners.add(axisMoveListener);
	}

	@Override
	public void removeAxisMoveListener(IAxisChangeListener axisMoveListener) {
		axisChangeListeners.remove(axisMoveListener);
	}

	@Override
	public void addDataListenerToStream(IDataListener listener) throws LiveStreamException {
		if (getStream() == null) {
			throw new LiveStreamException("Stream is not connected.");
		}
		getStream().addDataListener(listener);
	}

	@Override
	public void removeDataListenerFromStream(IDataListener listener) {
		if (getStream() != null) {
			stream.removeDataListener(listener);
		}
	}

	@Override
	public CameraConfiguration getCameraConfig() {
		return cameraConfig;
	}

	@Override
	public StreamType getStreamType() {
		return streamType;
	}

	private IDatasetConnector setupEpicsArrayStream() throws LiveStreamException {
		try {
			setStream(new EpicsV3DynamicDatasetConnector(getCameraConfig().getArrayPv()));
			return getStream();
		} catch (NoClassDefFoundError e) {
			// As uk.ac.gda.epics is an optional dependency if it is not included in the client. The code will fail
			// here.
			throw new LiveStreamException("Could not connect to EPICS stream, Is uk.ac.gda.epics in the product?", e);
		}
	}

	private IDatasetConnector setupMpegStream() throws LiveStreamException {
		final URL url;
		try {
			url = new URL(getCameraConfig().getUrl());
		} catch (MalformedURLException e) {
			throw new LiveStreamException("Malformed URL check camera configuration", e);
		}

		// If sleepTime or cacheSize are set use them, else use the defaults
		final long sleepTime = getCameraConfig().getSleepTime() != 0 ? cameraConfig.getSleepTime()
				: MJPEG_DEFAULT_SLEEP_TIME; // ms
		final int cacheSize = getCameraConfig().getCacheSize() != 0 ? cameraConfig.getCacheSize()
				: MJPEG_DEFAULT_CACHE_SIZE; // frames

		try {
			if (cameraConfig.isRgb()) {
				stream = PlatformUI.getWorkbench().getService(IRemoteDatasetService.class).createMJPGDataset(url,
						sleepTime, cacheSize);
			} else {
				stream = PlatformUI.getWorkbench().getService(IRemoteDatasetService.class)
						.createGrayScaleMJPGDataset(url, sleepTime, cacheSize);
			}
			stream.connect();
			return stream;
		} catch (Exception e) {
			throw new LiveStreamException("Could not connect to MJPEG Stream at: " + url, e);
		}
	}

	/**
	 * Sets up the x and y axis of the live stream, if the {@link CameraConfiguration} has been set in the Spring
	 * configuration.
	 *
	 * @throws LiveStreamException
	 */
	private void setupAxes() {
		final CalibratedAxesProvider calibratedAxesProvider = getCameraConfig().getCalibratedAxesProvider();
		if (calibratedAxesProvider == null)
			return;
		logger.debug("Setting up axes");
		calibratedAxesProvider.connect();
		this.axesUpdater = new AxesUpdater(calibratedAxesProvider);
		stream.addDataListener(axesUpdater);

		xAxisDataset = calibratedAxesProvider.getXAxisDataset();
		yAxisDataset = calibratedAxesProvider.getYAxisDataset();
	}

	public List<IDataset> getAxes() {
		if (xAxisDataset == null || yAxisDataset == null) {
			return null; // returns null rather than empty list to indicate that axes have not been configured
							// (therefore NOSONAR please)
		}

		return Arrays.asList(xAxisDataset, yAxisDataset);
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	public boolean hasAxesProvider() {
		return getCameraConfig().getCalibratedAxesProvider() != null;
	}

	public int getConnectionCount() {
		return connectionCount.get();
	}

	private void increaseConnectionCount() {
		connectionCount.incrementAndGet();
	}

	private void decreaseConnectionCount() {
		if (getConnectionCount() > 0) {
			connectionCount.decrementAndGet();
		}
	}

	public IDatasetConnector getStream() {
		return stream;
	}


	/**
	 * Utility method to compare this instance with the essential component of a LiveStreamConnection object
	 * @param cameraConfig
	 * @param streamType
	 * @return <code>true</code> if the configuration is the same, <code>false</code> in any other case
	 */
	public final boolean sameConfiguration(final CameraConfiguration cameraConfig, final StreamType streamType) {
		return cameraConfig.equals(getCameraConfig()) && streamType.equals(getStreamType());
	}

	private void setStream(IDatasetConnector stream) throws LiveStreamException {
		if (stream == null) {
			return;
		}

		if (getStream() == null) {
			try {
				stream.connect(5, TimeUnit.SECONDS);
				this.stream = stream;
			} catch (DatasetException e) {
				throw new LiveStreamException(
						"Could not connect to EPICS Array Stream PV: " + cameraConfig.getArrayPv(), e);
			}
		}
	}

	class AxesUpdater implements IDataListener {

		private int[] streamDataShape;
		private final CalibratedAxesProvider calibratedAxesProvider;

		public AxesUpdater(CalibratedAxesProvider calibratedAxesProvider) {
			this.calibratedAxesProvider = calibratedAxesProvider;
		}

		@Override
		public void dataChangePerformed(DataEvent dataEvent) {
			if (!Arrays.equals(streamDataShape, dataEvent.getShape())) {
				calibratedAxesProvider.resizeStream(dataEvent.getShape());
				streamDataShape = dataEvent.getShape();
			}

			final IDataset xDataset = calibratedAxesProvider.getXAxisDataset();
			final IDataset yDataset = calibratedAxesProvider.getYAxisDataset();

			if (xDataset.equals(xAxisDataset) && yDataset.equals(yAxisDataset)) {
				// no change
				return;
			}

			xAxisDataset = xDataset;
			yAxisDataset = yDataset;
			fireAxisMoveListeners();
		}

		private void fireAxisMoveListeners() {
			for (IAxisChangeListener axisChangeListener : axisChangeListeners) {
				axisChangeListener.axisChanged();
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LiveStreamConnection other = (LiveStreamConnection) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
