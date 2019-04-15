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

import java.util.function.Function;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

/**
 * Identity function (yValue = xValue).
 */
public class IdentityFunction implements Function<Amount<? extends Quantity>, Amount<? extends Quantity>> {

	@Override
	public Amount<? extends Quantity> apply(Amount<? extends Quantity> xValue) {
		return xValue;
	}
}
