
package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import gda.jython.ICommandRunner;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

public class RunSequenceHandler extends SetSequenceHandler {

	private final ScanCommandBuilder scanCommandBuilder = new ScanCommandBuilder();
	private final ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();
	private final ITerminalPrinter terminalPrinter = InterfaceProvider.getTerminalPrinter();

	@Override
	@Execute
	public void execute(MPart part, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		// Set the sequence on the analyser
		super.execute(part, shell);

		String scanCommand = scanCommandBuilder.buildScanCommand();

		// Print the command to the terminal for reference
		terminalPrinter.print(scanCommand);

		// Execute a scan
		commandRunner.runCommand(scanCommand);
	}

}