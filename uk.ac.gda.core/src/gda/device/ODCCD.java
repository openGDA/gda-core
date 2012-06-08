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

import gda.device.detector.odccd.ODCCDImage;

import java.io.IOException;

/**
 * Methods (additional to those of Device and Detector) required to control Oxford Detector CCD.
 */
public interface ODCCD extends Device, Detector {

	/**
	 * Connect to the IS software on remote host.
	 * 
	 * @param host
	 *            The remote host IS is running on.
	 * @throws IOException
	 */
	public void connect(String host) throws IOException;

	/**
	 * Use this method to disconnect from the IS software.
	 */
	public void disconnect();

	/**
	 * Is the CCD control object connected to the CCD?
	 * 
	 * @return true or false
	 */
	public boolean isConnected();

	/**
	 * Returns the name of the last data read from the CCD.
	 * 
	 * @return The data name.
	 */
	public String getDataName();

	/**
	 * Read the CCD temperature.
	 * 
	 * @return The CCD temperature.
	 */
	public double temperature();

	/**
	 * Read the chiller unit water temperature.
	 * 
	 * @return The water temperature.
	 */
	public double waterTemperature();

	/**
	 * Use this method to call a user script on the IS host. Example 1: call save_dark 1.0 2 \"d:/dark2.img\" Example 3:
	 * call dark_cor 10.0 2 "//root/Darks/"
	 * 
	 * @param command
	 *            The command to run on IS
	 */
	public void runScript(String command);

	/**
	 * Read the shutter status
	 * 
	 * @return OPEN or CLOSED
	 */
	public String shutter();

	/**
	 * Method to open the shutter. It returns the status of the shutter.
	 * 
	 * @return OPEN
	 */
	public String openShutter();

	/**
	 * Method to close the shutter. It returns the status of the shutter.
	 * 
	 * @return CLOSED
	 */
	public String closeShutter();

	/**
	 * Reads the data from an IS database node.
	 * 
	 * @param pathname
	 *            The location of the data
	 * @return The image data in a ODCCDImage object.
	 */
	public ODCCDImage readDataFromISDataBase(String pathname);

}
