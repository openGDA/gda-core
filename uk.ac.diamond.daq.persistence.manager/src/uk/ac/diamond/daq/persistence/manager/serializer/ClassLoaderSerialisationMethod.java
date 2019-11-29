/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.persistence.manager.serializer;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import gda.factory.Finder;
import uk.ac.diamond.daq.application.persistence.service.CustomSerialisationMethod;
import uk.ac.diamond.daq.application.persistence.service.PersistenceException;
import uk.ac.diamond.daq.persistence.classloader.PersistenceClassLoader;

/**
 * Use the Marshaling Service to custom serialise Mapping elements in the persistence service
 */
public class ClassLoaderSerialisationMethod implements CustomSerialisationMethod {

	private IMarshallerService marshallingService = PersistenceActivator.getService(IMarshallerService.class);

	private static final String TYPE_INFO_FIELD_NAME = "@type";
	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public ObjectNode serialise(Object value) throws PersistenceException {
		try {
			JsonNode mapped = objectMapper.valueToTree(marshallingService.marshal(value));
			if (mapped.isObject()) {
				((ObjectNode) mapped).set(TYPE_INFO_FIELD_NAME, new TextNode(value.getClass().getCanonicalName()));
				return (ObjectNode) mapped;
			} else {
				ObjectNode toReturn = (ObjectNode) new ObjectMapper().readTree(mapped.asText());
				toReturn.set(TYPE_INFO_FIELD_NAME, new TextNode(value.getClass().getCanonicalName()));
				return toReturn;
			}
		} catch (Exception e) {
			throw new PersistenceException("Unable to serialise item " + value.toString(), e);
		}
	}

	@Override
	public Object deserialise(ObjectNode node) throws PersistenceException {
		try {
			PersistenceClassLoader classLoader = Finder.getInstance().find("classLoaderService");
			Class<?> clazz = classLoader.forName(node.get(TYPE_INFO_FIELD_NAME).textValue());

			return marshallingService.unmarshal(node.toString(), clazz);
		} catch (Exception e) {
			throw new PersistenceException("Unable to convert node " + node.toString(), e);
		}
	}
}
