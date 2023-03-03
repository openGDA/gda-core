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

package uk.ac.diamond.daq.server.configuration;

import static java.util.Collections.emptyMap;

import java.util.Map;

/** Anything that can be used as a source of configuration for GDA */
public interface ConfigurationSource {
	ConfigurationOptions getSpringXml();

	ConfigurationOptions getProfiles();

	ConfigurationOptions getPropertiesFiles();

	ConfigurationOptions getLoggingConfiguration();
	/**
	 * Properties that are set as defaults directly in the configuration source. As these do not
	 * need any files to be read to be available, they can be used for variable expansion in the
	 * paths returned by propertiesFiles whereas all other paths can use properties read from files
	 * as well.
	 *
	 * @return a map of default properties to be available before reading any properties files.
	 */
	Map<String, String> getProperties();
	/**
	 * Any properties that should be set directly to System.properties rather than being included
	 * with the rest of the GDA properties. This is mostly intended for configuring third party
	 * libraries.
	 *
	 * @return map of system properties to be set
	 */
	default Map<String, String> systemProperties() {
		return emptyMap();
	}
}
