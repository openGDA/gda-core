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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static tec.units.indriya.AbstractUnit.ONE;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.Units.METRE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.measure.Quantity;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;

import tec.units.indriya.quantity.Quantities;

@RunWith(Enclosed.class)
public class PositionConvertorFunctionsTest {
	// Tolerance for imprecision of floating-point calculations
	private static final double FP_TOLERANCE = 0.00001;

	@RunWith(Parameterized.class)
	public static class ParameterizedTests {

		@Parameters(name = "Test {index}: {2}")
		public static Collection<Object[]> data() {
			// Array of {input, expectedResult, description}
			return Arrays.asList(new Object[][] {
					// testToObject
					{ PositionConvertorFunctions.toObject(new Object[] { 1 }), 1, "toObject(new Object[] { 1 })" },
					{ PositionConvertorFunctions.toObject(new Object[] { new PyString("abc") }), new PyString("abc"),
							"toObject(new Object[] { new PyString(\"abc\") })" },
					// testToDoubleWithVariousJava
					{ PositionConvertorFunctions.toDouble(1.), new Double(1.), "toDouble(1.)" },
					{ PositionConvertorFunctions.toDouble(new Double(1.)), new Double(1.), "toDouble(new Double(1.))" },
					{ PositionConvertorFunctions.toDouble(1), new Double(1.), "toDouble(1)" },
					{ PositionConvertorFunctions.toDouble(new Integer(1)), new Double(1.), "toDouble(new Integer(1))" },
					// testToDoubleWithVariousPyObject
					{ PositionConvertorFunctions.toDouble(new PyFloat(1.)), new Double(1.),
							"toDouble(new PyFloat(1.))" },
					{ PositionConvertorFunctions.toDouble(new PyInteger(1)), new Double(1.),
							"toDouble(new PyInteger(1))" },
					// testToQuantityWithQuantities
					{ PositionConvertorFunctions.toQuantity(Quantities.getQuantity(1., MILLI(METRE)), MILLI(METRE)),
							Quantities.getQuantity(1., MILLI(METRE)),
							"toQuantity(Quantities.getQuantity(1., MILLI(METRE)), MILLI(METRE))" },
					{ PositionConvertorFunctions.toQuantity(Quantities.getQuantity(1000., MILLI(METRE)), METRE),
							Quantities.getQuantity(1., METRE), "toQuantity(Quantities.getQuantity(1000., MILLI(METRE)), METRE)" },
					{ PositionConvertorFunctions.toQuantity(Quantities.getQuantity(1., ONE), METRE),
							Quantities.getQuantity(1., METRE), "valueOf(1., Unit.ONE), METRE)" },
					// testToQuantityWithStrings
					{ PositionConvertorFunctions.toQuantity("1 mm", MILLI(METRE)), Quantities.getQuantity(1., MILLI(METRE)),
							"toQuantity(\"1 mm\", MILLI(METRE))" },
					{ PositionConvertorFunctions.toQuantity("1 m", METRE), Quantities.getQuantity(1., METRE),
							"toQuantity(\"1 m\", METRE)" },
					{ PositionConvertorFunctions.toQuantity("1", METRE), Quantities.getQuantity(1., METRE),
							"toQuantity(\"1\", METRE)" },
					// testToQuantityWithPyStrings
					{ PositionConvertorFunctions.toQuantity(new PyString("1 mm"), MILLI(METRE)),
							Quantities.getQuantity(1., MILLI(METRE)), "toQuantity(new PyString(\"1 mm\"), MILLI(METRE))" },
					{ PositionConvertorFunctions.toQuantity(new PyString("1 m"), METRE), Quantities.getQuantity(1., METRE),
							"toQuantity(new PyString(\"1 m\"), METRE)" },
					{ PositionConvertorFunctions.toQuantity(new PyString("1"), METRE), Quantities.getQuantity(1., METRE),
							"toQuantity(new PyString(\"1\"), METRE)" },
					// test with infinite/NaN values
					{ PositionConvertorFunctions.toDouble(Quantities.getQuantity(Double.POSITIVE_INFINITY, METRE)), Double.POSITIVE_INFINITY, "toDouble(Quantities.getQuantity(+Infinity, METRE))" },
					{ PositionConvertorFunctions.toDouble(Quantities.getQuantity(Double.NEGATIVE_INFINITY, METRE)), Double.NEGATIVE_INFINITY, "toDouble(Quantities.getQuantity(-Infinity, METRE))" },
					{ PositionConvertorFunctions.toDouble(Quantities.getQuantity(Double.NaN, METRE)), Double.NaN, "toDouble(Quantities.getQuantity(NaN, METRE))" }});
		}

