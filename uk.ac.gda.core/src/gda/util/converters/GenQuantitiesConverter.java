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
 * Package private class used by the public LookupTableConverterHolder and JEPConverterHolder This is a helper class to
 * wrap an IQuantityConverter converter and use it to implement a IQuantitiesConverter. In this way the conversion
 * between two single Quantities is used to create a conversion between two collections of Quantities where the
 * conversion is the same.
 */
final class GenQuantitiesConverter implements IQuantitiesConverter {
	private final IQuantityConverter converter;

	GenQuantitiesConverter(IQuantityConverter converter) {
		this.converter = converter;
	}

	@Override
	public ArrayList<ArrayList<Unit<? extends Quantity>>> getAcceptableUnits() {
		ArrayList<ArrayList<Unit<? extends Quantity>>> units = new ArrayList<ArrayList<Unit<? extends Quantity>>>();
		units.add(converter.getAcceptableSourceUnits());
		return units;
	}

	@Override
	public Quantity[] calculateMoveables(Quantity[] sources, Object[] moveables) throws Exception {
		Quantity target = converter.toTarget(sources[0]);
		Quantity targets[] = new Quantity[moveables.length];
		java.util.Arrays.fill(targets, target);
		return targets;
	}

	@Override
	public Quantity[] toSource(Quantity[] targets, Object[] moveables) throws Exception {
		Quantity source = converter.toSource(targets[0]);
		Quantity sources[] = new Quantity[moveables.length];
		java.util.Arrays.fill(sources, source);
		return sources;
	}

	@Override
	public ArrayList<ArrayList<Unit<? extends Quantity>>> getAcceptableMoveableUnits() {
		ArrayList<ArrayList<Unit<? extends Quantity>>> units = new ArrayList<ArrayList<Unit<? extends Quantity>>>();
		units.add(converter.getAcceptableTargetUnits());
		return units;
	}

	@Override
	public String toString() {
		return "GenQuantitiesConverter wrapper of  " + converter.toString();
	}

	public ArrayList<Unit<? extends Quantity>> getAcceptableSourceUnits() {
		return converter.getAcceptableSourceUnits();
	}

	public ArrayList<Unit<? extends Quantity>> getAcceptableTargetUnits() {
		return converter.getAcceptableTargetUnits();
	}

	public Quantity toSource(Quantity target) throws Exception {
		return converter.toSource(target);
	}

	public Quantity toTarget(Quantity source) throws Exception {
		return converter.toTarget(source);
	}

	@Override
	public boolean sourceMinIsTargetMax() {
		return converter.sourceMinIsTargetMax();
	}
	

	public boolean handlesStoT(){
		return converter.handlesStoT();
	}
	
	public boolean handlesTtoS(){
		return converter.handlesTtoS();
	}
}
