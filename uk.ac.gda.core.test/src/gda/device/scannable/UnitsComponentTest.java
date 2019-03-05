/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import static org.jscience.physics.units.SI.METER;
import static org.jscience.physics.units.SI.MILLI;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.python.core.PyFloat;
import org.python.core.PyTuple;

import gda.device.DeviceException;
import gda.device.scannable.component.UnitsComponent;

/**
 * Note: this is largely tested through ScannableMotionUnitsBaseTeast
 */
public class UnitsComponentTest {
	private UnitsComponent uc;
	private static final Quantity q1m = Quantity.valueOf(1, METER);
	private static final Quantity q2m = Quantity.valueOf(2, METER);
	private static final Quantity q1000mm = Quantity.valueOf(1000, MILLI(METER));
	private static final Quantity q3000mm = Quantity.valueOf(3000, MILLI(METER));

	private static final List<String> LENGTH_UNITS = Arrays.asList(
			"m", "nm", "mm", "µm", "micron", "um", "Ang", "Angstrom", "micron", "microns", "m");

	private static final List<String> ANGLE_UNITS = Arrays.asList(
			"rad", "Deg", "degrees", "mDeg", "deg", "mdeg", "mRad", "mrad", "uDeg", "uRad", "urad");

	private static final List<String> TEMPERATURE_UNITS = Arrays.asList("centigrade", "K");
	private static final List<String> FORCE_UNITS = Arrays.asList("N");
	private static final List<String> ELECTRICAL_POTENTIAL_UNITS = Arrays.asList("V");
	private static final List<String> COUNT_UNITS = Arrays.asList("cts", "kcts");
	private static final List<String> ENERGY_UNITS = Arrays.asList("keV", "eV", "GeV");
	private static final List<String> DIMENSIONLESS_UNITS = Arrays.asList(Unit.ONE.toString());
	private static final List<String> ELECTRIC_CURRENT_UNITS = Arrays.asList("A", "μA", "uA", "mA");
	private static final List<String> DURATION_UNITS = Arrays.asList("s", "ms");
	private static final List<String> VOLUME_UNITS = Arrays.asList("L", "m³");
	private static final List<String> VOLUMETRIC_DENSITY_UNITS = Arrays.asList("mg/mL");

	@BeforeClass
	public static void setUpClass() {
		Collections.sort(LENGTH_UNITS);
		Collections.sort(ANGLE_UNITS);
		Collections.sort(TEMPERATURE_UNITS);
		Collections.sort(FORCE_UNITS);
		Collections.sort(ELECTRICAL_POTENTIAL_UNITS);
		Collections.sort(COUNT_UNITS);
		Collections.sort(ENERGY_UNITS);
		Collections.sort(DIMENSIONLESS_UNITS);
		Collections.sort(ELECTRIC_CURRENT_UNITS);
		Collections.sort(DURATION_UNITS);
		Collections.sort(VOLUME_UNITS);
		Collections.sort(VOLUMETRIC_DENSITY_UNITS);
	}

	@Before
	public void setUp() {
		uc = new UnitsComponent();
	}

	@Test
	public void testExternalTowardInternalNoUnitsSet() {
		Object object = new Object();
		assertEquals(object, uc.externalTowardInternal(object));
	}

	@Test
	public void testInternalTowardExternal() {
		Object object = new Object();
		assertEquals(object, uc.internalTowardExternal(object));
	}

	@Test
	public void testExternalTowardInternalWithObjectArray() throws DeviceException {
		uc.setUserUnits("m");
		uc.setHardwareUnitString("mm");
		final double[] internalExpected = new double[] { 1000., 2000., 3000., 4000., 5000. };
		final Object[] result = (Object[]) uc.externalTowardInternal(new Object[] { 1, q2m, q3000mm, "4m", "5000 mm" });
		assertEquals(internalExpected.length, result.length);
		for (int i = 0; i < result.length; i++) {
			assertEquals(internalExpected[i], (double) result[i], 0.0001);
		}
	}

	@Test
	public void testExternalTowardInternalWithPyTuple() throws DeviceException {
		uc.setUserUnits("m");
		uc.setHardwareUnitString("mm");
		final PyTuple internalExpectedTuple = new PyTuple(new PyFloat(1000.), new PyFloat(2000.));
		final PyTuple result = (PyTuple) uc.externalTowardInternal(new PyTuple(new PyFloat(1.), new PyFloat(2.)));
		assertEquals(internalExpectedTuple.size(), result.size());
		for (int i = 0; i < result.size(); i++) {
			assertEquals((double) internalExpectedTuple.get(i), (double) result.get(i), 0.0001);
		}
	}

	@Test
	public void testInternalTowardExternalWithPyTuple() throws DeviceException {
		uc.setHardwareUnitString("mm");
		uc.setUserUnits("m");
		final PyTuple externalExpectedTuple = new PyTuple(new PyFloat(1.), new PyFloat(2.));
		final PyTuple result = (PyTuple) uc.internalTowardExternal(new PyTuple(new PyFloat(1000.), new PyFloat(2000.)));
		assertEquals(externalExpectedTuple.size(), result.size());
		for (int i = 0; i < result.size(); i++) {
			assertEquals((double) externalExpectedTuple.get(i), (double) result.get(i), 0.0001);
		}
	}

