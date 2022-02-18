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

package uk.ac.diamond.daq.server.configuration.services;

import static uk.ac.diamond.daq.server.configuration.ConfigurationDefaults.APP_MODE;
import static uk.ac.diamond.daq.server.configuration.ConfigurationDefaults.APP_PROFILES;
import static uk.ac.diamond.daq.server.configuration.ConfigurationDefaults.APP_SPRING_XML_FILE_PATHS;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import uk.ac.diamond.daq.server.configuration.ConfigurationDefaults;
import uk.ac.diamond.daq.server.configuration.IGDAConfigurationService;
import uk.ac.diamond.daq.server.configuration.commands.ObjectFactoryCommand;
import uk.ac.diamond.daq.server.configuration.commands.ServerCommand;

@Component(name = "BasicConfigurationService", immediate = true, property = "configuration.layout=STANDARD")
public class BasicConfigurationService implements IGDAConfigurationService {
	private final List<ServerCommand> objectServerCommands = new ArrayList<>();
	private String instanceConfigRoot;

	@Override
	public void loadConfiguration() {
		ConfigurationDefaults.initialise();
		final String[] profiles = getProfiles();
		final String[] springPathsStrings = APP_SPRING_XML_FILE_PATHS.toString().split(",");

		// check they're both the same length

		for (int i = 0; i < profiles.length; i++) {
			objectServerCommands.add(new ObjectFactoryCommand(springPathsStrings[i]));
		}
		// Jonathan's change (gerrit 1251)_ should in future load the properties through Spring making them available through the environment
		// of the individual object servers. Currently they are loaded statically when the object server initialises its logging.
	}


	@Override
	public String getMode() {
		return APP_MODE.toString();
	}

	@Override
	public String[] getProfiles() {
		return APP_PROFILES.toString().split(",");
	}

	@Override
	public List<ServerCommand> getObjectServerCommands() {
		return objectServerCommands;
	}

	@Override
	public void setInstanceConfigRoot(String path) {
		instanceConfigRoot = path;
	}

	@Override
	public String getInstanceConfigRoot() {
		return instanceConfigRoot;
	}

	@Activate
	protected void activate(final ComponentContext context) {
		System.out.println("Starting Basic Configuration Service");
	}
}

