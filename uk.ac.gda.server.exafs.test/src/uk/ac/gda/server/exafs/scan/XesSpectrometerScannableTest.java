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

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.MotorException;
import gda.device.Scannable;
import gda.device.enumpositioner.DummyEnumPositioner;
import gda.device.motor.DummyMotor;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.exafs.xes.XesUtils;
import gda.factory.FactoryException;
import uk.ac.gda.beamline.i20.scannable.XesSpectrometerScannable;

public class XesSpectrometerScannableTest {

	private XesSpectrometerScannable xesSpectrometer;
	private double radius = 1000;
	private static final double tolerance = 1e-4;

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

	@Before
	public void setup() throws FactoryException, DeviceException {
		// Save motor persistence files in test-scratch so git doesn't see them
		System.setProperty("gda.motordir", "test-scratch/motors");

		xesSpectrometer = new XesSpectrometerScannable();
		xesSpectrometer.setRadiusScannable(createScannableMotor("radius"));
		xesSpectrometer.setDet_x(createScannableMotor("detX"));
		xesSpectrometer.setDet_y(createScannableMotor("detY"));
		xesSpectrometer.setDet_rot(createScannableMotor("detRot"));
		xesSpectrometer.setXtal_x(createScannableMotor("xtalX"));

		xesSpectrometer.setXtal_minus1_x(createScannableMotor("minusX"));
		xesSpectrometer.setXtal_minus1_y(createScannableMotor("minusY"));
		xesSpectrometer.setXtal_minus1_pitch(createScannableMotor("minusPitch"));
		xesSpectrometer.setXtal_minus1_rot(createScannableMotor("minusRot"));

		xesSpectrometer.setXtal_central_y(createScannableMotor("centreY"));
		xesSpectrometer.setXtal_central_pitch(createScannableMotor("centrePitch"));
		xesSpectrometer.setXtal_central_rot(createScannableMotor("centreRot"));

		xesSpectrometer.setxtal_plus1_x(createScannableMotor("plusX"));
		xesSpectrometer.setxtal_plus1_y(createScannableMotor("plusY"));
		xesSpectrometer.setxtal_plus1_pitch(createScannableMotor("plusPitch"));
		xesSpectrometer.setxtal_plus1_rot(createScannableMotor("plusRot"));

		xesSpectrometer.configure();

		// Set to large value so that detector won't be moved along trajectory
		xesSpectrometer.setTrajectoryStepSize(100.0);

		// set detector and crystal position  give reasonable starting bragg angle
		xesSpectrometer.getDet_y().moveTo(475);
		xesSpectrometer.getXtal_x().moveTo(radius);
		xesSpectrometer.getRadiusScannable().moveTo(radius);

		xesSpectrometer.setMinusCrystalAllowedToMove(createBooleanPositioner("minusAllowedToMove"));
		xesSpectrometer.setCentreCrystalAllowedToMove(createBooleanPositioner("centreAllowedToMove"));
		xesSpectrometer.setPlusCrystalAllowedToMove(createBooleanPositioner("plusAllowedToMove"));

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
		checkPosition(xesSpectrometer.getXtal_x(), getSpectrometerXPos(braggAngle));
		checkPositions(xesSpectrometer.getMinusCrystalGroup(), getMinusPosition(braggAngle));
		checkPositions(xesSpectrometer.getCentreCrystalGroup(), getCentrePosition(braggAngle));
		checkPositions(xesSpectrometer.getPlusCrystalGroup(), getPlusPosition(braggAngle));
	}

