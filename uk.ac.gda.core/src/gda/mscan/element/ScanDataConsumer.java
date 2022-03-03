/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

/**
 * Holder for transformations that can be applied to the output data (nexus file) of an mscan which maps the
 * transformation to type of object used to define its parameters
 */
public enum ScanDataConsumer implements IMScanElementEnum {
	TEMPLATE("temp", asList("templates"), List.class),
	PROCESSOR("proc", asList("processors"), Map.class),
	PER_SCAN_MONITOR("psms", asList("perscanmonitors", "psm"), Set.class),
	SAMPLE("samp", asList("sample"), Map.class);

	private static final Map<String, ScanDataConsumer> termsMap;
	private final List<String> terms = new ArrayList<>();
	private final Class<?> paramsType;

	private ScanDataConsumer(final String text, final List<String> aliases, final Class<?> type) {
		this.terms.add(text);
		this.terms.addAll(aliases);
		this.paramsType = type;
		}

	/**
	 * Initialise the {@link java.util.Map} of text terms (including aliases) to {@link ScanDataConsumer} instance
	 */
	static {
		termsMap = stream(values())
				.map(consumer -> consumer.terms().stream()
						.map(term -> new Pair<String, ScanDataConsumer>(term, consumer)))
				.flatMap(Function.identity())
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	public static Map<String, ScanDataConsumer> termsMap() {
		return termsMap;
	}

	public String getText() {
		return terms.get(0);
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
