package org.opengda.detector.electronanalyser.client.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;

public class StartCollectionAction extends AbstractHandler implements IHandler {
	private static final Logger logger = LoggerFactory.getLogger(StartCollectionAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the command
		String command = CommandToClipboardAction.buildCommand(event);
		if (command == null) {
			logger.error("Building command failed");
			return null;
		}

		logger.info("Running command: {}", command);

		// Print the command to the console to make the history consistent
		InterfaceProvider.getTerminalPrinter().print(command);

		// Run the command in Jython
		InterfaceProvider.getCommandRunner().runCommand(command);

		return null;

	}

	@Override
	public boolean isEnabled() {
		// Always enabled
		return true;
	}
}
