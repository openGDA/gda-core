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

import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation for National Instruments GPIB interface
 */

public class GpibNiWin extends GpibBase {
	
	private static final Logger logger = LoggerFactory.getLogger(GpibNiWin.class);
	
	private final int READ = 0x1400;

	private final int WRITE = 0x1800;

	private final int BUFFERSIZE = 512;

	private final int DUMMYINT = 0;

	// private final String DUMMYSTRING = "";
	private JniGpib jniGpib;

	private HashMap<String, Integer> deviceIndex;

	// private String reply = "";

	/**
	 * Constructor
	 */
	public GpibNiWin() {
		jniGpib = new JniGpib("gpibc");
		deviceIndex = new HashMap<String, Integer>();
		logger.debug("GPIB Constructor");
	}
	
	@Override
	public void configure(){
		// no configuration required
	}

	/**
	 * Finds GPIB board/device
	 * 
	 * @param deviceName
	 *            name of the device/borad to be found
	 * @return integer representing the handle to the device/board
	 * @exception DeviceException
	 *                if the GPIB device cannot be found
	 */
	@Override
	public int findDevice(String deviceName) throws DeviceException {
		int deviceDescriptor[] = { -1 };
		if (deviceIndex.containsKey(deviceName))
			deviceDescriptor[0] = deviceIndex.get(deviceName);
		else {
			byte[] buffer1 = deviceName.getBytes();
			logger.debug("devicename is " + deviceName);
			jniGpib.callJni2("gpibinit", deviceDescriptor, buffer1, 1);
			deviceIndex.put(deviceName, new Integer(deviceDescriptor[0]));
		}
		return deviceDescriptor[0];
	}

	/**
	 * Conducts a serial poll
	 * 
	 * @param deviceName
	 *            name of the device to be polled
	 * @return returns the poll byte
	 * @exception DeviceException
	 *                if the GPIB device is in an error state
	 */
	@Override
	public int getSerialPollByte(String deviceName) throws DeviceException {
		int deviceDescriptor[] = { findDevice(deviceName) };
		byte[] buffer = new byte[1];

		jniGpib.callJni2("gpibserialpollbyte", deviceDescriptor, buffer, DUMMYINT);
		logger.debug("the serial poll byte is " + buffer[0]);
		int replyInt = buffer[0];
		return replyInt;
	}

	/**
	 * Clears a specific device
	 * 
	 * @param deviceName
	 *            name of the device to be cleared
	 * @exception DeviceException
	 *                if the GPIB device is in an error state
	 */
	@Override
	public void sendDeviceClear(String deviceName) throws DeviceException {
		int deviceDescriptor[] = { findDevice(deviceName) };
		int value[] = { 0 };
		jniGpib.callJni("gpibdeviceclear", deviceDescriptor, value);
	}

	/**
	 * Assert interface clear
	 * 
	 * @param interfaceName
	 * @exception DeviceException
	 *                if the GPIB is in an error state
	 */
	@Override
	public void sendInterfaceClear(String interfaceName) throws DeviceException {
		int interfaceDescriptor[] = { findDevice(interfaceName) };
		int value[] = { 0 };
		jniGpib.callJni("gpibinterfaceclear", interfaceDescriptor, value);
	}

	/**
	 * Change the I/O timeout period
	 * 
	 * @param deviceName
	 *            name of the Gpib device
	 * @param timeout
	 *            allowed for reads/writes in milliseconds
	 * @throws DeviceException
	 */
	@Override
	public void setTimeOut(String deviceName, int timeout) throws DeviceException {
		int deviceDescriptor[] = { findDevice(deviceName) };
		int tmoCode[] = { setTimeOutCode(timeout) };
		jniGpib.callJni("gpibsettimeout", deviceDescriptor, tmoCode);
	}

	/**
	 * Gets the time aollowed for reads/writes in milliseconds
	 * 
	 * @param deviceName
	 *            name of the Gpib device
	 * @return timeout the allowed for reads/writes in milliseconds
	 * @exception DeviceException
	 *                if the GPIB device is in an error state
	 */
	@Override
	public int getTimeOut(String deviceName) throws DeviceException {
		int deviceDescriptor[] = { findDevice(deviceName) };
		int value[] = { 0 };
		jniGpib.callJni("gpibgettimeout", deviceDescriptor, value);
		return value[0];
	}

	/**
	 * Sets read/write termination chracter
	 * 
	 * @param deviceName
	 *            name of the Gpib device
	 * @param cterm
	 *            the termination character
	 * @exception DeviceException
	 *                if the GPIB device is in an error state
	 */
	@Override
	public void setTerminator(String deviceName, char cterm) throws DeviceException {
		int deviceDescriptor[] = { findDevice(deviceName) };
		int term[] = { cterm | READ | WRITE };
		jniGpib.callJni("gpibsetterminator", deviceDescriptor, term);
	}

	/**
	 * Gets read/write termination character
	 * 
	 * @param deviceName
	 *            name of the Gpib device
	 * @return the termination character
	 * @exception DeviceException
	 *                if the GPIB device is in an error state
	 */
	@Override
	public char getTerminator(String deviceName) throws DeviceException {
		int deviceDescriptor[] = { findDevice(deviceName) };
		int value[] = { 0 };
		jniGpib.callJni("gpibgetterminator", deviceDescriptor, value);
		logger.debug("the terminator is " + value[0]);
		return (char) value[0];
	}

