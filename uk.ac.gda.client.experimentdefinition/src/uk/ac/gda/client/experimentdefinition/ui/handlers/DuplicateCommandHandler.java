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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.action.IAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.common.rcp.util.EclipseUtils;

/**
 * Makes a copy of the current folder/scan/run object at the same level
 * 
 */
public class DuplicateCommandHandler extends AbstractExperimentCommandHandler {

	private final static Logger logger = LoggerFactory.getLogger(DuplicateCommandHandler.class);

	@Override
	public void run(IAction action) {
		try {
			doCopy(action.getActionDefinitionId());
		} catch (Exception e) {
			logger.error("Editor action failed " + getClass().getName(), e);
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return doCopy(event.getCommand().getId());
	}

	private Object doCopy(final String id) {

		final ACTION_TYPE type = getActionType(id);
		if (type == ACTION_TYPE.FOLDER) {

			final IFolder sel = getController().getSelectedFolder();
			if (sel == null)
				return false;

			final String name = EclipseUtils.getUnique(sel);
			final IFolder dir = getController().getIFolder(name);
			try {
				sel.copy(dir.getFullPath(), true, null);
				ExperimentFactory.getExperimentEditorManager().closeAllEditors(true);
				getController().refreshViewers();
				getController().select(dir);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

		} else if (type == ACTION_TYPE.SCAN) {
			final IExperimentObjectManager man = getController().getSelectedMultiScan();
			if (man == null)
				return false;
			try {
				final IFile file = EclipseUtils.getUniqueFile(man.getFile(), "scan");
				man.getFile().copy(file.getFullPath(), true, null);
				getController().refreshViewers();
				getController().select(ExperimentFactory.getManager(file));

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

		} else if (type == ACTION_TYPE.RUN) {
			final IExperimentObjectManager man = getController().getSelectedMultiScan();
			if (man == null)
				return false;

			final IExperimentObject ob = getController().getSelectedScan();
			if (ob == null)
				return false;

			try {
				final IExperimentObject copy = man.createCopyOfExperiment(ob);
				man.insertExperimentAfter(ob, copy);
				if (getController().getActiveRunEditor() != null) {
					getController().getActiveRunEditor().editRunName(copy);
				} else if (getController().getViewer() != null) {
					getController().refreshViewers();
					getController().getViewer().editElement(copy);
				}
				getController().refreshViewers();
			} catch (Exception ne) {
				logger.error("Cannot create copy", ne);
			}
		}
		return true;
	}

}
