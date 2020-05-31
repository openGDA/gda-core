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

package uk.ac.diamond.daq.mapping.api.document;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.gda.api.exception.GDAException;

/**
 * Maps subclasses types to their type. This mapping is registered into an {@link ObjectMapper} so that serialisation
 * and deserialisation are not ambiguous.
 *
 * @author Maurizio Nagni
 */
@Component("documentMapper")
public class DocumentMapper {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private DocumentMapper() {
	}

	/**
	 * @return a mapper for mapping.api.document classes
	 */
	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public final String toJSON(Object value) throws GDAException {
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new GDAException("Cannot create json document", e);
		}
	}

	public final <T> T toJSON(String content, Class<T> clazz) throws GDAException {
		try {
			return objectMapper.readValue(content, clazz);
		} catch (JsonProcessingException e) {
			throw new GDAException("Cannot create json document", e);
		} catch (IOException e) {
			throw new GDAException("Cannot read json document", e);
		}
	}
}
