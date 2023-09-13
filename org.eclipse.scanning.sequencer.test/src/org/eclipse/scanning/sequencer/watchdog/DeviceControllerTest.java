package org.eclipse.scanning.sequencer.watchdog;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.models.IDeviceWatchdogModel;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.status.WatchdogStatusRecord;
import org.eclipse.scanning.api.event.status.WatchdogStatusRecord.WatchdogState;
import org.eclipse.scanning.server.servlet.ScanProcess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Test {@link DeviceController}
 *
 * This is currently a skeleton test class, created as a regression test when
 * refactoring.<br>
 * At a future date, it should be expanded to test more functionality.
 */
class DeviceControllerTest {
	
	private DeviceController controller;
	private IPausableDevice<?> device;

	private IDeviceWatchdog<?> watchdog1;
	private IDeviceWatchdog<?> watchdog2;
	
	private IPublisher<WatchdogStatusRecord> publisher;
	
	@BeforeEach
	public void setUp() {
		mockWatchdogStatusPublisher();
		
		device = mock(IPausableDevice.class);
		controller = new DeviceController(device);

		watchdog1 = mock(IDeviceWatchdog.class);
		watchdog2 = mock(IDeviceWatchdog.class);
		
		when(watchdog1.getId()).thenReturn("id1");
		when(watchdog2.getId()).thenReturn("id2");
		
		var watchdogs = List.of(watchdog1, watchdog2);
		controller.configure(watchdogs);
	}
	
	@AfterEach
	public void tearDown() {
		ServiceProvider.reset();
	}
	
	@SuppressWarnings("unchecked")
	private void mockWatchdogStatusPublisher() {
		System.setProperty("org.eclipse.scanning.broker.uri", "");
		IEventService eventService = mock(IEventService.class);
		ServiceProvider.setService(IEventService.class, eventService);
		publisher = mock(IPublisher.class);
		doReturn(publisher).when(eventService).createPublisher(any(), eq(EventConstants.WATCHDOG_STATUS_TOPIC));
	}

	@Test
	void isActiveAllWatchdogsActive() {
		when(watchdog1.isActive()).thenReturn(true);
		when(watchdog2.isActive()).thenReturn(true);

		controller.setObjects(Arrays.asList(watchdog1, watchdog2));
		assertTrue(controller.isActive());
	}

	@Test
	void isActiveIgnoresNonWatchdogs() {
		when(watchdog1.isActive()).thenReturn(true);
		when(watchdog2.isActive()).thenReturn(true);

		controller.setObjects(Arrays.asList(watchdog1, new InactiveDevice(), watchdog2));
		assertTrue(controller.isActive());
	}

	@Test
	void allWatchdogsMustBeActive() {
		when(watchdog1.isActive()).thenReturn(true);
		when(watchdog2.isActive()).thenReturn(false);

		controller.setObjects(Arrays.asList(watchdog1, watchdog2));
		assertFalse(controller.isActive());
	}
	
	@Test
	void resumeCalledOnDeviceWhenDeviceIsPaused() throws Exception {
		// device is paused and watchdog wants to resume
		when(device.getDeviceState()).thenReturn(DeviceState.PAUSED);
		controller.resume("id1");
		
		// device called resume
		verify(device, times(1)).resume();
	}
	
	@Test
	void resumeNotCalledOnDeviceWhenDeviceIsRunning() throws Exception {
		// device is paused and watchdog wants to resume
		when(device.getDeviceState()).thenReturn(DeviceState.RUNNING);
		controller.resume(watchdog1.getId());
		
		// device called resume
		verify(device, times(0)).resume();
	}
	
	@Test
	void resumeNotCalledOnDeviceWhenWatchdogIsPausing() throws Exception {
		// simulate scan was paused by a watchdog
		controller.pause("id", mock(IDeviceWatchdogModel.class));
		
		when(device.getDeviceState()).thenReturn(DeviceState.PAUSED);
		
		// attempt to resume when another watchdog is in a paused state
		controller.resume(watchdog2.getId());
		
		// different id, so resume not invoked on device
		verify(device, times(0)).resume();
	}
	
	@Test
	void scanProcessCanAlwaysResume() throws Exception {
		// simulate scan was paused by a watchdog
		controller.pause("id", mock(IDeviceWatchdogModel.class));
		
		// attempt to resume when another watchdog is in a paused state
		when(device.getDeviceState()).thenReturn(DeviceState.PAUSED);
		
		controller.resume(ScanProcess.class.getCanonicalName());
		
		// device called resume
		verify(device, times(1)).resume();
	}
	
	@Test
	void seekCalledOnDeviceWhenControllerCallsSeek() throws Exception {
		var stepNumber = 5;
		when(device.getDeviceState()).thenReturn(DeviceState.PAUSED);
		controller.seek("id", stepNumber);
		verify(device, times(1)).seek(stepNumber);
	}
	
	@Test
	void seekNotCalledOnDeviceWhenWatchdogIsPausing() throws Exception {
		// simulate scan was paused by a watchdog
		controller.pause("id", mock(IDeviceWatchdogModel.class));
		
		var stepNumber = 5;
		when(device.getDeviceState()).thenReturn(DeviceState.PAUSED);
		controller.seek("id2", stepNumber);
		verify(device, times(0)).seek(stepNumber);
	}
	
	@Test
	void pausedCalledOnDeviceWhenDeviceIsRunning() throws Exception {
		when(device.getDeviceState()).thenReturn(DeviceState.RUNNING);
		controller.pause("id1", mock(IDeviceWatchdogModel.class));
		verify(device, times(1)).pause();
	}	
	
	@Test
	void pausedNotCalledOnDeviceWhenDeviceIsPaused() throws Exception {
		when(device.getDeviceState()).thenReturn(DeviceState.PAUSED);
		controller.pause("id1", mock(IDeviceWatchdogModel.class));
		verify(device, times(0)).pause();
	}

	@Test
	void watchdogStatusPublishedOnceWhenRegisteredWatchdogPausesTwice() throws Exception {
		controller.pause(watchdog1.getId(), mock(IDeviceWatchdogModel.class));
		controller.pause(watchdog1.getId(), mock(IDeviceWatchdogModel.class));
		verify(publisher, times(1)).broadcast(new WatchdogStatusRecord(watchdog1.getName(), WatchdogState.PAUSING, true));
	}
	
	@Test
	void watchdogStatusPublishedWhenRegisteredWatchdogChangesState() throws Exception {
		controller.pause(watchdog1.getId(), mock(IDeviceWatchdogModel.class));
		controller.resume(watchdog1.getId());
		verify(publisher, times(1)).broadcast(new WatchdogStatusRecord(watchdog1.getName(), WatchdogState.PAUSING, true));
		verify(publisher, times(1)).broadcast(new WatchdogStatusRecord(watchdog1.getName(), WatchdogState.RESUMING, true));
	}
	
	@Test
	void watchdogStatusNotPublishedWhenScanProcessResumes() throws Exception {
		controller.resume(ScanProcess.class.getCanonicalName());
		verify(publisher, times(0)).broadcast(any());
	}
	
	@Test
	void watchdogStatusNotPublishedWhenExternalProcessResumes() throws Exception {
		controller.resume("id");
		verify(publisher, times(0)).broadcast(any());
	}
	
	@Test
	void abortCalledOnDeviceWhenControllerCalledAbort() throws Exception {
		controller.abort("id1");
		verify(device, times(1)).abort();
	}
	
	private class InactiveDevice {
		@SuppressWarnings("unused")
		public boolean isActive() {
			return false;
		}
	}
}
