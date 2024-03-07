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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import uk.ac.diamond.daq.configuration.ConfigurationOptions;
import uk.ac.diamond.daq.configuration.ConfigurationSource;
import uk.ac.diamond.daq.configuration.ConfigurationOptions.Action;

class ConfigurationOptionsTest {
	@Test
	void combineAppendedOptions() {
		var one = mock(ConfigurationSource.class);
		when(one.getSpringXml())
				.thenReturn(new ConfigurationOptions(Stream.of("one", "two"), Action.APPEND));
		var two = mock(ConfigurationSource.class);
		when(two.getSpringXml())
				.thenReturn(new ConfigurationOptions(Stream.of("three", "four"), Action.APPEND));
		var three = mock(ConfigurationSource.class);
		when(three.getSpringXml())
				.thenReturn(new ConfigurationOptions(Stream.of("five", "six"), Action.APPEND));
		var options =
				ConfigurationOptions.effectiveOptions(
								asList(one, two, three), ConfigurationSource::getSpringXml)
						.toList();
		assertThat(options, contains("one", "two", "three", "four", "five", "six"));
	}

	@Test
	void combineOverwrittenOptions() {
		var one = mock(ConfigurationSource.class);
		when(one.getSpringXml())
				.thenReturn(new ConfigurationOptions(Stream.of("one", "two"), Action.APPEND));
		var two = mock(ConfigurationSource.class);
		when(two.getSpringXml())
				.thenReturn(new ConfigurationOptions(Stream.of("three", "four"), Action.OVERWRITE));
		var three = mock(ConfigurationSource.class);
		when(three.getSpringXml())
				.thenReturn(new ConfigurationOptions(Stream.of("five", "six"), Action.APPEND));
		var options =
				ConfigurationOptions.effectiveOptions(
								asList(one, two, three), ConfigurationSource::getSpringXml)
						.toList();
		assertThat(options, contains("one", "two", "three", "four"));
	}
}
