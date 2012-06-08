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
import org.jscience.physics.quantities.Length;

/**
 * 
 */
public class ScatteringVector extends Vector {
	/**
	 * Default constructor (allows for derivation).
	 */
	protected ScatteringVector() {
	}

	/**
	 * Returns the Scattering Vector given an angle and wavelength.
	 * 
	 * @param theta
	 *            the angle
	 * @param wavelength
	 *            the wavelength
	 * @return the scattering vector
	 */
	public static Vector scatteringVectorOf(Angle theta, Length wavelength) {
		// Length.showAs(NonSI.ANGSTROM);
		return (Vector) (theta.sine().times(Constants.four_π).divide(wavelength));
	}

}
