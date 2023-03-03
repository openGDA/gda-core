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

package uk.ac.diamond.daq.server.configuration.test.source.directory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import uk.ac.diamond.daq.server.configuration.source.directory.ConfigLayout.LayoutBuilder;
import uk.ac.diamond.daq.server.configuration.source.directory.OptionsByMode;

class ConfigLayoutTest {

	@Test
	void buildWithUnknownProcess() {
		LayoutBuilder empty = LayoutBuilder.empty();
		assertThrows(IllegalArgumentException.class, () -> empty.build("not-a-process"));
	}

	@Test
	void mergingAppendsOptions() {
		var layout1 = LayoutBuilder.empty().withLogging(new OptionsByMode("common_path_1"));
		var layout2 = LayoutBuilder.empty().withLogging(new OptionsByMode("common_path_2"));
		var logging = layout1.update(layout2).build("server").loggingConfiguration(Path.of("root"));
		assertThat(logging.options().toList(), contains("root/common_path_1", "root/common_path_2"));
	}
}
