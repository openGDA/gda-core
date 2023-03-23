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

import static gda.device.scannable.PositionConvertorFunctions.toObjectArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import gda.device.DeviceException;
import gda.device.ScannableMotion;
import gda.device.scannable.component.PositionValidator;

class ScannableMotionBaseTest extends ScannableBaseTest {

	static class TestableScannableMotionBase extends ScannableMotionBase implements Testable {

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

	private TestableScannableMotionBase scannable;

	@Override
	public ScannableBase getSB() {
		return scannable;
	}

	public ScannableMotion getSM() {
		return scannable;
	}

	public ScannableMotionBase getSMB() {
		return scannable;
	}

	@Override
	public ScannableBase getDelegate() {
		return scannable.delegate;
	}

	@Override
	public void createScannableToTest() {
		scannable = new TestableScannableMotionBase();
	}

	@Override
	protected void configureTwoInputFields() throws DeviceException {
		getSB().setInputNames(new String[] { "i1", "i2" });
		getSB().setName("name");
		when(getSMB().rawGetPosition()).thenReturn(new Double[] { 1., 2. });
		// Note that an outputFormat has not been set. The default is checked in testDefaultValues
	}

	@Override
	protected void configureOneInputField() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		getSB().setName("name");
		when(getSMB().rawGetPosition()).thenReturn(new Double[] { 1. });
		// Note that an outputFormat has not been set. The default is checked in testDefaultValues
	}

	@Override
	protected void configureTwoExtraFields() throws DeviceException {
		getSB().setExtraNames(new String[] { "e1", "e2" });
		when(getSMB().rawGetPosition()).thenReturn(new Double[] { 1., 2., 3., 4. });
		// Note that an outputFormat has not been set. The default is checked in testDefaultValues
	}

	@Override
	@Test
	public void testDefaultValues() throws DeviceException {
		super.testDefaultValues();
		assertThat(getSB().getLevel(), is(5));
		assertThat(getSM().getNumberTries(), is(1));
		assertNull(getSM().getTolerances());
	}

	@Test
	void testDefaultGdaLimits() {
		assertThat(getSM().getLowerGdaLimits(), is(nullValue()));
		assertThat(getSM().getUpperGdaLimits(), is(nullValue()));
	}

	// TEST LIMIT SETTERS
	// Set with Double
	@Test
	void testSetGetGdaLimitsDouble() throws Exception {
		getSM().setInputNames(new String[] { "i" });
		getSM().setLowerGdaLimits(1.);
		getSM().setUpperGdaLimits(2.);
		assertThat(getSM().getLowerGdaLimits(), is(equalTo(new Double[] { 1. })));
		assertThat(getSM().getUpperGdaLimits(), is(equalTo(new Double[] { 2. })));
	}

	@Test
	void testClearGdaLimitsDouble() throws Exception {
		getSM().setInputNames(new String[] { "i" });
		Double nullDouble = null;
		getSM().setLowerGdaLimits(nullDouble);
		getSM().setUpperGdaLimits(nullDouble);
		assertThat(getSM().getLowerGdaLimits(), is(nullValue()));
		assertThat(getSM().getUpperGdaLimits(), is(nullValue()));
	}

	@Test
	void testSetUpperGdaLimitsDoubleException() throws Exception {
		assertThrows(Exception.class, () -> getSM().setUpperGdaLimits(1.)); // Two inputs expected
	}

	@Test
	void testSetLowerGdaLimitsDoubleException() throws Exception {
		assertThrows(Exception.class, () -> getSM().setLowerGdaLimits(1.)); // Two inputs expected
	}

