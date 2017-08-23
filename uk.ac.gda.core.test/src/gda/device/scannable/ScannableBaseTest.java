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

import static gda.device.scannable.PositionConvertorFunctions.toDoubleArray;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PySlice;
import org.python.core.PyString;

import gda.device.DeviceException;

public class ScannableBaseTest {

	public static class TestableScannableBase extends ScannableBase implements Testable{

		public ScannableBase delegate = mock(ScannableBase.class);

		@Override
		public void rawAsynchronousMoveTo(Object position) throws DeviceException {
			delegate.rawAsynchronousMoveTo(position);
		}

		@Override
		public Object rawGetPosition() throws DeviceException {
			return delegate.rawGetPosition();
		}

		@Override
		public boolean isBusy() throws DeviceException {
			return delegate.isBusy();
		}
	}

	private TestableScannableBase scannable;

	/**
	 * Casts scannable to ScannableBase. Allows this test class to be extended.
	 *
	 * @return scannable as ScannableBase
	 */
	public ScannableBase getSB() {
		return scannable;
	}

	public ScannableBase getDelegate() {
		return scannable.delegate;
	}


	// ScannableBase

	@Before
	public void setUp() throws DeviceException {
		createScannableToTest();
		configureTwoInputFields();
	}

	public void createScannableToTest() {
		scannable = new TestableScannableBase();
	}

	protected void configureTwoInputFields() throws DeviceException {
		getSB().setInputNames(new String[] { "i1", "i2" });
		getSB().setName("name");
		when(getSB().getPosition()).thenReturn( new Double[] { 1., 2. } );
		// Note that an outputFormat has not been set. The default is checked in testDefaultValues
	}

	protected void configureOneInputField() throws DeviceException {
		getSB().setName("name");
		when(getSB().getPosition()).thenReturn( new Double[] { 1. } );
		getSB().setInputNames(new String[] { "i1"});
		// Note that an outputFormat has not been set. The default is checked in testDefaultValues
	}

	protected void configureTwoExtraFields() throws DeviceException {
		getSB().setExtraNames(new String[] { "e1", "e2" });
		when(getSB().getPosition()).thenReturn( new Double[] { 1., 2., 3., 4. } );
		// Note that an outputFormat has not been set. The default is checked in testDefaultValues
	}

	// Tests to TestableScannableBase



	@Test
	public void testIsBusy() throws DeviceException {
		// technically tests the spying method!
		when(getSB().isBusy()).thenReturn(true, false);
		assertTrue(getSB().isBusy());
		assertFalse(getSB().isBusy());
		assertFalse(getSB().isBusy()); // reads last
		assertFalse(getSB().isBusy()); // reads last
	}

	@Test
	public void test__call__PyObject() throws DeviceException {
		when(getDelegate().isBusy()).thenReturn(true, false);
		PyList list = new PyList();
		list.add(new PyFloat(1));
		list.add(new PyFloat(2));
		getSB().__call__(list); // moves the thing and returns a string
		verify(getDelegate()).rawAsynchronousMoveTo(list);
		verify(getDelegate(), times(2)).isBusy();
	}



	// Tests to ScannableBase

	@Test
	public void testSetGetName() {
		getSB().setName("newname");
		assertEquals(getSB().getName(), "newname");
	}

	@Test
	public void testSetGetLevel() {
		getSB().setLevel(100);
		assertEquals(getSB().getLevel(), 100);
	}

	@SuppressWarnings("unused")
	public void testDefaultValues() throws DeviceException {
		createScannableToTest();
		assertArrayEquals(getSB().getInputNames(), new String[] { "value" });
		assertArrayEquals(getSB().getOutputFormat(), new String[] { "%5.5g" });
		assertArrayEquals(getSB().getExtraNames(), new String[] {});
		assertEquals(5, getSB().getLevel());
	}





	@Test
	public void testSetGetExtraNames() {
		getSB().setExtraNames(new String[] { "newe1" });
		assertArrayEquals(getSB().getExtraNames(), new String[] { "newe1" });
	}

