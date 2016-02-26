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

package gda.util;

import gda.jscience.physics.quantities.BraggAngle;
import gda.jscience.physics.quantities.PhotonEnergy;
import gda.jscience.physics.quantities.Vector;
import gda.jscience.physics.quantities.WaveVector;
import gda.jscience.physics.quantities.Wavelength;
import gda.jscience.physics.units.NonSIext;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A class to convert between the various units used in XAFS
 */

public class Converter {
	private static final Logger logger = LoggerFactory.getLogger(Converter.class);

	/**
	 *
	 */
	public static final String EV = "eV";

	/**
	 *
	 */
	public static final String KEV = "keV";

	/**
	 *
	 */
	public static final String MDEG = "mDeg";

	/**
	 *
	 */
	public static final String ANGSTROM = "\u00c5";

	/**
	 *
	 */
	public static final String PERANGSTROM = "\u00c5\u207b\u00b9";

	private static Length twoD = null;

	private static Energy edgeEnergy = null;

	private static Object[] allowedUnits = { EV, KEV, MDEG, ANGSTROM };

	/**
	 * @return allowedUnits
	 */
	public static Object[] getAllowedUnits() {
		return allowedUnits;
	}

	/**
	 * Default constructor prevents this class from being instantiated as only static methods exist.
	 */
	private Converter() {
	}

	/**
	 * Set the built in mono 2D spacing
	 *
	 * @param value
	 *            new value in Angstroms
	 */
	public static void setTwoD(double value) {
		twoD = Quantity.valueOf(value, NonSI.ANGSTROM);
	}

	/**
	 * Set the built in edge energy
	 *
	 * @param value
	 *            new value in keV
	 */
	public static void setEdgeEnergy(double value) {
		edgeEnergy = Quantity.valueOf(value, SI.KILO(NonSI.ELECTRON_VOLT));
	}

	/**
	 * Converts a value using previously specified values for the edge energy and twoD.
	 *
	 * @param value
	 *            the input value
	 * @param convertFromUnit
	 *            units to convert from
	 * @param convertToUnit
	 *            units to convert to
	 * @return the converted value
	 */
	public static double convert(double value, String convertFromUnit, String convertToUnit) {
		/* Use the built in edgeEnergy and twoD Quantities to call the real */
		/* converting method. */
		return convert(value, convertFromUnit, convertToUnit, edgeEnergy, twoD);
	}

	/**
	 * Converts a value using temporary values for the edge energy and twoD.
	 *
	 * @param value
	 *            the input value
	 * @param convertFromUnit
	 *            units to convert from
	 * @param convertToUnit
	 *            units to convert to
	 * @param edgeEnergy
	 *            the edge energy in keV
	 * @param twoD
	 *            twoD for the mono in Angstroms
	 * @return the converted value
	 */
	public static double convert(double value, String convertFromUnit, String convertToUnit, double edgeEnergy,
			double twoD) {
		/* Create Energy and Length Quantities from the temporary values and */
		/* call the real converting method. */
		return convert(value, convertFromUnit, convertToUnit, Quantity.valueOf(edgeEnergy, SI.KILO(NonSI.ELECTRON_VOLT)),
				Quantity.valueOf(twoD, NonSI.ANGSTROM));
	}

