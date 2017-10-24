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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;

public class DeleteCommandHandler extends AbstractExperimentCommandHandler {

	private final static Logger logger = LoggerFactory.getLogger(DeleteCommandHandler.class);

	@Override
	public void run(IAction action) {
		try {
			doDelete(action.getActionDefinitionId());
		} catch (Exception e) {
			logger.error("Editor action failed " + getClass().getName(), e);
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return doDelete(event.getCommand().getId());
	}

	private Object doDelete(final String id) {

		final ACTION_TYPE type = getActionType(id);
		if (type == ACTION_TYPE.FOLDER) {
			final IFolder dir = getEditorManager().getSelectedFolder();
			if (dir == null)
				return false;
			final boolean ok = MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell(), "Please confirm delete", "Delete '" + dir.getName() + "' and contents?");
			if (ok) {
				try {
					final List<IExperimentObjectManager> mans = ExperimentFactory.getRunManagers(dir);
					dir.delete(false, null);
					if (mans != null) {
						for (IExperimentObjectManager runObjectManager : mans)
							runObjectManager.fireExperimentObjectListeners();
					}

				} catch (Exception e) {
					logger.error("Error deleting {}", dir, e);
				} finally {
					ExperimentFactory.getExperimentEditorManager().closeAllEditors(true);
					getEditorManager().refreshViewers();
				}
			}

		} else if (type == ACTION_TYPE.SCAN) {
			final IFile file = getEditorManager().getSelectedFile();
			if (file == null)
				return false;
			DeleteCommandHandler.deleteScan(file);

		} else if (type == ACTION_TYPE.RUN) {
			final IFile file = getEditorManager().getSelectedFile();
			if (file == null)
				return false;

			final IExperimentObject ob = getEditorManager().getSelectedScan();
			if (ob == null)
				return false;

			try {
				IExperimentObjectManager man = ExperimentFactory.getManager(ob);
				Object selectedInView = getEditorManager().getSelected();
				if (ob.equals(selectedInView)){
					getEditorManager().closeAllEditors(false);
				}
				man.removeExperiment(ob);
				man.fireExperimentObjectListeners();
				getEditorManager().select(man);
			} catch (Exception e) {
				logger.error("Cannot delete " + ob, e);
			}

			getEditorManager().refreshViewers();
		}
		return true;
	}

	/**
	 * @param file
	 */
	public static void deleteScan(IFile file) {
		final boolean ok = MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"Please confirm delete", "Delete '" + file.getName()
						+ "' and parameter files used by this scan and not referenced elsewhere?");
		if (ok) {
			try {
				ExperimentFactory.deleteManager(file);
				ExperimentFactory.getExperimentEditorManager().closeAllEditors(true);
				ExperimentFactory.getExperimentEditorManager().refreshViewers();
				ExperimentFactory.getExperimentEditorManager().select(file.getParent());
			} catch (Exception e) {
				logger.error("Error deleting scan", e);
			}
		}
	}

}
