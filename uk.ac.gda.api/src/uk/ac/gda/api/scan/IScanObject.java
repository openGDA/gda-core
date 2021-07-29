/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.api.scan;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.scan.IScanStepId;
import gda.scan.ScanPositionProvider;

/**
 * Interface for a ScanObject to reduce the incentive to split the gda.scan package across plugins just so you can get at the scannable member The ScanObject
 * abstract class this replaces has been moved back to the main gda.scan package in uk.ac.gda.core.
 *
 * @author Keith Ralphs
 */
public interface IScanObject {

	/**
	 * The type of scan object.
	 */
	public enum ScanObjectType {

		/**
		 * An implicit scan object, defined by start, stop and step.
		 */
		IMPLICIT,

		/**
		 * An explicit scan object, which explicitly knows each point in the scan,
		 * via a {@link ScanPositionProvider}.
		 */
		EXPLICIT
	}

	ScanObjectType getType();

	/**
	 * The {@link Scannable} that this {@link IScanObject} moves.
	 * @return the scananble
	 */
	Scannable getScannable();

	/**
	 * @return true if the object has a defined start position
	 */
	boolean hasStart();

	/**
	 * @return true if the object has a defined stop position (i.e. defines an entire scan)
	 */
	boolean hasStop();

	/**
	 * Move the scannable to the start position
	 *
	 * @throws Exception
	 */
	IScanStepId moveToStart() throws Exception;

	/**
	 * Move the scannable to its next position
	 *
	 * @throws Exception
	 */
	IScanStepId moveStep() throws Exception;

	/**
	 * The number of points this object can move its scannable to.
	 *
	 * @return the size of the array of points
	 */
	int getNumberPoints();

	/**
	 * Checks whether the points in this scan object are valid. Note that for {@link IImplicitScanObject}s,
	 * {@link IImplicitScanObject#calculateScanPoints()} must have been called first.
	 * @return <code>null</code> if all points in the array of points are valid for the scannable, or the toString() of the point which is unacceptable
	 * @throws DeviceException
	 */
	String arePointsValid() throws DeviceException;

}
