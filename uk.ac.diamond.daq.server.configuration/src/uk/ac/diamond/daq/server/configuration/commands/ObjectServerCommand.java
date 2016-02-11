package uk.ac.diamond.daq.server.configuration.commands;

import gda.util.ObjectServer;


/**
 * Command object class used to start new object servers in their own thread. Should be created with a 
 * distinct profile and spring file path combination
 * @author fri44821
 *
 */
public class ObjectServerCommand {
	
	private final String profile;
	private final String springFilePath;
	private final String[] args;
	
	
/** 
 * @param profile				The profile that this command object corresponds to
 * @param springFilePath		The path to the Spring config file that identifies the beans to be made available
 */
	public ObjectServerCommand(final String profile, final String springFilePath) {
		this.profile = profile;
		this.springFilePath = springFilePath;
		args = new String[] {"-p", profile, "-f", springFilePath};
	}

	public String getProfile() {
		return profile;
	}
	
	public String getSpringFilePath() {
		return springFilePath;
	}

	// Start a new SpringObjectServer in its own thread using the server.xml that corresponds to the profile
	/**
	 * Start a new SpringObjectServer in its own thread using the server.xml that corresponds to the profile
	 * 
	 * @return	null or the created instance
	 */
	public ObjectServer execute() {
		return ObjectServer.spawn(args);
	}
	
}