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

final class CoupledQuantitiesConverter<S extends Quantity<S>, T extends Quantity<T>, I extends Quantity<I>> implements IQuantitiesConverter<S, T>, IQuantityConverter<S, T> {
	/**
	 * Converter to be used to convert between Source and an intermediate quantity
	 */
	private final IQuantitiesConverter<S, I> sourceConverter;

	/**
	 * Converter to be used to convert between an intermediate quantity and Target
	 */
	private final IQuantitiesConverter<I, T> targetConverter;

	CoupledQuantitiesConverter(IQuantitiesConverter<S, I> sourceConverter, IQuantitiesConverter<I, T> targetConverter) {
		if (targetConverter == null || sourceConverter == null) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.CoupledQuantityConverter: converters cannot be null");
		}
		this.targetConverter = targetConverter;
		this.sourceConverter = sourceConverter;
	}

	@Override
	public Quantity<T>[] calculateMoveables(Quantity<S>[] sources, Object[] moveables) throws Exception {
		final Quantity<I>[] q = sourceConverter.calculateMoveables(sources, moveables);
		// do not check units as this is done by the actual converter
		return targetConverter.calculateMoveables(q, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#toSource(Quantity[], Object[])
	 */
	@Override
	public Quantity<S>[] toSource(Quantity<T>[] targets, Object[] moveables) throws Exception {
		Quantity<I>[] q = targetConverter.toSource(targets, moveables);
		// do not check units as this is done by the actual converter
		return sourceConverter.toSource(q, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#getAcceptableUnits()
	 */
	@Override
	public List<List<String>> getAcceptableUnits() {
		return sourceConverter.getAcceptableUnits();
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#getAcceptableMoveableUnits()
	 */
	@Override
	public List<List<String>> getAcceptableMoveableUnits() {
		return targetConverter.getAcceptableMoveableUnits();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CoupledQuantitiesConverter using source converter " + sourceConverter.toString()
				+ " and target converter " + targetConverter.toString();
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#sourceMinIsTargetMax()
	 */
	@Override
	public boolean sourceMinIsTargetMax() {
		return sourceConverter.sourceMinIsTargetMax() ^ targetConverter.sourceMinIsTargetMax();
	}

	@Override
	public Quantity<S> toSource(Quantity<T> target) throws Exception {
		if (!(targetConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.toSource: targetConverter does not support IQuantityConverter ");
		}
		if (!(sourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.toSource: sourceConverter does not support IQuantityConverter ");
		}

		@SuppressWarnings("unchecked")
		final IQuantityConverter<S, I> sourceConv = (IQuantityConverter<S, I>) sourceConverter;
		@SuppressWarnings("unchecked")
		final IQuantityConverter<I, T> targetConv = (IQuantityConverter<I, T>) targetConverter;

		final Quantity<I> intermediate = targetConv.toSource(target);
		return sourceConv.toSource(intermediate);
	}

	@Override
	public Quantity<T> toTarget(Quantity<S> source) throws Exception {
		if (!(targetConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.toSource: targetConverter does not support IQuantityConverter ");
		}
		if (!(sourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.toSource: sourceConverter does not support IQuantityConverter ");
		}

		@SuppressWarnings("unchecked")
		final IQuantityConverter<S, I> sourceConv = (IQuantityConverter<S, I>) sourceConverter;
		@SuppressWarnings("unchecked")
		final IQuantityConverter<I, T> targetConv = (IQuantityConverter<I, T>) targetConverter;

		final Quantity<I> intermediate = sourceConv.toTarget(source);
		return targetConv.toTarget(intermediate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAcceptableSourceUnits() {
		if (!(sourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.getAcceptableSourceUnits: sourceConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter<S, I>) sourceConverter).getAcceptableSourceUnits();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAcceptableTargetUnits() {
		if (!(targetConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"getAcceptableTargetUnits: targetConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter<I, T>) targetConverter).getAcceptableTargetUnits();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean handlesStoT() {
		if (!(sourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.getAcceptableSourceUnits: sourceConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter<S, I>) sourceConverter).handlesStoT();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean handlesTtoS() {
		if (!(targetConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"getAcceptableTargetUnits: targetConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter<I, T>) targetConverter).handlesTtoS();
	}

}