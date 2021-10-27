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

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import gda.configuration.properties.LocalProperties;

/**
 * Wrapper around the attributes of an xml element that filters by mode.
 * <br>
 * Attributes can be set for all modes or can be specific to a given mode.
 * eg
 * <pre>{@code
 * <tag
 *     foo="bar"
 *     live-foo="non-bar"
 * />
 * }</pre>
 *
 * In this example foo would have the value "bar" unless the mode was live unless
 * in which case it would be "not-bar".
 */
public class BeanAttributes {

	/** null mode value to mark mode-independent attributes */
	private static final String ALL_MODES = null;

	/** The mode gda is currently using */
	private final String mode = LocalProperties.get("gda.mode");

	/** All the keys/values, grouped by mode */
	private final Map<String, Map<String, String>> attributesByMode;

	/** Cache of the effective attributes used for each mode */
	private final Map<String, Map<String, String>> combinedAttributeCache = new HashMap<>();

	/** Wrap an xml element's attributes in a mode specific view */
	public static BeanAttributes from(Element element) {
		NamedNodeMap attributes = element.getAttributes();
		return new BeanAttributes(range(0, attributes.getLength())
				.mapToObj(attributes::item)
				.collect(toMap(Node::getLocalName, Node::getNodeValue)));
	}

	/**
	 * Wrap a map of key-value pairs in a mode specific view.
	 * @param attributes Map of key value pairs
	 */
	public BeanAttributes(Map<String, String> attributes) {
		attributesByMode = attributes.entrySet()
				.stream()
				.map(Attribute::new)
				// convoluted toMap instead of groupingBy to allow null mode for defaults
				.collect(toMap(a -> a.mode,
						Attribute::asMap,
						(l,r) -> {l.putAll(r); return l;},
						HashMap::new));
		// make sure there's a default map
		attributesByMode.putIfAbsent(ALL_MODES, emptyMap());
	}

	/** Get the value of a key for the default mode */
	public Optional<String> get(String key) {
		return getForMode(key, mode);
	}

	/** Get the value of a key for a given mode */
	public Optional<String> getForMode(String key, String mode) {
		return ofNullable(modeAttributes(mode).get(key));
	}

	/** Get stream of properties for the current mode as map entries*/
	public Stream<Entry<String, String>> modeProperties() {
		return modeAttributes(mode).entrySet().stream();
	}

	/** Get map of attributes for the given mode */
	private Map<String, String> modeAttributes(String mode) {
		return combinedAttributeCache.computeIfAbsent(mode,
				m -> withDefaults(attributesByMode.getOrDefault(m, emptyMap())));
	}

	/**
	 * Include the default, 'all_mode' attributes if they are not overridden by
	 * the given map.
	 * @param original map of key-value pairs
	 * @return map of original key/value pairs and any defaults not overridden.
	 */
	private Map<String, String> withDefaults(Map<String, String> original) {
		Map<String, String> combined = new HashMap<>(attributesByMode.get(ALL_MODES));
		combined.putAll(original);
		return combined;
	}

	/**
	 * Wrapper around the raw attribute from the element that will extract a
	 * mode if one exists. If no mode is found, it is assumed to be used for all
	 * modes.
	 */
	private static class Attribute {
		private final String mode;
		private final String key;
		private final String value;

		public Attribute(Entry<String, String> keyValue) {
			String[] parts = keyValue.getKey().split("-", 2);
			if (parts.length == 1) {
				mode = ALL_MODES;
				key = parts[0];
			} else {
				mode = parts[0];
				key = parts[1];
			}
			this.value = keyValue.getValue();
		}
		private Map<String, String> asMap() {
			HashMap<String, String> map = new HashMap<>();
			map.put(key, value);
			return map;
		}
		@Override
		public String toString() {
			return "Attribute[" + key +"=" + value + ", " + mode + "]";
		}
	}
}
