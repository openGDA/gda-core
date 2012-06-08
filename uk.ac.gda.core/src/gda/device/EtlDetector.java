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

/**
 * EtlDetector interface
 */
public interface EtlDetector extends Detector {
	/**
	 * Sets the high voltage input in milli-volt for the detector.
	 * 
	 * @param mv
	 * @throws DeviceException
	 */
	public void setHV(int mv) throws DeviceException;

	/**
	 * Gets the actual output high voltage at the detector.
	 * 
	 * @return actual HV
	 * @throws DeviceException
	 */
	public int getActualHV() throws DeviceException;

	/**
	 * Gets the requested High Voltage from the detector register.
	 * 
	 * @return HV
	 * @throws DeviceException
	 */
	public int getHV() throws DeviceException;

	/**
	 * Sets the window's upper threshold for the detector.
	 * 
	 * @param ulim
	 * @throws DeviceException
	 */
	public void setUpperThreshold(int ulim) throws DeviceException;

	/**
	 * Gets the window's upper threshold from the detector.
	 * 
	 * @return Upper Threshold
	 * @throws DeviceException
	 */
	public int getUpperThreshold() throws DeviceException;

	/**
	 * Sets the window's lower threshold of the detector.
	 * 
	 * @param llim
	 * @throws DeviceException
	 */
	public void setLowerThreshold(int llim) throws DeviceException;

	/**
	 * Gets window's lower threshold from the detector.
	 * 
	 * @return Lower Threshold
	 * @throws DeviceException
	 */
	public int getLowerThreshold() throws DeviceException;

}
