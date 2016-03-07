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

package gda.device.zebra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import gda.device.DeviceException;
import gda.device.continuouscontroller.DummyHardwareTriggerProvider;
import gda.device.continuouscontroller.DummyTrajectoryMoveController;
import gda.device.detector.hardwaretriggerable.DummyHardwareTriggeredDetector;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;
import gda.device.scannable.DummyScannableMotor;
import gda.device.zebra.controller.Zebra;
import gda.device.zebra.controller.impl.ZebraDummy;
import gda.factory.FactoryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

public class ZebraConstantVelocityMoveControllerTest {

	private ZebraConstantVelocityMoveController controller;
	private DummyScannableMotor scannableMotor;
	private DummyZebraMotorInfoProvider infoProvider;
	private DummyHardwareTriggeredDetector detector;
	private ZebraDummy zebra;

	@Before
	public void setUp() throws DeviceException {
		scannableMotor = new DummyScannableMotor();

		infoProvider = new DummyZebraMotorInfoProvider();
		infoProvider.setPcEnc(Zebra.PC_ENC_ENC2);
		infoProvider.setScannableMotor(scannableMotor);

		detector = new DummyHardwareTriggeredDetector();
		detector.setHardwareTriggerProvider(new DummyHardwareTriggerProvider());
		detector.setCollectionTime(30.0);

		zebra = new ZebraDummy();

		controller = new ZebraConstantVelocityMoveController();
		controller.setZebraMotorInfoProvider(infoProvider);
		controller.setZebra(zebra);
		controller.setDetectors(Arrays.asList((HardwareTriggeredDetector) detector));
		controller.setTriggerPeriod(2.0);
		controller.setStart(0);
		controller.setEnd(7);
		controller.setStep(1.5);
	}

	@Test
	public void testInitialState() throws DeviceException {
		final String[] extraNames = controller.getExtraNames();
		assertEquals(1, extraNames.length);
		assertEquals("CaptureTime", extraNames[0]);

		assertEquals(0, controller.getInputNames().length);

		final String[] outputFormat = controller.getOutputFormat();
		assertEquals(1, outputFormat.length);
		assertEquals("%5.5g", outputFormat[0]);

		assertNotNull(controller.getZebra());
		assertNotNull(controller.getScannableMotor());
		assertNotNull(controller.getZebraMotorInfoProvider());

		assertEquals(0, controller.getPointBeingPrepared());
		assertEquals(1, controller.getMode());
		assertFalse(controller.isPcPulseGateNotTrigger());
		assertFalse(controller.isMoving());
		assertEquals(2.0, controller.getTriggerPeriod(), 0.001);
		assertEquals(5.0, controller.getNumberTriggers(), 0.001);
		assertEquals(8.0, controller.getTotalTime(), 0.001);

		assertEquals(0, controller.getStart(), 0.001);
		assertEquals(7.0, controller.getEnd(), 0.001);
		assertEquals(1.5, controller.getStep(), 0.001);

		assertNull(controller.getTimeSeriesCollection());

		assertNull(controller.getLastPointAdded());
		assertFalse(controller.isBusy());
		assertFalse(controller.isOperatingContinously());

		assertEquals(controller, controller.getContinuousMoveController());
		assertNotNull(controller.getDetectors());

		assertEquals(0.5, controller.getMinimumAccelerationDistance(), 0.001);
		assertEquals(0, controller.getRequiredSpeed(), 0.001);
		assertEquals(0, controller.getScannableMotorEndPosition(), 0.001);
	}

	@Test
	public void testConfigure() throws FactoryException {
		controller.configure();
	}

	@Test
	public void testRawGetPosition() throws DeviceException {
		assertEquals(0, (Double) controller.rawGetPosition(), 0.001);
	}

	@Test
	public void testAtScanLineEnd() throws DeviceException {
		controller.atScanLineEnd();
		assertNull(controller.getTimeSeriesCollection());
	}

	@Test
	public void testAtCommandFailure() throws DeviceException {
		controller.atCommandFailure();
		assertEquals(0, controller.getPointBeingPrepared());
	}

	@Test
	public void testAtScanLineStart() throws DeviceException {
		controller.setTimeSeriesCollection(new ArrayList<ZebraCaptureInputStreamCollection>());
		assertNotNull(controller.getTimeSeriesCollection());

		controller.atScanLineStart();
		assertNull(controller.getTimeSeriesCollection());
		assertFalse(controller.isMoving());
	}

	@Test
	public void testStop() throws DeviceException {
		controller.setTimeSeriesCollection(new ArrayList<ZebraCaptureInputStreamCollection>());

		controller.stop();
		assertNull(controller.getTimeSeriesCollection());
		assertEquals(0, controller.getPointBeingPrepared());
	}

	@Test
	public void testPrepareControllerToBeUsedForUpcomingScan() throws DeviceException {
		controller.setTimeSeriesCollection(new ArrayList<ZebraCaptureInputStreamCollection>());
		assertNotNull(controller.getTimeSeriesCollection());

		controller.prepareControllerToBeUsedForUpcomingScan();
		assertNull(controller.getTimeSeriesCollection());
		assertFalse(controller.isMoving());
	}

