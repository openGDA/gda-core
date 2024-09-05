/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static uk.ac.gda.server.exafs.scan.SpectrometerTestFunctions.createBooleanPositioner;
import static uk.ac.gda.server.exafs.scan.SpectrometerTestFunctions.createCrystalGroup;
import static uk.ac.gda.server.exafs.scan.SpectrometerTestFunctions.createDetectorGroup;
import static uk.ac.gda.server.exafs.scan.SpectrometerTestFunctions.createMockScannableMotor;
import static uk.ac.gda.server.exafs.scan.SpectrometerTestFunctions.createScannableGroup;
import static uk.ac.gda.server.exafs.scan.SpectrometerTestFunctions.createScannableMotor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;

import gda.TestHelpers;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.XesSpectrometerCrystal;
import gda.device.scannable.XesSpectrometerScannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.exafs.xes.XesUtils;
import gda.factory.FactoryException;

public class XesSpectrometerScannableTest {

	private XesSpectrometerScannable xesSpectrometer;
	private double radius = 1000;
	private static final double tolerance = 1e-4;
	private static final String scannablePositionsFilePath = "testfiles/gda/device/scannable/positions_%.1f.txt";

	private XesSpectrometerCrystal minusCrystal;
	private XesSpectrometerCrystal centreCrystal;
	private XesSpectrometerCrystal plusCrystal;
	private EnumPositioner minusCrystalAllowedToMove;
	private EnumPositioner centreCrystalAllowedToMove;
	private EnumPositioner plusCrystalAllowedToMove;


	@Before
	public void setup() throws Exception {
		String testDir = TestHelpers.setUpTest(XesSpectrometerScannable.class, "", false);

		// Save motor persistence files in test-scratch so git doesn't see them
		System.setProperty("gda.motordir", testDir+"/motors");

		// Set up the 3 XesSpectrometerCrystal objects and ScannableGroup to contain them
		minusCrystal = createCrystalGroup("minus", -1);
		centreCrystal = createCrystalGroup("centre", 0);
		plusCrystal = createCrystalGroup("plus", 1);

		ScannableGroup crystalGroup = createScannableGroup("crystalGroup", minusCrystal, centreCrystal, plusCrystal);

		// Setup the 'allowed to move' enum positioners and ScannableGroup to contain them
		minusCrystalAllowedToMove = createBooleanPositioner("minusAllowedToMove");
		centreCrystalAllowedToMove = createBooleanPositioner("centreAllowedToMove");
		plusCrystalAllowedToMove = createBooleanPositioner("plusAllowedToMove");

		ScannableGroup allowedToMoveGroup = createScannableGroup("allowedToMove", minusCrystalAllowedToMove, centreCrystalAllowedToMove, plusCrystalAllowedToMove);

		xesSpectrometer = new XesSpectrometerScannable();
		xesSpectrometer.setName("xesSpectrometer");
		xesSpectrometer.setRadiusScannable(createScannableMotor("radius"));
		xesSpectrometer.setDetectorGroup(createDetectorGroup());
		xesSpectrometer.setSpectrometerX(createScannableMotor("xtalX"));
		xesSpectrometer.setCrystalsGroup(crystalGroup);
		xesSpectrometer.setCrystalsAllowedToMove(allowedToMoveGroup);
		xesSpectrometer.setUpperRow(true);
		xesSpectrometer.setAnalyserDemandPrecision(new double [] {0, 0, 0, 0});
		xesSpectrometer.setAbsoluteXPos(true);
		xesSpectrometer.configure();

		// Set to large value so that detector won't be moved along trajectory
		xesSpectrometer.setTrajectoryStepSize(100.0);

		// set detector and crystal position  give reasonable starting bragg angle
//		xesSpectrometer.getDet_y().moveTo(475);
		xesSpectrometer.getSpectrometerX().moveTo(radius);
		xesSpectrometer.getRadiusScannable().moveTo(radius);
		centreCrystal.getyMotor().moveTo(0.0);
		centreCrystal.getRotMotor().moveTo(0.0);
		centreCrystal.getPitchMotor().moveTo(0.0);

		// Do initial move to get all crystal, detector positions consistent
		xesSpectrometer.moveTo(75);
	}

	@AfterClass
	public static void afterClass() {
		System.clearProperty("gda.motordir");
	}