	/**
	 * Converts a value
	 *
	 * @param value
	 *            the input value
	 * @param convertFromUnit
	 *            units to convert from
	 * @param convertToUnit
	 *            units to convert to
	 * @param edgeEnergy
	 *            an Energy representing the edge energy in keV
	 * @param twoD
	 *            a Length representing twoD for the mono in Angstroms
	 * @return the converted value
	 */
	private static double convert(double value, String convertFromUnit, String convertToUnit, Energy edgeEnergy,
			Length twoD) {
		if (convertFromUnit.equals(EV)) {
			Energy energy = Quantity.valueOf(value, NonSI.ELECTRON_VOLT);

			if (convertToUnit.equals(KEV)) {
				value = value / 1000.0;
			} else if (convertToUnit.equals(MDEG)) {
				Angle angle = BraggAngle.braggAngleOf(energy, twoD);
				value = angle.to(NonSIext.mDEG_ANGLE).getAmount();
			} else if (convertToUnit.equals(ANGSTROM)) {
				Length length = Wavelength.wavelengthOf(energy);
				value = length.to(NonSI.ANGSTROM).getAmount();
			} else if (convertToUnit.equals(PERANGSTROM)) {
				Vector waveVector = WaveVector.waveVectorOf(edgeEnergy, energy);
				value = waveVector.to(NonSIext.PER_ANGSTROM).getAmount();
			}
		} else if (convertFromUnit.equals(KEV)) {
			Energy energy = Quantity.valueOf(value, SI.KILO(NonSI.ELECTRON_VOLT));
			logger.debug("energy is " + energy);

			if (convertToUnit.equals(EV))
				value = value * 1000.0;
			else if (convertToUnit.equals(MDEG)) {
				logger.debug("twoD is " + twoD);
				Angle angle = BraggAngle.braggAngleOf(energy, twoD);
				logger.debug("angle is " + angle);
				value = angle.to(NonSIext.mDEG_ANGLE).getAmount();
			} else if (convertToUnit.equals(ANGSTROM)) {
				Length length = Wavelength.wavelengthOf(energy);
				value = length.to(NonSI.ANGSTROM).getAmount();
			} else if (convertToUnit.equals(PERANGSTROM)) {
				Vector waveVector = WaveVector.waveVectorOf(edgeEnergy, energy);
				value = waveVector.to(NonSIext.PER_ANGSTROM).getAmount();
			}
		} else if (convertFromUnit.equals(MDEG)) {
			Angle angle = Quantity.valueOf(value, NonSIext.mDEG_ANGLE);

			if (convertToUnit.equals(EV)) {
				Energy energy = PhotonEnergy.photonEnergyOf(angle, twoD);
				value = energy.to(NonSI.ELECTRON_VOLT).getAmount();
			} else if (convertToUnit.equals(KEV)) {
				Energy energy = PhotonEnergy.photonEnergyOf(angle, twoD);
				value = energy.to(SI.KILO(NonSI.ELECTRON_VOLT)).getAmount();
			} else if (convertToUnit.equals(ANGSTROM)) {
				Length length = Wavelength.wavelengthOf(angle, twoD);
				value = length.to(NonSI.ANGSTROM).getAmount();
			} else if (convertToUnit.equals(PERANGSTROM)) {
				Energy energy = PhotonEnergy.photonEnergyOf(angle, twoD);
				Vector waveVector = WaveVector.waveVectorOf(edgeEnergy, energy);
				value = waveVector.to(NonSIext.PER_ANGSTROM).getAmount();
			}
		} else if (convertFromUnit.equals(ANGSTROM)) {
			Length length = Quantity.valueOf(value, NonSI.ANGSTROM);

			if (convertToUnit.equals(EV)) {
				Energy energy = PhotonEnergy.photonEnergyOf(length);
				value = energy.to(NonSI.ELECTRON_VOLT).getAmount();
			} else if (convertToUnit.equals(KEV)) {
				Energy energy = PhotonEnergy.photonEnergyOf(length);
				value = energy.to(SI.KILO(NonSI.ELECTRON_VOLT)).getAmount();
			} else if (convertToUnit.equals(MDEG)) {
				Angle angle = BraggAngle.braggAngleOf(length, twoD);
				value = angle.to(NonSIext.mDEG_ANGLE).getAmount();
			} else if (convertToUnit.equals(PERANGSTROM)) {
				Energy energy = PhotonEnergy.photonEnergyOf(length);
				Vector waveVector = WaveVector.waveVectorOf(edgeEnergy, energy);
				value = waveVector.to(NonSIext.PER_ANGSTROM).getAmount();
			}
		} else if (convertFromUnit.equals(PERANGSTROM)) {
			Quantity v = Quantity.valueOf(value, NonSIext.PER_ANGSTROM);
			Energy energy = PhotonEnergy.photonEnergyOf(edgeEnergy, v);
			if (convertToUnit.equals(EV)) {
				value = energy.to(NonSI.ELECTRON_VOLT).getAmount();
			} else if (convertToUnit.equals(KEV)) {
				value = energy.to(SI.KILO(NonSI.ELECTRON_VOLT)).getAmount();
			} else if (convertToUnit.equals(MDEG)) {
				Angle angle = BraggAngle.braggAngleOf(energy, twoD);
				value = angle.to(NonSIext.mDEG_ANGLE).getAmount();
			}
		}
		return value;
	}

	/**
	 * @return twoD
	 */
	public static double getTwoD() {
		return twoD.to(NonSI.ANGSTROM).getAmount();
	}
}
