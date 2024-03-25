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

package uk.ac.diamond.daq.configuration;

import java.net.URL;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.configuration2.Configuration;

import gda.configuration.properties.PropertiesConfig;
import uk.ac.diamond.daq.configuration.properties.ConfigurationServicePropertyConfig;

public interface BeamlineConfiguration {
	Stream<URL> getSpringXml();

	Stream<URL> getPropertiesFiles();

	Stream<URL> getLoggingConfiguration();

	Stream<String> getProfiles();

	Configuration properties();

	Map<String, String> properties(Predicate<String> keyFilter);
	/**
	 * The properties made available directly rather than loaded via files
	 *
	 * @deprecated Temporary function during transition to new configuration
	 */
	@Deprecated
	Map<String, String> directProperties();

	default PropertiesConfig asPropertiesConfig() {
		return new ConfigurationServicePropertyConfig(properties());
	}
}
