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

package gda.jscience.physics.quantities;

import static gda.jscience.physics.quantities.QuantityConstants.ELECTRON_MASS_TIMES_TWO;
import static gda.jscience.physics.quantities.QuantityConstants.H_BAR_SQUARED;
import static gda.jscience.physics.quantities.QuantityConstants.PLANCKS_CONSTANT;
import static gda.jscience.physics.quantities.QuantityConstants.SPEED_OF_LIGHT;

/*
 * Extension to JScience - Java(TM) Tools and Libraries for the Advancement of Sciences. Developed for synchrotron
 * radiation applications All rights reserved. Permission to use, copy, modify, and distribute this software is freely
 * granted, provided that this notice is preserved.
 */

import static gda.jscience.physics.quantities.QuantityConstants.ZERO_ANGLE;
import static gda.jscience.physics.quantities.QuantityConstants.ZERO_ENERGY;
import static gda.jscience.physics.quantities.QuantityConstants.ZERO_LENGTH;
import static javax.measure.unit.SI.JOULE;
import static javax.measure.unit.SI.KILOGRAM;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A method only class to extend the functionality of the {@link Energy}. This class extends
 * {@link Energy} to provide only the additional methods required for the conversions
 * between Photon Energy, Wavelength of X-Ray, and Bragg Angle of the crystal of the Monochromator. While these
 * additional functions should be accessed using class name {@link Wavelength}, their return types are {@link Energy}
 * in order to keep them compatible with the existing Unit System in JScience. Ideally these methods should be
 * implemented directly into the {@link Energy} class.
 */
public class PhotonEnergy implements Energy {
	private static final Logger logger = LoggerFactory.getLogger(PhotonEnergy.class);

	@SuppressWarnings("unused")
	private static final long serialVersionUID = -4217480403676154739L;

	/**
	 * Default constructor (allows for derivation).
	 */
	protected PhotonEnergy() {
	}

	// //////////////////
	// X-RAY SPECIFIC //
	// //////////////////
	/**
	 * Returns the {@link PhotonEnergy} for the specified X-Ray wavelength.
	 *
	 * @param wavelength
	 *            Wavelength the specified X-Ray wavelength.
	 * @return PhotonEnergy the energy of the photon.
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
	 * Returns the {@link PhotonEnergy} for the specified Bragg Angle of the crystal.
	 *
	 * @param braggAngle
	 *            Angle the Bragg angle of the crystal.
	 * @param twoD
	 *            Length the 2*d spacing of the crystal.
	 * @return PhotonEnergy the energy of the photon from the crystal.
	 */
	public static Amount<Energy> photonEnergyFromBraggAngle(Amount<Angle> braggAngle, Amount<Length> twoD) {
		logger.debug("photonEnergyFromBraggAngle(braggAngle = {}, twoD = {})", braggAngle, twoD);
		if (braggAngle != null && twoD != null && braggAngle.isGreaterThan(ZERO_ANGLE) && twoD.isGreaterThan(ZERO_LENGTH)) {
			// calculate wavelength first then the photon energy
			return PhotonEnergy.photonEnergyFromWavelength(Wavelength.wavelengthOf(braggAngle, twoD));
		}
		return null;
	}

	/**
	 * Returns the {@link PhotonEnergy} for the specified edge.
	 *
	 * @param edge
	 * @param k
	 * @return PhotonEnergy the energy of the photon from the crystal.
	 */
	public static Amount<Energy> photonEnergyFromEdgeAndVector(Amount<Energy> edge, Amount<Vector> k) {
		logger.debug("photonEnergyFromEdgeAndVector(edge = {}, k = {})", edge, k);
		if (edge != null && k != null && edge.isGreaterThan(ZERO_ENERGY) && k.isGreaterThan(Vector.ZERO)) {
			final Amount<? extends Quantity> a = H_BAR_SQUARED.divide(ELECTRON_MASS_TIMES_TWO);
			final Amount<? extends Quantity> kSquared = k.times(k);
			return edge.plus(kSquared.times(a));
		}
		return null;
	}

	/**
	 * Returns the {@link PhotonEnergy} for the specified edge.
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
}
