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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.beans.validation.AbstractValidator;
import uk.ac.gda.beans.validation.InvalidBeanException;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;

public class RunExperimentCommandHandler extends AbstractExperimentCommandHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (event.getCommand().getId().equals("uk.ac.gda.client.experimentdefinition.RunMultiExperimentCommand")) {

			queueMultiScan();

		} else if (event.getCommand().getId()
				.equals("uk.ac.gda.client.experimentdefinition.RunSingleExperimentCommand")) {

			queueSingleScan();

		} else if (event.getCommand().getId().equals("uk.ac.gda.client.experimentdefinition.RunSingleScanOnlyCommand")) {

			queueSingleScanSingleRepetition();
		}
		return null;
	}

	protected void queueSingleScanSingleRepetition() throws ExecutionException {
		final IExperimentObject ob = getController().getSelectedScan();
		if (ob == null)
			return;

		final IExperimentObject single = ExperimentFactory.getManager(ob).cloneExperiment(ob);
		single.setNumberRepetitions(1);
		addExperimentToQueue(single);
	}

	protected void queueSingleScan() throws ExecutionException {
		final IExperimentObject ob = getController().getSelectedScan();
		addExperimentToQueue(ob);
	}

	protected void queueMultiScan() throws ExecutionException {
		final IExperimentObjectManager man = getController().getSelectedMultiScan();
		if (man == null)
			return;

		List<IExperimentObject> exptList = man.getExperimentList();
		for (IExperimentObject expt : exptList) {
			addExperimentToQueue(expt);
		}
	}

	private void addExperimentToQueue(final IExperimentObject ob) throws ExecutionException {

		if (!saveAllOpenEditors()) {
			return;
		}

		AbstractValidator validator = ExperimentFactory.getValidator();
		if (validator != null) {
			try {
				validator.validate(ob);
			} catch (InvalidBeanException e) {
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Experiment XML invalid", e.getMessage());
				return;
			}
		}

		ExperimentCommandProvider command;
		try {
			command = new ExperimentCommandProvider(ob);
		} catch (Exception e) {
			throw new ExecutionException("Exception creating ExperimentCommandProvider.", e);
		}

		try {
			CommandQueueViewFactory.getQueue().addToTail(command);
		} catch (Exception e) {
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
