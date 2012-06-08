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
 * class used to test the concept used in SplitConverterHolder without the need to instantiate an ObjectServer
 */
final class SplitQuantitiesConverter implements IQuantitiesConverter, IQuantityConverter {

	private final IQuantitiesConverter toSourceConverter, calculateMoveablesConverter;

	SplitQuantitiesConverter(IQuantitiesConverter toSourceConverter, IQuantitiesConverter calculateMoveablesConverter) {
		if (toSourceConverter == null || calculateMoveablesConverter == null) {
			throw new IllegalArgumentException(
					"SplitQuantitiesConverter.SplitQuantitiesConverter: converters cannot be null");
		}
		this.toSourceConverter = toSourceConverter;
		this.calculateMoveablesConverter = calculateMoveablesConverter;
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#calculateMoveables(org.jscience.physics.quantities.Quantity[],
	 *      java.lang.Object[])
	 */
	@Override
	public Quantity[] calculateMoveables(Quantity[] sources, Object[] moveables) throws Exception {
		return calculateMoveablesConverter.calculateMoveables(sources, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#toSource(org.jscience.physics.quantities.Quantity[],
	 *      java.lang.Object[])
	 */
	@Override
	public Quantity[] toSource(Quantity[] targets, Object[] moveables) throws Exception {
		return toSourceConverter.toSource(targets, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#getAcceptableUnits()
	 */
	@Override
	public ArrayList<ArrayList<Unit<? extends Quantity>>> getAcceptableUnits() {
		return calculateMoveablesConverter.getAcceptableUnits();
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#getAcceptableMoveableUnits()
	 */
	@Override
	public ArrayList<ArrayList<Unit<? extends Quantity>>> getAcceptableMoveableUnits() {
		return toSourceConverter.getAcceptableMoveableUnits();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SplitQuantitiesConverter using toSourceConverter " + toSourceConverter.toString()
				+ " and calculateMoveablesConverter " + calculateMoveablesConverter.toString();
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#sourceMinIsTargetMax()
	 */
	@Override
	public boolean sourceMinIsTargetMax() {
		return toSourceConverter.sourceMinIsTargetMax();
	}

	@Override
	public Quantity toSource(Quantity target) throws Exception {
		if (!(toSourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"SplitQuantitiesConverter.toSource: toSourceConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) toSourceConverter).toSource(target);
	}

	@Override
	public Quantity toTarget(Quantity source) throws Exception {
		if (!(calculateMoveablesConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"SplitQuantitiesConverter.toTarget: calculateMoveablesConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) calculateMoveablesConverter).toTarget(source);
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableSourceUnits() {
		if (!(calculateMoveablesConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"SplitQuantitiesConverter.getAcceptableSourceUnits: calculateMoveablesConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) calculateMoveablesConverter).getAcceptableSourceUnits();
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableTargetUnits() {
		if (!(toSourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"SplitQuantitiesConverter.getAcceptableTargetUnits: toSourceConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) toSourceConverter).getAcceptableTargetUnits();
	}

	@Override
	public boolean handlesStoT() {
		if (!(calculateMoveablesConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"SplitQuantitiesConverter.handlesStoT: calculateMoveablesConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) calculateMoveablesConverter).handlesStoT();
	}

	@Override
	public boolean handlesTtoS() {
		if (!(toSourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"SplitQuantitiesConverter.handlesTtoS: toSourceConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) toSourceConverter).handlesTtoS();
	}

}
