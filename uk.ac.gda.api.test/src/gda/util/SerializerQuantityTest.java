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

import static org.junit.Assert.assertEquals;
import static si.uom.NonSI.ANGSTROM;
import static si.uom.NonSI.ELECTRON_VOLT;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.Units.HERTZ;
import static tec.units.indriya.unit.Units.JOULE;
import static tec.units.indriya.unit.Units.KILOGRAM;
import static tec.units.indriya.unit.Units.METRE;
import static tec.units.indriya.unit.Units.MINUTE;
import static tec.units.indriya.unit.Units.RADIAN;
import static tec.units.indriya.unit.Units.SECOND;

import java.io.IOException;
import java.io.Serializable;

import javax.measure.Quantity;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.units.indriya.quantity.Quantities;

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
		testSerializer(Quantities.getQuantity(5.82, METRE));
	}

	@Test
	public void testSerializeMillimetres() throws ClassNotFoundException, IOException {
		testSerializer(Quantities.getQuantity(1.0, MILLI(METRE)));
	}

	@Test
	public void testSerializeSeconds() throws ClassNotFoundException, IOException {
		testSerializer(Quantities.getQuantity(45, SECOND));
	}

	@Test
	public void testSerializeRadians() throws ClassNotFoundException, IOException {
		testSerializer(Quantities.getQuantity(2.74, RADIAN));
	}

	@Test
	public void testSerializeHertz() throws ClassNotFoundException, IOException {
		testSerializer(Quantities.getQuantity(1.3, HERTZ));
	}

	@Test
	public void testSerializeJoule() throws ClassNotFoundException, IOException {
		testSerializer(Quantities.getQuantity(0.35, JOULE));
	}

	@Test
	public void testSerializeKilogram() throws ClassNotFoundException, IOException {
		testSerializer(Quantities.getQuantity(1.9, KILOGRAM));
	}

	//--------------------------------------
	// Test with a selection of non-SI units
	//--------------------------------------
	@Test
	public void testSerializeElectronVolts() throws ClassNotFoundException, IOException {
		testSerializer(Quantities.getQuantity(3.22, ELECTRON_VOLT));
	}

	@Test
	public void testSerializeMinutes() throws ClassNotFoundException, IOException {
		testSerializer(Quantities.getQuantity(3.6, MINUTE));
	}

	@Test
	public void testSerializeAngstroms() throws ClassNotFoundException, IOException {
		testSerializer(Quantities.getQuantity(963, ANGSTROM));
	}

	private void testSerializer(Quantity<? extends Quantity<?>> origObject) throws ClassNotFoundException, IOException {
		final byte[] origBuffer = Serializer.toByte((Serializable) origObject);
		logger.debug("Object {} serialized ({} bytes)", origObject, origBuffer.length);

		final Object restoredObject = Serializer.toObject(origBuffer);
		logger.debug("Object deserialized into {}", restoredObject);
		assertEquals(origObject, restoredObject);
	}
}
