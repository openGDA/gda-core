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

package gda.device.syringepump.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;
import gda.device.syringepump.SyringePump;
import gda.device.syringepump.SyringePumpController;
import gda.factory.FactoryException;

public class SyringePumpTest {

	private SyringePump pump;
	private SyringePumpController control;
	@Before
	public void setUp() throws FactoryException, DeviceException {
		pump = new SyringePump();
		pump.setName("testSyringePump");
		control = mock(SyringePumpController.class);
		when(control.getCapacity()).thenReturn(100.0);
		when(control.getVolume()).thenReturn(70.0);
		when(control.isEnabled()).thenReturn(true);
		pump.setController(control);
		pump.configure();
	}

	@Test
	public void testStop() throws DeviceException {
		pump.stop();
		verify(control).stop();
	}

	@Test
	public void testInfuse() throws Exception {
		pump.infuse(23);
		verify(control).infuse(23);
	}

	@Test
	public void testGetVolume() throws DeviceException {
		pump.getVolume();
		verify(control).getVolume();
	}

	@Test
	public void testGetCapacity() {
		pump.getCapacity();
		verify(control).getCapacity();
	}

	@Test
	public void testIsBusy() throws DeviceException {
		pump.isBusy();
		verify(control).isBusy();
	}

	@Test(expected = DeviceException.class)
	public void testExceptionsNotIgnoredByInfuse() throws Exception {
		doThrow(new DeviceException("Device is busy")).when(control).infuse(17.0);
		pump.infuse(17);
	}
	@Test(expected = FactoryException.class)
	public void testConfigureWithoutController() throws Exception {
		pump = new SyringePump();
		pump.setName("dummypump");
		pump.configure();
	}

	@Test(expected = FactoryException.class)
	public void testConfigureWithoutName() throws Exception {
		pump = new SyringePump();
		pump.setController(control);
		pump.configure();
	}

	@Test
	public void testRemainingTime() throws DeviceException {
		when(control.getInfuseRate()).thenReturn(12.5);
		assertEquals("Remaining time not correct", 70 / 12.5, pump.getRemainingTime(), 0.001);
	}

	@Test
	public void testSetVolume() throws DeviceException {
		pump.setVolume(40.0);
		verify(control).setVolume(40.0);
	}

	@Test(expected = IllegalStateException.class)
	public void testErrorStateWhenVolumeGreaterThanCapacity() throws DeviceException {
		when(control.getVolume()).thenThrow(new IllegalStateException());
		pump.getVolume();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetVolumeGreaterThanCapacity() throws DeviceException {
		doThrow(new IllegalArgumentException()).when(control).setVolume(110.0);
		pump.setVolume(110);
	}

	@Test
	public void testGetController() {
		// anything for the code coverage stats
		assertEquals("Controller not returned", control, pump.getController());
	}

	@Test
	public void testToString() throws DeviceException {
		// normal
		String expected = "testSyringePump - Capacity: 100.0000, currentVolume: 70.0000";
		assertEquals(expected, pump.toString());
		// controller not enabled
		when(control.getCapacity()).thenReturn(100.0);
		String controllerNonEnabled = "testSyringePump - Controller is not enabled";
		when(control.isEnabled()).thenReturn(false);
		assertEquals(controllerNonEnabled, pump.toString());
		// Error reading volume
		when(control.getVolume()).thenThrow(new DeviceException("ERROR"));
		when(control.isEnabled()).thenReturn(true);
		String volumeError = "testSyringePump - Could not get current volume from controller: ERROR";
		assertEquals(volumeError, pump.toString());
		// pump not configured
		pump = new SyringePump();
		String errorStateExpected = "null - is not configured"; // null as no name set
		assertEquals(errorStateExpected, pump.toString());
	}
}
