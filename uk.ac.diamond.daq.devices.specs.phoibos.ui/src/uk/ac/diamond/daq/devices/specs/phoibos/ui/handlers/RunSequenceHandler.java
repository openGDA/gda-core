
package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.jython.ICommandRunner;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceValidation;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

public class RunSequenceHandler extends HandlerBase {

	private static final Logger logger = LoggerFactory.getLogger(RunSequenceHandler.class);
	private final ScanCommandBuilder scanCommandBuilder = new ScanCommandBuilder();
	private final ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();
	private final ITerminalPrinter terminalPrinter = InterfaceProvider.getTerminalPrinter();

	@Execute
	public void execute(MPart part, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) throws DeviceException {
		// Get the sequence open in the editor
		SpecsPhoibosSequence sequence = (SpecsPhoibosSequence) part.getTransientData().get(SpecsUiConstants.OPEN_SEQUENCE);

		SpecsPhoibosSequenceValidation sequenceValidationResult = validateSequence(shell, sequence, analyser);

		presentValidationResults(sequenceValidationResult, eventBroker, partService, shell);

		// Set and run sequence
		if (sequenceValidationResult.isValid()) {
			try {
				// Setup the analyser
				String path = part.getPersistedState().get(SpecsUiConstants.OPEN_SEQUENCE_FILE_PATH);
				analyser.setSequence(sequence, path);
				String scanCommand = scanCommandBuilder.buildScanCommand();
				// Print the command to the terminal for reference
				terminalPrinter.print(scanCommand);
				// Execute a scan
				commandRunner.runCommand(scanCommand);
			} catch (IllegalArgumentException e) {
				logger.error("Failed to set sequence: {}", sequence, e);
				// Couldn't set the sequence
				MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				dialog.setText("Setting up analyser failed");
				dialog.setMessage("Failed to setup analyser: " + e.getMessage() + "\n\n" + sequence);
				dialog.open();

				// Rethrow to prevent running the analyser with a old sequence
				throw e;
			}
			logger.debug("Analyser is successfully configured and sequence is running");
		}
	}

}