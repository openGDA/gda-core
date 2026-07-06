/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.scan;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static uk.ac.gda.server.exafs.scan.SpectrometerTestFunctions.checkPositions;
import static uk.ac.gda.server.exafs.scan.SpectrometerTestFunctions.createBooleanPositioner;
import static uk.ac.gda.server.exafs.scan.SpectrometerTestFunctions.createCrystalGroup;
import static uk.ac.gda.server.exafs.scan.SpectrometerTestFunctions.createDetectorGroup;
import static uk.ac.gda.server.exafs.scan.SpectrometerTestFunctions.createScannableGroup;
import static uk.ac.gda.server.exafs.scan.SpectrometerTestFunctions.createScannableMotor;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gda.TestHelpers;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.scannable.JohannSpectrometer;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.XesSpectrometerCrystal;

public class JohannSpectrometerTest {

	private JohannSpectrometer johannSpectrometer;
	private XesSpectrometerCrystal minusCrystal;
	private XesSpectrometerCrystal centreCrystal;
	private XesSpectrometerCrystal plusCrystal;
	private EnumPositioner minusCrystalAllowedToMove;
	private EnumPositioner centreCrystalAllowedToMove;
	private EnumPositioner plusCrystalAllowedToMove;

	private double radius = 500.0;
	private double tolerance = 1e-5;
	private double midBraggAngle;

	@Before
	public void setupJohannSpectrometer() throws Exception {
		TestHelpers.setUpTest(JohannSpectrometerTest.class, "", false);

		System.setProperty("gda.motordir", "test-scratch/motors");

		// Set up the 3 XesSpectrometerCrystal objects and ScannableGroup to contain them
		minusCrystal = createCrystalGroup("minus", -1);
		centreCrystal = createCrystalGroup("centre", 1); // need to have index !=0 here to have motor for X movement created
		centreCrystal.setHorizontalIndex(0); // now set the correct index
		plusCrystal = createCrystalGroup("plus", 1);

		// Setup the 'allowed to move' enum positioners and ScannableGroup to contain them
		minusCrystalAllowedToMove = createBooleanPositioner("minusAllowedToMove");
		centreCrystalAllowedToMove = createBooleanPositioner("centreAllowedToMove");
		plusCrystalAllowedToMove = createBooleanPositioner("plusAllowedToMove");

		johannSpectrometer = new JohannSpectrometer();
		johannSpectrometer.setName("xesSpectrometer");
		johannSpectrometer.setRadiusScannable(createScannableMotor("radius"));
		johannSpectrometer.setDetectorGroup(createDetectorGroup());
		johannSpectrometer.setCrystalsGroup(createScannableGroup("crystalGroup", minusCrystal, centreCrystal, plusCrystal));
		johannSpectrometer.setCrystalsAllowedToMove(createScannableGroup("allowedToMove", minusCrystalAllowedToMove, centreCrystalAllowedToMove, plusCrystalAllowedToMove));
		johannSpectrometer.setIncludeYawCorrection(false);
		johannSpectrometer.configure();

		minusCrystal.moveTo(new double[] {0,0,0,0});
		centreCrystal.moveTo(new double[] {0,0,0,0});
		plusCrystal.moveTo(new double[] {0,0,0,0});

		johannSpectrometer.getRadiusScannable().moveTo(radius);
		midBraggAngle = (johannSpectrometer.getMinTheta() + johannSpectrometer.getMaxTheta()) * 0.5;
	}

	@Test
	public void testPositionsAreCorrectNoYaw() throws DeviceException {
		johannSpectrometer.setIncludeYawCorrection(false);

		double braggAngle = midBraggAngle;
		johannSpectrometer.moveTo(braggAngle);
		double[] analyserPosition = getAnalyserPosition(braggAngle);

		checkPositions(johannSpectrometer.getDetectorGroup(), getDetPosition(braggAngle), tolerance);
		// All three analysers should have the same position
		checkPositions(minusCrystal, analyserPosition, tolerance);
		checkPositions(centreCrystal, analyserPosition, tolerance);
		checkPositions(plusCrystal, analyserPosition, tolerance);
	}

	@Test
	public void testPositionsAreCorrectYaw() throws DeviceException {
		johannSpectrometer.setIncludeYawCorrection(true);

		double braggAngle = midBraggAngle;
		johannSpectrometer.moveTo(braggAngle);
		double[] expectedPositions = getAnalyserPosition(braggAngle);
		double yawAngle = getYawCorrection(braggAngle);

		checkPositions(johannSpectrometer.getDetectorGroup(), getDetPosition(braggAngle), tolerance);
		// All three analysers should have the same position
		for(var cryst : List.of(minusCrystal, centreCrystal, plusCrystal)) {
			double[] positions = ScannableUtils.getCurrentPositionArray(cryst);
			expectedPositions[2] = yawAngle*cryst.getHorizontalIndex();
			assertArrayEquals("Positions for "+cryst.getName()+" are incorrect", expectedPositions, positions, 1e-3);
		}
	}

