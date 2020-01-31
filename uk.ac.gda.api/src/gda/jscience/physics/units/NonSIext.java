/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

import static javax.measure.unit.NonSI.ANGSTROM;
import static javax.measure.unit.NonSI.DEGREE_ANGLE;
import static javax.measure.unit.NonSI.ELECTRON_VOLT;
import static javax.measure.unit.NonSI.LITRE;
import static javax.measure.unit.SI.AMPERE;
import static javax.measure.unit.SI.CELSIUS;
import static javax.measure.unit.SI.GIGA;
import static javax.measure.unit.SI.KILO;
import static javax.measure.unit.SI.METRE;
import static javax.measure.unit.SI.MICRO;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.RADIAN;
import static javax.measure.unit.SI.SECOND;

import java.util.Map;

import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import com.google.common.collect.ImmutableMap;

import gda.jscience.physics.quantities.Count;
import gda.jscience.physics.quantities.Vector;

/**
 * This class contains units that are not part of the {@link NonSI} set<br>
 * It is an extension to NonSI to customise unit usage in GDA.
 * <p>
 * In jscience 4, you can no longer create alternate forms non-standard units, so this class now just creates aliases.
 * Thus, for example <code>micron</code> and <code>um</code> refer to the same underlying unit and will both be displayed
 * as <code>µm</code>
 * <p>
 * The <code>Unit</code> objects are set in a static initialiser. This is necessary because we need to control the
 * order: the aliases must be defined to JScience before being used to create the objects.
 * <p>
 * When adding a unit to this class, please remember to update 'unitStrings' accordingly
 */
@SuppressWarnings("unchecked")
public final class NonSIext {
	public static final String DEG_ANGLE_STRING = "Deg";
	public static final String DEG_ANGLE_LOWERCASE_STRING = "deg";
	public static final String DEGREES_ANGLE_STRING = "degrees";
	public static final String DEG_ANGLE_SYMBOL = "°";
	public static final String MILLI_DEG_ANGLE_STRING = "mDeg";
	public static final String MILLI_DEG_ANGLE_LOWERCASE_STRING = "mdeg";
	public static final String MICRO_DEG_ANGLE_STRING = "\u00b5Deg";
	public static final String MICRO_DEG_MU_ANGLE_STRING = "\u03bcDeg";
	public static final String MICRO_DEG_U_ANGLE_STRING = "uDeg";

	public static final String MILLI_RADIAN_ANGLE_STRING = "mRad";
	public static final String MILLI_RADIAN_ANGLE_LOWERCASE_STRING = "mrad";
	public static final String MICRO_RADIAN_ANGLE_STRING = "\u00b5Rad";
	public static final String MICRO_RADIAN_MU_ANGLE_STRING = "\u03bcRad";
	public static final String MICRO_RADIAN_ANGLE_LOWERCASE_STRING = "\u00b5rad";
	public static final String MICRO_RADIAN_MU_ANGLE_LOWERCASE_STRING = "\u03bcrad";
	public static final String MICRO_RADIAN_U_ANGLE_STRING = "uRad";
	public static final String MICRO_RADIAN_U_ANGLE_LOWERCASE_STRING = "urad";

	public static final String MICRON_STRING = "micron";
	public static final String MICRONS_STRING = "microns";
	public static final String MICRON_UM_STRING = "um";
	public static final String MICRON_MU_STRING = "\u03bcm";
	public static final String MICRON_SYMBOL_STRING = "\u00b5m";

	public static final String MICROSECOND_STRING = "\u00b5S";
	public static final String MICROSECOND_U_STRING = "uS";
	public static final String MICROSECOND_MU_STRING = "\u03bcS";
	public static final String MICROSECOND_LOWERCASE_STRING = "\u00b5s";
	public static final String MICROSECOND_U_LOWERCASE_STRING = "us";
	public static final String MICROSECOND_MU_LOWERCASE_STRING = "\u03bcs";

	public static final String ANG_STRING = "Ang";
	public static final String ANGSTROM_STRING = "Angstrom";
	public static final String ANGSTROM_SYMBOL = "\u00c5";
	public static final String ANGSTROM_SYMBOL_ALTERNATIVE = "\u212b";
	public static final String PER_ANGSTROM_STRING = "Per_Angstrom";

