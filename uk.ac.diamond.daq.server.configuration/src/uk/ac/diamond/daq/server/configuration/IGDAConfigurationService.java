package uk.ac.diamond.daq.server.configuration;

import java.util.List;

import uk.ac.diamond.daq.server.configuration.commands.ObjectServerCommand;

public interface IGDAConfigurationService {

	void loadConfiguration();

	String getMode();

	String[] getProfiles();

	List<ObjectServerCommand> getObjectServerCommands();

	void setInstanceConfigRoot(final String path);

	String getInstanceConfigRoot();

}
