/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.jscience.physics.quantities;

import static gda.jscience.physics.quantities.QuantityConstants.ELECTRON_MASS_TIMES_TWO;
import static gda.jscience.physics.quantities.QuantityConstants.H_BAR_SQUARED;
import static gda.jscience.physics.quantities.QuantityConstants.PLANCKS_CONSTANT;
import static gda.jscience.physics.quantities.QuantityConstants.SPEED_OF_LIGHT;
import static gda.jscience.physics.quantities.QuantityConstants.ZERO_ANGLE;
import static gda.jscience.physics.quantities.QuantityConstants.ZERO_ENERGY;
import static gda.jscience.physics.quantities.QuantityConstants.ZERO_LENGTH;
import static gda.jscience.physics.units.NonSIext.PER_ANGSTROM;
import static javax.measure.unit.SI.JOULE;
import static javax.measure.unit.SI.KILOGRAM;
import static javax.measure.unit.SI.RADIAN;
import static org.jscience.physics.amount.Constants.me;
import static org.jscience.physics.amount.Constants.ℏ;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.DSpacing;

public class QuantityConverters {

	private static final Logger logger = LoggerFactory.getLogger(QuantityConverters.class);

	private QuantityConverters() {
		// prevent instantiation
	}

	/*
	 * Bragg Angle conversion functions
	 */
	/**
	 * Returns the Bragg Angle for the specified X-Ray wavelength
	 *
	 * @param wavelength
	 *            the specified X-Ray wavelength.
	 * @param twoD
	 *            the 2*d spacing of the crystal.
	 * @return BraggAngle the Bragg Angle of the crystal.
	 */
	public static Amount<Angle> braggAngleFromWavelength(Amount<Length> wavelength, Amount<Length> twoD) {
		logger.debug("braggAngleFromWavelength(wavelength = {}, twoD = {})", wavelength, twoD);
		if (wavelength == null || twoD == null) {
			return null;
		}
		final double wavelengthVal = wavelength.doubleValue(Length.UNIT);
		final double twoDVal = twoD.doubleValue(Length.UNIT);
		if (wavelengthVal <= 0 || twoDVal <= 0) {
			return null;
		}
		final double result = DSpacing.coneAngleFromDSpacing(wavelengthVal, twoDVal / 2.0) / 2.0;
		return Amount.valueOf(result, RADIAN);
	}

	/**
	 * Returns the Bragg Angle for the specified X-Ray photon energy.
	 *
	 * @param photonEnergy
	 *            PhotonEnergy the specified X-Ray photon energy.
	 * @param twoD
	 *            Length the 2*d spacing of the crystal.
	 * @return the Bragg Angle of the crystal.
	 */
	public static Amount<Angle> braggAngleFromEnergy(Amount<Energy> photonEnergy, Amount<Length> twoD) {
		logger.debug("braggAngleFromEnergy(photonEnergy = {}, twoD = {})", photonEnergy, twoD);
		if (photonEnergy != null && twoD != null && photonEnergy.doubleValue(Energy.UNIT) > 0 && twoD.doubleValue(Length.UNIT) > 0) {
			return braggAngleFromWavelength(wavelengthOf(photonEnergy), twoD);
		}
		return null;
	}

	/*
	 * Photon energy conversion functions (X-ray specific)
	 *
	 * These functions extend the functionality of Energy to provide only the additional methods required for the
	 * conversions between Photon Energy, Wavelength of X-Ray, and Bragg Angle of the crystal of the Monochromator.
	 * While these additional functions should be accessed using class name Wavelength, their return types are Energy in
	 * order to keep them compatible with the existing Unit System in JScience. Ideally these methods should be
	 * implemented directly into the Energy class.
	 */
	/**
	 * Returns the photon energy for the specified X-Ray wavelength.
	 *
	 * @param wavelength
	 *            Wavelength the specified X-Ray wavelength.
	 * @return the energy of the photon.
	 */
	public static Amount<Energy> photonEnergyFromWavelength(Amount<Length> wavelength) {
		logger.debug("photonEnergyFromWavelength(wavelength = {})", wavelength);
		if (wavelength != null && wavelength.isGreaterThan(ZERO_LENGTH)) {
			// returns (h*c/λ)
			return PLANCKS_CONSTANT.times(SPEED_OF_LIGHT).divide(wavelength).to(Energy.UNIT);
		}
		return null;
	}

	/**
	 * Returns the photon energy for the specified Bragg Angle of the crystal.
	 *
	 * @param braggAngle
	 *            Angle the Bragg angle of the crystal.
	 * @param twoD
	 *            Length the 2*d spacing of the crystal.
	 * @return the energy of the photon from the crystal.
	 */
	public static Amount<Energy> photonEnergyFromBraggAngle(Amount<Angle> braggAngle, Amount<Length> twoD) {
		logger.debug("photonEnergyFromBraggAngle(braggAngle = {}, twoD = {})", braggAngle, twoD);
		if (braggAngle != null && twoD != null && braggAngle.isGreaterThan(ZERO_ANGLE) && twoD.isGreaterThan(ZERO_LENGTH)) {
			// calculate wavelength first then the photon energy
			return photonEnergyFromWavelength(wavelengthOf(braggAngle, twoD));
		}
		return null;
	}

