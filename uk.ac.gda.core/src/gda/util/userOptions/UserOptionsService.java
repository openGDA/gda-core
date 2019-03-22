/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.util.userOptions;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

import gda.factory.Configurable;
import gda.factory.Findable;

/**
 * Service Interface for maintaining run-time options with a set of current values stored in the specified location,
 * typically the current visit. A set of default (template) values are maintained in the beamline configuration.
 *
 * Each UserOption is referenced in the service-provided Map by a key.
 *
 * Properties are used to specify the template folder location and the default options filename
 */
public interface UserOptionsService extends Findable, Configurable {

	String PROP_OPTIONS_FILENAME = "gda.util.userOptions.defaultConfigName";
	String PROP_TEMPLATE_DIRECTORY = "gda.util.userOptions.configDirTemplate";

	/**
	 * Create a UserOptionsMap from the configuration template, using default values in template
	 * @return UserOptionsMap using default template values
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	UserOptionsMap createOptionsMapFromTemplate() throws ConfigurationException, IOException;

	/**
	 * Fetches UserOptionsMap from current visit folder
	 * @return UserOptionsMap with values from options file in visit folder
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	UserOptionsMap getOptionsCurrent()  throws ConfigurationException, IOException;

	/**
	 * Fetches UserOptionsMap from specified file
	 * @param directory location of options file name
	 * @param filename for options file
	 * @return UserOptionsMap with template defaults updated by specified file
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	UserOptionsMap getOptions(String directory, String filename) throws ConfigurationException, IOException;

	/**
	 * Indicates that a template is available
	 * @return true if service is configured and template exists
	 */
	Boolean hasTemplate();

	/**
	 * Replaces UserOptionsMap from specified file with the default template values
	 * @param directory location of options file name
	 * @param filename for options file
	 * @return UserOptionsMap using default template values
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	UserOptionsMap resetOptions(String directory, String filename) throws ConfigurationException, IOException;

	/**
	 * Saves the UserOptionsMap to the specified file
	 * @param directory location of options file name
	 * @param filename for options file
	 * @param options map of options to save
	 * @return UserOptionsMap
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	UserOptionsMap saveOptions(String directory, String filename, UserOptionsMap options)
			throws ConfigurationException, IOException;

	/**
	 * Saves the UserOptionsMap to the current visit directory
	 * @param options map of options to save
	 * @return UserOptionsMap
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	UserOptionsMap saveOptionsCurrent(UserOptionsMap options) throws ConfigurationException, IOException;

}
