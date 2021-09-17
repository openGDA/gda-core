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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.models.ExpressionWatchdogModel;
import org.eclipse.scanning.api.device.models.TopupWatchdogModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.example.scannable.MockTopupScannable;
import org.eclipse.scanning.sequencer.expression.ServerExpressionService;
import org.eclipse.scanning.sequencer.watchdog.ExpressionWatchdog;
import org.eclipse.scanning.sequencer.watchdog.TopupWatchdog;
import org.eclipse.scanning.server.servlet.Services;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class WatchdogCombinedTest extends AbstractWatchdogTest {

	private static IDeviceWatchdog<ExpressionWatchdogModel> expressionWatchdog;
	private static IDeviceWatchdog<TopupWatchdogModel> topupWatchdog;

	@BeforeClass
	public static void createWatchdogs() throws Exception {

		assertNotNull(connector.getScannable("beamcurrent"));
		assertNotNull(connector.getScannable("portshutter"));

		ExpressionWatchdog.setTestExpressionService(new ServerExpressionService());

		// We create a device watchdog (done in spring for real server)
		final ExpressionWatchdogModel expressionModel = new ExpressionWatchdogModel();
		expressionModel.setExpression("beamcurrent >= 1.0 && !portshutter.equalsIgnoreCase(\"Closed\")");

		expressionWatchdog = new ExpressionWatchdog(expressionModel);
		expressionWatchdog.activate();

		final IScannable<Number> topups = connector.getScannable("topup");
		final MockTopupScannable topup = (MockTopupScannable) topups;
		assertNotNull(topup);
		topup.disconnect();
		Thread.sleep(120); // Make sure it stops, it sets value every 100ms but it should get interrupted
		assertFalse(topup.isConnected());
		topup.setPosition(1000);
		assertTrue("Topup is "+topup.getPosition(), topup.getPosition().doubleValue()>=1000);

		// We create a device watchdog (done in spring for real server)
		final TopupWatchdogModel topupModel = new TopupWatchdogModel();
		topupModel.setCountdownName("topup");
		topupModel.setCooloff(500); // Pause 500ms before
		topupModel.setWarmup(200);  // Unpause 200ms after
		topupModel.setTopupTime(150);
		topupModel.setPeriod(5000);

		topupWatchdog = new TopupWatchdog(topupModel);
		topupWatchdog.activate();

	}

	@Before
	public void before() throws Exception {
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
		topup.disconnect();
		Thread.sleep(120); // Make sure it stops, it sets value every 100ms but it should get interrupted
		assertFalse(topup.isConnected());
		topup.setPosition(1000);
		assertTrue("Topup is "+topup.getPosition(), topup.getPosition().doubleValue()>=1000);

		assertNotNull(connector.getScannable("beamcurrent"));
		assertNotNull(connector.getScannable("portshutter"));

		connector.getScannable("beamcurrent").setPosition(5d);
		connector.getScannable("portshutter").setPosition("Open");
	}

	@Test
	public void dogsSame() {
		assertEquals(expressionWatchdog, Services.getWatchdogService().getWatchdog("ExpressionWatchdog"));
		assertEquals(topupWatchdog, Services.getWatchdogService().getWatchdog("TopupWatchdog"));
	}

	@Test
	public void deactivated() throws Exception {
		try {
			// Deactivate!=disabled because deactivate removes it from the service.
			expressionWatchdog.deactivate(); // Are a testing a pausing monitor here
			topupWatchdog.deactivate(); // Are a testing a pausing monitor here
			runQuickie();
		} finally {
			expressionWatchdog.activate();
			topupWatchdog.activate();
		}
	}


	@Test
	public void disabled() throws Exception {
		try {
			expressionWatchdog.setEnabled(false); // Are a testing a pausing monitor here
			topupWatchdog.setEnabled(false); // Are a testing a pausing monitor here
			IRunnableEventDevice<?> scanner = runQuickie(true);
			final IScannable<String>   mon  = connector.getScannable("portshutter");
			mon.setPosition("Closed");
			final IScannable<Number>   topup  = connector.getScannable("topup");
			topup.setPosition(10);
			assertTrue(scanner.latch(10, TimeUnit.SECONDS));

		} finally {
			expressionWatchdog.setEnabled(true);
			topupWatchdog.setEnabled(true);
		}
	}

	@Test
	public void deactivatedExpression() throws Exception {
		try {
			// Deactivate!=disabled because deactivate removes it from the service.
			expressionWatchdog.deactivate(); // Are a testing a pausing monitor here
			runQuickie();
		} finally {
			expressionWatchdog.activate();
		}
	}

	@Test
	public void disabledExpression() throws Exception {
		try {
			// Deactivate!=disabled because deactivate removes it from the service.
			expressionWatchdog.setEnabled(false); // Are a testing a pausing monitor here
			IRunnableEventDevice<?> scanner = runQuickie(true);
			final IScannable<String>   mon  = connector.getScannable("portshutter");
			mon.setPosition("Closed");
			assertTrue(scanner.latch(10, TimeUnit.SECONDS));
		} finally {
			expressionWatchdog.setEnabled(true);
		}
	}

	@Test
	public void deactivatedTopup() throws Exception {
		try {
			// Deactivate!=disabled because deactivate removes it from the service.
			topupWatchdog.deactivate(); // Are a testing a pausing monitor here
			runQuickie();
		} finally {
			topupWatchdog.activate();
		}
	}

	@Test
	public void startWhenExpressionWatchdogEvaluatesFalse() throws Exception {
		// make edog evaluate to false
		connector.getScannable("beamcurrent").setPosition(0.5);

		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();

		scanner.start(null);
		scanner.latch(500, TimeUnit.MILLISECONDS);

		// the device should be now be paused
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());
	}

	@Test
	public void disabledTopup() throws Exception {
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);

		long fastPeriod = 500;
		long orig = topup.getPeriod();
		topup.setPeriod(fastPeriod);
		topup.start();
		Thread.sleep(100);
		try {
			// Deactivate!=disabled because deactivate removes it from the service.
			topupWatchdog.setEnabled(false); // Are a testing a pausing monitor here
			IRunnableEventDevice<?> scanner = runQuickie(true);
			assertTrue(scanner.latch(10, TimeUnit.SECONDS));
		} finally {
			topupWatchdog.setEnabled(true);
			topup.setPeriod(orig);
		}
	}

	@Test
	public void shutterWithExternalPause() throws Exception {
		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();

		Set<DeviceState> states = new HashSet<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(IRunListener.createStateChangedListener(event -> states.add(event.getDeviceState())));

		scanner.start(null);
		scanner.latch(200, TimeUnit.MILLISECONDS);
		controller.pause("test", null);  // Pausing externally should override any watchdog resume.

		final IScannable<String>   mon  = connector.getScannable("portshutter");
		mon.setPosition("Closed");
		mon.setPosition("Open");
		scanner.latch(100, TimeUnit.MILLISECONDS);
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		controller.resume("test");

		scanner.latch(100, TimeUnit.MILLISECONDS);
		assertNotEquals(DeviceState.PAUSED, scanner.getDeviceState());

		mon.setPosition("Closed");

		scanner.latch(100, TimeUnit.MILLISECONDS);
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		controller.resume("test"); // The external resume should still not resume it

		scanner.latch(100, TimeUnit.MILLISECONDS);
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		controller.abort("test");
	}

	@Test
	public void topupWithExternalPause() throws Exception {
		// Stop topup, we want to controll it programmatically.
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
		topup.disconnect();
		Thread.sleep(120); // Make sure it stops, it sets value every 100ms but it should get interrupted
		assertFalse(topup.isConnected());
		topup.setPosition(5000);

		// x and y are level 3
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

		topup.setPosition(0);

		scanner.latch(100, TimeUnit.MILLISECONDS);
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		controller.resume("test"); // It shouldn't because now topup has set to pause.

		scanner.latch(25, TimeUnit.MILLISECONDS);
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		controller.abort("test");
	}

	@Test
	public void shutterAndTopupWithExternalPause() throws Exception {

		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);

		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();

		Set<DeviceState> states = new HashSet<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(IRunListener.createStateChangedListener(event -> states.add(event.getDeviceState())));

		scanner.start(null);
		Thread.sleep(25);  // Do a bit
		controller.pause("test", null);   // Pausing externally should override any watchdog resume.

		topup.setPosition(0);    // Should do nothing, device is already paused
		topup.setPosition(5000); // Gets it ready to think it has to resume
		topup.setPosition(4000); // Will resume it because warmup passed

		Thread.sleep(100);       // Ensure watchdog event has fired and it did something.
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState()); // Should still be paused

		final IScannable<String>   mon  = connector.getScannable("portshutter");
		mon.setPosition("Closed");
		mon.setPosition("Open");
		Thread.sleep(100); // Watchdog should not start it again, it was paused first..
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		controller.resume("test");

		Thread.sleep(100);
		assertNotEquals(DeviceState.PAUSED, scanner.getDeviceState());

		mon.setPosition("Closed");
		Thread.sleep(100); // Watchdog should not start it again, it was paused first..

		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		topup.setPosition(0);    // Should do nothing, device is already paused
		topup.setPosition(5000); // Gets it ready to think it has to resume
		topup.setPosition(4000); // Will resume it because warmup passed

		Thread.sleep(100);       // Ensure watchdog event has fired and it did something.
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState()); // Should still be paused

		mon.setPosition("Open");
		Thread.sleep(100); // Watchdog should not start it again, it was paused first..
		assertNotEquals(DeviceState.PAUSED, scanner.getDeviceState());

		topup.setPosition(100);    // Should do nothing, device is already paused
		Thread.sleep(100); // Watchdog should not start it again, it was paused first..
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		mon.setPosition("Open");
		Thread.sleep(100); // Watchdog should not start it again, it was paused first..

		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		topup.setPosition(5000); // Gets it ready to think it has to resume
		topup.setPosition(4000); // Will resume it because warmup passed

		Thread.sleep(100);       // Ensure watchdog event has fired and it did something.
		assertNotEquals(DeviceState.PAUSED, scanner.getDeviceState());
	}
}
