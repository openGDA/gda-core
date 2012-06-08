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
 * All GPIB devices must implement this interface.
 */
public interface Gpib extends Device {
	/**
	 * Finds GPIB board/device.
	 * 
	 * @param deviceName
	 *            name of the device/board to be found
	 * @return The handle to the device/board
	 * @throws DeviceException
	 */
	public int findDevice(String deviceName) throws DeviceException;

	/**
	 * Conducts a serial poll, which fetches a single byte from the GPIB device.
	 * 
	 * @param deviceName
	 *            name of the device to be polled
	 * @return The poll byte
	 * @throws DeviceException
	 */
	public int getSerialPollByte(String deviceName) throws DeviceException;

	/**
	 * Clears a specific device.
	 * 
	 * @param deviceName
	 *            name of the device to be cleared
	 * @throws DeviceException
	 */
	public void sendDeviceClear(String deviceName) throws DeviceException;

	/**
	 * Assert interface clear.
	 * 
	 * @param interFaceName
	 * @throws DeviceException
	 */
	public void sendInterfaceClear(String interFaceName) throws DeviceException;

	/**
	 * Change the I/O timeout period.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @param timeout
	 *            allowed for reads/writes in milliseconds
	 * @throws DeviceException
	 */
	public void setTimeOut(String deviceName, int timeout) throws DeviceException;

	/**
	 * Gets the time allowed for reads/writes in milliseconds.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @return timeout the allowed for reads/writes in milliseconds
	 * @throws DeviceException
	 */
	public int getTimeOut(String deviceName) throws DeviceException;

	/**
	 * Sets read/write termination character.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @param term
	 *            the termination character
	 * @throws DeviceException
	 */
	public void setTerminator(String deviceName, char term) throws DeviceException;

	/**
	 * Gets read/write termination character.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @return the termination character
	 * @throws DeviceException
	 */
	public char getTerminator(String deviceName) throws DeviceException;

	/**
	 * Sets read termination for talkers.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @param terminate
	 *            if true terminator character is used to terminate reads, if false no read termination is performed.
	 * @throws DeviceException
	 */
	public void setReadTermination(String deviceName, boolean terminate) throws DeviceException;

	/**
	 * Sets write termination for listeners.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @param terminate
	 *            if true terminator character is used to terminate writes, if false no write termination is performed
	 * @throws DeviceException
	 */
	public void setWriteTermination(String deviceName, boolean terminate) throws DeviceException;

	/**
	 * Gets read termination for talkers.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @return if true termination chracter is used to terminate reads, if false no read termination is performed
	 * @throws DeviceException
	 */
	public boolean getReadTermination(String deviceName) throws DeviceException;

	/**
	 * Gets write termination for listeners.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @return if true termination chracter is used to terminate write, if false no write termination is performed
	 * @throws DeviceException
	 */
	public boolean getWriteTermination(String deviceName) throws DeviceException;

	/**
	 * Reads the reply string from GPIB.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @return the reply
	 * @throws DeviceException
	 */
	public String read(String deviceName) throws DeviceException;

	/**
	 * Reads a string of specified length.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @param strLength
	 *            length of the string to be read
	 * @return the reply
	 * @throws DeviceException
	 */
	public String read(String deviceName, int strLength) throws DeviceException;

	/**
	 * Writes a String to the GPIB device.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @param buffer
	 *            the string to be written
	 * @throws DeviceException
	 */
	public void write(String deviceName, String buffer) throws DeviceException;

}
