/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.function;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class LinearFunctionSerializer extends StdSerializer<ILinearFunction> {

	public LinearFunctionSerializer() {
		this(null);
	}

	protected LinearFunctionSerializer(Class<ILinearFunction> t) {
		super(t);
	}

	@Override
	public void serialize(ILinearFunction value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("interception", value.getInterception());
		gen.writeStringField("slopeDividend", value.getSlopeDividend());
		gen.writeStringField("slopeDivisor", value.getSlopeDivisor());
		gen.writeEndObject();
	}
}
