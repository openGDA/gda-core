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

package gda.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Formats Strings and numbers (rounding effectively)
 */

public class Formatter {
	/**
	 * Gets a formatted string value appropriate for the units
	 * 
	 * @param value
	 *            value to be formatted
	 * @param units
	 *            to use in formatting the string
	 * @return formatted string
	 */
	public static String getFormattedString(double value, String units) {
		NumberFormat numberFormat = NumberFormat.getInstance();

		if (units.equals(Converter.MDEG)) {
			((DecimalFormat) numberFormat).applyPattern("0.#;-0.#");
		} else if (units.equals("s")) {
			((DecimalFormat) numberFormat).applyPattern("0.000");
		} else {
			((DecimalFormat) numberFormat).applyPattern("0.000000");
		}

		return (numberFormat.format(value));
	}

	/**
	 * Gets a value rounded appropriately to the type of units
	 * 
	 * @param value
	 *            value to be formatted
	 * @param units
	 *            the units to use in formatting the string
	 * @return value extracted from the formatted string
	 */
	public static double getFormattedDouble(double value, String units) {
		NumberFormat numberFormat = NumberFormat.getInstance();

		if (units.equals(Converter.MDEG)) {
			((DecimalFormat) numberFormat).applyPattern("0.#;-0.#");
		} else if (units.equals("s")) {
			((DecimalFormat) numberFormat).applyPattern("0.000");
		} else {
			((DecimalFormat) numberFormat).applyPattern("0.000000");
		}

		if ((new Double(value)).isNaN()) {
			return Double.NaN;
		}
		return (new Double(numberFormat.format(value))).doubleValue();
	}
}