	@Test
	public void testExternalTowardInternalWithObject() throws DeviceException {
		uc.setUserUnits("m");
		uc.setHardwareUnitString("mm");
		assertEquals(1000.0, (double) uc.externalTowardInternal(1), 0.0001);
		assertEquals(1000.0, (double) uc.externalTowardInternal(1.), 0.0001);
		assertEquals(1000.0, (double) uc.externalTowardInternal(q1m), 0.0001);
		assertEquals(1000.0, (double) uc.externalTowardInternal(q1000mm), 0.0001);
		assertEquals(1000.0, (double) uc.externalTowardInternal("1"), 0.0001);
		assertEquals(1000.0, (double) uc.externalTowardInternal("1m"), 0.0001);
		assertEquals(1000.0, (double) uc.externalTowardInternal("1000mm"), 0.0001);
	}

	@Test
	public void testInternalTowardExternalWithObjectArray() throws DeviceException {
		uc.setHardwareUnitString("mm");
		uc.setUserUnits("m");
		final double[] externalExpected = new double[] { 1., 2., 3., 4., 5. };
		final Object[] result = (Object[]) uc.internalTowardExternal(new Object[] { 1000, q2m, q3000mm, "4m", "5000 mm" });
		assertEquals(externalExpected.length, result.length);
		for (int i = 0; i < result.length; i++) {
			assertEquals(externalExpected[i], (double) result[i], 0.0001);
		}
	}

	@Test
	public void testInternalTowardExternalWithObject() throws DeviceException {
		uc.setHardwareUnitString("mm");
		uc.setUserUnits("m");
		assertEquals(1.0, (double) uc.internalTowardExternal(1000), 0.0001);
		assertEquals(1.0, (double) uc.internalTowardExternal(1000.), 0.0001);
		assertEquals(1.0, (double) uc.internalTowardExternal(q1m), 0.0001);
		assertEquals(1.0, (double) uc.internalTowardExternal(q1000mm), 0.0001);
		assertEquals(1.0, (double) uc.internalTowardExternal("1000"), 0.0001);
		assertEquals(1.0, (double) uc.internalTowardExternal("1m"), 0.0001);
		assertEquals(1.0, (double) uc.internalTowardExternal("1000mm"), 0.0001);
	}

	@Test
	public void testGetAcceptableUnitsLength() throws DeviceException {
		// Any length unit should return all length units as compatible
		for (String unit : LENGTH_UNITS) {
			testGetAcceptableUnits(unit, LENGTH_UNITS);
		}
	}

	@Test
	public void testGetAcceptableUnitsAngle() throws DeviceException {
		for (String unit : ANGLE_UNITS) {
			testGetAcceptableUnits(unit, ANGLE_UNITS);
		}
	}

	@Test
	public void testGetAcceptableUnitsTemperature() throws DeviceException {
		for (String unit : TEMPERATURE_UNITS) {
			testGetAcceptableUnits(unit, TEMPERATURE_UNITS);
		}
	}

	@Test
	public void testGetAcceptableUnitsForce() throws DeviceException {
		for (String unit : FORCE_UNITS) {
			testGetAcceptableUnits(unit, FORCE_UNITS);
		}
	}

	@Test
	public void testGetAcceptableUnitsElectricalPotential() throws DeviceException {
		for (String unit : ELECTRICAL_POTENTIAL_UNITS) {
			testGetAcceptableUnits(unit, ELECTRICAL_POTENTIAL_UNITS);
		}
	}

	@Test
	public void testGetAcceptableUnitsCount() throws DeviceException {
		for (String unit : COUNT_UNITS) {
			testGetAcceptableUnits(unit, COUNT_UNITS);
		}
	}

	@Test
	public void testGetAcceptableUnitsEnergy() throws DeviceException {
		for (String unit : ENERGY_UNITS) {
			testGetAcceptableUnits(unit, ENERGY_UNITS);
		}
	}

	@Test
	public void testGetAcceptableUnitsDimensionless() throws DeviceException {
		uc.setHardwareUnitString(Unit.ONE.toString());
		final String[] acceptableUnits = uc.getAcceptableUnits();
		assertEquals(1, acceptableUnits.length);
		assertEquals("", acceptableUnits[0]);
	}

	@Test
	public void testGetAcceptableUnitsElectricCurrent() throws DeviceException {
		for (String unit : ELECTRIC_CURRENT_UNITS) {
			testGetAcceptableUnits(unit, ELECTRIC_CURRENT_UNITS);
		}
	}

	@Test
	public void testGetAcceptableUnitsDuration() throws DeviceException {
		for (String unit : DURATION_UNITS) {
			testGetAcceptableUnits(unit, DURATION_UNITS);
		}
	}

	@Test
	public void testGetAcceptableUnitsVolume() throws DeviceException {
		for (String unit : VOLUME_UNITS) {
			testGetAcceptableUnits(unit, VOLUME_UNITS);
		}
	}

	@Test
	public void testGetAcceptableUnitsVolumetricDensity() throws DeviceException {
		for (String unit : VOLUMETRIC_DENSITY_UNITS) {
			testGetAcceptableUnits(unit, VOLUMETRIC_DENSITY_UNITS);
		}
	}

	@Test(expected = DeviceException.class)
	public void testGetAcceptableUnitsVolumetricDensityGramsPerLitre() throws DeviceException {
		uc.setHardwareUnitString("g/L");
	}

	private void testGetAcceptableUnits(String hardwareUnitString, List<String> expectedAcceptableUnits) throws DeviceException {
		uc.setHardwareUnitString(hardwareUnitString);
		final List<String> acceptableUnits = Arrays.asList(uc.getAcceptableUnits());
		Collections.sort(acceptableUnits);
		assertEquals(expectedAcceptableUnits, acceptableUnits);
	}
}
