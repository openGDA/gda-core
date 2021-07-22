/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

/**
 * A scan object defined implicitly by start, stop and step positions.
 * Note that any or all of these values may not be specified (i.e. are <code>null</code>).
 */
public interface IImplicitScanObject extends IScanObject {

	/**
	 * Set the number of points to move to. This should be used when the scan object
	 * does not initially have enough information to know this, for example if
	 * only start and stop are specified - this can be the case for scannable
	 * s2 in the scan command {@code scan s1 0 5 1 s2 0 10}, where the second scannable
	 * only has start and step defined. This method will be called with to set the size
	 * of this scan object to be the same as that for scannable s1.
	 *
	 * @param numberPoints
	 */
	public void setNumberPoints(int numberPoints);

	public Object getStart();

	public Object getStop();

	public Object getStep();

	public boolean hasStep();

	/**
	 * Calculate the points of the scan. This method must be called before {@link #arePointsValid()}
	 * is called and before the scan starts. The method {@link #setNumberPoints(int)} should be called
	 * before calling this method if necessary (for example if stop isn't set).
	 */
	public void calculateScanPoints();

}
