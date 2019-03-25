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

package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.models.DeviceWatchdogModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.event.IMalcolmEventListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.example.scannable.WaitingScannable;
import org.eclipse.scanning.sequencer.watchdog.TopupWatchdog;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.messaging.FileUtils;
import org.eclipse.scanning.test.scan.nexus.DummyMalcolmDeviceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class WatchdogInOuterScanMoveTest extends AbstractWatchdogTest {

	/**
	 * A topup scannable that triggers topup when {@link #trigger()} is called,
	 * rather than running a counter, so that we can control it from our test.
	 */
	private static class TriggeredTestTopupScannable extends MockScannable {

		private final double initialValue;

		public TriggeredTestTopupScannable(String name, double initialValue) {
			super(name, initialValue);
			this.initialValue = initialValue;
		}

		public void trigger() throws ScanningException {
			firePosition(0);
		}

		public void resetTopup() throws ScanningException {
			firePosition(initialValue);
		}

		private void firePosition(double position) throws ScanningException {
			delegate.firePositionChanged(getLevel(), new Scalar<>(getName(), -1, position));
		}

	}

	private static class TestPositionListener implements IPositionListener {

		private IPosition lastPositionPerformed = null;
		private int numPositionsPerformed = 0;

		@Override
		public void positionPerformed(PositionEvent event) throws ScanningException {
			lastPositionPerformed = event.getPosition();
			numPositionsPerformed++;
		}

		public IPosition getLastPositionPerformed() {
			return lastPositionPerformed;
		}

		public int getNumPositionsPerformed() {
			return numPositionsPerformed;
		}

	}

	private static final int INNER_SCAN_SIZE = 25;

	private File dir;
	private TriggeredTestTopupScannable topupScannable;
	private WaitingScannable outerScannable;

	@Before
	public void startUp() throws Exception {
		this.dir = Files.createTempDirectory(DummyMalcolmDeviceTest.class.getSimpleName()).toFile();
		dir.deleteOnExit();

		// Create the topup scannable
		topupScannable = new TriggeredTestTopupScannable("topup", 3);
		connector.register(topupScannable);

		// Create the outer scannable, a special scananble that can be waited on
		outerScannable = new WaitingScannable("outer");
		connector.register(outerScannable);

		// Create the topup watchdog
		DeviceWatchdogModel model = new DeviceWatchdogModel();
		model.setCountdownName("topup");
		model.setCooloff(500); // all values in ms
		model.setWarmup(200);
		model.setTopupTime(150);
		model.setPeriod(5000);

		TopupWatchdog topupWatchdog = new TopupWatchdog(model);
		topupWatchdog.setName("topupWatchdog");
		topupWatchdog.activate();
	}

	private DummyMalcolmModel createDummyMalcolmModel() {
		DummyMalcolmModel model = DummyMalcolmDeviceTest.createModel();
		model.setExposureTime(0.005);
		model.setAxesToMove(Arrays.asList("x", "y"));
		return model;
	}

	@After
	public void tearDown() throws Exception {
		if (dir!=null) FileUtils.recursiveDelete(dir);
	}

	/**
	 * Simulates topup occurring during an outer scannable move
	 *
	 * @throws Exception
	 */
	@Test
	public void testTopupWithOuterScanTest() throws Exception {
		DummyMalcolmModel model = createDummyMalcolmModel();

		final IMalcolmDevice<DummyMalcolmModel> malcolmDevice =
				(IMalcolmDevice<DummyMalcolmModel>) ServiceTestHelper.getRunnableDeviceService().createRunnableDevice(model, false);
		final List<String> axisNames = Arrays.asList("x", "y", "outer");
		IDeviceController controller = createTestScanner(null, null, malcolmDevice, model, 3, axisNames, null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();

		final TestPositionListener scanPositionListener = new TestPositionListener();
		((IPositionListenable) scanner).addPositionListener(scanPositionListener);

		final IMalcolmEventListener malcolmEventListener = mock(IMalcolmEventListener.class);
		final ArgumentCaptor<MalcolmEvent> malcolmEventCaptor = ArgumentCaptor.forClass(MalcolmEvent.class);
		((IMalcolmDevice<?>) malcolmDevice).addMalcolmListener(malcolmEventListener);

		scanner.start(null);
		outerScannable.waitForSetPosition(); // initial move at start of scan
		assertTrue(controller.isActive());
		assertEquals(DeviceState.RUNNING, scanner.getDeviceState());
		assertEquals(0, scanPositionListener.getNumPositionsPerformed());
		assertEquals(null, scanPositionListener.getLastPositionPerformed());
		verifyZeroInteractions(malcolmEventListener);

		// allow the scan to resume and wait for outer scannable move to 2nd position
		outerScannable.resume();
		outerScannable.waitForSetPosition();
		assertTrue(controller.isActive());
		assertEquals(DeviceState.RUNNING, scanner.getDeviceState());
		assertEquals(1, scanPositionListener.getNumPositionsPerformed());
		assertEquals(new Scalar<>("outer", 0, 290.0), scanPositionListener.getLastPositionPerformed());
		verify(malcolmEventListener, times(INNER_SCAN_SIZE)).eventPerformed(malcolmEventCaptor.capture());
		List<MalcolmEvent> malcolmEvents = malcolmEventCaptor.getAllValues();
		assertEquals(INNER_SCAN_SIZE, malcolmEvents.size());
		for (int i = 0; i < INNER_SCAN_SIZE; i++) {
			assertEquals(MalcolmEvent.forStepsCompleted(malcolmDevice, i, "Completed step " + i), malcolmEvents.get(i));
		}

		topupScannable.trigger(); // trigger the topup watchdog while the outer scannable is waiting to be resumed
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState()); // check the scan state is paused

		outerScannable.resume(); // resume the outer scannable
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState()); // check the scan state is paused

		// check the scan is actually paused by checking no more positions have been perfomed after a short sleep
		Thread.sleep(500);
		assertEquals(1, scanPositionListener.getNumPositionsPerformed());
		assertEquals(new Scalar<>("outer", 0, 290.0), scanPositionListener.getLastPositionPerformed());
		assertEquals(INNER_SCAN_SIZE, malcolmEvents.size());
		for (int i = 0; i < INNER_SCAN_SIZE; i++) {
			assertEquals(MalcolmEvent.forStepsCompleted(malcolmDevice, i, "Completed step " + i), malcolmEvents.get(i));
		}

		topupScannable.resetTopup(); // reset the topup to resume the scan
		assertEquals(DeviceState.RUNNING, scanner.getDeviceState()); // check the device state is running

		scanner.latch(); // latch to the end of the scan and check it's finished
		assertTrue(!controller.isActive());
		assertEquals(DeviceState.ARMED, scanner.getDeviceState());
		assertEquals(3, scanPositionListener.getNumPositionsPerformed());
		final Scalar<Double> expectedOuterPos = new Scalar<>("outer", 2, 292.0);
		expectedOuterPos.setStepIndex(INNER_SCAN_SIZE * 3 - 1);
		assertEquals(expectedOuterPos, scanPositionListener.getLastPositionPerformed());

		malcolmEvents.clear();
		verify(malcolmEventListener, times(INNER_SCAN_SIZE * 3)).eventPerformed(malcolmEventCaptor.capture());
		malcolmEvents = malcolmEventCaptor.getAllValues();
		assertEquals(INNER_SCAN_SIZE * 3, malcolmEvents.size());
		assertEquals(MalcolmEvent.forStepsCompleted(malcolmDevice, INNER_SCAN_SIZE * 3 - 1, "Completed step " + (INNER_SCAN_SIZE * 3 - 1)),
				malcolmEvents.get(malcolmEvents.size() - 1));
	}

}
