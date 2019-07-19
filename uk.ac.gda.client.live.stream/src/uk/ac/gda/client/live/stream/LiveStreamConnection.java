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

import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.epics.connector.EpicsV3DynamicDatasetConnector;
import uk.ac.gda.client.live.stream.calibration.CalibratedAxesProvider;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * An instance of this class encapsulates a connection to a live stream, i.e. a camera,
 * as defined by a {@link CameraConfiguration} and a {@link StreamType}.
 */
public class LiveStreamConnection {

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

	public LiveStreamConnection(CameraConfiguration cameraConfig, StreamType streamType) {
		this.cameraConfig = cameraConfig;
		this.streamType = streamType;
	}

	public IDatasetConnector connect() throws LiveStreamException {
		if (stream != null) {
			throw new LiveStreamException("Stream is already connected");
		}

		if (streamType == StreamType.MJPEG && cameraConfig.getUrl() == null) {
			throw new LiveStreamException("MJPEG stream requested but no url defined for " + cameraConfig.getName());
		}
		if (streamType == StreamType.EPICS_ARRAY && cameraConfig.getArrayPv() == null) {
			throw new LiveStreamException("EPICS stream requested but no array PV defined for " + cameraConfig.getName());
		}

		// Attach the IDatasetConnector of the MJPEG stream to the trace.
		logger.debug("Connecting to live stream");
		switch (streamType) {
			case MJPEG:
				stream = setupMpegStream();
				break;
			case EPICS_ARRAY:
				stream = setupEpicsArrayStream();
				break;
			default:
				throw new LiveStreamException("Stream type '" + streamType + "' not supported");
		}

		setupAxes();

		connected = true;
		return stream;
	}

	public IDatasetConnector getStream() throws LiveStreamException {
		if (!connected) {
			connect();
		}
		return stream;
	}

	public CameraConfiguration getCameraConfiguration() {
		return cameraConfig;
	}

	public void disconnect() throws LiveStreamException {
		// Disconnect the existing stream
		if (stream != null) { // Will be null the first time
			try {
				CalibratedAxesProvider calibratedAxesProvider = cameraConfig.getCalibratedAxesProvider();
				if (calibratedAxesProvider != null) {
					calibratedAxesProvider.disconnect();
					stream.removeDataListener(axesUpdater);
				}
				stream.disconnect();
			} catch (Exception e) {
				throw new LiveStreamException("Error disconnecting from live stream", e);
			} finally {
				stream = null;
			}
		}

		xAxisDataset = null;
		yAxisDataset = null;
		connected = false;
	}

	public void addAxisMoveListener(IAxisChangeListener axisMoveListener) {
		axisChangeListeners.add(axisMoveListener);
	}

	public void removeAxisMoveListener(IAxisChangeListener axisMoveListener) {
		axisChangeListeners.remove(axisMoveListener);
	}

	private IDatasetConnector setupEpicsArrayStream() throws LiveStreamException {
		try {
			stream = new EpicsV3DynamicDatasetConnector(cameraConfig.getArrayPv());
		} catch (NoClassDefFoundError e) {
			// As uk.ac.gda.epics is an optional dependency if it is not included in the client. The code will fail here.
			throw new LiveStreamException("Could not connect to EPICS stream, Is uk.ac.gda.epics in the product?", e);
		}
		try {
			stream.connect();
			return stream;
		} catch (DatasetException e) {
			throw new LiveStreamException("Could not connect to EPICS Array Stream PV: " + cameraConfig.getArrayPv(), e);
		}
	}

	private IDatasetConnector setupMpegStream() throws LiveStreamException {
		final URL url;
		try {
			url = new URL(cameraConfig.getUrl());
		} catch (MalformedURLException e) {
			throw new LiveStreamException("Malformed URL check camera configuration", e);
		}

		// If sleepTime or cacheSize are set use them, else use the defaults
		final long sleepTime = cameraConfig.getSleepTime() != 0 ? cameraConfig.getSleepTime() : MJPEG_DEFAULT_SLEEP_TIME; // ms
		final int cacheSize = cameraConfig.getCacheSize() != 0 ? cameraConfig.getCacheSize() : MJPEG_DEFAULT_CACHE_SIZE; // frames

		try {
			if (cameraConfig.isRgb()) {
				stream = PlatformUI.getWorkbench().getService(IRemoteDatasetService.class).createMJPGDataset(url, sleepTime, cacheSize);
			} else {
				stream = PlatformUI.getWorkbench().getService(IRemoteDatasetService.class).createGrayScaleMJPGDataset(url, sleepTime, cacheSize);
			}
			stream.connect();
			return stream;
		} catch (Exception e) {
			throw new LiveStreamException("Could not connect to MJPEG Stream at: " + url, e);
		}
	}

	/**
	 * Sets up the x and y axis of the live stream, if the {@link CameraConfiguration} has been set
	 * in the Spring configuration.
	 * @throws LiveStreamException
	 */
	private void setupAxes() {
		final CalibratedAxesProvider calibratedAxesProvider = cameraConfig.getCalibratedAxesProvider();
		if (calibratedAxesProvider == null) return;
		logger.debug("Setting up axes");
		calibratedAxesProvider.connect();
		this.axesUpdater = new AxesUpdater(calibratedAxesProvider);
		stream.addDataListener(axesUpdater);

		xAxisDataset = calibratedAxesProvider.getXAxisDataset();
		yAxisDataset = calibratedAxesProvider.getYAxisDataset();
	}


	public List<IDataset> getAxes() {
		if (xAxisDataset == null || yAxisDataset == null) {
			return null; // returns null rather than empty list to indicate that axes have not been configured (therefore NOSONAR please)
		}

		return Arrays.asList(xAxisDataset, yAxisDataset);
	}

	public StreamType getStreamType() {
		return streamType;
	}

	public boolean isConnected() {
		return connected;
	}

	public boolean hasAxesProvider() {
		return cameraConfig.getCalibratedAxesProvider() != null;
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

}
