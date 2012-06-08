/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.scannable;

/**
 * Preferred interface that objects returned from a monitor or scannable getPosition method may implement 
 */
public interface ScannableGetPosition {

	/**
	 * @return Number of items within the position. This should match the sum of InputNames and extraNames
	 */
	public int getElementCount();

	/**
	 * @return String representation of the position to go into ScanDataPoint. The length should match
	 * the value returned by getElementCount
	 */
	public String[] getStringFormattedValues();
}
