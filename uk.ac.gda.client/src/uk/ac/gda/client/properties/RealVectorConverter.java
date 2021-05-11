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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.gda.common.exception.GDAException;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * Converts an JSON 1D array of doubles to {@code RealVector}
 *
 * @see ClientSpringProperties
 *
 * @author Maurizio Nagni
 */
public class RealVectorConverter implements Converter<String, RealVector> {

	private static final Logger logger = LoggerFactory.getLogger(RealVectorConverter.class);

	@Override
	public RealVector convert(String source) {
		try {
			return new ArrayRealVector(DocumentMapper.fromJSON(source, double[].class), false);
		} catch (GDAException e) {
			logger.error("Cannot convert {}", source);
		}
		return null;
	}

}
