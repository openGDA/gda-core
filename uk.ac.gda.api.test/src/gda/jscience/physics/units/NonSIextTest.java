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

package gda.jscience.physics.units;

import static gda.jscience.physics.units.NonSIext.ANG;
import static gda.jscience.physics.units.NonSIext.ANGSTROM;
import static gda.jscience.physics.units.NonSIext.ANGSTROM_STRING;
import static gda.jscience.physics.units.NonSIext.CENTIGRADE;
import static gda.jscience.physics.units.NonSIext.CENTIGRADE_STRING;
import static gda.jscience.physics.units.NonSIext.COUNTS;
import static gda.jscience.physics.units.NonSIext.COUNTS_STRING;
import static gda.jscience.physics.units.NonSIext.DEGREES_ANGLE;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE_LOWERCASE;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.GIGAELECTRONVOLT;
import static gda.jscience.physics.units.NonSIext.GIGAELECTRONVOLT_STRING;
import static gda.jscience.physics.units.NonSIext.KILOCOUNTS;
import static gda.jscience.physics.units.NonSIext.KILOCOUNTS_STRING;
import static gda.jscience.physics.units.NonSIext.KILOELECTRONVOLT;
import static gda.jscience.physics.units.NonSIext.KILOELECTRONVOLT_STRING;
import static gda.jscience.physics.units.NonSIext.MICROAMPERE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRON;
import static gda.jscience.physics.units.NonSIext.MICRONS;
import static gda.jscience.physics.units.NonSIext.MICRON_SYMBOL_STRING;
import static gda.jscience.physics.units.NonSIext.MICRON_UM;
import static gda.jscience.physics.units.NonSIext.MICROSECOND;
import static gda.jscience.physics.units.NonSIext.MICROSECOND_LOWERCASE;
import static gda.jscience.physics.units.NonSIext.MICROSECOND_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MICROSECOND_MU;
import static gda.jscience.physics.units.NonSIext.MICROSECOND_MU_LOWERCASE;
import static gda.jscience.physics.units.NonSIext.MICRO_AMPERE;
import static gda.jscience.physics.units.NonSIext.MICRO_AMPERE_U;
import static gda.jscience.physics.units.NonSIext.MICRO_DEG_ANGLE;
import static gda.jscience.physics.units.NonSIext.MICRO_DEG_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_DEG_U_ANGLE;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_ANGLE;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_U_ANGLE;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_U_ANGLE_LOWERCASE;
import static gda.jscience.physics.units.NonSIext.MILLI_DEG_ANGLE;
import static gda.jscience.physics.units.NonSIext.MILLI_DEG_ANGLE_LOWERCASE;
import static gda.jscience.physics.units.NonSIext.MILLI_DEG_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MILLI_RADIAN_ANGLE;
import static gda.jscience.physics.units.NonSIext.MILLI_RADIAN_ANGLE_LOWERCASE;
import static gda.jscience.physics.units.NonSIext.MILLI_RADIAN_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.PER_ANGSTROM;
import static gda.jscience.physics.units.NonSIext.PER_ANGSTROM_STRING;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.junit.BeforeClass;
import org.junit.Test;

public class NonSIextTest {

	@BeforeClass
	public static void setUp() {
		NonSIext.initializeClass();
	}

	@Test
	public void testDegrees() {
		final List<Unit<? extends Quantity>> degrees = Arrays.asList(DEG_ANGLE, DEG_ANGLE_LOWERCASE, DEGREES_ANGLE);
		testUnitStrings(degrees, DEG_ANGLE_LOWERCASE_STRING);
	}

	@Test
	public void testMilliDegrees() {
		final List<Unit<? extends Quantity>> milliDegrees = Arrays.asList(MILLI_DEG_ANGLE, MILLI_DEG_ANGLE_LOWERCASE);
		testUnitStrings(milliDegrees, MILLI_DEG_ANGLE_LOWERCASE_STRING);
	}

