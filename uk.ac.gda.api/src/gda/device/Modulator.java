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

import org.jscience.physics.quantities.Frequency;
import org.jscience.physics.quantities.Length;

/**
 * Interface to control the PEM-90 PhotoElastic modulator.
 */
public interface Modulator extends Device {
	/**
	 * Get the current wavelength
	 * 
	 * @return wavelength
	 * @throws DeviceException
	 */
	public Length getWaveLength() throws DeviceException;

	/**
	 * Set the wavelength.
	 * 
	 * @param waveLength
	 *            to be set
	 * @throws DeviceException
	 */
	public void setWaveLength(double waveLength) throws DeviceException;

	/**
	 * Get the current retardation
	 * 
	 * @return current retardation value
	 * @throws DeviceException
	 */
	public int getRetardation() throws DeviceException;

	/**
	 * Set the retardation
	 * 
	 * @param retardation
	 *            to be set
	 * @throws DeviceException
	 */
	public void setRetardation(double retardation) throws DeviceException;

	/**
	 * Reset the device to its factory default settings
	 * 
	 * @throws DeviceException
	 */
	public void reset() throws DeviceException;

	/**
	 * Switch echo ON or OFF
	 * 
	 * @param echo
	 *            if true- ON otherwise - OFF
	 * @throws DeviceException
	 */
	public void setEcho(boolean echo) throws DeviceException;

	/**
	 * Read the current reference frequency
	 * 
	 * @param noOfTimes
	 *            should be either 1 or 2
	 * @return the frequency
	 * @throws DeviceException
	 */
	public Frequency readFrequency(int noOfTimes) throws DeviceException;

	/**
	 * Set the retardation to INHIBIT or NORMAL mode
	 * 
	 * @param inhibit
	 *            if true mode - INHIBIT otherwise mode - NORMAL
	 * @throws DeviceException
	 */
	public void setInhibit(boolean inhibit) throws DeviceException;

}