	@Test
	public void testGetPcCaptureBitField() {
		assertEquals(1, controller.getPcCaptureBitField(0));
		assertEquals(2, controller.getPcCaptureBitField(1));
	}

	@Test
	public void testResetPointBeingPrepared() {
		try {
			// Call to startMove() will fail but will increment pointBeingPrepared
			controller.startMove();
		} catch (DeviceException e) {
		}
		assertEquals(1, controller.getPointBeingPrepared());
		controller.resetPointBeingPrepared();
		assertEquals(0, controller.getPointBeingPrepared());
	}

	@Test
	public void testPrepareForMove() throws Exception {
		controller.prepareForMove();

		assertEquals(Zebra.PC_ARM_SOURCE_SOFT, zebra.getPCArmSource());
		assertEquals(Zebra.PC_PULSE_SOURCE_TIME, zebra.getPCPulseSource());
		assertEquals(2, zebra.getPCCaptureBitField());
		assertEquals(Zebra.PC_ENC_ENC2, zebra.getPCEnc());
		assertEquals(Zebra.PC_DIR_POSITIVE, zebra.getPCDir());
		assertEquals(1, zebra.getPCGateNumberOfGates());
		assertEquals(Zebra.PC_TIMEUNIT_MS, zebra.getPCTimeUnit());
		assertEquals(2000.0,  zebra.getPCPulseStep(), 0.0001);
		assertEquals(15000.0, zebra.getPCPulseDelay(), 0.0001);
		assertEquals(0.0001, zebra.getPCPulseWidth(), 0.0001);
		assertEquals(17.75, zebra.getPCGateWidth(), 0.0001);
		assertEquals(5, zebra.getPCPulseMax());
		assertEquals(Zebra.PC_GATE_SOURCE_POSITION, zebra.getPCGateSource());
		assertEquals(-11.25, zebra.getPCGateStart(), 0.0001);
		assertEquals(17.75, zebra.getPCGateWidth(), 0.0001);
		assertTrue(zebra.isPCArmed());

		assertEquals(-11.75, scannableMotor.getPosition());

		assertEquals(1, controller.getMode());
		assertEquals(0.75, controller.getRequiredSpeed(), 0.0001);
		assertEquals(6.5, controller.getScannableMotorEndPosition(), 0.0001);
	}

	@Test
	public void testSetModeValidVaue() {
		controller.setMode(Zebra.PC_PULSE_SOURCE_TIME);
		assertEquals(Zebra.PC_PULSE_SOURCE_TIME, controller.getMode());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetModeInvalidVaue() {
		controller.setMode(Zebra.PC_PULSE_SOURCE_POSITION);
	}

	@Test
	public void testStartMove() throws DeviceException, InterruptedException {
		controller.prepareForMove();
		controller.startMove();
		assertTrue(controller.isMoving());
	}

	@Test
	public void testWaitWhileMoving() throws DeviceException, InterruptedException {
		controller.prepareForMove();
		controller.startMove();
		controller.waitWhileMoving();
		assertFalse(controller.isMoving());
	}

	@Test
	public void testStopAndReset() throws DeviceException, InterruptedException {
		controller.prepareForMove();
		controller.startMove();
		controller.stopAndReset();
		assertNull(controller.getLastPointAdded());
	}

	@Test
	public void testGetNumberTriggers() {
		assertEquals(5, controller.getNumberTriggers());
	}

	@Test
	public void testGetTotalTime() throws DeviceException, InterruptedException {
		controller.prepareForMove();
		controller.startMove();
		controller.waitWhileMoving();
		assertEquals(8.0, controller.getTotalTime(), 0.0001);
	}

	@Test
	public void testGetLastPointAdded() {
		controller.addPoint(2.5);
		assertEquals(2.5, controller.getLastPointAdded(), 0.0001);

		controller.addPoint(4.2);
		assertEquals(4.2, controller.getLastPointAdded(), 0.0001);
	}

	@Test
	public void testAfterPropertiesSetValid() throws Exception {
		controller.afterPropertiesSet();
	}

	@Test(expected = Exception.class)
	public void testAfterPropertiesSetInvalid() throws Exception {
		final ZebraConstantVelocityMoveController controller2 = new ZebraConstantVelocityMoveController();
		controller2.afterPropertiesSet();
	}

	@Test
	public void testGetPositionSteamIndexer() throws DeviceException, InterruptedException {
		controller.prepareForMove();
		controller.startMove();
		controller.waitWhileMoving();

		assertNotNull(controller.getPositionSteamIndexer(0));
	}

	@Test
	public void testGetPositionCallable() throws DeviceException, InterruptedException {
		controller.prepareForMove();
		controller.startMove();
		controller.waitWhileMoving();

		final Callable<Double> positionCallable = controller.getPositionCallable();
		assertNotNull(positionCallable);
	}

	@Test
	public void testGetContinuousMoveController() {
		assertEquals(controller, controller.getContinuousMoveController());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetContinuousMoveController() {
		controller.setContinuousMoveController(new DummyTrajectoryMoveController());
	}

	@Test
	public void testCreateScannable() {
		// TODO: Implement when there is more testing infrastructure: see DASCTEST-343
	}

}
