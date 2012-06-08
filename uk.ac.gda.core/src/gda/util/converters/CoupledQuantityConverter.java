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
 * class used to test the concept used in CoupoleConverterHolder without the need to instantiate an ObjectServer
 */
final class CoupledQuantityConverter implements IQuantityConverter {
	private final IQuantityConverter sourceConverter, targetConverter;

	CoupledQuantityConverter(IQuantityConverter sourceConverter, IQuantityConverter targetConverter) {
		if (targetConverter == null || sourceConverter == null) {
			throw new IllegalArgumentException(
					"CoupledQuantityConverter.CoupledQuantityConverter: converters cannot be null");
		}
		this.targetConverter = targetConverter;
		this.sourceConverter = sourceConverter;

		if (!getSourceConverterTargetUnit().equals(getTargetConverterSourceUnit())) {
			throw new IllegalArgumentException("CoupledQuantityConverter.CoupledQuantityConverter: Error target unit ("
					+ getSourceConverterTargetUnit() + ")\n of converter (" + targetConverter.toString()
					+ ")\n does not match source units (" + getTargetConverterSourceUnit() + ")\n of converter ("
					+ sourceConverter.toString() + ")");
		}
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableSourceUnits() {
		return sourceConverter.getAcceptableSourceUnits();
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableTargetUnits() {
		return targetConverter.getAcceptableTargetUnits();
	}

	@Override
	public Quantity toSource(Quantity target) throws Exception {
		Quantity q = targetConverter.toSource(target);
		// check units are of the sort we expect for the conversion - else
		// convert to it first
		return sourceConverter.toSource(q);
	}

	@Override
	public Quantity toTarget(Quantity source) throws Exception {
		Quantity q = sourceConverter.toTarget(source);
		// check units are of the sort we expect for the conversion - else
		// convert to it first
		if (!q.getUnit().equals(getTargetConverterSourceUnit())) {
			throw new IllegalArgumentException("JEPQuantityConverter.ToSource: source units (" + q.getUnit()
					+ ") do not match acceptableUnits (" + getTargetConverterSourceUnit() + ")");
		}
		return targetConverter.toTarget(q);
	}

	private Unit<? extends Quantity> getSourceConverterTargetUnit() {
		return sourceConverter.getAcceptableTargetUnits().get(0);
	}

	private Unit<? extends Quantity> getTargetConverterSourceUnit() {
		return targetConverter.getAcceptableSourceUnits().get(0);
	}

	@Override
	public boolean sourceMinIsTargetMax() {
		return sourceConverter.sourceMinIsTargetMax() ^ targetConverter.sourceMinIsTargetMax();
	}
	@Override
	public boolean handlesStoT() {
		return sourceConverter.handlesStoT() && targetConverter.handlesStoT();
	}

	@Override
	public boolean handlesTtoS() {
		return sourceConverter.handlesTtoS() && targetConverter.handlesTtoS();
	}

	
	
}
