/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.jython.completion;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type of object represented by a completion candidate<br>
 * Class, Function etc
 */
public enum CompletionType {
	IMPORT,
	CLASS,
	FUNCTION,
	ATTRIBUTE,
	BUILTIN,
	PARAM,
	NONE,
	;

	private static final Map<Integer, CompletionType> map;
	static {
		Map<Integer, CompletionType> tmap = Arrays.stream(values())
				.collect(Collectors.toMap(CompletionType::ordinal, v -> v));
		map = Collections.unmodifiableMap(tmap);
	}

	/**
	 * Reverse lookup of {@code CompletionType.ordinal()}
	 * @param i
	 * @return {@link CompletionType} for which the ordinal is {@code i}
	 */
	static CompletionType fromInt(int i) {
		return map.get(i);
	}
}