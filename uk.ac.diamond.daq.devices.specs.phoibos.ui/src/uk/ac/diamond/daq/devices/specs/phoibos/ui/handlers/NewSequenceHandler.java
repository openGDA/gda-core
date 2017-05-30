
package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

public class NewSequenceHandler {

	@Inject
	IEventBroker eventBroker;

	@Execute
	public void execute(MPart part, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		if (part.getTransientData().get(SpecsUiConstants.OPEN_SEQUENCE) != null) {
			// create a dialog with ok and cancel buttons and a question icon
			MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
			dialog.setText("New sequence?");
			dialog.setMessage("You currently have a sequnece open, creating a new sequence will replace it.\n\nAre you sure?");

			// open dialog and await user selection
			int returnCode = dialog.open();
			// If user Cancelled just return here
			if (returnCode == SWT.CANCEL) {
				return;
			}
		}


		// Create a new sequence
		SpecsPhoibosSequence sequence = new SpecsPhoibosSequence();

		// Add one default region
		sequence.addRegion(new SpecsPhoibosRegion());

		// Use blocking event, as need to ensure its done before giving the user thread back
		eventBroker.send(SpecsUiConstants.OPEN_SEQUENCE_EVENT, sequence);

		// Set the open sequence path to null indicating its unsaved
		part.getPersistedState().put(SpecsUiConstants.OPEN_SEQUENCE_FILE_PATH, null);

	}

}