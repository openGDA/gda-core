/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.tenma.api;

import gda.device.DeviceException;

public interface IPsu722931Controller {

	/**
	 * Gets the current
	 *
	 * @return The current
	 * @throws DeviceException
	 */
	double getCurrent() throws DeviceException;

	/**
	 * Sets the current
	 *
	 * @param current
	 *            The requested current
	 * @throws DeviceException
	 *             If there is a problem communicating with the device
	 */
	void setCurrent(double current) throws DeviceException;

	/**
	 * Gets the voltage
	 *
	 * @return The voltage
	 * @throws DeviceException
	 *             If there is a problem communicating with the device
	 */
	double getVoltage() throws DeviceException;

	/**
	 * Sets the voltage
	 *
	 * @param voltage
	 * 			The voltage
	 * @throws DeviceException
	 * 			If there is a problem communicating with the device
	 */
	void setVoltage(double voltage) throws DeviceException;

	/**
	 * Turns on the output
	 *
	 * @throws DeviceException
	 *             If there is a problem communicating with the device
	 */
	void outputOn() throws DeviceException;

	/**
	 * Turns off the output
	 *
	 * @throws DeviceException
	 *             If there is a problem communicating with the device
	 */
	void outputOff() throws DeviceException;

	/**
	 * Indicates whether the output is currently on
	 *
	 * @return A boolean indicating if the output is on
	 * @throws DeviceException
	 *             If there is a problem communicating with the device
	 */
	boolean outputIsOn() throws DeviceException;
}
