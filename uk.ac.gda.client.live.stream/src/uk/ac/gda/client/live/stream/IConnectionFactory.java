/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * Wraps the {@link LiveStreamConnectionManager} to offer a Flyweight pattern for the existing liveStreams.
 *
 * @author Maurizio Nagni
 */
public class IConnectionFactory {

	private static final LiveStreamConnectionManager liveStreamConnectionManager = new LiveStreamConnectionManager();

	private IConnectionFactory() {}

	public static LiveStreamConnection getLiveStremConnection(final CameraConfiguration cameraConfig, final StreamType streamType) {
		return liveStreamConnectionManager.getSharedLiveStreamConnection(cameraConfig, streamType);
	}

}
