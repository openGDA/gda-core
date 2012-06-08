/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.pco4000;

import gda.device.DeviceException;

/**
 * Interface to the PCO hardware, this is for teh simulation and the real device
 *
 */
public interface IPCO4000Hardware {

	/**
	 * This method should expose the detector for the time specified, and put the data into
	 * the filename specified
	 * @param fileName
	 * @param exposureTime
	 * @throws DeviceException 
	 */
	public void exposeDetector(String fileName, Double exposureTime) throws DeviceException;
	
	/**
	 * returns the ID of the device, this is device specific.
	 * @return The ID to be given to Nexus.
	 */
	public String getDetectorID();
	
}
