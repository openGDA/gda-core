/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.device.epicsdevice;

import gda.device.DeviceException;

/**
 * Extend IEpicsDevice to add the functions required to control an XMAP device
 */
public interface XmapEpicsDevice extends IEpicsDevice {

	/**
	 * Tests whether the device has been configured (via the configure() method)
	 *
	 * @return true if configured, false otherwise
	 */
	boolean isConfigured();

	/**
	 * Set the value of an Epics record, specifying the type
	 *
	 * @param type
	 *            Type of the value: note that null may be the only valid value in some implementations
	 * @param record
	 *            Epics record to set
	 * @param field
	 *            Field within the record (can be empty string)
	 * @param val
	 *            Value to set
	 * @param putTimeout
	 *            Timeout on the call in seconds
	 * @throws DeviceException
	 */
	void setValue(Object type, String record, String field, Object val, double putTimeout) throws DeviceException;

	/**
	 * Set the value of an Epics record, using a default timeout.
	 *
	 * @param record
	 *            Epics record to set
	 * @param field
	 *            Field within the record (can be empty string)
	 * @param val
	 *            Value to set
	 * @throws DeviceException
	 */
	void setValue(String record, String field, Object val) throws DeviceException;

	/**
	 * Set the value of an Epics record, without waiting for the put to complete
	 *
	 * @param record
	 *            Epics record to set
	 * @param field
	 *            Field within the record (can be empty string)
	 * @param val
	 *            Value to set
	 * @throws DeviceException
	 */
	void setValueNoWait(String record, String field, Object val) throws DeviceException;

	/**
	 * Get the value of an Epics record, specifying the return type required
	 *
	 * @param returnType
	 *            One of the permitted values of ReturnType
	 * @param record
	 *            Epics record to get
	 * @param field
	 *            Field within the record (can be empty string)
	 * @return Value of the Epics record
	 * @throws DeviceException
	 */
	Object getValue(ReturnType returnType, String record, String field) throws DeviceException;

	/**
	 * Get the value of an Epics record as a string
	 *
	 * @param record
	 *            Epics record to get
	 * @param field
	 *            Field within the record (can be empty string)
	 * @return Value of the Epics record as a string
	 * @throws DeviceException
	 */
	String getValueAsString(String record, String field) throws DeviceException;

	/**
	 * Get the PV associated with a given element
	 *
	 * @param mcaName
	 *            Name of the element
	 * @return Corresponding PV
	 */
	String getRecordPV(String mcaName);
}
