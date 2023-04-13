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

import java.util.Arrays;
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

import gda.commandqueue.Queue;
import gda.commandqueue.QueuedCommandSummary;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.QueueEntry;
import uk.ac.gda.client.experimentdefinition.ui.handlers.ExperimentCommandProvider;

public class EditRepetitionsHandler extends AbstractHandler {
	private static final Logger logger = LoggerFactory.getLogger(EditRepetitionsHandler.class);
	private static final int numRepsIndex = 6; /** Index in the command string of the number of repetitions parameter */

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// get the active page for the event and get the selected item from the command queue table(!)
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection instanceof IStructuredSelection structuredSelection) {
			try {
				updateRepetitions(structuredSelection);
				return null;
			}catch(Exception e) {
				throw new ExecutionException("Problem editing number of repetitions", e);
			}
		}
		return null;
	}

	/**
	 * Create a new scan command identical to the selected one except with the user specified
	 * number of repetitions. The new scan command replaces the currently selected one.
	 *
	 * @param selection currently selected item in command queue
	 * @throws Exception
	 */
	private void updateRepetitions(IStructuredSelection selection) throws Exception {
		String scanCommand = getScanCommand(selection);

		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "", "Enter number of repetitions", getNumRepsFromScanCommand(scanCommand), validator);

		if (dlg.open() != Window.OK) {
			return;
		}

		try {
			String newNumReps = dlg.getValue();

			logger.info("Updating number of repetition from {} to {}", getNumRepsFromScanCommand(scanCommand), newNumReps);

			// Generate new command string with user specified number of repetitions
			String newCommand = getCommandString(scanCommand, newNumReps);

			// Add/update the repeats part of the description (i.e. the end of the string enclosed by [])
			String oldDescription = getCommandDescription(selection);
			String[] splitDescription = oldDescription.split("\\[");
			String newDescription = splitDescription[0].trim() + " ["+newNumReps+" repeats]";

			logger.debug("Old, new scan commands : {}, {}", scanCommand, newCommand);
			logger.debug("Old, new descriptions  : {}, {}", oldDescription, newDescription);

			// Add the new command to the queue
			var expCommand = new ExperimentCommandProvider(newCommand, newDescription);
			Queue queue = CommandQueueViewFactory.getQueue();
			var newCommandId = queue.addToTail(expCommand);

			// move new command in front of the old item, then delete the old item.
			var oldCommandId = getCommandSummary(selection).id;
			queue.moveToBefore(oldCommandId, List.of(newCommandId));
			queue.remove(oldCommandId);
		} catch (Exception e) {
			logger.warn("Problem changing number of repetitions for scan to {}", dlg.getValue(), e);
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
	private String getNumRepsFromScanCommand(String scanCommand) {
		String[] splitStr = scanCommand.split("\\s+");
		return splitStr[numRepsIndex];
	}

	/**
	 * Generate new scan command string with specified number of repetitions
	 * @param numReps
	 * @return
	 */
	private String getCommandString(String scanCommand, String numReps) {
		List<String> command = Arrays.asList(scanCommand.split("\\s+"));
		command.set(numRepsIndex, numReps);
		return command.stream().collect(Collectors.joining(" "));
	}

	/**
	 * Get the scan command for the currently selected item from the queue
	 * @param selection
	 * @return scan command
	 * @throws Exception
	 */
	private String getScanCommand(IStructuredSelection selection) throws Exception {
		Queue queue = CommandQueueViewFactory.getQueue();
		return queue.getCommandDetails(getCommandSummary(selection).id).getSimpleDetails();
	}

	/**
	 * Get the scan description for the currently selected item from the queue
	 * @param selection
	 * @return description
	 * @throws Exception
	 */
	private String getCommandDescription(IStructuredSelection selection) throws Exception {
		Queue queue = CommandQueueViewFactory.getQueue();
		return queue.getCommandSummary(getCommandSummary(selection).id).getDescription();
	}

	private QueuedCommandSummary getCommandSummary(IStructuredSelection selection) {
		QueueEntry ent = (QueueEntry) selection.getFirstElement();
		return ent.getQueueCommandSummary();
	}
}
