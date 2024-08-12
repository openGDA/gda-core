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
 * An interface for devices capable of having characters read from them.
 */
public interface CharReadableDev {
	/**
	 * Reads a character from the device.
	 * 
	 * @return the character read
	 * @throws DeviceException
	 */
	public char readChar() throws DeviceException;

	/**
	 * Flushes any unread characters from the device.
	 * 
	 * @throws DeviceException
	 */
	public void flush() throws DeviceException;

	/**
	 * Sets the maximum length of time to wait for a character to become available to read.
	 * 
	 * @param time
	 *            the length of time in milliseconds.
	 * @throws DeviceException
	 */
	public void setReadTimeout(int time) throws DeviceException;
}
