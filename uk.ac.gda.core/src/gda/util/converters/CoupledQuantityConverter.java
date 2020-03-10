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
import javax.measure.Unit;

import gda.util.QuantityFactory;

/**
 * class used to test the concept used in CoupoleConverterHolder without the need to instantiate an ObjectServer
 */
final class CoupledQuantityConverter<S extends Quantity<S>, T extends Quantity<T>, Q extends Quantity<Q>> implements IQuantityConverter<S, T> {
	/**
	 * Converter to be used to convert between Source and an intermediate quantity
	 */
	private final IQuantityConverter<S, Q> sourceConverter;

	/**
	 * Converter to be used to convert between an intermediate quantity and Target
	 */
	private final IQuantityConverter<Q, T> targetConverter;

	CoupledQuantityConverter(IQuantityConverter<S, Q> sourceConverter, IQuantityConverter<Q, T> targetConverter) {
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
	public List<String> getAcceptableSourceUnits() {
		return sourceConverter.getAcceptableSourceUnits();
	}

	@Override
	public List<String> getAcceptableTargetUnits() {
		return targetConverter.getAcceptableTargetUnits();
	}

	@Override
	public Quantity<S> toSource(Quantity<T> target) throws Exception {
		final Quantity<Q> q = targetConverter.toSource(target);
		// check units are of the sort we expect for the conversion - else convert to it first
		return sourceConverter.toSource(q);
	}

	@Override
	public Quantity<T> toTarget(Quantity<S> source) throws Exception {
		final Quantity<Q> q = sourceConverter.toTarget(source);
		// check units are of the sort we expect for the conversion - else convert to it first
		final Unit<T> targetUnit = QuantityFactory.createUnitFromString(getTargetConverterSourceUnit());
		if (!q.getUnit().equals(targetUnit)) {
			throw new IllegalArgumentException("JEPQuantityConverter.ToSource: source units (" + q.getUnit()
					+ ") do not match acceptableUnits (" + targetUnit + ")");
		}
		return targetConverter.toTarget(q);
	}

	private String getSourceConverterTargetUnit() {
		return sourceConverter.getAcceptableTargetUnits().get(0);
	}

	private String getTargetConverterSourceUnit() {
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
