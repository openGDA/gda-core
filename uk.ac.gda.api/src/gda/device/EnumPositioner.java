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
 * Interface for devices which have an enumerated set of positions, for example a valve which can be open or closed.
 */

public interface EnumPositioner extends Scannable {
	/** Possible status value, indicates device is idle. */
	public static final int IDLE = 0;

	/** Possible status value, indicates device is moving. */
	public static final int MOVING = IDLE + 1;

	/** Possible status value, indicates device has an error. */
	public static final int ERROR = MOVING + 1;

	/**
	 * Returns an array of the positions which this device can be moved to.
	 * 
	 * @return an array of positions
	 * @throws DeviceException
	 */
	public String[] getPositions() throws DeviceException;

	/**
	 * Returns the current status
	 * 
	 * @return the status
	 * @throws DeviceException
	 */
	public EnumPositionerStatus getStatus() throws DeviceException;

	/**
	 * Stops the current movement.
	 * 
	 * @throws DeviceException
	 */
	@Override
	public void stop() throws DeviceException;

}
