/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool;

import java.text.DecimalFormat;

/**
 * Collection of various text formats either predefined, to standardise widget layout, or more customisable for special
 * cases
 *
 * @author Maurizio Nagni
 */

public class ClientTextFormats {

	/**
	 * Three decimal text format pattern
	 */
	public static final String DEFAULT_DECIMAL_FORMAT = "#0.000";

	/**
	 * Integer text format pattern
	 */
	public static final String DEFAULT_INTEGER_FORMAT = "#";

	/**
	 * Formats
	 */
	public static final DecimalFormat decimalFormat = new DecimalFormat(DEFAULT_DECIMAL_FORMAT);
	public static final DecimalFormat integerFormat = new DecimalFormat(DEFAULT_INTEGER_FORMAT);

	private ClientTextFormats() {
	}

	/**
	 * Formats a double using {@link #DEFAULT_DECIMAL_FORMAT} as pattern
	 * @param value
	 *            the number to format
	 * @return the formatted {@code double} as String
	 */
	public static final String formatDecimal(double value) {
		return decimalFormat.format(value);
	}

	/**
	 * Formats a integer using {@link #DEFAULT_DECIMAL_FORMAT} as pattern
	 * @param value
	 *            the number to format
	 * @return the formatted {@code integer} as String
	 */
	public static final String formatDecimal(int value) {
		return integerFormat.format(value);
	}

	/**
	 * Formats a {@code double} using a given pattern.
	 * @param pattern
	 *            how format the number.
	 * @param value
	 *            the number to format
	 * @return the formatted {@code double} as String
	 * @see java.text.NumberFormat#getInstance
	 */
	public static final String formatDecimal(String pattern, double value) {
		return new DecimalFormat(pattern).format(value);
	}
}
