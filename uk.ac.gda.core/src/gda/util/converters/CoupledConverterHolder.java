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

import java.util.ArrayList;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

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
public final class CoupledConverterHolder implements IReloadableQuantitiesConverter, Findable, IQuantityConverter

{
	private String sourceConverterName, targetConverterName;

	private final String name;

	private IQuantitiesConverter converter = null;

	private IReloadableQuantitiesConverter sourceConverter = null, targetConverter = null;

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
		this.name = name;
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
	public CoupledConverterHolder(String name, IReloadableQuantitiesConverter sourceConverter, IReloadableQuantitiesConverter targetConverter) {
		this.name = name;
		this.sourceConverter = sourceConverter;
		this.targetConverter = targetConverter;
	}

	// Castor functions
	@Override
	public String getName() {
		return name;
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
		converter = new CoupledQuantitiesConverter(sourceConverter, targetConverter);
	}

	private synchronized IQuantitiesConverter getConverter() {
		if (converter == null) {
			reloadConverter();
		}
		return converter;
	}

	@Override
	public Quantity[] calculateMoveables(Quantity[] sources, Object[] moveables) throws Exception {
		return getConverter().calculateMoveables(sources, moveables);
	}

	@Override
	public Quantity[] toSource(Quantity[] targets, Object[] moveables) throws Exception {
		return getConverter().toSource(targets, moveables);
	}

	@Override
	public ArrayList<ArrayList<Unit<? extends Quantity>>> getAcceptableUnits() {
		return getConverter().getAcceptableUnits();
	}

	@Override
	public ArrayList<ArrayList<Unit<? extends Quantity>>> getAcceptableMoveableUnits() {
		return getConverter().getAcceptableMoveableUnits();
	}

	/**
	 * @param converterName
	 * @return IReloadableQuantitiesConverter
	 */
	static public IReloadableQuantitiesConverter FindReloadableQuantitiesConverter(String converterName) {
		if (converterName == null || converterName.equals("")) {
			throw new IllegalArgumentException("CoupledQuantitiesConverterHolder.FindReloadableQuantitiesConverter : "
					+ converterName + " is null or empty");
		}
		Findable findable = Finder.getInstance().find(converterName);
		if (findable == null || !(findable instanceof IReloadableQuantitiesConverter)) {
			throw new IllegalArgumentException("CoupledQuantitiesConverterHolder.FindReloadableQuantitiesConverter: "
					+ converterName + " is not a findable IReloadableQuantitiesConverter");
		}
		return (IReloadableQuantitiesConverter) findable;
	}

	/**
	 * @param converterName
	 * @return IQuantitiesConverter
	 */
	static public IQuantitiesConverter FindQuantitiesConverter(String converterName) {
		if (converterName == null || converterName.equals("")) {
			throw new IllegalArgumentException("CoupledQuantitiesConverterHolder.FindQuantitiesConverter : "
					+ converterName + " is null or empty");
		}
		Findable findable = Finder.getInstance().find(converterName.trim());
		if (findable == null || !(findable instanceof IQuantitiesConverter)) {
			throw new IllegalArgumentException("CoupledQuantitiesConverterHolder.FindQuantitiesConverter: "
					+ converterName + " is not a findable IReloadableQuantitiesConverter");
		}
		return (IReloadableQuantitiesConverter) findable;
	}

	@Override
	public String toString() {
		// Do not call getConverter as toString should not change the state of
		// the class
		return "CoupledQuantitiesConverter using source converter name " + getSourceConverterName()
				+ " and target converter " + getTargetConverterName() + ". Constructed converter is "
				+ ((converter != null) ? converter.toString() : " - converter not yet loaded");
	}

	/**
	 * @param obj
	 * @return IQuantityConverter
	 */
	public static IQuantityConverter getIQuantityConverter(Object obj) {
		if (!(obj instanceof IQuantityConverter)) {
			throw new IllegalArgumentException(
					"CoupledQuantitiesConverterHolder.getIQuantityConverter: converter does not support IQuantityConverter ");
		}
		return (IQuantityConverter) obj;
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableSourceUnits() {
		return getIQuantityConverter(getConverter()).getAcceptableSourceUnits();
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableTargetUnits() {
		return getIQuantityConverter(getConverter()).getAcceptableTargetUnits();
	}

	@Override
	public Quantity toSource(Quantity target) throws Exception {
		return getIQuantityConverter(getConverter()).toSource(target);
	}

	@Override
	public Quantity toTarget(Quantity source) throws Exception {
		return getIQuantityConverter(getConverter()).toTarget(source);
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
