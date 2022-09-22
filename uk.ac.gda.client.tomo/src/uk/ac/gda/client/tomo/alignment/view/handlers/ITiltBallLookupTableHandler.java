/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers;

import gda.device.DeviceException;


/**
 * Lookup table handler for looking up values for conducting the Tilt alignment. For i12 the lookup table is placed in
 * "{SOFTWARE_LOC}/i12-config/lookupTables/tomo/tilt_ball_roi_table.txt"
 * 
 * @author rsr31645 - Ravi Somayaji
 */
public interface ITiltBallLookupTableHandler {

	/**
	 * @param module
	 * @return the tx motor offset for a given module
	 * @throws DeviceException 
	 */
	public abstract double getTxOffset(Integer module) throws DeviceException;

	/**
	 * @param module
	 * @return the minY that should be set on the detector for a given module.
	 * @throws DeviceException 
	 */
	public abstract int getMinY(Integer module) throws DeviceException;

	/**
	 * @param module
	 * @return the maxY that should be set on the detector for a given module.
	 * @throws DeviceException 
	 */
	public abstract int getMaxY(Integer module) throws DeviceException;

	/**
	 * @param value
	 * @return minX
	 * @throws DeviceException 
	 */
	public abstract int getMinX(Integer value) throws DeviceException;

	/**
	 * @param value
	 * @return maxX
	 * @throws DeviceException 
	 */
	public abstract int getMaxX(Integer value) throws DeviceException;

}