		@Parameter
		public Object input;

		@Parameter(1)
		public Object expectedResult;

		@Parameter(2)
		public String description;

		@Test
		public void test() {
			if (expectedResult instanceof Double) {
				assertEquals((double) expectedResult, (double) input, FP_TOLERANCE);
			} else {
				assertEquals(expectedResult, input);
			}
		}
	}

	@RunWith(Parameterized.class)
	public static class ParameterizedArrayTests {

		@Parameters(name = "Test {index}: {2}")
		public static Collection<Object[]> data() {
			// Array of {input, expectedResult, description}
			return Arrays.asList(new Object[][] {
					// testToObjectArrayWithSingleObjects
					{ PositionConvertorFunctions.toObjectArray(1), new Object[] { 1 }, "toObjectArray(1)" },
					{ PositionConvertorFunctions.toObjectArray(1.), new Object[] { 1. }, "toObjectArray(1.)" },
					{ PositionConvertorFunctions.toObjectArray("abc"), new Object[] { "abc" },
							"toObjectArray(\"abc\")" },
					// testToObjectArrayWithSinglePyObjects
					{ PositionConvertorFunctions.toObjectArray(new PyInteger(1)), new Object[] { new PyInteger(1) },
							"toObjectArray(new PyInteger(1))" },
					{ PositionConvertorFunctions.toObjectArray(new PyFloat(1.)), new Object[] { new PyFloat(1.) },
							"toObjectArray(new PyFloat(1.))" },
					{ PositionConvertorFunctions.toObjectArray(new PyString("abc")),
							new Object[] { new PyString("abc") }, "toObjectArray(new PyString(\"abc\"))" },
					// testToObjectArrayWithString
					{ PositionConvertorFunctions.toObjectArray("string"), new Object[] { "string" },
							"toObjectArray(\"string\")" },
					{ PositionConvertorFunctions.toObjectArray(new String("string")),
							new Object[] { new String("string") }, "toObjectArray(new String(\"string\"))" },
					// testToObjectArrayWithDoubleArray
					{ PositionConvertorFunctions.toObjectArray(new Double[] { 1., 2., null }),
							new Double[] { 1., 2., null }, "toObjectArray(new Double[] { 1., 2., null})" },
					// testToObjectArrayWithPrimitiveDoubleArray
					{ PositionConvertorFunctions.toObjectArray(new Double[] { 1., 2. }), new Double[] { 1., 2. },
							"toObjectArray(new Double[] {1., 2.})" },
					// testToDoubleArrayWithDoubleArray
					{ PositionConvertorFunctions.toDoubleArray(new Object[] { 1., 2, null }),
							new Double[] { 1., 2., null }, "toDoubleArray(new Object[] { 1., 2, null })" },
					{ PositionConvertorFunctions.toDoubleArray(new Object[] { "1", "2.", "3.00" }),
							new Double[] { 1., 2., 3. }, "toDoubleArray(new Object[] {\"1\", \"2.\", \"3.00\" })" },
					{ PositionConvertorFunctions.toDoubleArray(new Object[] { new PyInteger(1), new PyFloat(2.) }),
							(new Double[] { 1., 2. }),
							"toDoubleArray(new Object[] {new PyInteger(1), new PyFloat(2.)})" },
					// testToDoubleArrayWithPyStrings
					{ PositionConvertorFunctions.toDoubleArray(new Object[] { new PyString("1.") }),
							(new Double[] { 1. }), "toDoubleArray(new Object[] { new PyString(\"1.\") })" },
					// test with infinite/NaN values
					{ PositionConvertorFunctions.toAmountArray(
							new Quantity<?>[] { Quantities.getQuantity(Double.POSITIVE_INFINITY, METRE), Quantities.getQuantity(Double.NEGATIVE_INFINITY, METRE) }),
							(new Double[] { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY }),
							"toAmountArray(new Quantity<?>[] { Quantities.getQuantity(+Infinity, METRE), Quantities.getQuantity(-Infinity, METRE) }" },
					{ PositionConvertorFunctions.toAmountArray(new Quantity<?>[] { Quantities.getQuantity(Double.NaN, METRE) }), (new Double[] { Double.NaN }),
							"toAmountArray(new Quantity<?>[] { Quantities.getQuantity(NaN, METRE) }" }
							});
		}