	@Test
	public void testSetGetInputNames() {
		getSB().setInputNames(new String[] { "newi1" });
		assertArrayEquals(getSB().getInputNames(), new String[] { "newi1" });
	}

	@Test
	public void testIsPositionValid() {
		getSB().setInputNames(new String[] { "newi1" });
		try {
			assertTrue(getSB().checkPositionValid(1) == null);
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void test__call__() throws DeviceException {
		when(getSB().getPosition()).thenReturn( new Double[] { 1., 2. } );
		assertArrayEquals(toDoubleArray(getSB().__call__()), new Double[] { 1., 2. });
	}

	@Test
	public void test__getitem__() throws DeviceException {
		when(getSB().getPosition()).thenReturn( new Double[] { 1., 2. } );
		assertEquals(getSB().__getitem__(new PyInteger(0)), new PyFloat(1.));
	}

	@Test
	public void test__getitem__Slice() throws DeviceException {
		when(getSB().getPosition()).thenReturn( new Double[] { 0., 1. } );
		PyFloat[] values = { new PyFloat(0.), new PyFloat(1.) };
		PyList expected = new PyList(values);
		PySlice slice = new PySlice( new PyInteger(0), new PyInteger(2), new PyInteger(1) );
		assertEquals( expected, getSB().__getitem__(slice) );
	}

	@Test
	public void test__getitem__SliceWithNone() throws DeviceException {
		when(getSB().getPosition()).thenReturn( new Double[] { 0., 1. } );
		PyFloat[] values = { new PyFloat(0.), new PyFloat(1.) };
		PyList expected = new PyList(values);
		PySlice slice = new PySlice();
		assertEquals( expected, getSB().__getitem__(slice) );
	}

	@Test
	public void test__getitem__OutOfRange() throws DeviceException {
		//might fail when run on its own, as Py.IndexError is sometimes null when it's added to the PyException
		when(getSB().getPosition()).thenReturn( new Double[] { 0., 1.} );
		try {
			getSB().__getitem__( new PyInteger(2) );
			fail("Should throw Python IndexError exception");
		} catch (PyException e) {
			assertEquals( Py.IndexError, e.type );
		}
	}

	static String repr = "name : i1: 1.0000 i2: 2.0000 e1: 3.0000 e2: 4.0000";

//	@Test
//	public void testGetPositionWithExtraNames() throws DeviceException {
//		configureTwoExtraFields();
//		assertArrayEquals(new Double[] { 1., 2., 3., 4. }, (Object[]) getSB().getPosition());
//	}

	@Test
	public void test__len__() throws DeviceException {
		configureTwoExtraFields();
		assertEquals(getSB().__len__(), 2); // Returns only the length of the input names
	}

	@Test
	public void testToStringWithIncorrectOutputFormatSet() throws DeviceException {
		configureTwoExtraFields();

		assertEquals(repr, getSB().toFormattedString());
	}

	@Test
	public void testToString() throws DeviceException {
		configureTwoExtraFields();
		when(getSB().getPosition()).thenReturn( new Double[] { 1., 2., 3., 4. } );
		getSB().setOutputFormat(new String[] { "%1.1g", "%1.2g", "%1.3g", "%1.4g" });
		assertEquals("name : i1: 1 i2: 2.0 e1: 3.00 e2: 4.000", getSB().toFormattedString());

	}

	@Test
	public void testToStringWithZieScannable() throws DeviceException {
		getSB().setInputNames(new String[]{});
		getSB().setExtraNames(new String[]{});
		getSB().setOutputFormat(new String[]{});
		when(getDelegate().rawGetPosition()).thenReturn(null);
		assertEquals("name : ---", getSB().toFormattedString());
	}

	@Test
	public void testToStringWithIncorrectOutputFormatSetSingleField() throws DeviceException {
		createScannableToTest();
		configureOneInputField();
		getSB().setOutputFormat(new String[] { });
		assertEquals("name : i1: 1.0", getSB().toFormattedString());
	}

	@Test
	public void testToStringSingleField() throws DeviceException {
		createScannableToTest();
		configureOneInputField();
		getSB().setOutputFormat(new String[] { "%1.4g" });
		assertEquals("name : i1: 1.000", getSB().toFormattedString());
	}

	@Test
	public void testToStringSingleFieldMatchingName() throws DeviceException {
		createScannableToTest();
		configureOneInputField();
		getSB().setInputNames(new String[] {"name"});
		getSB().setOutputFormat(new String[] { "%1.4g" });
		assertEquals("name : 1.000", getSB().toFormattedString());
	}

	@Test
	public void test__str__() throws DeviceException {
		configureTwoExtraFields();
		assertEquals(new PyString(repr), getSB().__str__());
	}

	@Test
	public void test__repr__() throws DeviceException {
		configureTwoExtraFields();
		assertEquals(new PyString(repr), getSB().__repr__());
	}

	@Test
	public void testWaitWhileBusyWhenNotBusy() throws DeviceException, InterruptedException {
		when(getSB().isBusy()).thenReturn(false);
		getSB().waitWhileBusy();
		verify(getDelegate(), times(1)).isBusy();

	}

	@Test
	public void testWaitWhileBusyWhenStartingBusy() throws DeviceException, InterruptedException {
		when(getSB().isBusy()).thenReturn(true, false);
		getSB().waitWhileBusy();
		verify(getDelegate(), times(2)).isBusy();
	}

	@Test
	public void testWaitWhileBusyWithTimeout() throws DeviceException, InterruptedException {
		when(getSB().isBusy()).thenReturn(true, false);
		getSB().waitWhileBusy(.150);
	}

	@Test(expected = DeviceException.class)
	public void testWaitWhileBusyWithTimeoutFailure() throws DeviceException, InterruptedException {
		when(getSB().isBusy()).thenReturn(true, false);
		getSB().waitWhileBusy(.050);
	}

	@Test
	public void testMoveToWhenStartingBusy() throws DeviceException {
		when(getSB().isBusy()).thenReturn(true, false, false);
		getSB().moveTo(new Double[] {1.,2.});
		verify(getDelegate()).rawAsynchronousMoveTo(new Double[] {1.,2.});
		verify(getDelegate(), times(2)).isBusy();
	}

	@Test
	public void testIsAtWithStrings() throws DeviceException {
		getSB().setInputNames(new String[]{"i1"});
		when(getDelegate().rawGetPosition()).thenReturn("open");
		assertTrue(getSB().isAt("open"));
		assertFalse(getSB().isAt("closed"));
	}

	@Test
	public void testIsAtWithArrays() throws DeviceException {
		when(getSB().getPosition()).thenReturn(new Double[] {1.,2.});
		assertTrue(getSB().isAt(new Double[] {1.,2.}));
		assertFalse(getSB().isAt(new Double[] {1.,2.1}));
	}
	@Test
	public void testIsAtWithArraysWithExtraNames() throws DeviceException {
		getSB().setExtraNames(new String[]{"e1", "e2"});
		when(getDelegate().rawGetPosition()).thenReturn(new Double[] {1.,2.,3.,4.});
		assertTrue(getSB().isAt(new Double[] {1.,2.}));
		assertFalse(getSB().isAt(new Double[] {1.,2.1}));
	}

	@Test
	public void testIsAtWithIntegerArrays() throws DeviceException {
		when(getSB().getPosition()).thenReturn(new int[] {1,2});
		assertTrue(getSB().isAt(new int[] {1,2}));
		assertFalse(getSB().isAt(new int[] {11,21}));
	}

	@Test
	public void testExternalToInternalWithNoConversionObject() throws DeviceException {

		Object object1 = new Object();
		configureOneInputField();
		assertEquals(object1, getSB().externalToInternal(object1));
	}
}