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

import static gda.jscience.physics.units.NonSIext.DEG_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE_SYMBOL;
import static gda.jscience.physics.units.NonSIext.MILLI_DEG_ANGLE_LOWERCASE_STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static si.uom.NonSI.DEGREE_ANGLE;
import static tec.units.indriya.AbstractUnit.ONE;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.MetricPrefix.NANO;
import static tec.units.indriya.unit.Units.METRE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.junit.BeforeClass;
import org.junit.Test;
import org.python.core.PyFloat;
import org.python.core.PyTuple;

import gda.device.DeviceException;
import gda.device.scannable.component.UnitsComponent;
import si.uom.quantity.Density;
import tec.units.indriya.quantity.Quantities;

/**
 * Note: the tests in {@link ScannableMotionUnitsBaseTest} also test this class indirectly
 */
public class UnitsComponentTest {
	private static final Quantity<Length> q1m = Quantities.getQuantity(1, METRE);
	private static final Quantity<Length> q2m = Quantities.getQuantity(2, METRE);
	private static final Quantity<Length> q1000mm = Quantities.getQuantity(1000, MILLI(METRE));
	private static final Quantity<Length> q3000mm = Quantities.getQuantity(3000, MILLI(METRE));

	private static final List<String> LENGTH_UNITS = Arrays.asList(
			"m", "nm", "mm", "\u00b5m", "\u03bcm", "micron", "um", "Ang", "Angstrom", "\u212b", "\u00c5", "microns");

	private static final List<String> ANGLE_UNITS = Arrays.asList(
			"rad", "Deg", "degrees", DEG_ANGLE_SYMBOL,
			"mDeg", "deg", "mdeg", "mRad", "mrad",
			"uDeg", "\u00b5Deg", "\u03bcDeg", "uRad", "urad", "\u00b5Rad", "\u03bcRad", "\u00b5rad", "\u03bcrad");

