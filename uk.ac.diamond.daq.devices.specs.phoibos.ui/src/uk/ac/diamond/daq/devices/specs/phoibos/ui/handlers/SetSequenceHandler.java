
package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceValidation;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

public class SetSequenceHandler {
	private static final Logger logger = LoggerFactory.getLogger(SetSequenceHandler.class);

	ISpecsPhoibosAnalyser analyser;

	public SetSequenceHandler() {
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
	public void execute(MPart part, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) throws DeviceException {
		// Get the sequence open in the editor
		SpecsPhoibosSequence sequence = (SpecsPhoibosSequence) part.getTransientData().get(SpecsUiConstants.OPEN_SEQUENCE);
		logger.trace("About to configure analyser with sequence: {}", sequence);

		SpecsPhoibosSequenceValidation validationResult = null;
		try {
			validationResult = analyser.validateSequence(sequence);
		} catch (DeviceException exception) {
			logger.error("Device errors encountered during sequence validation", exception);
			MessageBox validationDialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			validationDialog.setText("Device errors encountered");
			validationDialog.setMessage("Device errors were encountered while trying to validate your sequence.");
			validationDialog.open();
			throw exception;
		}

		if (validationResult.isValid()) {
			try {
				// Setup the analyser
				String path = part.getPersistedState().get(SpecsUiConstants.OPEN_SEQUENCE_FILE_PATH);
				analyser.setSequence(sequence, path);
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
			logger.error("Sequence failed validation.");
			MessageBox validationDialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			validationDialog.setText("Invalid values in sequence");
			validationDialog.setMessage(validationResult.toString());
			validationDialog.open();
		}
	}


	@CanExecute
	public boolean canExecute(MPart part) {
		// Check if a sequence is loaded
		String path = part.getPersistedState().get(SpecsUiConstants.OPEN_SEQUENCE_FILE_PATH);
		return path != null;
	}

}