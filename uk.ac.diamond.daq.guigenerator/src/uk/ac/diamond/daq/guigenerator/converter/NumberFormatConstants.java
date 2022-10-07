/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.guigenerator.converter;

import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.NumberFormat;

/**
 * This class exists as a common place to put the two {@link NumberFormat} objects that
 * are used by both the {@link DoubleToStringConverter} and the {@link StringToDoubleConverter}.
 */
final class NumberFormatConstants {

	public static final NumberFormat STANDARD_FORMAT = NumberFormat.getNumberInstance();

	public static final NumberFormat SCIENTIFIC_FORMAT = new DecimalFormat("0.###E0");

	private NumberFormatConstants() {
		// private constructor to prevent instantiation
	}

}
