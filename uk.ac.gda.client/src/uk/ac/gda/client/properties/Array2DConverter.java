/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.gda.api.exception.GDAException;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * Converts an JSON 2D array of doubles to {@code double[][]}
 *
 * @see ClientSpringProperties
 *
 * @author Maurizio Nagni
 */
public class Array2DConverter implements Converter<String, double[][]> {

	private static final Logger logger = LoggerFactory.getLogger(Array2DConverter.class);

	@Override
	public double[][] convert(String source) {
		try {
			return DocumentMapper.fromJSON(source, double[][].class);
		} catch (GDAException e) {
			logger.error("Cannot convert {}", source);
		}
		return null;
	}

}
