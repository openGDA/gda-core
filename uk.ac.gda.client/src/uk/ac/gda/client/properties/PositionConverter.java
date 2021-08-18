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

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import uk.ac.gda.client.properties.stage.position.Position;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * Converts a {@code String} to a {@link Position}.
 *
 * @see ClientSpringProperties
 *
 * @author Maurizio Nagni
 */
@Component
public class PositionConverter implements Converter<String, Position> {

	@Override
	public Position convert(String source) {
		return Position.valueOf(source);
	}
}
