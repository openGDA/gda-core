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

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Duration;
import org.jscience.physics.quantities.ElectricCurrent;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Temperature;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;

import gda.jscience.physics.quantities.Count;
import gda.jscience.physics.quantities.Vector;

/**
 * <p>
 * This class contains units that are not part of the org.jscience.physics.units.NonSI, It is an extension to NonSI to
 * customise unit usage in GDA.
 */
public final class NonSIext {
	private static boolean IsInitialized;

	/**
	 * Default constructor (prevents this class from being instantiated).
	 */
	private NonSIext() {
	}

	// /////////
	// Angle //
	// /////////

	/**
	 * A unit of angle equal to <code>1/360</code> REVOLUTION (standard name <code>°</code>, alternative name
	 * defined here as <code>Deg</code>).
	 */
	public static final Unit<Angle> DEG_ANGLE = NonSI.DEGREE_ANGLE.alternate("Deg");

	/**
	 * A unit of angle equal to <code>1/360</code> REVOLUTION (standard name <code>°</code>, alternative name
	 * defined here as <code>Deg</code>).
	 */
	public static final Unit<Angle> DEG_ANGLE_LOWERCASE = NonSI.DEGREE_ANGLE.alternate("deg");
	public static final Unit<Angle> DEGREES_ANGLE = NonSI.DEGREE_ANGLE.alternate("degrees");

	/**
	 * A unit of angle equal to one thousandth of one degree (alternative name <code>mDeg</code>).
	 */
	public static final Unit<Angle> mDEG_ANGLE = DEG_ANGLE.times(1e-3).alternate("mDeg");

	/**
	 * A unit of angle equal to one thousandth of one degree (alternative name <code>mDeg</code>).
	 */
	public static final Unit<Angle> mDEG_ANGLE_LOWERCASE = DEG_ANGLE.times(1e-3).alternate("mdeg");

	/**
	 * A unit of angle equal to one thousandth of one milli-degree (alternative name <code>μDeg</code>).
	 */
	public static final Unit<Angle> μDEG_ANGLE = DEG_ANGLE.times(1e-6).alternate("μDeg");
	/**
	 * A unit of angle equal to one thousandth of one milli-degree (alternative name <code>μDeg</code>).
	 */
	public static final Unit<Angle> uDEG_ANGLE = DEG_ANGLE.times(1e-6).alternate("uDeg");
	/**
	 * A unit of plane angle equal to one thousandth of one RADIAN (alternative name <code>mRad</code>).
	 */
	public static final Unit<Angle> mRADIAN_ANGLE = SI.RADIAN.times(1e-3).alternate("mRad");
	/**
	 * A unit of plane angle equal to one thousandth of one RADIAN (alternative name <code>mRad</code>).
	 */
	public static final Unit<Angle> mRADIAN_ANGLE_LC = SI.RADIAN.times(1e-3).alternate("mrad");
	/**
	 * A unit of plane angle equal to one thousandth of one MILLI_RADIAN (alternative name <code>μRad</code>).
	 */
	public static final Unit<Angle> μRADIAN_ANGLE = SI.MICRO(SI.RADIAN).alternate("μRad");
	/**
	 * A unit of plane angle equal to one thousandth of one MILLI_RADIAN (alternative name <code>μRad</code>).
	 */
	public static final Unit<Angle> uRADIAN_ANGLE = SI.MICRO(SI.RADIAN).alternate("uRad");
	/**
	 * A unit of plane angle equal to one thousandth of one MILLI_RADIAN (alternative name <code>μRad</code>).
	 */
	public static final Unit<Angle> uRADIAN_ANGLE_LC = SI.MICRO(SI.RADIAN).alternate("urad");
	/**
	 * A unit of length equal to one millionth of one meter (alternative name <code>micron</code>).
	 */
	public static final Unit<Length> MICRON = SI.METER.times(1e-6).alternate("micron");
	/**
	 * A unit of length equal to one millionth of one meter (alternative name <code>micron</code>).
	 */
	public static final Unit<Length> MICRONS = SI.METER.times(1e-6).alternate("microns");
	/**
	 * A unit of length equal to one millionth of one meter (alternative name <code>um</code>).
	 */
	public static final Unit<Length> MICRON_UM = SI.METER.times(1e-6).alternate("um");
	/**
	 * A unit of length equal to one millionth of one second (alternative name <code>uS</code>).
	 */
	public static final Unit<Duration> MICROSECOND = SI.SECOND.times(1e-6).alternate("uS");
	/**
	 * A unit of length equal to one millionth of one second (alternative name <code>μS</code>).
	 */
	public static final Unit<Duration> MICROSECOND_MU = SI.SECOND.times(1e-6).alternate("μS");
	/**
	 * A unit of length equal to one millionth of one second (alternative name <code>us</code>).
	 */
	public static final Unit<Duration> MICROSECOND_LC = SI.SECOND.times(1e-6).alternate("us");
	/**
	 * A unit of length equal to one millionth of one second (alternative name <code>μs</code>).
	 */
	public static final Unit<Duration> MICROSECOND_MU_LC = SI.SECOND.times(1e-6).alternate("μs");
	/**
	 * An alternative unit name for ANGSTROM (alternative name <code>Ang</code>).
	 */
	public static final Unit<Length> ANG = SI.METER.times(1e-10).alternate("Ang");
	/**
	 *
	 */
	public static final Unit<Length> ANGSTROM = SI.METER.times(1e-10).alternate("Angstrom");

	/**
	 * An alternative unit name for one over ANGSTROM (alternative name <code>Per_Angstrom</code>).
	 */
	public static final Unit<Vector> PER_ANGSTROM = SI.METER.times(1e-10).inverse().alternate("Per_Angstrom");

	/**
	 * A unit of temperature equal to the Kelvin temeprature shifted by -273.15.
	 */
	public static final Unit<Temperature> CENTIGRADE = SI.KELVIN.plus(-273.15).alternate("centigrade");

	/**
	 * keV is a useful unit for DCM energies.
	 */
	public static final Unit<Energy> KILOELECTRONVOLT = NonSI.ELECTRON_VOLT.times(1e3).alternate("keV");

	/**
	 * GeV is a useful unit for machine energies.
	 */
	public static final Unit<Energy> GIGAELECTRONVOLT = NonSI.ELECTRON_VOLT.times(1e9).alternate("GeV");

	/**
	 * microAmp (uA)
	 */
	public static final Unit<ElectricCurrent> uAMPERE = SI.AMPERE.times(1e-6).alternate("uA");
	/**
	 * microAmp (μA)
	 */
	public static final Unit<ElectricCurrent> MICROAMPERE = SI.AMPERE.times(1e-6).alternate("μA");


	/**
	 * A unit of counts or motor steps.
	 */
	public static final Unit<Count> COUNT = SI.METER.inverse().alternate("cts");

	/**
	 * A unit of counts or motor steps.
	 */
	public static final Unit<Count> KILOCOUNT = NonSIext.COUNT.times(1e3).alternate("kcts");


	/**
	 * Static method to force class initialization.
	 */
	public static void initializeClass() {
		if (!NonSIext.IsInitialized) {
			// Performs initialization only once.
			NonSIext.IsInitialized = true;
			NonSIext.Volatile = Vector.ZERO;
			NonSIext.Volatile = Count.ZERO;
			ANG.alias("A*"); // add an alias symbol to support EPICS unit string for Angstrom
		}
	}

	static volatile Object Volatile;
}
