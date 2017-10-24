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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.SI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for the Serializer class. This does not work if you transfer the file to a remote machine and try to
 * deserialize it, since the Quantities are serialized using their own mechanism.
 *
 * @see gda.util.Serializer
 */
public class SerializerTester {
	private static final Logger logger = LoggerFactory.getLogger(SerializerTester.class);

	/**
	 * Main method.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		byte[] b = new byte[1024];
		Quantity q = Quantity.valueOf(1.0, SI.MILLI(SI.METER));
		try {
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
					"serializedQuantity.dat")));
			b = Serializer.toByte(q);
			out.write(b);
			out.flush();
			out.close();
			logger.debug("Object serialized");

			ObjectInputStream in;
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream("serializedQuantity.dat")));
			in.read(b);
			in.close();
			Object obj = Serializer.toObject(b);
			logger.debug("Object deserialized into " + obj);
		} catch (IOException e) {
			logger.error("Could not serialize and deserialize quantity {}", q, e);
		}
	}
}
