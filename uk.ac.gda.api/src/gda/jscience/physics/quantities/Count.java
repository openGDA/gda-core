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

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jscience.physics.units.NonSIext;

public class Count extends Quantity {
	private static final Logger logger = LoggerFactory.getLogger(Count.class);

    /**
     * Holds the associated unit.
     */
	private static final Unit<Count> UNIT = NonSIext.COUNT;

	/**
	 * Holds the factory for this class.
	 */
	@SuppressWarnings("unused")
	private static final Factory<Count> FACTORY = new Factory<Count>(UNIT) {
		@Override
		protected Count create() {
			logger.debug("Count created");
			return new Count();
		}
	};

	/**
	 * Represents a {@link Count} amounting to nothing.
	 */
	public static final Count ZERO = Quantity.valueOf(0, UNIT);

	/**
	 * Default constructor (allows for derivation).
	 */
	protected Count() {
	}

    private static final long serialVersionUID = 1L;
}
