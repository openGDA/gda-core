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

import java.io.IOException;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * Converts an JSON 2D array of doubles to {@code RealMatrix}
 *
 * @see ClientSpringProperties
 *
 * @author Maurizio Nagni
 */
public class RealMatrixConverter implements Converter<String, RealMatrix> {

	private static final Logger logger = LoggerFactory.getLogger(RealMatrixConverter.class);

	@Override
	public RealMatrix convert(String source) {

		try {
			double[][] matrix = convert(source, double[][].class);
			return MatrixUtils.createRealMatrix(matrix);
		} catch (IOException e) {
			logger.error("Cannot convert {}", source);
		}
		return null;
	}

	private <T> T convert(String value, Class<T> valueType) throws IOException {
		var objectMapper = new ObjectMapper();
		return objectMapper.readValue(value, valueType);
	}
}