	/**
	 * Returns the photon energy for the specified edge.
	 *
	 * @param edge
	 * @param k
	 * @return the energy of the photon from the crystal.
	 */
	public static Amount<Energy> photonEnergyFromEdgeAndVector(Amount<Energy> edge, Amount<WaveVector> k) {
		logger.debug("photonEnergyFromEdgeAndVector(edge = {}, k = {})", edge, k);
		if (edge != null && k != null && edge.isGreaterThan(ZERO_ENERGY) && k.isGreaterThan(WaveVector.ZERO)) {
			final Amount<? extends Quantity> a = H_BAR_SQUARED.divide(ELECTRON_MASS_TIMES_TWO);
			final Amount<? extends Quantity> kSquared = k.times(k);
			return edge.plus(kSquared.times(a));
		}
		return null;
	}

	/**
	 * Returns the photon energy for the specified edge.
	 *
	 * @param edge
	 * @param value
	 * @return PhotonEnergy the energy of the photon from the crystal.
	 */
	public static Amount<Energy> photonEnergyFromEdgeAndValue(Amount<Energy> edge, double value) {
		logger.debug("photonEnergyFromEdgeAndValue(edge = {}, double value = {})", edge, value);
		if (edge != null && edge.isGreaterThan(ZERO_ENERGY) && value > 0.0) {
			final double newValue = edge.doubleValue(Energy.UNIT)
					+ (1.0E20 * value * value * H_BAR_SQUARED.getEstimatedValue() / ELECTRON_MASS_TIMES_TWO.doubleValue(KILOGRAM));

			return Amount.valueOf(newValue, JOULE);
		}
		return null;
	}

	/*
	 * Wavelength conversions functions
	 *
	 * These provide additional methods for conversions between Wavelength, Bragg Angle, and Photon Energy for X-Ray
	 * application. While these additional methods should be accessed using class name, their returns are of the Length
	 * type, not Wavelength type, in order to make them compatible with the Unit System defined in JScience. Ideally
	 * these method should be implemented directly into the Length class.
	 */
	/**
	 * Returns the X-Ray wavelength of the specified photon energy.
	 *
	 * @param photonEnergy
	 *            the energy of the photon.
	 * @return the X-Ray wavelength of the specified photon energy.
	 */
	public static Amount<Length> wavelengthOf(Amount<Energy> photonEnergy) {
		logger.debug("wavelengthOf(photonEnergy = {}", photonEnergy);
		if (photonEnergy != null && photonEnergy.isGreaterThan(ZERO_ENERGY)) {
			// Returns (h*c/E)
			return PLANCKS_CONSTANT.times(SPEED_OF_LIGHT).divide(photonEnergy).to(Length.UNIT);
		}
		return null;
	}

	/**
	 * Returns the X-Ray wavelength of the specified Bragg Angle for the crystal.
	 *
	 * @param braggAngle
	 *            the Bragg Angle of the crystal.
	 * @param twoD
	 *            the 2d spacing of the crystal.
	 * @return the X-Ray wavelength selected by the crystal.
	 */
	public static Amount<Length> wavelengthOf(Amount<Angle> braggAngle, Amount<Length> twoD) {
		logger.debug("wavelengthOf(braggAngle = {}, twoD = {})", braggAngle, twoD);
		if (braggAngle != null && twoD != null && braggAngle.isGreaterThan(ZERO_ANGLE)
				&& twoD.isGreaterThan(ZERO_LENGTH)) {
			final double braggAngleSine = Math.sin(braggAngle.doubleValue(RADIAN));
			return twoD.times(braggAngleSine);
		}
		return null;
	}

	/*
	 * WaveVector function
	 */
	/**
	 * Returns the Wave Vector for the electron with specified electron energy above the specified edge energy. (Mainly
	 * for use in XAFS calculations hence the absorption edge based terminology.)
	 * <p>
	 * It is not entirely clear what equation this function is supposed to implement.<br>
	 * In the previous version, in which the energy and electron mass values were not converted to their SI equivalents,
	 * the intermediate quantity q had units eV^1:2·kg^1:2/(J·s) and the JScience2 function doubleValue() multiplied the
	 * raw value by roughly a factor of 4 to return the double value. This does not work in JScience4, which complains
	 * about the fractional exponents.
	 * <p>
	 * In the absence of any clear documentation, this version converts the input quantities to SI units before
	 * calculating q. The estimated value of q is then the same as the value returned by doubleValue() in JScience2. The
	 * units are ignored and replaced by PER_ANGSTROM (as in the previous version).
	 * <p>
	 * We think that this calculation is used in the EXAFS GUI to indicate wavelength to the user, rather than actually
	 * being used in the scan.
	 *
	 * @param edgeEnergy
	 *            Energy the absorption edge energy.
	 * @param electronEnergy
	 *            Energy the energy of the electron.
	 * @return WaveVector of the electron
	 */
	public static Amount<WaveVector> waveVectorOf(Amount<Energy> edgeEnergy, Amount<Energy> electronEnergy) {
		logger.debug("waveVectorOf(edgeEnergy = {}, electronEnergy = {})", edgeEnergy, electronEnergy);
		if (edgeEnergy != null && electronEnergy != null && edgeEnergy.isGreaterThan(ZERO_ENERGY) && electronEnergy.isGreaterThan(ZERO_ENERGY)) {
			return electronEnergy.minus(edgeEnergy).to(Energy.UNIT).times(me).times(2.0).root(2).divide(ℏ).to(PER_ANGSTROM);
		}
		return null;
	}
}