		@Parameter
		public Object[] input;

		@Parameter(1)
		public Object[] expectedResult;

		@Parameter(2)
		public String description;

		@Test
		public void testArrays() {
			assertArrayEquals(expectedResult, input);
		}

	}

	public static class NonParameterizedTests {

		@Test
		public void testToObjectArrayWithArray() {
			Object[] array = new Object[] { 1, 2., "abc", new PyInteger(1), new PyFloat(2.), new PyString("abc") };
			assertArrayEquals(array, PositionConvertorFunctions.toObjectArray(array));
		}

		@Test
		public void testToObjectArrayWithPySequences() {
			PyObject[] array = new PyObject[] { new PyInteger(1), new PyFloat(2.), new PyString("abc") };
			assertArrayEquals(array, PositionConvertorFunctions.toObjectArray(array));
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
			Object[] array = new Object[] { 1, 2., "abc", new PyInteger(1), new PyFloat(2.), new PyString("abc") };
			assertEquals(array, PositionConvertorFunctions.toObject(array));
		}

		// Test all with nulls
		@Test
		public void testToDoubleArrayWithNull() {
			Object nullObject = null;
			Object[] nullObjectArray = null;
			assertEquals(null, PositionConvertorFunctions.toDouble(nullObject));
			assertArrayEquals(null, PositionConvertorFunctions.toDoubleArray(nullObject));
			assertArrayEquals(null, PositionConvertorFunctions.toDoubleArray(nullObjectArray));
			assertEquals(null, PositionConvertorFunctions.toObject(nullObjectArray));
			assertArrayEquals(null, PositionConvertorFunctions.toObjectArray(nullObject));
		}

		@Test(expected = IllegalArgumentException.class)
		public void testToDoubleExceptionThrown() {
			// Test object array of length > 1
			PositionConvertorFunctions.toDouble(new Object[] { new PyInteger(3), new PyInteger(2), new PyInteger(1) });
		}

		@Test(expected = IllegalArgumentException.class)
		public void testToQuantityWithStringsUnParsableString() {
			PositionConvertorFunctions.toQuantity("closed", METRE);
		}

		@Test
		public void testToQuantityArray() {
			final Quantity<?>[] expected = new Quantity<?>[] { Quantities.getQuantity(1., METRE), Quantities.getQuantity(1., METRE), null };
			final Quantity<?>[] actual = PositionConvertorFunctions.toQuantityArray(
					new Quantity<?>[] { Quantities.getQuantity(1., METRE), Quantities.getQuantity(1000., MILLI(METRE)), null }, METRE);
			assertEquals(expected.length, actual.length);
			for (int i = 0; i < expected.length; i++) {
				if (expected[i] == null) {
					assertNull(actual[i]);
				} else {
					assertEquals(expected[i].getValue().doubleValue(), actual[i].getValue().doubleValue(), FP_TOLERANCE);
				}
			}
		}

		@Test
		public void testToAmountArray() {
			final Double[] actual = PositionConvertorFunctions.toAmountArray(
					new Quantity<?>[] { Quantities.getQuantity(1., METRE), Quantities.getQuantity(1000., MILLI(METRE)), null });
			assertEquals(3, actual.length);
			assertEquals(1., actual[0], FP_TOLERANCE);
			assertEquals(1000., actual[1], FP_TOLERANCE);
			assertNull(actual[2]);
		}
	}
}
