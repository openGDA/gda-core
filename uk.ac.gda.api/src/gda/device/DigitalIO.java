/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device;

/**
 * An interface for a distributed Digital I/O class
 */
public interface DigitalIO extends Device {
	/**
	 * high state is assumed to be TTL high of >= 2.2V
	 */
	public static final int HIGH_STATE = 1;

	/**
	 * Low state is assumed to be TTL of 0-0.8V.
	 */
	public static final int LOW_STATE = 0;

	/**
	 * Get the current logic state for the specified channel
	 * 
	 * @param channelName
	 *            i/o channel name to read
	 * @return current TTL line state
	 * @throws DeviceException
	 */
	public int getState(String channelName) throws DeviceException;

	/**
	 * Set the current logic state for the specified channel
	 * 
	 * @param channelName
	 *            i/o channel name to write to
	 * @param state
	 *            TTL line state : 0=low and any other value=high
	 * @throws DeviceException
	 */
	public void setState(String channelName, int state) throws DeviceException;

	/**
	 * Carries out a negative edge sync operation by intialising the state to high (just in case it's low from a
	 * previous operation), then setting a low state. Finally a return is made to the resting high state after a delay
	 * period accessed by the methods getEdgeSyncDelayTime() and setEdgeSyncDelayTime() in msecs. Thus it's possible to
	 * use this also as a negative level sync.
	 * 
	 * @param channelName
	 *            i/o channel name to write to
	 * @throws DeviceException
	 */
	public void setNegativeEdgeSync(String channelName) throws DeviceException;

	/**
	 * Carries out a positive edge sync operation by intialising the state to low (just in case it's high from a
	 * previous operation), then setting a high state. Finally a return is made to the resting low state after a delay
	 * period accessed by the methods getEdgeSyncDelayTime() and setEdgeSyncDelayTime() in msecs. Thus it's possible to
	 * use this also as a positive level sync.
	 * 
	 * @param channelName
	 *            i/o channel name to write to
	 * @throws DeviceException
	 */
	public void setPositiveEdgeSync(String channelName) throws DeviceException;

	/**
	 * Carries out a 2 line handshake with an external device which obeys the protocol rules. The handshake is safe as
	 * both devices wait for each other using a read input line and set a state on a write output line. Very useful for
	 * integrating decoupled subsystems. This device waits for the input line to go high indicating the external device
	 * is idle and can be triggered. Then the external device is triggered with a low output state. The input line then
	 * waits for a low state to show the external device has received the trigger and is busy. The input line then waits
	 * for a high state to show the external device has finished its actions and is idle again (i.e. back to the
	 * pre-trigger state again). A timeout can be specified by the methods get2LineSyncTimeout() and
	 * set2LineSyncTimeout() in msecs.
	 * 
	 * @param inputChannelName
	 *            i/o channel name to read input line state
	 * @param outputChannelName
	 *            i/o channel name to write output line state
	 * @throws DeviceException
	 */
	public void setNegative2LineSync(String inputChannelName, String outputChannelName) throws DeviceException;

	/**
	 * Carries out a 2 line handshake with an external device which obeys the protocol rules. The handshake is safe as
	 * both devices wait for each other using a read input line and set a state on a write output line. Very useful for
	 * integrating decoupled subsystems. This device waits for the input line to go low indicating the external device
	 * is idle and can be triggered. Then the external device is triggered with a high output state. The input line then
	 * waits for a high state to show the external device has received the trigger and is busy. The input line then
	 * waits for a low state to show the external device has finished its actions and is idle again (i.e. back to the
	 * pre-trigger state again). A timeout can be specified by the methods get2LineSyncTimeout() and
	 * set2LineSyncTimeout() in msecs.
	 * 
	 * @param inputChannelName
	 *            i/o channel name to read input line state
	 * @param outputChannelName
	 *            i/o channel name to write output line state
	 * @throws DeviceException
	 */
	public void setPositive2LineSync(String inputChannelName, String outputChannelName) throws DeviceException;

	/**
	 * Get the current timeout for 2 line sync operations
	 * 
	 * @return current 2 line sync timeout in msecs
	 * @throws DeviceException
	 */
	public int getTwoLineSyncTimeout() throws DeviceException;

	/**
	 * Set the required timeout for 2 line sync operations
	 * 
	 * @param msecs
	 *            2 line sync timeout in msecs
	 * @throws DeviceException
	 */
	public void setTwoLineSyncTimeout(int msecs) throws DeviceException;

	/**
	 * Get the current delay time after edge sync operations
	 * 
	 * @return the current edge sync delay time in msecs
	 * @throws DeviceException
	 */
	public int getEdgeSyncDelayTime() throws DeviceException;

	/**
	 * Set the required delay after edge sync operations
	 * 
	 * @param msecs
	 *            2 line sync timeout in msecs
	 * @throws DeviceException
	 */
	public void setEdgeSyncDelayTime(int msecs) throws DeviceException;

}
