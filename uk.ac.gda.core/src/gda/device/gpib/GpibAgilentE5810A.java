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

package gda.device.gpib;

import gda.device.DeviceException;

/**
 * <p>
 * <b>Title: </b>Class to allow to control GPIB devices through a VXI-11 complient device.</b>
 * </p>
 * <p>
 * <b>Description: </b>This class uses the AgilentE5810A dll, via JNI, which is currently acting as a wrapper for a
 * windows implementaion of a VISA library (Virtual Instrument Software Architecture), visa32.dll. Both these libraries
 * should be in the search PATH (ie. in $GDA_ROOT$/lib/). Note: this is currently a partial implementation, and is
 * untested with real hardware at the Java level (the native library is tested).
 * </p>
 */

public class GpibAgilentE5810A extends GpibBase {

	private long mSession = 0;

	private long mDmm = 0;

	private long mNull = 0;

	/**
	 * Wrapper for native C VISA method. Open a default resource manager.
	 * 
	 * @param session
	 * @return error code
	 */
	private native long visaOpenDefaultRM(long session);

	/**
	 * Wrapper for native C method.
	 * 
	 * @param session
	 * @param name
	 * @param mode
	 * @param timeout
	 * @param vi
	 * @return error code
	 */
	private native long visaOpen(long session, String name, long mode, long timeout, long vi);

	/**
	 * Wrapper for native C method.
	 * 
	 * @param session
	 * @return error code
	 */
	private native long visaClose(long session);

	/**
	 * Wrapper for native C method.
	 * 
	 * @param session
	 * @param mesg
	 * @return error code
	 */
	private native long visaPrintf(long session, String mesg);

	/**
	 * Wrapper for native C method.
	 * 
	 * @param session
	 * @return error code
	 */
	private native String visaScanf(long session);

	/**
	 * Constructor.
	 */
	public GpibAgilentE5810A() {
		// Load in VISA wrapper library.
		System.loadLibrary("AgilentE5810A");
	}
	
	@Override
	public void configure(){
		// no configuration required
	}

	/**
	 * Open the session to a device.
	 */
	public void openSession() {
		/* Open session to VXI-11 device at address 2 */
		visaOpenDefaultRM(mSession);
		visaOpen(mSession, "TCPIP0::gpib001.sc.diamond.ac.uk::gpib0,2::INSTR", mNull, mNull, mDmm);
	}

	/**
	 * Close the session.
	 */
	public void closeSession() {
		visaClose(mSession);
	}

	/**
	 * Method which requests the ID string of the GPIB device.
	 * 
	 * @return ID string
	 */
	public String getID() {
		visaPrintf(mSession, "*IDN?\n");
		return visaScanf(mSession);
	}

	/**
	 * Finds GPIB board/device.
	 * 
	 * @param deviceName
	 *            name of the device/board to be found
	 * @return The handle to the device/board
	 * @throws DeviceException
	 */
	@Override
	public int findDevice(String deviceName) throws DeviceException {
		return 0;
	}

	/**
	 * Conducts a serial poll, which fetches a single byte from the GPIB device.
	 * 
	 * @param deviceName
	 *            name of the device to be polled
	 * @return The poll byte
	 * @throws DeviceException
	 */
	@Override
	public int getSerialPollByte(String deviceName) throws DeviceException {
		return 0;
	}

	/**
	 * Clears a specific device.
	 * 
	 * @param deviceName
	 *            name of the device to be cleared
	 * @throws DeviceException
	 */
	@Override
	public void sendDeviceClear(String deviceName) throws DeviceException {

	}

	/**
	 * Assert interface clear.
	 * 
	 * @param interFaceName
	 * @throws DeviceException
	 */
	@Override
	public void sendInterfaceClear(String interFaceName) throws DeviceException {

	}

	/**
	 * Change the I/O timeout period.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @param timeout
	 *            allowed for reads/writes in milliseconds
	 * @throws DeviceException
	 */
	@Override
	public void setTimeOut(String deviceName, int timeout) throws DeviceException {

	}

	/**
	 * Gets the time allowed for reads/writes in milliseconds.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @return timeout the allowed for reads/writes in milliseconds
	 * @throws DeviceException
	 */
	@Override
	public int getTimeOut(String deviceName) throws DeviceException {
		return 0;
	}

	/**
	 * Sets read/write termination character.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @param term
	 *            the termination character
	 * @throws DeviceException
	 */
	@Override
	public void setTerminator(String deviceName, char term) throws DeviceException {

	}

	/**
	 * Gets read/write termination character.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @return the termination character
	 * @throws DeviceException
	 */
	@Override
	public char getTerminator(String deviceName) throws DeviceException {
		return '0';
	}

	/**
	 * Sets read termination for talkers.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @param terminate
	 *            if true terminator character is used to terminate reads, if false no read termination is performed.
	 * @throws DeviceException
	 */
	@Override
	public void setReadTermination(String deviceName, boolean terminate) throws DeviceException {

	}

	/**
	 * Sets write termination for listeners.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @param terminate
	 *            if true terminator character is used to terminate writes, if false no write termination is performed
	 * @throws DeviceException
	 */
	@Override
	public void setWriteTermination(String deviceName, boolean terminate) throws DeviceException {

	}

	/**
	 * Gets read termination for talkers.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @return if true termination chracter is used to terminate reads, if false no read termination is performed
	 * @throws DeviceException
	 */
	@Override
	public boolean getReadTermination(String deviceName) throws DeviceException {
		return false;
	}

	/**
	 * Gets write termination for listeners.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @return if true termination chracter is used to terminate write, if false no write termination is performed
	 * @throws DeviceException
	 */
	@Override
	public boolean getWriteTermination(String deviceName) throws DeviceException {
		return false;
	}

	/**
	 * Reads the reply string from GPIB.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @return the reply
	 * @throws DeviceException
	 */
	@Override
	public String read(String deviceName) throws DeviceException {
		return "0xCAFEBABE";
	}

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
	@Override
	public String read(String deviceName, int strLength) throws DeviceException {
		return "0xCAFEBABE";
	}

	/**
	 * Writes a String to the GPIB device.
	 * 
	 * @param deviceName
	 *            name of the GPIB device
	 * @param buffer
	 *            the string to be written
	 * @throws DeviceException
	 */
	@Override
	public void write(String deviceName, String buffer) throws DeviceException {

	}

}
