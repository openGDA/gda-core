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

package uk.ac.gda.devices.phantom;

import gda.device.Detector;
import gda.device.DeviceException;

/**
 * Definition of the CORBA interface for the Phantom Detector
 *
 */
public interface Phantom extends Detector{

	/**
	 * This method sets up the camera to take a collection, outside of the standard scan mechinism
	 * @param numberOfFrames the number of frames to take
	 * @param framesPerSecond the rate of the camera in frames per second
	 * @param width the width of the image in pixels
	 * @param height the height of the image in pixels
	 * @throws DeviceException
	 */
	public void setUpForCollection(int numberOfFrames, int framesPerSecond, int width, int height)
			throws DeviceException;

	/**
	 * method to retrieve the data over corba
	 * @param cineNumber The number of the cine to read the data from
	 * @param start number of the first frame to transfer
	 * @param count number of frames to transfer in total
	 * @return A ScanFileHolder containing the information
	 * @throws DeviceException
	 */
	public Object retrieveData(int cineNumber, int start, int count) throws DeviceException;

	/**
	 * This simple method passes the command directly to the camera and passes back the responce.
	 * This should make prototyping in jython simple
	 * @param commandString the string to send to the camera
	 * @return the responce from the camera
	 * @throws DeviceException if there are problems contacting the camera.
	 */
	public String command(String commandString) throws DeviceException;

}
