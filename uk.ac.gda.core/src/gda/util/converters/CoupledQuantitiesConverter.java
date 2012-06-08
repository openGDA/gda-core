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

final class CoupledQuantitiesConverter implements IQuantitiesConverter , IQuantityConverter{
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
	public Quantity[] calculateMoveables(Quantity[] sources, Object[] moveables) throws Exception {
		Quantity[] q = sourceConverter.calculateMoveables(sources, moveables);
		// do not check units as this is done by the actual converter
		return targetConverter.calculateMoveables(q, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#toSource(org.jscience.physics.quantities.Quantity[],
	 *      java.lang.Object[])
	 */
	@Override
	public Quantity[] toSource(Quantity[] targets, Object[] moveables) throws Exception {
		Quantity[] q = targetConverter.toSource(targets, moveables);
		// do not check units as this is done by the actual converter
		return sourceConverter.toSource(q, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#getAcceptableUnits()
	 */
	@Override
	public ArrayList<ArrayList<Unit<? extends Quantity>>> getAcceptableUnits() {
		return sourceConverter.getAcceptableUnits();
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#getAcceptableMoveableUnits()
	 */
	@Override
	public ArrayList<ArrayList<Unit<? extends Quantity>>> getAcceptableMoveableUnits() {
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
	public Quantity toSource(Quantity target) throws Exception {
		if (!(targetConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.toSource: targetConverter does not support IQuantityConverter ");
		}
		Quantity intermediate = ((IQuantityConverter) targetConverter).toSource(target);	
		if (!(sourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.toSource: sourceConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) sourceConverter).toSource(intermediate);
	}

	@Override
	public Quantity toTarget(Quantity source) throws Exception {
		if (!(sourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.toTarget: sourceConverter does not support IQuantityConverter ");
		}
		Quantity intermediate = ((IQuantityConverter) sourceConverter).toTarget(source);	
		if (!(targetConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.toTarget: targetConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) targetConverter).toTarget(intermediate);
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableSourceUnits() {
		if (!(sourceConverter instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverter.getAcceptableSourceUnits: sourceConverter does not support IQuantityConverter ");
		}
		return ((IQuantityConverter) sourceConverter).getAcceptableSourceUnits();
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableTargetUnits() {
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