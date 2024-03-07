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

import static uk.ac.diamond.daq.configuration.ConfigurationOptions.Action.APPEND;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import uk.ac.diamond.daq.configuration.ConfigurationOptions;

/**
 * A collection of details about the layout of a config directory where paths are relative to an
 * unspecified root. Intended to be used in conjunction with a root path in a {@link
 * ConfigDirectory}.
 */
public final class ConfigLayout {
	private final String mode;

	private final OptionsByMode springXml;
	private final OptionsByMode propertiesFiles;
	private final OptionsByMode profiles;
	private final OptionsByMode logging;
	private final Map<String, String> defaultProperties;
	private final Map<String, String> systemProperties;

	private ConfigLayout(Options builder) {
		this(builder, null);
	}

	private ConfigLayout(Options builder, String mode) {
		this.mode = mode;
		springXml = builder.springXml;
		propertiesFiles = builder.propertiesFiles;
		profiles = builder.profiles;
		logging = builder.logging;
		defaultProperties = builder.defaultProperties;
		systemProperties = builder.systemProperties;
	}

	public ConfigLayout withMode(String mode) {
		var opts = new Options();
		opts.springXml = springXml;
		opts.propertiesFiles = propertiesFiles;
		opts.logging = logging;
		opts.profiles = profiles;
		opts.defaultProperties = defaultProperties;
		opts.systemProperties = systemProperties;
		return new ConfigLayout(opts, mode);
	}

	public ConfigurationOptions springXml(Path root) {
		var fileStream = springXml.forMode(mode).map(root::resolve).map(Path::toString);
		return new ConfigurationOptions(fileStream, APPEND);
	}

	public ConfigurationOptions profiles() {
		return new ConfigurationOptions(profiles.forMode(mode), APPEND);
	}

	public Map<String, String> defaultProperties() {
		return defaultProperties;
	}

	public Map<String, String> systemProperties() {
		return systemProperties;
	}

	public ConfigurationOptions propertiesFiles(Path root) {
		var fileStream = propertiesFiles.forMode(mode).map(root::resolve).map(Path::toString);
		return new ConfigurationOptions(fileStream, APPEND);
	}

	public ConfigurationOptions loggingConfiguration(Path root) {
		var fileStream = logging.forMode(mode).map(root::resolve).map(Path::toString);
		return new ConfigurationOptions(fileStream, APPEND);
	}

	public static class LayoutBuilder {
		private final Options options = new Options();
		private final Options serverOverrides = new Options();
		private final Options clientOverrides = new Options();

		/** Create a builder with no paths configured */
		public static LayoutBuilder empty() {
			return new LayoutBuilder();
		}

		public LayoutBuilder withSpringXml(OptionsByMode serverXml) {
			options.springXml = serverXml;
			return this;
		}

		public LayoutBuilder withPropertiesFiles(OptionsByMode serverProperties) {
			options.propertiesFiles = serverProperties;
			return this;
		}

		public LayoutBuilder withProfiles(OptionsByMode profiles) {
			options.profiles = profiles;
			return this;
		}

		public LayoutBuilder withLogging(OptionsByMode logging) {
			options.logging = logging;
			return this;
		}

		public LayoutBuilder withDefaultProperties(Map<String, String> defaults) {
			options.defaultProperties = defaults;
			return this;
		}

		public LayoutBuilder withSystemProperties(Map<String, String> system) {
			options.systemProperties = system;
			return this;
		}

		public LayoutBuilder withServer(LayoutBuilder server) {
			serverOverrides.merge(server.options);
			return this;
		}

		public LayoutBuilder withClient(LayoutBuilder client) {
			clientOverrides.merge(client.options);
			return this;
		}

		public LayoutBuilder update(LayoutBuilder other) {
			options.merge(other.options);
			serverOverrides.merge(other.serverOverrides);
			clientOverrides.merge(other.clientOverrides);
			return this;
		}
		/** Finalise this builder and create a ConfigLayout */
		public ConfigLayout build(String process) {
			return switch (process) {
				case "server" -> options.merge(serverOverrides).build();
				case "client" -> options.merge(clientOverrides).build();
				default -> throw new IllegalArgumentException(
						"Unrecognised process name: " + process);
			};
		}
	}

	@Override
	public String toString() {
		return "ConfigLayout [springXml=" + springXml + ", propertiesFiles=" + propertiesFiles + ", profiles="
				+ profiles + ", logging=" + logging + ", defaultProperties=" + defaultProperties + ", systemProperties="
				+ systemProperties + "]";
	}

	/**
	 * Container for the options valid for either the default level config or the process specific
	 * versions.
	 */
	private static class Options {
		private OptionsByMode springXml = new OptionsByMode();
		private OptionsByMode propertiesFiles = new OptionsByMode();
		private OptionsByMode profiles = new OptionsByMode();
		private OptionsByMode logging = new OptionsByMode();
		private Map<String, String> defaultProperties = new HashMap<>();
		private Map<String, String> systemProperties = new HashMap<>();

		/**
		 * Merge the values of these options with the values of another. Any fields configured in
		 * the other layout will be merged into the values of this one. Duplicates are not removed
		 * and the order is maintained (merged values are added to end of initial values).
		 *
		 * @param other The other layout to be merged in to this one
		 * @return This layout updated to include any fields defined in the other layout
		 */
		public Options merge(Options other) {
			springXml = this.springXml.merge(other.springXml);
			propertiesFiles = this.propertiesFiles.merge(other.propertiesFiles);
			profiles = this.profiles.merge(other.profiles);
			logging = this.logging.merge(other.logging);
			defaultProperties.putAll(other.defaultProperties);
			systemProperties.putAll(other.systemProperties);
			return this;
		}

		public ConfigLayout build() {
			return new ConfigLayout(this);
		}
	}
}
