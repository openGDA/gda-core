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
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.epics.connector.EpicsV3DynamicDatasetConnector;
import uk.ac.gda.client.live.stream.view.CameraCalibration;
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

	private IScannable<? extends Number> xAxisScannable;
	private IDataset xAxisDataset;

	private IScannable<? extends Number> yAxisScannable;
	private IDataset yAxisDataset;

	private final Logger logger = LoggerFactory.getLogger(LiveStreamConnection.class);

	private final Set<IAxisChangeListener> axisChangeListeners = new HashSet<>(4);
	private final IPositionListener axisScannableMoveListener = new IPositionListener() {

		@Override
		public void positionChanged(PositionEvent event) throws ScanningException {
			try {
				if (stream != null) updateAxes();
				fireAxisMoveListeners();
			} catch (LiveStreamException e) {
				logger.error("Could not update axes for live stream", e);
			}
		}

	};

	public LiveStreamConnection(CameraConfiguration cameraConfig, StreamType streamType) {
		this.cameraConfig = cameraConfig;
		this.streamType = streamType;
	}

	public IDatasetConnector connect() throws LiveStreamException {
		if (stream != null) {
			throw new LiveStreamException("Stream is already connected");
		}

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

		return stream;
	}

	private void fireAxisMoveListeners() {
		for (IAxisChangeListener axisChangeListener : axisChangeListeners) {
			axisChangeListener.axisChanged();
		}
	}

	public IDatasetConnector getStream() {
		return stream;
	}

	public CameraConfiguration getCameraConfiguration() {
		return cameraConfig;
	}

	public void disconnect() throws LiveStreamException {
		// Disconnect the existing stream
		if (stream != null) { // Will be null the first time
			try {
				stream.disconnect();
			} catch (Exception e) {
				throw new LiveStreamException("Error disconnecting from live stream", e);
			} finally {
				stream = null;
			}
		}
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
	private void setupAxes() throws LiveStreamException {
		if (cameraConfig.getCameraCalibration() == null) return;
		logger.debug("Setting up axes");

		// set the scannable for the x-axis and add a position listener to it
		final IScannableDeviceService scannableDeviceService = PlatformUI.getWorkbench().getService(IScannableDeviceService.class);
		final String xAxisScannableName = cameraConfig.getCameraCalibration().getxAxisScannableName();
		try {
			xAxisScannable = scannableDeviceService.getScannable(xAxisScannableName);
			if (xAxisScannable instanceof IPositionListenable) {
				((IPositionListenable) xAxisScannable).addPositionListener(axisScannableMoveListener);
			}
		} catch (ScanningException e) {
			throw new LiveStreamException("Could not get scannable for x-axis of camera: " + xAxisScannableName, e);
		}

		// set the scannable for the y-axis and add a position listener to it
		final String yAxisScannableName = cameraConfig.getCameraCalibration().getyAxisScannableName();
		try {
			yAxisScannable = scannableDeviceService.getScannable(yAxisScannableName);
			if (yAxisScannable instanceof IPositionListenable) {
				((IPositionListenable) yAxisScannable).addPositionListener(axisScannableMoveListener);
			}
		} catch (ScanningException e) {
			throw new LiveStreamException("Could not get scannable for y-axis of camera: " + yAxisScannableName, e);
		}

		updateAxes();
	}

	private void updateAxes() throws LiveStreamException {
		if (cameraConfig.getCameraCalibration() == null || xAxisScannable == null || yAxisScannable == null)
			return;

		final CameraCalibration calibration = cameraConfig.getCameraCalibration();
		final int[] dataShape = stream.getDataset().getShape();
		if (dataShape.length < 2) { // note: this may be the case at the start of the scan, so don't throw an exception
			return;
		}

		xAxisDataset = createAxisDataset(xAxisScannable,
				calibration.getxAxisOffset(), calibration.getxAxisPixelScaling(), dataShape[1]);
		yAxisDataset = createAxisDataset(yAxisScannable,
				calibration.getyAxisOffset(), calibration.getyAxisPixelScaling(), dataShape[0]);
	}

	/**
	 * Creates and returns a dataset for an axis given the parameters below.
	 * @param axisScannable the {@link IScannable} for the axis
	 * @param cameraOffset the offset of the camera position relative to the scannable
	 * @param pixelScaling the size of a camera pixel in the units used by the scannable
	 * @param numPixels the number of pixels in the given axis of the scannable
	 * @return a dataset for the axis
	 * @throws LiveStreamException
	 */
	private IDataset createAxisDataset(IScannable<? extends Number> axisScannable, double cameraOffset, double pixelScaling, int numPixels) throws LiveStreamException {
		// get the current position of the axis scannable
		final double pos;
		try {
			pos = axisScannable.getPosition().doubleValue();
		} catch (Exception e) {
			throw new LiveStreamException("Could not get position for axis: " + axisScannable.getName(), e);
		}

		// calculate the camera position and size
		final double cameraPos = pos + cameraOffset;
		final double imageSize = numPixels * pixelScaling;

		// from those values, we can get the image start and stop values
		final double imageStart = cameraPos - imageSize/2;
		final double imageStop = cameraPos + imageSize/2;

		// create the linear dataset and set the name
		final IDataset axisDataset = DatasetFactory.createLinearSpace(DoubleDataset.class, imageStart, imageStop, numPixels);
		axisDataset.setName(axisScannable.getName());

		return axisDataset;
	}

	public List<IDataset> getAxes() {
		if (xAxisDataset == null || yAxisDataset == null) {
			// returns null rather than empty list to indicate that axes have not been configured
			return null;
		}

		return Arrays.asList(xAxisDataset, yAxisDataset);
	}

	public StreamType getStreamType() {
		return streamType;
	}

}
