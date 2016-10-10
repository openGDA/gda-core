/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.insertiondevice;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;

public class Apple2IDGapPolarPosTest {

	private Apple2IDBase controller;
	private Apple2IDGapPolarPos scannable;

	@Before
	public void setUp() throws DeviceException {
		controller = mock(Apple2IDBase.class);
		when(controller.getIDMode()).thenReturn("GAP AND PHASE");
		when(controller.isEnabled()).thenReturn(true);
		when(controller.getMaxPhaseMotorPos()).thenReturn(30.0);
		when(controller.getMotorPositionTolerance()).thenReturn(0.012);
		when(controller.motorPositionsEqual(anyDouble(), anyDouble())).thenCallRealMethod();
		when(controller.getPolarisationMode(any(Apple2IDPosition.class))).thenCallRealMethod();

		scannable = new Apple2IDGapPolarPos();
		scannable.setController(controller);
	}

	@Test(expected = DeviceException.class)
	public void testPositionNotList() throws DeviceException {
		scannable.rawAsynchronousMoveTo(new Double(5));
	}

	@Test(expected = DeviceException.class)
	public void testNotEnoughElementsInPosition() throws DeviceException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList(24, "CR")));
	}

	@Test(expected = NumberFormatException.class)
	public void testInvalidGap() throws DeviceException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList("GAP", "CR", 15)));
	}

	@Test(expected = NumberFormatException.class)
	public void testInvalidMotorPosition() throws DeviceException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList(24, "CR", "POS")));
	}

	@Test(expected = DeviceException.class)
	public void testInvalidPolarisation() throws DeviceException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList(24, "X", 15)));
	}

	@Test
	public void testMoveToCircular() throws DeviceException, InterruptedException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList(24, "CR", 15)));
		while (scannable.isBusy()) {
			Thread.sleep(10);
		}
		verify(controller).asynchronousMoveTo(new Apple2IDPosition(24, 15, 0, 0, 15));
	}

	@Test
	public void testMoveToLinear1() throws DeviceException, InterruptedException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList(24, "LAP", 15)));
		while (scannable.isBusy()) {
			Thread.sleep(10);
		}
		verify(controller).asynchronousMoveTo(new Apple2IDPosition(24, 15, 0, 0, -15));
	}

	@Test
	public void testMoveToLinear2() throws DeviceException, InterruptedException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList(24, "LAN", 15)));
		while (scannable.isBusy()) {
			Thread.sleep(10);
		}
		verify(controller).asynchronousMoveTo(new Apple2IDPosition(24, 0, 15, -15, 0));
	}

	@Test
	public void testMoveToHorizontal() throws DeviceException, InterruptedException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList(24, "LH", 15)));
		while (scannable.isBusy()) {
			Thread.sleep(10);
		}
		verify(controller).asynchronousMoveTo(new Apple2IDPosition(24, 0, 0, 0, 0));
	}

	@Test
	public void testRawGetPositionHorizontal() throws DeviceException {
		when(controller.getPosition()).thenReturn(new Apple2IDPosition(37, 0, 0, 0, 0));
		Object result = scannable.rawGetPosition();
		checkPositionResult(result, 37, "LH", 0, "GAP AND PHASE", true, 0, 0, 0, 0);
	}

	@Test
	public void testRawGetPositionCircular() throws DeviceException {
		when(controller.getPosition()).thenReturn(new Apple2IDPosition(37, 23, 0, 0, 23));
		Object result = scannable.rawGetPosition();
		checkPositionResult(result, 37, "CR", 23, "GAP AND PHASE", true, 23, 0, 0, 23);
	}

	@Test
	public void testRawGetPositionLinear1() throws DeviceException {
		when(controller.getPosition()).thenReturn(new Apple2IDPosition(37, 23, 0, 0, -23));
		Object result = scannable.rawGetPosition();
		checkPositionResult(result, 37, "LAP", 23, "GAP AND PHASE", true, 23, 0, 0, -23);
	}

	@Test
	public void testRawGetPositionLinear2() throws DeviceException {
		when(controller.getPosition()).thenReturn(new Apple2IDPosition(37, 0, 23, -23, 0));
		Object result = scannable.rawGetPosition();
		checkPositionResult(result, 37, "LAN", 23, "GAP AND PHASE", true, 0, 23, -23, 0);
	}

	@Test
	public void testRawGetPositionUnknown() throws DeviceException {
		when(controller.getPosition()).thenReturn(new Apple2IDPosition(37, 45, 23, -23, 0));
		Object result = scannable.rawGetPosition();
		checkPositionResult(result, 37, "UNKNOWN", 45, "GAP AND PHASE", true, 45, 23, -23, 0);
	}

	private void checkPositionResult(Object result, double gap, String polarisationMode, double motorPos, String idMode, boolean enabled, double topOuterPos,
			double topInnerPos, double bottomOuterPos, double bottonInnerPos) {
		assertThat(result, instanceOf(Object[].class));
		Object[] resultArray = (Object[]) result;
		assertEquals(9, resultArray.length);
		assertEquals(gap, resultArray[0]);
		assertEquals(polarisationMode, resultArray[1]);
		assertEquals(motorPos, resultArray[2]);
		assertEquals(idMode, resultArray[3]);
		assertEquals(enabled, resultArray[4]);
		assertEquals(topOuterPos, resultArray[5]);
		assertEquals(topInnerPos, resultArray[6]);
		assertEquals(bottomOuterPos, resultArray[7]);
		assertEquals(bottonInnerPos, resultArray[8]);
	}
}