	private static final List<String> TEMPERATURE_UNITS = Arrays.asList("centigrade", "K");
	private static final List<String> FORCE_UNITS = Arrays.asList("N");
	private static final List<String> ELECTRICAL_POTENTIAL_UNITS = Arrays.asList("V");
	private static final List<String> COUNT_UNITS = Arrays.asList("Kcount", "ct", "cts", "kct", "kcts");
	private static final List<String> ENERGY_UNITS = Arrays.asList("J", "keV", "eV", "GeV");
	private static final List<String> DIMENSIONLESS_UNITS = Arrays.asList(ONE.toString(), "");
	private static final List<String> ELECTRIC_CURRENT_UNITS = Arrays.asList("A", "\u00b5A", "\u03bcA", "uA", "mA");
	private static final List<String> DURATION_UNITS = Arrays.asList("s", "ms");
	private static final List<String> VOLUME_UNITS = Arrays.asList("L", "l", "m\u00b3", "\u00b5L", "uL", "\u03bcL", "\u33a5");
	private static final List<String> VOLUMETRIC_DENSITY_UNITS = Arrays.asList("mg/ml");
	private static final List<String> PRESSURE_UNITS = Arrays.asList("Pa", "mPa", "kPa", "MPa", "bar", "mbar");

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
		Collections.sort(PRESSURE_UNITS);
	}

	@Test
	public void testInitialState() {
		final UnitsComponent<? extends Quantity<?>> uc = new UnitsComponent<>();
		assertEquals("", uc.getUserUnitString());
		assertEquals("", uc.getHardwareUnitString());
		assertFalse(uc.unitHasBeenSet());
		assertEquals(ONE, uc.getUserUnit());
		assertEquals(ONE, uc.getHardwareUnit());
		final List<String> acceptableUnits = Arrays.asList(uc.getAcceptableUnits());
		Collections.sort(acceptableUnits);
		assertEquals(DIMENSIONLESS_UNITS, acceptableUnits);
	}

	@Test
	public void testExternalTowardInternalNoUnitsSet() {
		final UnitsComponent<? extends Quantity<?>> uc = new UnitsComponent<>();
		Object object = new Object();
		assertEquals(object, uc.externalTowardInternal(object));
	}

	@Test
	public void testInternalTowardExternal() {
		final UnitsComponent<? extends Quantity<?>> uc = new UnitsComponent<>();
		Object object = new Object();
		assertEquals(object, uc.internalTowardExternal(object));
	}

	@Test
	public void testExternalTowardInternalWithObjectArray() throws DeviceException {
		final UnitsComponent<Length> uc = new UnitsComponent<>();
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
		final UnitsComponent<Length> uc = new UnitsComponent<>();
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
		final UnitsComponent<Length> uc = new UnitsComponent<>();
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
		final UnitsComponent<Length> uc = new UnitsComponent<>();
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
		final UnitsComponent<Length> uc = new UnitsComponent<>();
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
		final UnitsComponent<Length> uc = new UnitsComponent<>();
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
		for (String unit : DIMENSIONLESS_UNITS) {
			testGetAcceptableUnits(unit, DIMENSIONLESS_UNITS);
		}
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

	@Test
	public void testGetAcceptableUnitsPressure() throws DeviceException {
		for (String unit : PRESSURE_UNITS) {
			testGetAcceptableUnits(unit, PRESSURE_UNITS);
		}
	}

	@Test(expected = DeviceException.class)
	public void testGetAcceptableUnitsVolumetricDensityGramsPerLitre() throws DeviceException {
		final UnitsComponent<Density> uc = new UnitsComponent<>();
		uc.setHardwareUnitString("g/L");
	}

	private <Q extends Quantity<Q>> void testGetAcceptableUnits(String hardwareUnitString, List<String> expectedAcceptableUnits) throws DeviceException {
		final UnitsComponent<Q> uc = new UnitsComponent<>();
		uc.setHardwareUnitString(hardwareUnitString);
		final List<String> acceptableUnits = Arrays.asList(uc.getAcceptableUnits());
		Collections.sort(acceptableUnits);
		assertEquals(expectedAcceptableUnits, acceptableUnits);
	}

	@Test
	public void testSetHardwareUnitStringOnly() throws DeviceException {
		final UnitsComponent<Length> uc = new UnitsComponent<>();
		uc.setHardwareUnitString("mm");
		assertTrue(uc.unitHasBeenSet());
		assertEquals("mm", uc.getHardwareUnitString());
		assertEquals(MILLI(METRE), uc.getHardwareUnit());
		assertEquals("mm", uc.getUserUnitString());
		assertEquals(MILLI(METRE), uc.getUserUnit());
	}

	@Test
	public void testSetUserUnitStringOnly() throws DeviceException {
		final UnitsComponent<Length> uc = new UnitsComponent<>();
		uc.setUserUnits("mm");
		assertTrue(uc.unitHasBeenSet());
		assertEquals("mm", uc.getUserUnitString());
		assertEquals(MILLI(METRE), uc.getUserUnit());
		assertEquals("mm", uc.getHardwareUnitString());
		assertEquals(MILLI(METRE), uc.getHardwareUnit());
	}

	@Test
	public void testSetHardwareThenUserUnit() throws DeviceException {
		final UnitsComponent<Length> uc = new UnitsComponent<>();
		uc.setHardwareUnitString("mm");
		uc.setUserUnits("nm");
		assertEquals("mm", uc.getHardwareUnitString());
		assertEquals(MILLI(METRE), uc.getHardwareUnit());
		assertEquals("nm", uc.getUserUnitString());
		assertEquals(NANO(METRE), uc.getUserUnit());
	}

	@Test
	public void testSetUserThenHardwareUnitLength() throws DeviceException {
		final UnitsComponent<Length> uc = new UnitsComponent<>();
		uc.setUserUnits("nm");
		uc.setHardwareUnitString("mm");
		assertEquals("mm", uc.getHardwareUnitString());
		assertEquals(MILLI(METRE), uc.getHardwareUnit());
		assertEquals("nm", uc.getUserUnitString());
		assertEquals(NANO(METRE), uc.getUserUnit());
	}

	@Test
	public void testSetUserThenHardwareUnitAngle() throws DeviceException {
		final UnitsComponent<Angle> uc = new UnitsComponent<>();
		uc.setUserUnits("mDeg");
		uc.setHardwareUnitString("Deg");
		assertEquals(DEG_ANGLE_LOWERCASE_STRING, uc.getHardwareUnitString());
		assertEquals(DEGREE_ANGLE, uc.getHardwareUnit());
		assertEquals(MILLI_DEG_ANGLE_LOWERCASE_STRING, uc.getUserUnitString());
		assertEquals(MILLI(DEGREE_ANGLE), uc.getUserUnit());
	}

	@Test(expected = DeviceException.class)
	public void testSetHardwareThenIncompatibleUserUnit() throws DeviceException {
		final UnitsComponent<Length> uc = new UnitsComponent<>();
		uc.setHardwareUnitString("mm");
		uc.setUserUnits("deg");
	}

	@Test(expected = DeviceException.class)
	public void testSetUserThenIncompatibleHardwareUnit() throws DeviceException {
		final UnitsComponent<Angle> uc = new UnitsComponent<>();
		uc.setUserUnits("deg");
		uc.setHardwareUnitString("mm");
	}

	@Test
	public void testAddAcceptableUnit() throws DeviceException {
		final UnitsComponent<Length> uc = new UnitsComponent<>();
		uc.setHardwareUnitString("mm");
		uc.addAcceptableUnit("pm");

		final List<String> expectedAcceptableUnits = new ArrayList<>(LENGTH_UNITS);
		expectedAcceptableUnits.add("pm");
		Collections.sort(expectedAcceptableUnits);

		final List<String> acceptableUnits = Arrays.asList(uc.getAcceptableUnits());
		Collections.sort(acceptableUnits);

		assertEquals(expectedAcceptableUnits, acceptableUnits);
	}

	@Test
	public void testAddAcceptableUnitIncompatible() throws DeviceException {
		final UnitsComponent<Length> uc = new UnitsComponent<>();
		uc.setHardwareUnitString("mm");
		uc.addAcceptableUnit("deg");

		final List<String> acceptableUnits = Arrays.asList(uc.getAcceptableUnits());
		Collections.sort(acceptableUnits);

		// deg not added, because it is incompatible, so we just get back the usual length units
		assertEquals(LENGTH_UNITS, acceptableUnits);
	}

	@Test
	public void testGetHardwareUnitStringOverridden() throws DeviceException {
		// Units whose default string value is overridden in UnitsComponent
		final UnitsComponent<? extends Quantity<?>> uc = new UnitsComponent<>();
		uc.setHardwareUnitString(DEG_ANGLE_SYMBOL);
		assertEquals("deg", uc.getHardwareUnitString());

		uc.setHardwareUnitString("ct");
		assertEquals("cts", uc.getHardwareUnitString());

		uc.setHardwareUnitString("cts");
		assertEquals("cts", uc.getHardwareUnitString());

		uc.setHardwareUnitString("kct");
		assertEquals("kcts", uc.getHardwareUnitString());

		uc.setHardwareUnitString("kcts");
		assertEquals("kcts", uc.getHardwareUnitString());
	}

	@Test
	public void testGetUserUnitStringOverridden() throws DeviceException {
		// Units whose default string value is overridden in UnitsComponent
		final UnitsComponent<? extends Quantity<?>> uc = new UnitsComponent<>();
		uc.setUserUnits(DEG_ANGLE_SYMBOL);
		assertEquals("deg", uc.getUserUnitString());

		uc.setUserUnits("cts");
		assertEquals("cts", uc.getUserUnitString());

		uc.setUserUnits("kcts");
		assertEquals("kcts", uc.getUserUnitString());
	}
}
