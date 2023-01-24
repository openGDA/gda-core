/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.CommandDetails;
import gda.commandqueue.CommandDetailsPath;
import gda.commandqueue.Queue;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.QueueEntry;

public class EditRepetitionsHandler extends AbstractHandler {
	private static final Logger logger = LoggerFactory.getLogger(EditRepetitionsHandler.class);
	private static final int numRepsIndex = 6; /** Index in the command string of the number of repetitions parameter */
	private String fileName = "";
	private String scanComand = "";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// get the active page for the event and get the selected item from the command queue table(!)
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection == null || !(selection instanceof IStructuredSelection)) {
			return null;
		}

		try {
			updateRepetitions((IStructuredSelection)selection);
			return null;
		}catch(Exception e) {
			throw new ExecutionException("Problem editing number of repetitions", e);
		}
	}

	/**
	 * Create a new scan .py file containing scan command with user specified number of repetitions.
	 * Command queue is also refreshed so that the updated value appears in the GUI.
	 * @param selection currently selected item in command queue
	 * @throws Exception
	 */
	private void updateRepetitions(IStructuredSelection selection) throws Exception {
		setScanDetails(selection);

		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "", "Enter number of repetitions", getNumRepsFromScanCommand(), validator);
		if (dlg.open() == Window.OK) {
			try {
				String newNumReps = dlg.getValue();
				// Generate new command string with user specified number of repetitions
				String newCommand = getCommandString(newNumReps);
				// Replace the current scan .py file with a new with with new scan scan command
				try(FileWriter writer = new FileWriter(new File(fileName))) {
					writer.write(newCommand);
				}
				Queue queue = CommandQueueViewFactory.getQueue();
				queue.remove(Collections.emptyList()); // don't remove anything from queue just call to trigger the notify observers method of the queue so command queue GUI updates

			} catch (Exception e) {
				logger.warn("Problem changing number of repetitions for scan to {}", dlg.getValue(), e);
			}
		}
	}

	/**
	 * Validator to make sure dialog box only accepts integers > 0
	 */
	IInputValidator validator = newText -> {
		try {
		    int intValue = Integer.parseInt(newText);

		    // Determine if input is too short or too long
		    if (intValue < 1) return "Too few repetiions";

		    // Input must be OK
		    return null;
		} catch(NumberFormatException nfe) {
			return "Invalid integer";
		}
	};

	/**
	 * Return number of repetitions by parsing the scan command string
	 * @return
	 */
	private String getNumRepsFromScanCommand() {
		String[] splitStr = scanComand.split("\\s+");
		return splitStr[numRepsIndex];
	}

	/**
	 * Generate new scan command string with specified number of repetitions
	 * @param numReps
	 * @return
	 */
	private String getCommandString(String numReps) {
		List<String> command = Arrays.asList(scanComand.split("\\s+"));
		command.set(numRepsIndex, numReps);
		return command.stream().collect(Collectors.joining(" "));
	}

	/**
	 * Store the filename of command queue .py script file used to run the scan and it's contents (i.e. the scan command)
	 * @param selection
	 * @throws Exception
	 */
	private void setScanDetails(IStructuredSelection selection) throws Exception {
		QueueEntry ent = (QueueEntry) selection.getFirstElement();
		Queue queue = CommandQueueViewFactory.getQueue();
		CommandDetails commandDetails = queue.getCommandDetails(ent.getQueueCommandSummary().id);
		if (commandDetails instanceof CommandDetailsPath) {
			fileName = ((CommandDetailsPath) commandDetails).getPath();
			// set the 'run name' from first word in the line
			try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
				scanComand = br.readLine();
			}
		}
	}

}
