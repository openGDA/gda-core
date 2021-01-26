/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDynamicShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnectionBuilder;
import uk.ac.gda.client.live.stream.handlers.LiveStreamPlottable;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 *
 * @author Maurizio Nagni
 */
@Component
public class CameraStreamsManager {
	private final Set<LiveStreamConnection> liveStreamConnections;

	private static final Logger logger = LoggerFactory.getLogger(CameraStreamsManager.class);

	private CameraStreamsManager() {
		this.liveStreamConnections = Collections.synchronizedSet(new HashSet<>());
	}

	public boolean isILiveStreamConnectionAvailable(CameraConfiguration cameraConfig, StreamType streamType) {
		return findStreamConnection(cameraConfig, streamType) != null;
	}

	public LiveStreamConnection getStreamConnection(final CameraConfiguration cameraConfig,
			final StreamType streamType) {
		Optional<LiveStreamConnection> connection = liveStreamConnections.stream()
				.filter(s -> sameConfiguration(s, cameraConfig, streamType)).findFirst();

		// Same Configuration and StreamType
		if (connection.isPresent()) {
			return connection.get();
		}

		// Create new connection
		return createStreamConnection(cameraConfig, streamType);
	}

	public IDynamicShape getDynamicShape(final CameraConfiguration cameraConfig,
			final StreamType streamType) {
		return getStreamConnection(cameraConfig, streamType).getStream();
	}

	public LiveStreamPlottable getLiveStreamPlottable(final CameraConfiguration cameraConfig,
			final StreamType streamType) {
		return Optional.ofNullable(getStreamConnection(cameraConfig, streamType))
			.map(LiveStreamPlottable::new)
			.orElseGet(() -> null);
	}

	/**
	 * Creates a new StreamConnection
	 *
	 * @param cameraConfig
	 * @param liveStream
	 * @return
	 */
	private LiveStreamConnection createStreamConnection(CameraConfiguration cameraConfig, StreamType streamType)
			{
		final LiveStreamConnection liveStream = new LiveStreamConnectionBuilder(cameraConfig, streamType).build();
		try {
			liveStream.getStream().connect();
		} catch (DatasetException e) {
			logger.error("Cannot connect to stream", e);
		}
		liveStreamConnections.add(liveStream);
		return liveStream;
	}

	private LiveStreamConnection findStreamConnection(CameraConfiguration cameraConfig, StreamType streamType) {
		return liveStreamConnections.stream()
				.filter(s -> s.getCameraConfig().equals(cameraConfig))
				.filter(s -> s.getStreamType().equals(streamType))
				.findFirst()
				.orElseGet(() -> getStreamConnection(cameraConfig, streamType));
	}



	/**
	 * Compares the identity of this instance
	 * <ol>
	 * <li>{@link LiveStreamConnection#getCameraConfig()} against instance</li>
	 * <li>{@link LiveStreamConnection#getStreamType()} against instance</li>
	 * </ol>
	 *
	 * @param cameraConfig
	 * @param streamType
	 * @return <code>true</code> if the configuration is the same, <code>false</code> in any other case
	 */
	private boolean sameConfiguration(LiveStreamConnection liveStream, final CameraConfiguration cameraConfig, final StreamType streamType) {
		return cameraConfig.equals(liveStream.getCameraConfig()) && streamType.equals(liveStream.getStreamType());
	}

}
