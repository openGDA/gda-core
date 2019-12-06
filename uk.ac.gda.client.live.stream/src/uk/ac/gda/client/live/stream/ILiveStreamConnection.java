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

import java.util.UUID;

import org.eclipse.january.dataset.IDataListener;

import uk.ac.gda.client.live.stream.LiveStreamConnection.IAxisChangeListener;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * An attempt to define at higher level what a live stream should be.
 *
 * @author Maurizio Nagni
 */
public interface ILiveStreamConnection {


	/**
	 * Returns this connection unique id
	 *
	 * @return the UUID associated with this connection
	 */
	UUID getId();

	StreamType getStreamType();

	CameraConfiguration getCameraConfig();

	void addAxisMoveListener(IAxisChangeListener axisMoveListener);

	void removeAxisMoveListener(IAxisChangeListener axisMoveListener);

	void addDataListenerToStream(IDataListener listener) throws LiveStreamException;

	void removeDataListenerFromStream(IDataListener listener);
}
