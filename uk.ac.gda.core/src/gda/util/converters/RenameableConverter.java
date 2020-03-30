/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import gda.factory.FindableBase;

/**
 * RenameableConverter Class
 */
public class RenameableConverter<S extends Quantity<S>, T extends Quantity<T>> extends FindableBase
		implements IReloadableQuantitiesConverter<S, T> {
	private String converterName = "";

	private IReloadableQuantitiesConverter<S, T> converter = null;

	/**
	 * @param name
	 * @param converterName
	 */
	public RenameableConverter(String name, String converterName) {
		if (name == null || converterName == null) {
			throw new IllegalArgumentException("RenameableConverter. name or converterName cannot be null");
		}
		if (name.equals(converterName)) {
			throw new IllegalArgumentException("RenameableConverter. name and converterName cannot be the same");
		}
		super.setName(name);
		this.converterName = converterName;
	}

	@Override
	public void reloadConverter() {
		getConverter().reloadConverter();
	}

	private synchronized IReloadableQuantitiesConverter<S, T> getConverter() {
		if (converter == null) {
			converter = CoupledConverterHolder.FindReloadableQuantitiesConverter(converterName);
		}
		return converter;
	}

	/**
	 * @see gda.factory.Findable#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		// I need to support the function but will not do anything with the name
		// as I do not want to allow the name to be changed after construction
		// this.name = name;
		throw new IllegalArgumentException("RenameableConverter.setName() : Error this should not be called");
	}

	/**
	 * @return converterName
	 */
	public String getConverterName() {
		return converterName;
	}

	/**
	 * @param converterName
	 */
	public void setConverterName(String converterName) {
		if (getName().equals(converterName)) {
			throw new IllegalArgumentException("RenameableConverter. name and converterName cannot be the same");
		}
		final IReloadableQuantitiesConverter<S, T> newConverter = CoupledConverterHolder.FindReloadableQuantitiesConverter(converterName);
		if (converter != null) {
			ConverterUtils.checkUnitsAreEqual(converter, newConverter);
		}
		converter = newConverter;
		this.converterName = converterName;
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#calculateMoveables(Quantity[], Object[])
	 */
	@Override
	public Quantity<T>[] calculateMoveables(Quantity<S>[] sources, Object[] moveables) throws Exception {
		return getConverter().calculateMoveables(sources, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#toSource(Quantity[], Object[])
	 */
	@Override
	public Quantity<S>[] toSource(Quantity<T>[] targets, Object[] moveables) throws Exception {
		return getConverter().toSource(targets, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#getAcceptableUnits()
	 */
	@Override
	public List<List<String>> getAcceptableUnits() {
		return getConverter().getAcceptableUnits();
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#getAcceptableMoveableUnits()
	 */
	@Override
	public List<List<String>> getAcceptableMoveableUnits() {
		return getConverter().getAcceptableMoveableUnits();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// Do not call getConverter as toString should not change the state of
		// the class
		return "RenameableConverter using converter " + getConverterName().toString() + ". Constructed converter is "
				+ ((converter != null) ? converter.toString() : " not yet loaded");
	}

	/**
	 * @see gda.util.converters.IQuantityConverter#getAcceptableSourceUnits()
	 */
	@Override
	public List<String> getAcceptableSourceUnits() {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).getAcceptableSourceUnits();
	}

	/**
	 * @see gda.util.converters.IQuantityConverter#getAcceptableTargetUnits()
	 */
	@Override
	public List<String> getAcceptableTargetUnits() {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).getAcceptableTargetUnits();
	}

	@Override
	public Quantity<S> toSource(Quantity<T> target) throws Exception {
		final IQuantityConverter<S, T> conv = (CoupledConverterHolder.getIQuantityConverter(getConverter()));
		return conv.toSource(target);
	}

	@Override
	public Quantity<T> toTarget(Quantity<S> source) throws Exception {
		final IQuantityConverter<S, T> conv = (CoupledConverterHolder.getIQuantityConverter(getConverter()));
		return conv.toTarget(source);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#sourceMinIsTargetMax()
	 */
	@Override
	public boolean sourceMinIsTargetMax() {
		return getConverter().sourceMinIsTargetMax();
	}

	@Override
	public boolean handlesStoT() {
		return true;
	}

	@Override
	public boolean handlesTtoS() {
		return true;
	}
}
