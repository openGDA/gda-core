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

import static gda.jscience.physics.quantities.QuantityConstants.ZERO_ENERGY;
import static gda.jscience.physics.quantities.QuantityConstants.ZERO_LENGTH;
import static javax.measure.unit.SI.RADIAN;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the Bragg angle of a crystal.
 */
public final class BraggAngle implements Angle {
	private static final Logger logger = LoggerFactory.getLogger(BraggAngle.class);

	/**
	 * Prevent instantiation
	 */
	private BraggAngle() {
	}

	/**
	 * Returns the {@link BraggAngle} for the specified X-Ray wavelength
	 *
	 * @param wavelength
	 *            Wavelength the specified X-Ray wavelength.
	 * @param twoD
	 *            Length the 2*d spacing of the crystal.
	 * @return BraggAngle the Bragg Angle of the crystal.
	 */
	public static Amount<Angle> braggAngleFromWavelength(Amount<Length> wavelength, Amount<Length> twoD) {
		logger.debug("braggAngleFromWavelength(wavelength = {}, twoD = {})", wavelength, twoD);
		if (wavelength != null && twoD != null && wavelength.isGreaterThan(ZERO_LENGTH)
				&& twoD.isGreaterThan(ZERO_LENGTH)) {
			final Amount<Dimensionless> wavelengthBySpacing = wavelength.divide(twoD).to(Dimensionless.UNIT);
			return Amount.valueOf(Math.asin(wavelengthBySpacing.doubleValue(Dimensionless.UNIT)), RADIAN);
		}
		return null;
	}

	/**
	 * Returns the {@link BraggAngle} for the specified X-Ray photon energy.
	 *
	 * @param photonEnergy
	 *            PhotonEnergy the specified X-Ray photon energy.
	 * @param twoD
	 *            Length the 2*d spacing of the crystal.
	 * @return BraggAngle the Bragg Angle of the crystal.
	 */
	public static Amount<Angle> braggAngleFromEnergy(Amount<Energy> photonEnergy, Amount<Length> twoD) {
		logger.debug("braggAngleFromEnergy(photonEnergy = {}, twoD = {})", photonEnergy, twoD);
		if (photonEnergy != null && twoD != null && photonEnergy.isGreaterThan(ZERO_ENERGY)
				&& twoD.isGreaterThan(ZERO_LENGTH)) {
			return BraggAngle.braggAngleFromWavelength(Wavelength.wavelengthOf(photonEnergy), twoD);
		}
		return null;
	}
}
