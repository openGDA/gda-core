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

package gda.device.temperature;

import gda.device.TemperatureRamp;
import gda.util.PollerEvent;

/**
 * Stages connected to LinkamCI controllers must implement this interface
 */
public interface LinkamStage {

	/**
	 * LinkamCI will call this after sending its own startup commands
	 */
	public void sendStartupCommands();

	/**
	 * LinkamCI will call this before sending a ramp
	 * 
	 * @param ramp
	 */
	public void sendRamp(TemperatureRamp ramp);

	/**
	 * LinkamCI will call this when ramping starts (start of ramp 0)
	 */
	public void startRamping();

	/**
	 * LinkamCI will call this at start of experiment (start of ramp 1)
	 */
	public void startExperiment();

	/**
	 * LinkamCI will call this when asked to stop
	 */
	public void stop();

	/**
	 * LinkamCI will call this from within its own pollDone
	 * 
	 * @param pe
	 */
	public void pollDone(PollerEvent pe);

	/**
	 * LinkamCI will used this if asked for a file name for the temperature data
	 * 
	 * @return data filename
	 */
	public String getDataFileName();
}
