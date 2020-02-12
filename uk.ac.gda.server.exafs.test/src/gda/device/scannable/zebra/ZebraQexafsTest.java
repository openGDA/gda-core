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

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.jscience.physics.amount.Amount;
import org.junit.Before;
import org.junit.Test;

import gda.TestHelpers;
import gda.device.ContinuousParameters;
import gda.device.motor.DummyMotor;
import gda.device.zebra.controller.impl.ZebraDummy;

public class ZebraQexafsTest {
	private ZebraQexafsScannableForTest scn;

	/** Crystal 2d spacing [metres] */
	private double twoD = 0.62711e-9;

	/** Motor acceleration [deg/s/s] - from DCM acceleration readback value (BL18B-OP-DCM-01:BRAGG:MPACCEL:RBV) */
	private static final double ACCELERATION = 0.225;

	private static final double TOLERANCE = 1e-6;


	class ZebraQexafsScannableForTest extends ZebraQexafsScannable {

		public ZebraQexafsScannableForTest() {
			channelsConfigured = true;
			setConfigured(true);
			setName("ZebraQexafsScannableForTest");
			setOutputFormat(new String[] {"%.2f"});
		}

		@Override
		protected void resetDCMSpeed() {
		}
		@Override
		protected void setEnergySwitchOn() {
		}
		@Override
		protected void toggleEnergyControl() {
		}

		@Override
		protected Amount<Length> getTwoD() {
			return Amount.valueOf(twoD, SI.METER);
		}

		@Override
		protected double getAcceleration() {
			return ACCELERATION;
		}

		public double getStartAngle() {
			return startAngle.doubleValue(SI.RADIAN);
		}

		public double getEndAngle() {
			return endAngle.doubleValue(SI.RADIAN);
		}

		public double getStepSize() {
			return stepSize.doubleValue(SI.RADIAN);
		}

		public double getDesiredSpeed() {
			return desiredSpeed;
		}

		public double getRunupPosition() {
			return runupPosition.doubleValue(SI.RADIAN);
		}

		public double getRundownPosition() {
			return runDownPosition.doubleValue(SI.RADIAN);
		}

		@Override
		public boolean isBusy() {
			return false;
		}
	}

	@Before
	public void setup() {
		scn = new ZebraQexafsScannableForTest();
		scn.setZebraDevice(new ZebraDummy());
		scn.setMotor(new DummyMotor());
		scn.setDemandPositionTolerance(0.01);
	}

	@Test
	public void testMotionCalculation() throws Exception {
		TestHelpers.setUpTest(ZebraQexafsTest.class, "testParams", false);

		ContinuousParameters params = new ContinuousParameters();
		params.setStartPosition(10000);
		params.setEndPosition(12000);
		params.setNumberDataPoints(1000);
		params.setTotalTime(5);

		scn.setContinuousParameters(params);
		scn.setPosition(10500.0);
		scn.prepareForContinuousMove();

		checkMotionParameters(params);
		checkZebraParameters(params);
	}

	/**
	 * Test that zebra gat start, width and pulse step have been set correctly
	 * @param params
	 * @throws Exception
	 */
	private void checkZebraParameters(ContinuousParameters params) throws Exception {
		// Start, end, step size angles for zebra are in degrees
		double start = Math.toDegrees(getAngleFromEnergy(params.getStartPosition()));
		double end = Math.toDegrees(getAngleFromEnergy(params.getEndPosition()));
		double step = Math.abs(end - start)/params.getNumberDataPoints();

		assertEquals(start, scn.getZebraDevice().getPCGateStart(), TOLERANCE);
		assertEquals(Math.abs(end-start), scn.getZebraDevice().getPCGateWidth(), TOLERANCE);
		assertEquals(step, scn.getZebraDevice().getPCPulseStep(), TOLERANCE);
	}

	/**
	 * Test that the angle conversions, motor speed, position calculations done
	 * by {@link QexafsScannable#calculateMotionInDegrees()} are correct.
	 * @throws Exception
	 */
	private void checkMotionParameters(ContinuousParameters params) {

		double start = getAngleFromEnergy(params.getStartPosition());
		double end = getAngleFromEnergy(params.getEndPosition());

		assertEquals(start, scn.getStartAngle(), TOLERANCE);
		assertEquals(end, scn.getEndAngle(), TOLERANCE);
		assertEquals((start - end) / params.getNumberDataPoints(), scn.getStepSize(), TOLERANCE);

		// Scan angular speed (degrees per second)
		double speedDegrees = Math.toDegrees((end - start) / params.getTotalTime());
		assertEquals(Math.abs(speedDegrees), scn.getDesiredSpeed(), TOLERANCE);

		// Run up/down angle distance (degrees)
		double runUpDegress = scn.getRunUpScaleFactor() * (speedDegrees * speedDegrees) / (2 * ACCELERATION);

		// Runup angle distance (radians)
		double runupRadians = Math.toRadians(runUpDegress);

		// Start, end angles including runup, rundown (radians)
		double startPosWithRunup = start + runupRadians;
		double endPosWithRundown = end - runupRadians;

		assertEquals(Math.abs(startPosWithRunup), scn.getRunupPosition(), TOLERANCE);
		assertEquals(Math.abs(endPosWithRundown), scn.getRundownPosition(), TOLERANCE);
	}

	/**
	 * Convert from energy [eV] to bragg angle [radians]
	 * @param energyEv
	 * @return
	 */
	private double getAngleFromEnergy(double energyEv) {
		double wave = getWavelengthFromEnergy(energyEv);
		return getAngleFromWavelength(wave);
	}

	/**
	 * Convert from energy [eV] to wavelength [metres]
	 * @param energy [eV]
	 * @return
	 */
	private double getWavelengthFromEnergy(double energyEv) {
		DiffractionCrystalEnvironment de = new DiffractionCrystalEnvironment();
		de.setWavelengthFromEnergykeV(energyEv*0.001);
		return de.getWavelength()*1e-10;
	}

	/**
	 * Convert from wavelength [metres] to angle [radians] using bragg equation
	 * @param wavelength
	 * @return
	 */
	private double getAngleFromWavelength(double wavelengthM) {
		return Math.asin(wavelengthM/twoD);
	}
}
