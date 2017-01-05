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

import gda.device.DeviceException;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * This interface is here to allow a small abstraction layer between the camera and the drivers. It also allows for a
 * simulation to be put in place
 */
public interface IPhantomV73Controller {

	/**
	 * This method forces a connection between the camera and the server
	 * 
	 * @throws UnknownHostException
	 *             if the host name is unknown
	 * @throws IOException
	 *             if there are communication problems
	 */
	public void connectToCamera() throws UnknownHostException, IOException;

	/**
	 * This simple method sends the command string given to the camera
	 * 
	 * @param commandString
	 *            The comand to give to the camera
	 * @return The string result of the command
	 * @throws DeviceException
	 *             if there is a communication issue
	 */
	public String command(String commandString) throws DeviceException;

	/**
	 * @return true if the camera is connected, false otherwise.
	 */
	public boolean isConneted();

	/**
	 * Sets the controller ready to recieve data on a particular port
	 * 
	 * @param portNumber
	 *            the number of the port that that camera will attach to
	 * @param sixteenBit
	 *            set to true, if the result is to be read out in sixteen bit depth
	 * @throws DeviceException
	 */
	public void prepareForDataTransfer(int portNumber, boolean sixteenBit) throws DeviceException;

	/**
	 * Grabs some data from the buffer held in the controler, and then the data is discarded.
	 * 
	 * @param sizeOfArray
	 *            the amount of data to be pulled off the buffer i.e. the size of the array.
	 * @return the double array of the data which has been requested.
	 * @throws DeviceException
	 *             If there is an IO problem with the camera
	 * @throws IndexOutOfBoundsException
	 *             if there is not enough data in the buffer to fulfil the request
	 */
	public double[] getDataBlock(int sizeOfArray) throws DeviceException, IndexOutOfBoundsException;

	/**
	 * Free up any resources that are being used, such as in this case the socket connection.
	 */
	public void finishDataTransfer();

}
