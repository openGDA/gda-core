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

import gda.device.DeviceException;

/**
 * Interface which is used by SRControl and DummySRControl so they can be treated in the same way by SRControlMotor
 */
public interface SRControlInterface {
	/**
	 * Analogue value setpoint command
	 */
	final public String SET_VALUE = "CCV";

	/**
	 * Status setpoint command
	 */
	final public String SET_STATUS = "STCC";

	/**
	 * Minimum value setpoint command
	 */
	final public String SET_MINVALUE = "MINV";

	/**
	 * Maximum value setpoint command
	 */
	final public String SET_MAXVALUE = "MAXV";

	/**
	 * Analogue value readback command
	 */
	final public String GET_VALUE = "AQN";

	/**
	 * Status readback command
	 */
	final public String GET_STATUS = "STAQ";

	/**
	 * Interlocks readback command
	 */
	final public String GET_INTERLOCKS = "ITLKS";

	/**
	 * Minimum value readback command
	 */
	final public String GET_MINVALUE = "MINV";

	/**
	 * Maximum value readback command
	 */
	final public String GET_MAXVALUE = "MAXV";

	/**
	 * Bit 4 of interlock word specifying upper limit
	 */
	final public short UPPERLIMIT_INTERLOCK = 8;

	/**
	 * Bit 3 of interlock word specifying lower limit
	 */
	final public short LOWERLIMIT_INTERLOCK = 4;

	/**
	 * Bit 7 of interlock word specifying ?
	 */
	final public short ARRAYTILT_INTERLOCK = 64;

	/**
	 * Bit 12 of interlock word specifying ?
	 */
	final public short GEARBOXFAIL_INTERLOCK = 2048;

	/**
	 * Bit 10 of interlock word specifying ?
	 */
	final public short JOGACTIVE_INTERLOCK = 512;

	/**
	 * Bit 11 of interlock word specifying ?
	 */
	final public short DATUM_INTERLOCK = 1024;

	/**
	 * Bit 2 of interlock word specifying ?
	 */
	final public short ERROR_INTERLOCK = 2;

	/**
	 * Initialize SRControl
	 * 
	 * @throws DeviceException
	 */
	public void initialise() throws DeviceException;

	/**
	 * Set a Value
	 * 
	 * @param parameter
	 *            name of parameter
	 * @param setProperty
	 *            name of property belonging to parameter
	 * @param data
	 *            value to set property to
	 * @throws DeviceException
	 */
	public void setValue(String parameter, String setProperty, double[] data) throws DeviceException;

	/**
	 * Get a Value
	 * 
	 * @param parameter
	 *            name of parameter
	 * @param getProperty
	 *            name of property belonging to parameter
	 * @param data
	 *            value got from property
	 * @throws DeviceException
	 */
	public void getValue(String parameter, String getProperty, double[] data) throws DeviceException;

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
	public void getStatusString(String parameter, double code, StringBuffer statusString, int length)
			throws DeviceException;

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
	public void getUnitsString(String parameter, StringBuffer unitsString, int length) throws DeviceException;

	/**
	 * Returns a byte containing the decimal points descriptor for the control parameter in question
	 * 
	 * @param parameter -
	 *            string containing name of control systeme parameter
	 * @return the decimal points descriptor
	 */
	public byte getDecimalPlaces(String parameter);

}
