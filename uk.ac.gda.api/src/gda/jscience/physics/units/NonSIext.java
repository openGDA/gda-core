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

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import gda.jscience.physics.quantities.Count;
import gda.jscience.physics.quantities.Vector;

/**
 * This class contains units that are not part of the {@link NonSI} set<br>
 * It is an extension to NonSI to customise unit usage in GDA.
 * <p>
 * In jscience 4, you can no longer create alternate forms non-standard units, so this class now just creates aliases.
 * Thus, for example {@link #MICRON} and {@link #MICRON_UM} refer to the same underlying unit and will both be displayed
 * as <code>μm</code>
 * <p>
 * The <code>Unit</code> objects are set in a static initialiser. This is necessary because we need to control the
 * order: the aliases must be defined to JScience before being used to create the objects.
 */
@SuppressWarnings("unchecked")
public final class NonSIext {
	public static final String DEG_ANGLE_STRING = "Deg";
	public static final String DEG_ANGLE_LOWERCASE_STRING = "deg";
	public static final String DEGREES_ANGLE_STRING = "degrees";
	public static final String DEG_ANGLE_SYMBOL = "°";
	public static final String mDEG_ANGLE_STRING = "mDeg";
	public static final String mDEG_ANGLE_LOWERCASE_STRING = "mdeg";
	public static final String μDEG_ANGLE_STRING = "μDeg";
	public static final String uDEG_ANGLE_STRING = "uDeg";

	public static final String mRADIAN_ANGLE_STRING = "mRad";
	public static final String mRADIAN_ANGLE_LC_STRING = "mrad";
	public static final String μRADIAN_ANGLE_STRING = "μRad";
	public static final String uRADIAN_ANGLE_STRING = "uRad";
	public static final String uRADIAN_ANGLE_LC_STRING = "urad";

	public static final String MICRON_STRING = "micron";
	public static final String MICRONS_STRING = "microns";
	public static final String MICRON_UM_STRING = "um";

	public static final String MICROSECOND_STRING = "uS";
	public static final String MICROSECOND_MU_STRING = "μS";
	public static final String MICROSECOND_LC_STRING = "us";
	public static final String MICROSECOND_MU_LC_STRING = "μs";

	public static final String ANG_STRING = "Ang";
	public static final String ANGSTROM_STRING = "Angstrom";
	public static final String ANGSTROM_SYMBOL = "Å";
	public static final String PER_ANGSTROM_STRING = "Per_Angstrom";

	public static final String CENTIGRADE_STRING = "centigrade";

	public static final String KILOELECTRONVOLT_STRING = "keV";
	public static final String GIGAELECTRONVOLT_STRING = "GeV";

	public static final String uAMPERE_STRING = "uA";
	public static final String MICROAMPERE_STRING = "μA";

	public static final String COUNT_STRING = "cts";
	public static final String KILOCOUNT_STRING = "kcts";

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
	public static final Unit<Angle> mDEG_ANGLE;

	/** A unit of angle equal to one thousandth of one degree (alternative name <code>mDeg</code>). */
	public static final Unit<Angle> mDEG_ANGLE_LOWERCASE;

	/** A unit of angle equal to one thousandth of one milli-degree (alternative name <code>μDeg</code>). */
	public static final Unit<Angle> μDEG_ANGLE;

	/** A unit of angle equal to one thousandth of one milli-degree (alternative name <code>μDeg</code>). */
	public static final Unit<Angle> uDEG_ANGLE;

	/** A unit of plane angle equal to one thousandth of one RADIAN (alternative name <code>mRad</code>). */
	public static final Unit<Angle> mRADIAN_ANGLE;

	/** A unit of plane angle equal to one thousandth of one RADIAN (alternative name <code>mRad</code>). */
	public static final Unit<Angle> mRADIAN_ANGLE_LC;

	/** A unit of plane angle equal to one thousandth of one MILLI_RADIAN (alternative name <code>μRad</code>). */
	public static final Unit<Angle> μRADIAN_ANGLE;

