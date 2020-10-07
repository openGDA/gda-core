/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import static gda.device.scannable.ScannableUtils.objectToArray;
import static gda.device.scannable.ScannableUtils.objectToDouble;
import static java.lang.Double.NaN;
import static java.util.stream.Stream.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.array;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tec.units.indriya.quantity.Quantities.getQuantity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.python.core.Py;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import gda.device.Detector;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import tec.units.indriya.AbstractUnit;
import tec.units.indriya.quantity.Quantities;
import tec.units.indriya.unit.Units;


/**
 * tests the methods in ScannableUtils
 */
public class ScannableUtilsTest {


	private Scannable scannable;
	private Scannable vectorscn;

	/**
	 * start with a scannable with reasonable defaults
	 */
	@Before
	public void setUp() {
		scannable = mock(Scannable.class);
		when(scannable.getName()).thenReturn(new String("Alfred"));
		when(scannable.getInputNames()).thenReturn(new String[] {"value"});
		when(scannable.getExtraNames()).thenReturn(new String[] {});

		vectorscn = mock(Scannable.class);
		when(vectorscn.getName()).thenReturn("abc");
		when(vectorscn.getInputNames()).thenReturn(new String[]{"a","b","c"});
	}

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetFormattedCurrentPosition() throws Exception {
		Object position =  new Double(1.0/3);
		when(scannable.getPosition()).thenReturn(position);
		String[] format = new String[] {"LALALA%3.3fUAUAUA"};
		when(scannable.getOutputFormat()).thenReturn(format);

		String formattedCurrentPosition = ScannableUtils.getFormattedCurrentPosition(scannable);

		String expected = String.format(format[0], position);
		assertTrue(formattedCurrentPosition.contains(expected));
	}

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetFormattedCurrentPositionArrayScannable() throws Exception {
		Object position =  new Double(1.0/3);
		when(scannable.getPosition()).thenReturn(position);
		String[] format = new String[] {"%3.3f"};
		when(scannable.getOutputFormat()).thenReturn(format);

		String[] formattedCurrentPositionArray = ScannableUtils.getFormattedCurrentPositionArray(scannable);

		assertEquals(formattedCurrentPositionArray[0], String.format(format[0], position));
	}

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetFormattedCurrentPositionArrayScannableInteger() throws Exception {
		Object position =  new Integer(42);
		when(scannable.getPosition()).thenReturn(position);
		String[] format = new String[] {"%d"};
		when(scannable.getOutputFormat()).thenReturn(format);

		String[] formattedCurrentPositionArray = ScannableUtils.getFormattedCurrentPositionArray(scannable);

		assertEquals(formattedCurrentPositionArray[0], String.format(format[0], position));
	}

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetFormattedCurrentPositionArrayScannableFloat() throws Exception {
		Object position =  new Float(1.0/9);
		when(scannable.getPosition()).thenReturn(position);
		String[] format = new String[] {"%10.1f"};
		when(scannable.getOutputFormat()).thenReturn(format);

		String[] formattedCurrentPositionArray = ScannableUtils.getFormattedCurrentPositionArray(scannable);

		assertEquals(formattedCurrentPositionArray[0], String.format(format[0], position));
	}


	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetFormattedCurrentPositionArrayScannableString() throws Exception {
		Object position =  new String("POSITION");
		when(scannable.getPosition()).thenReturn(position);
		String[] format = new String[] {"%s"};
		when(scannable.getOutputFormat()).thenReturn(format);

		String[] formattedCurrentPositionArray = ScannableUtils.getFormattedCurrentPositionArray(scannable);

		assertEquals(formattedCurrentPositionArray[0], String.format(format[0], position));
	}


	/**
	 *
	 * @throws Exception
	 */
	public void testGetFormattedCurrentPositionArrayScannableDate() throws Exception {
		Object position =  new Date();
		when(scannable.getPosition()).thenReturn(position);
		String[] format = new String[] {"%c"};
		when(scannable.getOutputFormat()).thenReturn(format);

		String[] formattedCurrentPositionArray = ScannableUtils.getFormattedCurrentPositionArray(scannable);

		assertEquals(formattedCurrentPositionArray[0], String.format(format[0], position));
	}

