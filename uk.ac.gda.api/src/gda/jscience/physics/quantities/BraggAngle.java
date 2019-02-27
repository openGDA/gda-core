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

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.SI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the Bragg angle of a crystal.
 */
public final class BraggAngle extends Angle {
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
	public static Angle braggAngleOf(Length wavelength, Length twoD) {
		logger.debug("braggAngleOf(wavelength = {}, twoD = {})", wavelength, twoD);
		if (wavelength != null && twoD != null && wavelength.isGreaterThan(Length.ZERO)
				&& twoD.isGreaterThan(Length.ZERO)) {
			return Quantity.valueOf(Math.asin(wavelength.divide(twoD).doubleValue()), SI.RADIAN);
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
	public static Angle braggAngleOf(Energy photonEnergy, Length twoD) {
		logger.debug("braggAngleOf(photonEnergy = {}, twoD = {})", photonEnergy, twoD);
		if (photonEnergy != null && twoD != null && photonEnergy.isGreaterThan(Energy.ZERO)
				&& twoD.isGreaterThan(Length.ZERO)) {
			return BraggAngle.braggAngleOf(Wavelength.wavelengthOf(photonEnergy), twoD);
		}
		return null;
	}
}
