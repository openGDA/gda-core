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

package uk.ac.gda.server.ncd.detectorsystem;

import uk.ac.gda.server.ncd.subdetector.INcdSubDetector;
import gda.device.DeviceException;
import gda.device.detector.NexusDetector;

/**
 * Detector system of non crystalline diffraction to allow scans to take time series at each point.
 */
public interface NcdDetector extends NexusDetector {

	public void clear() throws DeviceException;

	public void start() throws DeviceException;

	@Override
	public void stop() throws DeviceException;

	/**
	 * @return number of collected frames
	 * @throws DeviceException
	 */
	public int getNumberOfFrames() throws DeviceException;

	public String getTfgName() throws DeviceException;

	public void addDetector(INcdSubDetector det) throws DeviceException;
	
	public void removeDetector(INcdSubDetector det);
}