/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server;

import uk.ac.diamond.daq.server.configuration.ConfigurationLoader;
import uk.ac.diamond.daq.server.configuration.IGDAConfigurationService;

public class ApplicationEnvironment {

	private ApplicationEnvironment() {
		throw new UnsupportedOperationException("Class cannot be instantiated");
	}

	private static boolean initialzed = false;

	/**
	 * Initialises the configuration loader
	 *
	 * @throws Exception
	 */
	public static void initialize() throws Exception {
		if (!initialzed) {
			injectBeamlineConfigurationService();
			initialzed = true;
		}
	}

	/**
	 * Retrieves the correct configuration service for the beamline via the {@link ConfigurationLoader} component
	 * and then uses it to load the configuration for the beamline.
	 *
	 * @throws Exception    If the {@link ConfigurationLoader} component has not started or the required configuration
	 *                      service cannot be retrieved.
	 */
	private static void injectBeamlineConfigurationService() throws Exception {
		if (ConfigurationLoader.getInstance() == null) {
			throw new Exception("The ConfigurationLoader component was not activated");
		}
		IGDAConfigurationService configurationService = ConfigurationLoader.getInstance().getConfigurationService();
		if (configurationService == null){
			throw new Exception("The Configuration Service could not be retrieved");
		}
		GDAServerApplication.setConfigurationService(configurationService);
	}
}
