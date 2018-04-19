/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.device.enumpositioner;

import static gda.device.enumpositioner.ValvePosition.CLOSE;
import static gda.device.enumpositioner.ValvePosition.OPEN;
import static gda.device.enumpositioner.ValvePosition.RESET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;
import gda.device.EnumPositionerStatus;
import gda.observable.IObserver;

public class DummyValveTest {

	private DummyValveForTest valve;
	private IObserver observer;

	@Before
	public void setUp() {
		valve = new DummyValveForTest();
		observer = mock(IObserver.class);

		valve.setName("test_valve");
		valve.setTimeToMove(0); // we want tests to run as fast as possible
		valve.setPosition(OPEN);
		valve.addIObserver(observer);
	}

	@Test
	public void testConfigure() {
		valve.configure();
		assertEquals(Arrays.asList(OPEN, CLOSE, RESET), valve.getPositionsList());
		assertTrue(valve.isConfigured());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeTimeToMoveFails() throws Exception {
		valve.setTimeToMove(-5);
	}

	@Test
	public void testIsBusyFalseAtStart() throws Exception {
		assertFalse(valve.isBusy());
	}

	@Test
	public void testIsBusyTrueWhenMoving() throws Exception {
		valve.setPositionerStatusForTest(EnumPositionerStatus.MOVING);
		assertTrue(valve.isBusy());
	}

	@Test(expected = DeviceException.class)
	public void testRawAsynchronousMoveToFailsWhenBusy() throws Exception {
		valve.setPositionerStatusForTest(EnumPositionerStatus.MOVING);
		valve.rawAsynchronousMoveTo(CLOSE);
	}

	@Test(expected = DeviceException.class)
	public void testMoveToFailsWhenBusy() throws Exception {
		valve.setPositionerStatusForTest(EnumPositionerStatus.MOVING);
		valve.moveTo(CLOSE);
	}

	@Test
	public void testMoveFromOpenToClosed() throws Exception {
		valve.moveTo(CLOSE);
		verify(observer).update(valve, "Closing");
		verify(observer).update(valve, "Closed");
		assertEquals("Closed", valve.getPosition());
	}

	@Test
	public void testMoveFromOpenToOpenDoesNothing() throws Exception {
		valve.moveTo(OPEN);
		verifyZeroInteractions(observer);
	}

	@Test
	public void testResetFromOpen() throws Exception {
		valve.moveTo(RESET);
		verify(observer).update(valve, "Reset");
		verify(observer).update(valve, "Open");
		assertEquals("Open", valve.getPosition());
	}

	@Test
	public void testMoveFromClosedToOpen() throws Exception {
		valve.setPosition("Closed");
		valve.moveTo(OPEN);
		verify(observer).update(valve, "Opening");
		verify(observer).update(valve, "Open");
		assertEquals("Open", valve.getPosition());
	}

	@Test
	public void testMoveFromClosedToClosedDoesNothing() throws Exception {
		valve.setPosition("Closed");
		valve.moveTo(CLOSE);
		verifyZeroInteractions(observer);
	}

	@Test
	public void testResetFromClosed() throws Exception {
		valve.setPosition("Closed");
		valve.moveTo(RESET);
		verify(observer).update(valve, "Reset");
		verify(observer).update(valve, "Closed");
		assertEquals("Closed", valve.getPosition());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMoveToInvalidPositionFails() throws Exception {
		valve.moveTo("Somewhere");
	}

	/**
	 * Minimal extension to DummyValve to allow access to protected functions<br>
	 * setPositionerStatus() is not currently protected but will be as soon as we can fix problems with access to
	 * protected members from inner classes of derived classes.
	 */
	private class DummyValveForTest extends DummyValve {
		public void setPositionerStatusForTest(EnumPositionerStatus status) {
			setPositionerStatus(status);
		}
	}
}
