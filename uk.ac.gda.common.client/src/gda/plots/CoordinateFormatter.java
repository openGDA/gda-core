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

package gda.plots;

/**
 * Classes which are to be used for formatting the SimplePlot mouse position must implement this interface.
 */

interface CoordinateFormatter {
	/**
	 * Should produce a string representation of the given SimpleDataCoordinate
	 *
	 * @param sdc
	 *            the SimpleDataCoordinate to be formatted
	 * @return a String containing the formatted values
	 */
	public String formatCoordinates(SimpleDataCoordinate sdc);

	/**
	 * Should set the number of digits used (for both X and Y values).
	 *
	 * @param digits
	 *            the number of digits.
	 */
	public void setDigits(int digits);

	/**
	 * Should set the number of digits used for X value.
	 *
	 * @param digits
	 *            the number of digits.
	 */
	public void setXDigits(int digits);

	/**
	 * Should set the number of digits used for Y value.
	 *
	 * @param digits
	 *            the number of digits.
	 */
	public void setYDigits(int digits);
}
