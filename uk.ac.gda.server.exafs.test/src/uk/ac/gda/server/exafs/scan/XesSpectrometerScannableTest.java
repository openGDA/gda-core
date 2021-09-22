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

package gda.device.scannable;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.MotorException;
import gda.device.Scannable;
import gda.device.enumpositioner.DummyEnumPositioner;
import gda.device.motor.DummyMotor;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.exafs.xes.XesUtils;
import gda.factory.FactoryException;
import uk.ac.gda.beamline.i20.scannable.XesSpectrometerCrystal;
import uk.ac.gda.beamline.i20.scannable.XesSpectrometerScannable;

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

	private ScannableMotor createScannableMotor(String name) throws MotorException, FactoryException {
		DummyMotor dummyMotor = new DummyMotor();
		dummyMotor.setName(name+"DummyMotor");
		dummyMotor.setMinPosition(-10000);
		dummyMotor.setMaxPosition(10000);
		dummyMotor.setPosition(0);
		dummyMotor.setSpeed(1000000);
		dummyMotor.configure();

		ScannableMotor scnMotor = new ScannableMotor();
		scnMotor.setName(name);
		scnMotor.setMotor(dummyMotor);
		scnMotor.configure();
		return scnMotor;
	}

	private EnumPositioner createBooleanPositioner(String name) throws DeviceException, FactoryException {
		DummyEnumPositioner positioner = new DummyEnumPositioner();
		positioner.setName(name);
		positioner.setPositions(Arrays.asList(Boolean.TRUE.toString(), Boolean.FALSE.toString()));
		positioner.configure();
		positioner.setPosition(Boolean.TRUE.toString());
		return positioner;
	}

	private XesSpectrometerCrystal createCrystalGroup(String baseName, int index) throws MotorException, FactoryException {
		XesSpectrometerCrystal crystal = new XesSpectrometerCrystal();
		crystal.setName(baseName);
		crystal.setHorizontalIndex(index);
		if (index!=0) {
			crystal.setxMotor(createScannableMotor(baseName+"X"));
		}
		crystal.setyMotor(createScannableMotor(baseName+"Y"));
		crystal.setPitchMotor(createScannableMotor(baseName+"Pitch"));
		crystal.setRotMotor(createScannableMotor(baseName+"Rot"));
		crystal.configure();
		return crystal;
	}

	private ScannableGroup createDetectorGroup() throws MotorException, FactoryException {
		ScannableGroup scnGroup = new ScannableGroup();
		scnGroup.setName("detector");
		scnGroup.addGroupMember(createScannableMotor("detX"));
		scnGroup.addGroupMember(createScannableMotor("detY"));
		scnGroup.addGroupMember(createScannableMotor("detRot"));
		scnGroup.configure();
		return scnGroup;
	}

	@Before
	public void setup() throws FactoryException, DeviceException {
		// Save motor persistence files in test-scratch so git doesn't see them
		System.setProperty("gda.motordir", "test-scratch/motors");

		// Set up the 3 XesSpectrometerCrystal objects and ScannableGroup to contain them
		minusCrystal = createCrystalGroup("minus", -1);
		centreCrystal = createCrystalGroup("centre", 0);
		plusCrystal = createCrystalGroup("plus", 1);

		ScannableGroup crystalGroup = new ScannableGroup("crystalGroup",
				Arrays.asList(minusCrystal, centreCrystal, plusCrystal));
		crystalGroup.configure();

		// Setup the 'allowed to move' enum positioners and ScannableGroup to contain them
		minusCrystalAllowedToMove = createBooleanPositioner("minusAllowedToMove");
		centreCrystalAllowedToMove = createBooleanPositioner("centreAllowedToMove");
		plusCrystalAllowedToMove = createBooleanPositioner("plusAllowedToMove");

		ScannableGroup allowedToMoveGroup = new ScannableGroup("allowedToMove",
				Arrays.asList(minusCrystalAllowedToMove, centreCrystalAllowedToMove, plusCrystalAllowedToMove));
		allowedToMoveGroup.configure();

		xesSpectrometer = new XesSpectrometerScannable();
		xesSpectrometer.setName("xesSpectrometer");
		xesSpectrometer.setRadiusScannable(createScannableMotor("radius"));
		xesSpectrometer.setDetectorGroup(createDetectorGroup());
		xesSpectrometer.setSpectrometerX(createScannableMotor("xtalX"));
		xesSpectrometer.setCrystalsGroup(crystalGroup);
		xesSpectrometer.setCrystalsAllowedToMove(allowedToMoveGroup);
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
		double braggAngle = 35;
		xesSpectrometer.moveTo(braggAngle);

		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(braggAngle));
		checkPosition(xesSpectrometer.getSpectrometerX(), getSpectrometerXPos(braggAngle));
		checkPositions(minusCrystal, getMinusPosition(braggAngle));
		checkPositions(centreCrystal, getCentrePosition(braggAngle));
		checkPositions(plusCrystal, getPlusPosition(braggAngle));
	}

	@Test
	public void testAgainstOldPositions() throws DeviceException, IOException {
		double[] angles = {65, 70, 75, 80, 85};
		for(double braggAngle : angles) {
			xesSpectrometer.moveTo(braggAngle);
			Map<String, Double> expectedPositions = getScannablePositionsFromFile(braggAngle);
			for(var ent : xesSpectrometer.getScannablePositions().entrySet()) {
				Double expected = expectedPositions.get(ent.getKey());
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
	public void generatePositionsFiles() throws DeviceException, IOException {
		String filePathPattern = scannablePositionsFilePath.replace("positions", "new_positions");
		double[] angles = {65, 70, 75, 80, 85};
		for(double angle : angles) {
			xesSpectrometer.moveTo(angle);
			Map<String, Double> map = xesSpectrometer.getScannablePositions();
			String jsonString = gson.toJson(map).replace(",", ",\n");
			Files.writeString(Paths.get(String.format(filePathPattern, angle)), jsonString);
		}
	}

	@Test
	public void testNoMinusCrystalMove() throws DeviceException {
		double origBraggAngle = (double) xesSpectrometer.getPosition();
		double newBraggAngle = 75;
		minusCrystalAllowedToMove.moveTo(Boolean.FALSE.toString());
		xesSpectrometer.moveTo(newBraggAngle);

		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(newBraggAngle));
		checkPosition(xesSpectrometer.getSpectrometerX(), getSpectrometerXPos(newBraggAngle));
		// 'minus' crystal should be at original location
		checkPositions(minusCrystal, getMinusPosition(origBraggAngle));
		checkPositions(centreCrystal, getCentrePosition(newBraggAngle));
		checkPositions(plusCrystal, getPlusPosition(newBraggAngle));
	}

	@Test
	public void testNoCentreCrystalMoves() throws DeviceException {
		double origBraggAngle = (double) xesSpectrometer.getPosition();
		double newBraggAngle = 78;
		centreCrystalAllowedToMove.moveTo(Boolean.FALSE.toString());
		xesSpectrometer.moveTo(newBraggAngle);

		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(newBraggAngle));
		checkPosition(xesSpectrometer.getSpectrometerX(), getSpectrometerXPos(newBraggAngle));
		checkPositions(minusCrystal, getMinusPosition(newBraggAngle));
		// centre crystal should be at original location
		checkPositions(centreCrystal, getCentrePosition(origBraggAngle));
		checkPositions(plusCrystal, getPlusPosition(newBraggAngle));
	}

	@Test
	public void testNoPlusCrystalMove() throws DeviceException {
		double origBraggAngle = (double) xesSpectrometer.getPosition();
		double newBraggAngle = 62;
		plusCrystalAllowedToMove.moveTo(Boolean.FALSE.toString());
		xesSpectrometer.moveTo(newBraggAngle);

		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(newBraggAngle));
		checkPosition(xesSpectrometer.getSpectrometerX(), getSpectrometerXPos(newBraggAngle));
		checkPositions(minusCrystal, getMinusPosition(newBraggAngle));
		checkPositions(centreCrystal, getCentrePosition(newBraggAngle));
		// 'plus' crystal should be at original location
		checkPositions(plusCrystal, getPlusPosition(origBraggAngle));
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
		checkPosition(xesSpectrometer.getSpectrometerX(), getSpectrometerXPos(braggAngle));
	}

	@Test(expected = DeviceException.class)
	public void testInvalidPositionGivesDeviceException() throws DeviceException {
		double currentBragg = (double) xesSpectrometer.getPosition();

		XesSpectrometerCrystal crystal = centreCrystal;
		ScannableMotor mot = (ScannableMotor) crystal.getPitchMotor();
		double currentPos = (double) mot.getPosition();
		mot.getMotor().setMinPosition(currentPos-1);
		mot.getMotor().setMaxPosition(currentPos+1);

		// Do 5 degree move, moving detector along trajectory with 0.5 degree steps.
		xesSpectrometer.moveTo(currentBragg + 10);
	}

	private void checkPositions(ScannableGroup group, double[] expectedPositions) throws NumberFormatException, DeviceException {
		List<Scannable> scannables = group.getGroupMembers();
		assertEquals(expectedPositions.length, scannables.size());
		for(int i=0; i<expectedPositions.length; i++) {
			double position = Double.parseDouble(scannables.get(i).getPosition().toString());
			assertEquals("Value for '"+scannables.get(i).getName()+"' in scannable group '"+group.getName()+"' is not within tolerance",
						expectedPositions[i], position, tolerance);
		}
	}

	private void checkPosition(Scannable scannable, double expectedPosition) throws NumberFormatException, DeviceException {
		double position = Double.parseDouble(scannable.getPosition().toString());
		assertEquals("Value for '"+scannable.getName()+"' is not within tolerance", expectedPosition, position, tolerance);
	}

	private double[] getDetPosition(double bragg) {
		return new double[] {XesUtils.getDx(radius, bragg), XesUtils.getDy(radius, bragg), 2.0*XesUtils.getCrystalRotation(bragg)};
	}

	private double[] getMinusPosition(double bragg) {
		return XesUtils.getAdditionalCrystalPositions(radius, bragg, -xesSpectrometer.getHorizontalCrystalOffset());
	}

	private double[] getPlusPosition(double bragg) {
		double[] minusPos = getMinusPosition(bragg);
		return new double[] {minusPos[0], minusPos[1], -minusPos[2], minusPos[3]};
	}

	private double[] getCentrePosition(double bragg) {
		double angle = XesUtils.getCrystalRotation(bragg);
		return new double[] {0, 0, angle};
	}

	private double getSpectrometerXPos(double bragg) {
		return XesUtils.getL(radius, bragg);
	}
}
