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

import javax.measure.Quantity;
import javax.measure.Unit;

import org.junit.BeforeClass;
import org.junit.Test;

import tec.units.indriya.format.SimpleUnitFormat;

/**
 * NonSIext defines aliases for variants in unit specification e.g. <code>Deg</code>, <code>deg</code>, <code>degrees</code>
 * <p>
 * These test verify that the same object with the canonical form of the unit string is created in each case.
 */
public class NonSIextTest {

	private static SimpleUnitFormat unitFormat = SimpleUnitFormat.getInstance();

	@BeforeClass
	public static void setUp() {
		NonSIext.initializeClass();
	}

	@Test
	public void testDegrees() {
		final List<Unit<? extends Quantity<?>>> degrees = Arrays.asList(
				unitFormat.parse(DEG_ANGLE_STRING),
				unitFormat.parse(DEG_ANGLE_LOWERCASE_STRING),
				unitFormat.parse(DEGREES_ANGLE_STRING));
		testUnitStrings(degrees, DEG_ANGLE_LOWERCASE_STRING);
	}

	@Test
	public void testMilliDegrees() {
		final List<Unit<? extends Quantity<?>>> milliDegrees = Arrays.asList(
				unitFormat.parse(MILLI_DEG_ANGLE_STRING),
				unitFormat.parse(MILLI_DEG_ANGLE_LOWERCASE_STRING));
		testUnitStrings(milliDegrees, MILLI_DEG_ANGLE_LOWERCASE_STRING);
	}

	@Test
	public void testMicroDegrees() {
		final List<Unit<? extends Quantity<?>>> microDegrees = Arrays.asList(
				unitFormat.parse(MICRO_DEG_ANGLE_STRING),
				unitFormat.parse(MICRO_DEG_MU_ANGLE_STRING),
				unitFormat.parse(MICRO_DEG_U_ANGLE_STRING));
		testUnitStrings(microDegrees, MICRO_DEG_ANGLE_STRING);
	}

	@Test
	public void testMilliRadians() {
		final List<Unit<? extends Quantity<?>>> milliRadians = Arrays.asList(
				unitFormat.parse(MILLI_RADIAN_ANGLE_STRING),
				unitFormat.parse(MILLI_RADIAN_ANGLE_LOWERCASE_STRING));
		testUnitStrings(milliRadians, MILLI_RADIAN_ANGLE_LOWERCASE_STRING);
	}

	@Test
	public void testMicroRadians() {
		final List<Unit<? extends Quantity<?>>> microRadians = Arrays.asList(
				unitFormat.parse(MICRO_RADIAN_ANGLE_STRING),
				unitFormat.parse(MICRO_RADIAN_MU_ANGLE_STRING),
				unitFormat.parse(MICRO_RADIAN_ANGLE_LOWERCASE_STRING),
				unitFormat.parse(MICRO_RADIAN_MU_ANGLE_LOWERCASE_STRING),
				unitFormat.parse(MICRO_RADIAN_U_ANGLE_STRING),
				unitFormat.parse(MICRO_RADIAN_U_ANGLE_LOWERCASE_STRING));
		testUnitStrings(microRadians, MICRO_RADIAN_ANGLE_LOWERCASE_STRING);
	}

	@Test
	public void testMicrons() {
		final List<Unit<? extends Quantity<?>>> microns = Arrays.asList(
				unitFormat.parse(MICRON_STRING),
				unitFormat.parse(MICRONS_STRING),
				unitFormat.parse(MICRON_UM_STRING),
				unitFormat.parse(MICRON_MU_STRING),
				unitFormat.parse(MICRON_SYMBOL_STRING));
		testUnitStrings(microns, MICRON_SYMBOL_STRING);
	}

