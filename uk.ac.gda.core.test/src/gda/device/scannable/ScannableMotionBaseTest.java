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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.device.DeviceException;
import gda.device.ScannableMotion;
import gda.device.scannable.component.PositionValidator;
import junit.framework.Assert;
import junitx.framework.ArrayAssert;

import org.junit.Test;

public class ScannableMotionBaseTest extends ScannableBaseTest {

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
		assertEquals(5, getSB().getLevel());
		assertEquals(1, getSM().getNumberTries());
		assertNull(getSM().getTolerances());
	}

	@Test
	public void testDefaultGdaLimits() {
		ArrayAssert.assertEquals(null, getSM().getLowerGdaLimits());
		ArrayAssert.assertEquals(null, getSM().getUpperGdaLimits());
	}

	// TEST LIMIT SETTERS
	// Set with Double
	@Test
	public void testSetGetGdaLimitsDouble() throws Exception {
		getSM().setInputNames(new String[] { "i" });
		getSM().setLowerGdaLimits(1.);
		getSM().setUpperGdaLimits(2.);
		ArrayAssert.assertEquals(new Double[] { 1. }, getSM().getLowerGdaLimits());
		ArrayAssert.assertEquals(new Double[] { 2. }, getSM().getUpperGdaLimits());
	}

	@Test
	public void testClearGdaLimitsDouble() throws Exception {
		getSM().setInputNames(new String[] { "i" });
		Double nullDouble = null;
		getSM().setLowerGdaLimits(nullDouble);
		getSM().setUpperGdaLimits(nullDouble);
		assertEquals(null, (Object) getSM().getLowerGdaLimits());
		assertEquals(null, (Object) getSM().getUpperGdaLimits());
	}

	@Test(expected = Exception.class)
	public void testSetUpperGdaLimitsDoubleException() throws Exception {
		getSM().setUpperGdaLimits(1.); // Two inputs expected
	}

	@Test(expected = Exception.class)
	public void testSetLowerGdaLimitsDoubleException() throws Exception {
		getSM().setLowerGdaLimits(1.); // Two inputs expected
	}

	// Set with array
	@Test
	public void testSetGetGdaLimitsArray() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { 1., 2. });
		getSM().setUpperGdaLimits(new Double[] { 3., 4. });
		ArrayAssert.assertEquals(new Double[] { 1., 2. }, getSM().getLowerGdaLimits());
		ArrayAssert.assertEquals(new Double[] { 3., 4. }, getSM().getUpperGdaLimits());
	}

	@Test
	public void testSetGetGdaLimitsArrayWithNulls() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { 1., null });
		getSM().setUpperGdaLimits(new Double[] { null, 4. });
		ArrayAssert.assertEquals(new Double[] { 1., null }, getSM().getLowerGdaLimits());
		ArrayAssert.assertEquals(new Double[] { null, 4. }, getSM().getUpperGdaLimits());
	}

	@Test
	public void testSetGetGdaLimitsArrayWithExtraNames() throws Exception {
		configureTwoExtraFields();
		testSetGetGdaLimitsArray();
	}

	@Test
	public void testClearGdaLimitsArray() throws Exception {
		Double[] nullArray = null;
		getSM().setLowerGdaLimits(nullArray);
		getSM().setUpperGdaLimits(nullArray);
		assertEquals(null, (Object) getSM().getLowerGdaLimits());
		assertEquals(null, (Object) getSM().getUpperGdaLimits());
	}

	@Test(expected = Exception.class)
	public void testSetUpperGdaLimitsArrayException() throws Exception {
		getSM().setUpperGdaLimits(new Double[] { 1. });
	}

	@Test(expected = Exception.class)
	public void testSetLowerGdaLimitsArrayException() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { 1., 2., 3. });
	}

	// TEST LIMIT CHECKING
	// Set with Double
	public void testCheckPositionWithNoLimitsSet() throws DeviceException {
		assertNull(getSM().checkPositionValid(1.5));
		assertNull(getSM().checkPositionWithinGdaLimits(1.5));
		assertTrue(getSM().checkPositionValid(1.5) == null);
		assertNull(getSM().checkPositionValid("string"));
		assertNull(getSM().checkPositionWithinGdaLimits("string"));
		assertTrue(getSM().checkPositionValid("string") == null);
		assertNull(getSM().checkPositionValid(new Double[] { 1.5, null }));
		assertNull(getSM().checkPositionWithinGdaLimits(new Double[] { 1.5, null }));
		assertTrue(getSM().checkPositionValid(new Double[] { 1.5, null }) == null);

	}

	@Test
	public void testCheckPositionWithinLimitsDouble() throws Exception {
		getSM().setInputNames(new String[] { "i" });
		getSM().setLowerGdaLimits(1.);
		getSM().setUpperGdaLimits(2.);
		assertNull(getSM().checkPositionValid(1.5));
		assertNull(getSM().checkPositionWithinGdaLimits(1.5));
		assertTrue(getSM().checkPositionValid(1.5) == null);
		getSM().asynchronousMoveTo(1.5);
	}

	@Test
	public void testCheckPositionWithinOneSidedLimitsDouble() throws Exception {
		getSM().setInputNames(new String[] { "i" });
		getSM().setLowerGdaLimits(1.);
		assertNull(getSM().checkPositionValid(2.5));
		assertNull(getSM().checkPositionWithinGdaLimits(2.5));
		assertTrue(getSM().checkPositionValid(2.5) == null);
		getSM().asynchronousMoveTo(2.5);

		Double nullDouble = null;
		getSM().setLowerGdaLimits(nullDouble);
		getSM().setUpperGdaLimits(2.);
		assertNull(getSM().checkPositionValid(.5));
		assertNull(getSM().checkPositionWithinGdaLimits(.5));
		assertTrue(getSM().checkPositionValid(.5) == null);
		getSM().asynchronousMoveTo(.5);
	}

	@Test
	public void testCheckPositionViolationLimitsDouble() throws Exception {
		getSM().setInputNames(new String[] { "i" });
		getSM().setLowerGdaLimits(1.);
		getSM().setUpperGdaLimits(2.);
		// upper
		assertEquals("Scannable limit violation on name.i: 2.5 > 2.0 (internal/hardware/dial values).", getSM()
				.checkPositionValid(2.5));
		assertTrue(getSM().checkPositionValid(2.5) != null);
		// lower
		assertEquals("Scannable limit violation on name.i: 0.5 < 1.0 (internal/hardware/dial values).", getSM()
				.checkPositionValid(.5));
		assertTrue(getSM().checkPositionValid(.5) != null);
	}

	@Test
	public void testCheckPositionViolationOneSidedLimitsDouble() throws Exception {
		getSM().setInputNames(new String[] { "i" });
		getSM().setLowerGdaLimits(1.);
		assertNotNull(getSM().checkPositionValid(.5));
		assertNotNull(getSM().checkPositionWithinGdaLimits(.5));
		assertTrue(getSM().checkPositionValid(.5) != null);

		Double nullDouble = null;
		getSM().setLowerGdaLimits(nullDouble);
		getSM().setUpperGdaLimits(2.);
		assertNotNull(getSM().checkPositionValid(2.5));
		assertNotNull(getSM().checkPositionWithinGdaLimits(2.5));
		assertTrue(getSM().checkPositionValid(2.5) != null);
	}

	// Set with Double arrays
	@Test
	public void testCheckPositionWithinLimitsDoubleArray() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { -1., -2. });
		getSM().setUpperGdaLimits(new Double[] { 1., null });
		assertNull(getSM().checkPositionValid(new Double[] { 0., 0. }));
		assertNull(getSM().checkPositionValid(new Double[] { 0., null }));
		assertNull(getSM().checkPositionValid(new Double[] { null, null }));
		assertNull(getSM().checkPositionValid(new Double[] { 0., 999. }));
	}

	@Test
	public void testCheckPositionWithinOneSidedLimitsDoubleArray() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { -1., null });
		assertNull(getSM().checkPositionValid(new Double[] { 0., 0. }));
		assertNull(getSM().checkPositionValid(new Double[] { 0., null }));
		assertNull(getSM().checkPositionValid(new Double[] { null, null }));
		assertNull(getSM().checkPositionValid(new Double[] { 0., -999. }));

		Double[] nullDoubleArray = null;
		getSM().setLowerGdaLimits(nullDoubleArray);
		getSM().setUpperGdaLimits(new Double[] { 1., null });
		assertNull(getSM().checkPositionValid(new Double[] { 0., 0. }));
		assertNull(getSM().checkPositionValid(new Double[] { 0., null }));
		assertNull(getSM().checkPositionValid(new Double[] { null, null }));
		assertNull(getSM().checkPositionValid(new Double[] { 0., 999. }));

	}

	@Test
	public void testCheckPositionViolationLimitsDoubleArray() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { -1., -2. });
		getSM().setUpperGdaLimits(new Double[] { 1., null });
		assertNotNull(getSM().checkPositionValid(new Double[] { 0., -2.1 }));
		assertNotNull(getSM().checkPositionValid(new Double[] { -1.1, null }));
		assertNotNull(getSM().checkPositionValid(new Double[] { 1.1, 999. }));
	}

	@Test
	public void testAddPositionValidator() {
		assertEquals(0, getSMB().getAdditionalPositionValidators().size());
		PositionValidator v1 = mock(PositionValidator.class);
		PositionValidator v2 = mock(PositionValidator.class);
		getSMB().addPositionValidator(v1);
		getSMB().addPositionValidator(v2);
		assertEquals(2, getSMB().getAdditionalPositionValidators().size());
		assertTrue(getSMB().getAdditionalPositionValidators().contains(v1));
		assertTrue(getSMB().getAdditionalPositionValidators().contains(v2));
	}

	@Test
	public void testCheckWithAdditionalValidators() throws DeviceException {
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
		assertEquals("check v2 failed", scannable.checkPositionValid(new Double[] { 0., 0. }));
		verify(v1).checkInternalPosition(new Double[] { -1., -2. });
		verify(v2).checkInternalPosition(new Double[] { -1., -2. });

	}

	// TEST OTHER
	@Test
	public void testSetGetNumberTries() {
		getSM().setNumberTries(3);
		assertEquals(3, getSM().getNumberTries());
	}

	@Test
	public void testSetGetTolerance() throws DeviceException {
		configureTwoExtraFields();
		getSM().setTolerances(new Double[] { 1., 2. });
		ArrayAssert.assertEquals(new Double[] { 1., 2. }, getSM().getTolerances());
	}

	@Test
	public void testDefaultToleranceWithExtraFields() throws DeviceException {
		configureTwoExtraFields();
		Assert.assertNull(getSM().getTolerances());
	}

	@Test
	@Override
	public void testIsAtWithArrays() throws DeviceException {
		getSB().asynchronousMoveTo(new double[] { 1., 2. });
		assertTrue(getSB().isAt(new Double[] { 1., 2. }));
		assertFalse(getSB().isAt(new Double[] { 1., 2.1 }));
		getSM().setTolerances(new Double[] { .01, .01 });
		assertTrue(getSB().isAt(new Double[] { 1., 2. }));
		assertFalse(getSB().isAt(new Double[] { 1., 2.1 }));
	}

	@Test
	@Override
	public void testIsAtWithArraysWithExtraNames() throws DeviceException {
		configureTwoExtraFields();
		getSB().asynchronousMoveTo(new double[] { 1., 2. });
		assertTrue(getSB().isAt(new Double[] { 1., 2. }));
		assertFalse(getSB().isAt(new Double[] { 1., 2.1 }));
		getSM().setTolerances(new Double[] { .01, .01 });
		assertTrue(getSB().isAt(new Double[] { 1., 2. }));
		assertFalse(getSB().isAt(new Double[] { 1., 2.1 }));
	}

	@Override
	@Test
	public void testIsAtWithIntegerArrays() throws DeviceException {
		getSB().asynchronousMoveTo(new int[] { 1, 2 });
		assertTrue(getSB().isAt(new double[] { 1, 2 }));
		assertFalse(getSB().isAt(new double[] { 11, 21 }));
		getSM().setTolerances(new Double[] { .01, .01 });
		assertTrue(getSB().isAt(new double[] { 1, 2 }));
		assertFalse(getSB().isAt(new double[] { 11, 21 }));
	}

	@Test
	public void testIsAtWithNoToleranceSet() throws DeviceException {
		configureTwoExtraFields();
		getSB().asynchronousMoveTo(new double[] { 1., 2. });
		assertTrue(getSB().isAt(new double[] { 1., 2. }));
		assertFalse(getSB().isAt(new double[] { 999, 999 }));
	}

	@Test
	public void testAsynchronousMoveToDoubleArray() throws DeviceException {
		getSB().asynchronousMoveTo(new Double[] { 1.1, 2.1 });
		verify(getDelegate()).rawAsynchronousMoveTo(new Double[] { 1.1, 2.1 });
	}

	@Test
	public void testAsynchronousMoveDouble() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		getSB().asynchronousMoveTo(3.);
		verify(getDelegate()).rawAsynchronousMoveTo(3.);
	}

	@Test
	public void testAsynchronousMoveToString() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		getSB().asynchronousMoveTo("string");
		verify(getDelegate()).rawAsynchronousMoveTo("string");
	}

	@Test
	public void testAsynchronousMoveObject() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		Object object = new Object();
		getSB().asynchronousMoveTo(object);
		verify(getDelegate()).rawAsynchronousMoveTo(object);
	}

	@Test
	public void testAsynchronousMoveWithOffset() throws DeviceException {
		scannable = new TestableScannableMotionBase();
		scannable.setInputNames(new String[] { "i1" });
		scannable.setOffset(10.);
		scannable.setScalingFactor(2.);
		assertEquals(45., scannable.externalToInternal(100.));
		scannable.asynchronousMoveTo(100.);
		// verify(getSMB()).rawAsynchronousMoveTo(45.); Mockito fails with STUB, manually verified on March 22 2010
	}

	@Test
	public void testGetPositionWithOffset() {
		scannable = new TestableScannableMotionBase();
		scannable.setInputNames(new String[] { "i1" });
		scannable.setOffset(10.);
		scannable.setScalingFactor(2.);
		assertEquals(100., scannable.internalToExternal(45.));
		// TODO Replace test when no longer using a spy
	}

	@Test
	public void testGetPositionDoubleArray() throws DeviceException {
		when(getSMB().rawGetPosition()).thenReturn(new Double[] { 1.1, 2.1 });
		ArrayAssert.assertEquals(new Double[] { 1.1, 2.1 }, toObjectArray(getSB().getPosition()));
	}

	@Test
	public void testGetPositionDouble() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		when(getSMB().rawGetPosition()).thenReturn(3.);
		assertEquals(3., getSB().getPosition());
	}

	@Test
	public void testGetPositionString() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		when(getSMB().rawGetPosition()).thenReturn("string");
		assertEquals("string", getSB().getPosition());
	}

	@Test
	public void testGetPositionObject() throws DeviceException {
		getSB().setInputNames(new String[] { "i1" });
		Object object = new Object();
		when(getSMB().rawGetPosition()).thenReturn(object);
		assertEquals(object, getSB().getPosition());
	}

	@Test
	public void testA() throws DeviceException {
		getSM().a(new Double[] { 1., null });
		verify(getDelegate()).rawAsynchronousMoveTo(new Double[] { 1., null });
	}

	@Test
	public void testAr() throws DeviceException {
		when(getSM().getPosition()).thenReturn(new Double[] { 1., 2. });
		getSM().ar(new Double[] { .1, .2 });
		verify(getDelegate()).rawAsynchronousMoveTo(new Double[] { 1.1, 2.2 });
	}

	@Test
	public void testGetAttribute() throws Exception {
		getSM().setLowerGdaLimits(new Double[] { 1., 2. });
		getSM().setUpperGdaLimits(new Double[] { 3., 4. });
		getSM().setTolerances(new Double[] { .1, .2 });
		getSM().setNumberTries(99);

		ArrayAssert.assertEquals(new Double[] { 1., 2. }, (Object[]) getSM().getAttribute("lowerGdaLimits"));
		ArrayAssert.assertEquals(new Double[] { 3., 4. }, (Object[]) getSM().getAttribute("upperGdaLimits"));
		ArrayAssert.assertEquals(new Double[] { .1, .2 }, (Double[]) getSM().getAttribute("tolerance"));
		ArrayAssert.assertEquals(new Double[] { .1, .2 }, (Double[]) getSM().getAttribute("tolerance"));
		assertEquals(99, getSM().getAttribute("numberTries()"));
		ArrayAssert.assertEquals(new Double[] { 1., 3. }, (Double[]) getSMB().getAttribute(ScannableMotion.FIRSTINPUTLIMITS));
		getSM().setLowerGdaLimits(new Double[] { null, 2. });
		ArrayAssert.assertEquals(new Double[] {null, 3. }, (Double[]) getSMB().getAttribute(ScannableMotion.FIRSTINPUTLIMITS));
		Double[] nullDoubleArray = null;
		getSM().setUpperGdaLimits(nullDoubleArray);
		Assert.assertNull( getSMB().getAttribute(ScannableMotion.FIRSTINPUTLIMITS));
		Assert.assertNull(getSMB().getFirstInputLimits());
	}

	@Test
	public void testMoveToWithDefaultNoRetries() throws DeviceException {
		getSM().setTolerances(new Double[] { .1, .1 });
		when(getSM().isBusy()).thenReturn(false);
		getSM().moveTo(new Double[] { 10., 20. });
		verify(getDelegate(), never()).rawGetPosition();
		verify(getDelegate(), times(1)).rawAsynchronousMoveTo(new Double[] { 10., 20. });
	}

	@Test
	public void testMoveToWithGoodScannableAndNoRetries() throws DeviceException {
		getSM().setNumberTries(1);
		getSM().setTolerances(new Double[] { .1, .1 });
		when(getSM().isBusy()).thenReturn(false);
		when(getSMB().rawGetPosition()).thenReturn(new double[] { 11., 21. }, new double[] { 10., 20. });
		getSM().moveTo(new Double[] { 10., 20. });
		verify(getDelegate(), times(0)).rawGetPosition();
		verify(getDelegate(), times(1)).rawAsynchronousMoveTo(new Double[] { 10., 20. });
	}

	@Test
	public void testMoveToWithOneRetry() throws DeviceException {
		getSM().setNumberTries(2);
		getSM().setTolerances(new Double[] { .1, .1 });
		when(getSM().isBusy()).thenReturn(false);
		when(getSMB().rawGetPosition()).thenReturn(new double[] { 11., 21. }, new double[] { 10., 20. });
		getSM().moveTo(new Double[] { 10., 20. });
		verify(getDelegate(), times(2)).rawGetPosition();
		verify(getDelegate(), times(2)).rawAsynchronousMoveTo(new Double[] { 10., 20. });
	}

	@Test
	public void testMoveToWithRetryWhenAlreadyThere() throws DeviceException {
		getSM().setNumberTries(1);
		getSM().setTolerances(new Double[] { .1, .1 });
		when(getSMB().rawGetPosition()).thenReturn(new double[] { 10., 20. });
		getSM().moveTo(new double[] { 10., 20. });
		verify(getDelegate(), never()).rawGetPosition();
		verify(getDelegate(), times(1)).rawAsynchronousMoveTo(anyObject());
	}

	@Test
	public void testGetPositionWithOffsetAndExtraFields() throws DeviceException {
		configureTwoExtraFields();
		getSMB().setOffset(.1, .2, .3, .4);
		when(getDelegate().rawGetPosition()).thenReturn(new Double[] { 1., 2., 3., 4. });
		ArrayAssert.assertEquals(new Double[] { 1.1, 2.2, 3.3, 4.4 }, (Object[]) getSB().getPosition()); // debug
	}

	@Test
	public void testGetPositionWithOffsetOnInputFieldsOnlyAndExtraFields() throws DeviceException {
		configureTwoExtraFields();
		getSMB().setOffset(.1, .2);
		when(getDelegate().rawGetPosition()).thenReturn(new Double[] { 1., 2., 3., 4. });
		ArrayAssert.assertEquals(new Double[] { 1.1, 2.2, 3., 4. }, (Object[]) getSB().getPosition()); // debug
	}

	@Test
	public void testToStringWithOffsets() throws DeviceException {
		configureTwoExtraFields();
		getSMB().setOffset(null, .2);
		when(getDelegate().rawGetPosition()).thenReturn(new Double[] { 1., 2., 3., 4. });
		getSB().setOutputFormat(new String[] { "%1.2g", "%1.2g", "%1.3g", "%1.4g" });
		assertEquals("name : i1: 1.0 i2: 2.2(+0.20) e1: 3.00 e2: 4.000", getSB().toFormattedString());
	}

	@Test
	public void testToStringWithOffsetsSingleInput() throws DeviceException {
		configureOneInputField();
		getSMB().setOffset(.1);
		when(getDelegate().rawGetPosition()).thenReturn(new Double[] { 1. });
		getSB().setOutputFormat(new String[] { "%1.2g" });
		assertEquals("name : i1: 1.1(+0.10)", getSB().toFormattedString());
	}

	@Test
	public void testSetGetLimitsWithNegativeScalingFactor_SingleField() throws Exception {
		configureOneInputField();
		getSM().setScalingFactor(-1.);
		getSM().setLowerGdaLimits(-1000.);
		getSM().setUpperGdaLimits(800.);
		assertEquals(new Double(-1000), getSM().getLowerGdaLimits()[0]);
		assertEquals(new Double(800), getSM().getUpperGdaLimits()[0]);
	}

	@Test
	public void testCheckPositionWithNegativeScalingFactor_SingleField() throws Exception {
		configureOneInputField();
		getSM().setScalingFactor(-1.);
		getSM().setLowerGdaLimits(-1000.);
		getSM().setUpperGdaLimits(800.);

		assertEquals(null, getSM().checkPositionValid(0.));// micron
		assertEquals(null, getSM().checkPositionValid(-999));// micron
		assertEquals(null, getSM().checkPositionValid(799));// micron
		assertEquals("Scannable limit violation on name.i1: 1001.0 > 1000.0 (internal/hardware/dial values).", getSM()
				.checkPositionValid(-1001));// micron
		assertEquals("Scannable limit violation on name.i1: -801.0 < -800.0 (internal/hardware/dial values).", getSM()
				.checkPositionValid(801));// micron
	}

	@Test
	public void testSetGetLimitsWithScalingFactor_ThreeFields() throws Exception {
		getSB().setInputNames(new String[] { "i1", "i2", "i3" });
		getSB().setName("name");

		getSM().setScalingFactor(new Double[] { null, 1., -1. });
		getSM().setLowerGdaLimits(new Double[] { -1000., -1000., -1000. });
		getSM().setUpperGdaLimits(new Double[] { 800., 800., 800. });
		ArrayAssert.assertEquals(new Double[] { -1000., -1000., -1000. }, getSM().getLowerGdaLimits());
		ArrayAssert.assertEquals(new Double[] { 800., 800., 800. }, getSM().getUpperGdaLimits());
	}

	@Test
	public void testCheckPositionWithScalingFactor_ThreeFields() throws Exception {
		getSB().setInputNames(new String[] { "i1", "i2", "i3" });
		getSB().setName("name");
		getSM().setScalingFactor(new Double[] { null, 1., -1. });
		getSM().setLowerGdaLimits(new Double[] { -1000., -1000., -1000. });
		getSM().setUpperGdaLimits(new Double[] { 800., 800., 800. });

		assertEquals(null, getSM().checkPositionValid(new Double[] { 0., 0., 0. }));// micron
		assertEquals(null, getSM().checkPositionValid(new Double[] { -999., -999., -999. }));// micron
		assertEquals(null, getSM().checkPositionValid(new Double[] { 799., 799., 799. }));// micron

		assertEquals("Scannable limit violation on name.i1: -1100.0 < -1000.0 (internal/hardware/dial values).", getSM().checkPositionValid(new Double[] { -1100., 0., 0. } ));// micron
		assertEquals("Scannable limit violation on name.i1: 900.0 > 800.0 (internal/hardware/dial values).", getSM().checkPositionValid(new Double[] { 900., 0., 0. } ));// micron
		assertEquals("Scannable limit violation on name.i2: -1100.0 < -1000.0 (internal/hardware/dial values).", getSM().checkPositionValid(new Double[] { 0., -1100., 0. } ));// micron
		assertEquals("Scannable limit violation on name.i2: 900.0 > 800.0 (internal/hardware/dial values).", getSM().checkPositionValid(new Double[] { 0., 900., 0. } ));// micron
		assertEquals("Scannable limit violation on name.i3: 1100.0 > 1000.0 (internal/hardware/dial values).", getSM().checkPositionValid(new Double[] { 0., 0., -1100. } ));// micron
		assertEquals("Scannable limit violation on name.i3: -900.0 < -800.0 (internal/hardware/dial values).", getSM().checkPositionValid(new Double[] { 0., 0., 900. } ));// micron
	}
}
