/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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
import org.eclipse.jface.action.IAction;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;

/**
 * Similar to DuplicateCommandHandler, but only works on ScanObjects. In this case it does not make new xml files, but
 * uses exactly the same xml files in a new scan.
 */
public class RepeatCommandHandler extends AbstractExperimentCommandHandler {

	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(RepeatCommandHandler.class);

	@Override
	public void run(IAction action) {
		try {
			doRepeat(action.getActionDefinitionId());
		} catch (Exception e) {
			logger.error("Editor action failed " + getClass().getName(), e);
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return doRepeat(event.getCommand().getId());
	}

	private Object doRepeat(final String id) {

		final ACTION_TYPE type = getActionType(id);
		if (type == ACTION_TYPE.RUN) {
			final IExperimentObjectManager man = getEditorManager().getSelectedMultiScan();
			if (man == null)
				return false;

			final IExperimentObject ob = getEditorManager().getSelectedScan();
			if (ob == null)
				return false;

			try {
				final IExperimentObject copy = man.cloneExperiment(ob);
				man.insertExperimentAfter(ob, copy);
				if (getEditorManager().getActiveRunEditor() != null) {
					getEditorManager().getActiveRunEditor().editRunName(copy);
					getEditorManager().refreshViewers();
				} else if (getEditorManager().getViewer() != null) {
					getEditorManager().setSelected(copy);
					getEditorManager().openDefaultEditors(copy, true);
					getEditorManager().refreshViewers();
					getEditorManager().getViewer().editElement(copy);
				}
			} catch (Exception ne) {
				logger.error("Cannot create copy", ne);
			}
		}

		return true;
	}

}
