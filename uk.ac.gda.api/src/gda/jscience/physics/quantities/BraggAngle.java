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
import org.jscience.physics.quantities.Constants;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.SI;

/**
 * Represents the Bragg angle of a crystal.
 */
public final class BraggAngle extends Angle {
	/**
	 * Default constructor (allows for derivation).
	 */
	protected BraggAngle() {
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
	 *            PhotonEnergy the sepecified X-Ray photon energy.
	 * @param twoD
	 *            Length the 2*d spacing of the crystal.
	 * @return BraggAngle the Bragg Angle of the crystal.
	 */
	public static Angle braggAngleOf(Energy photonEnergy, Length twoD) {
		if (photonEnergy != null && twoD != null && photonEnergy.isGreaterThan(Energy.ZERO)
				&& twoD.isGreaterThan(Length.ZERO)) {
			return BraggAngle.braggAngleOf(Wavelength.wavelengthOf(photonEnergy), twoD);
		}
		return null;
	}

	/**
	 * Returns the {@link BraggAngle} for the specified scattering vector.
	 * 
	 * @param scatteringVector
	 * @param wavelength
	 *            the wavelength of the incident X-Ray.
	 * @return BraggAngle the Bragg Angle of the crystal.
	 */
	public static Angle braggAngleOf(ScatteringVector scatteringVector, Length wavelength) {
		return valueOf(Math.asin((scatteringVector.times(wavelength)).divide(Constants.four_π).doubleValue()),
				SI.RADIAN);
	}
}
