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

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDatasetConnector;

import uk.ac.diamond.daq.epics.connector.EpicsV3DynamicDatasetConnector;
import uk.ac.diamond.daq.epics.connector.EpicsV4DynamicDatasetConnector;
import uk.ac.gda.client.live.stream.connector.MjpegDynamicDatasetConnector;
import uk.ac.gda.client.live.stream.simulator.connector.BeamSimulationCamera;
import uk.ac.gda.client.live.stream.simulator.connector.BeamSimulationCameraConnector;
import uk.ac.gda.client.live.stream.simulator.connector.ImageDatasetConnector;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * Creates a brand new IDatasetConnector then open the connection. This class is restricted to the package because its
 * use is supposed to be limited to {@link LiveStreamConnection} class
 *
 * @author Maurizio Nagni
 */
class IDatasetConnectorFactory {

	private static final String GDA_DATASET_SIMULATOR = "gdaSimulator";

	/**
	 * Hides constructor
	 */
	private IDatasetConnectorFactory() {
		super();
	}

	public static IDatasetConnector getDatasetConnector(final CameraConfiguration cameraConfiguration,
			final StreamType streamType) throws LiveStreamException {
		IDatasetConnectorGenerator generator = new IDatasetConnectorGenerator(cameraConfiguration, streamType);
		return generator.createDatasetConnector();
	}

	private static class IDatasetConnectorGenerator {
		private final CameraConfiguration cameraConfiguration;
		private final StreamType streamType;

		public IDatasetConnectorGenerator(CameraConfiguration cameraConfiguration, StreamType streamType)
				throws LiveStreamException {
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
			if (streamType == StreamType.EPICS_PVA && cameraConfiguration.getPvAccessPv() == null) {
				throw new LiveStreamException(
						"EPICS PVA stream requested but no PVA PV defined for " + cameraConfiguration.getName());
			}
		}

		public IDatasetConnector createDatasetConnector() throws LiveStreamException {
			IDatasetConnector newStream;
			switch (streamType) {
			case MJPEG:
				newStream = createMjpegStream();
				break;
			case EPICS_ARRAY:
				if (cameraConfiguration.getArrayPv().startsWith(GDA_DATASET_SIMULATOR)) {
					newStream = createSimulatorDatasetConnector();
				} else {
					newStream = createEpicsArrayStream();
				}
				break;
			case EPICS_PVA:
				newStream = createEpicsPvaStream();
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

		private IDatasetConnector createEpicsPvaStream() {
			return new EpicsV4DynamicDatasetConnector(cameraConfiguration.getPvAccessPv());
		}

		private IDatasetConnector createMjpegStream()  {
			return new MjpegDynamicDatasetConnector(cameraConfiguration.getUrl());

		}

		private void connectIDatasetConnector(IDatasetConnector dataConnector) throws LiveStreamException {
			try {
				dataConnector.connect(5, TimeUnit.SECONDS);
			} catch (DatasetException e) {
				throw new LiveStreamException("Cannot connect to DataConnector", e);
			}
		}

		/**
		 * Returns a {@link IDatasetConnector} generated internally by GDA for simulation purposes.
		 * The stream depends on the specific implementation
		 * @see {@link BeamSimulationCameraConnector}, {@link ImageDatasetConnector}
		 * @return
		 */
		private IDatasetConnector createSimulatorDatasetConnector() {
			String cameraName = cameraConfiguration.getArrayPv().split(":")[1];
			if (BeamSimulationCamera.class.isInstance(cameraConfiguration)) {
				return new BeamSimulationCameraConnector(cameraName, BeamSimulationCamera.class.cast(cameraConfiguration));
			} else {
				return new ImageDatasetConnector(cameraName);
			}
		}
	}
}
