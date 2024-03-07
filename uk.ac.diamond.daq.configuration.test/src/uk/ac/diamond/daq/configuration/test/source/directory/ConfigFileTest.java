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

package uk.ac.diamond.daq.configuration.test.source.directory;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.ac.diamond.daq.configuration.ConfigurationOptions.Action.APPEND;
import static uk.ac.diamond.daq.configuration.test.Matchers.containsOptions;
import static uk.ac.diamond.daq.configuration.test.Matchers.emptyOptions;

import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;

import uk.ac.diamond.daq.configuration.source.directory.ConfigFile;
import uk.ac.diamond.daq.configuration.source.directory.ConfigRef;

class ConfigFileTest {

	private static ObjectMapper mapper = new ObjectMapper(TomlFactory.builder().build());

	@ParameterizedTest
	@ValueSource(strings = {"server", "client"})
	void emptyString(String process) throws Exception {
		var root = Path.of(".");
		var config = readConfig("");

		assertThat(config.getRoot(), is(nullValue()));
		assertThat(config.extras().toList(), is(empty()));

		var layout = config.asLayout().build(process);
		assertThat(layout.springXml(root), emptyOptions(APPEND));
		assertThat(layout.propertiesFiles(root), emptyOptions(APPEND));
		assertThat(layout.loggingConfiguration(root), emptyOptions(APPEND));
		assertThat(layout.defaultProperties(), is(emptyMap()));
		assertThat(layout.profiles(), emptyOptions(APPEND));
	}

	@Test
	void fullFile() throws Exception {
		var root = Path.of("");
		var content =
				"""
				spring-xml = ["path/to/common1.xml", "path/to/common2.xml"]
				logging = ["path/to/logging.xml"]
				properties = ["path/to/java.properties"]
				profiles = ["one", "two"]

				server.spring-xml = ["server.xml"]
				client.spring-xml.common = "client.xml"
				client.spring-xml.mode.live = ["live_client.xml"]
				client.spring-xml.mode.dummy= "live_client.xml"


				server.profiles = "server-profile"
				client.profiles = ["client-profile"]

				server.properties = ["server.properties"]
				client.properties = "client.properties"

				server.logging = "server.logging"
				client.logging = ["client.logging"]

				[defaults]
				default = "value"
				[system]
				"common.system.property" = "common"
				"third.party.configuration" = "helloWorld"

				[server.defaults]
				"common.property" = "server.value"
				[client.defaults]
				"common.property" = "client.value"

				[server.system]
				"third.party.configuration" = "server.system"
				[client.system]
				"third.party.configuration" = "client.system"

				""";
		var config = readConfig(content);

		assertThat(config.getRoot(), is(nullValue()));

		var layout = config.asLayout().build("server");
		assertThat(
				layout.springXml(root),
				containsOptions(
						APPEND, "path/to/common1.xml", "path/to/common2.xml", "server.xml"));
		assertThat(
				layout.propertiesFiles(root),
				containsOptions(APPEND, "path/to/java.properties", "server.properties"));
		assertThat(
				layout.loggingConfiguration(root),
				containsOptions(APPEND, "path/to/logging.xml", "server.logging"));
		assertThat(
				layout.defaultProperties(),
				is(Map.of("default", "value", "common.property", "server.value")));
		assertThat(layout.profiles(), containsOptions(APPEND, "one", "two", "server-profile"));
		assertThat(
				layout.systemProperties(),
				is(
						Map.of(
								"third.party.configuration",
								"server.system",
								"common.system.property",
								"common")));
	}

	@Test
	void extrasByRoot() throws Exception {
		var content =
				"""
				[extras]
				facility = "path/to/facility"
				core = "path/to/core"
				""";
		var config = readConfig(content);
		var extras = config.extras().toList();
		assertThat(extras.get(0), is(equalTo(new ConfigRef("path/to/facility"))));
		assertThat(extras.get(1), is(equalTo(new ConfigRef("path/to/core"))));
	}

	@Test
	void extrasByTable() throws Exception {
		var content =
				"""
				[extras.facility]
				root = "path/to/facility"
				[extras.core]
				root = "path/to/core"
				""";
		var config = readConfig(content);
		var extras = config.extras().toList();
		assertThat(extras.get(0).root(), is(Path.of("path/to/facility")));
		assertThat(extras.get(1).root(), is(Path.of("path/to/core")));
	}

	@Test
	void singleStringLists() throws Exception {
		var content = """
				spring-xml = "path/to/server.xml"
				""";
		var config = readConfig(content);
		var serverXml =
				config.asLayout().build("server").springXml(Path.of(".")).options().toList();
		assertThat(serverXml, contains("./path/to/server.xml"));
	}

	@Test
	void expandedModeLists() throws Exception {
		var content = """
				spring-xml.common = "path/to/server.xml"
				spring-xml.mode.live = "path/to/live/server.xml"
				spring-xml.mode.dummy= "path/to/dummy/server.xml"
				""";
		var config = readConfig(content);
		var serverXml = config.asLayout().build("server").withMode("live").springXml(Path.of(".")).options().toList();
		assertThat(serverXml, contains("./path/to/live/server.xml", "./path/to/server.xml"));
	}

	@Test
	void extrasWithoutRoot() {
		assertThrows(
				JacksonException.class,
				() ->
						readConfig(
								"""
				[extras.core]
				spring-xml = "path/to/server.xml"
				"""));
		assertThrows(
				JacksonException.class,
				() ->
						readConfig(
								"""
				[extras.core]
				root = ""
				spring-xml = "path/to/server.xml"
				"""));
		assertThrows(
				JacksonException.class,
				() ->
						readConfig(
								"""
				[extras.core]
				root = "	"
				spring-xml = "path/to/server.xml"
				"""));
	}

	@Test
	void invalidFilePaths() {
		assertThrows(JacksonException.class, () -> readConfig("""
				spring-xml = 23
				"""));
		assertThrows(
				JacksonException.class,
				() -> readConfig("""
				spring-xml = ["path/to/server.xml", 23]
				"""));
	}

	private static ConfigFile readConfig(String content) throws Exception {
		return mapper.readValue(content, ConfigFile.class);
	}
}
