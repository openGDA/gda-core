/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.mscan.element;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.List;

/**
 * Links the revised mscan syntax to a typed representation of the possible scanpath mutator specifiers.
 * @since GDA 9.9
 */
public enum Mutator implements IMScanElementEnum {
	ALTERNATING("alte", 0, 0, new boolean[]{}),
	RANDOM_OFFSET("roff", 2, 1, new boolean[]{true, false}),
	CONTINUOUS("cont", 0, 0, new boolean[]{});

	private final String text;
	private final int maxValueCount;
	private final int minValueCount;
	private final boolean[] positiveValuesOnly;

	private Mutator(final String text, final int maxValueCount,
			final int minValueCount, final boolean[] positiveValuesOnly) {
		this.text = text;
		this.maxValueCount = maxValueCount;
		this.minValueCount = minValueCount;
		this.positiveValuesOnly = positiveValuesOnly;
	}

	/**
	 * The default text values that correspond to the instances of Roi
	 *
	 * @return		List of default text for the instances
	 */
	public static List<String> strValues() {
		return stream(values()).map(val -> val.text).collect(toList());
	}

	/**
	 * The maximum number of values accepted to construct the mutator
	 *
	 * @return		The max number of values required to construct the mutator
	 */
	public int maxValueCount() {
		return maxValueCount;
	}

	/**
	 * The minimum number of values required to construct the mutator
	 *
	 * @return		The min number of values required to construct the mutator
	 */
	public int minValueCount() {
		return minValueCount;
	}

	/**
	 * Indicates whether the parameter value at the supplied index for this instance can only be positive
	 *
	 * @param index		The index of the parameter to be checked
	 * @return			true if the parameter at the specified index must be greater than -1
	 *
	 * @throws			IndexOutOfBoundsException if the specified index is not valid for this instance.
	 */
	public boolean positiveValuesOnlyFor(final int index) {
		return positiveValuesOnly[index];
	}

	/**
	 * Indicates whether the params associated with a mutator should be checked for null values. At the moment
	 * this is based purely on whether there are any to check.
	 *
	 * @return	true if the params associated with this mutator should be checked for Null values
	 */
	public boolean shouldParamsBeNullChecked() {
		return positiveValuesOnly.length > 0;
	}

	@Override
	public String toString() {
		return name().replace("_", " ").toLowerCase();
	}
}
