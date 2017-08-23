/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.scannable;

import static org.jscience.physics.units.SI.METER;
import static org.jscience.physics.units.SI.MILLI;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.junit.Test;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;

public class PositionConvertorFunctionsTest {


	@Test
	public void testToObjectArrayWithSingleObjects() {
		assertArrayEquals(new Object[] { 1 }, PositionConvertorFunctions.toObjectArray(1));
		assertArrayEquals(new Object[] { 1. }, PositionConvertorFunctions.toObjectArray(1.));
		assertArrayEquals(new Object[] { "abc" }, PositionConvertorFunctions.toObjectArray("abc"));
	}

	@Test
	public void testToObjectArrayWithSinglePyObjects() {
		assertArrayEquals(new Object[] { new PyInteger(1) }, PositionConvertorFunctions
				.toObjectArray(new PyInteger(1)));
		assertArrayEquals(new Object[] { new PyFloat(1.) }, PositionConvertorFunctions
				.toObjectArray(new PyFloat(1.)));
		assertArrayEquals(new Object[] { new PyString("abc") }, PositionConvertorFunctions
				.toObjectArray(new PyString("abc")));
	}

	@Test
	public void testToObjectArrayWithArray() {
		Object[] array = new Object[] { 1, 2., "abc", new PyInteger(1), new PyFloat(2.), new PyString("abc") };
		assertArrayEquals(array, PositionConvertorFunctions.toObjectArray(array));
	}

	@Test
	public void testToObjectArrayWithDoubleArray() {
		Double[] array = new Double[] { 1., 2., null} ;
		assertArrayEquals(array, PositionConvertorFunctions.toObjectArray(array));
	}

	@Test
	public void testToObjectArrayWithString() {
		assertArrayEquals(new Object[] {"string"}, PositionConvertorFunctions.toObjectArray("string"));
		assertArrayEquals(new Object[] {new String("string")}, PositionConvertorFunctions.toObjectArray(new String("string")));
	}

	@Test
	public void testToObjectArrayWithPySequences() {
		PyObject[] array = new PyObject[] { new PyInteger(1), new PyFloat(2.), new PyString("abc") };
		assertArrayEquals(array, PositionConvertorFunctions.toObjectArray(array));
	}

	@Test
	public void testToObjectArrayWithPrimitiveDoubleArray() {
		double[] array = new double[] { 1., 2.} ;
		assertArrayEquals(new Double[] {1., 2.}, PositionConvertorFunctions.toObjectArray(array));
	}

	@Test
	public void testToObjectArrayWithArrayTypeCollections() {
		List<Object> list = new ArrayList<Object>();
		list.add(1);
		list.add(2);
		list.add("abc");
		Object[] objectArray = PositionConvertorFunctions.toObjectArray(list);
		assertEquals(1, objectArray[0]);
		assertEquals(2, objectArray[1]);
		assertEquals("abc", objectArray[2]);
	}


	@Test
	public void testToObject() {
		assertEquals(1, PositionConvertorFunctions.toObject(new Object[] { 1 }));
		assertEquals(new PyString("abc"), PositionConvertorFunctions.toObject(new Object[] { new PyString("abc") }));
		Object[] array = new Object[] { 1, 2., "abc", new PyInteger(1), new PyFloat(2.), new PyString("abc") };
		assertEquals(array, PositionConvertorFunctions.toObject(array));
	}

	@Test
	public void testToDoubleArray() {
		assertArrayEquals(new Double[] { 1., 2., null },
				PositionConvertorFunctions.toDoubleArray(new Object[] { 1., 2, null }));
		assertArrayEquals(new Double[] { 1., 2., 3. },
				PositionConvertorFunctions.toDoubleArray(new Object[] {"1", "2.", "3.00" }));
		assertArrayEquals(new Double[] { 1., 2.},
				PositionConvertorFunctions.toDoubleArray(new Object[] {new PyInteger(1), new PyFloat(2.)}));
	}

