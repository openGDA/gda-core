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

package uk.ac.diamond.daq.server.configuration.test;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.diamond.daq.server.configuration.ConfigurationOptions.Action.APPEND;
import static uk.ac.diamond.daq.server.configuration.ConfigurationOptions.Action.OVERWRITE;
import static uk.ac.diamond.daq.server.configuration.test.Matchers.containsURLs;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import uk.ac.diamond.daq.server.configuration.CompositeBeamlineConfiguration;
import uk.ac.diamond.daq.server.configuration.ConfigurationOptions;
import uk.ac.diamond.daq.server.configuration.ConfigurationSource;

class CompositeBeamlineConfigurationTest {
	@Test
	void singleSource() {
		var cs = source().withSpringXml(append("one", "two"));
		var config = combinationOf(cs);
		assertThat(
				config.getSpringXml().toList(), containsURLs("config/dir/one", "config/dir/two"));
	}

	@Test
	void multipleSources() {
		var cs1 = source().withSpringXml(append("one", "two"));
		var cs2 =
				source().withSpringXml(append("/absolute/three", "four"))
						.withLogging(append("logging/customisation.xml"));
		var config = combinationOf(cs1, cs2);

		assertThat(
				config.getSpringXml().toList(),
				containsURLs(
						"config/dir/one", "config/dir/two", "/absolute/three", "config/dir/four"));
		assertThat(
				config.getLoggingConfiguration().toList(),
				containsURLs("config/dir/logging/customisation.xml"));
	}

	@Test
	void overwritingOptions() {
		var cs1 = source().withSpringXml(overwrite("one", "two"));
		var cs2 = source().withSpringXml(append("three", "four"));
		var config = combinationOf(cs1, cs2);

		assertThat(
				config.getSpringXml().toList(), containsURLs("config/dir/one", "config/dir/two"));
	}

	@Test
	void variableExpansion() {
		var cs1 = source().withSpringXml(append("path/${subdirectory}/server.xml"));
		var cs2 = source().withProperties(Map.of("subdirectory", "foo"));
		var config = combinationOf(cs1, cs2);

		assertThat(config.getSpringXml().toList(), containsURLs("config/dir/path/foo/server.xml"));
	}

	@Test
	void defaultVariableExpansion() {
		var cs1 = source().withSpringXml(append("path/${undefined:-default}.xml"));
		var config = combinationOf(cs1);

		assertThat(config.getSpringXml().toList(), containsURLs("config/dir/path/default.xml"));
	}

	@Test
	void overwritingProfiles() {
		var cs1 = source().withProfiles(overwrite("one", "two"));
		var cs2 = source().withProfiles(append("three", "four"));
		var config = combinationOf(cs1, cs2);

		assertThat(config.getProfiles().toList(), contains("one", "two"));
	}

	@Test
	void propertiesFilter() {
		var cs1 =
				source().withProperties(
								Map.of(
										"foo.bar", "one",
										"foo.buzz", "two",
										"fizz.buzz", "three"));
		var config = combinationOf(cs1);
		assertThat(
				config.properties(k -> k.startsWith("foo")),
				is(
						Map.of(
								"foo.bar", "one",
								"foo.buzz", "two")));
	}

	@Test
	void invalidPropertiesFile() {
		var cs = source().withPropertiesFiles(append("this/does/not/exist"));
		assertThrows(IllegalStateException.class, () -> combinationOf(cs));
	}

	@Test
	void propertyExpansion() {
		var cs = source().withPropertiesFiles(append("properties/common/java.properties"));
		var cs2 = source().withSpringXml(append("servers/${ixx.config.common}/server.xml"));
		var config = combinationOf("resources/ixx-config", cs, cs2);
		assertThat(
				config.getSpringXml().toList(),
				containsURLs("resources/ixx-config/servers/sample/server.xml"));
	}

	// Property files can only use the properties passed in at runtime via cli/envargs/system props
	// as the properties can't have been loaded yet
	@Test
	void propertyFileVariableExpansion() {
		var cs = source().withProperties(Map.of("sample", "common"));
		var cs2 = source().withPropertiesFiles(append("properties/${sample}/java.properties"));
		var config = combinationOf("resources/ixx-config", cs, cs2);
		assertThat(
				config.getPropertiesFiles().toList(),
				contains("resources/ixx-config/properties/common/java.properties"));
	}

	@Test
	void systemProperties() {
		String key = CompositeBeamlineConfiguration.class.getCanonicalName();
		System.setProperty(key, "foo");
		try {
			var conf = new CompositeBeamlineConfiguration(Path.of("config/dir"), Stream.empty());
			assertThat(conf.properties().getString(key), is("foo"));
			System.setProperty(key, "bar");
			// CombinedConfig includes a copy of System properties and doesn't update
			assertThat(conf.properties().getString(key), is("foo"));
		} finally {
			System.clearProperty(key);
		}
	}

	@Test
	void propertyPrecedence() {
		// properties passed in to the first source should override later ones
		var cs = source().withProperties(Map.of("gda.foo", "fizz"));
		var cs2 = source().withProperties(Map.of("gda.foo", "buzz"));
		var config = combinationOf(cs, cs2);

		assertThat(config.properties().getString("gda.foo"), is("fizz"));
	}

	private static Answer<ConfigurationOptions> append(String... options) {
		return inv -> new ConfigurationOptions(stream(options), APPEND);
	}

	private static Answer<ConfigurationOptions> overwrite(String... options) {
		return inv -> new ConfigurationOptions(stream(options), OVERWRITE);
	}

	private CompositeBeamlineConfiguration combinationOf(SourceBuilder... sources) {
		return combinationOf("config/dir", sources);
	}

	private CompositeBeamlineConfiguration combinationOf(String config, SourceBuilder... sources) {
		return new CompositeBeamlineConfiguration(
				Path.of(config), stream(sources).map(SourceBuilder::build));
	}

	private SourceBuilder source() {
		return new SourceBuilder();
	}

	private static class SourceBuilder {
		private ConfigurationSource source = mock(ConfigurationSource.class);

		private SourceBuilder() {
			// thenAnswer instead of theReturn so that streams are recreated each time
			when(source.getPropertiesFiles()).thenAnswer(append());
			when(source.getSpringXml()).thenAnswer(append());
			when(source.getProfiles()).thenAnswer(append());
			when(source.getLoggingConfiguration()).thenAnswer(append());
			when(source.getProperties()).thenReturn(emptyMap());
		}

		private SourceBuilder withSpringXml(Answer<ConfigurationOptions> xml) {
			when(source.getSpringXml()).thenAnswer(xml);
			return this;
		}

		private SourceBuilder withProfiles(Answer<ConfigurationOptions> profiles) {
			when(source.getProfiles()).thenAnswer(profiles);
			return this;
		}

		private SourceBuilder withPropertiesFiles(Answer<ConfigurationOptions> files) {
			when(source.getPropertiesFiles()).thenAnswer(files);
			return this;
		}

		private SourceBuilder withLogging(Answer<ConfigurationOptions> conf) {
			when(source.getLoggingConfiguration()).thenAnswer(conf);
			return this;
		}

		private SourceBuilder withProperties(Map<String, String> props) {
			when(source.getProperties()).thenReturn(props);
			return this;
		}

		private ConfigurationSource build() {
			return source;
		}
	}
}
