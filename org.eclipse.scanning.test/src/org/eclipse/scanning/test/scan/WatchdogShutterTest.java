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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.models.ExpressionWatchdogModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.sequencer.expression.ServerExpressionService;
import org.eclipse.scanning.sequencer.watchdog.ExpressionWatchdog;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.util.WaitingScannable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class WatchdogShutterTest extends AbstractWatchdogTest {

	private static IDeviceWatchdog<ExpressionWatchdogModel> dog;
	private WaitingScannable yAxis;

	@BeforeAll
	public static void createWatchdogs() throws Exception {
		assertNotNull(connector.getScannable("beamcurrent"));
		assertNotNull(connector.getScannable("portshutter"));

		ExpressionWatchdog.setTestExpressionService(new ServerExpressionService());

		// We create a device watchdog (done in spring for real server)
		final ExpressionWatchdogModel model = new ExpressionWatchdogModel();
		model.setExpression("beamcurrent >= 1.0 && !portshutter.equalsIgnoreCase(\"Closed\")");

		dog = new ExpressionWatchdog(model);
		dog.setName("expr1");
		dog.activate();
	}

	@BeforeEach
	public void before() throws Exception {
		initializeWatchedScannables();
		initializeScanAxes();
	}

	private void initializeWatchedScannables() throws ScanningException {
		IScannable<Number> beamCurrent = connector.getScannable("beamcurrent");
		IScannable<String> portShutter = connector.getScannable("portshutter");
		assertNotNull(beamCurrent);
		assertNotNull(portShutter);

		try {
			beamCurrent.setPosition(5d);
			portShutter.setPosition("Open");
		} catch (NullPointerException e) {
			// This is to provide context as NPE stack traces are
			// often omitted due to a JVM optimisation
			e.printStackTrace(System.out);
			throw e;
		}
	}

	private void initializeScanAxes() throws InterruptedException, ScanningException {
		yAxis = new WaitingScannable("y", 0.);

		// Ensure yAxis has overridden the existing y Scannable
		connector.register(yAxis);
		assertEquals(yAxis, connector.getScannable("y"));

		// Ensure yAxis is where we expect
		assertEquals(0., yAxis.getPosition());
	}


	@Test
	public void dogsSame() {
		assertEquals(dog, Services.getWatchdogService().getWatchdog("expr1"));
	}

	@Test
	public void expressionDeactivated() throws Exception {
		try {
			// Deactivate!=disabled because deactivate removes it from the service.
			dog.deactivate(); // Are a testing a pausing monitor here
			runQuickie();
		} finally {
			dog.activate();
		}
	}


	@Test
	public void expressionDisabled() throws Exception {

		try {
			dog.setEnabled(false); // Are a testing a pausing monitor here
			runQuickie();
		} finally {
			dog.setEnabled(true);
		}
	}


	@Test
	public void beamLostInScan() throws Exception {
		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();

		startAndVerifyScan(scanner);

		killBeam();
		assertInState(scanner, DeviceState.PAUSED);

		startBeam();
		assertInState(scanner, DeviceState.RUNNING);

		verifyFinishes(scanner);
	}

	@Test
	public void shutterClosedInScan() throws Exception {
		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();

		startAndVerifyScan(scanner);

		closeShutter();
		assertInState(scanner, DeviceState.PAUSED);

		openShutter();
		assertInState(scanner, DeviceState.RUNNING);

		verifyFinishes(scanner);
	}

	@Test
	public void scanDuringShutterClosed() throws Exception {
		// Set a longer exposure to that scanner remains Running for longer, to prevent race conditions
		detector.getModel().setExposureTime(1.0);

		// Stop topup, we want to control it programmatically.
		closeShutter();

		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();

		// Can be used to block until the scan has gone through the expected 3 changes of state
		// We can't synchronise on yAxis here because it shouldn't move.
		CountDownLatch latch = latchUntilStateChanges(scanner, 3);
		scanner.start(null);

		latch.await();
		assertInState(scanner, DeviceState.PAUSED);

		controller.abort("test");
		verifyInterrupted(scanner);
	}

	@Test
	public void monitorWithExternalPauseSimple() throws Exception {
		// Set a longer exposure to that scanner remains Running for longer, to prevent race conditions
		detector.getModel().setExposureTime(1.0);

		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>) controller.getDevice();

		startAndVerifyScan(scanner);

		controller.pause("test", null);  // Pausing externally should override any watchdog resume.

		closeShutter();
		openShutter();

		assertInState(scanner, DeviceState.PAUSED);

		controller.abort("test");

		closeShutter();
		assertNotInState(scanner, DeviceState.PAUSED);

		verifyInterrupted(scanner);
	}

	@Test
	public void monitorWithExternalPauseComplex() throws Exception {
		// Set a longer exposure to that scanner remains Running for longer, to prevent race conditions
		detector.getModel().setExposureTime(1.0);

		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();

		startAndVerifyScan(scanner);

		controller.pause("test", null);  // Pausing externally should override any watchdog resume.

		closeShutter();
		openShutter();
		assertInState(scanner, DeviceState.PAUSED);

		controller.resume("test");
		assertNotInState(scanner, DeviceState.PAUSED);

		closeShutter();
		assertInState(scanner, DeviceState.PAUSED);

		controller.resume("test"); // The external resume should still not resume it
		assertInState(scanner, DeviceState.PAUSED);

		controller.abort("test");

		verifyInterrupted(scanner);
	}

	private void startAndVerifyScan(IRunnableEventDevice<?> scanner) throws InterruptedException, ScanningException, TimeoutException, ExecutionException {
		yAxis.enableBlocking();
		scanner.start(null);
		yAxis.waitForSetPosition();
		assertInState(scanner, DeviceState.RUNNING);
		yAxis.resume();
		yAxis.disableBlocking();
	}

	private void openShutter() throws ScanningException {
		setShutterPosition("Open");
	}

	private void closeShutter() throws ScanningException {
		setShutterPosition("Closed");
	}

	private void setShutterPosition(String pos) throws ScanningException {
		final IScannable<String> shutter = connector.getScannable("portshutter");

		// This should block until the watchdog has done its job
		shutter.setPosition(pos);
	}

	private void killBeam() throws ScanningException {
		setBeamCurrent(0.1);
	}

	private void startBeam() throws ScanningException {
		setBeamCurrent(2.1);
	}

	private void setBeamCurrent(double current) throws ScanningException {
		final IScannable<Number> currentMonitor = connector.getScannable("beamcurrent");

		// This should block until the watchdog has done its job
		currentMonitor.setPosition(current);
	}

	private void assertInState(
			IRunnableEventDevice<?> scanner,
			DeviceState expectedState) throws ScanningException {
		assertEquals(expectedState, scanner.getDeviceState());
	}

	private void assertNotInState(
			IRunnableEventDevice<?> scanner,
			DeviceState unexpectedState) throws ScanningException {
		assertNotEquals(unexpectedState, scanner.getDeviceState());
	}

	private void verifyFinishes(IRunnableEventDevice<?> scanner) throws ScanningException, InterruptedException, TimeoutException, ExecutionException {
		scanner.latch();
	}

	private void verifyInterrupted(IRunnableEventDevice<?> scanner) {
		assertThrows(InterruptedException.class, scanner::latch);
	}

	private CountDownLatch latchUntilStateChanges(
			IRunnableEventDevice<?> scanner,
			int numChanges) throws ScanningException {
		CountDownLatch latch = new CountDownLatch(numChanges);
		scanner.addRunListener(new IRunListener() {
			@Override
			public void stateChanged(RunEvent evt) throws ScanningException {
				latch.countDown();
				if (latch.getCount() <= 0)
					scanner.removeRunListener(this);
			}
		});
		return latch;
	}
}