	@Test
	public void testFormatScannableWithChildrenNoParentPositionNoErrors() throws Exception {
		final Scannable s1 = new DummyScannable("s1");
		final Scannable s2 = new DummyScannable("s2");
		final ScannableGroup group = new ScannableGroup("sg1", new Scannable[] { s1, s2 });
		final String expectedResult = "sg1 ::\n  s1 : 0.0000 (-1.7977e+308:1.7977e+308)\n  s2 : 0.0000 (-1.7977e+308:1.7977e+308)";

		final String result = ScannableUtils.formatScannableWithChildren(group, group.getGroupMembers(), false);
		assertEquals(expectedResult, result);
	}

	@Test
	public void testFormatScannableWithChildrenNoParentPositionWithErrors() throws Exception {
		// In the event of an exception in one scannable, the ScannableGroup should show the value as dashes
		// and continue with the remaining scannables.
		final Scannable s1 = new DummyScannable("s1") {
			@Override
			public String toFormattedString() {
				throw new RuntimeException("failure in toFormattedString()");
			}
		};
		final Scannable s2 = new DummyScannable("s2");
		final ScannableGroup group = new ScannableGroup("sg1", new Scannable[] { s1, s2 });
		final String expectedResult = "sg1 ::\n  s1 : UNAVAILABLE\n  s2 : 0.0000 (-1.7977e+308:1.7977e+308)";

		final String result = ScannableUtils.formatScannableWithChildren(group, group.getGroupMembers(), false);
		assertEquals(expectedResult, result);
	}

	@Test
	public void testFormatScannableWithChildrenWithParentPositionNoErrors() throws Exception {
		final Scannable s1 = new DummyScannable("s1");
		final Scannable s2 = new DummyScannable("s2");
		final ScannableGroup group = new ScannableGroup("sg1", new Scannable[] { s1, s2 });
		final String expectedResult = "sg1  : s1: 0.0000 s2: 0.0000 ::\n  s1 : 0.0000 (-1.7977e+308:1.7977e+308)\n  s2 : 0.0000 (-1.7977e+308:1.7977e+308)";

		final String result = ScannableUtils.formatScannableWithChildren(group, group.getGroupMembers(), true);
		assertEquals(expectedResult, result);
	}

	@Test
	public void testFormatScannableWithChildrenWithParentPositionWithErrors() throws Exception {
		final Scannable s1 = new DummyScannable("s1");
		final Scannable s2 = new DummyScannable("s2");
		final ScannableGroup group = new ScannableGroup("sg1", new Scannable[] { s1, s2 }) {
			@Override
			public String getPosition() {
				throw new RuntimeException("failure in getPosition()");
			}
		};
		final String expectedResult = "sg1  : UNAVAILABLE ::\n  s1 : 0.0000 (-1.7977e+308:1.7977e+308)\n  s2 : 0.0000 (-1.7977e+308:1.7977e+308)";

		final String result = ScannableUtils.formatScannableWithChildren(group, group.getGroupMembers(), true);
		assertEquals(expectedResult, result);
	}

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetCurrentPositionArray() throws Exception {
		Number[] position =  new Number[] {1.0/3, 1};
		when(scannable.getPosition()).thenReturn(position);
		String[] format = new String[] {"%3.3f", "%d"};
		when(scannable.getOutputFormat()).thenReturn(format);
		when(scannable.getInputNames()).thenReturn(new String[] {"value0", "value1"});

		double[] actual = ScannableUtils.getCurrentPositionArray(scannable);
		for (int i = 0; i < position.length; i++) {
			assertEquals(position[i].doubleValue(), actual[i], 0);
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	public void testGetCurrentPositionArray_InputsOnly() throws Exception {
		Object[] position =  new Double[] {12.0, 9.0, -0.1, 3.1415};
		when(scannable.getPosition()).thenReturn(position);
		String[] format = new String[] {"%f","%f","%f","%f"};
		when(scannable.getOutputFormat()).thenReturn(format);
		when(scannable.getInputNames()).thenReturn(new String[] {"value0", "value1"});
		when(scannable.getExtraNames()).thenReturn(new String[] {"value2", "value3"});


		double[] positionArray = ScannableUtils.getCurrentPositionArray_InputsOnly(scannable);

		assertEquals(2, positionArray.length);
		assertEquals(position[0], positionArray[0]);
		assertEquals(position[1], positionArray[1]);
	}
	@Test
	public void testGetCurrentPositionMultipleFields() throws Exception {
		Object position =  new Double[] {12.0, .1, -0.1, 3.145};
		String[] format = new String[] {"%.1f","%.2f","%.3f","%.4f"};
		String[] result = ScannableUtils.getFormattedCurrentPositionArray(position, 4, format);
		assertArrayEquals(new String[]{"12.0", "0.10", "-0.100", "3.1450"}, result);
	}

	@Test
	public void testGetCurrentPositionMultipleFieldsIncludingNulls() throws Exception {
		Object position =  new Double[] {12.0, null, -0.1, 3.145};
		String[] format = new String[] {"%.1f","%.2f","%.3f","%.4f"};
		String[] result = ScannableUtils.getFormattedCurrentPositionArray(position, 4, format);
		assertArrayEquals(new String[]{"12.0", "unknown", "-0.100", "3.1450"}, result);
	}
	@Test
	public void testGetCurrentPositionMultipleFieldsBadFormat() throws Exception {
		Object position =  new Double[] {12.0, .1, -0.1, 3.145};
		String[] format = new String[] {"%.1f"};
		String[] result = ScannableUtils.getFormattedCurrentPositionArray(position, 4, format);
		assertArrayEquals(new String[]{"12.0", "0.1", "-0.1", "3.1"}, result);
	}

	public void testCalculateNextPoint() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNumberSteps() throws Exception {
		assertEquals(100, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{-.05207, 4., 0.0}, new Double[]{.04793, 4., 0.0}, new Double[]{.001, 0., 0.0}));
	}

	@Test
	public void testGetNumberStepsWithSomeNegatives() throws Exception {
		assertEquals(10, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{-10., 0., 1.234567}, new Double[]{10., 5., 1.234567}, new Double[]{2., .5, 0.}));
	}

