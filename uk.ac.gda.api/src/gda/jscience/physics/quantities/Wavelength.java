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
 * - for X-Ray applications. All rights reserved. Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Constants;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;

/**
 * A method only class to extend the functionality of the {@link Length} class. This class extends
 * {@link org.jscience.physics.quantities.Length} to provide additional methods for conversions between Wavelength,
 * Bragg Angle, and Photon Energy for X-Ray application. While these additional methods should be accessed using class
 * name, their returns are of the {@link Length} type, not {@link Wavelength} type, in order to make them compatible
 * with the Unit System defined in JScience. Ideally these method should be implemented directly into the {@link Length}
 * class.
 */
public class Wavelength extends Length {
	private static final long serialVersionUID = 2110601334827305173L;

	/**
	 * Default constructor (allows for derivation).
	 */
	protected Wavelength() {
	}

	/**
	 * Returns the X-Ray wavelength of the specified photon energy.
	 * 
	 * @param photonEnergy
	 *            PhotonEnergy the energy of the photon.
	 * @return Wavelength the X-Ray wavelength of the specified photon energy.
	 */
	public static Length wavelengthOf(Energy photonEnergy) {
		if (photonEnergy != null && photonEnergy.isGreaterThan(Energy.ZERO)) {
			// Returns (h*c/E)
			return (Length) (Constants.h.times(Constants.c).divide(photonEnergy));
		}
		return null;
	}

	/**
	 * Returns the X-Ray wavelength of the specified Bragg Angle for the crystal.
	 * 
	 * @param braggAngle
	 *            Angle the Bragg Angle of the crystal.
	 * @param twoD
	 *            Length the 2d spacing of the crystal.
	 * @return Wavelength the X-Ray wavelength selected by the crystal.
	 */
	public static Length wavelengthOf(Angle braggAngle, Length twoD) {
		if (braggAngle != null && twoD != null && braggAngle.isGreaterThan(Angle.ZERO)
				&& twoD.isGreaterThan(Length.ZERO)) {
			return (Length) (twoD.times(braggAngle.sine()));
		}
		return null;
	}
}
