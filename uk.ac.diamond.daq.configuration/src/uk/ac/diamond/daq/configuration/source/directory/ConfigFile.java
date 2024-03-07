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

package uk.ac.diamond.daq.configuration.source.directory;

import static java.util.Collections.emptyMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;

import uk.ac.diamond.daq.configuration.source.directory.ConfigLayout.LayoutBuilder;

/**
 * Representation of the config.toml optionally found at the root of config directories <br>
 * Files should be of the format
 *
 * <pre>
 * # List of files to be included in the spring context for all processes
 * spring-xml = ["path/to/file1.xml", "path/to/file2.xml"]
 *
 * # lists with a single value can be passed as a string
 * logging = "path/to/single.xml"
 * # ...or as a list as normal
 * properties = ["path/to/properties.file"]
 *
 * profiles = ["profile1", "profile2"]
 *
 * # default properties that can be used for string interpolation can be
 * # added to a 'defaults' table
 * [defaults]
 * beamline = "ixx"
 * "dotted.key.names" = "should be quoted"
 *
 * # system properties that should be set to configure third party libraries
 * # can be set in a 'system' table similar to 'defaults'
 * [system]
 * "third.party.custom.property" = "property to be set as string"
 *
 * # values that should only be set for either the server or the client can
 * # be added to a 'server' or 'client' map. For 'normal' fields, this can be
 * # managed using dotted key notation, eg
 *
 * server.spring-xml = "path/to/server.xml"
 *
 * # for nested tables, this should use the dotted notation in the table name
 *
 * [server.system] # system properties to set only in the server process
 * "java.awt.headless" = "true"
 *
 * # Extra configuration directories that should be included can be passed
 * # either as a single string in the 'extras' table referring to the root
 * # of the directory
 * [extras]
 * facility = "path/to/facility/config"
 *
 * # or as a table within the 'extras' table where aspects of the directory
 * # can be overridden other than 'root' and 'extras', the nested table is
 * # the same format and spec as the whole toml file.
 * [extras.core]
 * # if passed as a nested table, a 'root' is required. This is equivalent to
 * # the string passed above
 * root = "path/to/core/config"
 * # anything passed in this table will override the values in the config.toml
 * # in the included directory
 * server-xml = "non/standard/server.xml"
 * </pre>
 */
public class ConfigFile {
	/** Deserialisation helper to allow either single string or list of strings */
	static class Options {
		final List<String> options;

		@JsonCreator
		public Options(String single) {
			this.options = List.of(single);
		}

		@JsonCreator
		public Options(List<Object> options) {
			if (options.stream().anyMatch(o -> !(o instanceof String))) {
				throw new IllegalArgumentException("Options must all be strings");
			}
			this.options =
					options.stream()
							.map(String.class::cast)
							.toList();
		}
	}

	private static class LayoutValues {
		private LayoutBuilder builder = LayoutBuilder.empty();

		@JsonProperty("spring-xml")
		private void setSpringXml(OptionsByMode springXml) {
			builder.withSpringXml(springXml);
		}

		@SuppressWarnings("unused")
		private void setProperties(OptionsByMode properties) {
			builder.withPropertiesFiles(properties);
		}

		@SuppressWarnings("unused")
		private void setProfiles(OptionsByMode profiles) {
			builder.withProfiles(profiles);
		}

		@SuppressWarnings("unused")
		private void setLogging(OptionsByMode logging) {
			builder.withLogging(logging);
		}

		@SuppressWarnings("unused")
		private void setDefaults(Map<String, String> defaults) {
			builder.withDefaultProperties(defaults);
		}

		@SuppressWarnings("unused")
		private void setSystem(Map<String, String> systemProperties) {
			builder.withSystemProperties(systemProperties);
		}
	}

	private LayoutBuilder builder = LayoutBuilder.empty();
	private Map<String, ConfigRef> extras = emptyMap();

	private String root;

	@JsonProperty("spring-xml")
	public void setSpringXml(OptionsByMode springXml) {
		builder.withSpringXml(springXml);
	}

	public void setProperties(OptionsByMode properties) {
		builder.withPropertiesFiles(properties);
	}

	public void setProfiles(OptionsByMode profiles) {
		builder.withProfiles(profiles);
	}

	public void setLogging(OptionsByMode logging) {
		builder.withLogging(logging);
	}

	public void setDefaults(Map<String, String> defaults) {
		builder.withDefaultProperties(defaults);
	}

	public void setSystem(Map<String, String> systemProperties) {
		builder.withSystemProperties(systemProperties);
	}

	public void setServer(LayoutValues values) {
		builder.withServer(values.builder);
	}

	public void setClient(LayoutValues values) {
		builder.withClient(values.builder);
	}

	public void setExtras(Map<String, ConfigRef> extras) {
		this.extras = extras;
	}

	public Stream<ConfigRef> extras() {
		return extras.values().stream();
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public LayoutBuilder asLayout() {
		return builder;
	}

	public static Optional<ConfigFile> read(Path path) {
		if (!Files.exists(path)) return Optional.empty();

		ObjectMapper map = new ObjectMapper(TomlFactory.builder().build());
		try {
			var file = map.readValue(path.toFile(), ConfigFile.class);
			return Optional.of(file);
		} catch (IOException e) {
			throw new UncheckedIOException("Error reading or deserialising config file: " + path, e);
		}
	}
}
