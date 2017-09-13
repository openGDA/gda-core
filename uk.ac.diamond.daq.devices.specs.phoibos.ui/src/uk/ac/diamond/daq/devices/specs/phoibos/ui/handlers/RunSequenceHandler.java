
package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.ICommandRunner;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

public class RunSequenceHandler extends SetSequenceHandler {
	private static final Logger logger = LoggerFactory.getLogger(RunSequenceHandler.class);

	// TODO Would be nice to replace this with a static scan so we don't need the dummy scannable
	private static final String SCAN_BASE_COMMAND = "scan dummy_a 0 0 1 analyser";

	private final ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();
	private final ITerminalPrinter terminalPrinter = InterfaceProvider.getTerminalPrinter();

	@Override
	@Execute
	public void execute(MPart part) {
		// Set the sequence on the analyser
		super.execute(part);

		// Find out if any extraDetectors are configured, should exist and return a empty string if none are required
		String extraDetectors = commandRunner.evaluateCommand("extraDetectors");
		if (extraDetectors == null) {
			logger.warn("extraDetectors was not in the Jython namespace, no extraDetectors will be used");
			extraDetectors = "";
		}
		logger.debug("Extra detectors configured: {}", extraDetectors);

		// Add the extra detectors to the scan command
		String scanCommand = SCAN_BASE_COMMAND + " " + extraDetectors;
		// Clean trailing white space if extra detectors is empty
		scanCommand = scanCommand.trim();

		// Print the command to the terminal for reference
		terminalPrinter.print(scanCommand);

		// Execute a scan
		commandRunner.runCommand(scanCommand);
	}

}