/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

/**
 * Utility enum for general verbs uised by msacn syntax to link the instances to their aliases
 */
public enum Action implements IMScanElementEnum {
	RERUN("rrun", asList("rerun"), String.class);

	private static final Map<String, Action> termsMap;
	private final List<String> terms = new ArrayList<>();
	private final Class<?> paramsType;

	private Action(final String text, final List<String> aliases, final Class<?> type) {
		this.terms.add(text);
		this.terms.addAll(aliases);
		this.paramsType = type;
		}

	/**
	 * Initialise the {@link java.util.Map} of text terms (including aliases) to {@link Action} instance
	 */
	static {
		termsMap = stream(values())
				.map(action -> action.terms().stream()
						.map(term -> new Pair<String, Action>(term, action)))
				.flatMap(Function.identity())
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	public static Map<String, Action> termsMap() {
		return termsMap;
	}

	public List<String> terms() {
		return terms;
	}

	public List<String> aliases() {
		return terms.subList(1, terms.size() - 1);
	}

	public Class<?> getParamsType() {
		return paramsType;
	}
}
