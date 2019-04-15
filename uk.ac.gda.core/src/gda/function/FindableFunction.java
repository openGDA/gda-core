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

import gda.factory.Findable;
import gda.factory.FindableBase;

/**
 * Base class for functions that operate on, and return, a Quantity and are {@link Findable}, so they (for example) can
 * be manipulated in Jython.
 *
 * @since GDA 9.9
 */

public abstract class FindableFunction extends FindableBase implements Function<Amount<? extends Quantity>, Amount<? extends Quantity>> {
}
