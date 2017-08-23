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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;

import gda.device.DeviceException;
import gda.device.scannable.scannablegroup.ScannableMotionWithScannableFieldsBase;
import gda.device.scannable.scannablegroup.ScannableMotionWithScannableFieldsBase.ScannableField;

public class ScannableMotionWithScannableFieldsBaseTest {

	ScannableMotionWithScannableFieldsBase scn;
	private ScannableField i1;
	private ScannableField i2;
	private ScannableField e1;
	private ScannableField e2;

	@Before
	public void setUp() throws Exception {
		scn = new ScannableMotionWithScannableFieldsBase();// "scn", new String[] { "i1", "i2" }, new String[] { "e1", "e2" });
		scn = spy(scn);
		scn.setName("scn");
		scn.setInputNames(new String[] { "i1", "i2" });
		scn.setExtraNames(new String[] { "e1", "e2" });
		when(scn.getOutputFormat()).thenReturn(new String[] { "%.0f", "%.1f", "%.2f", "%.3f" });
		doReturn(new Double[] { 0., 1., 2., 3. }).when(scn).rawGetPosition();
		doNothing().when(scn).rawAsynchronousMoveTo(anyObject());
		i1 = scn.__getattr__("i1");
		i2 = scn.__getattr__("i2");
		e1 = scn.__getattr__("e1");
		e2 = scn.__getattr__("e2");
	}

	@Test
	public void testBasics() throws DeviceException {
		assertEquals("scn", scn.getName());
		assertArrayEquals(new String[] { "i1", "i2" }, scn.getInputNames());
		assertArrayEquals(new String[] { "e1", "e2" }, scn.getExtraNames());
		assertArrayEquals(new String[] { "%.0f", "%.1f", "%.2f", "%.3f" }, scn.getOutputFormat());
		assertArrayEquals(new Double[] { 0., 1., 2., 3. }, gda.device.scannable.ScannableUtils.objectToArray(scn
				.getPosition()));
		assertFalse(scn.isAutoCompletePartialMoveToTargets());
	}

	@Test
	public void testSFConstruction() {
		assertEquals(i1.getName(), "i1");
		assertTrue(i1.isInputField());
		assertArrayEquals(new String[] { "i1" }, i1.getInputNames());
		assertArrayEquals(new String[] {}, i1.getExtraNames());

		assertEquals(i2.getName(), "i2");
		assertTrue(i2.isInputField());
		assertArrayEquals(new String[] { "i2" }, i2.getInputNames());
		assertArrayEquals(new String[] {}, i2.getExtraNames());

		assertEquals(e1.getName(), "e1");
		assertFalse(e1.isInputField());
		assertArrayEquals(new String[] {}, e1.getInputNames());
		assertArrayEquals(new String[] { "e1" }, e1.getExtraNames());

		assertEquals(e2.getName(), "e2");
		assertFalse(e2.isInputField());
		assertArrayEquals(new String[] {}, e2.getInputNames());
		assertArrayEquals(new String[] { "e2" }, e2.getExtraNames());
	}

	@Test
	public void testSFGetOutputFormat() {
		assertArrayEquals(new String[] { "%.0f" }, i1.getOutputFormat());
		assertArrayEquals(new String[] { "%.1f" }, i2.getOutputFormat());
		assertArrayEquals(new String[] { "%.2f" }, e1.getOutputFormat());
		assertArrayEquals(new String[] { "%.3f" }, e2.getOutputFormat());
	}

	@Test
	public void testSFAtCommandFailure() throws DeviceException {
		i1.atCommandFailure();
		i2.atCommandFailure();
		e1.atCommandFailure();
		e2.atCommandFailure();
		verify(scn, times(4)).atCommandFailure();
	}

	@Test
	public void testFullCoordinatedMoveOutsideScanWithAutoComplete() throws DeviceException {
		scn.setAutoCompletePartialMoveToTargets(true);
		i1.atLevelMoveStart();
		i2.atLevelMoveStart();
		i1.asynchronousMoveTo(10.);
		i2.asynchronousMoveTo(10.1);
		verify(scn).rawAsynchronousMoveTo(new Double[] { 10., 10.1 });
		assertFalse(scn.isTargeting());
	}
	@Test
	public void testFullCoordinatedMoveInScanWithAutoComplete() throws DeviceException {
		scn.setAutoCompletePartialMoveToTargets(true);
		scn.atScanStart();
		i1.atLevelMoveStart();
		i2.atLevelMoveStart();
		i1.asynchronousMoveTo(10.);
		i2.asynchronousMoveTo(10.1);
		verify(scn).rawAsynchronousMoveTo(new Double[] { 10., 10.1 });
		assertFalse(scn.isTargeting());
	}