	@Test
	public void testMicroSeconds() {
		final List<Unit<? extends Quantity<?>>> microSeconds = Arrays.asList(
				unitFormat.parse(MICROSECOND_STRING),
				unitFormat.parse(MICROSECOND_U_STRING),
				unitFormat.parse(MICROSECOND_MU_STRING),
				unitFormat.parse(MICROSECOND_LOWERCASE_STRING),
				unitFormat.parse(MICROSECOND_U_LOWERCASE_STRING),
				unitFormat.parse(MICROSECOND_MU_LOWERCASE_STRING));
		testUnitStrings(microSeconds, MICROSECOND_LOWERCASE_STRING);
	}

	@Test
	public void testAnstroms() {
		final List<Unit<? extends Quantity<?>>> angstroms = Arrays.asList(
				unitFormat.parse(ANG_STRING),
				unitFormat.parse(ANGSTROM_STRING),
				unitFormat.parse(ANGSTROM_SYMBOL),
				unitFormat.parse(ANGSTROM_SYMBOL_ALTERNATIVE));
		testUnitStrings(angstroms, ANGSTROM_STRING);
	}

	@Test
	public void testPerAnstrom() {
		final List<Unit<? extends Quantity<?>>> perAngstrom = Arrays.asList(unitFormat.parse(PER_ANGSTROM_STRING));
		testUnitStrings(perAngstrom, PER_ANGSTROM_STRING);
	}

	@Test
	public void testCentigrade() {
		final List<Unit<? extends Quantity<?>>> centigrade = Arrays.asList(unitFormat.parse(CENTIGRADE_STRING));
		testUnitStrings(centigrade, CENTIGRADE_STRING);
	}

	@Test
	public void testKiloElectronVolts() {
		final List<Unit<? extends Quantity<?>>> kiloElectronVolts = Arrays.asList(unitFormat.parse(KILOELECTRONVOLT_STRING));
		testUnitStrings(kiloElectronVolts, KILOELECTRONVOLT_STRING);
	}

	@Test
	public void testGigaElectronVolts() {
		final List<Unit<? extends Quantity<?>>> gigaElectronVolts = Arrays.asList(unitFormat.parse(GIGAELECTRONVOLT_STRING));
		testUnitStrings(gigaElectronVolts, GIGAELECTRONVOLT_STRING);
	}

	@Test
	public void testMicroAmperes() {
		final List<Unit<? extends Quantity<?>>> microAmperes = Arrays.asList(
				unitFormat.parse(MICROAMPERE_STRING),
				unitFormat.parse(MICROAMPERE_U_STRING),
				unitFormat.parse(MICROAMPERE_MU_STRING));
		testUnitStrings(microAmperes, MICROAMPERE_STRING);
	}

	@Test
	public void testCounts() {
		final List<Unit<? extends Quantity<?>>> counts = Arrays.asList(
				unitFormat.parse(COUNT_STRING),
				unitFormat.parse(COUNTS_STRING));
		testUnitStrings(counts, COUNTS_STRING);
	}

	@Test
	public void testKiloCounts() {
		final List<Unit<? extends Quantity<?>>> kiloCounts = Arrays.asList(
				unitFormat.parse(KILOCOUNT_STRING),
				unitFormat.parse(KILOCOUNTS_STRING),
				unitFormat.parse(KILOCOUNTS_UC_STRING));
		testUnitStrings(kiloCounts, KILOCOUNTS_STRING);
	}

	@Test
	public void testMicroLitres() {
		final List<Unit<? extends Quantity<?>>> microLitres = Arrays.asList(
				unitFormat.parse(MICROLITRE_STRING),
				unitFormat.parse(MICROLITRE_U_STRING),
				unitFormat.parse(MICROLITRE_MU_STRING));
		testUnitStrings(microLitres, MICROLITRE_STRING);
	}

	private void testUnitStrings(List<Unit<? extends Quantity<?>>> units, String expectedString) {
		for (Unit<? extends Quantity<?>> unit : units) {
			assertEquals(expectedString, NonSIext.getUnitString(unit));
		}
	}
}
