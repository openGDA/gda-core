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

import static uk.ac.diamond.daq.guigenerator.converter.NumberFormatConstants.SCIENTIFIC_FORMAT;
import static uk.ac.diamond.daq.guigenerator.converter.NumberFormatConstants.STANDARD_FORMAT;

import java.text.NumberFormat;

import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.NumberToStringConverter;

/**
 * An {@link IConverter} to convert a {@link Double} to a {@link String}.
 * If the magnitude of the value to convert is non-zero but smaller than
 * 10<sup>-3</sup> or larger than or equal to 10<sup>7</sup> then it is
 * represented in scientific notation, e.g. 0.123E-5. Otherwise
 * it is formatted according to the {@link NumberFormat} object returned
 * by {@link NumberFormat#getNumberInstance()}. Typically this is the integer part,
 * with no leading zeroes, followed by '{code .}', followed by up to three decimal
 * digits representing the fractional part.
 */
public class DoubleToStringConverter extends Converter {

	private static final double TEN_TO_POWER_SEVEN = Math.pow(10.0, 7.0);
	private static final double TEN_TO_POWER_MINUS_THREE = Math.pow(10.0, -3.0);

	private static final NumberToStringConverter STANDARD_CONVERTER = NumberToStringConverter.fromDouble(STANDARD_FORMAT, false);
	private static final NumberToStringConverter SCIENTIFIC_CONVERTER = NumberToStringConverter.fromDouble(SCIENTIFIC_FORMAT, false);

	public DoubleToStringConverter() {
		super(Double.class, String.class);
	}

	@Override
	public Object convert(Object fromObject) {
		if (fromObject == null) {
			return "";
		}

		Double doubleObj = (Double) fromObject;
		IConverter converter = getConverterToUse(doubleObj);

		return converter.convert(fromObject);
	}

	private IConverter getConverterToUse(Double doubleObj) {
		double absValue = Math.abs(doubleObj.doubleValue());
		if (absValue != 0.0 && (absValue < TEN_TO_POWER_MINUS_THREE || absValue >= TEN_TO_POWER_SEVEN)) {
			return SCIENTIFIC_CONVERTER;
		}

		return STANDARD_CONVERTER;
	}

}