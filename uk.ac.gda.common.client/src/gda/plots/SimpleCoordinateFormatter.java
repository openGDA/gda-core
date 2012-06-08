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

import java.text.NumberFormat;

/**
 * Default implementation of CoordinateFormatter, used by SimplePlot to display mouse position unless a different one is
 * specified.
 */
public class SimpleCoordinateFormatter implements CoordinateFormatter {
	private NumberFormat xNumberFormat = NumberFormat.getNumberInstance();

	private NumberFormat yNumberFormat = NumberFormat.getNumberInstance();

	/**
	 * Constructor.
	 */
	public SimpleCoordinateFormatter() {
		xNumberFormat.setMaximumFractionDigits(2);
		xNumberFormat.setMinimumFractionDigits(2);
		yNumberFormat.setMaximumFractionDigits(2);
		yNumberFormat.setMinimumFractionDigits(2);
	}

	/**
	 * Formats the given coordinates into a String of the form "mouse position x y".
	 * 
	 * @param sdc
	 *            SimpleDataCoordinate object containing x and y
	 * @return the String to represent them
	 */
	@Override
	public String formatCoordinates(SimpleDataCoordinate sdc) {
		return "mouse position " + xNumberFormat.format(sdc.getX()) + " " + yNumberFormat.format(sdc.getY());
	}

	/**
	 * @return Returns the numberFormat used for X values.
	 */
	public NumberFormat getXNumberFormat() {
		return xNumberFormat;
	}

	/**
	 * @return Returns the numberFormat used for Y values.
	 */
	public NumberFormat getYNumberFormat() {
		return yNumberFormat;
	}

	/**
	 * Sets the NumberFormat used for X values.
	 * 
	 * @param numberFormat
	 *            The numberFormat to set.
	 */
	public void setXNumberFormat(NumberFormat numberFormat) {
		this.xNumberFormat = numberFormat;
	}

	/**
	 * Sets the NumberFormat used for Y values.
	 * 
	 * @param numberFormat
	 *            The numberFormat to set.
	 */
	public void setYNumberFormat(NumberFormat numberFormat) {
		this.yNumberFormat = numberFormat;
	}

	/**
	 * Sets both X and Y NumberFormat to the given format.
	 * 
	 * @param numberFormat
	 *            The numberFormat to set.
	 */
	public void setNumberFormat(NumberFormat numberFormat) {
		setXNumberFormat(numberFormat);
		setYNumberFormat(numberFormat);
	}

	/**
	 * Sets the number of digits to be used for X values.
	 * 
	 * @param digits
	 *            the number of digits to use.
	 */
	@Override
	public void setXDigits(int digits) {
		xNumberFormat.setMaximumFractionDigits(digits);
		xNumberFormat.setMinimumFractionDigits(digits);
	}

	/**
	 * Sets the number of digits to be used for Y values.
	 * 
	 * @param digits
	 *            the number of digits to use.
	 */
	@Override
	public void setYDigits(int digits) {
		yNumberFormat.setMaximumFractionDigits(digits);
		yNumberFormat.setMinimumFractionDigits(digits);
	}

	/**
	 * Sets the number of digits to be used for both X and Y values.
	 * 
	 * @param digits
	 *            the number of digits to use.
	 */
	@Override
	public void setDigits(int digits) {
		setXDigits(digits);
		setYDigits(digits);
	}
}
