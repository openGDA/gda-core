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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import gda.device.IScannableMotor;

public class IScannableMotorSerializer extends StdSerializer<IScannableMotor> {

	private static final Logger logger = LoggerFactory.getLogger(IScannableMotorSerializer.class);

	public IScannableMotorSerializer() {
		this(null);
	}

	public IScannableMotorSerializer(Class<IScannableMotor> t) {
		super(t);
	}

	@Override
	public void serialize(IScannableMotor value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("name", value.getName());
		//convertEpicsMotor(value.getMotor(), jgen);
		jgen.writeEndObject();
	}

//	private void convertEpicsMotor(Motor motor, JsonGenerator jgen) {
//		if (EpicsMotor.class.isInstance(motor)) {
//			try {
//				jgen.writeStringField("pvName", EpicsMotor.class.cast(motor).getPvName());
//			} catch (IOException e) {
//				logger.error("Cannot serialize Epics motor", e);
//			}
//		}
//	}

}
