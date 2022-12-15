/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition.ui.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.validation.AbstractValidator;
import uk.ac.gda.beans.validation.InvalidBeanException;
import uk.ac.gda.beans.validation.WarningType;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;

public class RunExperimentCommandHandler extends AbstractExperimentCommandHandler {
	private static final Logger logger = LoggerFactory.getLogger(RunExperimentCommandHandler.class);
	private boolean motorStageWarning=true;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (event.getCommand().getId().equals("uk.ac.gda.client.experimentdefinition.RunSeveralMultiExperimentCommand")) {

			queueSeveralMultiScans();

		} else if (event.getCommand().getId().equals("uk.ac.gda.client.experimentdefinition.RunMultiExperimentCommand")) {

			queueMultiScan();

		} else if (event.getCommand().getId()
				.equals("uk.ac.gda.client.experimentdefinition.RunSingleExperimentCommand")) {

			queueSingleScan();

		} else if (event.getCommand().getId().equals("uk.ac.gda.client.experimentdefinition.RunSingleScanOnlyCommand")) {

			queueSingleScanSingleRepetition();
		}
		setMotorStageWarning(true);
		return null;
	}

	public void setMotorStageWarning(boolean newWarningValue) {
		motorStageWarning = newWarningValue;
	}

	protected void queueSingleScanSingleRepetition() throws ExecutionException {
		final IExperimentObject ob = getEditorManager().getSelectedScan();
		if (ob == null)
			return;

		final IExperimentObject single = ExperimentFactory.getManager(ob).cloneExperiment(ob);
		single.setNumberRepetitions(1);
		addExperimentToQueue(single, new String[]{"Run Scan", "Cancel"},"");
	}

	protected void queueSingleScan() throws ExecutionException {
		final IExperimentObject ob = getEditorManager().getSelectedScan();
		addExperimentToQueue(ob, new String[]{"Run Scan", "Cancel"},"");
	}

	protected void queueMultiScan() throws ExecutionException {
		final IExperimentObjectManager man = getEditorManager().getSelectedMultiScan();
		if (man == null)
			return;

		List<IExperimentObject> exptList = man.getExperimentList();
		for (IExperimentObject expt : exptList) {
			addExperimentToQueue(expt, new String[]{"Continue", "Cancel this Scan"}, "\nIf you choose to continue you will not be warned about motor movements for other scans in the selected multi-scan.");
		}
	}

	protected void queueSeveralMultiScans() throws ExecutionException {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "Queue several multi-scans", "Enter number of multi-scans to add", Integer.toString(1), validator);
		dlg.setBlockOnOpen(true);
		if (dlg.open() == Window.OK) {
			int n = Integer.parseInt(dlg.getValue());
			logger.info("Adding {} repetitions of multi-scan to queue", n);
			for(int i=0; i<n; i++) {
				queueMultiScan();
			}
		}
	}

	/**
	 * Validator to make sure dialog box only accepts integers > 0
	 */
	private IInputValidator validator = newText -> {
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

	private void addExperimentToQueue(final IExperimentObject ob, String[] buttons, String extraMessage) throws ExecutionException {

		if (!saveAllOpenEditors()) {
			return;
		}

		AbstractValidator validator = ExperimentFactory.getValidator();
		if (validator != null) {
			try {
				validator.validate(ob);
			} catch (InvalidBeanException e) {
				MessageDialog md = switch (e.getSeverity()) {
					case LOW,MEDIUM -> showLowWarning(e, buttons, extraMessage, ob.getRunName());
					case HIGH -> showHighWarning(e, ob.getRunName());
				};

				// If warning.stageAxes is true, or the warning level is High, the display the MessageDialog
				if(motorStageWarning || e.getSeverity() == WarningType.HIGH) {
					int choice = md.open();
					if (choice==Window.CANCEL) {
						return;
					}
				}
				motorStageWarning=false;
			}
		}

		ExperimentCommandProvider commandProvider;
		try {
			commandProvider = new ExperimentCommandProvider(ob);
		} catch (Exception e) {
			logger.error("Exception creating ExperimentCommandProvider", e);
			throw new ExecutionException("Exception creating ExperimentCommandProvider.", e);
		}

		submitCommandToQueue(commandProvider);
	}

	private MessageDialog showLowWarning(InvalidBeanException e, String[] buttons, String extraMessage, String runName) {
		return new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"INFO For Scan: "+runName,null,
				e.getMessage()+extraMessage,MessageDialog.INFORMATION, buttons,1);
	}
	private MessageDialog showHighWarning(InvalidBeanException e, String runName) {
		return new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error(s) in XML file(s) for Scan: "+runName,null,
				e.getMessage(),MessageDialog.ERROR,new String[]{"Ignore errors","Cancel Scan"},1);
	}

	protected void submitCommandToQueue(ExperimentCommandProvider commandProvider) throws ExecutionException {
		try {
			CommandQueueViewFactory.getQueue().addToTail(commandProvider);
		} catch (Exception e) {
			logger.error("Exception adding ExperimentCommandProvider to CommandQueue", e);
			throw new ExecutionException("Exception adding ExperimentCommandProvider to CommandQueue.", e);
		}
	}

	private boolean saveAllOpenEditors() {
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getDirtyEditors().length == 0) {
			return true;
		}
		if (MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"Unsaved editors", "All editors need to be saved before adding to the Command Queue.\nIs this OK?")) {
			IEditorPart[] dirties = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getDirtyEditors();
			for (IEditorPart part : dirties) {
				part.doSave(new NullProgressMonitor());
			}
			return true;
		}
		return false;
	}
}
