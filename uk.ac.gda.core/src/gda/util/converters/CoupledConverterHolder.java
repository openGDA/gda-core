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

import gda.factory.FindableBase;
import gda.factory.Finder;

/**
 * Class that can be used to create a converter made up of two other converters in series. An occasion where this is
 * useful is if one can convert from a value of Source to Target using a combination of a conversion using a Java
 * Expression Parser representing the conversion expected from geometric design of a piece of equipment followed by a
 * conversion using a lookup table to correct for manufacturing errors.
 * <p>
 * An instance of this object is to be created by the Castor system within the ObjectServer due to presence in the main
 * xml file of content of the from:
 * <p>
 * <preset>&lt;CoupledConverter name="BeamLineEnergy_DCM_Perp_converter"
 * sourceConverterName="BeamLineEnergy_DCM_Perp_converter1" targetConverterName="BeamLineEnergy_DCM_Perp_converter2"
 * /&gt;</preset>
 * <p>
 * The sourceConverterName value is the name of a findable implementor of IReloadableQuantitiesConverter to be used to
 * perform the first conversion between the Source and an intermediate quantity.
 * <p>
 * The targetConverterName value is the name of a findable implementor of IReloadableQuantitiesConverter to be used to
 * convert between the intermediate quantity and the Target.
 * <p>
 * The object implements findable so it can be found via the Jython script using the command
 * <p>
 * <code>converter = finder.find("BeamLineEnergy_DCM_Perp_converter")</code>
 * <p>
 * The object implements IReloadableQuantitiesConverter so that the dependent converters can be re-loaded using the
 * command:
 * <p>
 * <code>finder.find("BeamLineEnergy_DCM_Perp_converter").ReloadConverter</code>
 * <p>
 * The object implements IQuantitiesConverter so that the object can be referenced by CombinedDOF.
 */
public final class CoupledConverterHolder<S extends Quantity<S>, T extends Quantity<T>, I extends Quantity<I>> extends FindableBase implements IReloadableQuantitiesConverter<S, T> {
	private String sourceConverterName;
	private String targetConverterName;

	private CoupledQuantitiesConverter<S, T, I> converter = null;

	private IReloadableQuantitiesConverter<S, I> sourceConverter = null;
	private IReloadableQuantitiesConverter<I, T> targetConverter = null;

	/**
	 * @param name
	 *            The name of the converter by which the object can be found using the Finder
	 * @param sourceConverterName
	 *            The name of a findable implementor of IReloadableQuantitiesConverter to be used to perform the first
	 *            conversion between the Source and an intermediate quantity.
	 * @param targetConverterName
	 *            The name of a findable implementor of IReloadableQuantitiesConverter to be used to convert between the
	 *            intermediate quantity and the Target.
	 */
	public CoupledConverterHolder(String name, String sourceConverterName, String targetConverterName) {
		super.setName(name);
		this.sourceConverterName = sourceConverterName;
		this.targetConverterName = targetConverterName;
	}

	/**
	 * Creates a coupled converter holder which uses the specified source and target converters.
	 *
	 * @param name the converter name
	 * @param sourceConverter the source converter
	 * @param targetConverter the target converter
	 */
	public CoupledConverterHolder(String name, IReloadableQuantitiesConverter<S, I> sourceConverter, IReloadableQuantitiesConverter<I, T> targetConverter) {
		super.setName(name);
		this.sourceConverter = sourceConverter;
		this.targetConverter = targetConverter;
	}

	/**
	 * @return sourceConverterName
	 */
	public String getSourceConverterName() {
		return sourceConverterName;
	}

	/**
	 * @return targetConverterName
	 */
	public String getTargetConverterName() {
		return targetConverterName;
	}

	@Override
	public void setName(String name) {
		// I need to support the function but will not do anything with the name
		// as I do not want to allow the name to be changed after construction
		// this.name = name;
		throw new IllegalArgumentException("CoupledConverterHolder.setName() : Error this should not be called");
	}

