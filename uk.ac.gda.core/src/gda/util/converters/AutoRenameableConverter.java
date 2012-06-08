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

import gda.factory.Findable;
import gda.factory.Finder;
import gda.util.converters.util.ConverterNameProvider;

import java.util.ArrayList;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AutoRenameableConverter Class
 */
public class AutoRenameableConverter implements IReloadableQuantitiesConverter, Findable, IQuantitiesConverter , IQuantityConverter{

	private String name = null;

	private String providerName = "";

	private IReloadableQuantitiesConverter converter = null;

	// private ArrayList<RangeandConverterNameHolder> converterList = new
	// ArrayList<RangeandConverterNameHolder>();
	private boolean autoConversion = false;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(AutoRenameableConverter.class);

	private ConverterNameProvider provider;

	/**
	 * @param name 
	 * @param providerName
	 * @param autoConversion
	 */
	public AutoRenameableConverter(String name, String providerName, boolean autoConversion) {
		if (name == null || providerName == null) {
			throw new IllegalArgumentException("RenameableConverter. name or converterName cannot be null");
		}
		if (name.equals(providerName)) {
			throw new IllegalArgumentException("RenameableConverter. name and converterName cannot be the same");
		}
		this.name = name;
		setProviderName(providerName);
		this.autoConversion = autoConversion;
	}

	/**
	 * @param name
	 * @param providerName
	 */
	public AutoRenameableConverter(String name, String providerName) {
		this(name, providerName, false);
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
	 * @return converter name
	 */
	public String getConverterName() {
		return getProvider().getConverterName();
	}

	/**
	 * @param converterName
	 */
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

	/**
	 * @param providerName
	 */
	public void setProviderName(String providerName) {
		this.providerName = providerName;
		if (provider == null) {
			provider = (ConverterNameProvider) Finder.getInstance().find(providerName);
		}
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#calculateMoveables(org.jscience.physics.quantities.Quantity[],
	 *      java.lang.Object[])
	 */
	@Override
	public Quantity[] calculateMoveables(Quantity[] sources, Object[] moveables) throws Exception {

		// if(autoConversion)
		// selectConverter(sources,moveables);
		if (autoConversion) {
			setConverterName(getProvider().getConverterName(sources[0].getAmount()));
			reloadConverter();
		}
		System.out.println("the converterName is " + this.getConverterName());
		return getConverter().calculateMoveables(sources, moveables);
	}

	/*
	 * private void selectConverter(Quantity[] sources, Object[] moveables) { int count = converterList.size(); for(int
	 * i =0; i < count ; i++) { RangeandConverterNameHolder rcnh = converterList.get(i); if(sources[0].getAmount() >=
	 * rcnh.getRangeStart() && sources[0].getAmount() <=rcnh.getRangeStop()) {
	 * if(!rcnh.getConverterName().equals(converterName)) { setConverterName(rcnh.getConverterName());
	 * reloadConverter(); } break; } } }
	 */

	/**
	 * @see gda.util.converters.IQuantitiesConverter#toSource(org.jscience.physics.quantities.Quantity[],
	 *      java.lang.Object[])
	 */
	@Override
	public Quantity[] toSource(Quantity[] targets, Object[] moveables) throws Exception {
		return getConverter().toSource(targets, moveables);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#getAcceptableUnits()
	 */
	@Override
	public ArrayList<ArrayList<Unit<? extends Quantity>>> getAcceptableUnits() {
		return getConverter().getAcceptableUnits();
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#getAcceptableMoveableUnits()
	 */
	@Override
	public ArrayList<ArrayList<Unit<? extends Quantity>>> getAcceptableMoveableUnits() {
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
	 * @return ArrayList of Source units
	 */
	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableSourceUnits() {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).getAcceptableSourceUnits();
	}

	/**
	 * @see gda.util.converters.IQuantityConverter#getAcceptableTargetUnits()
	 * @return ArrayList of Target units
	 */
	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableTargetUnits() {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).getAcceptableTargetUnits();
	}

	/**
	 * @see gda.util.converters.IConverter#toSource(java.lang.Object)
	 * @param target
	 * @return Quantity
	 * @throws Exception
	 */
	@Override
	public Quantity toSource(Quantity target) throws Exception {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).toSource(target);
	}

	/**
	 * @see gda.util.converters.IConverter#toTarget(java.lang.Object)
	 * @param source
	 * @return Quantity
	 * @throws Exception
	 */
	@Override
	public Quantity toTarget(Quantity source) throws Exception {
		if (autoConversion) {
			setConverterName(getProvider().getConverterName(source.getAmount()));
			reloadConverter();
		}
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).toTarget(source);
	}

	/**
	 * @see gda.util.converters.IQuantitiesConverter#sourceMinIsTargetMax()
	 */
	@Override
	public boolean sourceMinIsTargetMax() {
		return getConverter().sourceMinIsTargetMax();
	}

	/**
	 * 
	 */
	public void enableAutoConversion() {
		autoConversion = true;
	}

	/**
	 * 
	 */
	public void disableAutoConversion() {
		autoConversion = false;
	}

	/*
	 * public void addConverter(RangeandConverterNameHolder rcnh) { converterList.add(rcnh); } public ArrayList<RangeandConverterNameHolder>
	 * getConverterList() { return converterList; }
	 */
	private ConverterNameProvider getProvider() {
		if (provider == null) {
			provider = (ConverterNameProvider) Finder.getInstance().find(providerName);
		}
		return provider;
	}

	/**
	 * @return provider name
	 */
	public String getProviderName() {
		return this.providerName;
	}

	/**
	 * @return autoConversion
	 */
	public boolean isAutoConversion() {
		return autoConversion;
	}

	/**
	 * @param autoConversion
	 */
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
