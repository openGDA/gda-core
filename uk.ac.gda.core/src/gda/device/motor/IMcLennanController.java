/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

public interface IMcLennanController {

	public abstract String getName();
	public abstract void close();

	/**
	 * Transmits a command string to a single motor on the serial line. This method is synchronised to ensure the
	 * correct reply returns, to the correct Object. McLennan replies, particularly errors, are generously sprinkled
	 * with carriage returns (without line feeds). These mess up the output, hence the replaceAll() in the printing out
	 * of replies.
	 * 
	 * @param command
	 *            Command code string
	 * @return reply from motor
	 * @throws MotorException
	 */
	public abstract String sendCommand(String command) throws MotorException;

	/**
	 * Issue a single character command to the connected serial device that acts on all motors with immediate effect.
	 * 
	 * @param command
	 *            Global command character
	 * @throws MotorException
	 */
	public abstract void  globalCommand(char command) throws MotorException;
}