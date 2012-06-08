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

import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.quantities.QuantityFormat;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.Unit;

/**
 * 
 */
public class Vector extends Quantity {
	/**
	 * Holds the system unit.
	 */
	private final static Unit<Vector> UNIT = NonSIext.PER_ANGSTROM;

	/**
	 * Holds the factory for this class.
	 */
	@SuppressWarnings("unused")
	private final static Factory<Vector> FACTORY = new Factory<Vector>(UNIT) {
		@Override
		protected Vector create() {
			return new Vector();
		}
	};// .useFor(NonSIext.PER_ANGSTROM);

	/**
	 * Represents a {@link Length} amounting to nothing.
	 */
	public final static Vector ZERO = Quantity.valueOf(0, UNIT);

	/**
	 * Default constructor (allows for derivation).
	 */
	protected Vector() {
	}

	/**
	 * Returns the {@link Length} corresponding to the specified quantity.
	 * 
	 * @param length
	 *            a quantity compatible with {@link Length}.
	 * @return the specified quantity or a new {@link Length} instance.
	 * @throws ConversionException
	 *             if the current model does not allow the specified quantity to be converted to {@link Length}.
	 */
	public static Vector vectorOf(Length length) {
		return length.inverse().to(UNIT);
	}

	/**
	 * Shows {@link Length} instances in the specified unit.
	 * 
	 * @param unit
	 *            the output unit for {@link Length} instances.
	 * @see QuantityFormat#getOutputUnit
	 */
	public static void showAs(Unit<? extends Quantity> unit) {
		QuantityFormat.show(Vector.class, unit);
	}

}
