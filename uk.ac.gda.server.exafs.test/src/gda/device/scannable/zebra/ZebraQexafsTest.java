/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.device.scannable.zebra;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;

import gda.TestHelpers;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.motor.DummyMotor;
import gda.device.zebra.controller.impl.ZebraDummy;
import gda.epics.CachedLazyPVFactory;
import gda.epics.DummyPV;
import gda.epics.connection.EpicsController;
import gov.aps.jca.Channel;

public class ZebraQexafsTest {
	private ZebraQexafsScannableForTest scn;

	/** Crystal d spacing [Angstrom] */
	private double d = 6.2711 / 2;

	/** Motor acceleration [deg/s/s] - from DCM acceleration readback value (BL18B-OP-DCM-01:BRAGG:MPACCEL:RBV) */
	private static final double ACCELERATION = 0.225;

	private static final double TOLERANCE = 1e-6;

	/**
	 * Energy equivalent (1eV)/(h c) [m^-1] from NIST CODATA 2018
	 */
	private static final double CODATA_FACTOR = 8.065543937e5;

	class ZebraQexafsScannableForTest extends ZebraQexafsScannable {

		private double demandPosition;

		public ZebraQexafsScannableForTest() {
			channelsConfigured = true;
			setConfigured(true);
			setName("ZebraQexafsScannableForTest");
			setOutputFormat(new String[] {"%.2f"});

			// Setup the Mocks for channel access
			controller = Mockito.mock(EpicsController.class);
			currentSpeedChnl = Mockito.mock(Channel.class);
			setPvFactory(Mockito.mock(CachedLazyPVFactory.class));
			Mockito.when(getPvFactory().getPVDouble(Matchers.any())).thenReturn(new DummyPV<Double>("dummy", 0.0));
		}

		public EpicsController getController() {
			return controller;
		}

		public Channel getSpeedChannel() {
			return currentSpeedChnl;
		}

		@Override
		public double getMaxSpeed() {
			return 100000;
		}

		/**
		 * The initial position of motor at start of scan in
		 * @return motor position (eV)
		 */
		@Override
		public Object getDemandPosition() {
			return demandPosition;
		}

		@Override
		public void asynchronousMoveTo(Object demandPosition) throws DeviceException {
			this.demandPosition = (double) demandPosition;
		}

		@Override
		protected void setEnergySwitchOn() {
		}

		@Override
		protected void toggleEnergyControl() {
		}

		@Override
		protected double getD() {
			return d;
		}

		@Override
		protected double getAcceleration() {
			return ACCELERATION;
		}

		public double getStartAngle() {
			return startAngle;
		}

		public double getEndAngle() {
			return endAngle;
		}

		public double getStepSize() {
			return stepSize;
		}

		public double getDesiredSpeed() {
			return desiredSpeed;
		}

		public double getRunupPosition() {
			return runupPosition;
		}

		public double getRundownPosition() {
			return runDownPosition;
		}

		@Override
		public boolean isBusy() {
			return false;
		}
	}

	@Before
	public void setup() throws Exception {
		scn = new ZebraQexafsScannableForTest();
		scn.setZebraDevice(new ZebraDummy());
		scn.setMotor(new DummyMotor());
		scn.setDemandPositionTolerance(0.01);
		scn.setPosition(10500.0);

		ContinuousParameters params = new ContinuousParameters();
		params.setStartPosition(10000);
		params.setEndPosition(12000);
		params.setNumberDataPoints(1000);
		params.setTotalTime(5);
		scn.setContinuousParameters(params);
	}


