/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.configuration;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static uk.ac.diamond.daq.configuration.ConfigurationOptions.empty;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import uk.ac.diamond.daq.configuration.ConfigurationOptions.Action;
import uk.ac.diamond.daq.configuration.source.directory.ConfigRef;

public class ConfigurationBuilder {
	private final Path root;
	private List<SourceGenerator> sources = new ArrayList<>();

	private ConfigurationBuilder(Path root) {
		this.root = root;
	}

	public static ConfigurationBuilder withRoot(Path dir) {
		return new ConfigurationBuilder(dir);
	}

	public ConfigurationBuilder withDirectory(String dir) {
		sources.add(proc -> new ConfigRef(dir).resolveConfigs(proc, root).map(ConfigurationSource.class::cast));
		return this;
	}

	public ConfigurationBuilder withSource(ConfigurationSource src) {
		sources.add(proc -> Stream.of(src));
		return this;
	}

	public ConfigurationBuilder withProperty(String key, String value) {
		sources.add(proc -> Stream.of(new SourceAdapter() {
			@Override
			public Map<String, String> getProperties() {
				return Map.of(key, value);
			}
		}));
		return this;
	}

	public ConfigurationBuilder withServerXml(String xml) {
		sources.add(proc -> "server".equals(proc) ? Stream.of(new SourceAdapter() {
			@Override
			public ConfigurationOptions getSpringXml() {
				return new ConfigurationOptions(Stream.of(xml), Action.APPEND);
			}
		}) : Stream.empty());
		return this;
	}

	public ConfigurationBuilder withClientXml(String xml) {
		sources.add(proc -> "client".equals(proc) ? Stream.of(new SourceAdapter() {
			@Override
			public ConfigurationOptions getSpringXml() {
				return new ConfigurationOptions(Stream.of(xml), Action.APPEND);
			}
		}) : Stream.empty());
		return this;
	}

	public ConfigurationBuilder withPropertiesFile(String props) {
		sources.add(proc -> Stream.of(new SourceAdapter() {
			@Override
			public ConfigurationOptions getPropertiesFiles() {
				return new ConfigurationOptions(Stream.of(props), Action.APPEND);
			}
		}));
		return this;
	}

	public BeamlineConfiguration build(String process) {
		var sourceStream = sources.stream().flatMap(s -> s.sources(process));
		return new CompositeBeamlineConfiguration(root, sourceStream, emptyList());
	}

	private interface SourceGenerator {
		Stream<ConfigurationSource> sources(String process);
	}
	private abstract class SourceAdapter implements ConfigurationSource {

		@Override
		public ConfigurationOptions getSpringXml() {
			return empty();
		}

		@Override
		public ConfigurationOptions getProfiles() {
			return empty();
		}

		@Override
		public ConfigurationOptions getPropertiesFiles() {
			return empty();
		}

		@Override
		public ConfigurationOptions getLoggingConfiguration() {
			return empty();
		}

		@Override
		public Map<String, String> getProperties() {
			return emptyMap();
		}

	}
}
