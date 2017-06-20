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

import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.StringToNumberConverter;

/**
 * An {@link IConverter} to convert from a {@link String} to a {@link Double}.
 * If the string contains the letter {@code e}, this string is assumed to formatted in scientific format,
 * e.g. {@code 1.23e-5}. Otherwise, the string is assume to be in standard decimal format.
 *
 */
public class StringToDoubleConverter extends Converter {

	private final StringToNumberConverter STANDARD_CONVERTER = StringToNumberConverter.toDouble(STANDARD_FORMAT, false);
	private final StringToNumberConverter SCIENTIFIC_CONVERTER = StringToNumberConverter.toDouble(SCIENTIFIC_FORMAT, false);

	public StringToDoubleConverter() {
		super(String.class, Double.class);
	}

	@Override
	public Object convert(Object fromObject) {
		final String strVal = (String) fromObject;
		return getConverterToUse(strVal).convert(strVal);
	}

	private IConverter getConverterToUse(String strVal) {
		if (strVal.contains("E") || strVal.contains("e")) {
			return SCIENTIFIC_CONVERTER;
		}

		return STANDARD_CONVERTER;
	}

}