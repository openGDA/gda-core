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

package gda.util.converters;

import javax.measure.Quantity;

import gda.factory.Findable;

/**
 * Allows a object that supports IQuantitiesConverter to reload its state from any resource on which it may depend e.g.
 * re-read a lookup table.
 */
public interface IReloadableQuantitiesConverter<S extends Quantity<S>, T extends Quantity<T>> extends IQuantitiesConverter<S, T>, Findable {
	/**
	 * Reload its state from any resource on which it may depend e.g. re-read a lookup table.
	 */
	public void reloadConverter();
}
