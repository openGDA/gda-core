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
 * Interface for monitoring devices. These devices observe some number, but have no control over the underlying device.
 */
public interface Monitor extends Scannable {
	/**
	 * gets the physical unit of the monitor.
	 * 
	 * @return String the physical unit
	 * @throws DeviceException
	 */
	public String getUnit() throws DeviceException;

	/**
	 * returns the number of elements in this monitor.
	 * 
	 * @return int the element count
	 * @throws DeviceException
	 */
	public int getElementCount() throws DeviceException;
}