	public static final String CENTIGRADE_STRING = "centigrade";

	public static final String KILOELECTRONVOLT_STRING = "keV";
	public static final String GIGAELECTRONVOLT_STRING = "GeV";

	public static final String MICROAMPERE_STRING = "\u00b5A";
	public static final String MICROAMPERE_U_STRING = "uA";
	public static final String MICROAMPERE_MU_STRING = "\u03bcA";

	public static final String COUNT_STRING = "ct";
	public static final String COUNTS_STRING = "cts";
	public static final String KILOCOUNT_STRING = "kct";
	public static final String KILOCOUNTS_STRING = "kcts";
	public static final String KILOCOUNTS_UC_STRING = "Kcount";

	public static final String MICROLITRE_STRING = "\u00b5L";
	public static final String MICROLITRE_U_STRING = "uL";
	public static final String MICROLITRE_MU_STRING = "\u03bcL";

	/** An alternative unit name for one over ANGSTROM (alternative name <code>Per_Angstrom</code>). */
	public static final Unit<Vector> PER_ANGSTROM = (Unit<Vector>) ANGSTROM.inverse();

	/** A unit of counts or motor steps. */
	public static final Unit<Count> COUNTS = (Unit<Count>) METRE.inverse();

	/** A unit of counts or motor steps. */
	public static final Unit<Count> KILOCOUNTS = (Unit<Count>) METRE.inverse().times(1e3);

	/** Static initialiser: see explanation at top of class */
	static {
		final UnitFormat unitFormat = UnitFormat.getInstance();

		unitFormat.alias(DEGREE_ANGLE, DEG_ANGLE_STRING);
		unitFormat.alias(DEGREE_ANGLE, DEG_ANGLE_LOWERCASE_STRING);
		unitFormat.alias(DEGREE_ANGLE, DEGREES_ANGLE_STRING);
		unitFormat.alias(MILLI(DEGREE_ANGLE), MILLI_DEG_ANGLE_STRING);
		unitFormat.alias(MILLI(DEGREE_ANGLE), MILLI_DEG_ANGLE_LOWERCASE_STRING);
		unitFormat.alias(MICRO(DEGREE_ANGLE), MICRO_DEG_ANGLE_STRING);
		unitFormat.alias(MICRO(DEGREE_ANGLE), MICRO_DEG_MU_ANGLE_STRING);
		unitFormat.alias(MICRO(DEGREE_ANGLE), MICRO_DEG_U_ANGLE_STRING);

		unitFormat.alias(MILLI(RADIAN), MILLI_RADIAN_ANGLE_STRING);
		unitFormat.alias(MILLI(RADIAN), MILLI_RADIAN_ANGLE_LOWERCASE_STRING);
		unitFormat.alias(MICRO(RADIAN), MICRO_RADIAN_ANGLE_STRING);
		unitFormat.alias(MICRO(RADIAN), MICRO_RADIAN_MU_ANGLE_STRING);
		unitFormat.alias(MICRO(RADIAN), MICRO_RADIAN_ANGLE_LOWERCASE_STRING);
		unitFormat.alias(MICRO(RADIAN), MICRO_RADIAN_MU_ANGLE_LOWERCASE_STRING);
		unitFormat.alias(MICRO(RADIAN), MICRO_RADIAN_U_ANGLE_STRING);
		unitFormat.alias(MICRO(RADIAN), MICRO_RADIAN_U_ANGLE_LOWERCASE_STRING);

		unitFormat.alias(MICRO(METRE), MICRON_STRING);
		unitFormat.alias(MICRO(METRE), MICRONS_STRING);
		unitFormat.alias(MICRO(METRE), MICRON_UM_STRING);
		unitFormat.alias(MICRO(METRE), MICRON_MU_STRING);
		unitFormat.alias(MICRO(METRE), MICRON_SYMBOL_STRING);

		unitFormat.alias(MICRO(SECOND), MICROSECOND_STRING);
		unitFormat.alias(MICRO(SECOND), MICROSECOND_U_STRING);
		unitFormat.alias(MICRO(SECOND), MICROSECOND_MU_STRING);
		unitFormat.alias(MICRO(SECOND), MICROSECOND_LOWERCASE_STRING);
		unitFormat.alias(MICRO(SECOND), MICROSECOND_U_LOWERCASE_STRING);
		unitFormat.alias(MICRO(SECOND), MICROSECOND_MU_LOWERCASE_STRING);

		unitFormat.alias(ANGSTROM, ANG_STRING);
		unitFormat.alias(ANGSTROM, ANGSTROM_STRING);
		unitFormat.alias(ANGSTROM, ANGSTROM_SYMBOL);
		unitFormat.alias(ANGSTROM, ANGSTROM_SYMBOL_ALTERNATIVE);
		unitFormat.alias(PER_ANGSTROM, PER_ANGSTROM_STRING);

		unitFormat.alias(CELSIUS, CENTIGRADE_STRING);
		unitFormat.alias(KILO(ELECTRON_VOLT), KILOELECTRONVOLT_STRING);
		unitFormat.alias(GIGA(ELECTRON_VOLT), GIGAELECTRONVOLT_STRING);

		unitFormat.alias(MICRO(AMPERE), MICROAMPERE_U_STRING);
		unitFormat.alias(MICRO(AMPERE), MICROAMPERE_MU_STRING);
		unitFormat.alias(MICRO(AMPERE), MICROAMPERE_STRING);

		unitFormat.alias(COUNTS, COUNT_STRING);
		unitFormat.alias(COUNTS, COUNTS_STRING);
		unitFormat.alias(KILOCOUNTS, KILOCOUNT_STRING);
		unitFormat.alias(KILOCOUNTS, KILOCOUNTS_STRING);
		unitFormat.alias(KILOCOUNTS, KILOCOUNTS_UC_STRING);

		unitFormat.alias(MICRO(LITRE), MICROLITRE_STRING);
		unitFormat.alias(MICRO(LITRE), MICROLITRE_U_STRING);
		unitFormat.alias(MICRO(LITRE), MICROLITRE_MU_STRING);
	}

