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

import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

import gda.jscience.physics.units.NonSIext;

public class Vector extends Quantity {
	/**
	 * Holds the system unit.
	 */
	public static final Unit<Vector> UNIT = NonSIext.PER_ANGSTROM;

	/**
	 * Holds the factory for this class.
	 */
	@SuppressWarnings("unused")
	private static final Factory<Vector> FACTORY = new Factory<Vector>(UNIT) {
		@Override
		protected Vector create() {
			return new Vector();
		}
	};

	/**
	 * Represents a {@link Length} amounting to nothing.
	 */
	public static final Vector ZERO = Quantity.valueOf(0, UNIT);

	/**
	 * Default constructor (allows for derivation).
	 */
	protected Vector() {
	}
}
