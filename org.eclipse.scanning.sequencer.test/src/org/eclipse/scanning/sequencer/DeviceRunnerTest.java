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

package org.eclipse.scanning.sequencer;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.LevelRole;
import org.eclipse.scanning.api.scan.ScanningException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeviceRunnerTest {
	@Mock
	private INameable scan;

	@Mock
	private AbstractRunnableDevice<INameable> deviceNotTimeoutable;

	@Mock
	private AbstractRunnableDevice<IDetectorModel> detector1;

	@Mock
	private AbstractRunnableDevice<IDetectorModel> detector2;

	@Mock
	private IDetectorModel modelTimeoutZero;

	@Mock
	private IDetectorModel modelTimeout1second;

	@Mock
	private IDetectorModel modelTimeout2seconds;

	@Mock
	private IDetectorModel modelTimeoutInfinity;

	@Mock
	private IPosition position;

	private DeviceRunner deviceRunner;

	@Before
	public void setUp() {
		when(scan.getName()).thenReturn("Test DeviceRunner");

		when(deviceNotTimeoutable.getName()).thenReturn("deviceNotTimeoutable");
		when(detector1.getName()).thenReturn("detector1");
		when(detector2.getName()).thenReturn("detector2");

		when(modelTimeoutZero.getTimeout()).thenReturn(0L);
		when(modelTimeoutZero.getExposureTime()).thenReturn(3.4);
		when(modelTimeout1second.getTimeout()).thenReturn(1L);
		when(modelTimeout2seconds.getTimeout()).thenReturn(2L);
		when(modelTimeoutInfinity.getTimeout()).thenReturn(Long.MAX_VALUE);
	}

	/**
	 * Test with a device that does not implement {@link org.eclipse.scanning.api.device.models.IDetectorModel}
	 */
	@Test
	public void testDefaultTimeout() {
		final INameable mockModel = mock(INameable.class);
		when(deviceNotTimeoutable.getModel()).thenReturn(mockModel);
		deviceRunner = new DeviceRunner(scan, asList(deviceNotTimeoutable));
		assertEquals(10, deviceRunner.getTimeout(), 0.001);
	}

	@Test
	public void testGetTimeoutFromDevice() {
		when(detector1.getModel()).thenReturn(modelTimeout1second);
		deviceRunner = new DeviceRunner(scan, asList(detector1));
		assertEquals(1, deviceRunner.getTimeout());
	}

	@Test
	public void testGetTimeoutUsesExposureTimeIfNoDeviceTimeout() {
		when(detector1.getModel()).thenReturn(modelTimeoutZero);
		deviceRunner = new DeviceRunner(scan, asList(detector1));
		assertEquals(5, deviceRunner.getTimeout());
	}

	@Test
	public void testGetTimeoutUsesMaximumDeviceTimeout() {
		when(detector1.getModel()).thenReturn(modelTimeout1second);
		when(detector2.getModel()).thenReturn(modelTimeout2seconds);
		deviceRunner = new DeviceRunner(scan, asList(detector1, detector2));
		assertEquals(2, deviceRunner.getTimeout());
	}

	@Test
	public void testGetDevices() {
		deviceRunner = new DeviceRunner(scan, asList(detector1, detector2));
		final Collection<IRunnableDevice<?>> devices = deviceRunner.getDevices();
		assertEquals(2, devices.size());
		assertTrue(devices.contains(detector1));
		assertTrue(devices.contains(detector2));
	}

	@Test
	public void testRun() throws Exception {
		when(detector1.getModel()).thenReturn(modelTimeout1second);
		deviceRunner = new DeviceRunner(scan, asList(detector1));
		deviceRunner.run(position);

		verify(detector1).fireRunWillPerform(position);
		verify(detector1).setBusy(true);
		verify(detector1).run(position);
		verify(detector1).setBusy(false);
		verify(detector1).fireRunPerformed(position);
	}

	@Test(expected = ScanningException.class)
	public void testRunAbortsIfDetectorThrowsException() throws Exception {
		when(detector1.getModel()).thenReturn(modelTimeoutInfinity);
		doThrow(new ScanningException("Failure in scanning")).when(detector1).run(any(IPosition.class));
		deviceRunner = new DeviceRunner(scan, asList(detector1));
		deviceRunner.run(position);
	}

	@Test
	public void testGetLevelRole() {
		deviceRunner = new DeviceRunner(scan, asList(detector1));
		assertEquals(LevelRole.RUN, deviceRunner.getLevelRole());
	}
}
