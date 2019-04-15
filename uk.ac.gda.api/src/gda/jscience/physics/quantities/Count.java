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

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import gda.jscience.physics.units.NonSIext;

public class Count implements Quantity {
	/**
     * Holds the associated unit.
     */
	public static final Unit<Count> UNIT = NonSIext.COUNT;

	/**
	 * Represents a {@link Count} amounting to nothing.
	 */
	public static final Amount<Count> ZERO = Amount.valueOf(0, UNIT);

	/**
	 * Default constructor (allows for derivation).
	 */
	protected Count() {
	}

    @SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
}
