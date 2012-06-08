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
 * Descriptor providing information about units and limits
 */
public interface IUnitsDescriptor extends IBasicDescriptor {

	/**
	 * Returns display format
	 * 
	 * @return display format
	 */
	public String getDisplayFormat();
	
	/**
	 * Returns unit string
	 * 
	 * @return unit string
	 */
	public String getUnit();

	/**
	 * Returns the maximum limit
	 * 
	 * @return maximum limit
	 * @throws DeviceException 
	 */
	public double getMaximumLimit() throws DeviceException;
	
	/**
	 * Returns the minimum limit
	 * 
	 * @return minimum limit
	 * @throws DeviceException 
	 */
	public double getMinimumLimit() throws DeviceException;
}
