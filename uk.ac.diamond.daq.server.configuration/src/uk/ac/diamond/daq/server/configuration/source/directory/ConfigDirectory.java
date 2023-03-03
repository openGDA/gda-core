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

package uk.ac.diamond.daq.server.configuration.source.directory;

import java.nio.file.Path;
import java.util.Map;

import uk.ac.diamond.daq.server.configuration.ConfigurationOptions;
import uk.ac.diamond.daq.server.configuration.ConfigurationSource;
import uk.ac.diamond.daq.server.configuration.ModeAwareConfigSource;

/**
 * Representation of a configuration directory made up of path to its root and a layout that defines
 * where, relative to the root, each aspect of the configuration can be found.
 */
public record ConfigDirectory(Path root, ConfigLayout layout) implements ModeAwareConfigSource {

	@Override
	public ConfigurationOptions getSpringXml() {
		return layout.springXml(root);
	}

	@Override
	public ConfigurationOptions getProfiles() {
		return layout.profiles();
	}

	@Override
	public ConfigurationOptions getPropertiesFiles() {
		return layout.propertiesFiles(root);
	}

	@Override
	public ConfigurationOptions getLoggingConfiguration() {
		return layout.loggingConfiguration(root);
	}

	@Override
	public Map<String, String> getProperties() {
		return layout.defaultProperties();
	}

	@Override
	public Map<String, String> systemProperties() {
		return layout.systemProperties();
	}

	@Override
	public ConfigurationSource withMode(String mode) {
		return new ConfigDirectory(root, layout.withMode(mode));
	}
}