	// Set with array
	@Test
	void testSetGetGdaLimitsArray() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { 1., 2. });
		getSM().setUpperGdaLimits(new Double[] { 3., 4. });
		assertThat(getSM().getLowerGdaLimits(), is(equalTo(new Double[] { 1., 2. })));
		assertThat(getSM().getUpperGdaLimits(), is(equalTo(new Double[] { 3., 4. })));
	}

	@Test
	void testSetGetGdaLimitsArrayWithNulls() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { 1., null });
		getSM().setUpperGdaLimits(new Double[] { null, 4. });
		assertThat(getSM().getLowerGdaLimits(), is(equalTo(new Double[] { 1., null })));
		assertThat(getSM().getUpperGdaLimits(), is(equalTo(new Double[] { null, 4. })));
	}

	@Test
	void testSetGetGdaLimitsArrayWithExtraNames() throws Exception {
		configureTwoExtraFields();
		testSetGetGdaLimitsArray();
	}

	@Test
	void testClearGdaLimitsArray() throws Exception {
		Double[] nullArray = null;
		getSM().setLowerGdaLimits(nullArray);
		getSM().setUpperGdaLimits(nullArray);
		assertThat(getSM().getLowerGdaLimits(), is(nullValue()));
		assertThat(getSM().getUpperGdaLimits(), is(nullValue()));
	}

	@Test
	void testSetUpperGdaLimitsArrayException() throws Exception {
		assertThrows(Exception.class, () -> getSM().setUpperGdaLimits(new Double[] { 1. }));
	}

	@Test
	void testSetLowerGdaLimitsArrayException() throws Exception {
		assertThrows(Exception.class, () -> getSM().setLowerGdaLimits(new Double[] { 1., 2., 3. }));
	}

	// TEST LIMIT CHECKING
	// Set with Double
	public void testCheckPositionWithNoLimitsSet() throws DeviceException {
		assertThat(getSM().checkPositionValid(1.5), is(nullValue()));
		assertThat(getSM().checkPositionWithinGdaLimits(1.5), is(nullValue()));
		assertThat(getSM().checkPositionValid(1.5), is(nullValue()));
		assertThat(getSM().checkPositionValid("string"), is(nullValue()));
		assertThat(getSM().checkPositionWithinGdaLimits("string"), is(nullValue()));
		assertThat(getSM().checkPositionValid("string"), is(nullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { 1.5, null }), is(nullValue()));
		assertThat(getSM().checkPositionWithinGdaLimits(new Double[] { 1.5, null }), is(nullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { 1.5, null }), is(nullValue()));
	}

	@Test
	void testCheckPositionWithinLimitsDouble() throws Exception {
		getSM().setInputNames(new String[] { "i" });
		getSM().setLowerGdaLimits(1.);
		getSM().setUpperGdaLimits(2.);
		assertThat(getSM().checkPositionValid(1.5), is(nullValue()));
		assertThat(getSM().checkPositionWithinGdaLimits(1.5), is(nullValue()));
		assertThat(getSM().checkPositionValid(1.5), is(nullValue()));
		getSM().asynchronousMoveTo(1.5);
	}

	@Test
	void testCheckPositionWithinOneSidedLimitsDouble() throws Exception {
		getSM().setInputNames(new String[] { "i" });
		getSM().setLowerGdaLimits(1.);
		assertThat(getSM().checkPositionValid(2.5), is(nullValue()));
		assertThat(getSM().checkPositionWithinGdaLimits(2.5), is(nullValue()));
		assertThat(getSM().checkPositionValid(2.5), is(nullValue()));
		getSM().asynchronousMoveTo(2.5);

		Double nullDouble = null;
		getSM().setLowerGdaLimits(nullDouble);
		getSM().setUpperGdaLimits(2.);
		assertThat(getSM().checkPositionValid(.5), is(nullValue()));
		assertThat(getSM().checkPositionWithinGdaLimits(.5), is(nullValue()));
		assertThat(getSM().checkPositionValid(.5), is(nullValue()));
		getSM().asynchronousMoveTo(.5);
	}

	@Test
	void testCheckPositionViolationLimitsDouble() throws Exception {
		getSM().setInputNames(new String[] { "i" });
		getSM().setLowerGdaLimits(1.);
		getSM().setUpperGdaLimits(2.);
		// upper
		assertThat(getSM().checkPositionValid(2.5),
				is(equalTo("Scannable limit violation on name.i: 2.5 > 2.0 (internal/hardware/dial values).")));
		// lower
		assertThat(getSM().checkPositionValid(.5),
				is(equalTo("Scannable limit violation on name.i: 0.5 < 1.0 (internal/hardware/dial values).")));
	}

	@Test
	void testCheckPositionViolationOneSidedLimitsDouble() throws Exception {
		getSM().setInputNames(new String[] { "i" });
		getSM().setLowerGdaLimits(1.);
		assertThat(getSM().checkPositionValid(.5), is(notNullValue()));
		assertThat(getSM().checkPositionWithinGdaLimits(.5), is(notNullValue()));
		assertThat(getSM().checkPositionValid(.5), is(notNullValue()));

		Double nullDouble = null;
		getSM().setLowerGdaLimits(nullDouble);
		getSM().setUpperGdaLimits(2.);
		assertThat(getSM().checkPositionValid(2.5), is(notNullValue()));
		assertThat(getSM().checkPositionWithinGdaLimits(2.5), is(notNullValue()));
		assertThat(getSM().checkPositionValid(2.5), is(notNullValue()));
	}

	// Set with Double arrays
	@Test
	void testCheckPositionWithinLimitsDoubleArray() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { -1., -2. });
		getSM().setUpperGdaLimits(new Double[] { 1., null });
		assertThat(getSM().checkPositionValid(new Double[] { 0., 0. }), is(nullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { 0., null }), is(nullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { null, null }), is(nullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { 0., 999. }), is(nullValue()));
	}

	@Test
	void testCheckPositionWithinOneSidedLimitsDoubleArray() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { -1., null });
		assertThat(getSM().checkPositionValid(new Double[] { 0., 0. }), is(nullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { 0., null }),  is(nullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { null, null }), is(nullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { 0., -999. }), is(nullValue()));

		Double[] nullDoubleArray = null;
		getSM().setLowerGdaLimits(nullDoubleArray);
		getSM().setUpperGdaLimits(new Double[] { 1., null });
		assertThat(getSM().checkPositionValid(new Double[] { 0., 0. }), is(nullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { 0., null }), is(nullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { null, null }), is(nullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { 0., 999. }), is(nullValue()));
	}

	@Test
	void testCheckPositionViolationLimitsDoubleArray() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { -1., -2. });
		getSM().setUpperGdaLimits(new Double[] { 1., null });
		assertThat(getSM().checkPositionValid(new Double[] { 0., -2.1 }), is(notNullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { -1.1, null }), is(notNullValue()));
		assertThat(getSM().checkPositionValid(new Double[] { 1.1, 999. }), is(notNullValue()));
	}

	@Test
	void testAddPositionValidator() {
		assertThat(getSMB().getAdditionalPositionValidators().isEmpty(), is(true));
		PositionValidator v1 = mock(PositionValidator.class);
		PositionValidator v2 = mock(PositionValidator.class);
		getSMB().addPositionValidator(v1);
		getSMB().addPositionValidator(v2);
		assertThat(getSMB().getAdditionalPositionValidators().size(), is(2));
		assertThat(getSMB().getAdditionalPositionValidators(), containsInAnyOrder(v1, v2));
	}

	@Test
	void testCheckWithAdditionalValidators() throws DeviceException {
		TestableScannableMotionBase scannable = new TestableScannableMotionBase(); // varargs fail with mocks
		scannable.setInputNames(new String[] { "i1", "i2" });
		scannable.setName("name");
		scannable.setOffset(1., 2.);
		PositionValidator v1 = mock(PositionValidator.class);
		PositionValidator v2 = mock(PositionValidator.class);
		scannable.addPositionValidator(v1);
		scannable.addPositionValidator(v2);
		when(v1.checkInternalPosition(new Double[] { -1., -2. })).thenReturn(null);
		when(v2.checkInternalPosition(new Double[] { -1., -2. })).thenReturn("check v2 failed");
		assertThat(scannable.checkPositionValid(new Double[] { 0., 0. }), is(equalTo("check v2 failed")));
		verify(v1).checkInternalPosition(new Double[] { -1., -2. });
		verify(v2).checkInternalPosition(new Double[] { -1., -2. });
	}

	// TEST OTHER
	@Test
	void testSetGetNumberTries() {
		getSM().setNumberTries(3);
		assertThat(getSM().getNumberTries(), is(3));
	}

	@Test
	void testSetGetTolerance() throws DeviceException {
		configureTwoExtraFields();
		getSM().setTolerances(new Double[] { 1., 2. });
		assertThat(getSM().getTolerances(), is(equalTo(new Double[] { 1., 2. })));
	}

	@Test
	void testDefaultToleranceWithExtraFields() throws DeviceException {
		configureTwoExtraFields();
		assertThat(getSM().getTolerances(), is(nullValue()));
	}

	@Test
	@Override
	public void testIsAtWithArrays() throws DeviceException {
		getSB().asynchronousMoveTo(new double[] { 1., 2. });
		assertThat(getSB().isAt(new Double[] { 1., 2. }), is(true));
		assertThat(getSB().isAt(new Double[] { 1., 2.1 }), is(false));
		getSM().setTolerances(new Double[] { .01, .01 });
		assertThat(getSB().isAt(new Double[] { 1., 2. }), is(true));
		assertThat(getSB().isAt(new Double[] { 1., 2.1 }), is(false));
	}

	@Test
	@Override
	public void testIsAtWithArraysWithExtraNames() throws DeviceException {
		configureTwoExtraFields();
		getSB().asynchronousMoveTo(new double[] { 1., 2. });
		assertThat(getSB().isAt(new Double[] { 1., 2. }), is(true));
		assertThat(getSB().isAt(new Double[] { 1., 2.1 }), is(false));
		getSM().setTolerances(new Double[] { .01, .01 });
		assertThat(getSB().isAt(new Double[] { 1., 2. }), is(true));
		assertThat(getSB().isAt(new Double[] { 1., 2.1 }), is(false));
	}

	@Override
	@Test
	public void testIsAtWithIntegerArrays() throws DeviceException {
		getSB().asynchronousMoveTo(new int[] { 1, 2 });
		assertThat(getSB().isAt(new double[] { 1, 2 }), is(true));
		assertThat(getSB().isAt(new double[] { 11, 21 }), is(false));
		getSM().setTolerances(new Double[] { .01, .01 });
		assertThat(getSB().isAt(new double[] { 1, 2 }), is(true));
		assertThat(getSB().isAt(new double[] { 11, 21 }), is(false));
	}

	@Test
	void testIsAtWithNoToleranceSet() throws DeviceException {
		configureTwoExtraFields();
		getSB().asynchronousMoveTo(new double[] { 1., 2. });
		assertThat(getSB().isAt(new double[] { 1., 2. }), is(true));
		assertThat(getSB().isAt(new double[] { 999, 999 }), is(false));
	}

	@Test
	void testAsynchronousMoveToDoubleArray() throws DeviceException {
		getSB().asynchronousMoveTo(new Double[] { 1.1, 2.1 });
		verify(getDelegate()).rawAsynchronousMoveTo(new Double[] { 1.1, 2.1 });
	}

	@Test
	void testAsynchronousMoveDouble() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		getSB().asynchronousMoveTo(3.);
		verify(getDelegate()).rawAsynchronousMoveTo(3.);
	}

	@Test
	void testAsynchronousMoveToString() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		getSB().asynchronousMoveTo("string");
		verify(getDelegate()).rawAsynchronousMoveTo("string");
	}

	@Test
	void testAsynchronousMoveObject() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		Object object = new Object();
		getSB().asynchronousMoveTo(object);
		verify(getDelegate()).rawAsynchronousMoveTo(object);
	}

	@Test
	void testAsynchronousMoveWithOffset() throws DeviceException {
		scannable = new TestableScannableMotionBase();
		scannable.setInputNames(new String[] { "i1" });
		scannable.setOffset(10.);
		scannable.setScalingFactor(2.);
		assertThat(scannable.externalToInternal(100.), is(equalTo(45.)));
		scannable.asynchronousMoveTo(100.);
		// verify(getSMB()).rawAsynchronousMoveTo(45.); Mockito fails with STUB, manually verified on March 22 2010
	}

	@Test
	void testGetPositionWithOffset() {
		scannable = new TestableScannableMotionBase();
		scannable.setInputNames(new String[] { "i1" });
		scannable.setOffset(10.);
		scannable.setScalingFactor(2.);
		assertThat(scannable.internalToExternal(45.), is(equalTo(100.)));
		// TODO Replace test when no longer using a spy
	}

	@Test
	void testGetPositionDoubleArray() throws DeviceException {
		when(getSMB().rawGetPosition()).thenReturn(new Double[] { 1.1, 2.1 });
		assertThat(toObjectArray(getSB().getPosition()), is(equalTo(new Double[] { 1.1, 2.1 })));
	}

	@Test
	void testGetPositionDouble() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		when(getSMB().rawGetPosition()).thenReturn(3.);
		assertThat(getSB().getPosition(), is(equalTo(3.)));
	}

	@Test
	void testGetPositionString() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		when(getSMB().rawGetPosition()).thenReturn("string");
		assertThat(getSB().getPosition(), is(equalTo("string")));
	}

	@Test
	void testGetPositionObject() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		Object object = new Object();
		when(getSMB().rawGetPosition()).thenReturn(object);
		assertThat(getSB().getPosition(), is(object));
	}

	@Test
	void testA() throws DeviceException {
		getSM().a(new Double[] { 1., null });
		verify(getDelegate()).rawAsynchronousMoveTo(new Double[] { 1., null });
	}

	@Test
	void testAr() throws DeviceException {
		when(getSM().getPosition()).thenReturn(new Double[] { 1., 2. });
		getSM().ar(new Double[] { .1, .2 });
		verify(getDelegate()).rawAsynchronousMoveTo(new Double[] { 1.1, 2.2 });
	}

	@Test
	void testGetAttribute() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { 1., 2. });
		getSM().setUpperGdaLimits(new Double[] { 3., 4. });
		getSM().setTolerances(new Double[] { .1, .2 });
		getSM().setNumberTries(99);

		assertThat(getSM().getAttribute("lowerGdaLimits"), is(equalTo(new Double[] { 1., 2. })));
		assertThat(getSM().getAttribute("upperGdaLimits"), is(equalTo(new Double[] { 3., 4. })));
		assertThat(getSM().getAttribute("tolerance"), is(equalTo(new Double[] { .1, .2 })));
		assertThat(getSM().getAttribute("tolerance"), is(equalTo(new Double[] { .1, .2 })));
		assertThat(getSM().getAttribute("numberTries()"), is(99));
		assertThat(getSMB().getAttribute(ScannableMotion.FIRSTINPUTLIMITS), is(equalTo(new Double[] { 1., 3. })));
		getSM().setLowerGdaLimits(new Double[] { null, 2. });
		assertThat(getSMB().getAttribute(ScannableMotion.FIRSTINPUTLIMITS), is(equalTo(new Double[] {null, 3. })));
		Double[] nullDoubleArray = null;
		getSM().setUpperGdaLimits(nullDoubleArray);
		assertThat(getSMB().getAttribute(ScannableMotion.FIRSTINPUTLIMITS), is(nullValue()));
		assertThat(getSMB().getFirstInputLimits(), is(nullValue()));
	}

	@Test
	void testMoveToWithDefaultNoRetries() throws DeviceException {
		getSM().setTolerances(new Double[] { .1, .1 });
		when(getSM().isBusy()).thenReturn(false);
		getSM().moveTo(new Double[] { 10., 20. });
		verify(getDelegate(), never()).rawGetPosition();
		verify(getDelegate(), times(1)).rawAsynchronousMoveTo(new Double[] { 10., 20. });
	}

	@Test
	void testMoveToWithGoodScannableAndNoRetries() throws DeviceException {
		getSM().setNumberTries(1);
		getSM().setTolerances(new Double[] { .1, .1 });
		when(getSM().isBusy()).thenReturn(false);
		when(getSMB().rawGetPosition()).thenReturn(new double[] { 11., 21. }, new double[] { 10., 20. });
		getSM().moveTo(new Double[] { 10., 20. });
		verify(getDelegate(), times(0)).rawGetPosition();
		verify(getDelegate(), times(1)).rawAsynchronousMoveTo(new Double[] { 10., 20. });
	}

	@Test
	void testMoveToWithOneRetry() throws DeviceException {
		getSM().setNumberTries(2);
		getSM().setTolerances(new Double[] { .1, .1 });
		when(getSM().isBusy()).thenReturn(false);
		when(getSMB().rawGetPosition()).thenReturn(new double[] { 11., 21. }, new double[] { 10., 20. });
		getSM().moveTo(new Double[] { 10., 20. });
		verify(getDelegate(), times(2)).rawGetPosition();
		verify(getDelegate(), times(2)).rawAsynchronousMoveTo(new Double[] { 10., 20. });
	}

	@Test
	void testMoveToWithRetryWhenAlreadyThere() throws DeviceException {
		getSM().setNumberTries(1);
		getSM().setTolerances(new Double[] { .1, .1 });
		when(getSMB().rawGetPosition()).thenReturn(new double[] { 10., 20. });
		getSM().moveTo(new double[] { 10., 20. });
		verify(getDelegate(), never()).rawGetPosition();
		verify(getDelegate(), times(1)).rawAsynchronousMoveTo(any());
	}

	@Test
	void testGetPositionWithOffsetAndExtraFields() throws DeviceException {
		configureTwoExtraFields();
		getSMB().setOffset(.1, .2, .3, .4);
		when(getDelegate().rawGetPosition()).thenReturn(new Double[] { 1., 2., 3., 4. });
		assertThat(getSB().getPosition(), is(equalTo(new Double[] { 1.1, 2.2, 3.3, 4.4 })));
	}

	@Test
	void testGetPositionWithOffsetOnInputFieldsOnlyAndExtraFields() throws DeviceException {
		configureTwoExtraFields();
		getSMB().setOffset(.1, .2);
		when(getDelegate().rawGetPosition()).thenReturn(new Double[] { 1., 2., 3., 4. });
		assertThat(getSB().getPosition(), is(equalTo(new Double[] { 1.1, 2.2, 3., 4. }))); // debug
	}

	@Test
	void testToStringWithOffsets() throws DeviceException {
		configureTwoExtraFields();
		getSMB().setOffset(null, .2);
		when(getDelegate().rawGetPosition()).thenReturn(new Double[] { 1., 2., 3., 4. });
		getSB().setOutputFormat(new String[] { "%1.2g", "%1.2g", "%1.3g", "%1.4g" });
		assertThat(getSB().toFormattedString(), is(equalTo("name : i1: 1.0 i2: 2.2(+0.20) e1: 3.00 e2: 4.000")));
	}

	@Test
	void testToStringWithOffsetsSingleInput() throws DeviceException {
		configureOneInputField();
		getSMB().setOffset(.1);
		when(getDelegate().rawGetPosition()).thenReturn(new Double[] { 1. });
		getSB().setOutputFormat(new String[] { "%1.2g" });
		assertThat(getSB().toFormattedString(), is(equalTo("name : i1: 1.1(+0.10)")));
	}

	@Test
	void testSetGetLimitsWithNegativeScalingFactor_SingleField() throws Exception {
		configureOneInputField();
		getSM().setScalingFactor(-1.);
		getSM().setLowerGdaLimits(-1000.);
		getSM().setUpperGdaLimits(800.);
		assertThat(getSM().getLowerGdaLimits()[0], is(equalTo(-1000.)));
		assertThat(getSM().getUpperGdaLimits()[0], is(equalTo(800.)));
	}

	@Test
	void testCheckPositionWithNegativeScalingFactor_SingleField() throws Exception {
		configureOneInputField();
		getSM().setScalingFactor(-1.);
		getSM().setLowerGdaLimits(-1000.);
		getSM().setUpperGdaLimits(800.);

		assertThat(getSM().checkPositionValid(0.), is(nullValue()));// micron
		assertThat(getSM().checkPositionValid(-999), is(nullValue()));// micron
		assertThat(getSM().checkPositionValid(799), is(nullValue()));// micron
		assertThat(getSM().checkPositionValid(-1001),
				is(equalTo("Scannable limit violation on name.i1: 1001.0 > 1000.0 (internal/hardware/dial values).")));// micron
		assertThat(getSM().checkPositionValid(801),
				is(equalTo("Scannable limit violation on name.i1: -801.0 < -800.0 (internal/hardware/dial values).")));// micron
	}

	@Test
	void testSetGetLimitsWithScalingFactor_ThreeFields() throws Exception {
		getSB().setInputNames(new String[] { "i1", "i2", "i3" });
		getSB().setName("name");

		getSM().setScalingFactor(new Double[] { null, 1., -1. });
		getSM().setLowerGdaLimits(new Double[] { -1000., -1000., -1000. });
		getSM().setUpperGdaLimits(new Double[] { 800., 800., 800. });
		assertThat(getSM().getLowerGdaLimits(), is(equalTo(new Double[] { -1000., -1000., -1000. })));
		assertThat(getSM().getUpperGdaLimits(), is(equalTo(new Double[] { 800., 800., 800. })));
	}

	@Test
	void testCheckPositionWithScalingFactor_ThreeFields() throws Exception {
		getSB().setInputNames(new String[] { "i1", "i2", "i3" });
		getSB().setName("name");
		getSM().setScalingFactor(new Double[] { null, 1., -1. });
		getSM().setLowerGdaLimits(new Double[] { -1000., -1000., -1000. });
		getSM().setUpperGdaLimits(new Double[] { 800., 800., 800. });

		assertThat(getSM().checkPositionValid(new Double[] { 0., 0., 0. }), is(nullValue()));// micron
		assertThat(getSM().checkPositionValid(new Double[] { -999., -999., -999. }), is(nullValue()));// micron
		assertThat(getSM().checkPositionValid(new Double[] { 799., 799., 799. }), is(nullValue()));// micron

		assertThat(getSM().checkPositionValid(new Double[] { -1100., 0., 0. }), // micron
				is(equalTo("Scannable limit violation on name.i1: -1100.0 < -1000.0 (internal/hardware/dial values).")));
		assertThat(getSM().checkPositionValid(new Double[] { 900., 0., 0. }), // micron
				is(equalTo("Scannable limit violation on name.i1: 900.0 > 800.0 (internal/hardware/dial values).")));
		assertThat(getSM().checkPositionValid(new Double[] { 0., -1100., 0. }),// micron
				is(equalTo("Scannable limit violation on name.i2: -1100.0 < -1000.0 (internal/hardware/dial values).")));
		assertThat(getSM().checkPositionValid(new Double[] { 0., 900., 0. }),// micron
				is(equalTo("Scannable limit violation on name.i2: 900.0 > 800.0 (internal/hardware/dial values).")));
		assertThat(getSM().checkPositionValid(new Double[] { 0., 0., -1100. }),// micron
				is(equalTo("Scannable limit violation on name.i3: 1100.0 > 1000.0 (internal/hardware/dial values).")));
		assertThat(getSM().checkPositionValid(new Double[] { 0., 0., 900. }),// micron
				is(equalTo("Scannable limit violation on name.i3: -900.0 < -800.0 (internal/hardware/dial values).")));
	}
}
