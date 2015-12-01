/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gda.device.DeviceException;

import org.junit.Before;
import org.junit.Test;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;

public class DummyScannableTest {

	private static final double INITIAL_POSITION = 10.0;

	//The following string applies only for default limits. This does not work
	//after the limits are set in testLimits()
	private static String OUTPUTSTRING = "testPD : 10.000 (-1.7977e+308:1.7977e+308)";

	private DummyScannable theScannable;

	@Before
	public void setUp() throws Exception {
		theScannable = new DummyScannable("testPD");
		theScannable.moveTo(INITIAL_POSITION);
	}

	@Test
	public void testIsBusy() {
		try {
			assertFalse(theScannable.isBusy());
		} catch (DeviceException e) {
			fail("Exception thrown: " + e.getMessage());
		}
	}

	@Test
	public void testGetPosition() {
		try {
			assertEquals(theScannable.getPosition(), 10.0);
		} catch (DeviceException e) {
			fail("Exception thrown during testGetPosition: " + e.getMessage());
		}
	}

	@Test
	public void testAsynchronousMoveTo() {
		try {
			theScannable.asynchronousMoveTo(12.0);
			assertEquals(theScannable.getPosition(), 12.0);
		} catch (DeviceException e) {
			fail("Exception thrown during testAsynchronousMoveTo: " + e.getMessage());
		}
	}

	@Test
	public void testMoveTo() {
		try {
			theScannable.moveTo(12.0);
			assertEquals(theScannable.getPosition(), 12.0);
		} catch (DeviceException e) {
			fail("Exception thrown during testMoveTo: " + e.getMessage());
		}
	}

	@Test
	public void testIsPositionValid() {
		try {
			assertTrue(theScannable.checkPositionValid(10) == null);
			assertTrue(theScannable.checkPositionValid(10.0) == null);
			assertTrue(theScannable.checkPositionValid("hello") != null);
			assertTrue(theScannable.checkPositionValid("10.0") == null);
		} catch (DeviceException e) {
			fail("Exception thrown during testIsPositionValid: " + e.getMessage());
		}
	}

	@Test
	public void testGetNumberSteps() {
		try {
			assertEquals(ScannableUtils.getNumberSteps(theScannable, 100, 200, 1), 100);
		} catch (Exception e) {
			fail("Exception thrown during testGetNumberSteps: " + e.getMessage());
		}
	}

	@Test
	public void testGetCurrentPositionArray() {
		try {
			assertTrue(ScannableUtils.getCurrentPositionArray(theScannable)[0] == 10.0);
		} catch (Exception e) {
			fail("Exception thrown during testGetCurrentPositionArray: " + e.getMessage());
		}
	}

	@Test
	public void testGetName() {
		assertEquals(theScannable.getName(), "testPD");
	}

	@Test
	public void testSetName() {
		theScannable.setName("newname");
		assertEquals(theScannable.getName(), "newname");
	}

	@Test
	public void testSetLevel() {
		theScannable.setLevel(100);
		assertEquals(theScannable.getLevel(), 100);
	}

	@Test
	public void testGetLevel() {
		assertEquals(theScannable.getLevel(), 5);
	}

	@Test
	public void testGetInputNames() {
		assertEquals(theScannable.getInputNames()[0], "testPD");
	}

	@Test
	public void testGetOutputFormat() {
		assertEquals(theScannable.getOutputFormat()[0], "%5.5g");
	}

	@Test
	public void testGetExtraNames() {
		assertEquals(0, theScannable.getExtraNames().length);
	}

	@Test
	public void testSetExtraNames() {
		theScannable.setExtraNames(new String[] { "testing", "testing2" });
		assertEquals(theScannable.getExtraNames()[0], "testing");
		assertEquals(theScannable.getExtraNames()[1], "testing2");
	}

	@Test
	public void testSetInputNames() {
		theScannable.setInputNames(new String[] { "newname" });
		assertEquals(theScannable.getInputNames()[0], "newname");
	}

	@Test
	public void testToString() {
		assertEquals("testPD<class gda.device.scannable.DummyScannable>", theScannable.toString());
	}

	@Test
	public void test__call__() {
		assertTrue(theScannable.__call__().__float__().getValue() == 10.0);
	}

	@Test
	public void test__call__PyObject() {
		try {
			theScannable.__call__(new PyFloat(12.0));
			assertEquals(theScannable.getPosition(), 12.0);
		} catch (Exception e) {
			fail("Exception during __call__: " + e.getMessage());
		}
	}

	@Test
	public void test__len__() {
		assertEquals(theScannable.__len__(), 1);
	}

	@Test
	public void test__getitem__() {
		PyObject item = theScannable.__getitem__(new PyInteger(0));
		assertEquals(item.toString(), "10.0");
	}

	@Test
	public void test__str__() {
		assertEquals(new PyString(OUTPUTSTRING), theScannable.__str__());
	}

	@Test
	public void test__repr__() {
		assertEquals(new PyString(OUTPUTSTRING), theScannable.__repr__());
	}

	/**
	 * Check that the limits checking actually works. Use lower and upper GDA limits.
	 */
	@Test
	public void testLimits() {
		try {
			theScannable.setLowerGdaLimits(0.);
			theScannable.setUpperGdaLimits(10.);
		} catch (Exception e) {
			fail("Exception while setting limits for testLimits:" + e.getMessage());
		}
		try {
			assertTrue(theScannable.checkPositionValid(-1.) != null);
			assertTrue(theScannable.checkPositionValid(11.) != null);
			assertTrue(theScannable.checkPositionValid(5.) == null);
		} catch (DeviceException e) {
			fail("Exception while checking positions in testLimits: " + e.getMessage());
		}
	}

	/**
	 * Test behaviour when incrementing
	 */
	private static final double INCREMENT = 0.53;
	private static final double LOWER_LIMIT = -2.6;
	private static final double UPPER_LIMIT = 12.3;

	@Test
	public void testIncrement() throws DeviceException {
		theScannable.setIncrement(INCREMENT);
		assertEquals(INITIAL_POSITION + INCREMENT, (double) theScannable.getPosition(), 0.001);
		assertEquals(INITIAL_POSITION + 2 * INCREMENT, (double) theScannable.getPosition(), 0.001);
	}

	@Test
	public void testIncrementWithinLimits() throws Exception {
		// Test that the value keeps varying but stays within limits
		// subject to inaccuracies in double.
		theScannable.setIncrement(INCREMENT);
		theScannable.setLowerGdaLimits(LOWER_LIMIT);
		theScannable.setUpperGdaLimits(UPPER_LIMIT);

		double lastPos = INITIAL_POSITION;
		for (int i = 0; i < 100; i++) {
			final double currentPos = (double) theScannable.getPosition();
			assertTrue(Math.abs(currentPos - lastPos) > 0.001);
			assertTrue(currentPos - UPPER_LIMIT < 0.001);
			assertTrue(currentPos - LOWER_LIMIT > 0.001);
			lastPos = currentPos;
		}
	}
}
