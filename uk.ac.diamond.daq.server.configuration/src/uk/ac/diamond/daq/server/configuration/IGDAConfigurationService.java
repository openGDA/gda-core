package uk.ac.diamond.daq.server.configuration;

import java.util.List;

import uk.ac.diamond.daq.server.configuration.commands.ServerCommand;

public interface IGDAConfigurationService {

	void loadConfiguration();

	String getMode();

	String[] getProfiles();

	List<? extends ServerCommand> getObjectServerCommands();

	void setInstanceConfigRoot(final String path);

	String getInstanceConfigRoot();

}
