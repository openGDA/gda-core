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

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

/**
 * Links the revised mscan syntax to a typed representation of the possible scanpath mutator specifiers.
 * @since GDA 9.9
 */
public enum Mutator implements IMScanElementEnum {
	ALTERNATING("alte", asList("snak", "snake", "alternating"), 0, 0, new boolean[]{}),
	RANDOM_OFFSET("roff", asList("random_offset"), 2, 1, new boolean[]{true, false}),
	CONTINUOUS("cont", asList("continuous"), 0, 0, new boolean[]{});

	private static final Map<String, Mutator> termsMap;
	private final List<String> terms = new ArrayList<>();
	private final int maxValueCount;
	private final int minValueCount;
	private final boolean[] positiveValuesOnly;

	private Mutator(final String text, final List<String> aliases, final int maxValueCount,
			final int minValueCount, final boolean[] positiveValuesOnly) {
		this.terms.add(text);
		this.terms.addAll(aliases);
		this.maxValueCount = maxValueCount;
		this.minValueCount = minValueCount;
		this.positiveValuesOnly = positiveValuesOnly;
	}

	/**
	 * Initialise the {@link java.util.Map} of text terms (including aliases) to {@link Mutator} instance
	 */
	static {
		termsMap = stream(values())
				.map(mutator -> mutator.terms().stream()
						.map(term -> new Pair<String, Mutator>(term, mutator)))
				.flatMap(Function.identity())
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	public static Map<String, Mutator> termsMap() {
		return termsMap;
	}

	/**
	 * The default text values that correspond to the instances of Roi
	 *
	 * @return		List of default text for the instances
	 */
	public static List<String> strValues() {
		return stream(values()).map(val -> val.terms.get(0)).collect(toList());
	}

	public List<String> terms() {
		return terms;
	}

	public List<String> aliases() {
		return terms.subList(1, terms.size() - 1);
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
