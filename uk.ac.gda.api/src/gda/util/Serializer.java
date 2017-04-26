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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to serialize/de-serialize an object into a byte array. Initially written to pass an object over the CORBA
 * event service. The object must implement the java.io.Serializable interface.
 */
public final class Serializer {
	private static final Logger logger = LoggerFactory.getLogger(Serializer.class);

	private Serializer() {
		// Prevent instances being created
	}

	/**
	 * Serialize the specified object into a byte array.
	 *
	 * @param object
	 *            the object to serialize
	 * @return a byte array containing the serialized object
	 */
	public static byte[] toByte(final Object object) {
		byte[] byteData = null;

		if (object != null) {
			try {
				final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				final ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
				os.flush();
				os.writeObject(object);
				os.flush();
				byteData = byteStream.toByteArray();
				os.close();
			} catch (IOException e) {
				logger.error("Serialization failure: " + e.getMessage(),e);
			}
		} else {
			byteData = new byte[1];
			byteData[0] = 0;
		}
		return byteData;
	}

	/**
	 * De-serialize the given byte array and construct an instance of the object contained therein.
	 *
	 * @param byteData
	 *            a byte array containing the serialized object.
	 * @return the de-serialized object.
	 */
	public static Object toObject(final byte[] byteData) {
		Object object = null;

		if (byteData.length > 1 && byteData[0] != 0) {
			try {
				final ByteArrayInputStream byteStream = new ByteArrayInputStream(byteData);
				final ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
				object = is.readObject();
				is.close();
			} catch (IOException e) {
				logger.error("DeSerialization failure: " + e.getMessage(),e);
			} catch (ClassNotFoundException e) {
				logger.error("Class not found: " + e.getMessage(),e);
			}
		}
		return object;
	}
}