	@Test
	public void testReadbackBraggAngle() throws DeviceException {
		johannSpectrometer.setIncludeYawCorrection(true);

		double braggAngle = midBraggAngle;
		johannSpectrometer.moveTo(braggAngle);
		double readbackBragg = (double) johannSpectrometer.getPosition();
		assertEquals("Readback Bragg angle value (with yaw correction) is not correct", braggAngle, readbackBragg, 1e-5);

		// move out of position
		johannSpectrometer.moveTo(braggAngle+0.5);

		// do the same, but without yaw correction (Bragg angle should be the same)
		johannSpectrometer.setIncludeYawCorrection(false);
		johannSpectrometer.moveTo(braggAngle);
		readbackBragg = (double) johannSpectrometer.getPosition();
		assertEquals("Readback Bragg angle value (with yaw correction) is not correct", braggAngle, readbackBragg, 1e-5);
	}

	@Test(expected = DeviceException.class)
	public void testReadbackBraggAngleOutOfRange() throws DeviceException {
		var detRot = johannSpectrometer.getDetectorGroup().getGroupMembersAsArray()[2];
		detRot.moveTo(0);
		johannSpectrometer.getPosition();
	}

	@Test(expected = DeviceException.class)
	public void testBraggAngleTooBig() throws DeviceException {
		double maxAngle = johannSpectrometer.getMaxTheta();
		johannSpectrometer.moveTo(maxAngle+1);
	}

	@Test(expected = DeviceException.class)
	public void testBraggAngleTooSmall() throws DeviceException {
		double minAngle = johannSpectrometer.getMinTheta();
		johannSpectrometer.moveTo(minAngle-1);
	}

	@Test
	public void testMinusCrystalAllowedToMove() throws DeviceException {
		testAnalyser(minusCrystal, minusCrystalAllowedToMove);
	}

	@Test
	public void testCentreCrystalAllowedToMove() throws DeviceException {
		testAnalyser(centreCrystal, centreCrystalAllowedToMove);
	}

	@Test
	public void testPlusCrystalAllowedToMove() throws DeviceException {
		testAnalyser(plusCrystal, plusCrystalAllowedToMove);
	}

	/**
	 * Disable a single analyser crystal and check that the position before and
	 * after a Bragg angle change is the same.
	 *
	 * @param testCrystalGroup
	 * @param crystalGroupAllowedToMove
	 * @throws NumberFormatException
	 * @throws DeviceException
	 */
	protected void testAnalyser(XesSpectrometerCrystal testCrystalGroup, EnumPositioner crystalGroupAllowedToMove) throws NumberFormatException, DeviceException {
		double offsetAngle = midBraggAngle + 0.5;

		johannSpectrometer.moveTo(offsetAngle);
		crystalGroupAllowedToMove.moveTo(Boolean.FALSE.toString());
		johannSpectrometer.moveTo(midBraggAngle);

		var midBraggPosition = getAnalyserPosition(midBraggAngle);
		var offsetBraggPosition = getAnalyserPosition(offsetAngle);

		List<XesSpectrometerCrystal> crystalGroups = List.of(minusCrystal, centreCrystal, plusCrystal);
		for(var crystal : crystalGroups) {
			double[] position = crystal == testCrystalGroup ? offsetBraggPosition : midBraggPosition;
			checkPositions(crystal, position, tolerance);
		}
	}

	/**
	 *
	 * @param bragg
	 * @return Detector x, y position and surface orientation wrt to x-y plane
	 */
	private double[] getDetPosition(double bragg) {
		double[] analyserPos = getAnalyserPosition(bragg);
		return new double[] {0, 2*analyserPos[1], -(90-bragg)};
	}

	/**
	 * @param bragg
	 * @return analyser x, y position and crystal pitch angle.
	 */
	private double[] getAnalyserPosition(double bragg) {
		double braggRad = Math.toRadians(bragg);
		return new double[] { radius * Math.sin(braggRad) * Math.sin(braggRad), radius * Math.sin(2 * braggRad) / 2.0, 0, 0 };
	}

	private double getYawCorrection(double angle) {
		double[] analyserPosition = getAnalyserPosition(angle);
		double railAngle = Math.toRadians(johannSpectrometer.getAnalyserSeparationAngle());
		double sinAngle = johannSpectrometer.getSourceDisplacement()*Math.sin(railAngle)/analyserPosition[0];
		return Math.toDegrees(Math.asin(sinAngle));
	}
}