	@Test
	public void testFullCoordinatedMoveOutsideScan() throws DeviceException {
		i1.atLevelMoveStart();
		i2.atLevelMoveStart();
		i1.asynchronousMoveTo(10.);
		i2.asynchronousMoveTo(10.1);
		verify(scn).rawAsynchronousMoveTo(new Double[] { 10., 10.1 });
		assertFalse(scn.isTargeting());
	}

	@Test
	public void testPartialCoordinatedMoveOutsideScanWithAutoComplete() throws DeviceException {
		scn.setAutoCompletePartialMoveToTargets(true);
		when(scn.rawGetPosition()).thenReturn(new Double[] { 90., 91., 92., 93.});
		i1.atLevelMoveStart();
		i1.asynchronousMoveTo(10.);
		verify(scn).rawAsynchronousMoveTo(new Double[] { 10., 91. });
		assertFalse(scn.isTargeting());
	}

	@Test
	public void testGetPositionAtScanStart() throws DeviceException {
		assertEquals(null, (Object) scn.getPositionAtScanStart());
		scn.atScanStart();
		assertArrayEquals(new Object[] { 0., 1., 2., 3. }, scn.getPositionAtScanStart());
		scn.atCommandFailure();
		assertEquals(null, (Object) scn.getPositionAtScanStart());
	}

	@Test
	public void testGetPositionAtScanStartAfterFailure() throws DeviceException {
		scn.atScanStart();
		scn.atCommandFailure();
		assertEquals(null, (Object) scn.getPositionAtScanStart());
	}

	@Test
	public void testGetPositionAtScanStartAfterScanEnd() throws DeviceException {
		scn.atScanStart();
		scn.atScanEnd();
		assertEquals(null, (Object) scn.getPositionAtScanStart());
	}

	@Test
	public void testPartialCoordinatedMoveInScanWithAutoComplete() throws DeviceException {
		scn.setAutoCompletePartialMoveToTargets(true);
		when(scn.rawGetPosition()).thenReturn(new Double[] { 90., 91., 92., 93.});
		scn.atScanStart();
		// Introduce drift
		when(scn.rawGetPosition()).thenReturn(new Double[] { 90., 91.1, 92., 93.});
		i1.atLevelMoveStart();
		i1.asynchronousMoveTo(10.);
		verify(scn).rawAsynchronousMoveTo(new Double[] { 10., 91. });
		assertFalse(scn.isTargeting());
	}

	@Test
	public void testPartialCoordinatedMoveOutsideScan() throws DeviceException {
		i1.atLevelMoveStart();
		i1.asynchronousMoveTo(10.);
		verify(scn).rawAsynchronousMoveTo(new Double[] { 10., null });
		assertFalse(scn.isTargeting());
	}

	@Test
	public void testPartialCoordinatedMoveOutsideScanWithUnits() throws DeviceException {
		i1.setHardwareUnitString("m");
		i1.setUserUnits("mm");

		i1.atLevelMoveStart();
		i1.asynchronousMoveTo(10000.);
		verify(scn).rawAsynchronousMoveTo(new Double[] { 10., null });
		assertFalse(scn.isTargeting());
	}

	@Test
	public void testGetPoistionWithUnits() throws DeviceException {
		i1.setHardwareUnitString("m");
		i1.setUserUnits("mm");
		doReturn(new Double[] { 5., 1., 2., 3. }).when(scn).rawGetPosition();
		assertEquals(5000., i1.getPosition());
	}

	@Test
	public void testUncoordinatedMove() throws DeviceException {
		doNothing().when(scn).asynchronousMoveFieldTo(anyInt(), anyObject());
		i1.asynchronousMoveTo(10.);
		assertFalse(scn.isTargeting());
		verify(scn).asynchronousMoveFieldTo(0, 10.);
		verify(scn, never()).asynchronousMoveTo(anyObject());
	}

	@Test
	public void testSFMoveOutputField() throws Exception {
		try {
			e1.asynchronousMoveTo(10.);
			throw new Exception("DeviceException expected");
		} catch (DeviceException e) {
			assertEquals("Problem triggering e1 move to 10.0: The ScannableField scn.e1 represents an output. It therefore could not be moved to 10.0 .", e
					.getMessage());
		}
	}