	@Test
	public void testPositionsAreCorrect() throws DeviceException {
		double braggAngle = (xesSpectrometer.getMinTheta() + xesSpectrometer.getMaxTheta()) * 0.5;

		xesSpectrometer.moveTo(braggAngle);

		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(braggAngle));
		checkPositions(minusCrystal, getMinusPosition(braggAngle));
		checkPositions(centreCrystal, getCentrePosition(braggAngle));
		checkPositions(plusCrystal, getPlusPosition(braggAngle));
	}

	@Test
	public void testRoundedPositionsAreCorrect() throws DeviceException {
		double braggAngle = (xesSpectrometer.getMinTheta() + xesSpectrometer.getMaxTheta()) * 0.5;

		// Set the rounding parameters for the analyser and detector stages
		double[] motorDemandPrecisions = {0.01, 0.02, 0.01, 0.001};
		xesSpectrometer.setAnalyserDemandPrecision(motorDemandPrecisions);

		double[] detectorDemandPrecision = {0.01, 0.05, 0.025};
		xesSpectrometer.setDetectorDemandPrecision(detectorDemandPrecision);

		xesSpectrometer.moveTo(braggAngle);

		checkRoundedPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(braggAngle), detectorDemandPrecision);
		checkRoundedPositions(minusCrystal, getMinusPosition(braggAngle), motorDemandPrecisions);
		checkRoundedPositions(centreCrystal, getCentrePosition(braggAngle), motorDemandPrecisions);
		checkRoundedPositions(plusCrystal, getPlusPosition(braggAngle), motorDemandPrecisions);
	}

	@Test
	public void testAgainstOldPositions() throws IOException {
		double[] angles = {65, 70, 75, 80, 85};
		for(double braggAngle : angles) {
			Map<String, Double> expectedPositions = getScannablePositionsFromFile(braggAngle);
			xesSpectrometer.setAbsoluteXPos(false);
			for(var ent : xesSpectrometer.getSpectrometerPositions(braggAngle).entrySet()) {
				Double expected = expectedPositions.get(ent.getKey().getName());
				assertEquals("Position for bragg angle = "+braggAngle+", "+ent.getKey()+" does not match expected value", expected, ent.getValue(), 1e-3);
				System.out.println("angle = "+braggAngle+" , "+ent.getKey()+" is ok : "+ent.getValue());
			}
		}
	}

	private Gson gson = new Gson();

	private Map<String,Double> getScannablePositionsFromFile(double braggAngle) throws IOException {
		String jsonString = Files.readString(Paths.get(String.format(scannablePositionsFilePath, braggAngle)));
		return gson.fromJson(jsonString, Map.class);
	}

	@Ignore("This test can be run if required to generate a new set of position files")
	@Test
	public void generatePositionsFiles() throws IOException {
		String filePathPattern = scannablePositionsFilePath.replace("positions", "new_positions");
		double[] angles = {65, 70, 75, 80, 85};
		for(double angle : angles) {
			Map<Scannable, Double> scnMap = xesSpectrometer.getSpectrometerPositions(angle);

			Map<String,Double> stringMap = scnMap.entrySet()
				.stream()
				.collect(Collectors.toMap(e->e.getKey().getName(), e->e.getValue()));

			String jsonString = gson.toJson(stringMap).replace(",", ",\n");
			Files.writeString(Paths.get(String.format(filePathPattern, angle)), jsonString);
		}
	}

	@Test
	public void testNoMinusCrystalMove() throws DeviceException {
		// Store the current positions of the 'minus' crystal
		Double[] origMinusPositions = ScannableUtils.objectToArray(minusCrystal.getPosition());

		double newBraggAngle = 75;
		minusCrystalAllowedToMove.moveTo(Boolean.FALSE.toString());
		xesSpectrometer.moveTo(newBraggAngle);

		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(newBraggAngle));
		// 'minus' crystal should be at original location
		checkPositions(minusCrystal, origMinusPositions);
		checkPositions(centreCrystal, getCentrePosition(newBraggAngle));
		checkPositions(plusCrystal, getPlusPosition(newBraggAngle));
	}

	@Test
	public void testNoCentreCrystalMoves() throws DeviceException {
		// Store the current positions of the 'centre' crystal
		Double[] origCentrePositions = ScannableUtils.objectToArray(centreCrystal.getPosition());

		double newBraggAngle = 78;
		centreCrystalAllowedToMove.moveTo(Boolean.FALSE.toString());
		xesSpectrometer.moveTo(newBraggAngle);

		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(newBraggAngle));
		checkPositions(minusCrystal, getMinusPosition(newBraggAngle));
		// centre crystal should be at original location
		checkPositions(centreCrystal, origCentrePositions);
		checkPositions(plusCrystal, getPlusPosition(newBraggAngle));
	}

	@Test
	public void testNoPlusCrystalMove() throws DeviceException {
		// Store the current positions of the 'plus' crystal
		Double[] origPlusPositions = ScannableUtils.objectToArray(plusCrystal.getPosition());

		double newBraggAngle = 62;
		plusCrystalAllowedToMove.moveTo(Boolean.FALSE.toString());
		xesSpectrometer.moveTo(newBraggAngle);


		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(newBraggAngle));
		checkPositions(minusCrystal, getMinusPosition(newBraggAngle));
		checkPositions(centreCrystal, getCentrePosition(newBraggAngle));
		// 'plus' crystal should be at original location
		checkPositions(plusCrystal, origPlusPositions);
	}

	@Test
	public void testPositionsAreCorrectDetectorTrajectory() throws DeviceException {
		double currentBragg = (double) xesSpectrometer.getPosition();

		// Do 5 degree move, moving detector along trajectory with 0.5 degree steps.
		double braggAngle = currentBragg + 5;
		xesSpectrometer.setTrajectoryStepSize(0.5);
		xesSpectrometer.moveTo(braggAngle);

		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(braggAngle));
		checkPositions(minusCrystal, getMinusPosition(braggAngle));
		checkPositions(centreCrystal, getCentrePosition(braggAngle));
		checkPositions(plusCrystal, getPlusPosition(braggAngle));
	}

	@Test
	public void testInvalidPositionGivesDeviceException() throws DeviceException {
		double currentBragg = (double) xesSpectrometer.getPosition();

		XesSpectrometerCrystal crystal = centreCrystal;
		ScannableMotor mot = (ScannableMotor) crystal.getPitchMotor();
		double currentPos = (double) mot.getPosition();
		mot.getMotor().setMinPosition(currentPos-1);
		mot.getMotor().setMaxPosition(currentPos+1);

		// Do 5 degree move, moving detector along trajectory with 0.5 degree steps.
		Assert.assertThrows("Move pitch outside of motor limit", DeviceException.class, () -> xesSpectrometer.moveTo(currentBragg + 10));
		Assert.assertThrows("Move pitch outside of motor limit", DeviceException.class, () -> xesSpectrometer.moveTo(currentBragg - 10));

	}

	@Test
	public void testMoveOutsideBraggAngleLimitsGivesException() throws DeviceException {
		double currentBragg = (double) xesSpectrometer.getPosition();
		xesSpectrometer.setMinTheta(currentBragg-1);
		xesSpectrometer.setMaxTheta(currentBragg+1);
		// Moving Bragg angle beyond the minTheta and maxTheta limits should throw an exception
		Assert.assertThrows("Move to more than upper Bragg limit", DeviceException.class, () -> xesSpectrometer.moveTo(currentBragg + 10));
		Assert.assertThrows("Move to less than lower Bragg limit", DeviceException.class, () -> xesSpectrometer.moveTo(currentBragg - 10));
	}

	@Test
	public void testInvalidBraggGivesException() {
		Assert.assertThrows("Move to NaN", DeviceException.class, () -> xesSpectrometer.moveTo(Double.NaN));
		Assert.assertThrows("Move to +Infinity", DeviceException.class, () -> xesSpectrometer.moveTo(Double.NEGATIVE_INFINITY));
		Assert.assertThrows("Move to -Infinity", DeviceException.class, () -> xesSpectrometer.moveTo(Double.POSITIVE_INFINITY));
	}

	@Test
	public void testRepeatedMove() throws DeviceException, FactoryException {
		double oldBragg = (double) xesSpectrometer.getPosition();
		double newBragg = oldBragg + 5.0;

		// Move to new Bragg angle to force a move
		xesSpectrometer.moveTo(newBragg);

		// get the current detector position, and central analyser positions
		Double[] detectorPositions = ScannableUtils.objectToArray(xesSpectrometer.getDetectorGroup().getPosition());
		Double[] centreAnalyserPositions = ScannableUtils.objectToArray(centreCrystal.getPosition());

		// Setup dummy centre analyser, mock motors that return values for current bragg angle :
		XesSpectrometerCrystal specCrystal = new XesSpectrometerCrystal();
		specCrystal.setHorizontalIndex(0);
		specCrystal.setxMotor(createMockScannableMotor("centreX", centreAnalyserPositions[0]));
		specCrystal.setyMotor(createMockScannableMotor("centreY", centreAnalyserPositions[1]));
		specCrystal.setRotMotor(createMockScannableMotor("centreRot", centreAnalyserPositions[2]));
		specCrystal.setPitchMotor(createMockScannableMotor("centrePitch", centreAnalyserPositions[3]));
		specCrystal.setName("centreAnalyser");
		specCrystal.configure();

		// dummy detector group - mock motors with positions for current bragg angle
		ScannableGroup detGroup = new ScannableGroup();
		detGroup.addGroupMember(createMockScannableMotor("detX", detectorPositions[0]));
		detGroup.addGroupMember(createMockScannableMotor("detY", detectorPositions[1]));
		detGroup.addGroupMember(createMockScannableMotor("detRot", detectorPositions[2]));
		detGroup.setName("detGroup");
		detGroup.configure();

		xesSpectrometer.setCrystalsGroup(new ScannableGroup("mockCentreCrystal", List.of(specCrystal)));
		xesSpectrometer.setDetectorGroup(detGroup);

		// need to reconfigure to update the crystals list
		xesSpectrometer.reconfigure();

		// Move to same Bragg angle again
		xesSpectrometer.moveTo(newBragg);

		List<Scannable> allScannables = new ArrayList<>(specCrystal.getGroupMembers());
		allScannables.addAll(detGroup.getGroupMembers());

		// Verify that nothing has moved
		for(var scn : allScannables) {
			Mockito.verify(scn, times(0)).asynchronousMoveTo(any());
		}

		// Move back to the old Bragg angle
		xesSpectrometer.moveTo(oldBragg);

		// Verify that everything has moved
		for(var scn : allScannables) {
			Mockito.verify(scn, times(1)).asynchronousMoveTo(any());
		}
	}

	private void checkPositions(ScannableGroup group, double[] expectedPositions) throws NumberFormatException, DeviceException {
		SpectrometerTestFunctions.checkPositions(group, expectedPositions, tolerance);
	}
	private void checkPositions(ScannableGroup group, Double[] expectedPositions) throws NumberFormatException, DeviceException {
		SpectrometerTestFunctions.checkPositions(group, expectedPositions, tolerance);
	}

	private void checkRoundedPositions(ScannableGroup group, double[] expectedPositions, double[] tolerances) throws NumberFormatException, DeviceException {
		// Generate the demand expected values
		double[] roundedDemand = getRoundedNumbers(expectedPositions, tolerances);
		SpectrometerTestFunctions.checkPositions(group, roundedDemand, tolerance);
	}

	private double[] getRoundedNumbers(double[] position, double[] rounding) {
		double[] newVals = new double[position.length];
		for(int i=0; i<position.length; i++) {
			newVals[i] = getRoundedNumber(position[i], rounding[i]);
		}
		return newVals;
	}

	private double getRoundedNumber(double number, double rounding) {
		return Math.round(number/rounding)*rounding;
	}

	private double[] getDetPosition(double bragg) {
		return new double[] {XesUtils.getDx(radius, bragg), XesUtils.getDy(radius, bragg), 2.0*XesUtils.getCrystalRotation(bragg)};
	}

	private double[] getMinusPosition(double bragg) {
		double[] values = XesUtils.getAnalyserValues(radius, bragg, -xesSpectrometer.getHorizontalCrystalOffset());
		if (!xesSpectrometer.isAbsoluteXPos()) {
			double xpos = values[0] - XesUtils.getL(radius, bragg);
			values[0] = xpos;
		}
		return values;
	}

	private double[] getPlusPosition(double bragg) {
		double[] minusPos = getMinusPosition(bragg);
		return new double[] {minusPos[0], minusPos[1], -minusPos[2], minusPos[3]};
	}

	private double[] getCentrePosition(double bragg) {
		double angle = XesUtils.getCrystalRotation(bragg);
		return new double[] {XesUtils.getL(radius, bragg), 0, 0, angle};
	}

	private double getSpectrometerXPos(double bragg) {
		return XesUtils.getL(radius, bragg);
	}
}
