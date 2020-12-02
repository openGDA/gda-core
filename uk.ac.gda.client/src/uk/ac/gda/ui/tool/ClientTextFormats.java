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
	 * Formats
	 */

	private static final DecimalFormat decimalFormat = new DecimalFormat(DEFAULT_DECIMAL_FORMAT);

	private ClientTextFormats() {
	}

	/**
	 * Formats a double using {@link #DEFAULT_DECIMAL_FORMAT} as pattern
	 * @param value
	 *            the number to format
	 * @return the formatted String
	 */

	public static final String formatDecimal(double value) {
		return decimalFormat.format(value);
	}

	/**
	 * Creates a DecimalFormat using the given pattern and the symbols
	 * for the default {@link java.util.Locale.Category#FORMAT FORMAT} locale.
	 * @param pattern
	 *            how format the number.
	 * @param value
	 *            the number to format
	 * @return the formatted String
	 * @see java.text.NumberFormat#getInstance
	 */

	public static final String formatDecimal(String pattern, double value) {
		return new DecimalFormat(pattern).format(value);
	}
}
