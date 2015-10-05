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
 * An interface for a distributed PEEM Uview Detector class
 */
public interface UViewNew extends Detector {

	/**
	 * Acquire a single image from the detector
	 * 
	 * @return String = image file name
	 * @throws DeviceException
	 * @throws InterruptedException 
	 */
	public String shotSingleImage() throws DeviceException, InterruptedException;

	/**
	 * Prepare image file
	 * 
	 * @throws DeviceException 
	 */
	public void prepare() throws DeviceException;

	/**
	 * @return object
	 * @throws DeviceException
	 */
	public Object getHashROIs() throws DeviceException;
	
	/**
	 * @param nameROI
	 * @return int
	 * @throws DeviceException 
	 */
	public int createROI(String nameROI) throws DeviceException;

	/**
	 * @param nameROI
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @throws DeviceException 
	 */
	public void setBoundsROI(String nameROI, int x, int y, int width, int height) throws DeviceException;

	/**
	 * @param nameROI
	 * @return object
	 * @throws DeviceException 
	 */
	public Object getBoundsROI(String nameROI) throws DeviceException;

	/**
	 * @param nameROI
	 * @return object
	 * @throws DeviceException
	 */
	public Object readoutROI(String nameROI) throws DeviceException;

	/**
	 * Connect to the PEEM UView software on remote host.
	 * 
	 * @param host
	 *            The remote host IS is running on.
	 * @throws DeviceException
	 */
	public void connect(String host) throws DeviceException;

	/**
	 * Use this method to disconnect from the PEEM UView host.
	 */
	public void disconnect();

	/**
	 * Is the UView Detector object connected to the UView host?
	 * 
	 * @return true or false
	 */
	public boolean isConnected();

}