	@Test
	public void testToDoubleArrayWithPyStrings() {
		assertArrayEquals(new Double[] { 1.},
				PositionConvertorFunctions.toDoubleArray(new Object[] { new PyString("1.") }));
	}

	// Test all with nulls
	@Test
	public void testToDoubleArrayWithNull() {
		Object nullObject = null;
		Object[] nullObjectArray = null;
		assertEquals(null, PositionConvertorFunctions.toDouble(nullObject) );
		assertArrayEquals(null, PositionConvertorFunctions.toDoubleArray(nullObject) );
		assertArrayEquals(null, PositionConvertorFunctions.toDoubleArray(nullObjectArray) );
		assertEquals(null, PositionConvertorFunctions.toObject(nullObjectArray) );
		assertArrayEquals(null, PositionConvertorFunctions.toObjectArray(nullObject) );
	}

	@Test
	public void testToDoubleWithVariousJava() {
		assertEquals(new Double(1.), PositionConvertorFunctions.toDouble(1.));
		assertEquals(new Double(1.), PositionConvertorFunctions.toDouble(new Double(1.)));
		assertEquals(new Double(1.), PositionConvertorFunctions.toDouble(1));
		assertEquals(new Double(1.), PositionConvertorFunctions.toDouble(new Integer(1)));
	}

	@Test
	public void testToDoubleWithVariousPyObject() {
		assertEquals(new Double(1.), PositionConvertorFunctions.toDouble(1.));
		assertEquals(new Double(1.), PositionConvertorFunctions.toDouble(new PyFloat(1.)));
		assertEquals(new Double(1.), PositionConvertorFunctions.toDouble(1));
		assertEquals(new Double(1.), PositionConvertorFunctions.toDouble(new PyInteger(1)));
	}

	@Test
	public void testToQuantityWithQuantities() {
		assertEquals(Quantity.valueOf(1., MILLI(METER)),
				PositionConvertorFunctions.toQuantity(Quantity.valueOf(1., MILLI(METER)), MILLI(METER)));
		assertEquals(Quantity.valueOf(1., METER),
				PositionConvertorFunctions.toQuantity(Quantity.valueOf(1000., MILLI(METER)), METER));
		assertEquals(Quantity.valueOf(1., METER),
				PositionConvertorFunctions.toQuantity(Quantity.valueOf(1., Unit.ONE), METER));
	}

	@Test
	public void testToQuantityWithStrings() {
		assertEquals(Quantity.valueOf(1., MILLI(METER)),
				PositionConvertorFunctions.toQuantity("1 mm", MILLI(METER)));
		assertEquals(Quantity.valueOf(1., METER),
				PositionConvertorFunctions.toQuantity("1 m", METER));
		assertEquals(Quantity.valueOf(1., METER),
				PositionConvertorFunctions.toQuantity("1", METER));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testToQuantityWithStringsUnParsableString() {
		PositionConvertorFunctions.toQuantity("closed", METER );
	}

	@Test
	public void testToQuantityWithPyStrings() {
		assertEquals(Quantity.valueOf(1., MILLI(METER)),
				PositionConvertorFunctions.toQuantity(new PyString("1 mm"), MILLI(METER)));
		assertEquals(Quantity.valueOf(1., METER),
				PositionConvertorFunctions.toQuantity(new PyString("1 m"), METER));
		assertEquals(Quantity.valueOf(1., METER),
				PositionConvertorFunctions.toQuantity(new PyString("1"), METER));
	}

	@Test
	public void testToQuantityArray() {
		Quantity[] expected = new Quantity[]{Quantity.valueOf(1., METER), Quantity.valueOf(1., METER), null};
		Quantity[] actual = PositionConvertorFunctions.toQuantityArray(
				new Quantity[]{Quantity.valueOf(1., METER), Quantity.valueOf(1000., MILLI(METER)), null}, METER);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testToAmmountArray() {
		Double[] actual = PositionConvertorFunctions.toAmountArray(
				new Quantity[]{Quantity.valueOf(1., METER), Quantity.valueOf(1000., MILLI(METER)), null});
		assertArrayEquals(new Double[]{1., 1000., null}, actual);
	}

}

