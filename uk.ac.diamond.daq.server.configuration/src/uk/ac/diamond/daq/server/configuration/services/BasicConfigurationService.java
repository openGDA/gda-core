package uk.ac.diamond.daq.server.configuration.services;

import static uk.ac.diamond.daq.server.configuration.ConfigurationDefaults.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.ComponentContext;

import uk.ac.diamond.daq.server.configuration.IGDAConfigurationService;
import uk.ac.diamond.daq.server.configuration.commands.ObjectServerCommand;
import uk.ac.diamond.daq.server.configuration.commands.SubProcessCommand;


public class BasicConfigurationService implements IGDAConfigurationService {
	private final Map<ServerType, SubProcessCommand> commands = new HashMap<ServerType, SubProcessCommand>();
	private final List<ObjectServerCommand> objectServerCommands = new ArrayList<ObjectServerCommand>();
	private String instanceConfigRoot;

	@Override
	public void loadConfiguration() {
		// enum initialised here - sets up java properties as passed in or uses defaults
		initialiseObjectServerEnvironment();

		commands.put(ServerType.NAME, new SubProcessCommand(buildNameServerCommand(), APP_CORBA_CLASSPATH));
		commands.put(ServerType.LOG, new SubProcessCommand(buildLogServerCommand((String[])null), APP_BASE_SERVER_CLASSPATH));
		commands.put(ServerType.EVENT, new SubProcessCommand(buildChannelServerCommand((String[])null), APP_BASE_SERVER_CLASSPATH + File.pathSeparator + APP_CORBA_CLASSPATH));

		final String[] profiles = APP_PROFILES.toString().split(",");
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
		return new String[] {"main"};    // Will need to change for multiple object servers
	}

	@Override
	public List<ObjectServerCommand> getObjectServerCommands() {
		return objectServerCommands;
	}

	@Override
	public SubProcessCommand getNameServerCommand() {
		return commands.get(ServerType.NAME);
	}

	@Override
	public SubProcessCommand getLogServerCommand() {
		return commands.get(ServerType.LOG);
	}

	@Override
	public SubProcessCommand getEventServerCommand() {
		return commands.get(ServerType.EVENT);
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

