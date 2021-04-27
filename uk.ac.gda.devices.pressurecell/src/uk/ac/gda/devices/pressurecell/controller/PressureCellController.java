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

package uk.ac.gda.devices.pressurecell.controller;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import gda.device.DeviceException;

/** Low level controller for the Pressure Cell hardware
 * <br>
 * Allows both epics and dummy mode versions to be wrapped by a single PressureCell
 * scannable.
 *
 * Methods in these controllers should generally be blocking with any asynchronous
 * control being handled at the scannable level. The one {@link #asyncGo()} method allows
 * the pressure cell to be started without waiting for the move to complete. The returned
 * Future gives access to the ongoing move and returns the final pressure when the move is
 * complete.
 */
public interface PressureCellController {
	/** Get the current pressure at the sample cell
	 * @throws DeviceException */
	double getCellPressure() throws DeviceException;
	/** Get the pressure in the intermediate chamber (P2) */
	double getIntermediatePressure() throws DeviceException;
	/** Get the pressure at the pump */
	double getPumpPressure() throws DeviceException;
	/** Get the valve controller for V3 (between the pump and the intermediate chamber) */
	PressureValve getV3();
	/** Get the valve controller for V5 (between the intermediate chamber and the sample) */
	ArmablePressureValve getV5();
	/** Get the valve controller for V6 (between the intermediate chamber and the sample) */
	ArmablePressureValve getV6();
	/** RBV for the target pressure (non jump pressure) */
	double getTargetPressure() throws DeviceException;
	/** Set the target pressure */
	void setTargetPressure(double target) throws DeviceException;
	/** RBV for the pressure after a jump */
	double getJumpToPressure() throws DeviceException;
	/** Set the target for the pressure after a jump */
	void setJumpToPressure(double target) throws DeviceException;
	/** RBV for the pressure before a jump */
	double getJumpFromPressure() throws DeviceException;
	/** Set the starting pressure for a jump */
	void setJumpFromPressure(double target) throws DeviceException;
	/** Stop the pump */
	void stopPump() throws DeviceException;
	/** Stop everything? */
	void stop() throws DeviceException;

	/** Get the status of the Busy flag */
	CellStatus getGoToBusy() throws DeviceException;
	/**
	 * Get the busy state of the controller
	 * <br>
	 * This may be different to goToBusy if move has been requested but is yet to start
	 */
	boolean isBusy();

	/**
	 * Wait while device is busy
	 *
	 * @param timeout the time to wait in the given units. If timeout < 0,
	 * this will block indefinitely
	 * @param unit of time the timeout is given in.
	 */
	void waitForIdle(long timeout, TimeUnit unit) throws DeviceException, InterruptedException;

	/** Move to set pressure - blocks until pressure is reached */
	void go() throws DeviceException, InterruptedException;

	/**
	 * Prepare for pressure jump
	 * <br>
	 * Set cell to initial pressure and intermediate chamber to after pressure
	 * @throws DeviceException
	 * */
	void setJump() throws DeviceException;

	/** Start moving towards target pressure - non blocking but returns future */
	Future<Void> asyncGo() throws DeviceException;
}