	@Test
	public void checkMotionParameters() throws Exception {
		TestHelpers.setUpTest(ZebraQexafsTest.class, "checkMotion", false);

		scn.prepareForContinuousMove();
		ContinuousParameters params = scn.getContinuousParameters();

		// Check position calculation has been done correctly and applied to Zebra
		checkMotionParameters(params);
		checkZebraParameters(params);

		// Check speed has been set to max, and DCM is has been moved to start position
		InOrder inOrder = Mockito.inOrder(scn.getController());
		inOrder.verify(scn.getController()).caput(scn.getSpeedChannel(), scn.getMaxSpeed());
		assertEquals("DCM run up energy is not correct", getEnergyFromAngle(scn.getRunupPosition()), (double)scn.getDemandPosition(), TOLERANCE);

		// Check scan speed has been set, zebra is armed, and DCM is being moved to correct rundown position
		scn.performContinuousMove();
		inOrder.verify(scn.getController()).caput(scn.getSpeedChannel(), scn.getDesiredSpeed());
		assertEquals(true, scn.getZebraDevice().isPCArmed());
		assertEquals("DCM run down energy is not correct", getEnergyFromAngle(scn.getRundownPosition()), (double)scn.getDemandPosition(), TOLERANCE);

		// Check DCM speed has been reset at end of scan and the zebra has been disarmed
		scn.continuousMoveComplete();
		inOrder.verify(scn.getController()).caput(scn.getSpeedChannel(), scn.getMaxSpeed());
		assertEquals(false, scn.getZebraDevice().isPCArmed());
	}

	/**
	 * Test that zebra gate start, width and pulse step have been set correctly
	 * @param params
	 * @throws Exception
	 */
	private void checkZebraParameters(ContinuousParameters params) throws Exception {
		// Start, end, step size angles for zebra are in degrees
		double start = getAngleFromEnergy(params.getStartPosition());
		double end = getAngleFromEnergy(params.getEndPosition());
		double step = Math.abs(end - start)/params.getNumberDataPoints();

		assertEquals(start, scn.getZebraDevice().getPCGateStart(), TOLERANCE);
		assertEquals(Math.abs(end-start), scn.getZebraDevice().getPCGateWidth(), TOLERANCE);
		assertEquals(step, scn.getZebraDevice().getPCPulseStep(), TOLERANCE);
	}

	/**
	 * Test that the angle conversions, motor speed, position calculations done
	 * by {@link QexafsScannable#calculateMotionInDegrees()} are correct.
	 * @throws DeviceException
	 * @throws Exception
	 */
	private void checkMotionParameters(ContinuousParameters params) throws DeviceException {

		double start = getAngleFromEnergy(params.getStartPosition());
		double end = getAngleFromEnergy(params.getEndPosition());

		assertEquals(start, scn.getStartAngle(), TOLERANCE);
		assertEquals(end, scn.getEndAngle(), TOLERANCE);
		assertEquals((start - end) / params.getNumberDataPoints(), scn.getStepSize(), TOLERANCE);

		// Scan angular speed (degrees per second)
		double speedDegrees = (start - end) / params.getTotalTime();
		assertEquals(Math.abs(speedDegrees), scn.getDesiredSpeed(), TOLERANCE);

		// Run up/down angle distance (degrees)
		double runUpDegrees = scn.getRunUpScaleFactor() * (speedDegrees * speedDegrees) / (2 * ACCELERATION);

		// Start, end angles including runup, rundown (degrees)
		double startPosWithRunup = start + runUpDegrees;
		double endPosWithRundown = end - runUpDegrees;

		assertEquals(Math.abs(startPosWithRunup), scn.getRunupPosition(), TOLERANCE);
		assertEquals(Math.abs(endPosWithRundown), scn.getRundownPosition(), TOLERANCE);
	}

	/**
	 * Convert from bragg angle [degrees] to energy [eV]
	 * @param angle
	 * @return
	 */
	private double getEnergyFromAngle(double angle) {
		double wavelengthAngstroms = 2 * d * Math.sin(Math.toRadians(angle));
		return 1.0 / (CODATA_FACTOR * wavelengthAngstroms * 1e-10);
	}

	/**
	 * Convert from energy [eV] to bragg angle [degrees]
	 * @param energyEv
	 * @return
	 */
	private double getAngleFromEnergy(double energyEv) {
		double wavelengthAngstroms = 1e10 / (CODATA_FACTOR * energyEv);
		return Math.toDegrees(Math.asin(wavelengthAngstroms / (2 * d)));
	}

}
