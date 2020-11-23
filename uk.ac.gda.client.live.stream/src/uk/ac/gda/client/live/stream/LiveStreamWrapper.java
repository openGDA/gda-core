/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDatasetChangeChecker;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * Wraps an {@link IDatasetConnector} with reference counting to allow it to be shared by multiple connections.
 */
public class LiveStreamWrapper implements IDatasetConnector {
	private static Logger logger = LoggerFactory.getLogger(LiveStreamWrapper.class);

	private IDatasetConnector stream;

	private AtomicInteger connectionCount = new AtomicInteger(0);

	/** Camera configuration used to create the current stream */
	private CameraConfiguration cameraConfig;

	/** Type of the current stream */
	private StreamType streamType;

	public LiveStreamWrapper(CameraConfiguration cameraConfig, StreamType streamType) {
		this.cameraConfig = cameraConfig;
		this.streamType = streamType;
	}

	/**
	 * Connect to an existing dataset.<br>
	 * Callers should use {@link #isConfigCompatible(CameraConfiguration, StreamType)} to check that the wrapped stream
	 * is compatible before calling this function.
	 *
	 * @throws DatasetException
	 */
	@Override
	public String connect() throws DatasetException {
		if (stream == null) {
			try {
				stream = IDatasetConnectorFactory.getDatasetConnector(cameraConfig, streamType);
			} catch (LiveStreamException e) {
				throw new DatasetException("Error connecting to live stream", e);
			}
		}
		connectionCount.incrementAndGet();
		return stream.getDatasetName();
	}

	@Override
	public void disconnect() {
		final int newConnectionCount = connectionCount.decrementAndGet();
		if (newConnectionCount < 1 && stream != null) {
			try {
				stream.disconnect();
			} catch (DatasetException e) {
				logger.error("Error disconnecting from stream", e);
			} finally {
				stream = null;
			}
		}
	}

	/**
	 * Check that the given {@link CameraConfiguration} & {@link StreamType} are compatible with the values used to
	 * create this wrapper.
	 * <p>
	 * We check the parameters that are used to create the stream, not the ones that are GUI-related.
	 */
	public boolean isConfigCompatible(CameraConfiguration cameraConfig, StreamType streamType) {
		return streamType == this.streamType
				&& getConnectionString(cameraConfig, streamType).equals(getConnectionString(this.cameraConfig, this.streamType))
				&& cameraConfig.getSleepTime() == this.cameraConfig.getSleepTime()
				&& cameraConfig.getCacheSize() == this.cameraConfig.getCacheSize()
				&& cameraConfig.isRgb() == this.cameraConfig.isRgb();
	}

	/**
	 * Get the string (URL/PV) from the {@link CameraConfiguration} that is used to create the stream.<br>
	 * The field to be read will depend on the {@link StreamType} required.
	 */
	private String getConnectionString(CameraConfiguration cameraConfig, StreamType streamType) {
		switch (streamType) {
		case MJPEG:
			return cameraConfig.getUrl();
		case EPICS_ARRAY:
			return cameraConfig.getArrayPv();
		case EPICS_PVA:
			return cameraConfig.getPvAccessPv();
		default:
			// This should never happen
			return "";
		}
	}

	public int getConnectionCount() {
		return connectionCount.get();
	}

	@Override
	public String toString() {
		return "LiveStreamWrapper [stream=" + stream + ", connectionCount=" + connectionCount + ", cameraConfig="
				+ cameraConfig + ", streamType=" + streamType + "]";
	}

	//-------------------------------------------------------------------------
	// Remaining functions in IDatasetConnector interface
	//-------------------------------------------------------------------------
	@Override
	public String getPath() {
		return stream.getPath();
	}

	@Override
	public void setPath(String path) {
		stream.setPath(path);
	}

	@Override
	public ILazyDataset getDataset() {
		return stream.getDataset();
	}

	@Override
	public boolean resize(int... newShape) {
		return stream.resize(newShape);
	}

	@Override
	public int[] getMaxShape() {
		return stream.getMaxShape();
	}

	@Override
	public void setMaxShape(int... maxShape) {
		stream.setMaxShape(maxShape);
	}

	@Override
	public void startUpdateChecker(int milliseconds, IDatasetChangeChecker checker) {
		stream.startUpdateChecker(milliseconds, checker);
	}

	@Override
	public boolean refreshShape() {
		return stream.refreshShape();
	}

	@Override
	public void addDataListener(IDataListener l) {
		stream.addDataListener(l);
	}

	@Override
	public void removeDataListener(IDataListener l) {
		stream.removeDataListener(l);
	}

	@Override
	public void fireDataListeners() {
		stream.fireDataListeners();
	}

	@Override
	public String getDatasetName() {
		return stream.getDatasetName();
	}

	@Override
	public void setDatasetName(String datasetName) {
		stream.setDatasetName(datasetName);
	}

	@Override
	public void setWritingExpected(boolean expectWrite) {
		stream.setWritingExpected(expectWrite);
	}

	@Override
	public boolean isWritingExpected() {
		return stream.isWritingExpected();
	}

	@Override
	public String connect(long time, TimeUnit unit) throws DatasetException {
		logger.warn("connect() specifying timeout not supported: using defaults");
		return connect();
	}
}
