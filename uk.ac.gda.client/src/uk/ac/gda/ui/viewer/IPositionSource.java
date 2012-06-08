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

package uk.ac.gda.ui.viewer;

import gda.device.DeviceException;

/**
 * An interface to an object that gets and sets a position
 */
public interface IPositionSource {

	/**
	 * Returns the units descriptor of the position source. 
	 * 
	 * @return the units descriptor of the position source
	 */
	public IUnitsDescriptor getDescriptor();

	/**
	 * Returns true if the position source is busy, for example,
	 * it is in the middle of changing position
	 * 
	 * @return true if busy, otherwise false
	 * 
	 * @throws DeviceException
	 */
	public boolean isBusy() throws DeviceException;

	/**
	 * Returns the current position
	 * 
	 * @return double current position
	 * @throws DeviceException
	 */
	public double getPosition() throws DeviceException;

	/**
	 * Set the position
	 * 
	 * @param value new position
	 * @throws DeviceException
	 */
	public void setPosition(double value) throws DeviceException;

}
