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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.scanning.api.event.core.IConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.calibration.CalibratedAxesProvider;
import uk.ac.gda.client.live.stream.event.OpenConnectionEvent;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * An instance of this class encapsulates a connection to a live stream, i.e. a camera, as defined by a
 * {@link CameraConfiguration} and a {@link StreamType}.
 * <p>
 * Publishes {@link OpenConnectionEvent} when a connection is created
 * <p>
 * Instances of this class should be created using {@link LiveStreamConnectionBuilder} which will ensure it is attached
 * to a stream.
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
		void axisChanged();
	}

	private final CameraConfiguration cameraConfig;

	private final StreamType streamType;

	private IDatasetConnector stream;

	private boolean connected;

	private IDataset xAxisDataset;

	private IDataset yAxisDataset;

	private IDataListener axesUpdater;

	private final Logger logger = LoggerFactory.getLogger(LiveStreamConnection.class);

	private final Set<IAxisChangeListener> axisChangeListeners = new HashSet<>(4);

	private boolean initialised = false;

	LiveStreamConnection(CameraConfiguration cameraConfig, StreamType streamType) {
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

	public synchronized IDatasetConnector connect() throws LiveStreamException {
		try {
			stream.connect();
			if (!initialised) {
				setupAxes();
				initialised = true;
			}
		} catch (DatasetException e) {
			throw new LiveStreamException("Error connecting to live stream", e);
		}
		connected = true;
		return stream;
	}

	@Override
	public synchronized void disconnect() throws LiveStreamException {
		if (stream != null) { // Will be null the first time
			try {
				final CalibratedAxesProvider calibratedAxesProvider = getCameraConfig().getCalibratedAxesProvider();
				if (calibratedAxesProvider != null) {
					calibratedAxesProvider.disconnect();
					stream.removeDataListener(axesUpdater);
				}
				stream.disconnect();
			} catch (Exception e) {
				throw new LiveStreamException("Error disconnecting from live stream", e);
			} finally {
				initialised = false;
			}
		}

		xAxisDataset = null;
		yAxisDataset = null;
		connected = false;
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
		if (stream == null) {
			throw new LiveStreamException("Stream is not connected.");
		}
		stream.addDataListener(listener);
	}

	@Override
	public void removeDataListenerFromStream(IDataListener listener) {
		if (stream != null) {
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

	public IDatasetConnector getStream() {
		return stream;
	}

	public void setStream(IDatasetConnector stream) {
		this.stream = stream;
	}

	private class AxesUpdater implements IDataListener {

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
		result = prime * result + ((axesUpdater == null) ? 0 : axesUpdater.hashCode());
		result = prime * result + ((axisChangeListeners == null) ? 0 : axisChangeListeners.hashCode());
		result = prime * result + ((cameraConfig == null) ? 0 : cameraConfig.hashCode());
		result = prime * result + (connected ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (initialised ? 1231 : 1237);
		result = prime * result + ((logger == null) ? 0 : logger.hashCode());
		result = prime * result + ((stream == null) ? 0 : stream.hashCode());
		result = prime * result + ((streamType == null) ? 0 : streamType.hashCode());
		result = prime * result + ((xAxisDataset == null) ? 0 : xAxisDataset.hashCode());
		result = prime * result + ((yAxisDataset == null) ? 0 : yAxisDataset.hashCode());
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
		if (axesUpdater == null) {
			if (other.axesUpdater != null)
				return false;
		} else if (!axesUpdater.equals(other.axesUpdater))
			return false;
		if (axisChangeListeners == null) {
			if (other.axisChangeListeners != null)
				return false;
		} else if (!axisChangeListeners.equals(other.axisChangeListeners))
			return false;
		if (cameraConfig == null) {
			if (other.cameraConfig != null)
				return false;
		} else if (!cameraConfig.equals(other.cameraConfig))
			return false;
		if (connected != other.connected)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (initialised != other.initialised)
			return false;
		if (logger == null) {
			if (other.logger != null)
				return false;
		} else if (!logger.equals(other.logger))
			return false;
		if (stream == null) {
			if (other.stream != null)
				return false;
		} else if (!stream.equals(other.stream))
			return false;
		if (streamType != other.streamType)
			return false;
		if (xAxisDataset == null) {
			if (other.xAxisDataset != null)
				return false;
		} else if (!xAxisDataset.equals(other.xAxisDataset))
			return false;
		if (yAxisDataset == null) {
			if (other.yAxisDataset != null)
				return false;
		} else if (!yAxisDataset.equals(other.yAxisDataset))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LiveStreamConnection [id=" + id + ", cameraConfig=" + cameraConfig + ", streamType=" + streamType
				+ ", stream=" + stream + ", connected=" + connected + ", xAxisDataset=" + xAxisDataset
				+ ", yAxisDataset=" + yAxisDataset + ", axesUpdater=" + axesUpdater + ", logger=" + logger
				+ ", axisChangeListeners=" + axisChangeListeners + ", initialised=" + initialised + "]";
	}
}
