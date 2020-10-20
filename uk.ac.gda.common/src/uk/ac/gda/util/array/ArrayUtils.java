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

package uk.ac.gda.util.array;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

/**
 * Static library with array methods, where these are not already provided (e.g. by {@link Arrays})
 */
public final class ArrayUtils {

	private static final int FALLBACK_INDEX = -1;

	private ArrayUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Reports the index of the array element which has the least absolute difference from the given reference value.
	 * @param array the array
	 * @param reference the reference value to compare against array elements
	 * @return index of the closest element, or -1 if the array is empty
	 */
	public static int indexOfNearest(double[] array, double reference) {

		DoubleUnaryOperator proximityToRef = x -> Math.abs(reference-x);
		IntToDoubleFunction elementProximity = i -> proximityToRef.applyAsDouble(array[i]);
		Function<IntToDoubleFunction,IntBinaryOperator> smallerOf = f -> (i,j) -> f.applyAsDouble(i) < f.applyAsDouble(j) ? i : j;
		IntBinaryOperator closerElement = smallerOf.apply(elementProximity);

		return IntStream.range(0,array.length)
						.reduce( closerElement )
						.orElse(FALLBACK_INDEX);
	}
}
