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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.ui.PlatformUI;

import gda.factory.Finder;
import uk.ac.gda.client.live.stream.api.ILiveStreamConnectionService;
import uk.ac.gda.client.live.stream.handlers.LiveStreamPlottable;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * Main implementation of the {@link ILiveStreamConnectionService} interface to manage shared and unshared stream
 * connections.
 *
 * @author Keith Ralph
 * @author Maurizio Nagni
 */
public class LiveStreamConnectionManager implements ILiveStreamConnectionService {

	private final Set<LiveStreamConnection> sharedConnections;
	private final Set<LiveStreamConnection> unsharedConnections;
	private final Map<CameraConfiguration, LiveStreamConnection> connections;

	public LiveStreamConnectionManager() {
		this.connections = new ConcurrentHashMap<>();
		this.sharedConnections = Collections.synchronizedSet(new HashSet<>());
		this.unsharedConnections = Collections.synchronizedSet(new HashSet<>());
	}

	@Override
	public LiveStreamConnection getStreamConnection(UUID uuid) {
		return findStreamConnection(uuid);
	}

	@Override
	public boolean isStreamConnectionAvailable(UUID uuid, boolean shared) {
		return findStreamConnection(shared, uuid).isPresent();
	}

	@Override
	public LiveStreamConnection createStreamConnection(CameraConfiguration cameraConfig, StreamType streamType,
			boolean shared) {
		LiveStreamConnection instance = new LiveStreamConnection(cameraConfig, streamType);
		if (shared) {
			sharedConnections.add(instance);
			connections.putIfAbsent(cameraConfig, instance);
		} else {
			unsharedConnections.add(instance);
		}
		return instance;
	}

	/**
	 * Retrieves a {@link LiveStreamConnection} of the specified {@link StreamType} based on the specified
	 * {@link CameraConfiguration}. A fresh one is instantiated if none currently exists for this configuration,
	 * otherwise the existing one is provided.
	 */
	@Override
	@Deprecated
	public LiveStreamConnection getSharedLiveStreamConnection(final CameraConfiguration cameraConfig,
			final StreamType streamType) {
		return sharedConnections.stream().filter(s -> s.sameConfiguration(cameraConfig, streamType))
				.findFirst().orElse(createStreamConnection(cameraConfig, streamType, true));
	}

	/**
	 * Retrieves a {@link LiveStreamConnection} of the specified {@link StreamType} corresponding to the supplied named
	 * camera. A fresh one is instantiated if none currently exists otherwise the existing one is provided.
	 */
	@Override
	@Deprecated
	public LiveStreamConnection getSharedLiveStreamConnection(final String cameraName, final StreamType streamType) {
		CameraConfiguration cameraConfig = getConfigFromDisplayName(cameraName);
		if (null == cameraConfig) {
			throw new IllegalStateException(String.format("No Camera Configuration matching name: ", cameraName));
		}
		return sharedConnections.stream().filter(s -> s.sameConfiguration(cameraConfig, streamType))
				.findFirst().orElse(createStreamConnection(cameraConfig, streamType, true));
	}

	/**
	 * Instantiates a new {@link LiveStreamConnection} of the specified {@link StreamType} based on the specified
	 * {@link CameraConfiguration}
	 *
	 * @deprecated This method will removed in a future release as creates instances which are no more managed by this
	 *             service
	 */
	@Override
	@Deprecated
	public LiveStreamConnection getFreshLiveStreamConnection(final CameraConfiguration cameraConfig,
			final StreamType streamType) {
		return new LiveStreamConnection(cameraConfig, streamType);
	}

	/**
	 * Instantiates a new {@link LiveStreamConnection} of the specified {@link StreamType} corresponding to the supplied
	 * named camera
	 *
	 * @deprecated This method will be removed in a future release as creates instances which are no more managed by this
	 *             service
	 */
	@Override
	@Deprecated
	public LiveStreamConnection getFreshLiveStreamConnection(final String cameraName, final StreamType streamType) {
		CameraConfiguration configuration = getConfigFromDisplayName(cameraName);
		if (null == configuration) {
			throw new IllegalStateException(String.format("No Camera Configuration matching name: ", cameraName));
		}
		return getFreshLiveStreamConnection(configuration, streamType);
	}

	/**
	 * Obtains the packaged stream source using the identified connection
	 *
	 * @return An mappable version of the stream source of the connection
	 * @throws LiveStreamException
	 *             If the connection to the source is unsuccessful
	 */
	@Override
	public LiveStreamMapObject getLiveStreamMapObjectUsingConnection(final LiveStreamConnection liveStreamConnection)
			throws LiveStreamException {

		return new LiveStreamPlottable(liveStreamConnection);
	}

	/**
	 * Obtains the packaged stream source identified as the default for the beamline
	 *
	 * @return An {@link Optional} of the mappable version of the default stream source, empty if none has been set.
	 * @throws LiveStreamException
	 *             If the connection to the source is unsuccessful
	 */
	@Override
	public Optional<LiveStreamMapObject> getDefaultStreamSource() throws LiveStreamException {
		IStageScanConfiguration stageConfig = PlatformUI.getWorkbench().getService(IStageScanConfiguration.class);
		if (null == stageConfig) {
			throw new IllegalStateException("Could not retrieve the IStageScanConfiguration Service");
		}

		Optional<LiveStreamMapObject> source = Optional.empty();
		String defaultConfigName = stageConfig.getDefaultStreamSourceConfig();
		if (!defaultConfigName.isEmpty()) {
			CameraConfiguration config = Finder.getInstance().find(defaultConfigName);
			if (null != config) {
				StreamType streamType = config.getArrayPv() != null ? StreamType.EPICS_ARRAY : StreamType.MJPEG;
				LiveStreamConnection connection = getSharedLiveStreamConnection(config, streamType);
				source = Optional.of(getLiveStreamMapObjectUsingConnection(connection));
			}
		}
		return source;
	}

	/**
	 * Finds the matching {@link CameraConfiguraton} for the specified dame
	 *
	 * @param name
	 *            The display name of the Camera to be found
	 * @return The corresponding {@link CameraConfiguration} or null if no matching one can be found
	 */
	private CameraConfiguration getConfigFromDisplayName(final String name) {
		return Finder.getInstance().find(name);
	}

	private Optional<LiveStreamConnection> findStreamConnection(boolean shared, UUID uuid) {
		if (uuid == null) {
			return Optional.empty();
		}
		if (shared) {
			return sharedConnections.stream().filter(s -> uuid.equals(s.getId())).findFirst();
		}
		return unsharedConnections.stream().filter(s -> uuid.equals(s.getId())).findFirst();
	}

	private LiveStreamConnection findStreamConnection(UUID uuid) {
		if (uuid == null) {
			return null;
		}
		Optional<LiveStreamConnection> connection = sharedConnections.stream().filter(s -> uuid.equals(s.getId()))
				.findFirst();
		if (connection.isPresent()) {
			return connection.get();
		}
		connection = unsharedConnections.stream().filter(s -> uuid.equals(s.getId())).findFirst();
		if (connection.isPresent()) {
			return connection.get();
		}
		return null;
	}

}
