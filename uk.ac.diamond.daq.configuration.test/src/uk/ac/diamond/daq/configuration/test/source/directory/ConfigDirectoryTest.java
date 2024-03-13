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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.ac.diamond.daq.configuration.ConfigurationOptions.Action.APPEND;
import static uk.ac.diamond.daq.configuration.test.Matchers.containsOptions;
import static uk.ac.diamond.daq.configuration.test.Matchers.emptyOptions;

import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import uk.ac.diamond.daq.configuration.source.directory.ConfigDirectory;
import uk.ac.diamond.daq.configuration.source.directory.OptionsByMode;
import uk.ac.diamond.daq.configuration.source.directory.ConfigLayout.LayoutBuilder;

class ConfigDirectoryTest {
	@ParameterizedTest
	@ValueSource(strings = {"server", "client"})
	void emptyLayout(String process) {
		var dir = new ConfigDirectory(Path.of("config-dir"), LayoutBuilder.empty().build(process));
		assertThat(dir.getSpringXml(), is(emptyOptions(APPEND)));
		assertThat(dir.getProfiles(), is(emptyOptions(APPEND)));
		assertThat(dir.getPropertiesFiles(), is(emptyOptions(APPEND)));
		assertThat(dir.getLoggingConfiguration(), is(emptyOptions(APPEND)));
		assertThat(dir.getProperties(), is(emptyMap()));
	}

	@ParameterizedTest
	@ValueSource(strings = {"server", "client"})
	void pathResolution(String process) {
		var layout =
				LayoutBuilder.empty()
						.withSpringXml(new OptionsByMode(asList("servers/main/${variables.not.expanded}/server.xml")))
						.build(process);
		var dir = new ConfigDirectory(Path.of("config-dir"), layout);
		assertThat(
				dir.getSpringXml(),
				containsOptions(
						APPEND, "config-dir/servers/main/${variables.not.expanded}/server.xml"));
	}
}
