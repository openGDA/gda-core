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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

public class LiveStreamWrapperManager {

	private static final LiveStreamWrapperManager instance = new LiveStreamWrapperManager();

	private final Map<StreamType, Set<LiveStreamWrapper>> streamWrappers;

	public static LiveStreamWrapperManager getInstance() {
		return instance;
	}

	private LiveStreamWrapperManager() {
		final StreamType[] streamTypes = StreamType.values();
		streamWrappers = Collections.synchronizedMap(new HashMap<>(streamTypes.length));
		for (StreamType s : streamTypes) {
			streamWrappers.put(s, Collections.synchronizedSet(new HashSet<>()));
		}
	}

	/**
	 * Find an existing {@link LiveStreamWrapper} that is suitable for the given config/stream type, or create a new
	 * one.
	 * <p>
	 * Note that the calling function must call connect() on the wrapper before use - and of course disconnect() after
	 * use.
	 */
	public LiveStreamWrapper getStream(CameraConfiguration cameraConfig, StreamType streamType) {
		final Optional<LiveStreamWrapper> existingWrapper = streamWrappers.get(streamType).stream()
				.filter(w -> w.isConfigCompatible(cameraConfig, streamType))
				.findFirst();
		if (existingWrapper.isPresent()) {
			return existingWrapper.get();
		}
		final LiveStreamWrapper newWrapper = new LiveStreamWrapper(cameraConfig, streamType);
		streamWrappers.get(streamType).add(newWrapper);
		return newWrapper;
	}

	/** Remove unused connections */
	public void cleanUp() {
		for (Set<LiveStreamWrapper> wrappers : streamWrappers.values()) {
			final Iterator<LiveStreamWrapper> iter = wrappers.iterator();
			while (iter.hasNext()) {
				final LiveStreamWrapper wrapper = iter.next();
				if (wrapper.getConnectionCount() < 1) {
					iter.remove();
				}
			}
		}
	}
}
