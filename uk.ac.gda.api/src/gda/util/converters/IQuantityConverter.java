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

/**
 * Used by <code>gda.util.converters.GenQuantitiesConverter</code> to convert between two Quantities.
 *
 * Source is the value internal to GDa and target is the motor position.
 */
public interface IQuantityConverter<S extends Quantity<S>, T extends Quantity<T>> extends IConverter<Quantity<S>, Quantity<T>> {
	/**
	 * @return AcceptableSourceUnits
	 */
	public List<String> getAcceptableSourceUnits();

	/**
	 * @return AcceptableTargetUnits
	 */
	public List<String> getAcceptableTargetUnits();

	/**
	 *
	 * @return true if the converter converts from S to T
	 */
	public boolean handlesStoT();

	/**
	 *
	 * @return true if the converter converts from T to S
	 */
	public boolean handlesTtoS();

}
