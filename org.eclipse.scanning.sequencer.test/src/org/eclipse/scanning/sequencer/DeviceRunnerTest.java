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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.LevelRole;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
		when(detector1.getName()).thenReturn("detector1");
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
		final Collection<IRunnableDevice<? extends INameable>> devices = deviceRunner.getDevices();
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
	public void testAbortDevicesAbortedBeforeThreadInterrupted() throws Exception {
		deviceRunner = new DeviceRunner(scan, asList(detector1));
	
		// DeviceRunner.RunTask sets detector busy to true immediately before calling run
		// and to false in immediately after run returns or throws (i.e. in a finally block)
		final AtomicBoolean detectorBusy = new AtomicBoolean(false);
		doAnswer(invocation -> {
			detectorBusy.set((Boolean) invocation.getArgument(0)); return null; 
		}).when(detector1).setBusy(anyBoolean());

		
		final WaitingAnswer<Void> runAnswer = new WaitingAnswer<>(null); 
		doAnswer(runAnswer).when(detector1).run(position);
		
		// run the DeviceRunner in a different thread
		final AtomicReference<Exception> exception = new AtomicReference<>();
		final AtomicBoolean runReturned = new AtomicBoolean(false);
		final Thread runThread = new Thread(() -> {
			try {
				deviceRunner.run(position);
			} catch (ScanningException | InterruptedException e) {
				exception.set(e);
			} finally {
				runReturned.set(true);
			}
		});
		
		assertThat(detectorBusy.get(), is(false));
		
		// start the thread and wait until our run answer is called
		runThread.start();
		runAnswer.waitUntilCalled();
		
		// verify that run was called and busy set to true 
		assertThat(detectorBusy.get(), is(true));
		verify(detector1).run(position);

		// abort the device runner in a different thread - as it waits with a timeout for the tasks to  
		Thread abortThread = new Thread(() -> {
			deviceRunner.abort();
		});
		abortThread.start();
		
		// verify that abort was called, but that setBusy is still true, i.e.
		// the thread has not been interrupted
		verify(detector1, timeout(2000)).abort();
		assertThat(detectorBusy.get(), is(true)); 
		assertThat(exception.get(), is(nullValue()));
		
		// resume the run answer and wait for both the run and abort threads to finish
		runAnswer.resume();
		abortThread.join(5000);
		runThread.join(5000);
		assertThat(detectorBusy.get(), is(false));
		assertThat(exception.get(), is(instanceOf(InterruptedException.class)));
	}

	@Test
	public void testGetLevelRole() {
		deviceRunner = new DeviceRunner(scan, asList(detector1));
		assertEquals(LevelRole.RUN, deviceRunner.getLevelRole());
	}
}
