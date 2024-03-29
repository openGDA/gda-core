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

package uk.ac.diamond.daq.client.gui.camera.liveview;

import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;

/**
 * Represents a stream connection configuration.
 *
 * @author Mauriizio Nagni
 *
 */
public class StreamControlData {

	private final CameraConfigurationProperties cameraConfigurationProperties;
	private final StreamType streamType;

	public StreamControlData(CameraConfigurationProperties cameraConfigurationProperties, StreamType streamType) {
		this.cameraConfigurationProperties = cameraConfigurationProperties;
		this.streamType = streamType;
	}

	public CameraConfigurationProperties getCameraConfigurationProperties() {
		return cameraConfigurationProperties;
	}

	public StreamType getStreamType() {
		return streamType;
	}

}
