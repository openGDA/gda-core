
package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import gda.jython.ICommandRunner;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceValidation;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

public class RunSequenceHandler  {

	ISpecsPhoibosAnalyser analyser;
	private static final Logger logger = LoggerFactory.getLogger(RunSequenceHandler.class);
	private final ScanCommandBuilder scanCommandBuilder = new ScanCommandBuilder();
	private final ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();
	private final ITerminalPrinter terminalPrinter = InterfaceProvider.getTerminalPrinter();

	public RunSequenceHandler() {
		logger.trace("Constructor called");

		// Get an analyser
		List<ISpecsPhoibosAnalyser> analysers = Finder.getInstance()
				.listLocalFindablesOfType(ISpecsPhoibosAnalyser.class);
		if (analysers.size() != 1) {
			String msg = "No Analyser was found! (Or more than 1)";
			logger.error(msg);
			throw new IllegalStateException(msg);
		}
		analyser = analysers.get(0);
		logger.debug("Connected to analyser: {}", analyser);
	}

	@Execute
	public void execute(MPart part, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		// Get the sequence open in the editor
		SpecsPhoibosSequence sequence = (SpecsPhoibosSequence) part.getTransientData().get(SpecsUiConstants.OPEN_SEQUENCE);
		logger.trace("About to configure analyser with sequence: {}", sequence);
		SpecsPhoibosSequenceValidation validationResult = analyser.validateSequence(sequence);
		if (validationResult.isValid()) {
			try {
				// Setup the analyser
				analyser.setSequence(sequence);

				String scanCommand = scanCommandBuilder.buildScanCommand();

				// Print the command to the terminal for reference
				terminalPrinter.print(scanCommand);

				// Execute a scan
				commandRunner.runCommand(scanCommand);
			} catch (IllegalArgumentException e) {
				logger.error("Failed to set sequence: {}", sequence, e);
				// Couldn't set the sequence
				MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				dialog.setText("Setting up anlyser failed");
				dialog.setMessage("Failed to setup analyser: " + e.getMessage() + "\n\n" + sequence);
				dialog.open();

				// Rethrow to prevent running the analyser with a old sequence
				throw e;
			}
			logger.debug("Sucessfully configured analyser with sequence");
		}else {
			logger.error("Failed to validate energy values");
			MessageBox validationDialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			validationDialog.setText("Failed to validate energy values!");
			validationDialog.setMessage(validationResult.toString());
			validationDialog.open();
		}

	}


}