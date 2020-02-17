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

package uk.ac.gda.tomography.stage.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import gda.device.IScannableMotor;
import gda.factory.Finder;

/**
 * Deseralizes an {@link IScannableMotor}. As the IScannableMotor represents a real EPICS motor, the deserialization is done using the {@link Finder} to
 * retrieve the motor associated with the document "name" element.
 *
 * @author Maurizio Nagni
 */
public class IScannableMotorDeserializer extends StdDeserializer<IScannableMotor> {

	public IScannableMotorDeserializer() {
		this(null);
	}

	public IScannableMotorDeserializer(Class<IScannableMotor> t) {
		super(t);
	}

	@Override
	public IScannableMotor deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		String scannableMotorName = node.get("name").asText();
		return validateMotor(scannableMotorName, node);
	}

	private IScannableMotor validateMotor(String scannableMotorName, JsonNode node) {
		return Finder.getInstance().find(scannableMotorName);
	}
}
