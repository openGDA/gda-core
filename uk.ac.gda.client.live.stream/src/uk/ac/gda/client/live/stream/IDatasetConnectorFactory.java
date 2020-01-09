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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.daq.epics.connector.EpicsV3DynamicDatasetConnector;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * Creates a brand new IDatasetConnector then open the connection.
 * This class is restricted to the package because its use is supposed to be limited to {@link LiveStreamConnection} class
 *
 * @author Maurizio Nagni
 */
class IDatasetConnectorFactory {

	private static final long MJPEG_DEFAULT_SLEEP_TIME = 50; // ms i.e. 20 fps
	private static final int MJPEG_DEFAULT_CACHE_SIZE = 3; // frames



	/**
	 * Hides constructor
	 */
	private IDatasetConnectorFactory() {
		super();
	}

	public static IDatasetConnector getDatasetConnector(final CameraConfiguration cameraConfiguration, final StreamType streamType) throws LiveStreamException {
		IDatasetConnectorGenerator generator = new IDatasetConnectorGenerator(cameraConfiguration, streamType);
		return generator.createDatasetConnector();
	}

	private static class IDatasetConnectorGenerator {
		private final CameraConfiguration cameraConfiguration;
		private final StreamType streamType;

		public IDatasetConnectorGenerator(CameraConfiguration cameraConfiguration, StreamType streamType) throws LiveStreamException {
			super();
			this.cameraConfiguration = cameraConfiguration;
			this.streamType = streamType;
			validate();
		}

		private void validate() throws LiveStreamException {
			if (streamType == StreamType.MJPEG && cameraConfiguration.getUrl() == null) {
				throw new LiveStreamException(
						"MJPEG stream requested but no url defined for " + cameraConfiguration.getName());
			}
			if (streamType == StreamType.EPICS_ARRAY && cameraConfiguration.getArrayPv() == null) {
				throw new LiveStreamException(
						"EPICS stream requested but no array PV defined for " + cameraConfiguration.getName());
			}
		}

		public IDatasetConnector createDatasetConnector() throws LiveStreamException {
			IDatasetConnector newStream;
			switch (streamType) {
			case MJPEG:
				newStream = createMpegStream();
				break;
			case EPICS_ARRAY:
				newStream = createEpicsArrayStream();
				break;
			default:
				throw new LiveStreamException("Stream type '" + streamType + "' not supported");
			}
			connectIDatasetConnector(newStream);
			return newStream;
		}

		private IDatasetConnector createEpicsArrayStream() {
			return new EpicsV3DynamicDatasetConnector(cameraConfiguration.getArrayPv());
		}

		private IDatasetConnector createMpegStream() throws LiveStreamException {
			final URL url;
			try {
				url = new URL(cameraConfiguration.getUrl());
			} catch (MalformedURLException e) {
				throw new LiveStreamException("Malformed URL check camera configuration", e);
			}

			// If sleepTime or cacheSize are set use them, else use the defaults
			final long sleepTime = cameraConfiguration.getSleepTime() != 0 ? cameraConfiguration.getSleepTime()
					: MJPEG_DEFAULT_SLEEP_TIME; // ms
			final int cacheSize = cameraConfiguration.getCacheSize() != 0 ? cameraConfiguration.getCacheSize()
					: MJPEG_DEFAULT_CACHE_SIZE; // frames

			try {
				if (cameraConfiguration.isRgb()) {
					return PlatformUI.getWorkbench().getService(IRemoteDatasetService.class).createMJPGDataset(url,
							sleepTime, cacheSize);
				} else {
					return PlatformUI.getWorkbench().getService(IRemoteDatasetService.class).createGrayScaleMJPGDataset(url,
							sleepTime, cacheSize);
				}
			} catch (Exception e) {
				throw new LiveStreamException("Cannot retrieve IRemoteDatasetService for: " + url, e);
			}
		}

		private void connectIDatasetConnector(IDatasetConnector dataConnector) throws LiveStreamException {
			try {
				dataConnector.connect(5, TimeUnit.SECONDS);
			} catch (DatasetException e) {
				throw new LiveStreamException("Cannot connect to DataConnector", e);
			}
		}
	}
}
