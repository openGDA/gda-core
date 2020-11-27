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

import org.eclipse.january.dataset.IDatasetConnector;

import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

public class LiveStreamConnectionBuilder {

	private CameraConfiguration cameraConfig;

	private StreamType streamType = StreamType.MJPEG;

	private IDatasetConnector stream;

	public LiveStreamConnectionBuilder(CameraConfiguration cameraConfig, StreamType streamType) {
		this.cameraConfig = cameraConfig;
		this.streamType = streamType;
	}

	public LiveStreamConnectionBuilder setStream(IDatasetConnector stream) {
		this.stream = stream;
		return this;
	}

	public LiveStreamConnection build() {
		final LiveStreamConnection connection = new LiveStreamConnection(cameraConfig, streamType);
		if (stream == null) {
			connection.setStream(LiveStreamWrapperManager.getInstance().getStream(cameraConfig, streamType));
		} else {
			connection.setStream(stream);
		}
		return connection;
	}

	public LiveStreamConnection buildAndConnect() throws LiveStreamException {
		final LiveStreamConnection connection = build();
		connection.connect();
		return connection;
	}
}
