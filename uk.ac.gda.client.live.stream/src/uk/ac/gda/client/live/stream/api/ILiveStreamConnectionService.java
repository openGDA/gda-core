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

package uk.ac.gda.client.live.stream.api;

import java.util.UUID;

import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * An interface to create or retrieve {@link LiveStreamConnection}. The connections may be created in two flavours:
 * <ul>
 * <li>shared</li>
 * <li>unshared</li>
 * </ul>
 * A <code>shared</code> connection is created to be shared between different component so when any component changes
 * the connection parameters all other components are affected.
 *
 * A <code>unshared</code> connection is created to be managed by a single component which will be the final
 * responsible for it
 *
 * @author Keith Ralph
 * @author Maurizio Nagni
 *
 */
public interface ILiveStreamConnectionService extends IMappableLiveStreamConnectionSource {

	/**
	 * Returns the {@link LiveStreamConnection} associated with this service
	 *
	 * @param uuid
	 *            the required connection id
	 * @return the required connection or <code>null</code> if not found
	 */
	LiveStreamConnection getStreamConnection(UUID uuid);

	/**
	 * Verifies if a {@link LiveStreamConnection} exists and matches the <code>shared</code> type
	 *
	 * @param uuid
	 *            the required connection id
	 * @param shared
	 *            the connection type
	 * @return <code>true</code> if the connection is found, <code>false</code> otherwise
	 */
	boolean isStreamConnectionAvailable(UUID uuid, boolean shared);

	/**
	 * Creates a new connection stream
	 * @param cameraConfig
	 *            the camera configuration use to open the streaming connection
	 * @param streamType
	 *            the connection streaming type
	 * @param shared
	 *            the connection type
	 * @return the new connection
	 */
	LiveStreamConnection createStreamConnection(CameraConfiguration cameraConfig, StreamType streamType,
			boolean shared);

	/**
	 * Retrieve a shared {@link LiveStreamConnection} of the specified {@link StreamType} based on the specified
	 * {@link CameraConfiguration}.
	 *
	 * @deprecated to create a connection use {@link #createStreamConnection(CameraConfiguration, StreamType, boolean)},
	 *             to retrieve a connection use {@link #getStreamConnection(UUID)}
	 */
	@Deprecated
	LiveStreamConnection getSharedLiveStreamConnection(final CameraConfiguration cameraConfig,
			final StreamType streamType);

	/**
	 * Retrieve a shared {@link LiveStreamConnection} of the specified {@link StreamType} corresponding to the supplied
	 * named camera.
	 *
	 * @Deprecated to create a connection use {@link #createStreamConnection(CameraConfiguration, StreamType, boolean)},
	 *             to retrieve a connection use {@link #getStreamConnection(UUID)}
	 */
	@Deprecated
	LiveStreamConnection getSharedLiveStreamConnection(final String cameraName, final StreamType streamType);

	/**
	 * Instantiate a new {@link LiveStreamConnection} of the specified {@link StreamType} based on the specified
	 * {@link CameraConfiguration}
	 *
	 * @deprecated to create a connection use {@link #createStreamConnection(CameraConfiguration, StreamType, boolean)},
	 *             to retrieve a connection use {@link #getStreamConnection(UUID)}
	 */
	@Deprecated
	LiveStreamConnection getFreshLiveStreamConnection(final CameraConfiguration cameraConfig,
			final StreamType streamType);

	/**
	 * Instantiate a new {@link LiveStreamConnection} of the specified {@link StreamType} corresponding to the supplied
	 * named camera
	 *
	 * @deprecated to create a connection use {@link #createStreamConnection(CameraConfiguration, StreamType, boolean)},
	 *             to retrieve a connection use {@link #getStreamConnection(UUID)}
	 */
	@Deprecated
	LiveStreamConnection getFreshLiveStreamConnection(final String cameraName, final StreamType streamType);
}
