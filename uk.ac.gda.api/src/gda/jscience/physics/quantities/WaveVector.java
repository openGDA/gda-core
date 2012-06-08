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

import gda.jscience.physics.units.NonSIext;

import org.jscience.physics.quantities.Constants;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Quantity;

/**
 */
public class WaveVector extends Vector {
	/**
	 * Default constructor (allows for derivation).
	 */
	protected WaveVector() {
	}

	/**
	 * Returns the Wave Vector for the electron with specified electron energy above the specified edge energy. (Mainly
	 * for use in XAFS calculations hence the absorption edge based terminology.)
	 * 
	 * @param edgeEnergy
	 *            Energy the absorption edge energy.
	 * @param electronEnergy
	 *            Energy the energy of the electron.
	 * @return WaveVector of the electron
	 */
	public static Vector waveVectorOf(Energy edgeEnergy, Energy electronEnergy) {
		Quantity electronMassTimesTwo = Constants.me.times(2.0);
		Quantity q = electronEnergy.minus(edgeEnergy).times(electronMassTimesTwo).root(2).divide(Constants.hBar);

		// FIXME: This method used to just return (Vector)q but that seems to cause a class cast exception now. BFI
		// reconstruction of a Vector with the correct values seems to be the only way to get it to work now.
		return Quantity.valueOf(q.doubleValue() / 1.0E10, NonSIext.PER_ANGSTROM);
	}
}
