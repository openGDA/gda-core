package uk.ac.diamond.daq.server.configuration.commands;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.IOException;

import uk.ac.diamond.daq.server.configuration.ConfigurationDefaults;


/**
 * Command object class used execute arbitrary commands in a separate sub process.
 * Used to start subordinate servers i.e. log, name, event.
 * @author fri44821
 *
 */
public class SubProcessCommand {
	private static final String CLASSPATH= "CLASSPATH";

	private final String[] command;
	private final String classpath;

	public SubProcessCommand(final String[] command, final ConfigurationDefaults classpath) {
		this(command, classpath.toString());
	}

	public SubProcessCommand(final String[] command, final String classpath) {
		this.command = command;
		this.classpath = classpath;
	}

	/**
	 * Create the subprocess and execute the command supplying the appropriate classpath.
	 *
	 * @return					the newly created sub process
	 * @throws ioException		if the created process fails to start for any reason
	 */
	public Process execute() throws IOException {
		final ProcessBuilder pBuilder = new ProcessBuilder(command).inheritIO();
		pBuilder.environment().put(CLASSPATH, classpath);
		return pBuilder.start();
	}

	@Override
	public String toString() {
		return stream(command).collect(joining(" ", "SubProcessCommand(", ")"));
	}
}
