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
import java.util.Objects;

import javax.measure.Quantity;

import gda.factory.FindableBase;
import gda.util.converters.util.ConverterNameProvider;

/**
 * AutoRenameableConverter Class
 */
public class AutoRenameableConverter<S extends Quantity<S>, T extends Quantity<T>> extends FindableBase implements IReloadableQuantitiesConverter<S, T> {

	private IReloadableQuantitiesConverter<S, T> converter = null;

	private boolean autoConversion = false;

	private ConverterNameProvider provider;

	public AutoRenameableConverter(String name, ConverterNameProvider provider, boolean autoConversion) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(provider);
		super.setName(name);
		this.provider = provider;
		this.autoConversion = autoConversion;
	}

	public AutoRenameableConverter(String name, ConverterNameProvider provider) {
		this(name, provider, false);
	}

	@Override
	public void reloadConverter() {
		getConverter().reloadConverter();
	}

	private synchronized IReloadableQuantitiesConverter<S, T> getConverter() {
		if (converter == null) {
			converter = CoupledConverterHolder.FindReloadableQuantitiesConverter(getProvider().getConverterName());
		}
		return converter;
	}

	@Override
	public void setName(String name) {
		// I need to support the function but will not do anything with the name
		// as I do not want to allow the name to be changed after construction
		throw new IllegalArgumentException("RenameableConverter.setName() : Error this should not be called");
	}

	public String getConverterName() {
		return getProvider().getConverterName();
	}

	public void setConverterName(String converterName) {
		if (getName().equals(converterName)) {
			throw new IllegalArgumentException("RenameableConverter. name and converterName cannot be the same");
		}
		final IReloadableQuantitiesConverter<S, T> newConverter = CoupledConverterHolder.FindReloadableQuantitiesConverter(converterName);
		if (converter != null) {
			LookupTableConverterHolder.CheckUnitsAreEqual(converter, newConverter);
		}
		converter = newConverter;
		getProvider().setConverterName(converterName);
	}

	@Override
	public Quantity<T>[] calculateMoveables(Quantity<S>[] sources, Object[] moveables) throws Exception {
		if (autoConversion) {
			setConverterName(getProvider().getConverterName(sources[0].getValue().doubleValue()));
			reloadConverter();
		}
		return getConverter().calculateMoveables(sources, moveables);
	}

	@Override
	public Quantity<S>[] toSource(Quantity<T>[] targets, Object[] moveables) throws Exception {
		return getConverter().toSource(targets, moveables);
	}

	@Override
	public List<List<String>> getAcceptableUnits() {
		return getConverter().getAcceptableUnits();
	}

	@Override
	public List<List<String>> getAcceptableMoveableUnits() {
		return getConverter().getAcceptableMoveableUnits();
	}

	@Override
	public String toString() {
		// Do not call getConverter as toString should not change the state of
		// the class
		return "RenameableConverter using converter " + getConverterName() + ". Constructed converter is "
				+ ((converter != null) ? converter.toString() : " not yet loaded");
	}

	@Override
	public List<String> getAcceptableSourceUnits() {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).getAcceptableSourceUnits();
	}

	@Override
	public List<String> getAcceptableTargetUnits() {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).getAcceptableTargetUnits();
	}

	@Override
	public Quantity<S> toSource(Quantity<T> target) throws Exception {
		final IQuantityConverter<S, T> quantityConverter = CoupledConverterHolder.getIQuantityConverter(getConverter());
		return quantityConverter.toSource(target);
	}

	@Override
	public Quantity<T> toTarget(Quantity<S> source) throws Exception {
		if (autoConversion) {
			setConverterName(getProvider().getConverterName(source.getValue().doubleValue()));
			reloadConverter();
		}
		final IQuantityConverter<S, T> quantityConverter = CoupledConverterHolder.getIQuantityConverter(getConverter());
		return quantityConverter.toTarget(source);
	}

	@Override
	public boolean sourceMinIsTargetMax() {
		return getConverter().sourceMinIsTargetMax();
	}

	public void enableAutoConversion() {
		autoConversion = true;
	}

	public void disableAutoConversion() {
		autoConversion = false;
	}

	private ConverterNameProvider getProvider() {
		return provider;
	}

	public boolean isAutoConversion() {
		return autoConversion;
	}

	public void setAutoConversion(boolean autoConversion) {
		this.autoConversion = autoConversion;
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
