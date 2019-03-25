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

import java.util.Map;
import java.util.Optional;
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
 * A Service class to to manage shared access to {@link LiveStreamConnection}s in
 * various forms
 */

public class LiveStreamConnectionManager implements ILiveStreamConnectionService {

	private Map<CameraConfiguration, LiveStreamConnection> connections = new ConcurrentHashMap<>();

	/**
	 * Retrieves a {@link LiveStreamConnection} of the specified {@link StreamType} based on the specified
	 * {@link CameraConfiguration}. A fresh one is instantiated if none currently exists for this configuration,
	 * otherwise the existing one is provided.
	 */
	@Override
	public LiveStreamConnection getSharedLiveStreamConnection(
			final CameraConfiguration cameraConfig, final StreamType streamType) {
		return connections.computeIfAbsent(cameraConfig, config -> getFreshLiveStreamConnection(config, streamType));
	}

	/**
	 * Retrieves a {@link LiveStreamConnection} of the specified {@link StreamType} corresponding to the supplied
	 * named camera. A fresh one is instantiated if none currently exists otherwise the existing one is provided.
	 */
	@Override
	public LiveStreamConnection getSharedLiveStreamConnection(final String cameraName, final StreamType streamType) {
		CameraConfiguration configuration = getConfigFromDisplayName(cameraName);
		if (null == configuration) {
			throw new IllegalStateException(String.format("No Camera Configuration matching name: ", cameraName));
		}
		return getSharedLiveStreamConnection(configuration, streamType);
	}

	/**
	 * Instantiates a new {@link LiveStreamConnection} of the specified {@link StreamType} based on the specified
	 * {@link CameraConfiguration}
	 */
	@Override
	public LiveStreamConnection getFreshLiveStreamConnection(
			final CameraConfiguration cameraConfig, final StreamType streamType) {
		return new LiveStreamConnection(cameraConfig, streamType);
	}

	/**
	 * Instantiates a new {@link LiveStreamConnection} of the specified {@link StreamType} corresponding to the supplied
	 * named camera
	 */
	@Override
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
	 * @return	An mappable version of the stream source of the connection
	 * @throws	LiveStreamException If the connection to the source is unsuccessful
	 */
	@Override
	public LiveStreamMapObject getLiveStreamMapObjectUsingConnection(final LiveStreamConnection liveStreamConnection)
			throws LiveStreamException {
		if (!liveStreamConnection.isConnected()) {
			liveStreamConnection.connect();
		}
		return new LiveStreamPlottable(liveStreamConnection);
	}

	/**
	 * Obtains the packaged stream source identified as the default for the beamline
	 *
	 * @return	An {@link Optional} of the mappable version of the default stream source, empty if none has been set.
	 * @throws	LiveStreamException If the connection to the source is unsuccessful
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
				LiveStreamConnection connection = getSharedLiveStreamConnection(config, StreamType.EPICS_ARRAY);
				source = Optional.of(getLiveStreamMapObjectUsingConnection(connection));
			}
		}
		return source;
	}

	/**
	 * Finds the matching {@link CameraConfiguraton} for the specified dame
	 *
	 * @param name	The display name of the Camera to be found
	 * @return		The corresponding {@link CameraConfiguration} or null if no matching one can be found
	 */
	private CameraConfiguration getConfigFromDisplayName(final String name) {
		return Finder.getInstance().find(name);
	}

}
