/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.function;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

public class InterpolationWithoutExtrapolationFunction extends InterpolationFunction {

	public InterpolationWithoutExtrapolationFunction(double[] column, double[] column2,
			Unit<? extends Quantity> columnUnits, Unit<? extends Quantity> columnUnits2) {
		super(column, column2, columnUnits, columnUnits2);
	}

	@Override
	protected int[] calculateBeforeAfterPair(double x, boolean isAscending, boolean isDescending, double[] xValues, int numberOfXValues) {
		int before, after;
		if (isAscending) {
			// ascending values in x
			if (x < xValues[0] ) {
				throw new IllegalArgumentException(String.format("Input value %.2f below accepted minimum %.2f", x, xValues[0]));
			} else if (x > xValues[numberOfXValues - 1]) {
				throw new IllegalArgumentException(String.format("Input value %.2f above accepted maximum %.2f", x, xValues[numberOfXValues - 1]));
			} else {
				// Interpolate from the two surrounding xValues
				for (before = 0, after = 1; before < numberOfXValues; before++, after++) {
					if (x >= xValues[before] && x <= xValues[after]) {
						break;
					}
				}
			}
		} else if (isDescending) {
			// descending values in x
			if (x > xValues[0]) {
				throw new IllegalArgumentException(String.format("Input value %.2f above accepted maximum %.2f", x, xValues[0]));
			} else if (x < xValues[numberOfXValues - 1]) {
				throw new IllegalArgumentException(String.format("Input value %.2f below accepted minimum %.2f", x, xValues[numberOfXValues - 1]));
			} else {
				// Interpolate from the two surrounding xValues
				for (before = 0, after = 1; before < numberOfXValues; before++, after++) {
					if (x <= xValues[before] && x >= xValues[after]) {
						break;
					}
				}
			}
		} else {
			//should never get here, should have been caught by constructor
			throw new IllegalArgumentException("InterpolationFunction. xValues must be increasing or decreasing");
		}
		return new int[] {before, after};
	}
}
