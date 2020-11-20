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

package uk.ac.diamond.daq.client.gui.camera.liveview.state;

import uk.ac.gda.client.live.stream.LiveStreamException;

/**
 * Defines the states allowed for a stream connection consumer.
 * <ul>
 * <li>
 * 	<i>idle</i> when the consumer is not listening
 * </li>
 * <li>
 * 	<i>listening</i> when the consumer is active
 * </li>
 * <li>
 * 	<i>same</i> when the consumer requires to update but not change the actual state
 * </li>
 * </ul>
 * @author Maurizio Nagni
 *
 */
public interface StreamControlState {

	/**
	 * Requires to stop listening the stream defined by the data controller
	 * @param streamControlData the consumer stream data object
	 * @throws LiveStreamException if error occur
	 */
	void idleState(StreamController streamControlData) throws LiveStreamException;
	/**
	 * Requires to start listening the stream defined by the data controller
	 * @param streamControlData the consumer stream data object
	 * @throws LiveStreamException if error occur
	 */
	void listeningState(StreamController streamControlData) throws LiveStreamException;
	/**
	 * Requires the refresh the state defined by the data controller
	 * @param streamControlData the consumer stream data object
	 * @throws LiveStreamException if error occur
	 */
	void sameState(StreamController streamControlData) throws LiveStreamException;

}