	/** A unit of plane angle equal to one thousandth of one MILLI_RADIAN (alternative name <code>μRad</code>). */
	public static final Unit<Angle> uRADIAN_ANGLE;

	/** A unit of plane angle equal to one thousandth of one MILLI_RADIAN (alternative name <code>μRad</code>). */
	public static final Unit<Angle> uRADIAN_ANGLE_LC;

	/** A unit of length equal to one millionth of one meter (alternative name <code>micron</code>). */
	public static final Unit<Length> MICRON;

	/** A unit of length equal to one millionth of one meter (alternative name <code>micron</code>). */
	public static final Unit<Length> MICRONS;

	/** A unit of length equal to one millionth of one meter (alternative name <code>um</code>). */
	public static final Unit<Length> MICRON_UM;

	/** A unit of length equal to one millionth of one second (alternative name <code>uS</code>). */
	public static final Unit<Duration> MICROSECOND;

	/** A unit of length equal to one millionth of one second (alternative name <code>μS</code>). */
	public static final Unit<Duration> MICROSECOND_MU;

	/** A unit of length equal to one millionth of one second (alternative name <code>us</code>). */
	public static final Unit<Duration> MICROSECOND_LC;

	/** A unit of length equal to one millionth of one second (alternative name <code>μs</code>). */
	public static final Unit<Duration> MICROSECOND_MU_LC;

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
	public static final Unit<ElectricCurrent> uAMPERE;

	/** microAmp (μA) */
	public static final Unit<ElectricCurrent> MICROAMPERE;

	/** A unit of counts or motor steps. */
	public static final Unit<Count> COUNT;

	/** A unit of counts or motor steps. */
	public static final Unit<Count> KILOCOUNT;