	/**
	 * For some units, the output of <code>Unit.toString()</code> is not very easy to read, for example the degree
	 * symbol or <code>(1/m)*1000.0</code> for kilocounts.<br>
	 * This table overrides string to be printed for these cases.
	 */
	private static final Map<Object, Object> unitStrings = ImmutableMap.builder()
			.put(DEGREE_ANGLE, DEG_ANGLE_LOWERCASE_STRING)
			.put(MILLI(DEGREE_ANGLE), MILLI_DEG_ANGLE_LOWERCASE_STRING)
			.put(MICRO(DEGREE_ANGLE), MICRO_DEG_ANGLE_STRING)
			.put(MILLI(RADIAN), MILLI_RADIAN_ANGLE_LOWERCASE_STRING)
			.put(MICRO(RADIAN), MICRO_RADIAN_ANGLE_LOWERCASE_STRING)
			.put(MICRO(METRE), MICRON_SYMBOL_STRING)
			.put(MICRO(SECOND), MICROSECOND_LOWERCASE_STRING)
			.put(ANGSTROM, ANGSTROM_STRING)
			.put(PER_ANGSTROM, PER_ANGSTROM_STRING)
			.put(CELSIUS, CENTIGRADE_STRING)
			.put(KILO(ELECTRON_VOLT), KILOELECTRONVOLT_STRING)
			.put(GIGA(ELECTRON_VOLT), GIGAELECTRONVOLT_STRING)
			.put(MICRO(AMPERE), MICROAMPERE_STRING)
			.put(COUNTS, COUNTS_STRING)
			.put(KILOCOUNTS, KILOCOUNTS_STRING)
			.put(MICRO(LITRE), MICROLITRE_STRING)
			.build();

	public static String getUnitString(Unit<? extends Quantity> unit) {
		return (String) unitStrings.getOrDefault(unit, unit.toString());
	}

	/** Default constructor (prevents this class from being instantiated). */
	private NonSIext() {
	}

	/** Static method to force class initialization. */
	public static void initializeClass() {
		// Just needs to exist to trigger the initialiser block when called
	}
}
