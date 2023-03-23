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
import static gda.device.scannable.PositionConvertorFunctions.toIntegerArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PySlice;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.commands.ScannableCommands;

class ScannableBaseTest {

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

	@BeforeEach
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
	void testIsBusy() throws DeviceException {
		// technically tests the spying method!
		when(getSB().isBusy()).thenReturn(true, false);
		assertThat(getSB().isBusy(), is(true));
		assertThat(getSB().isBusy(), is(false));
		assertThat(getSB().isBusy(), is(false)); // reads last
		assertThat(getSB().isBusy(), is(false)); // reads last
	}

	@Test
	void test__call__PyObject() throws DeviceException {
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
	void testSetGetName() {
		getSB().setName("newname");
		assertThat(getSB().getName(), is(Matchers.equalTo("newname")));
	}

	@Test
	void testSetGetLevel() {
		getSB().setLevel(100);
		assertThat(getSB().getLevel(), is(100));
	}

	@SuppressWarnings("unused")
	public void testDefaultValues() throws DeviceException {
		createScannableToTest();
		assertThat(getSB().getInputNames(), is(arrayContaining("value")));
		assertThat(getSB().getOutputFormat(), is(arrayContaining("%5.5g")));
		assertThat(getSB().getExtraNames(), is(emptyArray()));
		assertThat(getSB().getLevel(), is(5));
	}

	@Test
	void testSetGetExtraNames() {
		getSB().setExtraNames(new String[] { "newe1" });
		assertThat(getSB().getExtraNames(), is(arrayContaining("newe1")));
	}

	@Test
	void testSetGetInputNames() {
		getSB().setInputNames(new String[] { "newi1" });
		assertThat(getSB().getInputNames(), is(arrayContaining("newi1")));
	}

	@Test
	void testIsPositionValid() throws DeviceException {
		getSB().setInputNames(new String[] { "newi1" });
		assertThat(getSB().checkPositionValid(1), is(nullValue()));
	}

	@Test
	void test__call__() throws DeviceException {
		when(getSB().getPosition()).thenReturn( new Double[] { 1., 2. } );
		assertThat(toDoubleArray(getSB().__call__()), is(equalTo(new Double[] { 1., 2. })));
	}

	@Test
	void testPrimitive__call__() throws DeviceException {
		when(getSB().getPosition()).thenReturn( new double[] { 1., 2. } );
		assertThat(toDoubleArray(getSB().__call__()), is(equalTo(new Double[] { 1., 2. })));
		when(getSB().getPosition()).thenReturn( new int[] { 1, 2 } );
		assertThat(toIntegerArray(getSB().__call__()), is(equalTo(new Integer[] { 1, 2 })));
	}

	@Test
	void test__getitem__() throws DeviceException {
		when(getSB().getPosition()).thenReturn( new Double[] { 1., 2. } );
		assertThat(getSB().__getitem__(new PyInteger(0)), is(equalTo(new PyFloat(1.))));
	}

	@Test
	void test__getitem__Slice() throws DeviceException {
		when(getSB().getPosition()).thenReturn( new Double[] { 0., 1. } );
		PyFloat[] values = { new PyFloat(0.), new PyFloat(1.) };
		PyList expected = new PyList(values);
		PySlice slice = new PySlice( new PyInteger(0), new PyInteger(2), new PyInteger(1) );
		assertThat(getSB().__getitem__(slice), is(equalTo(expected)));
	}

	@Test
	void test__getitem__SliceWithNone() throws DeviceException {
		when(getSB().getPosition()).thenReturn( new Double[] { 0., 1. } );
		PyFloat[] values = { new PyFloat(0.), new PyFloat(1.) };
		PyList expected = new PyList(values);
		PySlice slice = new PySlice();
		assertThat(getSB().__getitem__(slice), is(equalTo(expected)));
	}

	@Test
	void test__getitem__OutOfRange() throws DeviceException {
		//might fail when run on its own, as Py.IndexError is sometimes null when it's added to the PyException
		when(getSB().getPosition()).thenReturn( new Double[] { 0., 1.} );
		PyException e = assertThrows(PyException.class, () -> getSB().__getitem__(new PyInteger(2)),
				"Should throw Python IndexError exception");
		assertThat(e.type, is(equalTo(Py.IndexError)));
	}

	private static final String REPR = "name : i1: 1.0000 i2: 2.0000 e1: 3.0000 e2: 4.0000";

//	@Test
//	public void testGetPositionWithExtraNames() throws DeviceException {
//		configureTwoExtraFields();
//		assertArrayEquals(new Double[] { 1., 2., 3., 4. }, (Object[]) getSB().getPosition());
//	}

	@Test
	void test__len__() throws DeviceException {
		configureTwoExtraFields();
		assertThat(getSB().__len__(), is(2)); // Returns only the length of the input names
	}

	@Test
	void testToStringWithIncorrectOutputFormatSet() throws DeviceException {
		configureTwoExtraFields();

		assertThat(REPR, is(equalTo(getSB().toFormattedString())));
	}

	@Test
	void testToString() throws DeviceException {
		configureTwoExtraFields();
		when(getSB().getPosition()).thenReturn( new Double[] { 1., 2., 3., 4. } );
		getSB().setOutputFormat(new String[] { "%1.1g", "%1.2g", "%1.3g", "%1.4g" });
		assertThat(getSB().toFormattedString(), is(equalTo("name : i1: 1 i2: 2.0 e1: 3.00 e2: 4.000")));
	}

	@Test
	void testToStringWithZieScannable() throws DeviceException {
		getSB().setInputNames(new String[]{});
		getSB().setExtraNames(new String[]{});
		getSB().setOutputFormat(new String[]{});
		when(getDelegate().rawGetPosition()).thenReturn(null);
		assertThat(getSB().toFormattedString(), is(equalTo("name : ---")));
	}

	@Test
	void testToStringWithIncorrectOutputFormatSetSingleField() throws DeviceException {
		createScannableToTest();
		configureOneInputField();
		getSB().setOutputFormat(new String[] { });
		assertThat(getSB().toFormattedString(), is(equalTo("name : i1: 1.0")));
	}

	@Test
	void testToStringSingleField() throws DeviceException {
		createScannableToTest();
		configureOneInputField();
		getSB().setOutputFormat(new String[] { "%1.4g" });
		assertThat(getSB().toFormattedString(), is(equalTo("name : i1: 1.000")));
	}

	@Test
	void testToStringSingleFieldMatchingName() throws DeviceException {
		createScannableToTest();
		configureOneInputField();
		getSB().setInputNames(new String[] {"name"});
		getSB().setOutputFormat(new String[] { "%1.4g" });
		assertThat(getSB().toFormattedString(), is(equalTo("name : 1.000")));
	}

	@Test
	void test__str__() throws DeviceException {
		configureTwoExtraFields();
		assertThat(getSB().__str__(), is(equalTo(REPR)));
	}

	@Test
	void test__repr__() throws DeviceException {
		configureTwoExtraFields();
		assertThat(getSB().__repr__(), is(equalTo(REPR)));
	}

	@Test
	void testWaitWhileBusyWhenNotBusy() throws DeviceException, InterruptedException {
		when(getSB().isBusy()).thenReturn(false);
		getSB().waitWhileBusy();
		verify(getDelegate(), times(1)).isBusy();

	}

	@Test
	void testWaitWhileBusyWhenStartingBusy() throws DeviceException, InterruptedException {
		when(getSB().isBusy()).thenReturn(true, false);
		getSB().waitWhileBusy();
		verify(getDelegate(), times(2)).isBusy();
	}

	@Test
	void testWaitWhileBusyWithTimeout() throws DeviceException, InterruptedException {
		when(getSB().isBusy()).thenReturn(true, false);
		getSB().waitWhileBusy(.150);
	}

	@Test
	void testWaitWhileBusyWithTimeoutFailure() throws DeviceException, InterruptedException {
		when(getSB().isBusy()).thenReturn(true, false);
		assertThrows(DeviceException.class, () -> getSB().waitWhileBusy(.050));
	}

	@Test
	void testMoveToWhenStartingBusy() throws DeviceException {
		when(getSB().isBusy()).thenReturn(true, false, false);
		getSB().moveTo(new Double[] {1.,2.});
		verify(getDelegate()).rawAsynchronousMoveTo(new Double[] {1.,2.});
		verify(getDelegate(), times(2)).isBusy();
	}

	@Test
	void testIsAtWithStrings() throws DeviceException {
		getSB().setInputNames(new String[]{"i1"});
		when(getDelegate().rawGetPosition()).thenReturn("open");
		assertThat(getSB().isAt("open"), is(true));
		assertThat(getSB().isAt("closed"), is(false));
	}

	@Test
	void testIsAtWithArrays() throws DeviceException {
		when(getSB().getPosition()).thenReturn(new Double[] {1.,2.});
		assertThat(getSB().isAt(new Double[] {1.,2.}), is(true));
		assertThat(getSB().isAt(new Double[] {1.,2.1}), is(false));
	}
	@Test
	void testIsAtWithArraysWithExtraNames() throws DeviceException {
		getSB().setExtraNames(new String[]{"e1", "e2"});
		when(getDelegate().rawGetPosition()).thenReturn(new Double[] {1.,2.,3.,4.});
		assertThat(getSB().isAt(new Double[] {1.,2.}), is(true));
		assertThat(getSB().isAt(new Double[] {1.,2.1}), is(false));
	}

	@Test
	void testIsAtWithIntegerArrays() throws DeviceException {
		when(getSB().getPosition()).thenReturn(new int[] {1,2});
		assertThat(getSB().isAt(new int[] {1,2}), is(true));
		assertThat(getSB().isAt(new int[] {11,21}), is(false));
	}

	@Test
	void testExternalToInternalWithNoConversionObject() throws DeviceException {

		Object object1 = new Object();
		configureOneInputField();
		assertThat(getSB().externalToInternal(object1), is(object1));
	}

	/**
	 * This test ensures that getPosition is called exactly once per
	 * {@link ScannableCommands#pos(Scannable...)} as
	 * {@code pos} calls {@link Scannable#toFormattedString}.
	 * <p>
	 * {@code getPosition} is a potentially expensive operation.
	 * <p>
	 * For {@link ScannableBase} verifying {@code rawGetPosition} is equivalent.
	 * @throws DeviceException
	 *
	 */
	@Test
	void testToFormattedStringCallsGetPositionOnce() throws DeviceException {
		createScannableToTest();
		getSB().setName("value");
		when(getSB().getPosition()).thenReturn(8776.7);
		ScannableCommands.pos(getSB());
		verify(getDelegate()).rawGetPosition();
	}
}