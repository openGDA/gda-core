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

import java.util.List;

import javax.measure.Quantity;

public interface IQuantitiesConverter<S extends Quantity<S>, T extends Quantity<T>> extends IQuantityConverter<S, T> {

	/**
	 * Calculates an array of values in units of the source given an array of quantities in units of the moveables
	 *
	 * @param targets
	 *            Array of values in units of the moveables to be converted.
	 * @param moveables
	 *            Array of <code>Moveable</code>
	 * @return Array of calculated values in units of the source
	 * @throws Exception
	 */
	public Quantity<S>[] toSource(Quantity<T>[] targets, Object[] moveables) throws Exception;

	/**
	 * Calculates an array of values in units of the source given an array of quantities in units of the source
	 *
	 * @param sources
	 *            Array of values in units of the sources to be converted.
	 * @param moveables
	 *            Array of <code>Moveable</code>
	 * @return Array of calculated values in units of the moveables
	 * @throws Exception
	 */
	public Quantity<T>[] calculateMoveables(Quantity<S>[] sources, Object[] moveables) throws Exception;

	/**
	 * Returns the units in which the sources quantities passed into calculateMoveables can be expressed
	 *
	 * @return Each element of the array is an array of units that is acceptable for the corresponding source in calls
	 *         to calculateMoveables
	 */
	public List<List<String>> getAcceptableUnits();

	/**
	 * Returns the units in which the targets quantities passed into ToSource can be expressed
	 *
	 * @return Each element of the array is an array of units that is acceptable for the corresponding target in calls
	 *         to ToSource
	 */
	public List<List<String>> getAcceptableMoveableUnits();

}
