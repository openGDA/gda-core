/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

/**
 * Utility functions used by converter classes
 */
class ConverterUtils {

	private ConverterUtils() {
		// Prevent instantiation
	}

	static void checkUnitsAreEqual(IQuantitiesConverter<? extends Quantity<?>, ? extends Quantity<?>> o,
			IQuantitiesConverter<? extends Quantity<?>, ? extends Quantity<?>> n) {
		if (o == null || n == null) {
			throw new IllegalArgumentException("ConverterUtils.checkUnitsAreEqual() : o or n is null ");
		}
		final List<List<String>> newAcceptableUnits = n.getAcceptableUnits();
		if (!unitsAreEqual(newAcceptableUnits, o.getAcceptableUnits())) {
			throw new IllegalArgumentException(
					"ConverterUtils.checkUnitsAreEqual() : AcceptableUnits have changed from "
							+ o.getAcceptableUnits().toString() + " to " + newAcceptableUnits.toString());
		}

		final List<List<String>> newAcceptableMoveableUnits = n.getAcceptableMoveableUnits();
		if (!unitsAreEqual(newAcceptableMoveableUnits, o.getAcceptableMoveableUnits())) {
			throw new IllegalArgumentException(
					"ConverterUtils.checkUnitsAreEqual() : AcceptableMoveableUnits have changed from "
							+ o.getAcceptableMoveableUnits().toString() + " to "
							+ newAcceptableMoveableUnits.toString());
		}
	}

	static boolean unitsAreEqual(List<List<String>> o, List<List<String>> n) {
		if (o.size() != n.size())
			return false;

		for (int i = 0; i < o.size(); i++) {
			if (!unitsAreEqual1(o.get(i), n.get(0))) {
				return false;
			}
		}
		return true;
	}

	private static boolean unitsAreEqual1(List<String> o, List<String> n) {
		if (o.size() != n.size())
			return false;

		for (int i = 0; i < o.size(); i++) {
			if (!o.get(i).equals(n.get(i))) {
				return false;
			}
		}
		return true;
	}

}
