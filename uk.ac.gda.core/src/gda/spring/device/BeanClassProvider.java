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

package gda.spring.device;

import static java.util.stream.Collectors.joining;
import static gda.configuration.properties.LocalProperties.GDA_MODE;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import gda.configuration.properties.LocalProperties;

/**
 * Helper class to encapsulate process of deciding the class to use for a spring bean.
 * <br>
 * Class name can be taken from one of three places (in order of precedence):
 * <ol>
 * <li>The lookup function (mode -> FQCN) passed in to the {@link #forMode} method</li>
 * <li>The given property (with the mode appended)</li>
 * <li>The fallback classes given as map of {mode: FQCN}</li>
 * </ol>
 *
 * For each of these, the returned class can redirect to a different class so for
 * example a bean can use whatever the default class is for live mode when it is
 * running in dummy mode by setting the dummy class to "#live".
 */
public class BeanClassProvider {

	/** The name of the class property to read from BeanAttributes */
	private static final String CLASS_PROPERTY = "class";
	/** Prefix added to classes to enable modes to defer to another mode for the class */
	private static final String REDIRECT = "#";
	/** Filter to ensure class names are valid - only very basic check */
	private static final Predicate<String> VALID_CLASS = s -> s != null && !s.isEmpty();

	/** Lookup method for getting the class from the fallback map */
	private final UnaryOperator<String> fromFallbacks;
	/** Lookup method for getting the class from LocalProperties */
	private final UnaryOperator<String> fromProperties;

	/**
	 * Create a BeanClassProvider that determines the required class for spring beans.
	 *
	 * @param property the prefix of the property defining the class to use for each mode
	 *         eg prefix.dummy would be used to provide the class for dummy mode
	 * @param fallbacks map from mode to class to use if no properties are defined
	 */
	public BeanClassProvider(String property, Map<String, String> fallbacks) {
		fromFallbacks = fallbacks::get;
		fromProperties = m -> LocalProperties.get(property + m);
	}

	/**
	 * Get the FQCN of a bean following any redirects.
	 * <br>
	 * This takes a function rather than an optional class name so that the default
	 * can redirect to the value of another mode.
	 * @param mode to get the class name for
	 * @param attributes wrapping the attributes of an xml element
	 * @return the FQCN of the class required for the given mode
	 */
	public String forMode(String mode, BeanAttributes attributes) {
		return classNameFor(mode, m -> attributes.getForMode(CLASS_PROPERTY, m).orElse(null), new LinkedHashSet<>());
	}

	/**
	 * Get the FQCN of a bean following any redirects.
	 * <br>
	 * Similar to {@link #forMode(String, BeanAttributes)} but uses the current mode
	 * @param attributes wrapping the attributes of an xml element
	 * @return the FQCN of the class required for the given mode
	 */
	public String forCurrentMode(BeanAttributes attributes) {
		return forMode(LocalProperties.get(GDA_MODE), attributes);
	}

	private String classNameFor(String mode, UnaryOperator<String> lookup, Set<String> visited) {
		String className = Stream.of(lookup, fromProperties, fromFallbacks)
				.map(f -> f.apply(mode))
				.filter(VALID_CLASS)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No class for " + mode));
		if (className.startsWith(REDIRECT)) {
			if (visited.add(mode)) {
				className = classNameFor(className.substring(1),lookup, visited);
			} else {
				throw new IllegalStateException("Circular reference in class property: " +
					visited.stream().collect(joining(" -> ", "", " -> " + mode)));
			}
		}
		return className;
	}
}
