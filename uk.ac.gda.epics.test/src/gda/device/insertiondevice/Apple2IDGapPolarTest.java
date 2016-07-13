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
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import gda.device.DeviceException;

public class Apple2IDGapPolarTest {
	private IApple2ID controller;
	private Apple2IDGapPolar scannable;

	@Before
	public void setUp() throws DeviceException {
		controller = mock(IApple2ID.class);
		when(controller.getIDMode()).thenReturn("GAP AND PHASE");
		when(controller.isEnabled()).thenReturn(true);
		when(controller.getMaxPhaseMotorPos()).thenReturn(30.0);
		when(controller.motorPositionsEqual(anyDouble(), anyDouble())).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return args[0].equals(args[1]);
			}
		});

		scannable = new Apple2IDGapPolar();
		scannable.setController(controller);
	}

	@Test(expected = DeviceException.class)
	public void testPositionNotList() throws DeviceException {
		scannable.rawAsynchronousMoveTo(new Double(5));
	}

	@Test(expected = DeviceException.class)
	public void testNotEnoughElementsInPosition() throws DeviceException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList("LH")));
	}

	@Test(expected = NumberFormatException.class)
	public void testInvalidGap() throws DeviceException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList("GAP", "LV")));
	}

	@Test(expected = DeviceException.class)
	public void testInvalidPolarisation() throws DeviceException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList(24, "X")));
	}

	@Test
	public void testMoveToHorizontal() throws DeviceException, InterruptedException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList(24, "LH")));
		while (scannable.isBusy()) {
			Thread.sleep(10);
		}
		verify(controller).asynchronousMoveTo(new Apple2IDPosition(24, 0, 0, 0, 0));
	}

	@Test
	public void testMoveToVertical() throws DeviceException, InterruptedException {
		scannable.rawAsynchronousMoveTo(new ArrayList<Object>(Arrays.asList(28, "LV")));
		while (scannable.isBusy()) {
			Thread.sleep(10);
		}
		verify(controller).asynchronousMoveTo(new Apple2IDPosition(28, 30, 0, 0, 30));
	}

	@Test
	public void testRawGetPositionHorizontal() throws DeviceException {
		when(controller.getPosition()).thenReturn(new Apple2IDPosition(37, 0, 0, 0, 0));
		Object result = scannable.rawGetPosition();
		checkPositionResult(result, 37, "LH", "GAP AND PHASE", true, 0, 0, 0, 0);
	}

	@Test
	public void testRawGetPositionVertical() throws DeviceException {
		when(controller.getPosition()).thenReturn(new Apple2IDPosition(37, 30, 0, 0, 30));
		Object result = scannable.rawGetPosition();
		checkPositionResult(result, 37, "LV", "GAP AND PHASE", true, 30, 0, 0, 30);
	}

	private void checkPositionResult(Object result, double gap, String polarisationMode, String idMode, boolean enabled, double topOuterPos,
			double topInnerPos, double bottomOuterPos, double bottonInnerPos) {
		assertThat(result, instanceOf(Object[].class));
		Object[] resultArray = (Object[]) result;
		assertEquals(8, resultArray.length);
		assertEquals(gap, resultArray[0]);
		assertEquals(polarisationMode, resultArray[1]);
		assertEquals(idMode, resultArray[2]);
		assertEquals(enabled, resultArray[3]);
		assertEquals(topOuterPos, resultArray[4]);
		assertEquals(topInnerPos, resultArray[5]);
		assertEquals(bottomOuterPos, resultArray[6]);
		assertEquals(bottonInnerPos, resultArray[7]);
	}
}
