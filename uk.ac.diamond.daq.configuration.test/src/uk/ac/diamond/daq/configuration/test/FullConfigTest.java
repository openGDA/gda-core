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

package uk.ac.diamond.daq.configuration.test;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.ac.diamond.daq.configuration.test.Matchers.containsURLs;

import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;

import uk.ac.diamond.daq.configuration.CompositeBeamlineConfiguration;
import uk.ac.diamond.daq.configuration.source.CliOptions;
import uk.ac.diamond.daq.configuration.source.GdaEnvironment;
import uk.ac.diamond.daq.configuration.source.directory.ConfigRef;

/** Full integration test of all configuration sources wrapped in a composite config */
class FullConfigTest {
	@Test
	void withoutOverrides() {
		var config = serverConfig("", emptyMap());

		// default properties taken from config.toml
		assertThat(config.properties().getString("gda.mode"), is("dummy"));
		// properties loaded from linked property files
		assertThat(config.properties().getString("common.to.all.config"), is("ixx-dummy"));
		// Other config attributes
		assertThat(config.getProfiles().toList(), contains("ixx-profile", "dls-profile"));
		assertThat(
				config.getSpringXml().toList(),
				containsURLs(
						"resources/ixx-config/servers/common/server.xml",
						"resources/ixx-shared/list/of",
						"resources/ixx-shared/dummy/files",
						"resources/ixx-shared/shared/server.xml",
						"resources/dls-config/common/dls/spring.xml",
						"resources/core-config/servers/standard.xml",
						"resources/core-config/servers/non-standard.xml"));
		assertThat(
				config.getPropertiesFiles().toList(),
				containsURLs(
						"resources/ixx-config/properties/dummy/dummy_instance_java.properties",
						"resources/ixx-config/properties/common/java.properties",
						"resources/ixx-shared/properties/dummy/dummy_instance_java.properties",
						"resources/dls-config/properties/java.properties",
						"resources/dls-config/properties/dummy_dls_java.properties",
						"resources/core-config/properties/core.properties"));
		assertThat(
				config.getLoggingConfiguration().toList(),
				containsURLs(
						"resources/ixx-config/logging/customisation.xml",
						"resources/core-config/logging_config.xml"));
		assertThat(
				config.properties(key -> key.startsWith("gda.")),
				is(
						Map.of(
								"gda.mode", "dummy",
								"gda.core.config.key", "core-value",
								"gda.shared.key", "shared-value")));

		assertThat(
				config.systemProperties(),
				is(Map.of("core.system.property", "property/for/dummy/gda",
						"property.resolution", "ixx-dummy")));
	}

	@Test
	void modeOverride() {
		var config = serverConfig("--gda-mode live", emptyMap());

		assertThat(config.properties().getString("gda.mode"), is("live"));
		assertThat(config.properties().getString("common.to.all.config"), is("ixx-live"));
		assertThat(
				config.getSpringXml().toList(),
				containsURLs(
						"resources/ixx-config/servers/live/server.xml",
						"resources/ixx-config/servers/common/server.xml",
						"resources/ixx-shared/shared/server.xml",
						"resources/dls-config/common/dls/spring.xml",
						"resources/core-config/servers/standard.xml",
						"resources/core-config/servers/non-standard.xml"
						));
	}

	@Test
	void disabledDefaults() {
		var config = serverConfig("--no-default-spring-xml --no-default-profile", emptyMap());

		assertThat(config.getProfiles().toList(), is(empty()));
		assertThat(config.getSpringXml().toList(), is(empty()));

		config = serverConfig("", Map.of("GDA_NO_DEFAULT_PROFILES", "1"));
		assertThat(config.getProfiles().toList(), is(empty()));
	}

	@Test
	void invalidOverrides() {
		Map<String, String> env = emptyMap();
		assertThrows(
				IllegalStateException.class,
				() -> serverConfig("--properties path/does/not/exist", env));
	}

	@Test
	void modePropertyResolutionOrder() {
		var config = serverConfig("", emptyMap());
		assertThat(config.properties().getString("common.to.all.config"), is("ixx-dummy"));
		assertThat(config.systemProperties().get("property.resolution"), is("ixx-dummy"));

		var liveConfig = serverConfig("--gda-mode live", emptyMap());
		assertThat(liveConfig.properties().getString("common.to.all.config"), is("ixx-live"));
		assertThat(liveConfig.systemProperties().get("property.resolution"), is("ixx-live"));
	}

	private CompositeBeamlineConfiguration serverConfig(String args, Map<String, String> envVars) {
		return ixxConfig("server", args, envVars);
	}

	private CompositeBeamlineConfiguration ixxConfig(
			String process, String args, Map<String, String> envVars) {
		var cli =
				CliOptions.parse(
						args.split(" ")); // 'real' calls to this will use better arg splitting
		var env = GdaEnvironment.fromEnvironment(envVars);
		var directories = new ConfigRef("ixx-config").resolveConfigs(process, Path.of("resources"));
		return new CompositeBeamlineConfiguration(Path.of(""), concat(of(cli, env), directories), emptyList());
	}
}
