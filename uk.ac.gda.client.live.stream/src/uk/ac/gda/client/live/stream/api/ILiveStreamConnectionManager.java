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

import uk.ac.gda.client.live.stream.ILiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * An interface to create or retrieve {@link LiveStreamConnection}.
 *
 * @author Keith Ralph
 * @author Maurizio Nagni
 *
 */
public interface ILiveStreamConnectionManager extends IMappableLiveStreamConnectionSource {

	/**
	 * Returns the {@link ILiveStreamConnection} associated with this service
	 *
	 * @param uuid
	 *            the required connection id
	 * @return the required connection or <code>null</code> if not found
	 */
	ILiveStreamConnection getIStreamConnection(UUID uuid);

	/**
	 * Verifies if a {@link LiveStreamConnection} exists and matches the <code>shared</code> type
	 *
	 * @param uuid
	 *            the required connection id
	 * @return <code>true</code> if the connection is found, <code>false</code> otherwise
	 */
	boolean isILiveStreamConnectionAvailable(UUID uuid);


	/**
	 * Returns the connection associated with the (config, type) pair configuration, otherwise creates a new one.
	 * @param cameraConfig
	 * @param streamType
	 * @return a stream connection
	 * @throws LiveStreamException if error occurs during the connection creation
	 */
	UUID getIStreamConnection(CameraConfiguration cameraConfig, StreamType streamType) throws LiveStreamException;

}