	/**
	 * Sets read termination for talkers
	 * 
	 * @param deviceName
	 *            name of the Gpib device
	 * @param terminate
	 *            if true, terminator character is used to terminate reads if false, no read termination is performed.
	 * @exception DeviceException
	 *                if the GPIB device is in an error state
	 */
	@Override
	public void setReadTermination(String deviceName, boolean terminate) throws DeviceException {
		setTermination(deviceName, terminate, READ);
	}

	/**
	 * Sets write termination for listeners
	 * 
	 * @param deviceName
	 *            name of the Gpib device
	 * @param terminate
	 *            if true, terminator character is used to terminate writes if false no write termination is performed
	 * @exception DeviceException
	 *                if the GPIB device is in an error state
	 */
	@Override
	public void setWriteTermination(String deviceName, boolean terminate) throws DeviceException {
		setTermination(deviceName, terminate, WRITE);
	}

	/**
	 * @param deviceName
	 * @param terminate
	 * @param rw
	 * @throws DeviceException
	 */
	public void setTermination(String deviceName, boolean terminate, int rw) throws DeviceException {
		int deviceDescriptor[] = { findDevice(deviceName) };
		int term[] = new int[1];
		term[0] = getTerminator(deviceName);

		if (terminate) {
			term[0] |= rw;
		} else {
			term[0] &= 0x00FF;
		}

		jniGpib.callJni("gpibsetterminator", deviceDescriptor, term);
	}

	/**
	 * Gets read termination for talkers
	 * 
	 * @param deviceName
	 *            name of the Gpib device
	 * @return if true, termination chracter is used to terminate reads if false , no read termination is performed
	 * @throws DeviceException
	 */
	@Override
	public boolean getReadTermination(String deviceName) throws DeviceException {
		int deviceDescriptor[] = { findDevice(deviceName) };
		int value[] = { 0 };
		jniGpib.callJni("gpibgetreadtermination", deviceDescriptor, value);
		return (value[0] != 0);
	}

	/**
	 * Gets write termination for listeners
	 * 
	 * @param deviceName
	 *            name of the Gpib device
	 * @return if true, termination chracter is used to terminate write if false , no write termination is performed
	 * @throws DeviceException
	 */
	@Override
	public boolean getWriteTermination(String deviceName) throws DeviceException {
		int deviceDescriptor[] = { findDevice(deviceName) };
		int value[] = { 0 };
		jniGpib.callJni("gpibgetwritetermination", deviceDescriptor, value);
		return (value[0] != 0);
	}

	/**
	 * Reads the reply string from gpib
	 * 
	 * @param deviceName
	 *            name of the Gpib device
	 * @return the reply
	 * @exception DeviceException
	 */
	@Override
	public String read(String deviceName) throws DeviceException {
		return read(deviceName, BUFFERSIZE);
	}

	/**
	 * Reads a string of specified length
	 * 
	 * @param deviceName
	 *            name of the Gpib device
	 * @param strLength
	 *            length of the string to be read
	 * @return the reply string
	 * @exception DeviceException
	 */

	@Override
	public String read(String deviceName, int strLength) throws DeviceException {
		int deviceDescriptor[] = { findDevice(deviceName) };
		byte buffer[] = new byte[strLength];
		long n;
		n = jniGpib.callJni2("gpibread", deviceDescriptor, buffer, strLength);

		String reply = new String(buffer, 0, (int) n);
		logger.debug("The read value is " + reply);
		return reply;
	}

	/**
	 * Writes a String
	 * 
	 * @param deviceName
	 *            name of the Gpib device
	 * @param command
	 *            the string to be written
	 * @exception DeviceException
	 */
	@Override
	public void write(String deviceName, String command) throws DeviceException {
		// command = "\u00DE";
		int deviceDescriptor[] = { findDevice(deviceName) };
		byte[] buffer = command.getBytes();
		for (int i = 0; i < buffer.length; i++) {
			logger.debug("the buffer is" + buffer[i]);
		}
		long nChars = command.length();
		jniGpib.callJni2("gpibwrite", deviceDescriptor, buffer, nChars);
	}

	/**
	 * returns an array of device names found
	 * 
	 * @param attributeName
	 *            should be 'DeviceNameList'
	 * @return device names as Object[]
	 * @exception DeviceException
	 *                when the attributeName doesn't match or no devices exist.
	 */
	public Object[] getAttributes(String attributeName) throws DeviceException {
		if (deviceIndex.isEmpty())
			throw new DeviceException("No devices currently exist");
		else if (!attributeName.equals("DeviceNameList"))
			throw new DeviceException("No matching attribute name exist");
		else {
			Set<String> tempSet = deviceIndex.keySet();
			Object[] objArray = tempSet.toArray();
			/*
			 * FIXME - tidy this up if no longer needed? String[] deviceNameArray = new String[objArray.length]; for(int
			 * i =0 ; i < deviceNameArray.length; i++) { deviceNameArray[i] = (String)objArray[i];
			 * Message.out(deviceNameArray[i]); }
			 */
			return objArray;
		}
	}

	private int setTimeOutCode(int tmo) {
		int code = 0;
		if (tmo > 0) {
			code = 5;
			if (tmo > 1)
				code++;
			if (tmo > 3)
				code++;
			if (tmo > 10)
				code++;
			if (tmo > 30)
				code++;
			if (tmo > 100)
				code++;
			if (tmo > 300)
				code++;
			if (tmo > 1000)
				code++;
			if (tmo > 3000)
				code++;
			if (tmo > 10000)
				code++;
			if (tmo > 30000)
				code++;
			if (tmo > 100000)
				code++;
			if (tmo > 300000)
				code++;
		}
		return code;
	}
}
