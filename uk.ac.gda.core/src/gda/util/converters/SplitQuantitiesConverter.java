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
final class SplitQuantitiesConverter<S extends Quantity<S>, T extends Quantity<T>> implements IQuantitiesConverter<S, T> {

	private final IQuantitiesConverter<S, T> toSourceConverter;
	private final IQuantitiesConverter<S, T> calculateMoveablesConverter;

	SplitQuantitiesConverter(IQuantitiesConverter<S, T> toSourceConverter, IQuantitiesConverter<S, T> calculateMoveablesConverter) {
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
	public Quantity<T>[] calculateMoveables(Quantity<S>[] sources, Object[] moveables) throws Exception {
		return calculateMoveablesConverter.calculateMoveables(sources, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#toSource(Quantity[], Object[])
	 */
	@Override
	public Quantity<S>[] toSource(Quantity<T>[] targets, Object[] moveables) throws Exception {
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
	public Quantity<S> toSource(Quantity<T> target) throws Exception {
		return toSourceConverter.toSource(target);
	}

	@Override
	public Quantity<T> toTarget(Quantity<S> source) throws Exception {
		return calculateMoveablesConverter.toTarget(source);
	}

	@Override
	public List<String> getAcceptableSourceUnits() {
		return calculateMoveablesConverter.getAcceptableSourceUnits();
	}

	@Override
	public List<String> getAcceptableTargetUnits() {
		return toSourceConverter.getAcceptableTargetUnits();
	}

	@Override
	public boolean handlesStoT() {
		return calculateMoveablesConverter.handlesStoT();
	}

	@Override
	public boolean handlesTtoS() {
		return toSourceConverter.handlesTtoS();
	}

}
