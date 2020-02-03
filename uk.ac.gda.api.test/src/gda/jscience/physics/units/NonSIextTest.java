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

import static gda.jscience.physics.units.NonSIext.ANGSTROM_STRING;
import static gda.jscience.physics.units.NonSIext.ANGSTROM_SYMBOL;
import static gda.jscience.physics.units.NonSIext.ANGSTROM_SYMBOL_ALTERNATIVE;
import static gda.jscience.physics.units.NonSIext.ANG_STRING;
import static gda.jscience.physics.units.NonSIext.CENTIGRADE_STRING;
import static gda.jscience.physics.units.NonSIext.COUNTS_STRING;
import static gda.jscience.physics.units.NonSIext.COUNT_STRING;
import static gda.jscience.physics.units.NonSIext.DEGREES_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.GIGAELECTRONVOLT_STRING;
import static gda.jscience.physics.units.NonSIext.KILOCOUNTS_STRING;
import static gda.jscience.physics.units.NonSIext.KILOCOUNTS_UC_STRING;
import static gda.jscience.physics.units.NonSIext.KILOCOUNT_STRING;
import static gda.jscience.physics.units.NonSIext.KILOELECTRONVOLT_STRING;
import static gda.jscience.physics.units.NonSIext.MICROAMPERE_MU_STRING;
import static gda.jscience.physics.units.NonSIext.MICROAMPERE_STRING;
import static gda.jscience.physics.units.NonSIext.MICROAMPERE_U_STRING;
import static gda.jscience.physics.units.NonSIext.MICROLITRE_MU_STRING;
import static gda.jscience.physics.units.NonSIext.MICROLITRE_STRING;
import static gda.jscience.physics.units.NonSIext.MICROLITRE_U_STRING;
import static gda.jscience.physics.units.NonSIext.MICRONS_STRING;
import static gda.jscience.physics.units.NonSIext.MICRON_MU_STRING;
import static gda.jscience.physics.units.NonSIext.MICRON_STRING;
import static gda.jscience.physics.units.NonSIext.MICRON_SYMBOL_STRING;
import static gda.jscience.physics.units.NonSIext.MICRON_UM_STRING;
import static gda.jscience.physics.units.NonSIext.MICROSECOND_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MICROSECOND_MU_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MICROSECOND_MU_STRING;
import static gda.jscience.physics.units.NonSIext.MICROSECOND_STRING;
import static gda.jscience.physics.units.NonSIext.MICROSECOND_U_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MICROSECOND_U_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_DEG_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_DEG_MU_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_DEG_U_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_MU_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_MU_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_U_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_U_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MILLI_DEG_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MILLI_DEG_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MILLI_RADIAN_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MILLI_RADIAN_ANGLE_STRING;
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
		final List<Unit<? extends Quantity>> degrees = Arrays.asList(
				Unit.valueOf(DEG_ANGLE_STRING),
				Unit.valueOf(DEG_ANGLE_LOWERCASE_STRING),
				Unit.valueOf(DEGREES_ANGLE_STRING));
		testUnitStrings(degrees, DEG_ANGLE_LOWERCASE_STRING);
	}

	@Test
	public void testMilliDegrees() {
		final List<Unit<? extends Quantity>> milliDegrees = Arrays.asList(
				Unit.valueOf(MILLI_DEG_ANGLE_STRING),
				Unit.valueOf(MILLI_DEG_ANGLE_LOWERCASE_STRING));
		testUnitStrings(milliDegrees, MILLI_DEG_ANGLE_LOWERCASE_STRING);
	}

	@Test
	public void testMicroDegrees() {
		final List<Unit<? extends Quantity>> microDegrees = Arrays.asList(
				Unit.valueOf(MICRO_DEG_ANGLE_STRING),
				Unit.valueOf(MICRO_DEG_MU_ANGLE_STRING),
				Unit.valueOf(MICRO_DEG_U_ANGLE_STRING));
		testUnitStrings(microDegrees, MICRO_DEG_ANGLE_STRING);
	}

	@Test
	public void testMilliRadians() {
		final List<Unit<? extends Quantity>> milliRadians = Arrays.asList(
				Unit.valueOf(MILLI_RADIAN_ANGLE_STRING),
				Unit.valueOf(MILLI_RADIAN_ANGLE_LOWERCASE_STRING));
		testUnitStrings(milliRadians, MILLI_RADIAN_ANGLE_LOWERCASE_STRING);
	}

	@Test
	public void testMicroRadians() {
		final List<Unit<? extends Quantity>> microRadians = Arrays.asList(
				Unit.valueOf(MICRO_RADIAN_ANGLE_STRING),
				Unit.valueOf(MICRO_RADIAN_MU_ANGLE_STRING),
				Unit.valueOf(MICRO_RADIAN_ANGLE_LOWERCASE_STRING),
				Unit.valueOf(MICRO_RADIAN_MU_ANGLE_LOWERCASE_STRING),
				Unit.valueOf(MICRO_RADIAN_U_ANGLE_STRING),
				Unit.valueOf(MICRO_RADIAN_U_ANGLE_LOWERCASE_STRING));
		testUnitStrings(microRadians, MICRO_RADIAN_ANGLE_LOWERCASE_STRING);
	}

	@Test
	public void testMicrons() {
		final List<Unit<? extends Quantity>> microns = Arrays.asList(
				Unit.valueOf(MICRON_STRING),
				Unit.valueOf(MICRONS_STRING),
				Unit.valueOf(MICRON_UM_STRING),
				Unit.valueOf(MICRON_MU_STRING),
				Unit.valueOf(MICRON_SYMBOL_STRING));
		testUnitStrings(microns, MICRON_SYMBOL_STRING);
	}

	@Test
	public void testMicroSeconds() {
		final List<Unit<? extends Quantity>> microSeconds = Arrays.asList(
				Unit.valueOf(MICROSECOND_STRING),
				Unit.valueOf(MICROSECOND_U_STRING),
				Unit.valueOf(MICROSECOND_MU_STRING),
				Unit.valueOf(MICROSECOND_LOWERCASE_STRING),
				Unit.valueOf(MICROSECOND_U_LOWERCASE_STRING),
				Unit.valueOf(MICROSECOND_MU_LOWERCASE_STRING));
		testUnitStrings(microSeconds, MICROSECOND_LOWERCASE_STRING);
	}

	@Test
	public void testAnstroms() {
		final List<Unit<? extends Quantity>> angstroms = Arrays.asList(
				Unit.valueOf(ANG_STRING),
				Unit.valueOf(ANGSTROM_STRING),
				Unit.valueOf(ANGSTROM_SYMBOL),
				Unit.valueOf(ANGSTROM_SYMBOL_ALTERNATIVE));
		testUnitStrings(angstroms, ANGSTROM_STRING);
	}

	@Test
	public void testPerAnstrom() {
		final List<Unit<? extends Quantity>> perAngstrom = Arrays.asList(Unit.valueOf(PER_ANGSTROM_STRING));
		testUnitStrings(perAngstrom, PER_ANGSTROM_STRING);
	}

	@Test
	public void testCentigrade() {
		final List<Unit<? extends Quantity>> centigrade = Arrays.asList(Unit.valueOf(CENTIGRADE_STRING));
		testUnitStrings(centigrade, CENTIGRADE_STRING);
	}

	@Test
	public void testKiloElectronVolts() {
		final List<Unit<? extends Quantity>> kiloElectronVolts = Arrays.asList(Unit.valueOf(KILOELECTRONVOLT_STRING));
		testUnitStrings(kiloElectronVolts, KILOELECTRONVOLT_STRING);
	}

	@Test
	public void testGigaElectronVolts() {
		final List<Unit<? extends Quantity>> gigaElectronVolts = Arrays.asList(Unit.valueOf(GIGAELECTRONVOLT_STRING));
		testUnitStrings(gigaElectronVolts, GIGAELECTRONVOLT_STRING);
	}

	@Test
	public void testMicroAmperes() {
		final List<Unit<? extends Quantity>> microAmperes = Arrays.asList(
				Unit.valueOf(MICROAMPERE_STRING),
				Unit.valueOf(MICROAMPERE_U_STRING),
				Unit.valueOf(MICROAMPERE_MU_STRING));
		testUnitStrings(microAmperes, MICROAMPERE_STRING);
	}

	@Test
	public void testCounts() {
		final List<Unit<? extends Quantity>> counts = Arrays.asList(
				Unit.valueOf(COUNT_STRING),
				Unit.valueOf(COUNTS_STRING));
		testUnitStrings(counts, COUNTS_STRING);
	}

	@Test
	public void testKiloCounts() {
		final List<Unit<? extends Quantity>> kiloCounts = Arrays.asList(
				Unit.valueOf(KILOCOUNT_STRING),
				Unit.valueOf(KILOCOUNTS_STRING),
				Unit.valueOf(KILOCOUNTS_UC_STRING));
		testUnitStrings(kiloCounts, KILOCOUNTS_STRING);
	}

	@Test
	public void testMicroLitres() {
		final List<Unit<? extends Quantity>> microLitres = Arrays.asList(
				Unit.valueOf(MICROLITRE_STRING),
				Unit.valueOf(MICROLITRE_U_STRING),
				Unit.valueOf(MICROLITRE_MU_STRING));
		testUnitStrings(microLitres, MICROLITRE_STRING);
	}

	private void testUnitStrings(List<Unit<? extends Quantity>> units, String expectedString) {
		for (Unit<? extends Quantity> unit : units) {
			assertEquals(expectedString, NonSIext.getUnitString(unit));
		}
	}

}
