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
 * Interface to the Queensgate Piezo controller. Most of the methods are specific to the way a Queensgate works and so
 * perhaps should not be in a general interface. But all will be necessary to be sure it is working correctly.
 */
public interface Piezo extends Device {
	/**
	 * Set the (digital) offset of the controller
	 * 
	 * @param offset
	 *            the offset ( -8192 < offset < 8191 )
	 * @throws DeviceException
	 */
	public void setOffset(int offset) throws DeviceException;

	/**
	 * Change the (digital) offset of the controller
	 * 
	 * @param change
	 *            the amount to change by
	 * @throws DeviceException
	 */
	public void changeOffset(int change) throws DeviceException;

	/**
	 * Get the current offset of the controller
	 * 
	 * @return the current offset
	 * @throws DeviceException
	 */
	public int getOffset() throws DeviceException;

	/**
	 * Get the current position of the controller
	 * 
	 * @return the position (-2048 < position < 2047)
	 * @throws DeviceException
	 */
	public int getPosition() throws DeviceException;

	/**
	 * Get the current status of the controller
	 * 
	 * @return the status (0 == good, 1 == bad)
	 * @throws DeviceException
	 */
	public int getStatus() throws DeviceException;

	/**
	 * Get what the current front panel voltage ought to be
	 * 
	 * @return the front panel voltage
	 * @throws DeviceException
	 */
	public double getFPV() throws DeviceException;

}
