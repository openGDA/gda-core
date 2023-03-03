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
import static uk.ac.diamond.daq.server.configuration.test.Matchers.containsOptions;
import static uk.ac.diamond.daq.server.configuration.test.Matchers.emptyOptions;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import uk.ac.diamond.daq.server.configuration.source.CliOptions;

/**
 * Testing the handling of GDA configuration. See {@link CliArgsTest} for testing the underlying
 * argument parsing.
 */
class CliOptionsTest {
	@Test
	void emptyArgs() {
		var options = CliOptions.parse(new String[] {});
		assertThat(options.getSpringXml(), is(emptyOptions(APPEND)));
		assertThat(options.getSpringXml(), is(emptyOptions(APPEND)));
		assertThat(options.getProfiles(), is(emptyOptions(APPEND)));
		assertThat(options.getLoggingConfiguration(), is(emptyOptions(APPEND)));
		assertThat(options.getPropertiesFiles(), is(emptyOptions(APPEND)));
		assertThat(options.getProperties(), is(emptyMap()));
	}

	@Test
	void springXml() {
		var opts = parse("--spring-xml path/to/server1.xml --spring-xml path/to/server2.xml");
		assertThat(
				opts.getSpringXml(),
				containsOptions(APPEND, "path/to/server1.xml", "path/to/server2.xml"));

		opts = parse("-x path/to/server1.xml -x path/to/server2.xml");
		assertThat(
				opts.getSpringXml(),
				containsOptions(APPEND, "path/to/server1.xml", "path/to/server2.xml"));

		opts = parse("-x path/to/server1.xml --spring-xml path/to/server2.xml");
		assertThat(
				opts.getSpringXml(),
				containsOptions(APPEND, "path/to/server1.xml", "path/to/server2.xml"));
	}

	@Test
	void springXmlNoDefault() {
		var opts =
				parse(
						"--spring-xml path/to/server1.xml --spring-xml path/to/server2.xml"
								+ " --no-default-spring-xml");
		assertThat(
				opts.getSpringXml(),
				containsOptions(OVERWRITE, "path/to/server1.xml", "path/to/server2.xml"));

		opts = parse("--no-default-spring-xml");
		assertThat(opts.getSpringXml(), emptyOptions(OVERWRITE));
	}

	@Test
	void profiles() {
		var opts = parse("--profile prof1 --profile prof3");
		assertThat(opts.getProfiles(), containsOptions(APPEND, "prof1", "prof3"));

		opts = parse("-p prof1 -p prof3");
		assertThat(opts.getProfiles(), containsOptions(APPEND, "prof1", "prof3"));

		opts = parse("--profile prof1 -p prof3");
		assertThat(opts.getProfiles(), containsOptions(APPEND, "prof1", "prof3"));

	}

	@Test
	void profilesNoDefault() {
		var opts = parse("--profile prof1 --no-default-profile");
		assertThat(opts.getProfiles(), containsOptions(OVERWRITE, "prof1"));

		opts = parse("--no-default-profile");
		assertThat(opts.getProfiles(), emptyOptions(OVERWRITE));

		opts = parse("-p one -p two -P");
		assertThat(opts.getProfiles(), containsOptions(OVERWRITE, "one", "two"));
	}

	@Test
	void loggingConfiguration() {
		var opts = parse("--logging path/to/logging1.xml --logging path/to/logging2.xml");
		assertThat(
				opts.getLoggingConfiguration(),
				containsOptions(APPEND, "path/to/logging1.xml", "path/to/logging2.xml"));

		opts = parse("-l path/to/logging1.xml -l path/to/logging2.xml");
		assertThat(
				opts.getLoggingConfiguration(),
				containsOptions(APPEND, "path/to/logging1.xml", "path/to/logging2.xml"));

		opts = parse("-l path/to/logging1.xml --logging path/to/logging2.xml");
		assertThat(
				opts.getLoggingConfiguration(),
				containsOptions(APPEND, "path/to/logging1.xml", "path/to/logging2.xml"));

	}

	@Test
	void loggingConfigurationNoDefault() {
		var opts =
				parse("--logging path/to/logging1.xml --logging path/to/logging2.xml --no-default-logging");
		assertThat(
				opts.getLoggingConfiguration(),
				containsOptions(OVERWRITE, "path/to/logging1.xml", "path/to/logging2.xml"));

		opts = parse("--no-default-logging");
		assertThat(opts.getLoggingConfiguration(), emptyOptions(OVERWRITE));
	}

	@Test
	void propertiesFiles() {
		var opts = parse("--properties path/to/java1.props --properties path/to/java2.props");
		assertThat(
				opts.getPropertiesFiles(),
				containsOptions(APPEND, "path/to/java1.props", "path/to/java2.props"));

		opts = parse("-k path/to/java1.props -k path/to/java2.props");
		assertThat(
				opts.getPropertiesFiles(),
				containsOptions(APPEND, "path/to/java1.props", "path/to/java2.props"));

		opts = parse("-k path/to/java1.props --properties path/to/java2.props");
		assertThat(
				opts.getPropertiesFiles(),
				containsOptions(APPEND, "path/to/java1.props", "path/to/java2.props"));
	}

	@Test
	void propertiesFilesNoDefault() {
		var opts =
				parse(
						"--properties path/to/java1.props --properties path/to/java2.props"
								+ " --no-default-properties");
		assertThat(
				opts.getPropertiesFiles(),
				containsOptions(OVERWRITE, "path/to/java1.props", "path/to/java2.props"));

		opts = parse("--no-default-properties");
		assertThat(opts.getPropertiesFiles(), emptyOptions(OVERWRITE));
	}

	@Test
	void properties() {
		var opts = parse("--foo-bar one --fizz-buzz two");
		assertThat(opts.getProperties(), is(Map.of("foo.bar", "one", "fizz.buzz", "two")));
		assertThat(opts.getProperty("foo.bar"), is(Optional.of("one")));
		assertThat(opts.getProperty("fizz.buzz"), is(Optional.of("two")));
		assertThat(opts.getProperty("not.present"), is(Optional.empty()));
	}

	@Test
	void configDirectory() {
		var opts = parse("--gda-config path/to/config");
		assertThat(opts.configDirectory(), is(Optional.of("path/to/config")));
		assertThat(opts.getProperty("gda.config"), is(Optional.of("path/to/config")));

		opts = parse("-c path/to/config");
		assertThat(opts.configDirectory(), is(Optional.of("path/to/config")));
		assertThat(opts.getProperty("gda.config"), is(Optional.of("path/to/config")));

		opts = parse("");
		assertThat(opts.configDirectory(), is(Optional.empty()));
		assertThat(opts.getProperty("gda.config"), is(Optional.empty()));
	}

	@Test
	void noSystemProperties() {
		var opts = parse("");
		assertThat(opts.systemProperties(), is(emptyMap()));
	}

	private CliOptions parse(String argv) {
		return CliOptions.parse(argv.split(" "));
	}
}
