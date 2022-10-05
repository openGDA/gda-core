
package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.observable.IObserver;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceValidation;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

public class SetSequenceHandler extends HandlerBase implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(SetSequenceHandler.class);
	private static final String BUTTON_ID = "uk.ac.diamond.daq.devices.specs.phoibos.ui.handledtoolitem.setSequenceOnAnalyser";
	private boolean enableButton = true;

	@PostConstruct
	public void postConstruct() {
		status.addIObserver(this);
		enableButton = analyser.isNotBusy();
	}

	@Execute
	public void execute(MPart part, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) throws DeviceException {

		if (status.isBusy()) {
			MessageBox validationDialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			validationDialog.setText("Analyser is busy");
			validationDialog.setMessage("This sequence can't be set at the moment because the analyser is busy");
			validationDialog.open();
			return;
		}

		// Get the sequence open in the editor
		SpecsPhoibosSequence sequence = (SpecsPhoibosSequence) part.getTransientData().get(SpecsUiConstants.OPEN_SEQUENCE);

		SpecsPhoibosSequenceValidation sequenceValidationResult = validateSequence(shell, sequence, analyser);

		presentValidationResults(sequenceValidationResult, eventBroker, partService, shell);


		// Set sequence
		if (sequenceValidationResult.isValid()) {
			try {
				// Setup the analyser
				String path = part.getPersistedState().get(SpecsUiConstants.OPEN_SEQUENCE_FILE_PATH);
				analyser.setSequence(sequence, path);
			} catch (IllegalArgumentException e) {
				logger.error("Failed to set sequence: {}", sequence, e);
				MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				dialog.setText("Setting up analyser failed");
				dialog.setMessage("Failed to setup analyser: " + e.getMessage() + "\n\n" + sequence);
				dialog.open();
				// Rethrow to prevent running the analyser with a old sequence
				throw e;
			}
			logger.debug("Sucessfully configured analyser with sequence");
		}
	}

	@Override
	public void update(Object source, Object arg) {
		if (source == status && arg instanceof Boolean) {
			enableButton = !(boolean)arg;
			eventBroker.post(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, BUTTON_ID);
		}
	}

	@CanExecute
	public boolean canExecute(MPart part) {
		// Check if a sequence is loaded
		String path = part.getPersistedState().get(SpecsUiConstants.OPEN_SEQUENCE_FILE_PATH);
		return path != null && enableButton;
	}

}