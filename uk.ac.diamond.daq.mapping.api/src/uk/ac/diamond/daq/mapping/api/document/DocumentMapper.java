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
import java.net.URL;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.api.exception.GDAException;

/**
 * Wraps Jackson {@link ObjectMapper} in order to provide a simple and effective serializartion/deserialization service.
 *
 * <p>
 * For polymorphic classes the strategy used is the following
 * <ul>
 * <li>The base class is annotated with {@link JsonTypeInfo} to define the json property containing the class key, i.e.
 * {@link AcquisitionBase}</li>
 * <li>Any class extending the base class one will
 * <ul>
 * <li>be annotated with {@link JsonTypeName} to assign a key name to the class, i.e.
 * {@link ScanningAcquisition}</li>
 * <li>be registered into the {@code DocumentMapper} (at the moment no dynamic registration is implemented, consequently
 * the registration has to be done updating the class itself)</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 *
 * @author Maurizio Nagni
 */
@Component("documentMapper")
public class DocumentMapper {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.registerSubtypes(new NamedType(ScanningAcquisition.class));
	}

	private DocumentMapper() {
	}

	/**
	 * @return a mapper for mapping.api.document classes
	 */
	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public static final String toJSON(Object value) throws GDAException {
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new GDAException("Cannot create json document", e);
		}
	}

	public static final <T> T fromJSON(String content, Class<T> clazz) throws GDAException {
		try {
			return objectMapper.readValue(content, clazz);
		} catch (JsonProcessingException e) {
			throw new GDAException("Cannot create json document", e);
		} catch (IOException e) {
			throw new GDAException("Cannot read json document", e);
		}
	}

	public static final <T> T fromJSON(URL content, Class<T> clazz) throws GDAException {
		try {
			return objectMapper.readValue(content, clazz);
		} catch (JsonProcessingException e) {
			throw new GDAException("Cannot create json document", e);
		} catch (IOException e) {
			throw new GDAException("Cannot read json document", e);
		}
	}
}