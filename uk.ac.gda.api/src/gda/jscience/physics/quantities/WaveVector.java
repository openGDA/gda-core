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
import static gda.jscience.physics.units.NonSIext.PER_ANGSTROM;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaveVector extends Vector {
	private static final Logger logger = LoggerFactory.getLogger(WaveVector.class);
	/**
	 * Default constructor (allows for derivation).
	 */
	protected WaveVector() {
	}

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
	public static Amount<Vector> waveVectorOf(Amount<Energy> edgeEnergy, Amount<Energy> electronEnergy) {
		logger.debug("waveVectorOf(edgeEnergy = {}, electronEnergy = {})", edgeEnergy, electronEnergy);
		if (edgeEnergy != null && electronEnergy != null && edgeEnergy.isGreaterThan(ZERO_ENERGY) && electronEnergy.isGreaterThan(ZERO_ENERGY)) {

			final Amount<Energy> edgeEnergySi = edgeEnergy.to(Energy.UNIT);
			final Amount<Energy> electronEnergySi = electronEnergy.to(Energy.UNIT);
			final Amount<Mass> electronMassTimesTwoSi = Constants.me.times(2.0).to(Mass.UNIT);

			final Amount<? extends Quantity> q = electronEnergySi.minus(edgeEnergySi).times(electronMassTimesTwoSi).root(2).divide(Constants.ℏ);
			return Amount.valueOf(q.getEstimatedValue() / 1.0E10, PER_ANGSTROM);
		}
		return null;
	}
}
