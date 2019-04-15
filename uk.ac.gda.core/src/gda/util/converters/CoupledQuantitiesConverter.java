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

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

final class CoupledQuantitiesConverter implements IQuantitiesConverter, IQuantityConverter {
	private final IQuantitiesConverter sourceConverter, targetConverter;

	/**
	 * @param sourceConverter
	 *            Converter to be used to convert between Source and an intermediate quantity
	 * @param targetConverter
	 *            Converter to be used to convert between an intermediate quantity and Target
	 */
	CoupledQuantitiesConverter(IQuantitiesConverter sourceConverter, IQuantitiesConverter targetConverter) {
		if (targetConverter == null || sourceConverter == null) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.CoupledQuantityConverter: converters cannot be null");
		}
		this.targetConverter = targetConverter;
		this.sourceConverter = sourceConverter;
	}

	@Override
	public Amount<? extends Quantity>[] calculateMoveables(Amount<? extends Quantity>[] sources, Object[] moveables) throws Exception {
		Amount<? extends Quantity>[] q = sourceConverter.calculateMoveables(sources, moveables);
		// do not check units as this is done by the actual converter
		return targetConverter.calculateMoveables(q, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#toSource(Amount[], Object[])
	 */
	@Override
	public Amount<? extends Quantity>[] toSource(Amount<? extends Quantity>[] targets, Object[] moveables) throws Exception {
		Amount<? extends Quantity>[] q = targetConverter.toSource(targets, moveables);
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
	public Amount<? extends Quantity> toSource(Amount<? extends Quantity> target) throws Exception {
		if (!(targetConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.toSource: targetConverter does not support IQuantityConverter ");
		}
		Amount<? extends Quantity> intermediate = ((IQuantityConverter) targetConverter).toSource(target);
		if (!(sourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.toSource: sourceConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) sourceConverter).toSource(intermediate);
	}

	@Override
	public Amount<? extends Quantity> toTarget(Amount<? extends Quantity> source) throws Exception {
		if (!(sourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.toTarget: sourceConverter does not support IQuantityConverter ");
		}
		Amount<? extends Quantity> intermediate = ((IQuantityConverter) sourceConverter).toTarget(source);
		if (!(targetConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.toTarget: targetConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) targetConverter).toTarget(intermediate);
	}

	@Override
	public List<String> getAcceptableSourceUnits() {
		if (!(sourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.getAcceptableSourceUnits: sourceConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) sourceConverter).getAcceptableSourceUnits();
	}

	@Override
	public List<String> getAcceptableTargetUnits() {
		if (!(targetConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"getAcceptableTargetUnits: targetConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) targetConverter).getAcceptableTargetUnits();
	}
	@Override
	public boolean handlesStoT() {
		if (!(sourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.getAcceptableSourceUnits: sourceConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) sourceConverter).handlesStoT();
	}

	@Override
	public boolean handlesTtoS() {
		if (!(targetConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"getAcceptableTargetUnits: targetConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) targetConverter).handlesTtoS();
	}

}