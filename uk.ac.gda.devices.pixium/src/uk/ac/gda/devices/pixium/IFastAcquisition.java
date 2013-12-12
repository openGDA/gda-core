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

package uk.ac.gda.devices.pixium;

import gda.device.DeviceException;

import java.io.File;
import java.io.IOException;

public interface IFastAcquisition {

	public abstract void acquire(int numberOfImage) throws Exception;

	/**
	 * processes after collected data: e.g.
	 * <li>Archival of data collected</li>
	 * <li>Display the last image</li>
	 */
	public abstract void afterAcquire();

	/**
	 * processes to be done before starting acquire data from detector - for example:
	 * <li>create data storage parameters </li>
	 * <li> create metadata file, if required</li>
	 * 
	 * @throws Exception
	 * @throws IOException
	 * @throws DeviceException
	 */
	public abstract void beforeAcquire() throws Exception, IOException, DeviceException;

	// Helper methods for dealing with the file system.
	public abstract File createMainFileStructure() throws IOException;

}