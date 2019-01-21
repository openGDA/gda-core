/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A class to serialize/de-serialize an object into a byte array. Initially written to pass an object over the CORBA
 * event service. The object must implement the java.io.Serializable interface.
 *
 * @since GDA 9.12 considerable rewrite to simplify logic
 */
public final class Serializer {

	private static final byte[] SERIALIZED_NULL_OBJECT;

	static {
		try {
			SERIALIZED_NULL_OBJECT = toByte(new NullObject());
		} catch (IOException e) {
			throw new RuntimeException("Failed to contruct serialized NullObject", e);
		}
	}

	private Serializer() {
		// Prevent instances being created
	}

	/**
	 * Serialize the specified object into a byte array.
	 *
	 * @param object
	 *            the object to serialize
	 * @return a byte array containing the serialized object
	 * @throws IOException If any error occurs during the serialization
	 */
	public static byte[] toByte(final Serializable object) throws IOException {
		if (object == null) {
			return SERIALIZED_NULL_OBJECT;
		}

		// Try with resources to close the streams
		try (final ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
				final ObjectOutputStream os = new ObjectOutputStream(byteStream);) {
			os.writeObject(object);
			os.flush();
			return byteStream.toByteArray();
		}
	}

	/**
	 * De-serialize the given byte array and construct an instance of the object contained therein.
	 *
	 * @param byteData
	 *            a byte array containing the serialized object.
	 * @return the de-serialized object.
	 * @throws IOException If any error occurs during the de-serialization
	 * @throws ClassNotFoundException Class of a serialized object cannot be found.
	 */
	public static Object toObject(final byte[] byteData) throws ClassNotFoundException, IOException {
		// Try with resources to close the streams
		try (final ByteArrayInputStream byteStream = new ByteArrayInputStream(byteData);
				final ObjectInputStream is = new ObjectInputStream(byteStream);) {
			Object object = is.readObject();
			if (object instanceof NullObject) {
				return null;
			} else {
				return object;
			}
		}
	}

	/** Special class to represent null being sent */
	private static class NullObject implements Serializable {}
}
