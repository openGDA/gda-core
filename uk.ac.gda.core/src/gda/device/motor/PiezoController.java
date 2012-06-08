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

import gda.device.MotorException;

/**
 * PiezoController interface
 */
public interface PiezoController {
	/**
	 * Used to send a command to the piezo module that does not require a reply.
	 * 
	 * @param module
	 *            is the module number
	 * @param positionCommand -
	 *            the position to set in command
	 * @throws MotorException
	 */
	public abstract void setPosition(int module, String positionCommand) throws MotorException;

	/**
	 * Sends the commands required to return the current rawPosition (offset), converts this value to a position
	 * (offset) and compares with requested position (offset). Notifies when the process is complete.
	 * 
	 * @param module
	 *            the module to query the position and status
	 * @return the reply string
	 * @throws MotorException
	 */
	public abstract String getPositionAndStatus(int module) throws MotorException;
}