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

package uk.ac.gda.tomography.base.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import uk.ac.gda.tomography.model.DevicePosition;

/**
 * Deserialises a {@link DevicePosition}
 *
 * @author Maurizio Nagni
 */
public class DevicePositionDeserializer extends StdDeserializer<DevicePosition<? extends Number>> {

	public DevicePositionDeserializer() {
		this(null);
	}

	public DevicePositionDeserializer(Class<DevicePosition<? extends Number>> t) {
		super(t);
	}

	@Override
	public DevicePosition<? extends Number> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
        String name = node.get("name").asText();
        Double value = node.get("value").asDouble();
        return new DevicePosition<Number>(name, value);
	}
}
