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
	 * @see gda.util.converters.IQuantitiesConverter#calculateMoveables(Quantity[], Object[])
	 */
	@Override
	public Quantity<? extends Quantity<?>>[] calculateMoveables(Quantity<? extends Quantity<?>>[] sources, Object[] moveables) throws Exception {
		return calculateMoveablesConverter.calculateMoveables(sources, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#toSource(Quantity[], Object[])
	 */
	@Override
	public Quantity<? extends Quantity<?>>[] toSource(Quantity<? extends Quantity<?>>[] targets, Object[] moveables) throws Exception {
		return toSourceConverter.toSource(targets, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#getAcceptableUnits()
	 */
	@Override
	public List<List<String>> getAcceptableUnits() {
		return calculateMoveablesConverter.getAcceptableUnits();
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#getAcceptableMoveableUnits()
	 */
	@Override
	public List<List<String>> getAcceptableMoveableUnits() {
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
	public Quantity<? extends Quantity<?>> toSource(Quantity<? extends Quantity<?>> target) throws Exception {
		if (!(toSourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"SplitQuantitiesConverter.toSource: toSourceConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) toSourceConverter).toSource(target);
	}

	@Override
	public Quantity<? extends Quantity<?>> toTarget(Quantity<? extends Quantity<?>> source) throws Exception {
		if (!(calculateMoveablesConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"SplitQuantitiesConverter.toTarget: calculateMoveablesConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) calculateMoveablesConverter).toTarget(source);
	}

	@Override
	public List<String> getAcceptableSourceUnits() {
		if (!(calculateMoveablesConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"SplitQuantitiesConverter.getAcceptableSourceUnits: calculateMoveablesConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) calculateMoveablesConverter).getAcceptableSourceUnits();
	}

	@Override
	public List<String> getAcceptableTargetUnits() {
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