	@Test
	public void testDefaultAsynchronousMoveFieldToOutsideScanWithAutoComplete() throws DeviceException {
		scn.setAutoCompletePartialMoveToTargets(true);
		when(scn.rawGetPosition()).thenReturn(new Double[] { 90., 91., 92., 93.});
		scn.asynchronousMoveFieldTo(0, 1.);
		verify(scn).rawAsynchronousMoveTo(new Object[] { 1., 91.});
	}

	@Test
	public void testDefaultAsynchronousMoveFieldToOutsideScan() throws DeviceException {
		scn.asynchronousMoveFieldTo(0, 1.);
		verify(scn).rawAsynchronousMoveTo(new Object[] { 1., null});
	}

	@Test(expected = DeviceException.class)
	public void testDefaultAsynchronousMoveFieldToWithAnOutputField() throws DeviceException {
		scn.asynchronousMoveFieldTo(2, 1.);
	}

	@Test(expected = DeviceException.class)
	public void testDefaultAsynchronousMoveFieldToOutOfBounds() throws DeviceException {
		scn.asynchronousMoveFieldTo(5, 1.);
	}

	@Test
	public void testSFAtScanStart() throws DeviceException {

		doReturn(false).when(scn).isBusy(); // just as a marker

		InOrder inOrder = inOrder(scn);
		i1.atScanStart();
		e1.atScanStart();
		i1.atScanEnd();
		e1.atScanEnd();
		scn.isBusy(); // just as a marker
		i1.atScanStart();
		i2.atScanEnd();

		inOrder.verify(scn).atScanStart();
		inOrder.verify(scn).atScanEnd();
		inOrder.verify(scn).isBusy();
		inOrder.verify(scn).atScanStart();
		inOrder.verify(scn).atScanEnd();

		verify(scn, times(2)).atScanStart();
		verify(scn, times(2)).atScanEnd();
	}

	@Test
	public void testSFAtScanLineStart() throws DeviceException {

		doReturn(false).when(scn).isBusy(); // just as a marker

		InOrder inOrder = inOrder(scn);
		i1.atScanLineStart();
		e1.atScanLineStart();
		i1.atScanLineEnd();
		e1.atScanLineEnd();
		scn.isBusy(); // just as a marker
		i1.atScanLineStart();
		i2.atScanLineEnd();

		inOrder.verify(scn).atScanLineStart();
		inOrder.verify(scn).atScanLineEnd();
		inOrder.verify(scn).isBusy();
		inOrder.verify(scn).atScanLineStart();
		inOrder.verify(scn).atScanLineEnd();

		verify(scn, times(2)).atScanLineStart();
		verify(scn, times(2)).atScanLineEnd();
	}

	@Test
	public void testSFAtPointStart() throws DeviceException {

		doReturn(false).when(scn).isBusy(); // just as a marker

		InOrder inOrder = inOrder(scn);
		i1.atPointStart();
		e1.atPointStart();
		i1.atPointEnd();
		e1.atPointEnd();
		scn.isBusy(); // just as a markerexpecteds
		i1.atPointStart();
		i2.atPointEnd();

		inOrder.verify(scn).atPointStart();
		inOrder.verify(scn).atPointEnd();
		inOrder.verify(scn).isBusy();
		inOrder.verify(scn).atPointStart();
		inOrder.verify(scn).atPointEnd();

		verify(scn, times(2)).atPointStart();
		verify(scn, times(2)).atPointEnd();
	}

	@Test
	public void testSFSetToleranceDouble() throws DeviceException {
		i1.setTolerance(2.);
		verify(scn).setTolerances(new Double[] {2., 0.});
	}

	@Test(expected = DeviceException.class)
	public void testSFSetToleranceDoubleSettingNonInput() throws DeviceException {
		e1.setTolerance(2.);
		verify(scn).setTolerances(new Double[] {2., 0.});
	}

	@Test
	public void testSFSetToleranceDoubleArray() throws DeviceException {
		i1.setTolerances(new Double[] {2.});
		verify(scn).setTolerances(new Double[] {2., 0.});
	}


	@Test(expected = DeviceException.class)
	public void testSFSetToleranceDoubleArrayLongerThanOne() throws DeviceException {
		i1.setTolerances(new Double[] {2., 0.});
		verify(scn).setTolerances(new Double[] {2., 0.});
	}

	@Test
	public void testSFGetTolerance() throws DeviceException {
		when(scn.getTolerances()).thenReturn(new Double[] { 1.,2. });
		Double[] result = i1.getTolerances();
		assertEquals(1, result.length);
		assertEquals(1., result[0], .0001);
	}

