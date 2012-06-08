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

import java.util.ArrayList;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * Used by <code>gda.util.converters.GenQuantitiesConverter</code> to convert between two Quantities.
 * 
 * Source is the value internal to GDa and target is the motor position.
 */
public interface IQuantityConverter extends IConverter<Quantity, Quantity> {
	/**
	 * @return AcceptableSourceUnits
	 */
	public ArrayList<Unit<? extends Quantity>> getAcceptableSourceUnits();

	/**
	 * @return AcceptableTargetUnits
	 */
	public ArrayList<Unit<? extends Quantity>> getAcceptableTargetUnits();
	
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
