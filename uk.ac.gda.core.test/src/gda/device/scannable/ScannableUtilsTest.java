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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import gda.device.Detector;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;


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
		final String expectedResult = "sg1 ::\n  s1 : n/a\n  s2 : 0.0000 (-1.7977e+308:1.7977e+308)";

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
		final String expectedResult = "sg1  : n/a ::\n  s1 : 0.0000 (-1.7977e+308:1.7977e+308)\n  s2 : 0.0000 (-1.7977e+308:1.7977e+308)";

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
}