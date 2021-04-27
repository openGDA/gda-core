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

import static uk.ac.gda.devices.pressurecell.controller.PressureValve.ValveState.OPEN;

import gda.device.DeviceException;

/**
 * Controller for the valves in the Diamond pressure valves
 * <br>
 * These valves can be opened, closed and reset. These valves are protected by PSS
 * so the reset command can be used to clear interlocks when state is valid again.
 */
public interface PressureValve {
	public enum ValveState {
		FAULT, OPEN, OPENING, CLOSED, CLOSING;
	}
	/** Open the valve - blocks until move is complete */
	void open() throws DeviceException;
	/** Close the valve - blocks until move is complete */
	void close() throws DeviceException;
	/** Reset the status of the valve, clearing any interlocks status */
	void reset() throws DeviceException;
	/** Get the current state of the valve */
	ValveState getState() throws DeviceException;
	/** Check if the valve is currently open */
	default boolean isOpen() throws DeviceException {
		return getState() == OPEN;
	}
}
