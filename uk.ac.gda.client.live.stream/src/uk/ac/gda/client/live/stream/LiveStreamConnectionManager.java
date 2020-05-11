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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import uk.ac.gda.client.live.stream.api.ILiveStreamConnectionManager;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * Main implementation of the {@link ILiveStreamConnectionManager} interface to manage stream connections.
 *
 * @author Keith Ralph
 * @author Maurizio Nagni
 */
public class LiveStreamConnectionManager implements ILiveStreamConnectionManager {

	private static final LiveStreamConnectionManager instance = new LiveStreamConnectionManager();

	private final Set<LiveStreamConnection> liveStreamConnections;

	public static ILiveStreamConnectionManager getInstance() {
		return instance;
	}

	private LiveStreamConnectionManager() {
		this.liveStreamConnections = Collections.synchronizedSet(new HashSet<>());
	}

	@Override
	public ILiveStreamConnection getIStreamConnection(UUID uuid) {
		return findStreamConnection(uuid);
	}

	@Override
	public boolean isILiveStreamConnectionAvailable(UUID uuid) {
		return findStreamConnection(uuid) != null;
	}

	@Override
	public UUID getIStreamConnection(CameraConfiguration cameraConfig, StreamType streamType)
			throws LiveStreamException {
		return getStreamConnection(cameraConfig, streamType).getId();
	}

	private LiveStreamConnection getStreamConnection(final CameraConfiguration cameraConfig, final StreamType streamType)
			throws LiveStreamException {
		Optional<LiveStreamConnection> optional = liveStreamConnections.stream()
				.filter(s -> s.sameConfiguration(cameraConfig, streamType)).findFirst();
		// Same Configuration and StreamType
		if (optional.isPresent()) {
			return optional.get();
		}
		optional = liveStreamConnections.stream().filter(s -> s.similarConfiguration(cameraConfig, streamType))
				.findFirst();
		// Same StreamType, different Configuration
		if (optional.isPresent()) {
			return doIStreamConnection(cameraConfig, optional.get());
		}
		// Not existing Configuration and StreamType
		return doIStreamConnection(cameraConfig, streamType);
	}

	/**
	 * Creates a new StreamConnection
	 *
	 * @param cameraConfig
	 * @param liveStream
	 * @return
	 */
	private LiveStreamConnection doIStreamConnection(CameraConfiguration cameraConfig, StreamType streamType)
			throws LiveStreamException {
		LiveStreamConnection liveStream = new LiveStreamConnection(cameraConfig, streamType);
		liveStream.connect();
		liveStreamConnections.add(liveStream);
		return liveStream;
	}

	/**
	 * Clones an existing connection using a different CameraConfiguration but same {@link StreamType}
	 *
	 * @param cameraConfig
	 * @param liveStream
	 * @return
	 */
	private LiveStreamConnection doIStreamConnection(CameraConfiguration cameraConfig,
			LiveStreamConnection liveStream) {
		LiveStreamConnection newLiveStream = new LiveStreamConnection(cameraConfig, liveStream);
		liveStreamConnections.add(newLiveStream);
		return liveStream;
	}

	private LiveStreamConnection findStreamConnection(UUID uuid) {
		if (uuid == null) {
			return null;
		}
		return liveStreamConnections.stream().filter(s -> uuid.equals(s.getId())).findFirst().orElse(null);
	}
}
