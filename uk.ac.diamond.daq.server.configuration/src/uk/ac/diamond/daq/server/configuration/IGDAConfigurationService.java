package uk.ac.diamond.daq.server.configuration;

import java.util.List;

import uk.ac.diamond.daq.server.configuration.commands.ServerCommand;

public interface IGDAConfigurationService {

	/** This property should be set by DS components that implement this interface */
	String CONFIGURATION_LAYOUT_PROPERTY = "configuration.layout";

	void loadConfiguration();

	String getMode();

	String[] getProfiles();

	List<? extends ServerCommand> getObjectServerCommands();

	void setInstanceConfigRoot(final String path);

	String getInstanceConfigRoot();

}
