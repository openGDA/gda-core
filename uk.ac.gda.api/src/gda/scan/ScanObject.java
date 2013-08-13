/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.scan;

import gda.device.DeviceException;
import gda.device.Scannable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Structures to hold information about each object that will be scanned.
 */
public abstract class ScanObject {
	protected static final Logger logger = LoggerFactory.getLogger(ScanObject.class);

	/**
	 * The scannable this object operates.
	 */
	public Scannable scannable;

	/**
	 * @return true if the object has a defined start position
	 */
	abstract boolean hasStart();

	/**
	 * @return true if the object has a defined stop position (i.e. defines an entire scan)
	 */
	abstract boolean hasStop();

	/**
	 * Move the scannable to the start position
	 * 
	 * @throws Exception
	 */
	abstract IScanStepId moveToStart() throws Exception;

	/**
	 * Move the scannable to its next position
	 * 
	 * @throws Exception
	 */
	abstract IScanStepId moveStep() throws Exception;

	/**
	 * The number of points this object can move its scannable to.
	 * 
	 * @return the size of the array of points
	 */
	abstract int getNumberPoints();

	/**
	 * Set the size of the array of points to use.
	 * 
	 * @param numberPoints
	 */
	abstract void setNumberPoints(int numberPoints);

	/**
	 * @return null if all points in the array of points are valid for the scannable or the toString() of the point
	 *         which is unacceptable
	 * @throws DeviceException
	 */
	abstract String arePointsValid() throws DeviceException;
	
//	public abstract Vector<Object> getPoints();

}
