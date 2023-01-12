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

import static uk.ac.gda.devices.pressurecell.controller.ArmablePressureValve.ArmedValveState.CLOSED_ARMED;
import static uk.ac.gda.devices.pressurecell.controller.ArmablePressureValve.ArmedValveState.OPEN;
import static uk.ac.gda.devices.pressurecell.controller.ArmablePressureValve.ArmedValveState.OPEN_ARMED;

import gda.device.DeviceException;

/**
 * Controller for the armable valves in the Diamond pressure valves
 * <br>
 * These valves can be opened, closed and reset in the same way as the
 * non-armable valves, but these also support an external hardware signal
 * that allows the valves opening to be synchonised with data collection.
 */
public interface ArmablePressureValve {
	public enum ArmedValveState {
		FAULT,
		OPEN,
		OPEN_ARMED,
		CLOSED,
		CLOSED_ARMED;
		public boolean isOpen() {
			return this == OPEN || this == OPEN_ARMED;
		}
	}
	/** Open the valve - blocks until move is complete */
	void open() throws DeviceException;
	/** Close the valve - blocks until move is complete */
	void close() throws DeviceException;
	/** Reset the valve and clear any interlocks status */
	void reset() throws DeviceException;
	/** Arm the valve ready to be triggered by an external hardware signal */
	void arm() throws DeviceException;
	/** Reset the armed state so that it is no longer waiting for an external hardware signal */
	void disarm() throws DeviceException;
	/** Get the current state of this valve */
	ArmedValveState getState() throws DeviceException;

	/** Check if the valve is currently open (ignoring any armed status */
	default boolean isOpen() throws DeviceException {
		ArmedValveState state = getState();
		return state == OPEN || state == OPEN_ARMED;
	}

	/** Get the armed state, ignoring whether the valve is open or closed */
	default boolean isArmed() throws DeviceException {
		ArmedValveState state = getState();
		return state == CLOSED_ARMED || state == OPEN_ARMED;
	}
}
