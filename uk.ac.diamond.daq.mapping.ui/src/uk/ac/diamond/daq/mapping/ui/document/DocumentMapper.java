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

package uk.ac.diamond.daq.mapping.ui.document;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

import uk.ac.diamond.daq.mapping.ui.document.scanpath.AxialStepModelDocument;
import uk.ac.diamond.daq.mapping.ui.document.scanpath.TwoAxisGridPointsModelDocument;

/**
 * Maps subclasses types to their type. This mapping is registered into an {@link ObjectMapper} so that serialisation
 * and deserialisation are not ambiguous.
 *
 * @author Maurizio Nagni
 */
@Component("documetMapper")
public class DocumentMapper {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.registerSubtypes(new NamedType(AxialStepModelDocument.class, "AxialStep"));
		objectMapper.registerSubtypes(new NamedType(TwoAxisGridPointsModelDocument.class, "TwoAxisGridPoints"));
	}

	private DocumentMapper() {
	}

	/**
	 * @return a mapper for mapping.api.document classes
	 */
	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
