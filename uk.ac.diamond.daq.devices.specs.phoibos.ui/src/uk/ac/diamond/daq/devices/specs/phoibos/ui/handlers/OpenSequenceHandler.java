/*-
 * Copyright © 2016 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceHelper;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

/**
 * Provides the ability to load a sequence from file. It allows the user to select the file and then sends an event to
 * let the sequence editor change the displayed sequence.
 *
 * @author James Mudd
 */
public class OpenSequenceHandler {

	@Inject
	IEventBroker eventBroker;

	@Execute
	public void execute(MPart part, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		// Open the dialog for the user to select a sequence file
		String path = getOpenPath(shell);

		if(path == null) { //User pressed cancel
			return;
		}

		SpecsPhoibosSequence sequence = SpecsPhoibosSequenceHelper.loadSequence(path);

		if (sequence == null) {
			// Won't be able to open sequence file to tell user and abort
			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			dialog.setText("Open failed");
			dialog.setMessage("Failed to open sequence file: " + path);
			dialog.open();
			// Just return here can't continue.
			return;
		}

		// Set the open sequence path
		part.getPersistedState().put(SpecsUiConstants.OPEN_SEQUENCE_FILE_PATH, path);

		// Use blocking event, as need to ensure its done before giving the user thread back
		eventBroker.send(SpecsUiConstants.OPEN_SEQUENCE_EVENT, sequence);

	}

	/**
	 * This displays the file open box for the user to select a sequence file.
	 *
	 * @param shell
	 *            The shell to use to display the dialog
	 * @return The path to the file the user selected
	 */
	private String getOpenPath(Shell shell) {
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setFilterNames(new String[] { "Sequence Files (*.seq)", "All Files (*.*)" });
		dialog.setFilterExtensions(new String[] { "*.seq", "*.*" });
		return dialog.open();
	}
}