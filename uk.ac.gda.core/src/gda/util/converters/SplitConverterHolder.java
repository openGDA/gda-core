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

import java.util.ArrayList;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * Class that can be used to create a converter made up of two other converters in such a way that one converter is used
 * to convert from Source to Target and another used to convert from Target to Source. An occasion where this is useful
 * is if one can convert from a value of Source to a single value of Target using a lookup table but not the other way
 * using the same table.
 * <p>
 * An instance of this object is to be created by the Castor system within the ObjectServer due to presence in the main
 * xml file of content of the from:
 * <p>
 * <preset>&lt;SplitConverter name="BeamLineEnergy_Undulator_Gap_converter"
 * toSourceConverterName="BeamLineEnergy_Undulator_fromGap"
 * calculateMoveablesConverterName="BeamLineEnergy_Undulator_toGap" /&gt;</preset>
 * <p>
 * The toSourceConverterName value is the name of a findable implementor of IReloadableQuantitiesConverter to be used to
 * convert from Target to Source.
 * <p>
 * The calculateMoveablesConverterName value is the name of a findable implementor of IReloadableQuantitiesConverter to
 * be used to convert from Source to Target.
 * <p>
 * The object implements findable so it can be found via the Jython script using the command
 * <p>
 * <code>converter = finder.find("BeamLineEnergy_Undulator_Gap_converter")</code>
 * <p>
 * The object implements IReloadableQuantitiesConverter so that the dependent converters can be re-loaded using the
 * command:
 * <p>
 * <code>finder.find("BeamLineEnergy_Undulator_Gap_converter").ReloadConverter</code>
 * <p>
 * The object implements IQuantitiesConverter so that the object can be referenced by CombinedDOF.
 */
public final class SplitConverterHolder implements IReloadableQuantitiesConverter, Findable, IQuantityConverter

{

	private final String toSourceConverterName, calculateMoveablesConverterName;

	private final String name;

	private IQuantitiesConverter converter = null;

	private IReloadableQuantitiesConverter toSourceConverter = null, calculateMoveablesConverter = null;

	/**
	 * @param name
	 *            The name of the converter by which the object can be found using the Finder
	 * @param toSourceConverterName
	 *            The name of a findable implementor of IReloadableQuantitiesConverter to be used to convert from Target
	 *            to Source.
	 * @param calculateMoveablesConverterName
	 *            the name of a findable implementor of IReloadableQuantitiesConverter to be used to convert from Source
	 *            to Target.
	 */
	public SplitConverterHolder(String name, String toSourceConverterName, String calculateMoveablesConverterName) {
		this.name = name;
		this.toSourceConverterName = toSourceConverterName;
		this.calculateMoveablesConverterName = calculateMoveablesConverterName;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getToSourceConverterName() {
		return toSourceConverterName;
	}

	public String getCalculateMoveablesConverterName() {
		return calculateMoveablesConverterName;
	}

	@Override
	public void setName(String name) {
		// I need to support the function but will not do anything with the name
		// as I do not want to allow the name to be changed after construction
		// this.name = name;
		throw new IllegalArgumentException("SplitConverterHolder.setName() : Error this should not be called");
	}

	/**
	 * Re-builds the whole converter by calling on the child converters to re-build themselves. If the child is a
	 * reading a lookup table it is expected that this action will cause the lookup table to be re-read.
	 */
	@Override
	public void reloadConverter() {
		// To reduce race conditions create a brand new converter rather than
		// change existing which may be already being accessed on other threads
		if (toSourceConverter == null) {
			toSourceConverter = CoupledConverterHolder.FindReloadableQuantitiesConverter(getToSourceConverterName());
		}
		if (calculateMoveablesConverter == null) {
			calculateMoveablesConverter = CoupledConverterHolder
					.FindReloadableQuantitiesConverter(getCalculateMoveablesConverterName());
		}
		toSourceConverter.reloadConverter();
		calculateMoveablesConverter.reloadConverter();
		converter = new SplitQuantitiesConverter(toSourceConverter, calculateMoveablesConverter);
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

	@Override
	public String toString() {
		// Do not call getConverter as toString should not change the state of
		// the class
		return "SplitConverterHolder using toSourceConverterName " + getToSourceConverterName().toString()
				+ " and calculateMoveablesConverterName " + getCalculateMoveablesConverterName().toString()
				+ ". Constructed converter is "
				+ ((converter != null) ? converter.toString() : " - converter not yet loaded");
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableSourceUnits() {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).getAcceptableSourceUnits();
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableTargetUnits() {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).getAcceptableTargetUnits();
	}

	@Override
	public Quantity toSource(Quantity target) throws Exception {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).toSource(target);
	}

	@Override
	public Quantity toTarget(Quantity source) throws Exception {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).toTarget(source);
	}

	@Override
	public boolean sourceMinIsTargetMax() {
		return getConverter().sourceMinIsTargetMax();
	}
	
	@Override
	public boolean handlesStoT() {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).handlesStoT();
	}

	@Override
	public boolean handlesTtoS() {
		return CoupledConverterHolder.getIQuantityConverter(getConverter()).handlesTtoS();
	}
	
}
