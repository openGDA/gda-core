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

import uk.ac.diamond.daq.client.gui.camera.CameraComboItem;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * Represents a stream connection configuration.
 *
 * @author Mauriizio Nagni
 *
 */
public class StreamControlData {

	private final CameraComboItem camera;
	private final StreamType streamType;

	public StreamControlData(CameraComboItem camera, StreamType streamType) {
		this.camera = camera;
		this.streamType = streamType;
	}

	public CameraComboItem getCamera() {
		return camera;
	}

	public StreamType getStreamType() {
		return streamType;
	}

}