	/*
	 * Re-builds the whole converter by calling on the child converters to re-build themselves. If the child is a
	 * reading a lookup table it is expected that this action will cause the lookup table to be re-read.
	 */
	@Override
	public void reloadConverter() {
		// To reduce race conditions create a brand new converter rather than
		// change existing which may be already being accessed on other threads
		if (sourceConverter == null) {
			sourceConverter = FindReloadableQuantitiesConverter(sourceConverterName);
		}
		if (targetConverter == null) {
			targetConverter = FindReloadableQuantitiesConverter(targetConverterName);
		}
		sourceConverter.reloadConverter();
		targetConverter.reloadConverter();
		converter = new CoupledQuantitiesConverter<>(sourceConverter, targetConverter);
	}

	private synchronized CoupledQuantitiesConverter<S, T, I> getConverter() {
		if (converter == null) {
			reloadConverter();
		}
		return converter;
	}

	@Override
	public Quantity<T>[] calculateMoveables(Quantity<S>[] sources, Object[] moveables) throws Exception {
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

	/**
	 * @param converterName
	 * @return IReloadableQuantitiesConverter
	 */
	public static <Q extends Quantity<Q>, R extends Quantity<R>> IReloadableQuantitiesConverter<Q, R> FindReloadableQuantitiesConverter(String converterName) {
		if (converterName == null || converterName.length() == 0) {
			throw new IllegalArgumentException("CoupledQuantitiesConverterHolder.FindReloadableQuantitiesConverter : "
					+ converterName + " is null or empty");
		}
		return Finder.find(converterName.trim());
	}

	/**
	 * @param converterName
	 * @return IQuantitiesConverter
	 */
	public static <Q extends Quantity<Q>, R extends Quantity<R>> IQuantitiesConverter<Q, R> FindQuantitiesConverter(String converterName) {
		if (converterName == null || converterName.length() == 0) {
			throw new IllegalArgumentException("CoupledQuantitiesConverterHolder.FindQuantitiesConverter : "
					+ converterName + " is null or empty");
		}
		return Finder.find(converterName.trim());
	}

	@Override
	public String toString() {
		// Do not call getConverter as toString should not change the state of the class
		return "CoupledQuantitiesConverter using source converter name " + getSourceConverterName()
				+ " and target converter " + getTargetConverterName() + ". Constructed converter is "
				+ ((converter != null) ? converter.toString() : " - converter not yet loaded");
	}

	/**
	 * @param obj
	 * @return IQuantityConverter
	 */
	@SuppressWarnings("unchecked")
	public static <Q extends Quantity<Q>, R extends Quantity<R>> IQuantityConverter<Q, R> getIQuantityConverter(Object obj) {
		if (!(obj instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverterHolder.getIQuantityConverter: converter does not support IQuantityConverter ");
		}
		return (IQuantityConverter<Q, R>) obj;
	}

	@Override
	public List<String> getAcceptableSourceUnits() {
		return getIQuantityConverter(getConverter()).getAcceptableSourceUnits();
	}

	@Override
	public List<String> getAcceptableTargetUnits() {
		return getIQuantityConverter(getConverter()).getAcceptableTargetUnits();
	}

	@Override
	public Quantity<S> toSource(Quantity<T> target) throws Exception {
		final IQuantityConverter<S, T> quantityConverter = getIQuantityConverter(getConverter());
		return quantityConverter.toSource(target);
	}

	@Override
	public Quantity<T> toTarget(Quantity<S> source) throws Exception {
		final IQuantityConverter<S, T> quantityConverter = getIQuantityConverter(getConverter());
		return quantityConverter.toTarget(source);
	}

	@Override
	public boolean sourceMinIsTargetMax() {
		return getConverter().sourceMinIsTargetMax();
	}

	@Override
	public boolean handlesStoT() {
		return getIQuantityConverter(getConverter()).handlesStoT();
	}

	@Override
	public boolean handlesTtoS() {
		return getIQuantityConverter(getConverter()).handlesTtoS();
	}

}
