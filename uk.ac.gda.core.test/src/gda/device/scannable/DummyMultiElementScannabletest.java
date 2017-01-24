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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gda.device.DeviceException;

import org.junit.Before;
import org.junit.Test;
import org.python.core.PyArray;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;

/**
 * DummyMultiElementScannabletest Class
 */
public class DummyMultiElementScannabletest {
	DummyMultiElementScannable theScannable;

	double[] newPosition = new double[] { 12.0, 22.0, 32.0 };

	double[] outputNewPosition = new double[] { 12.0, 22.0, 32.0, 10.0 };

	double[] startPosition = new double[] { 10.0, 20.0, 30.0 };

	double[] outputStartPosition = new double[] { 10.0, 20.0, 30.0, 10.0 };

	double[] increment = new double[] { 2.0, 2.0, 2.0 };

	private boolean arrayCompare(PyArray array1, double[] array2) {
		if (array1.__len__() != array2.length) {
			return false;
		}

		for (int i = 0; i < array1.__len__(); i++) {
			if (((PyFloat) array1.__getitem__(i)).getValue() != array2[i]) {
				return false;
			}
		}

		return true;
	}

	private boolean arrayCompare(double[] array1, double[] array2) {
		if (array1.length != array2.length) {
			return false;
		}

		for (int i = 0; i < array1.length; i++) {
			if (array1[i] != array2[i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 */
	@Before
	public void setUp() {
		theScannable = new DummyMultiElementScannable("testPD", startPosition, new String[] { "first", "second",
				"third" });
	}

	/**
	 *
	 */
	@Test
	public void testIsBusy() {
		try {
			assertFalse(theScannable.isBusy());
		} catch (DeviceException e) {
			fail("Exception thrown: " + e.getMessage());
		}
	}

	/**
	 *
	 */
	@Test
	public void testGetPosition() {
		try {
			assertTrue(arrayCompare((double[]) theScannable.getPosition(), outputStartPosition));
		} catch (DeviceException e) {
			fail("Exception thrown: " + e.getMessage());
		}
	}

	/**
	 *
	 */
	@Test
	public void testAsynchronousMoveTo() {
		try {
			theScannable.asynchronousMoveTo(newPosition);
		} catch (Exception e) {
			fail("Exception during testAsynchronousMoveTo: " + e.getMessage());
		}

		try {
			assertTrue(arrayCompare((double[]) theScannable.getPosition(), outputNewPosition));
		} catch (DeviceException e) {
			fail("Exception thrown: " + e.getMessage());
		}
	}

	/**
	 *
	 */
	@Test
	public void testMoveTo() {
		try {
			theScannable.moveTo(newPosition);
		} catch (Exception e) {
			fail("Exception during testMoveTo: " + e.getMessage());
		}
		try {
			assertTrue(arrayCompare((double[]) theScannable.getPosition(), outputNewPosition));
		} catch (DeviceException e) {
			fail("Exception thrown: " + e.getMessage());
		}
	}

	/**
	 *
	 */
	@Test
	public void testIsPositionValid() {
		assertTrue(theScannable.checkPositionValid(newPosition) == null);
		assertTrue(theScannable.checkPositionValid("hello") != null);
		assertFalse(theScannable.checkPositionValid("10.0") == null);
	}

	/**
	 *
	 */
	@Test
	public void testGetNumberSteps() {
		try {
			assertEquals(ScannableUtils.getNumberSteps(theScannable, new double[] { 10.0, 10.0, 10.0 }, new double[] {
					100.0, 100.0, 100.0 }, new double[] { 10.0, 10.0, 10.0 }), 9);
		} catch (Exception e) {
			fail("Exception thrown during testGetNumberSteps: " + e.getMessage());
		}
	}

	/**
	 *
	 */
	@Test
	public void testGetCurrentPositionArray() {
		try {
			assertTrue(arrayCompare(ScannableUtils.getCurrentPositionArray(theScannable), startPosition));
		} catch (Exception e) {
			fail("Exception thrown during testGetCurrentPositionArray: " + e.getMessage());
		}
	}

	/**
	 *
	 */
	@Test
	public void testGetName() {
		assertEquals(theScannable.getName(), "testPD");
	}

	/**
	 *
	 */
	@Test
	public void testSetName() {
		theScannable.setName("newname");
		assertEquals(theScannable.getName(), "newname");
	}

	/**
	 *
	 */
	@Test
	public void testSetLevel() {
		theScannable.setLevel(100);
		assertEquals(theScannable.getLevel(), 100);
	}

	/**
	 *
	 */
	@Test
	public void testGetLevel() {
		assertEquals(theScannable.getLevel(), 5);
	}

	/**
	 *
	 */
	@Test
	public void testGetInputNames() {
		assertEquals(theScannable.getInputNames()[0], "first");
		assertEquals(theScannable.getInputNames()[1], "second");
		assertEquals(theScannable.getInputNames()[2], "third");
	}

	/**
	 *
	 */
	@Test
	public void testGetOutputFormat() {
		assertEquals(theScannable.getOutputFormat()[0], "%4.10f");
	}

	/**
	 *
	 */
	@Test
	public void testGetExtraNames() {
		assertEquals(theScannable.getExtraNames()[0], "extra");
	}

	/**
	 *
	 */
	@Test
	public void testSetExtraNames() {
		theScannable.setExtraNames(new String[] { "testing", "testing2" });
		assertEquals(theScannable.getExtraNames()[0], "testing");
		assertEquals(theScannable.getExtraNames()[1], "testing2");
	}

	/**
	 *
	 */
	@Test
	public void testSetInputNames() {
		theScannable.setInputNames(new String[] { "newname" });
		assertEquals(theScannable.getInputNames()[0], "newname");
	}

	/**
	 *
	 */
	@Test
	public void testToString() {
		assertEquals(theScannable.toString(),
				"first : 10.0000000000 second : 20.0000000000 third : 30.0000000000 extra : 10.0000000000");
	}

	/**
	 *
	 */
	@Test
	public void test__call__() throws DeviceException {
		assertTrue(arrayCompare((PyArray) theScannable.__call__(), startPosition));
	}

	/**
	 *
	 */
	@Test
	public void test__call__PyObject() {
		try {
			PyArray pyNewPosition = new PyArray(PyFloat.class, 3);
			pyNewPosition.__setitem__(0, new PyFloat(12));
			pyNewPosition.__setitem__(1, new PyFloat(22));
			pyNewPosition.__setitem__(2, new PyFloat(32));

			theScannable.__call__(pyNewPosition);
		} catch (Exception e) {
			fail("Exception during __call__: " + e.getMessage());
		}
		try {
			assertTrue(arrayCompare((double[]) theScannable.getPosition(), outputNewPosition));
		} catch (DeviceException e) {
			fail("Exception thrown: " + e.getMessage());
		}
	}

	/**
	 *
	 */
	@Test
	public void test__len__() {
		assertEquals(theScannable.__len__(), 3);
	}

	/**
	 *
	 */
	@Test
	public void test__getitem__() {
		PyObject item = theScannable.__getitem__(new PyInteger(0));
		assertEquals(item.toString(), "10.0");
	}

	/**
	 *
	 */
	@Test
	public void test__str__() {
		assertEquals(theScannable.__str__(), new PyString(
				"first : 10.0000000000 second : 20.0000000000 third : 30.0000000000 extra : 10.0000000000"));
	}

	/**
	 *
	 */
	@Test
	public void test__repr__() {
		assertEquals(theScannable.__repr__(), new PyString(
				"first : 10.0000000000 second : 20.0000000000 third : 30.0000000000 extra : 10.0000000000"));
	}

	/**
	 *
	 */
	@Test
	public void testValidatePositionObject() {
		try {
			theScannable.checkPositionValid(new double[] { 10, 20, 30 });
			theScannable.checkPositionValid(new Double[] { 10., 20., 30. });
		} catch (Exception e) {
			fail("Failure during testValidatePositionObject: " + e.getMessage());
		}
	}
}