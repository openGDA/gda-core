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

import java.util.List;

/**
 * Interface for devices which have an enumerated set of positions, for example a valve which can be open or closed.
 */

public interface EnumPositioner extends Scannable {

	/**
	 * Returns an array of the positions which this device can be moved to.
	 *
	 * @return an array of positions
	 * @throws DeviceException
	 */
	public String[] getPositions() throws DeviceException;

	/**
	 * Returns a list of the positions which this device can be moved to.
	 *
	 * @return a list of positions
	 * @throws DeviceException
	 */
	public List<String> getPositionsList() throws DeviceException;

	/**
	 * Returns the current status
	 *
	 * @return the status
	 * @throws DeviceException
	 */
	public EnumPositionerStatus getStatus() throws DeviceException;

	/**
	 * Reports whether the positioner is in the most recent demand position
	 *
	 * @throws DeviceException
	 */
	public boolean isInPos() throws DeviceException;

}
