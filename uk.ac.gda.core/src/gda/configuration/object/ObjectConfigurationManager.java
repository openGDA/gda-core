/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.configuration.object;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 * @author tjs15132
 *
 */
public class ObjectConfigurationManager {

	/**
	 * static reference to the Singleton Configuration Manager instance.
	 */
	private static ObjectConfigurationManager OBJECT_CONFIG_MANAGER;

	private ObjectConfigurationManager() {

	}

	/**
	 * create the singleton instance, check for null condition of
	 * CONFIG_MANAGER. If CONFIG_MANAGER is null create the ConfigurationManager
	 * instance assign it to CONFIG_MANAGER. There is no need to have
	 * synchronized here(while creating ConfigurationManager).
	 *
	 * @return TEMPLATE_CONFIG_MANAGER, instance of TemplateConfigAssocManager.

	 */
	public static ObjectConfigurationManager getDefault() {
		if(OBJECT_CONFIG_MANAGER == null) {
			OBJECT_CONFIG_MANAGER = new ObjectConfigurationManager();
		}
		return OBJECT_CONFIG_MANAGER;
	}

	/**
	 * returns the XML configuration data
	 * 
	 * @param filename
	 * @return XML configuration data
	 * @throws FileNotFoundException
	 */
	public Reader getConfiguration(String filename)
			throws FileNotFoundException {

		return new FileReader(filename);
	}
}