	@Test
	public void testSFGetOffset() {
		when(scn.getOffset()).thenReturn(new Double[] { 1., null });
		assertArrayEquals(new Double[]{1.}, i1.getOffset());
		assertArrayEquals(null, i2.getOffset());
	}

	@Test
	public void testSFGetOffsetWithNoOffsetsOnParent() {
		when(scn.getOffset()).thenReturn(null);
		assertArrayEquals(null, i1.getOffset());
		assertArrayEquals(null, i2.getOffset());
	}

	@Test
	public void testSFGetScalingFactor() {
		when(scn.getScalingFactor()).thenReturn(new Double[] { 1., null });
		assertArrayEquals(new Double[]{1.}, i1.getScalingFactor());
		assertArrayEquals(null, i2.getScalingFactor());
	}

	@Test
	public void testSFGetScalingFactorNoOffsetsOnParent() {
		when(scn.getOffset()).thenReturn(null);
		assertArrayEquals(null, i1.getScalingFactor());
		assertArrayEquals(null, i2.getScalingFactor());
	}

// TODO: can't test with spy.
//	@Test
//	public void testSFGSetOffset() {
//		when(scn.getOffset()).thenReturn(new Double[] { 1., null });
//		i2.setOffset(2.);
//		verify(scn).setOffset(new Double[] { 1., 2. });
//	}
//
//	@Test
//	public void testSFSetOffsetWithNoOffsetsOnParent() {
//		when(scn.getOffset()).thenReturn(null);
//		i2.setOffset(2.);
//		verify(scn).setOffset(new Double[] { null, 2., null, null });
//	}
//
//	@Test
//	public void testSFSetScalingFactor() {
//		when(scn.getScalingFactor()).thenReturn(new Double[] { 1., null });
//		assertArrayEquals(new Double[]{1.}, i1.getScalingFactor());
//		assertArrayEquals(null, i2.getScalingFactor());
//	}
//
//	@Test
//	public void testSFSetScalingFactorNoOffsetsOnParent() {
//		when(scn.getOffset()).thenReturn(null);
//		assertArrayEquals(null, i1.getScalingFactor());
//		assertArrayEquals(null, i2.getScalingFactor());
//	}

	@Test
	public void testSFStop() throws DeviceException {
		i1.stop();
		e1.stop();
		verify(scn, times(2)).stop();
	}

	@Test
	public void testGetFieldPositionWithArray() throws Exception {
		doReturn(new Double[] { 0., 1., 2., 3. }).when(scn).rawGetPosition();
		assertEquals(0., scn.getFieldPosition(0));
		assertEquals(1., scn.getFieldPosition(1));
		assertEquals(2., scn.getFieldPosition(2));
		assertEquals(3., scn.getFieldPosition(3));
	}

	@Test
	public void testGetFieldPositionWithSingleInput() throws Exception {
		scn.setInputNames(new String[] {"i1"});
		scn.setExtraNames(new String[] {});
		doReturn(1.).when(scn).rawGetPosition();
		assertEquals(1., scn.getFieldPosition(0));
	}
	@Test
	public void testGetFieldPositionWithPyList() throws Exception {
		PyList position = new PyList(new PyObject[]{new PyFloat(0.), new PyInteger(1), new PyFloat(2.), new PyInteger(3)});
		doReturn(position).when(scn).rawGetPosition();
		assertEquals(new PyFloat(0.), scn.getFieldPosition(0));
		assertEquals(new PyInteger(1), scn.getFieldPosition(1));
		assertEquals(new PyFloat(2.), scn.getFieldPosition(2));
		assertEquals(new PyInteger(3), scn.getFieldPosition(3));
	}

	@Test
	public void testGetFieldPositionWithPyArray() throws Exception {
		PyObject[] position = new PyObject[]{new PyFloat(0.), new PyInteger(1), new PyFloat(2.), new PyInteger(3)};
		doReturn(position).when(scn).rawGetPosition();
		assertEquals(new PyFloat(0.), scn.getFieldPosition(0));
		assertEquals(new PyInteger(1), scn.getFieldPosition(1));
		assertEquals(new PyFloat(2.), scn.getFieldPosition(2));
		assertEquals(new PyInteger(3), scn.getFieldPosition(3));
	}

	@Test
	public void testGetFieldPositionWithSinglePyInput() throws Exception {
		scn.setInputNames(new String[] { "i1"});
		scn.setExtraNames(new String[] {});
		doReturn(new PyFloat(1.)).when(scn).rawGetPosition();
		assertEquals(new PyFloat(1.), scn.getFieldPosition(0));
	}

}
