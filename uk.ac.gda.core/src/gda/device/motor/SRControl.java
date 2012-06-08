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

package gda.device.motor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;

/**
 * A simple device class for Windows systems to interface to the SR control system at DL. It uses the Java native
 * interface to provide access to the fastRPC.dll windows driver file supplied by the control group. Both the
 * fastRPC.dll and the Java wrapper dll, jFastRPC.dll, need to be in the windows system directory. The current user/pc
 * combination needs also needs to be registered with the control network.
 */
public class SRControl extends DeviceBase implements SRControlInterface {
	
	private static final Logger logger = LoggerFactory.getLogger(SRControl.class);
	
	final private int READ = 0;

	final private int WRITE = 1;

	// Native JNI function declarations
	private native int jInitRPC();

	private native int jFastRPC(String parameter, String Property, int mode, double[] data, int length);

	private native void jErrorString(int ErrorCode, StringBuffer Buffer);

	private native int jDblToStatus(String parameter, double data, StringBuffer Status, int length);

	private native int jGetUnits(String parameter, StringBuffer Status, int length);

	private native byte jGetDecimalPlaces(String parameter);

	// Load the 'jFastRPC.dll" library which interfaces this Java class
	// to the DL control group's Windows dll file "FastRPC.dll" via the
	// Java native interface. If any changes are made to the control group's
	// FastRPC.dll these will need to be reflected in the wrapper
	// jFastRPC.dll.
	static {
		System.loadLibrary("jFastRPC");
	}

	@Override
	public void configure(){
		// no configuration required
	}

	/**
	 * Initialise call to Control network, not strictly necessary though can be used to see if network operating
	 * correctly.
	 * 
	 * @throws DeviceException
	 */

	@Override
	public void initialise() throws DeviceException {
		int cc = 0;
		StringBuffer buff = new StringBuffer("");
		cc = jInitRPC();
		if (cc != 0) {
			jErrorString(cc, buff);
			throw new DeviceException("Error initializing connection to SR Control System: " + buff);
		}

	}

	/**
	 * Writes the given data value for the specified property of a control system parameter
	 * 
	 * @param parameter -
	 *            string containing name of control systeme parameter
	 * @param setProperty -
	 *            name of parameter's property to set (use public constants)
	 * @param data -
	 *            value to set, needs to be in a double array element
	 * @throws DeviceException
	 */
	@Override
	public void setValue(String parameter, String setProperty, double[] data) throws DeviceException {
		FastRPC(parameter, setProperty, this.WRITE, data, 1);
	}

	/**
	 * Reads the data value for the specified property of a control system parameter
	 * 
	 * @param parameter -
	 *            string containing name of control systeme parameter
	 * @param getProperty -
	 *            name of parameter's property to get (use public constants)
	 * @param data -
	 *            value to get, needs to be a double array element
	 * @throws DeviceException
	 */
	@Override
	public void getValue(String parameter, String getProperty, double[] data) throws DeviceException {
		FastRPC(parameter, getProperty, this.READ, data, 1);
	}

	/**
	 * Returns a string description of the input status code for the control parameter in question
	 * 
	 * @param parameter -
	 *            string containing name of control systeme parameter
	 * @param code -
	 *            value of parameter's status code
	 * @param statusString -
	 *            stringbuffer to return description in
	 * @param length -
	 *            length of statusString buffer passed in
	 * @throws DeviceException
	 */

	@Override
	public void getStatusString(String parameter, double code, StringBuffer statusString, int length)
			throws DeviceException {
		DblToStatus(parameter, code, statusString, length);
	}

	/**
	 * Returns the units string description for the control parameter in question
	 * 
	 * @param parameter -
	 *            string containing name of control systeme parameter
	 * @param unitsString -
	 *            stringbuffer to return description in
	 * @param length -
	 *            lenght of unitsString buffer
	 * @throws DeviceException
	 */
	@Override
	public void getUnitsString(String parameter, StringBuffer unitsString, int length) throws DeviceException {
		GetUnits(parameter, unitsString, length);
	}

	/**
	 * Returns a byte containing the decimal points descriptor for the control parameter in question
	 * 
	 * @param parameter -
	 *            string containing name of control systeme parameter
	 * @return decimal points descriptor
	 */
	@Override
	public byte getDecimalPlaces(String parameter) {
		byte b = GetDecimalPlaces(parameter);
		return b;
	}

	// Performs a fast RPC call via the Windows dll files jfastRpc.dll
	// and FastRPC.dll using the Java native interface for windows. Can
	// either
	// read or write a given data value for a specified property of a
	// control
	// system#
	// parameter
	private void FastRPC(String parameter, String property, int mode, double[] data, int length) throws DeviceException {
		int retries = 0;
		int cc = 0;
		StringBuffer buff = new StringBuffer("");

		// Error 117 indicates a network timeout. For this error we
		// try 10 times before giving up and throwing the exception.
		// See bug #364.
		do {
			cc = jFastRPC(parameter, property, mode, data, length);
			retries++;
		} while (cc == 117 && retries < 10);

		if (cc != 0) {
			logger.warn("jFastRPC returned an error, return value: " + cc);
			jErrorString(cc, buff);
			if (mode == READ) {
				throw new DeviceException("Error reading from SR Control System: " + buff);
			}
			throw new DeviceException("Error writing to SR Control System: " + buff);
		}
	}

	// Performs a DlToStatus call via the Windows dll files jfastRpc.dll
	// and FastRPC.dll using the Java native interface for windows.
	private int DblToStatus(String parameter, double data, StringBuffer status, int length) throws DeviceException {

		int cc = 0;
		StringBuffer buff = new StringBuffer("");
		cc = jDblToStatus(parameter, data, status, length);
		if (cc != 0) {
			jErrorString(cc, buff);
			throw new DeviceException("Error in DblToStatus - " + buff);
		}
		return (cc);
	}

	// Performs a GetUnits call via the Windows dll files jfastRpc.dll
	// and FastRPC.dll using the Java native interface for windows.
	private int GetUnits(String parameter, StringBuffer status, int length) throws DeviceException {

		int cc = 0;
		StringBuffer buff = new StringBuffer("");
		cc = jGetUnits(parameter, status, length);
		if (cc != 0) {
			jErrorString(cc, buff);
			throw new DeviceException("Error in GetUnits - " + buff);
		}
		return (cc);
	}

	// Performs a GetDecimalPLaces call via the Windows dll files
	// jfastRpc.dll
	// and FastRPC.dll using the Java native interface for windows. Can
	// either
	// read or write a given data value for a specified property of a
	// control
	// system#
	// parameter
	private byte GetDecimalPlaces(String parameter) {
		return jGetDecimalPlaces(parameter);
	}
}