	@Test
	public void testMicroDegrees() {
		final List<Unit<? extends Quantity>> microDegrees = Arrays.asList(MICRO_DEG_ANGLE, MICRO_DEG_U_ANGLE);
		testUnitStrings(microDegrees, MICRO_DEG_ANGLE_STRING);
	}

	@Test
	public void testMilliRadians() {
		final List<Unit<? extends Quantity>> milliRadians = Arrays.asList(MILLI_RADIAN_ANGLE, MILLI_RADIAN_ANGLE_LOWERCASE);
		testUnitStrings(milliRadians, MILLI_RADIAN_ANGLE_LOWERCASE_STRING);
	}

	@Test
	public void testMicroRadians() {
		final List<Unit<? extends Quantity>> microRadians = Arrays.asList(MICRO_RADIAN_ANGLE, MICRO_RADIAN_U_ANGLE, MICRO_RADIAN_U_ANGLE_LOWERCASE);
		testUnitStrings(microRadians, MICRO_RADIAN_ANGLE_LOWERCASE_STRING);
	}

	@Test
	public void testMicrons() {
		final List<Unit<? extends Quantity>> microns = Arrays.asList(MICRON, MICRONS, MICRON_UM);
		testUnitStrings(microns, MICRON_SYMBOL_STRING);
	}

	@Test
	public void testMicroSeconds() {
		final List<Unit<? extends Quantity>> microSeconds = Arrays.asList(MICROSECOND, MICROSECOND_MU, MICROSECOND_LOWERCASE, MICROSECOND_MU_LOWERCASE);
		testUnitStrings(microSeconds, MICROSECOND_LOWERCASE_STRING);
	}

	@Test
	public void testAnstroms() {
		final List<Unit<? extends Quantity>> angstroms = Arrays.asList(ANG, ANGSTROM);
		testUnitStrings(angstroms, ANGSTROM_STRING);
	}

	@Test
	public void testPerAnstrom() {
		final List<Unit<? extends Quantity>> perAngstrom = Arrays.asList(PER_ANGSTROM);
		testUnitStrings(perAngstrom, PER_ANGSTROM_STRING);
	}

	@Test
	public void testCentigrade() {
		final List<Unit<? extends Quantity>> centigrade = Arrays.asList(CENTIGRADE);
		testUnitStrings(centigrade, CENTIGRADE_STRING);
	}

	@Test
	public void testKiloElectronVolts() {
		final List<Unit<? extends Quantity>> kiloElectronVolts = Arrays.asList(KILOELECTRONVOLT);
		testUnitStrings(kiloElectronVolts, KILOELECTRONVOLT_STRING);
	}

	@Test
	public void testGigaElectronVolts() {
		final List<Unit<? extends Quantity>> gigaElectronVolts = Arrays.asList(GIGAELECTRONVOLT);
		testUnitStrings(gigaElectronVolts, GIGAELECTRONVOLT_STRING);
	}

	@Test
	public void testMicroAmperes() {
		final List<Unit<? extends Quantity>> microAmperes = Arrays.asList(MICRO_AMPERE_U, MICRO_AMPERE);
		testUnitStrings(microAmperes, MICROAMPERE_STRING);
	}

	@Test
	public void testCounts() {
		final List<Unit<? extends Quantity>> counts = Arrays.asList(COUNTS);
		testUnitStrings(counts, COUNTS_STRING);
	}

	@Test
	public void testKiloCounts() {
		final List<Unit<? extends Quantity>> counts = Arrays.asList(KILOCOUNTS);
		testUnitStrings(counts, KILOCOUNTS_STRING);
	}

	private void testUnitStrings(List<Unit<? extends Quantity>> units, String expectedString) {
		for (Unit<? extends Quantity> unit : units) {
			assertEquals(expectedString, NonSIext.getUnitString(unit));
		}
	}

}
