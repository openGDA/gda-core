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

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.action.IAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.client.experimentdefinition.components.ExperimentProjectNature;
import uk.ac.gda.util.io.FileUtils;

/**
 * @author Matthew Gerring
 */
public class AddCommandHandler extends AbstractExperimentCommandHandler {

	private final static Logger logger = LoggerFactory.getLogger(AddCommandHandler.class);

	@Override
	public void run(IAction action) {
		try {
			doAdd(action.getActionDefinitionId());
		} catch (Exception e) {
			logger.error("Editor action failed " + getClass().getName(), e);
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return doAdd(event.getCommand().getId());
	}

	private Object doAdd(final String id) {

		try {
			final ACTION_TYPE type = getActionType(id);
			if (type == ACTION_TYPE.FOLDER) {
				final File root = getEditorManager().getProjectFolder();
				File dir = FileUtils.getUnique(root, "Experiment_", null, 1);
				dir.mkdir();
				try {
					ExperimentProjectNature.createNewScan(getEditorManager().getIFolder(dir.getName()));
				} catch (IOException e) {
					logger.error("Error creating new scan", e);
				}
				getEditorManager().refreshViewers();
				getEditorManager().select(getEditorManager().getIFolder(dir.getName()));
				getEditorManager().editSelectedElement();

			} else if (type == ACTION_TYPE.SCAN) {
				final IFolder dir = getEditorManager().getSelectedFolder();
				if (dir == null)
					return false;
				try {
					final IExperimentObjectManager man = ExperimentProjectNature.createNewScan(dir);
					getEditorManager().refreshViewers();
					getEditorManager().select(man);
				} catch (Exception e) {
					logger.error("Error creating new scan", e);
				}

			} else if (type == ACTION_TYPE.RUN) {
				final IExperimentObjectManager man = getEditorManager().getSelectedMultiScan();
				if (man == null)
					return false;

				final IExperimentObject ob = getEditorManager().getSelectedScan();

				final IExperimentObject created = man.insertNewExperimentAfter(ob);
				if (getEditorManager().getActiveRunEditor() != null) {
					getEditorManager().getActiveRunEditor().editRunName(created);
				} else if (getEditorManager().getViewer() != null) {
					getEditorManager().setSelected(created);
					getEditorManager().openDefaultEditors(created, true);
					getEditorManager().refreshViewers();
					getEditorManager().getViewer().editElement(created);
				}
			}
		} catch (Exception ne) {
			getEditorManager().refreshViewers();
			logger.error("Cannot complete add action.", ne);
		}
		return true;
	}

}
