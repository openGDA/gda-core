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

package gda.device;

import java.util.ArrayList;

/**
 * An interface to be implemented by distributed Temperature controllers
 */
public interface Temperature extends Scannable {
	/**
	 * Get the current temperature
	 * 
	 * @return the current temperature
	 * @throws DeviceException
	 */
	public double getCurrentTemperature() throws DeviceException;

	/**
	 * Set the target or end temperature
	 * 
	 * @param target
	 *            the target temperature
	 * @throws DeviceException
	 */
	public void setTargetTemperature(double target) throws DeviceException;

	/**
	 * Get the target temperature
	 * 
	 * @return the target temperature
	 * @throws DeviceException
	 */
	public double getTargetTemperature() throws DeviceException;

	/**
	 * Check the current temperature against the target temperature
	 * 
	 * @return true if current temperature equals target temperature
	 * @throws DeviceException
	 */
	public boolean isAtTargetTemperature() throws DeviceException;

	/**
	 * Set to the lower operating temperature
	 * 
	 * @param lowLimit
	 *            the lower temperature limit in degrees C
	 * @throws DeviceException
	 */
	public void setLowerTemp(double lowLimit) throws DeviceException;

	/**
	 * Set the upper operating temperature
	 * 
	 * @param upperLimit
	 *            the upper temperature limit in degrees C
	 * @throws DeviceException
	 */
	public void setUpperTemp(double upperLimit) throws DeviceException;

	/**
	 * Get the upper operating temperature limit
	 * 
	 * @return the upper temperature limit in degrees C
	 * @throws DeviceException
	 */
	public double getUpperTemp() throws DeviceException;

	/**
	 * Get the lower operating temperature limit
	 * 
	 * @return the lower temperature limit in degrees C
	 * @throws DeviceException
	 */
	public double getLowerTemp() throws DeviceException;

	/**
	 * Get probe names
	 * 
	 * @return an array of probe names
	 * @throws DeviceException
	 */
	public ArrayList<String> getProbeNames() throws DeviceException;

	/**
	 * Select probe
	 * 
	 * @param probeName
	 *            the probe to use
	 * @throws DeviceException
	 */
	public void setProbe(String probeName) throws DeviceException;

	/**
	 * Suspend the current thread and wait for the controller to reach its target temperature
	 * 
	 * @throws DeviceException
	 */
	public void waitForTemp() throws DeviceException;

	/**
	 * Clears all specified ramps
	 * 
	 * @throws DeviceException
	 */
	public void clearRamps() throws DeviceException;

	/**
	 * Adds a new ramp
	 * 
	 * @param ramp
	 *            the new TemperatureRamp to add
	 * @throws DeviceException
	 */
	public void addRamp(TemperatureRamp ramp) throws DeviceException;

	/**
	 * Start running the ramps
	 * 
	 * @throws DeviceException
	 */
	public void start() throws DeviceException;

	/**
	 * Stops running ramps or moving to temperature (allows the device to do whatever it does while not running
	 * programs), or in some case just hold the temperature position
	 * 
	 * @throws DeviceException
	 */
	@Override
	public void stop() throws DeviceException;

	/**
	 * perform the start/restart up procedure of the controller
	 * 
	 * @throws DeviceException
	 */
	public void begin() throws DeviceException;

	/**
	 * perform the shutdown procedure of the controller
	 * 
	 * @throws DeviceException
	 */
	public void end() throws DeviceException;

	/**
	 * sets the ramp rate
	 * 
	 * @param rate
	 * @throws DeviceException
	 */
	public void setRampRate(double rate) throws DeviceException;

	/**
	 * gets the ramp rate
	 * 
	 * @return rate
	 * @throws DeviceException
	 */
	public double getRampRate() throws DeviceException;

	/**
	 * Stops running ramps or moving to temperature but holds the current temperature
	 * 
	 * @throws DeviceException
	 */
	public void hold() throws DeviceException;

	/**
	 * Sets a list of ramps
	 * 
	 * @param ramps
	 *            an ArrayList<TemperatureRamp> of TemperatureRamps
	 * @throws DeviceException
	 */
	public void setRamps(ArrayList<TemperatureRamp> ramps) throws DeviceException;

	/**
	 * Gets the running state of the controller
	 * 
	 * @return true if controller is running a program or moving to a target false otherwise
	 * @throws DeviceException
	 */
	public boolean isRunning() throws DeviceException;
}
