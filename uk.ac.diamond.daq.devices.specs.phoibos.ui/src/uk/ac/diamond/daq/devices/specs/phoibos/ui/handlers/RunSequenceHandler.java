
package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import gda.jython.ICommandRunner;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

public class RunSequenceHandler extends SetSequenceHandler {

	// TODO Would be nice to replace this with a static scan so we don't need the dummy scannable
	private static final String SCAN = "scan dummy_a 0 0 1 analyser";

	private final ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();
	private final ITerminalPrinter terminalPrinter = InterfaceProvider.getTerminalPrinter();

	@Override
	@Execute
	public void execute(MPart part) {
		// Set the sequence on the analyser
		super.execute(part);

		// Print the command to the terminal for reference
		terminalPrinter.print(SCAN);

		// Execute a scan
		commandRunner.runCommand(SCAN);
	}

}