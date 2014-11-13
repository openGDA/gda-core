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

package gda.function;

import gda.factory.Configurable;
import gda.factory.Finder;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * To change the template for this generated type comment go to Window - Preferences - Java - Code Generation - Code and
 * Comments
 */
public class InterpolationFunction extends Function implements Configurable {
	private double[] xValues;

	private int numberOfXValues;

	private double[] yValues;

	private boolean configured = false;

	private int xColumn = -1;

	private int yColumn = -1;

	private ColumnDataFile cdf = null;

	private String cdfName;

	private Unit<? extends Quantity> xUnits;

	private Unit<? extends Quantity> yUnits;

	private int yPlaces;

	private static int decimalPlacesToPreventRounding = -1;

	/**
	 * FIXME remove empty constructor
	 */
	public InterpolationFunction() {
	}

	/**
	 * Create an interpolation function for a series of x and y values
	 * 
	 * @param xvalues
	 *            the x values
	 * @param yvalues
	 *            the y values
	 * @param xunits
	 *            the x uinits
	 * @param yunits
	 *            the y units
	 * @param decimal
	 *            number of decimal places . Use InterpolationFunction.decimalPlacesToPreventRounding to prevent
	 *            rounding. Or use other constructor
	 */
	public InterpolationFunction(double[] xvalues, double[] yvalues, Unit<? extends Quantity> xunits,
			Unit<? extends Quantity> yunits, int decimal) {
		// TODO order the arrays in increasing value of x
		numberOfXValues = xvalues.length;
		xValues = xvalues;
		xUnits = xunits;
		yValues = yvalues;
		yUnits = yunits;
		yPlaces = decimal;

		/* Check x values change in the same direction */
		if (!(isAscending()) & !(isDescending())) {
			throw new IllegalArgumentException("InterpolationFunction. xValues must be increasing or decreasing");
		}
	}

	/**
	 * Create an interpolation function for a series of x and y values with no rounding
	 * 
	 * @param xvalues
	 *            the x values
	 * @param yvalues
	 *            the y values
	 * @param xunits
	 *            the x uinits
	 * @param yunits
	 *            the y units
	 */
	public InterpolationFunction(double[] xvalues, double[] yvalues, Unit<? extends Quantity> xunits,
			Unit<? extends Quantity> yunits) {
		this(xvalues, yvalues, xunits, yunits, decimalPlacesToPreventRounding);
	}

	@Override
	public void configure() {
		if (!configured) {
			cdf = (ColumnDataFile) Finder.getInstance().find(cdfName);
			numberOfXValues = cdf.getNumberOfXValues();
			xValues = cdf.getColumn(xColumn);
			xUnits = cdf.getColumnUnits(xColumn);
			yValues = cdf.getColumn(yColumn);
			yUnits = cdf.getColumnUnits(yColumn);
			yPlaces = cdf.getColumnDecimalPlaces(yColumn);
		}
	}

	/**
	 * @return Returns the cdf.
	 */
	public String getCdfName() {
		return cdfName;
	}

	/**
	 * @param cdfName
	 *            The cdf to set.
	 */
	public void setCdfName(String cdfName) {
		this.cdfName = cdfName;
	}

	/**
	 * @return Returns the xColumn.
	 */
	public int getXColumn() {
		return xColumn;
	}

	/**
	 * @param column
	 *            The xColumn to set.
	 */
	public void setXColumn(int column) {
		xColumn = column;
	}

	/**
	 * @return Returns the yColumn.
	 */
	public int getYColumn() {
		return yColumn;
	}

	/**
	 * @param column
	 *            The yColumn to set.
	 */
	public void setYColumn(int column) {
		yColumn = column;
	}

	@Override
	public Quantity evaluate(Quantity xValue) {
		Quantity rtrn = null;
		double x;

		x = xValue.to(xUnits).getAmount();
		if (Double.isNaN(x))
			throw new IllegalArgumentException("Interpolation function does not handle a value that is Nan");

		double y = 0.0;
		int before = -1;
		int after = -1;

		if (xValues != null && yValues != null) {
			// UA 1 - the xValues either ascend or descend but do not change
			// direction

			if (isAscending()) {
				// ascending values in x
				if (x < xValues[0]) {
					// Extrapolate from the first two values
					before = 0;
					after = 1;
				} else if (x > xValues[numberOfXValues - 1]) {
					// Extrapolate from the last two values
					before = numberOfXValues - 2;
					after = numberOfXValues - 1;
				} else {
					// Interpolate from the two surrounding xValues
					for (before = 0, after = 1; before < numberOfXValues; before++, after++) {
						if (x >= xValues[before] && x <= xValues[after]) {
							break;
						}
					}
				}
			} else if (isDescending()) {
				// descending values in x
				if (x > xValues[0]) {
					// Extrapolate from the first two values
					before = 0;
					after = 1;
				} else if (x < xValues[numberOfXValues - 1]) {
					// Extrapolate from the last two values
					before = numberOfXValues - 2;
					after = numberOfXValues - 1;
				} else {
					// Interpolate from the two surrounding xValues
					for (before = 0, after = 1; before < numberOfXValues; before++, after++) {
						if (x <= xValues[before] && x >= xValues[after]) {
							break;
						}
					}
				}
			} else {
				//should never get here, should have been caught by constructor
				throw new IllegalArgumentException("InterpolationFunction. xValues must be increasing or decreasing");
			}
			y = yValues[before] + (x - xValues[before]) * (yValues[after] - yValues[before])
					/ (xValues[after] - xValues[before]);

			// handle case where xValues are the same - a plateau -
			// isInfinite
			// handle case where diff and xValues AND diff in yValues are
			// both zero
			// - Nan
			if (Double.isInfinite(y) || Double.isNaN(y)) {
				y = yValues[before];
			}

			// FIXME: at this point accuracy of y should be reduced to
			// accuracy of
			// yValues[before]

			rtrn = Quantity.valueOf(roundTo(y, yPlaces), yUnits);
		}

		return rtrn;
	}

	private double roundTo(double value, int places) {
		if (places == decimalPlacesToPreventRounding) {
			return value;
		}

		double rtrn = value;
		double factor = Math.pow(10, places);
		rtrn = rtrn * factor;
		rtrn = Math.round(rtrn) / factor;
		return rtrn;
	}
	
	/**
	 * Make sure each point is greater than or equal to next
	 * @return isAscending
	 */
	private boolean isAscending() {
		double previous = xValues[0];
		for (double next : xValues) {
			if (previous > next) {
				return false;	
			}
			previous = next;
		}
		return true;
	}
	/**
	 * Make sure each point is smaller than or equal to next 
	 * @return isDescending
	 **/
	private boolean isDescending() {
		double previous = xValues[0];
		for (double next : xValues) {
			if (previous < next) {
				return false;
			}
			previous = next;
		}
		return true;
	}
}