	/** Static initialiser: see explanation at top of class */
	static {
		final UnitFormat unitFormat = UnitFormat.getInstance();

		unitFormat.alias(NonSI.DEGREE_ANGLE, DEG_ANGLE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE, DEG_ANGLE_LOWERCASE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE, DEGREES_ANGLE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE.times(1e-3), mDEG_ANGLE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE.times(1e-3), mDEG_ANGLE_LOWERCASE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE.times(1e-6), μDEG_ANGLE_STRING);
		unitFormat.alias(NonSI.DEGREE_ANGLE.times(1e-6), uDEG_ANGLE_STRING);

		unitFormat.alias(SI.RADIAN.times(1e-3), mRADIAN_ANGLE_STRING);
		unitFormat.alias(SI.RADIAN.times(1e-3), mRADIAN_ANGLE_LC_STRING);
		unitFormat.alias(SI.MICRO(SI.RADIAN), μRADIAN_ANGLE_STRING);
		unitFormat.alias(SI.MICRO(SI.RADIAN), uRADIAN_ANGLE_STRING);
		unitFormat.alias(SI.MICRO(SI.RADIAN), uRADIAN_ANGLE_LC_STRING);

		unitFormat.alias(SI.METER.times(1e-6), MICRON_STRING);
		unitFormat.alias(SI.METER.times(1e-6), MICRONS_STRING);
		unitFormat.alias(SI.METER.times(1e-6), MICRON_UM_STRING);

		unitFormat.alias(SI.SECOND.times(1e-6), MICROSECOND_STRING);
		unitFormat.alias(SI.SECOND.times(1e-6), MICROSECOND_MU_STRING);
		unitFormat.alias(SI.SECOND.times(1e-6), MICROSECOND_LC_STRING);
		unitFormat.alias(SI.SECOND.times(1e-6), MICROSECOND_MU_LC_STRING);

		unitFormat.alias(SI.METER.times(1e-10), ANG_STRING);
		unitFormat.alias(SI.METER.times(1e-10), ANGSTROM_STRING);
		unitFormat.alias(SI.METER.times(1e-10).inverse(), PER_ANGSTROM_STRING);

		unitFormat.alias(SI.KELVIN.plus(-273.15), CENTIGRADE_STRING);
		unitFormat.alias(NonSI.ELECTRON_VOLT.times(1e3), KILOELECTRONVOLT_STRING);
		unitFormat.alias(NonSI.ELECTRON_VOLT.times(1e9), GIGAELECTRONVOLT_STRING);

		unitFormat.alias(SI.AMPERE.times(1e-6), uAMPERE_STRING);
		unitFormat.alias(SI.AMPERE.times(1e-6), MICROAMPERE_STRING);

		unitFormat.alias(SI.METER.inverse(), COUNT_STRING);
		unitFormat.alias(SI.METER.inverse().times(1e3), KILOCOUNT_STRING);

		DEG_ANGLE = (Unit<Angle>) Unit.valueOf(DEG_ANGLE_STRING);
		DEG_ANGLE_LOWERCASE = (Unit<Angle>) Unit.valueOf(DEG_ANGLE_LOWERCASE_STRING);
		DEGREES_ANGLE = (Unit<Angle>) Unit.valueOf(DEGREES_ANGLE_STRING);
		mDEG_ANGLE = (Unit<Angle>) Unit.valueOf(mDEG_ANGLE_STRING);
		mDEG_ANGLE_LOWERCASE = (Unit<Angle>) Unit.valueOf(mDEG_ANGLE_LOWERCASE_STRING);
		μDEG_ANGLE = (Unit<Angle>) Unit.valueOf(μDEG_ANGLE_STRING);
		uDEG_ANGLE = (Unit<Angle>) Unit.valueOf(uDEG_ANGLE_STRING);

		mRADIAN_ANGLE = (Unit<Angle>) Unit.valueOf(mRADIAN_ANGLE_STRING);
		mRADIAN_ANGLE_LC = (Unit<Angle>) Unit.valueOf(mRADIAN_ANGLE_LC_STRING);
		μRADIAN_ANGLE = (Unit<Angle>) Unit.valueOf(μRADIAN_ANGLE_STRING);
		uRADIAN_ANGLE = (Unit<Angle>) Unit.valueOf(uRADIAN_ANGLE_STRING);
		uRADIAN_ANGLE_LC = (Unit<Angle>) Unit.valueOf(uRADIAN_ANGLE_LC_STRING);

		MICRON = (Unit<Length>) Unit.valueOf(MICRON_STRING);
		MICRONS = (Unit<Length>) Unit.valueOf(MICRONS_STRING);
		MICRON_UM = (Unit<Length>) Unit.valueOf(MICRON_UM_STRING);

		MICROSECOND = (Unit<Duration>) Unit.valueOf(MICROSECOND_STRING);
		MICROSECOND_MU = (Unit<Duration>) Unit.valueOf(MICROSECOND_MU_STRING);
		MICROSECOND_LC = (Unit<Duration>) Unit.valueOf(MICROSECOND_LC_STRING);
		MICROSECOND_MU_LC = (Unit<Duration>) Unit.valueOf(MICROSECOND_MU_LC_STRING);

		ANG = (Unit<Length>) Unit.valueOf(ANG_STRING);
		ANGSTROM = (Unit<Length>) Unit.valueOf(ANGSTROM_STRING);
		PER_ANGSTROM = (Unit<Vector>) Unit.valueOf(PER_ANGSTROM_STRING);

		CENTIGRADE = (Unit<Temperature>) Unit.valueOf(CENTIGRADE_STRING);
		KILOELECTRONVOLT = (Unit<Energy>) Unit.valueOf(KILOELECTRONVOLT_STRING);
		GIGAELECTRONVOLT = (Unit<Energy>) Unit.valueOf(GIGAELECTRONVOLT_STRING);

		uAMPERE = (Unit<ElectricCurrent>) Unit.valueOf(uAMPERE_STRING);
		MICROAMPERE = (Unit<ElectricCurrent>) Unit.valueOf(MICROAMPERE_STRING);

		COUNT = (Unit<Count>) Unit.valueOf(COUNT_STRING);
		KILOCOUNT = (Unit<Count>) Unit.valueOf(KILOCOUNT_STRING);
}

	/** Default constructor (prevents this class from being instantiated). */
	private NonSIext() {
	}

	/** Static method to force class initialization. */
	public static void initializeClass() {
		// Just needs to exist to trigger the initialiser block when called
	}
}
