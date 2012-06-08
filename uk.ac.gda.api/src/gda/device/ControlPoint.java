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
 * Interface to control a Control Point
 */
public interface ControlPoint extends Scannable {
	/**
	 * Returns the latest value observed by this Control Point.
	 * 
	 * @return the latest value of the monitor
	 * @throws DeviceException
	 */
	public double getValue() throws DeviceException;

	/**
	 * Set the target Control Point
	 * 
	 * @param target
	 *            the target control point value
	 * @throws DeviceException
	 */

	public void setValue(double target) throws DeviceException;

}
