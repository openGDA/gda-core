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

package uk.ac.diamond.daq.server.configuration.test.source;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.ac.diamond.daq.server.configuration.ConfigurationOptions.Action.APPEND;
import static uk.ac.diamond.daq.server.configuration.ConfigurationOptions.Action.OVERWRITE;
import static uk.ac.diamond.daq.server.configuration.source.GdaEnvironment.fromEnvironment;
import static uk.ac.diamond.daq.server.configuration.test.Matchers.containsOptions;
import static uk.ac.diamond.daq.server.configuration.test.Matchers.emptyOptions;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class GdaEnvironmentTest {
	@Test
	void emptyEnvironment() {
		var env = fromEnvironment(Map.of());
		assertThat(env.configDirectory(), is(Optional.empty()));
		assertThat(env.getSpringXml(), is(emptyOptions(APPEND)));
		assertThat(env.getProfiles(), is(emptyOptions(APPEND)));
		assertThat(env.getLoggingConfiguration(), is(emptyOptions(APPEND)));
		assertThat(env.getPropertiesFiles(), is(emptyOptions(APPEND)));
		assertThat(env.getProperties(), is(emptyMap()));
	}

	@Test
	void configDirectory() {
		var env = fromEnvironment(Map.of("GDA_CONFIG", "path/to/config"));
		assertThat(env.configDirectory(), is(Optional.of("path/to/config")));
	}

	@Test
	void springXml() {
		var env =
				fromEnvironment(
						Map.of("GDA_SPRING_XML", "path/to/server1.xml,path/to/server2.xml"));
		assertThat(
				env.getSpringXml(),
				containsOptions(APPEND, "path/to/server1.xml", "path/to/server2.xml"));
	}

	@Test
	void springXmlNoDefault() {
		var env =
				fromEnvironment(
						Map.of(
								"GDA_SPRING_XML", "path/to/server1.xml,path/to/server2.xml",
								"GDA_NO_DEFAULT_SPRING_XML", "1" // value doesn't matter
								));
		assertThat(
				env.getSpringXml(),
				containsOptions(OVERWRITE, "path/to/server1.xml", "path/to/server2.xml"));

		env = fromEnvironment(Map.of("GDA_NO_DEFAULT_SPRING_XML", "1"));
		assertThat(env.getSpringXml(), emptyOptions(OVERWRITE));
	}

	@Test
	void profiles() {
		var env = fromEnvironment(Map.of("GDA_PROFILES", "prof1,prof3"));
		assertThat(env.getProfiles(), containsOptions(APPEND, "prof1", "prof3"));
	}

	@Test
	void profilesNoDefault() {
		var env = fromEnvironment(Map.of("GDA_PROFILES", "prof1", "GDA_NO_DEFAULT_PROFILES", ""));
		assertThat(env.getProfiles(), containsOptions(OVERWRITE, "prof1"));

		env = fromEnvironment(Map.of("GDA_NO_DEFAULT_PROFILES", ""));
		assertThat(env.getProfiles(), emptyOptions(OVERWRITE));
	}

	@Test
	void loggingConfiguration() {
		var env =
				fromEnvironment(Map.of("GDA_LOGGING", "path/to/logging1.xml,path/to/logging2.xml"));
		assertThat(
				env.getLoggingConfiguration(),
				containsOptions(APPEND, "path/to/logging1.xml", "path/to/logging2.xml"));
	}

	@Test
	void loggingConfigurationNoDefault() {
		var env =
				fromEnvironment(
						Map.of(
								"GDA_LOGGING", "path/to/logging1.xml,path/to/logging2.xml",
								"GDA_NO_DEFAULT_LOGGING", ""));
		assertThat(
				env.getLoggingConfiguration(),
				containsOptions(OVERWRITE, "path/to/logging1.xml", "path/to/logging2.xml"));

		env = fromEnvironment(Map.of("GDA_NO_DEFAULT_LOGGING", ""));
		assertThat(env.getLoggingConfiguration(), emptyOptions(OVERWRITE));
	}

	@Test
	void propertiesFiles() {
		var env =
				fromEnvironment(
						Map.of("GDA_PROPERTIES", "path/to/java1.props,path/to/java2.props"));
		assertThat(
				env.getPropertiesFiles(),
				containsOptions(APPEND, "path/to/java1.props", "path/to/java2.props"));
	}

	@Test
	void propertiesFilesNoDefault() {
		var env =
				fromEnvironment(
						Map.of(
								"GDA_PROPERTIES", "path/to/java1.props,path/to/java2.props",
								"GDA_NO_DEFAULT_PROPERTIES", ""));
		assertThat(
				env.getPropertiesFiles(),
				containsOptions(OVERWRITE, "path/to/java1.props", "path/to/java2.props"));

		env = fromEnvironment(Map.of("GDA_NO_DEFAULT_PROPERTIES", ""));
		assertThat(env.getPropertiesFiles(), emptyOptions(OVERWRITE));
	}

	@Test
	void properties() {
		var env =
				fromEnvironment(
						Map.of(
								"GDA_FOO_BAR", "one",
								"GDA_FIZZ_BUZZ", "two",
								"NO_GDA_PREFIX", "three"));
		assertThat(env.getProperties(), is(Map.of("gda.foo.bar", "one", "gda.fizz.buzz", "two")));
		assertThat(env.getProperty("gda.foo.bar"), is(Optional.of("one")));
		assertThat(env.getProperty("gda.fizz.buzz"), is(Optional.of("two")));
		assertThat(env.getProperty("no.gda.prefix"), is(Optional.empty()));
		assertThat(env.getProperty("gda.not.present"), is(Optional.empty()));
	}
}
