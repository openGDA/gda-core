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
 * Interface to control three dimensional (x, y, time) memory devices. Methods are, in general, to be passed start and
 * increment for each dimension.
 */
public interface Memory extends Device {
	/**
	 * Clear the complete memory system.
	 * 
	 * @throws DeviceException
	 */
	public void clear() throws DeviceException;

	/**
	 * Clear the specified number of frames/images/spectra starting with specified frame/image/spectrum number.
	 * 
	 * @param start
	 *            is the starting frame/image/spectrum number (1st = 0)
	 * @param count
	 *            is the number of frames/images/spectra to clear
	 * @throws DeviceException
	 */
	public void clear(int start, int count) throws DeviceException;

	/**
	 * Clear the specified block of memory. The parameters give the ability to clear a 3D block of memory.
	 * 
	 * @param x
	 *            is the start address for the offset in the x dimension
	 * @param y
	 *            is the start address for the offset in the y dimension
	 * @param t
	 *            is the start address for the offset in the z or t dimension
	 * @param dx
	 *            is the address count in the x dimension
	 * @param dy
	 *            is the address count in the y dimension
	 * @param dt
	 *            is the address count in the z or t dimension
	 * @throws DeviceException
	 */
	public void clear(int x, int y, int t, int dx, int dy, int dt) throws DeviceException;

	/**
	 * Starts/enables the memory system.
	 * 
	 * @throws DeviceException
	 */
	public void start() throws DeviceException;

	/**
	 * Stops/disables the memory system.
	 * 
	 * @throws DeviceException
	 */
	public void stop() throws DeviceException;

	/**
	 * Read the specified block of memory. The parameters give the ability to read a 3D block of memory.
	 * 
	 * @param x
	 *            is the start address for the offset in the x dimension
	 * @param y
	 *            is the start address for the offset in the y dimension
	 * @param t
	 *            is the start address for the offset in the z or t dimension
	 * @param dx
	 *            is the address count in the x dimension
	 * @param dy
	 *            is the address count in the y dimension
	 * @param dt
	 *            is the address count in the z or t dimension
	 * @return the data
	 * @throws DeviceException
	 */
	public double[] read(int x, int y, int t, int dx, int dy, int dt) throws DeviceException;

	/**
	 * Read the specified frame/image/spectrum.
	 * 
	 * @param frame
	 *            is the frame/image/spectrum number (1st = 0)
	 * @return the data
	 * @throws DeviceException
	 */
	public double[] read(int frame) throws DeviceException;

	/**
	 * Set the size of the memory system.
	 * 
	 * @param d
	 *            is the
	 * @throws DeviceException
	 */
	public void setDimension(int[] d) throws DeviceException;

	/**
	 * Get the size of the memory system.
	 * 
	 * @return the memory dimension in x and y
	 * @throws DeviceException
	 */
	public int[] getDimension() throws DeviceException;

	/**
	 * Write to the specified block of memory. The parameters give the ability to write a 3D block of memory.
	 * 
	 * @param data
	 *            is the data to write to the memory
	 * @param x
	 *            is the start address for the offset in the x dimension
	 * @param y
	 *            is the start address for the offset in the y dimension
	 * @param t
	 *            is the start address for the offset in the z or t dimension
	 * @param dx
	 *            is the address count in the x dimension
	 * @param dy
	 *            is the address count in the y dimension
	 * @param dt
	 *            is the address count in the z or t dimension
	 * @throws DeviceException
	 */
	public void write(double[] data, int x, int y, int t, int dx, int dy, int dt) throws DeviceException;

	/**
	 * Write to the specified frame of memory.
	 * 
	 * @param data
	 *            is the data to write to the memory
	 * @param frame
	 *            the area in memory corresponding to the specified frame.
	 * @throws DeviceException
	 */
	public void write(double[] data, int frame) throws DeviceException;

	/**
	 * Output data from memory directly to file
	 * 
	 * @param file
	 *            is the fully qualified file name
	 * @throws DeviceException
	 */
	public void output(String file) throws DeviceException;

	/**
	 * Get the physical memory size
	 * 
	 * @return the physical memory size expressed in words.
	 * @throws DeviceException
	 */
	public int getMemorySize() throws DeviceException;

	/**
	 * If the detector supports multiple pixel settings, this will return an array of possible values. Null otherwise.
	 * Assumes the detector is 2D with identical axes.
	 * 
	 * @return list of supported resolutions
	 * @throws DeviceException
	 */
	public int[] getSupportedDimensions() throws DeviceException;
}
