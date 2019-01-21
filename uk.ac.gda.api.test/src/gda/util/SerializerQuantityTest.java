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

import static org.jscience.physics.units.NonSI.DECIBEL;
import static org.jscience.physics.units.NonSI.ELECTRON_VOLT;
import static org.jscience.physics.units.NonSI.FARADAY;
import static org.jscience.physics.units.NonSI.MINUTE;
import static org.jscience.physics.units.SI.HERTZ;
import static org.jscience.physics.units.SI.JOULE;
import static org.jscience.physics.units.SI.KILOGRAM;
import static org.jscience.physics.units.SI.METER;
import static org.jscience.physics.units.SI.MILLI;
import static org.jscience.physics.units.SI.RADIAN;
import static org.jscience.physics.units.SI.SECOND;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jscience.physics.quantities.Quantity;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for the Serializer class with Quantity objects.
 * <p>
 * There is a bug in jscience 2 which causes deserialisation to fail for some units.<br>
 * This is fixed in jscience 4
 *
 * @see gda.util.Serializer
 */
public class SerializerQuantityTest {
	private static final Logger logger = LoggerFactory.getLogger(SerializerQuantityTest.class);

	//----------------------------------
	// Test with a selection of SI units
	//----------------------------------
	@Test
	public void testSerializeMetres() throws ClassNotFoundException, IOException {
		testSerializer(Quantity.valueOf(5.82, METER));
	}

	@Test
	public void testSerializeMillimetres() throws ClassNotFoundException, IOException {
		testSerializer(Quantity.valueOf(1.0, MILLI(METER)));
	}

	@Test
	public void testSerializeSeconds() throws ClassNotFoundException, IOException {
		testSerializer(Quantity.valueOf(45, SECOND));
	}

	@Test
	@Ignore("Fails with NPE with JScience2 needs JScience4: see DAQ-1840")
	public void testSerializeRadians() throws ClassNotFoundException, IOException {
		testSerializer(Quantity.valueOf(2.74, RADIAN));
	}

	@Test
	@Ignore("Fails with NPE with JScience2 needs JScience4: see DAQ-1840")
	public void testSerializeHertz() throws ClassNotFoundException, IOException {
		testSerializer(Quantity.valueOf(1.3, HERTZ));
	}

	@Test
	@Ignore("Fails with NPE with JScience2 needs JScience4: see DAQ-1840")
	public void testSerializeJoule() throws ClassNotFoundException, IOException {
		testSerializer(Quantity.valueOf(0.35, JOULE));
	}

	@Test
	public void testSerializeKilogram() throws ClassNotFoundException, IOException {
		testSerializer(Quantity.valueOf(1.9, KILOGRAM));
	}

	//--------------------------------------
	// Test with a selection of non-SI units
	//--------------------------------------
	@Test
	@Ignore("Fails with NPE with JScience2 needs JScience4: see DAQ-1840")
	public void testSerializeElectronVolts() throws ClassNotFoundException, IOException {
		testSerializer(Quantity.valueOf(3.22, ELECTRON_VOLT));
	}

	@Test
	public void testSerializeDecibels() throws ClassNotFoundException, IOException {
		testSerializer(Quantity.valueOf(65, DECIBEL));
	}

	@Test
	public void testSerializeMinutes() throws ClassNotFoundException, IOException {
		testSerializer(Quantity.valueOf(3.6, MINUTE));
	}

	@Test
	@Ignore("Fails with NPE with JScience2 needs JScience4: see DAQ-1840")
	public void testSerializeFaradays() throws ClassNotFoundException, IOException {
		testSerializer(Quantity.valueOf(963, FARADAY));
	}

	private void testSerializer(Quantity origObject) throws ClassNotFoundException, IOException {
		final byte[] origBuffer = Serializer.toByte(origObject);
		logger.debug("Object {} serialized ({} bytes)", origObject, origBuffer.length);

		final Object restoredObject = Serializer.toObject(origBuffer);
		logger.debug("Object deserialized into {}", restoredObject);
		assertEquals(origObject, restoredObject);
	}
}
