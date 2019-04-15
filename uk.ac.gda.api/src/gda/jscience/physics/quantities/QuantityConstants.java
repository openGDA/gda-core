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

import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Quantity;
import javax.measure.quantity.Velocity;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.Constants;

public final class QuantityConstants {
	public static final Amount<Energy> ZERO_ENERGY = Amount.valueOf(0, Energy.UNIT);
	public static final Amount<Length> ZERO_LENGTH = Amount.valueOf(0, Length.UNIT);
	public static final Amount<Angle> ZERO_ANGLE = Amount.valueOf(0, Angle.UNIT);

	// Easier-to-read names for some physical constants & derived values
	public static final Amount<? extends Quantity> H_BAR_SQUARED = Constants.ℏ.times(Constants.ℏ); // Square of (Planck's constant over 2 pi)
	public static final Amount<? extends Quantity> PLANCKS_CONSTANT = Constants.ℎ;
	public static final Amount<Mass> ELECTRON_MASS_TIMES_TWO = Constants.me.times(2.0);
	public static final Amount<Velocity> SPEED_OF_LIGHT = Constants.c;

	private QuantityConstants() {
		// prevent instantiation
	}
}