	@Test
	public void testGetNumberStepsWithSomeBadStepVector() {
		try {
			ScannableUtils.getNumberSteps(vectorscn, new Double[] { -10., 0., 1.234567 }, new Double[] { 10., 400.,
					1.234567 }, new Double[] { 2., .5, 0. });
			fail("Exception expected");
		} catch (Exception e) {
			assertEquals(
					"The step-vector does not connect the start and end points within the allowed\n" +
					"tolerance of one step: in one basis direction 800 steps are required, but\n"+
					"in another only 10 steps are required.",
					e.getMessage());
		}
	}

	@Test
	public void testGetNumberStepsWithInnacurateEndPoint() throws Exception {
		assertEquals(3, ScannableUtils.getNumberSteps(scannable, 0, 10, 3));
	}

	@Test
	public void testGetNumberStepsWithInnacurateEndPointWithVectorDevices() throws Exception {
		assertEquals(100, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{-.05207, 4., 0.0}, new Double[]{.04793, 4., 0.0}, new Double[]{.001, 0., 0.0}));
	}

	@Test
	public void testGetNumberStepsWithVectorGood1() throws Exception {
		assertEquals(2, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{0., 0., 0.}, new Double[]{2., 0., 2.}, new Double[]{1., 0., .7}));
		assertEquals(2, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{0., 0., 0.}, new Double[]{-2., 0., 2.}, new Double[]{-1., 0., .7}));
		assertEquals(2, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{0., 0., -2.}, new Double[]{2., 0., 0.}, new Double[]{1., 0., .7}));
	}

	@Test
	public void testGetNumberStepsWithVectorGood2() throws Exception {
		assertEquals(2, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{0., 0., 0.}, new Double[]{2., 0., 2.}, new Double[]{.7, 0., 1.}));
		assertEquals(2, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{0., 0., 0.}, new Double[]{-2., 0., 2.}, new Double[]{-.7, 0., 1.}));
		assertEquals(2, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{0., 0., -2.}, new Double[]{2., 0., 0.}, new Double[]{.7, 0., 1.}));
	}

	@Test
	public void testGetNumberStepsWithVectorGood3() throws Exception {
		assertEquals(1, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{0., 0., 0.}, new Double[]{2., 0., 2.}, new Double[]{1., 0., 1.1}));
		assertEquals(1, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{0., 0., 0.}, new Double[]{-2., 0., 2.}, new Double[]{-1., 0., 1.1}));
		assertEquals(1, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{0., 0., -2.}, new Double[]{2., 0., 0.}, new Double[]{1., 0., 1.1}));
	}

	@Test
	public void testGetNumberStepsWithVectorGood4() throws Exception {
		assertEquals(1, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{0., 0., 0.}, new Double[]{2., 0., 2.}, new Double[]{1.1, 0., 1.}));
		assertEquals(1, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{0., 0., 0.}, new Double[]{-2., 0., 2.}, new Double[]{-1.1, 0., 1.}));
		assertEquals(1, ScannableUtils.getNumberSteps(vectorscn,
				new Double[]{0., 0., -2.}, new Double[]{2., 0., 0.}, new Double[]{1.1, 0., 1.}));
	}

	@Test
	public void testGetNumberStepsWithVectorBadBecauseOfZeroStepsize()  {
		try {
			assertEquals(1, ScannableUtils.getNumberSteps(vectorscn,
					new Double[]{0., 0., 0.}, new Double[]{2., 2., 2.}, new Double[]{1.1, 0., 1.}));
			fail("Exception expected");
		} catch (Exception e) {
			assertEquals("a step field is zero despite there being a distance to move in that direction.", e.getMessage());
		}
	}

	public void testValidate() {
		fail("Not yet implemented");
	}

	public void testGetScannableInputFieldNames() {
		fail("Not yet implemented");
	}

	public void testGetScannableExtraFieldNames() {
		fail("Not yet implemented");
	}

	public void testGetScannableFieldNames() {
		fail("Not yet implemented");
	}

	public void testGetDetectorFieldNames() {
		fail("Not yet implemented");
	}

	public void testGetMonitorFieldNames() {
		fail("Not yet implemented");
	}


	@Test
	public void testgetScannableFieldNames(){
		when(scannable.getInputNames()).thenReturn(new String[] {"value0", "value1"});
		when(scannable.getExtraNames()).thenReturn(new String[] {"value2", "value3"});

		Detector detectorWithExtra = mock(Detector.class);
		when(detectorWithExtra.getName()).thenReturn(new String("detectorWithExtra"));
		when(detectorWithExtra.getInputNames()).thenReturn(new String[] {"value"});
		when(detectorWithExtra.getExtraNames()).thenReturn(new String[] {"extra1","extra2"});

		Detector detectorWithoutExtra = mock(Detector.class);
		when(detectorWithoutExtra.getName()).thenReturn(new String("detectorWithoutExtra"));
		when(detectorWithoutExtra.getInputNames()).thenReturn(new String[] {"value"});
		when(detectorWithoutExtra.getExtraNames()).thenReturn(new String[] {});

		Vector<Scannable> scannables = new Vector<Scannable>();
		scannables.add(scannable);
		scannables.add(detectorWithExtra);
		scannables.add(detectorWithoutExtra);

		List<String> scannableFieldNames = ScannableUtils.getScannableFieldNames(scannables);

		assertEquals(4+2+1, scannableFieldNames.size());

	}

	@Test
	public void testGetExtraNamesFormatsShouldThrowException() {

		// Newly-created scannable has 1 input name, 0 extra names, and 1 output format
		Scannable s = new DummyScannable("s");
		assertEquals(1, s.getInputNames().length);
		assertEquals(0, s.getExtraNames().length);
		assertEquals(1, s.getOutputFormat().length);

		// Clear the output format
		s.setOutputFormat(new String[] {});
		assertEquals(1, s.getInputNames().length);
		assertEquals(0, s.getExtraNames().length);
		assertEquals(0, s.getOutputFormat().length);

		// Calling getExtraNamesFormats should fail
		try {
			ScannableUtils.getExtraNamesFormats(s);
		} catch (IllegalStateException e) {
			// expected
		}

		// But put the output format back...
		s.setOutputFormat(new String[] {"%.2f"});
		assertEquals(1, s.getInputNames().length);
		assertEquals(0, s.getExtraNames().length);
		assertEquals(1, s.getOutputFormat().length);

		// ...and the getExtraNamesFormats call should succeed
		ScannableUtils.getExtraNamesFormats(s);
	}

	@Test
	public void testSerializationOfScannableSnapshot() throws Exception {
		when(vectorscn.getPosition()).thenReturn(new Double[] {0., 12., 100., -1.});
		when(vectorscn.getOutputFormat()).thenReturn(new String[] {"%5.5g", "%5.5g", "%5.5g", "%5.5g"});
		when(vectorscn.getExtraNames()).thenReturn(new String[] {"test"});
		when(vectorscn.isBusy()).thenReturn(true);

		ScannableSnapshot in = new ScannableSnapshot(vectorscn);
		String b64 = ScannableUtils.getSerializedScannableSnapshot(vectorscn);
		ScannableSnapshot out = ScannableUtils.deserializeScannableSnapshot(b64);
		assertEquals(in.name, out.name);
		assertEquals(in.busy, out.busy);
		assertArrayEquals(in.inputNames, out.inputNames);
		assertArrayEquals(in.extraNames, out.extraNames);
		assertArrayEquals(in.outputFormat, out.outputFormat);
		assertArrayEquals(in.units, out.units);
		assertArrayEquals((Object[]) in.lastPosition, (Object[]) out.lastPosition);
	}

	@Test
	public void singleObjectToDouble() {
		assertEquals("double from double", objectToDouble(1.2), 1.2, 1e-6);
		assertEquals("double from int", objectToDouble(1), 1.0, 1e-6);
		assertEquals("double from long", objectToDouble(1L), 1.0, 1e-6);
		assertEquals("double from string", objectToDouble("12.34"), 12.34, 1e-6);
		assertEquals("double from PyInteger", objectToDouble(Py.newInteger(23)), 23.0, 1e-6);
		assertEquals("double from PyLong", objectToDouble(Py.newLong(42)), 42.0, 1e-6);
		assertEquals("double from PyString", objectToDouble(Py.newString("87.65")), 87.65, 1e-6);
		assertEquals("double from PyDecimal", objectToDouble(Py.newDecimal("76.54")), 76.54, 1e-6);
		assertEquals("double from distance", objectToDouble(getQuantity(12.2, Units.METRE)), 12.2, 1e-6);
		assertEquals("double from unitless quantity", objectToDouble(getQuantity(19.1, AbstractUnit.ONE)), 19.1, 1e-6);
		assertEquals("double from Arbitrary object string", objectToDouble(new NumberString("34.89")), 34.89, 1e-6);
		assertEquals("double from quantity string", objectToDouble("17.34 m"), 17.34, 1e-6);
		assertEquals("double from quantity PyString", objectToDouble(Py.newString("14.65 cm")), 14.65, 1e-6);

		assertThat("NaN from NaN value", objectToDouble(NaN), is(NaN));
		assertThat("NaN from NaN string", objectToDouble("NaN"), is(NaN));
		assertThat("NaN from Py NaN", objectToDouble(Py.newFloat(NaN)), is(NaN));
		assertThat("NaN from Py NaN string", objectToDouble(Py.newString("NaN")), is(NaN));

		assertNull("null from null", objectToDouble(null));
		assertNull("null from non number string", objectToDouble("not a number"));
		assertNull("null from non number", objectToDouble(new Object()));
		assertNull("null from non number PyObject", objectToDouble(Py.Ellipsis));
	}

	@Test
	public void doubleArrayObjectToArray() {
		// The most basic conversion - an object to itself
		Double[] input = new Double[] {1.0, 2.0, 3.0};
		Double[] output = objectToArray(input);
		assertDoubleArrayEquals(input, output);
	}

	@Test
	public void numberArrayObjectToArray() {
		Integer[] input = new Integer[] {1,2,3,4};
		Double[] output = objectToArray(input);
		assertDoubleArrayEquals(output, 1.0, 2.0, 3.0, 4.0);
	}

	@Test
	public void mixedObjectArrayToArray() {
		Object[] input = new Object[] {"1.2", 2.3, 4, 3L, 2.3f,
				Py.newInteger(42),
				Py.newLong(2L),
				Py.newFloat(87.65),
				new Object(), // unknown objects map to null
				new BigDecimal("34.56"),
				BigInteger.valueOf(1234)};
		Double[] output = objectToArray(input);
		assertDoubleArrayEquals(output, 1.2, 2.3, 4.0, 3.0, 2.3, 42.0, 2.0,
				87.65, null, 34.56, 1234.0);
	}

	@Test
	public void primitiveDoubleArrayToArray() {
		double[] input = new double[] {1.2, 2.3, 3.4, 4.5};
		Double[] output = objectToArray(input);
		assertDoubleArrayEquals(output, 1.2, 2.3, 3.4, 4.5);
	}

	@Test
	public void primitiveIntArrayToArray() {
		int[] input = new int[] {1, 2, 3, 4, 5};
		Double[] output = objectToArray(input);
		assertDoubleArrayEquals(output, 1.0, 2.0, 3.0, 4.0, 5.0);
	}

	@Test
	public void jythonSequenceToArray() {
		PyTuple inputTuple = new PyTuple(Py.newInteger(17),
				Py.newFloat(2.3),
				Py.newFloat(3.4f),
				Py.newDecimal("3.4"),
				Py.newString("7.2"));
		Double[] outputTuple = objectToArray(inputTuple);
		assertDoubleArrayEquals(outputTuple, 17.0, 2.3, 3.4, 3.4, 7.2);

		PyList inputList = new PyList(new PyObject[] {
				Py.newInteger(17),
				Py.newFloat(2.3),
				Py.newFloat(3.4f),
				Py.newDecimal("3.4"),
				Py.newString("7.2")});
		Double[] outputList = objectToArray(inputList);
		assertDoubleArrayEquals(outputList, 17.0, 2.3, 3.4, 3.4, 7.2);
	}

	@Test
	public void listToArray() {
		List<Double> inputDouble = Arrays.asList(1.2, 2.3, 3.4);
		Double[] outputDouble = objectToArray(inputDouble);
		assertDoubleArrayEquals(outputDouble, 1.2, 2.3, 3.4);

		List<Integer> inputInteger = Arrays.asList(1, 2, 3, 4, 5);
		Double[] outputInteger = objectToArray(inputInteger);
		assertDoubleArrayEquals(outputInteger, 1., 2., 3., 4., 5.);

		List<String> inputString = Arrays.asList("1", "2", "3.4");
		Double[] outputString = objectToArray(inputString);
		assertDoubleArrayEquals(outputString, 1., 2., 3.4);
	}

	@Test
	public void quantityToArray() {
		Quantity<Length> distanceMetre = Quantities.getQuantity(12.2, Units.METRE);
		Double[] outputDistanceMetre = objectToArray(distanceMetre);
		assertDoubleArrayEquals(outputDistanceMetre, 12.2);

		Quantity<Speed> speed = Quantities.getQuantity(10.4, Units.METRE_PER_SECOND);
		Double[] outputSpeed = objectToArray(speed);
		assertDoubleArrayEquals(outputSpeed, 10.4);
	}

	@Test
	public void stringToArray() {
		String input = "12.4";
		Double[] output = objectToArray(input);
		assertDoubleArrayEquals(output, 12.4);

		String inputQuantity = "1.23m";
		Double[] outputQuantity = objectToArray(inputQuantity);
		assertDoubleArrayEquals(outputQuantity, 1.23);

		String inputEmpty = "NaN";
		Double[] outputEmpty = objectToArray(inputEmpty);
		assertDoubleArrayEquals(outputEmpty, NaN);
	}

	@Test
	public void nullToArray() {
		Object input = null;
		Double[] output = objectToArray(input);
		assertDoubleArrayEquals(output);
	}

	@Test
	public void arbitraryObjectToArray() {
		Double[] output = objectToArray(new NumberString(12.4));
		assertDoubleArrayEquals(output, 12.4);
	}

	@Test
	public void arrayOfArbitraryObjectsToArray() {
		Object[] input = new Object[] { new NumberString(2), new NumberString(3)};
		Double[] output = objectToArray(input);
		assertDoubleArrayEquals(output, 2.0, 3.0);
	}

	@Test(expected = NumberFormatException.class)
	public void unknownObjectToArray() {
		assertDoubleArrayEquals(objectToArray(new Object()));
	}

	@Test
	public void singleJythonStringToArray() {
		Object input = Py.newString("12345");
		Double[] output = objectToArray(input);
		assertDoubleArrayEquals(output, 12345.0);
	}

	@SuppressWarnings("unchecked") // can't create array of Matcher<Double>
	private void assertDoubleArrayEquals(Double[] actual, Double... expected) {
		assertThat(actual, is(array(of(expected)
				.map(this::doubleMatcher)
				.toArray(Matcher[]::new))));
	}

	/** Get an appropriate matcher to match any type of Double */
	private Matcher<Double> doubleMatcher(Double target) {
		if (target == null) return is((Double)null);
		if (Double.isNaN(target)) return is(NaN);
		return closeTo(target, 1e-6);
	}

	/** Class to represent arbitrary objects that might look like numbers */
	class NumberString {
		private Object value;
		public NumberString(Object i) { value = i;	}
		@Override
		public String toString() { return String.valueOf(value); }
	}
}