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

package gda.device.scannable.component;

import gda.device.DeviceException;

public interface PositionValidator {

	/**
	 * Returns null if position is valid, otherwise a description of problem. Position is specified in its internal
	 * representation (i.e. with units stripped and offsets and other filters applied).
	 * 
	 * @param internalPosition in its internal representation.
	 * @return null if position is valid
	 * @throws DeviceException 
	 */
	public String checkInternalPosition(Object[] internalPosition) throws DeviceException;
	
}
