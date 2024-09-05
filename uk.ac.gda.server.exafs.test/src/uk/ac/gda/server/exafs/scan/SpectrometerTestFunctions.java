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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.mockito.Mockito;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.MotorException;
import gda.device.Scannable;
import gda.device.enumpositioner.DummyEnumPositioner;
import gda.device.motor.DummyMotor;
import gda.device.scannable.JohannSpectrometer;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.XesSpectrometerCrystal;
import gda.device.scannable.XesSpectrometerScannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.FactoryException;

/**
 * Collection of static methods for creating various scannables useful for {@link XesSpectrometerScannable} and {@link JohannSpectrometer} JUnit tests.
 */
public class SpectrometerTestFunctions {

	private SpectrometerTestFunctions() {
	}

	public static ScannableMotor createScannableMotor(String name) throws MotorException, FactoryException {
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

	public static ScannableMotor createMockScannableMotor(String name, double position) throws DeviceException {
		var scn = Mockito.mock(ScannableMotor.class);
		Mockito.when(scn.getPosition()).thenReturn(position);
		Mockito.when(scn.getName()).thenReturn(name);
		Mockito.when(scn.getInputNames()).thenReturn(new String[] {name});
		Mockito.when(scn.getExtraNames()).thenReturn(new String[] {});
		return scn;
	}

	public static EnumPositioner createBooleanPositioner(String name) throws DeviceException, FactoryException {
		DummyEnumPositioner positioner = new DummyEnumPositioner();
		positioner.setName(name);
		positioner.setPositions(Arrays.asList(Boolean.TRUE.toString(), Boolean.FALSE.toString()));
		positioner.configure();
		positioner.setPosition(Boolean.TRUE.toString());
		return positioner;
	}

	public static XesSpectrometerCrystal createCrystalGroup(String baseName, int index) throws MotorException, FactoryException {
		XesSpectrometerCrystal crystal = new XesSpectrometerCrystal();
		crystal.setName(baseName);
		crystal.setHorizontalIndex(index);
		crystal.setxMotor(createScannableMotor(baseName+"X"));
		crystal.setyMotor(createScannableMotor(baseName+"Y"));
		crystal.setPitchMotor(createScannableMotor(baseName+"Pitch"));
		crystal.setRotMotor(createScannableMotor(baseName+"Rot"));
		crystal.configure();
		return crystal;
	}

	public static ScannableGroup createScannableGroup(String name, Scannable... scannables) throws FactoryException {
		ScannableGroup allowedToMoveGroup = new ScannableGroup(name, scannables);
		allowedToMoveGroup.configure();
		return allowedToMoveGroup;
	}

	public static ScannableGroup createDetectorGroup() throws MotorException, FactoryException {
		ScannableGroup scnGroup = new ScannableGroup();
		scnGroup.setName("detector");
		scnGroup.addGroupMember(createScannableMotor("detX"));
		scnGroup.addGroupMember(createScannableMotor("detY"));
		scnGroup.addGroupMember(createScannableMotor("detRot"));
		scnGroup.configure();
		return scnGroup;
	}

	public static void checkPositions(ScannableGroup group, Double[] expectedPositions, double tolerance) throws NumberFormatException, DeviceException {
		checkPositions(group, Stream.of(expectedPositions).mapToDouble(Double::doubleValue).toArray(), tolerance);
	}

	public static void checkPositions(ScannableGroup group, double[] expectedPositions, double tolerance) throws NumberFormatException, DeviceException {
		List<Scannable> scannables = group.getGroupMembers();
		assertEquals(expectedPositions.length, scannables.size());
		for(int i=0; i<expectedPositions.length; i++) {
			double position = Double.parseDouble(scannables.get(i).getPosition().toString());
			assertEquals("Value for '"+scannables.get(i).getName()+"' in scannable group '"+group.getName()+"' is not within tolerance",
						expectedPositions[i], position, tolerance);
		}
	}
}
