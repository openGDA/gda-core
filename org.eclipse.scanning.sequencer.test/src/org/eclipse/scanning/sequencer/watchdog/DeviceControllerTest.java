package org.eclipse.scanning.sequencer.watchdog;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link DeviceController}
 *
 * This is currently a skeleton test class, created as a regression test when
 * refactoring.<br>
 * At a future date, it should be expanded to test more functionality.
 */
public class DeviceControllerTest {

	private DeviceController controller;
	private IPausableDevice<?> device;

	private IDeviceWatchdog<?> watchdog1;
	private IDeviceWatchdog<?> watchdog2;

	@Before
	public void setUp() {
		device = mock(IPausableDevice.class);
		controller = new DeviceController(device);

		watchdog1 = mock(IDeviceWatchdog.class);
		watchdog2 = mock(IDeviceWatchdog.class);
	}

	@Test
	public void testIsActiveAllWatchdogsActive() {
		when(watchdog1.isActive()).thenReturn(true);
		when(watchdog2.isActive()).thenReturn(true);

		controller.setObjects(Arrays.asList(watchdog1, watchdog2));
		assertTrue(controller.isActive());
	}

	@Test
	public void testIsActiveIgnoresNonWatchdogs() {
		when(watchdog1.isActive()).thenReturn(true);
		when(watchdog2.isActive()).thenReturn(true);

		controller.setObjects(Arrays.asList(watchdog1, new InactiveDevice(), watchdog2));
		assertTrue(controller.isActive());
	}

	@Test
	public void testAllWatchdogsMustBeActive() {
		when(watchdog1.isActive()).thenReturn(true);
		when(watchdog2.isActive()).thenReturn(false);

		controller.setObjects(Arrays.asList(watchdog1, watchdog2));
		assertFalse(controller.isActive());
	}

	private class InactiveDevice {
		@SuppressWarnings("unused")
		public boolean isActive() {
			return false;
		}
	}
}
