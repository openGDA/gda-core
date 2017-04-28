package uk.ac.diamond.daq.server.configuration;

import java.util.List;

import uk.ac.diamond.daq.server.configuration.commands.ObjectServerCommand;
import uk.ac.diamond.daq.server.configuration.commands.SubProcessCommand;

public interface IGDAConfigurationService {

	void loadConfiguration();

	String getMode();

	String[] getProfiles();

	SubProcessCommand getNameServerCommand();

	SubProcessCommand getLogServerCommand();

	SubProcessCommand getEventServerCommand();

	List<ObjectServerCommand> getObjectServerCommands();

	void setInstanceConfigRoot(final String path);

	String getInstanceConfigRoot();

	public enum ServerType {
		NAME,
		LOG,
		EVENT;
	}
}
