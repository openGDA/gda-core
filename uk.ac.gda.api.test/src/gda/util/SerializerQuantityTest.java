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

import static javax.measure.unit.NonSI.DECIBEL;
import static javax.measure.unit.NonSI.ELECTRON_VOLT;
import static javax.measure.unit.NonSI.FARADAY;
import static javax.measure.unit.NonSI.MINUTE;
import static javax.measure.unit.SI.HERTZ;
import static javax.measure.unit.SI.JOULE;
import static javax.measure.unit.SI.KILOGRAM;
import static javax.measure.unit.SI.METER;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.RADIAN;
import static javax.measure.unit.SI.SECOND;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for the Serializer class with Quantity objects.
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
		testSerializer(Amount.valueOf(5.82, METER));
	}

	@Test
	public void testSerializeMillimetres() throws ClassNotFoundException, IOException {
		testSerializer(Amount.valueOf(1.0, MILLI(METER)));
	}

	@Test
	public void testSerializeSeconds() throws ClassNotFoundException, IOException {
		testSerializer(Amount.valueOf(45, SECOND));
	}

	@Test
	public void testSerializeRadians() throws ClassNotFoundException, IOException {
		testSerializer(Amount.valueOf(2.74, RADIAN));
	}

	@Test
	public void testSerializeHertz() throws ClassNotFoundException, IOException {
		testSerializer(Amount.valueOf(1.3, HERTZ));
	}

	@Test
	public void testSerializeJoule() throws ClassNotFoundException, IOException {
		testSerializer(Amount.valueOf(0.35, JOULE));
	}

	@Test
	public void testSerializeKilogram() throws ClassNotFoundException, IOException {
		testSerializer(Amount.valueOf(1.9, KILOGRAM));
	}

	//--------------------------------------
	// Test with a selection of non-SI units
	//--------------------------------------
	@Test
	public void testSerializeElectronVolts() throws ClassNotFoundException, IOException {
		testSerializer(Amount.valueOf(3.22, ELECTRON_VOLT));
	}

	@Ignore("JScience4 serialisation/deserialisation does not preserve units")
	@Test
	public void testSerializeDecibels() throws ClassNotFoundException, IOException {
		testSerializer(Amount.valueOf(65, DECIBEL));
	}

	@Test
	public void testSerializeMinutes() throws ClassNotFoundException, IOException {
		testSerializer(Amount.valueOf(3.6, MINUTE));
	}

	@Test
	public void testSerializeFaradays() throws ClassNotFoundException, IOException {
		testSerializer(Amount.valueOf(963, FARADAY));
	}

	private void testSerializer(Amount<? extends Quantity> origObject) throws ClassNotFoundException, IOException {
		final byte[] origBuffer = Serializer.toByte(origObject);
		logger.debug("Object {} serialized ({} bytes)", origObject, origBuffer.length);

		final Object restoredObject = Serializer.toObject(origBuffer);
		logger.debug("Object deserialized into {}", restoredObject);
		assertEquals(origObject, restoredObject);
	}
}
