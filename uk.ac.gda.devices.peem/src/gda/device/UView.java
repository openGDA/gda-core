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

import java.io.IOException;

/**
 * An interface for a distributed PEEM Uview Detector class
 */
public interface UView extends Detector {

	/**
	 * Acquire a single image from the detector
	 * 
	 * @return String = image file name
	 * @throws IOException
	 */
	public String shotSingleImage() throws IOException;

	/**
	 * Prepare image file
	 * 
	 * @throws IOException
	 */
	public void prepare() throws IOException;

	/**
	 * @return object
	 * @throws IOException
	 */
	public Object getHashROIs() throws IOException;

	/**
	 * @param nameROI
	 * @return int
	 * @throws IOException
	 */
	public int createROI(String nameROI) throws IOException;

	/**
	 * @param nameROI
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @throws IOException
	 */
	public void setBoundsROI(String nameROI, int x, int y, int width, int height) throws IOException;

	/**
	 * @param nameROI
	 * @return object
	 * @throws IOException
	 */
	public Object getBoundsROI(String nameROI) throws IOException;

	/**
	 * @param nameROI
	 * @return object
	 * @throws IOException
	 */
	public Object readoutROI(String nameROI) throws IOException;

	/**
	 * Connect to the PEEM UView software on remote host.
	 * 
	 * @param host
	 *            The remote host IS is running on.
	 * @throws IOException
	 */
	public void connect(String host) throws IOException;

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
