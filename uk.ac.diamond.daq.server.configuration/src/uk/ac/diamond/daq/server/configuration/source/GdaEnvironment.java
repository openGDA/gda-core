/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.configuration.source;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import uk.ac.diamond.daq.server.configuration.ConfigurationOptions;
import uk.ac.diamond.daq.server.configuration.ConfigurationOptions.Action;
import uk.ac.diamond.daq.server.configuration.ConfigurationSource;

/**
 * Wrapper around environment variables, interpreting GDA_* variables as the equivalent properties
 * and extracting GDA config options from their respective keys.
 */
public record GdaEnvironment(Map<String, String> vars) implements ConfigurationSource {
	public static final String GDA_PREFIX = "GDA_";

	@Override
	public ConfigurationOptions getSpringXml() {
		return configOptions("gda.spring.xml", "spring.xml");
	}

	@Override
	public ConfigurationOptions getProfiles() {
		return configOptions("gda.profiles", "profiles");
	}

	@Override
	public ConfigurationOptions getPropertiesFiles() {
		return configOptions("gda.properties", "properties");
	}

	@Override
	public ConfigurationOptions getLoggingConfiguration() {
		return configOptions("gda.logging", "logging");
	}

	private ConfigurationOptions configOptions(String property, String action) {
		return new ConfigurationOptions(getValues(property), actionFor(action));
	}

	@Override
	public Map<String, String> getProperties() {
		return vars;
	}

	/**
	 * Determine whether a given key should be appended to, or overwrite, the same key from
	 * subsequent configuration sources.
	 */
	private Action actionFor(String key) {
		return vars.containsKey("gda.no.default." + key) ? Action.OVERWRITE : Action.APPEND;
	}

	/**
	 * Get a property (in dotted.name.format) from the equivalent SCREAMING_SNAKE_CASE environment
	 * variable. Only keys starting with {@value #GDA_PREFIX} are available.
	 */
	public Optional<String> getProperty(String key) {
		return Optional.ofNullable(vars.get(key));
	}

	/** Get the config directory from the environment if available */
	public Optional<String> configDirectory() {
		return getProperty("gda.config");
	}

	/**
	 * Convert a comma-separated list of values for the given key (if present) into a stream of
	 * individual values.
	 */
	private Stream<String> getValues(String key) {
		return getProperty(key).map(x -> Stream.of(x.split(","))).orElseGet(Stream::empty);
	}

	/** Build a GdaEnvironment configuration from the System environment */
	public static GdaEnvironment build() {
		return fromEnvironment(System.getenv());
	}

	/** Build a GdaEnvironment configuration from the given environment */
	/* Mainly here to support testing where the environment should be configurable */
	public static GdaEnvironment fromEnvironment(Map<String, String> env) {
		Map<String, String> vars =
				env.entrySet().stream()
						.filter(e -> e.getKey().startsWith(GDA_PREFIX))
						.collect(
								toMap(
										e -> e.getKey().toLowerCase().replace("_", "."),
										Entry::getValue));
		return new GdaEnvironment(vars);
	}
}