	@Test
	public void testNoMinusCrystalMove() throws DeviceException {
		double origBraggAngle = (double) xesSpectrometer.getPosition();
		double newBraggAngle = 75;
		xesSpectrometer.getMinusCrystalAllowedToMove().moveTo(Boolean.FALSE.toString());
		xesSpectrometer.moveTo(newBraggAngle);

		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(newBraggAngle));
		checkPosition(xesSpectrometer.getXtal_x(), getSpectrometerXPos(newBraggAngle));
		// 'minus' crystal should be at original location
		checkPositions(xesSpectrometer.getMinusCrystalGroup(), getMinusPosition(origBraggAngle));
		checkPositions(xesSpectrometer.getCentreCrystalGroup(), getCentrePosition(newBraggAngle));
		checkPositions(xesSpectrometer.getPlusCrystalGroup(), getPlusPosition(newBraggAngle));
	}

	@Test
	public void testNoCentreCrystalMoves() throws DeviceException {
		double origBraggAngle = (double) xesSpectrometer.getPosition();
		double newBraggAngle = 78;
		xesSpectrometer.getCentreCrystalAllowedToMove().moveTo(Boolean.FALSE.toString());
		xesSpectrometer.moveTo(newBraggAngle);

		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(newBraggAngle));
		checkPosition(xesSpectrometer.getXtal_x(), getSpectrometerXPos(newBraggAngle));
		checkPositions(xesSpectrometer.getMinusCrystalGroup(), getMinusPosition(newBraggAngle));
		// centre crystal should be at original location
		checkPositions(xesSpectrometer.getCentreCrystalGroup(), getCentrePosition(origBraggAngle));
		checkPositions(xesSpectrometer.getPlusCrystalGroup(), getPlusPosition(newBraggAngle));
	}

	@Test
	public void testNoPlusCrystalMove() throws DeviceException {
		double origBraggAngle = (double) xesSpectrometer.getPosition();
		double newBraggAngle = 62;
		xesSpectrometer.getPlusCrystalAllowedToMove().moveTo(Boolean.FALSE.toString());
		xesSpectrometer.moveTo(newBraggAngle);

		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(newBraggAngle));
		checkPosition(xesSpectrometer.getXtal_x(), getSpectrometerXPos(newBraggAngle));
		checkPositions(xesSpectrometer.getMinusCrystalGroup(), getMinusPosition(newBraggAngle));
		checkPositions(xesSpectrometer.getCentreCrystalGroup(), getCentrePosition(newBraggAngle));
		// 'plus' crystal should be at original location
		checkPositions(xesSpectrometer.getPlusCrystalGroup(), getPlusPosition(origBraggAngle));
	}

	@Test
	public void testPositionsAreCorrectDetectorTrajectory() throws DeviceException {
		double currentBragg = (double) xesSpectrometer.getPosition();

		// Do 5 degree move, moving detector along trajectory with 0.5 degree steps.
		double braggAngle = currentBragg + 5;
		xesSpectrometer.setTrajectoryStepSize(0.5);
		xesSpectrometer.moveTo(braggAngle);

		checkPositions(xesSpectrometer.getDetectorGroup(), getDetPosition(braggAngle));
		checkPositions(xesSpectrometer.getMinusCrystalGroup(), getMinusPosition(braggAngle));
		checkPositions(xesSpectrometer.getCentreCrystalGroup(), getCentrePosition(braggAngle));
		checkPositions(xesSpectrometer.getPlusCrystalGroup(), getPlusPosition(braggAngle));
		checkPosition(xesSpectrometer.getXtal_x(), getSpectrometerXPos(braggAngle));
	}

	private void checkPositions(ScannableGroup group, double[] expectedPositions) throws NumberFormatException, DeviceException {
		List<Scannable> scannables = group.getGroupMembers();
		assertEquals(expectedPositions.length, scannables.size());
		for(int i=0; i<expectedPositions.length; i++) {
			double position = Double.valueOf(scannables.get(i).getPosition().toString());
			assertEquals("Value for '"+scannables.get(i).getName()+"' in scannable group '"+group.getName()+"' is not within tolerance",
						expectedPositions[i], position, tolerance);
		}
	}

	private void checkPosition(Scannable scannable, double expectedPosition) throws NumberFormatException, DeviceException {
		double position = Double.valueOf(scannable.getPosition().toString());
		assertEquals("Value for '"+scannable.getName()+"' is not within tolerance", expectedPosition, position, tolerance);
	}

	private double[] getDetPosition(double bragg) {
		return new double[] {XesUtils.getDx(radius, bragg), XesUtils.getDy(radius, bragg), 2.0*XesUtils.getCrystalRotation(bragg)};
	}

	private double[] getMinusPosition(double bragg) {
		return XesUtils.getAdditionalCrystalPositions(radius, bragg, xesSpectrometer.getAdditionalCrystalHorizontalOffsets()[0]);
	}

	private double[] getPlusPosition(double bragg) {
		double[] minusPos = getMinusPosition(bragg);
		return new double[] {minusPos[0], minusPos[1], -minusPos[2], minusPos[3]};
	}

	private double[] getCentrePosition(double bragg) {
		double angle = XesUtils.getCrystalRotation(bragg);
		return new double[] {angle};
	}

	private double getSpectrometerXPos(double bragg) {
		return XesUtils.getL(radius, bragg);
	}
}
