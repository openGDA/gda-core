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

import org.jscience.physics.quantities.Quantity;

import gda.util.converters.util.ConverterNameProvider;

/**
 * AutoRenameableConverter Class
 */
public class AutoRenameableConverter implements IReloadableQuantitiesConverter, IQuantityConverter {

	private String name = null;

	private IReloadableQuantitiesConverter converter = null;

	private boolean autoConversion = false;

	private ConverterNameProvider provider;

	public AutoRenameableConverter(String name, ConverterNameProvider provider, boolean autoConversion) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(provider);
		this.name = name;
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

	private synchronized IReloadableQuantitiesConverter getConverter() {
		if (converter == null) {
			converter = CoupledConverterHolder.FindReloadableQuantitiesConverter(getProvider().getConverterName());
		}
		return converter;
	}

	@Override
	public String getName() {
		return name;
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
		if (name.equals(converterName)) {
			throw new IllegalArgumentException("RenameableConverter. name and converterName cannot be the same");
		}
		IReloadableQuantitiesConverter newConverter = CoupledConverterHolder
				.FindReloadableQuantitiesConverter(converterName);
		if (converter != null) {
			LookupTableConverterHolder.CheckUnitsAreEqual(converter, newConverter);
		}
		converter = newConverter;
		getProvider().setConverterName(converterName);
	}

	@Override
	public Quantity[] calculateMoveables(Quantity[] sources, Object[] moveables) throws Exception {

		if (autoConversion) {
			setConverterName(getProvider().getConverterName(sources[0].getAmount()));
			reloadConverter();
		}
		return getConverter().calculateMoveables(sources, moveables);
	}

	@Override
	public Quantity[] toSource(Quantity[] targets, Object[] moveables) throws Exception {
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
		return "RenameableConverter using converter " + getConverterName().toString() + ". Constructed converter is "
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
	public Quantity toSource(Quantity target) throws Exception {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).toSource(target);
	}

	@Override
	public Quantity toTarget(Quantity source) throws Exception {
		if (autoConversion) {
			setConverterName(getProvider().getConverterName(source.getAmount()));
			reloadConverter();
		}
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).toTarget(source);
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
