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

package gda.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.junit.Test;

public class SerializerTest {

	@Test
	public void testSerializeAndDeserializeNull() throws IOException, ClassNotFoundException {
		byte[] bytes = Serializer.toByte(null);
		Object deserializedObject = Serializer.toObject(bytes);
		assertThat(deserializedObject, is(nullValue()));
	}

	@Test
	public void testSerializeAndDeserializeDouble() throws IOException, ClassNotFoundException {
		byte[] bytes = Serializer.toByte(234.54); // Note will be boxed
		Object deserializedObject = Serializer.toObject(bytes);
		assertThat(deserializedObject, is(equalTo(234.54)));
	}

}
