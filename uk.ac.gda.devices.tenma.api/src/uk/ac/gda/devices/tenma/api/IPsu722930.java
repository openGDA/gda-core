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

public interface IPsu722930 {

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
	 * 			The requested voltage
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
	 */
	boolean outputIsOn() throws DeviceException;

	/**
	 * Gradually moves the current from one value to another
	 *
	 * @param startCurrent
	 *            The starting current
	 * @param endCurrent
	 *            The end current
	 * @param secondsToRamp
	 *            The time it should take to get from startCurrent to endCurrent
	 * @throws DeviceException
	 *             If there is a problem communicating with the device
	 */
	void rampCurrent(double startCurrent, double endCurrent, double secondsToRamp)
			throws DeviceException, InterruptedException;

	/**
	 * Gradually moves the current from its present value to zero over the specified number of seconds
	 *
	 * @param rampTimeInSeconds
	 *            The time in seconds it should take to reduce the current to zero
	 * @throws DeviceException
	 *             If there is a problem communicating with the device
	 */
	void rampToZero(double rampTimeInSeconds) throws DeviceException, InterruptedException;

	/**
	 * Gradually moves the current from its present value to zero over 60 seconds
	 *
	 * @throws DeviceException
	 *             If there is a problem communicating with the device
	 */
	void rampToZero() throws DeviceException, InterruptedException;

	/**
	 * Gets the step size used when gradually ramping the current
	 *
	 * @return The step size
	 */
	double getCurrentStepSize();

	/**
	 * Sets the step size used when gradually ramping the current
	 *
	 * @param currentStepSize
	 *            The step size
	 */
	void setCurrentStepSize(double currentStepSize);
}
