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

package gda.function;

import gda.factory.FactoryException;
import gda.util.QuantityFactory;

import org.jscience.physics.quantities.Quantity;

/**
 * Similar to the IdentityFunction, but holds an offset.
 */
public class OffsetFunction extends Function {

	private double offset = 0.0;

	@SuppressWarnings("unchecked")
	@Override
	public Quantity evaluate(Quantity value) {
		return value.plus(QuantityFactory.createFromObject(offset, value.getUnit()));
	}

	@Override
	public void configure() throws FactoryException {
	}

	/**
	 * @return Returns the offset.
	 */
	public double getOffset() {
		return offset;
	}

	/**
	 * @param offset
	 *            The offset to set.
	 */
	public void setOffset(double offset) {
		this.offset = offset;
	}
}
