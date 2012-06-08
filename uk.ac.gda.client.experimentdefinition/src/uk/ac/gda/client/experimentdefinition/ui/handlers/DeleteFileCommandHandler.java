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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.client.experimentdefinition.components.ExperimentFolderEditor;

public class DeleteFileCommandHandler extends AbstractExperimentCommandHandler {

	private final static Logger logger = LoggerFactory.getLogger(DeleteFileCommandHandler.class);

	@Override
	public void run(IAction action) {
		doDelete();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return doDelete();
	}

	private Object doDelete() {

		final ExperimentFolderEditor editor = getController().getActiveFolderEditor();

		final List<IFile> sel = editor.getSelected();
		if (sel == null)
			return false;
		try {
			for (IFile file : sel) {
				final IExperimentObjectManager man = ExperimentFactory.getManager(file);
				if (man != null) {
					DeleteCommandHandler.deleteScan(file);
					return true;
				}
				final List<IExperimentObjectManager> mans = ExperimentFactory.getReferencedManagers(file.getParent(),
						file.getName());
				if (mans == null || mans.isEmpty()) {
					file.delete(true, null);
					editor.refresh();
				} else {
					final boolean deleteAnyway = MessageDialog.openConfirm(editor.getSite().getShell(),
							"File Referenced", getScanMessage(mans, file));
					if (deleteAnyway) {
						file.delete(true, null);
//						ExperimentFactory.checkFolder(file.getParent());
						editor.refresh();
					}
				}
			}
		} catch (Exception e) {
			logger.error("Cannot delete", e);// Not expected.
		} finally {
			getController().refreshViewers();
		}

		return true;
	}

	private String getScanMessage(List<IExperimentObjectManager> mans, final IFile sel) {
		final StringBuilder buf = new StringBuilder("The file '");
		buf.append(sel.getName());
		buf.append("' is referenced in the following scan files:\n\n");
		for (IExperimentObjectManager runObjectManager : mans) {
			buf.append(runObjectManager.getName());
			buf.append(".scan\n");
		}
		buf.append("\nWould you like to delete anyway?");
		return buf.toString();
	}
}
