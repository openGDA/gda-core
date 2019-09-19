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

import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.quantity.Temperature;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
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
 * Thus, for example {@link #MICRON} and {@link #MICRON_UM} refer to the same underlying unit and will both be displayed
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

	/**
	 * A unit of angle equal to <code>1/360</code> REVOLUTION (standard name <code>°</code>, alternative name defined
	 * here as <code>Deg</code>).
	 */
	public static final Unit<Angle> DEG_ANGLE;

	/**
	 * A unit of angle equal to <code>1/360</code> REVOLUTION (standard name <code>°</code>, alternative name defined
	 * here as <code>deg</code>).
	 */
	public static final Unit<Angle> DEG_ANGLE_LOWERCASE;
	public static final Unit<Angle> DEGREES_ANGLE;

	/** A unit of angle equal to one thousandth of one degree (alternative name <code>mDeg</code>). */
	public static final Unit<Angle> MILLI_DEG_ANGLE;

	/** A unit of angle equal to one thousandth of one degree (alternative name <code>mDeg</code>). */
	public static final Unit<Angle> MILLI_DEG_ANGLE_LOWERCASE;

	/** A unit of angle equal to one thousandth of one milli-degree (alternative name <code>µDeg</code>). */
	public static final Unit<Angle> MICRO_DEG_ANGLE;

	/** A unit of angle equal to one thousandth of one milli-degree (alternative name <code>µDeg</code>). */
	public static final Unit<Angle> MICRO_DEG_U_ANGLE;

	/** A unit of plane angle equal to one thousandth of one RADIAN (alternative name <code>mRad</code>). */
	public static final Unit<Angle> MILLI_RADIAN_ANGLE;

	/** A unit of plane angle equal to one thousandth of one RADIAN (alternative name <code>mRad</code>). */
	public static final Unit<Angle> MILLI_RADIAN_ANGLE_LOWERCASE;

	/** A unit of plane angle equal to one thousandth of one MILLI_RADIAN (alternative name <code>µRad</code>). */
	public static final Unit<Angle> MICRO_RADIAN_ANGLE;

	/** A unit of plane angle equal to one thousandth of one MILLI_RADIAN (alternative name <code>µRad</code>). */
	public static final Unit<Angle> MICRO_RADIAN_U_ANGLE;

	/** A unit of plane angle equal to one thousandth of one MILLI_RADIAN (alternative name <code>µRad</code>). */
	public static final Unit<Angle> MICRO_RADIAN_U_ANGLE_LOWERCASE;

	/** A unit of length equal to one millionth of one meter (alternative name <code>micron</code>). */
	public static final Unit<Length> MICRON;

	/** A unit of length equal to one millionth of one meter (alternative name <code>micron</code>). */
	public static final Unit<Length> MICRONS;

	/** A unit of length equal to one millionth of one meter (alternative name <code>um</code>). */
	public static final Unit<Length> MICRON_UM;

	/** A unit of length equal to one millionth of one second (alternative name <code>uS</code>). */
	public static final Unit<Duration> MICROSECOND;

	/** A unit of length equal to one millionth of one second (alternative name <code>µS</code>). */
	public static final Unit<Duration> MICROSECOND_MU;

	/** A unit of length equal to one millionth of one second (alternative name <code>us</code>). */
	public static final Unit<Duration> MICROSECOND_LOWERCASE;

	/** A unit of length equal to one millionth of one second (alternative name <code>µs</code>). */
	public static final Unit<Duration> MICROSECOND_MU_LOWERCASE;

	/** An alternative unit name for ANGSTROM (alternative name <code>Ang</code>). */
	public static final Unit<Length> ANG;

	/** An alternative unit name for ANGSTROM (alternative name <code>Angstrom</code>). */
	public static final Unit<Length> ANGSTROM;

	/** An alternative unit name for one over ANGSTROM (alternative name <code>Per_Angstrom</code>). */
	public static final Unit<Vector> PER_ANGSTROM;

	/** A unit of temperature equal to the Kelvin temeprature shifted by -273.15. */
	public static final Unit<Temperature> CENTIGRADE;

	/** keV is a useful unit for DCM energies. */
	public static final Unit<Energy> KILOELECTRONVOLT;

	/** GeV is a useful unit for machine energies. */
	public static final Unit<Energy> GIGAELECTRONVOLT;

	/** microAmp (uA) */
	public static final Unit<ElectricCurrent> MICRO_AMPERE_U;

	/** microAmp (µA) */
	public static final Unit<ElectricCurrent> MICRO_AMPERE;

	/** A unit of counts or motor steps. */
	public static final Unit<Count> COUNTS;

	/** A unit of counts or motor steps. */
	public static final Unit<Count> KILOCOUNTS;

	/** Static initialiser: see explanation at top of class */
	static {
		final UnitFormat unitFormat = UnitFormat.getInstance();

		unitFormat.alias(NonSI.DEGREE_ANGLE, DEG_ANGLE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE, DEG_ANGLE_LOWERCASE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE, DEGREES_ANGLE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE.times(1e-3), MILLI_DEG_ANGLE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE.times(1e-3), MILLI_DEG_ANGLE_LOWERCASE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE.times(1e-6), MICRO_DEG_ANGLE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE.times(1e-6), MICRO_DEG_MU_ANGLE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE.times(1e-6), MICRO_DEG_U_ANGLE_STRING);

		unitFormat.alias(SI.RADIAN.times(1e-3), MILLI_RADIAN_ANGLE_STRING);
		unitFormat.alias(SI.RADIAN.times(1e-3), MILLI_RADIAN_ANGLE_LOWERCASE_STRING);
		unitFormat.alias(SI.MICRO(SI.RADIAN), MICRO_RADIAN_ANGLE_STRING);
		unitFormat.alias(SI.MICRO(SI.RADIAN), MICRO_RADIAN_MU_ANGLE_STRING);
		unitFormat.alias(SI.MICRO(SI.RADIAN), MICRO_RADIAN_ANGLE_LOWERCASE_STRING);
		unitFormat.alias(SI.MICRO(SI.RADIAN), MICRO_RADIAN_MU_ANGLE_LOWERCASE_STRING);
		unitFormat.alias(SI.MICRO(SI.RADIAN), MICRO_RADIAN_U_ANGLE_STRING);
		unitFormat.alias(SI.MICRO(SI.RADIAN), MICRO_RADIAN_U_ANGLE_LOWERCASE_STRING);

		unitFormat.alias(SI.METER.times(1e-6), MICRON_STRING);
		unitFormat.alias(SI.METER.times(1e-6), MICRONS_STRING);
		unitFormat.alias(SI.METER.times(1e-6), MICRON_UM_STRING);
		unitFormat.alias(SI.METER.times(1e-6), MICRON_MU_STRING);
		unitFormat.alias(SI.METER.times(1e-6), MICRON_SYMBOL_STRING);

		unitFormat.alias(SI.SECOND.times(1e-6), MICROSECOND_STRING);
		unitFormat.alias(SI.SECOND.times(1e-6), MICROSECOND_U_STRING);
		unitFormat.alias(SI.SECOND.times(1e-6), MICROSECOND_MU_STRING);
		unitFormat.alias(SI.SECOND.times(1e-6), MICROSECOND_LOWERCASE_STRING);
		unitFormat.alias(SI.SECOND.times(1e-6), MICROSECOND_U_LOWERCASE_STRING);
		unitFormat.alias(SI.SECOND.times(1e-6), MICROSECOND_MU_LOWERCASE_STRING);

		unitFormat.alias(SI.METER.times(1e-10), ANG_STRING);
		unitFormat.alias(SI.METER.times(1e-10), ANGSTROM_STRING);
		unitFormat.alias(SI.METER.times(1e-10), ANGSTROM_SYMBOL);
		unitFormat.alias(SI.METER.times(1e-10), ANGSTROM_SYMBOL_ALTERNATIVE);
		unitFormat.alias(SI.METER.times(1e-10).inverse(), PER_ANGSTROM_STRING);

		unitFormat.alias(SI.KELVIN.plus(-273.15), CENTIGRADE_STRING);
		unitFormat.alias(NonSI.ELECTRON_VOLT.times(1e3), KILOELECTRONVOLT_STRING);
		unitFormat.alias(NonSI.ELECTRON_VOLT.times(1e9), GIGAELECTRONVOLT_STRING);

		unitFormat.alias(SI.AMPERE.times(1e-6), MICROAMPERE_U_STRING);
		unitFormat.alias(SI.AMPERE.times(1e-6), MICROAMPERE_MU_STRING);
		unitFormat.alias(SI.AMPERE.times(1e-6), MICROAMPERE_STRING);

		unitFormat.alias(SI.METER.inverse(), COUNT_STRING);
		unitFormat.alias(SI.METER.inverse(), COUNTS_STRING);
		unitFormat.alias(SI.METER.inverse().times(1e3), KILOCOUNT_STRING);
		unitFormat.alias(SI.METER.inverse().times(1e3), KILOCOUNTS_STRING);
		unitFormat.alias(SI.METER.inverse().times(1e3), KILOCOUNTS_UC_STRING);

		unitFormat.alias(SI.MICRO(NonSI.LITRE), MICROLITRE_STRING);
		unitFormat.alias(SI.MICRO(NonSI.LITRE), MICROLITRE_U_STRING);
		unitFormat.alias(SI.MICRO(NonSI.LITRE), MICROLITRE_MU_STRING);

		DEG_ANGLE = (Unit<Angle>) Unit.valueOf(DEG_ANGLE_STRING);
		DEG_ANGLE_LOWERCASE = (Unit<Angle>) Unit.valueOf(DEG_ANGLE_LOWERCASE_STRING);
		DEGREES_ANGLE = (Unit<Angle>) Unit.valueOf(DEGREES_ANGLE_STRING);
		MILLI_DEG_ANGLE = (Unit<Angle>) Unit.valueOf(MILLI_DEG_ANGLE_STRING);
		MILLI_DEG_ANGLE_LOWERCASE = (Unit<Angle>) Unit.valueOf(MILLI_DEG_ANGLE_LOWERCASE_STRING);
		MICRO_DEG_ANGLE = (Unit<Angle>) Unit.valueOf(MICRO_DEG_MU_ANGLE_STRING);
		MICRO_DEG_U_ANGLE = (Unit<Angle>) Unit.valueOf(MICRO_DEG_U_ANGLE_STRING);

		MILLI_RADIAN_ANGLE = (Unit<Angle>) Unit.valueOf(MILLI_RADIAN_ANGLE_STRING);
		MILLI_RADIAN_ANGLE_LOWERCASE = (Unit<Angle>) Unit.valueOf(MILLI_RADIAN_ANGLE_LOWERCASE_STRING);
		MICRO_RADIAN_ANGLE = (Unit<Angle>) Unit.valueOf(MICRO_RADIAN_ANGLE_STRING);
		MICRO_RADIAN_U_ANGLE = (Unit<Angle>) Unit.valueOf(MICRO_RADIAN_U_ANGLE_STRING);
		MICRO_RADIAN_U_ANGLE_LOWERCASE = (Unit<Angle>) Unit.valueOf(MICRO_RADIAN_U_ANGLE_LOWERCASE_STRING);

		MICRON = (Unit<Length>) Unit.valueOf(MICRON_STRING);
		MICRONS = (Unit<Length>) Unit.valueOf(MICRONS_STRING);
		MICRON_UM = (Unit<Length>) Unit.valueOf(MICRON_UM_STRING);

		MICROSECOND = (Unit<Duration>) Unit.valueOf(MICROSECOND_STRING);
		MICROSECOND_MU = (Unit<Duration>) Unit.valueOf(MICROSECOND_MU_STRING);
		MICROSECOND_LOWERCASE = (Unit<Duration>) Unit.valueOf(MICROSECOND_LOWERCASE_STRING);
		MICROSECOND_MU_LOWERCASE = (Unit<Duration>) Unit.valueOf(MICROSECOND_MU_LOWERCASE_STRING);

		ANG = (Unit<Length>) Unit.valueOf(ANG_STRING);
		ANGSTROM = (Unit<Length>) Unit.valueOf(ANGSTROM_STRING);
		PER_ANGSTROM = (Unit<Vector>) Unit.valueOf(PER_ANGSTROM_STRING);

		CENTIGRADE = (Unit<Temperature>) Unit.valueOf(CENTIGRADE_STRING);
		KILOELECTRONVOLT = (Unit<Energy>) Unit.valueOf(KILOELECTRONVOLT_STRING);
		GIGAELECTRONVOLT = (Unit<Energy>) Unit.valueOf(GIGAELECTRONVOLT_STRING);

		MICRO_AMPERE_U = (Unit<ElectricCurrent>) Unit.valueOf(MICROAMPERE_U_STRING);
		MICRO_AMPERE = (Unit<ElectricCurrent>) Unit.valueOf(MICROAMPERE_STRING);

		COUNTS = (Unit<Count>) Unit.valueOf(COUNTS_STRING);
		KILOCOUNTS = (Unit<Count>) Unit.valueOf(KILOCOUNTS_STRING);
	}

	/**
	 * For some units, the output of <code>Unit.toString()</code> is not very easy to read, for example the degree
	 * symbol or <code>(1/m)*1000.0</code> for kilocounts.<br>
	 * This table overrides string to be printed for these cases.
	 * <p>
	 * Because of the aliasing defined above, <code>DEG_ANGLE</code>, <code>DEG_ANGLE_LOWERCASE</code> and
	 * <code>DEGREES_ANGLE</code> are all assigned to the same object, so only one needs to be put into this map.<br>
	 * The same applies to the other aliased units.
	 */
	private static final Map<Object, Object> unitStrings = ImmutableMap.builder()
			.put(DEG_ANGLE, DEG_ANGLE_LOWERCASE_STRING)
			.put(MILLI_DEG_ANGLE, MILLI_DEG_ANGLE_LOWERCASE_STRING)
			.put(MICRO_DEG_ANGLE, MICRO_DEG_ANGLE_STRING)
			.put(MILLI_RADIAN_ANGLE, MILLI_RADIAN_ANGLE_LOWERCASE_STRING)
			.put(MICRO_RADIAN_ANGLE, MICRO_RADIAN_ANGLE_LOWERCASE_STRING)
			.put(MICRON, MICRON_SYMBOL_STRING)
			.put(MICROSECOND, MICROSECOND_LOWERCASE_STRING)
			.put(ANGSTROM, ANGSTROM_STRING)
			.put(PER_ANGSTROM, PER_ANGSTROM_STRING)
			.put(CENTIGRADE, CENTIGRADE_STRING)
			.put(KILOELECTRONVOLT, KILOELECTRONVOLT_STRING)
			.put(GIGAELECTRONVOLT, GIGAELECTRONVOLT_STRING)
			.put(MICRO_AMPERE, MICROAMPERE_STRING)
			.put(COUNTS, COUNTS_STRING)
			.put(KILOCOUNTS, KILOCOUNTS_STRING)
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
