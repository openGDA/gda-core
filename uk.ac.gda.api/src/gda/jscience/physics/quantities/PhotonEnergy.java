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

/*
 * Extension to JScience - Java(TM) Tools and Libraries for the Advancement of Sciences. Developed for synchrotron
 * radiation applications All rights reserved. Permission to use, copy, modify, and distribute this software is freely
 * granted, provided that this notice is preserved.
 */

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Constants;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.SI;

/**
 * A method only class to extend the functionality of the {@link Energy}. This class extends
 * {@link org.jscience.physics.quantities.Energy} to provide only the additional methods required for the conversions
 * between Photon Energy, Wavelength of X-Ray, and Bragg Angle of the crystal of the Monochromator. While these
 * additional functions should be accessed using class name {@link Wavelength}, their return types are {@link Energy}
 * in order to keep them compatible with the existing Unit System in JScience. Ideally these methods should be
 * implemented directly into the {@link Energy} class.
 */
public class PhotonEnergy extends Energy {

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
	public static Energy photonEnergyOf(Length wavelength) {
		if (wavelength != null && wavelength.isGreaterThan(Length.ZERO)) {
			// returns (h*c/λ)
			return (Energy) (Constants.h.times(Constants.c).divide(wavelength));
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
	public static Energy photonEnergyOf(Angle braggAngle, Length twoD) {
		if (braggAngle != null && twoD != null && braggAngle.isGreaterThan(Angle.ZERO)
				&& twoD.isGreaterThan(Length.ZERO)) {
			// calculate wavelength first then the photon energy
			return PhotonEnergy.photonEnergyOf(Wavelength.wavelengthOf(braggAngle, twoD));
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
	public static Energy photonEnergyOf(Quantity edge, Quantity k) {
		if (edge != null && k != null && edge.isGreaterThan(Energy.ZERO) && k.isGreaterThan(Vector.ZERO)) {
			Quantity hBarSquared = Constants.hBar.times(Constants.hBar);
			Quantity electronMassTimesTwo = Constants.me.times(2.0);
			Quantity a = hBarSquared.divide(electronMassTimesTwo);
			Quantity kSquared = k.times(k);
			return (Energy) edge.plus(kSquared.times(a));
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
	public static Energy photonEnergyOf(Energy edge, double value) {
		if (edge != null && edge.isGreaterThan(Energy.ZERO) && value > 0.0) {
			Quantity hBarSquared = Constants.hBar.times(Constants.hBar);
			Quantity electronMassTimesTwo = Constants.me.times(2.0);
			double newValue = edge.doubleValue()
					+ (1.0E20 * value * value * hBarSquared.doubleValue() / electronMassTimesTwo.doubleValue());

			return Quantity.valueOf(newValue, SI.JOULE);
		}
		return null;
	}
}
