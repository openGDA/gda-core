/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.TopupWatchdogModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.example.scannable.MockTopupScannable;
import org.eclipse.scanning.sequencer.watchdog.TopupWatchdog;
import org.eclipse.scanning.test.scan.nexus.DummyMalcolmDeviceTest;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class WatchdogTopupTest extends AbstractWatchdogTest {

	private static IDeviceWatchdog<TopupWatchdogModel> dog;
	private static MockTopupScannable topup;

	@BeforeAll
	public static void createWatchdogs() throws ScanningException {
		final IScannable<Number>   topups  = scannableDeviceService.getScannable("topup");
		topup   = (MockTopupScannable)topups;

		// We create a device watchdog (done in spring for real server)
		final TopupWatchdogModel model = new TopupWatchdogModel();
		model.setCountdownName("topup");
		model.setCooloff(500); // Pause 500ms before
		model.setWarmup(200);  // Unpause 200ms after
		model.setTopupTime(150);
		model.setPeriod(5000);

		dog = new TopupWatchdog(model);
		dog.setName("topupDog");
		dog.activate();
	}

	@AfterEach
	public void disconnect() throws Exception {
		assertNotNull(topup);
		topup.disconnect();
		topup.setPosition(1000);
	}

	@Test
	public void dogsSame() {
		assertEquals(dog, ServiceProvider.getService(IDeviceWatchdogService.class).getWatchdog("topupDog"));
	}

	@Test
	public void testBeamOn() throws Exception {
		detector.getModel().setExposureTime(0.001);

		System.out.println(topup.getPosition());

		final IScannable<Number>   beamon   = scannableDeviceService.getScannable("beamon");
		beamon.setLevel(1);

		// x and y are level 3
		IDeviceController controller = createTestScanner(beamon);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();
		try {
			scanner.run(null);
		} catch (ScanningException e) {
			assertTrue(e.getMessage().equals("Cannot run scan further!"));
		}
		assertEquals(10, positions.size());
	}


	@Test
	public void topupPeriod() throws Exception {

		long fastPeriod = 500;
		long orig = topup.getPeriod();
		topup.setPeriod(fastPeriod);
		topup.start();
		Thread.sleep(100);

		try {
			int max = Integer.MIN_VALUE;
			int min = Integer.MAX_VALUE;
			// We start a check the topup value for 15s
			long start = System.currentTimeMillis();
			while((System.currentTimeMillis()-start)<(fastPeriod*2.1)) {
				int pos = topup.getPosition().intValue();
				max = Math.max(max, pos);
				min = Math.min(min, pos);
				Thread.sleep(fastPeriod/10);
			}
			assertTrue("The max is "+max, max<600&&max>300);
			assertTrue("The min is "+min, min<500&&min>-1);

		} finally {
			topup.setPeriod(orig);

		}
	}

	@Test
	public void topupIn2DScan() throws Exception {
        topupInScan(2, 0.05);
	}

	@Test
	public void topupIn3DScan() throws Exception {
        topupInScan(3, 0.05);
	}

	@Disabled("Needs to work and does but takes a long time so not part of main tests.")
	@Test
	public void topupIn5DScan() throws Exception {
        topupInScan(5);
	}

	@Test
	public void topupIn2DScanMalcolm() throws Exception {

		DummyMalcolmModel model = createModel();
		IRunnableDevice<IMalcolmModel> malcolmDevice = TestDetectorHelpers
				.createDummyMalcolmDetector();

		topupInScan(malcolmDevice, model, 2, 0.05);
	}

	@Test
	public void topupIn3DScanMalcolm() throws Exception {

		DummyMalcolmModel model = createModel();
		IRunnableDevice<IMalcolmModel> malcolmDevice = TestDetectorHelpers
				.createDummyMalcolmDetector();

		topupInScan(malcolmDevice, model, 3, 0.05);
	}

	@Test
	public void topupSeveralMalcolm() throws Exception {

		DummyMalcolmModel model = createModel();
		IRunnableDevice<IMalcolmModel> malcolmDevice = TestDetectorHelpers
				.createDummyMalcolmDetector();

		topupInScan(malcolmDevice, model, 2, 0.05);

		// We do another one to see if 2 in a row are the problem
		topupInScan(malcolmDevice, model, 3, 0.05);

		// We do another one to see if 3 in a row are the problem
		topupInScan(malcolmDevice, model, 3, 0.05);
	}


	private DummyMalcolmModel createModel() {

		DummyMalcolmModel model = DummyMalcolmDeviceTest.createModel();
		model.setExposureTime(0.001);
		model.setAxesToMove(Arrays.asList("stage_x", "stage_y"));
		return model;
	}

	private void topupInScan(int size) throws Exception {
		topupInScan(detector, null, size, 0.001);
	}

	private void topupInScan(int size, double exposureTime) throws Exception {
		topupInScan(detector, null, size, exposureTime);
	}

	private <T extends IDetectorModel> void topupInScan(IRunnableDevice<T> device, T detectorModel, int size, double exposureTime) throws Exception {

        topup.start();

		// x and y are level 3
		if (detectorModel != null) {
			detectorModel.setExposureTime(exposureTime);
		}
		if (device != null && device.getModel() != null) {
			device.getModel().setExposureTime(exposureTime);
		}
		IDeviceController controller = createTestScanner(null, device, detectorModel, size);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>) controller.getDevice();

		// This run should get paused for beam and restarted.
		Set<DeviceState> states = new HashSet<>();
		scanner.addRunListener(IRunListener.createStateChangedListener(evt -> states.add(evt.getDeviceState())));

		scanner.run(null);
		assertTrue(!controller.isActive());

		assertTrue("States contain no paused: "+states,  states.contains(DeviceState.PAUSED));
		assertTrue("States contain no running: "+states, states.contains(DeviceState.RUNNING));
		assertTrue("States contain no seeking: "+states, states.contains(DeviceState.SEEKING));
	}

	@Test
	public void scanDuringTopup() throws Exception {

		// Stop topup, we want to controll it programmatically.
		topup.disconnect();
		Thread.sleep(120); // Make sure it stops, it sets value every 100ms but it should get interrupted
		assertFalse(topup.isConnected());
		topup.setPosition(10);

		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();

		// This run should get paused for beam and restarted.
		final Set<DeviceState> states = new HashSet<>();
		scanner.addRunListener(IRunListener.createStateChangedListener(event -> states.add(event.getDeviceState())));

		scanner.start(null);
		scanner.latch(200, TimeUnit.MILLISECONDS);
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		topup.setPosition(0);    // Should do nothing, device is already paused
		topup.setPosition(5000); // Gets it ready to think it has to resume
		topup.setPosition(4000); // Will resume it because warmup passed

		Thread.sleep(100);       // Ensure watchdog event has fired and it did something.
		assertEquals(DeviceState.RUNNING, scanner.getDeviceState()); // Should still be paused

		scanner.latch();

		assertEquals(DeviceState.ARMED, scanner.getDeviceState()); // Should still be paused
	}


	@Test
	public void topupWithExternalPause() throws Exception {

		// Stop topup, we want to controll it programmatically.
		topup.disconnect();
		Thread.sleep(120); // Make sure it stops, it sets value every 100ms but it should get interrupted
		assertFalse(topup.isConnected());
		topup.setPosition(5000);

		// x and y are level 3
		if (detector != null && detector.getModel() != null) {
			detector.getModel().setExposureTime(0.05);
		}
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();

		Set<DeviceState> states = new HashSet<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(IRunListener.createStateChangedListener(event -> states.add(event.getDeviceState())));

		scanner.start(null);
		scanner.latch(200, TimeUnit.MILLISECONDS);
		controller.pause("test", null);   // Pausing externally should override any watchdog resume.

		topup.setPosition(0);    // Should do nothing, device is already paused
		topup.setPosition(5000); // Gets it ready to think it has to resume
		topup.setPosition(4000); // Will resume it because warmup passed

		scanner.latch(100, TimeUnit.MILLISECONDS);
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState()); // Should still be paused

		controller.resume("test");

		scanner.latch(100, TimeUnit.MILLISECONDS);
		assertNotEquals(DeviceState.PAUSED, scanner.getDeviceState());

		controller.pause("test", null);   // Pausing externally should override any watchdog resume.
		topup.setPosition(0);

		scanner.latch(100, TimeUnit.MILLISECONDS);
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		controller.resume("test"); // It shouldn't because now topup has set to pause.

		scanner.latch(25, TimeUnit.MILLISECONDS);
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		try {
			controller.abort("test");
		} catch (InterruptedException e) {
			boolean aborted = scanner.latch(100, TimeUnit.MILLISECONDS); // true if the countdown reached 0, false if we timed-out
			assertTrue(aborted);
			assertEquals(DeviceState.ABORTED, scanner.getDeviceState());
		}

	}


	@Test
	public void topupDeactivated() throws Exception {

		try {
			// Deactivate!=disabled because deactivate removes it from the service.
			dog.deactivate(); // Are a testing a pausing monitor here
			runQuickie();
		} finally {
			dog.activate();
		}
	}


	@Test
	public void topupDisabled() throws Exception {

		try {
			dog.setEnabled(false); // Are a testing a pausing monitor here
			runQuickie();
		} finally {
			dog.setEnabled(true);
		}
	}


	@Test
	public void testPause() throws Exception {

		try {
			dog.deactivate(); // Are a testing a pausing monitor here
			detector.getModel().setExposureTime(0.0001); // Save some scan time.

			final List<String> moved   = new ArrayList<>();
			final IScannable<Number>   pauser   = scannableDeviceService.getScannable("pauser");
			if (pauser instanceof MockScannable) {
				((MockScannable)pauser).setRealisticMove(false);
			}
			((IPositionListenable)pauser).addPositionListener(new IPositionListener() {
				@Override
				public void positionPerformed(PositionEvent evt) {
					moved.add(pauser.getName());
				}
			});
			final IScannable<Number>   x       = scannableDeviceService.getScannable("x");
			if (x instanceof MockScannable) {
				((MockScannable)x).setRealisticMove(false);
			}
			((IPositionListenable)x).addPositionListener(new IPositionListener() {
				@Override
				public void positionPerformed(PositionEvent evt) {
					moved.add(x.getName());
				}
			});
			pauser.setLevel(1);

			// x and y are level 3
			IDeviceController controller = createTestScanner(pauser);
			IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();
			scanner.run(null);

			assertEquals(25, positions.size());
			assertEquals(50, moved.size());
			assertTrue(moved.get(0).equals("pauser"));
			assertTrue(moved.get(1).equals("x"));

			moved.clear();
			positions.clear();
			pauser.setLevel(5); // Above x
			try {
				scanner.run(null);
			} catch (ScanningException e) {
				assertTrue(e.getMessage().equals("Interrupted while performing move"));
			}

		} finally {
			dog.activate();
		}
	}

}
