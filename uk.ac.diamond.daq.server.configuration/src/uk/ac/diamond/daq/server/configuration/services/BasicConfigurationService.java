package uk.ac.diamond.daq.server.configuration.services;

import static uk.ac.diamond.daq.server.configuration.ConfigurationDefaults.*;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.ComponentContext;

import uk.ac.diamond.daq.server.configuration.IGDAConfigurationService;
import uk.ac.diamond.daq.server.configuration.commands.ObjectServerCommand;


public class BasicConfigurationService implements IGDAConfigurationService {
	private final List<ObjectServerCommand> objectServerCommands = new ArrayList<ObjectServerCommand>();
	private String instanceConfigRoot;

	@Override
	public void loadConfiguration() {
		final String[] profiles = getProfiles();
		final String[] springPathsStrings = APP_SPRING_XML_FILE_PATHS.toString().split(",");

		// check they're both the same length

		for (int i = 0; i < profiles.length; i++) {
			objectServerCommands.add(new ObjectServerCommand(profiles[i], springPathsStrings[i]));
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
	public List<ObjectServerCommand> getObjectServerCommands() {
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

	protected void activate(final ComponentContext context) {
		System.out.println("Starting Basic Configuration Service");
	}
}

