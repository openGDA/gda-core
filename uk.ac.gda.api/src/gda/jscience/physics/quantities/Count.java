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

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.quantities.QuantityFormat;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.Unit;

/**
 * 
 */
public class Count extends Quantity {
    /**
     * Holds the associated unit.
     */
	private final static Unit<Count> UNIT = NonSIext.COUNT;

	/**
	 * Holds the factory for this class.
	 */
	@SuppressWarnings("unused")
	private final static Factory<Count> FACTORY = new Factory<Count>(UNIT) {
		@Override
		protected Count create() {
			return new Count();
		}
	};
	
	/**
	 * Represents a {@link Count} amounting to nothing.
	 */
	public final static Count ZERO = Quantity.valueOf(0, UNIT);
	
	/**
	 * Default constructor (allows for derivation).
	 */
	protected Count() {
	}

	/**
	 * Returns the {@link Count} corresponding to the specified quantity.
	 * 
	 * @param q
	 *            a quantity compatible with {@link Count}.
	 * @return the specified quantity or a new {@link Count} instance.
	 * @throws ConversionException
	 *             if the current model does not allow the specified quantity to be converted to {@link Count}.
	 */
	public static Count CountOf(Quantity q) {
		return q.to(UNIT);
	}

	/**
	 * Shows {@link Count} instances in the specified unit.
	 * 
	 * @param unit the display unit for {@link Count} instances.
	 */
	public static void showAs(Unit<? extends Quantity> unit) {
		QuantityFormat.show(Count.class, unit);
	}

    private static final long serialVersionUID = 1L;
	
}
