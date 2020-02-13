/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static si.uom.SI.JOULE_SECOND;
import static tec.units.indriya.unit.Units.KILOGRAM;
import static tec.units.indriya.unit.Units.METRE_PER_SECOND;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Speed;

import si.uom.quantity.Action;
import tec.units.indriya.quantity.Quantities;

/**
 * Easier-to-read names for some physical constants & derived values
 */
public final class QuantityConstants {
	// Physical constants from: https://physics.nist.gov/cuu/Constants/index.html
	public static final Quantity<Action> PLANCKS_CONSTANT = Quantities.getQuantity(6.62607015e-34, JOULE_SECOND);
	public static final Quantity<Mass> ELECTRON_MASS = Quantities.getQuantity(9.1093837015e-31, KILOGRAM);
	public static final Quantity<Speed> SPEED_OF_LIGHT = Quantities.getQuantity(2.99792458e8, METRE_PER_SECOND);

	// Derived values
	public static final Quantity<? extends Quantity<?>> H_BAR = PLANCKS_CONSTANT.divide(Math.PI * 2.0);
	public static final Quantity<? extends Quantity<?>> H_BAR_SQUARED = H_BAR.multiply(H_BAR); // Square of (Planck's constant over 2 pi)
	public static final Quantity<Mass> ELECTRON_MASS_TIMES_TWO = ELECTRON_MASS.multiply(2.0);

	private QuantityConstants() {
		// prevent instantiation
	}
}
