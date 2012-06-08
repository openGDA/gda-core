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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.client.experimentdefinition.components.ExperimentFolderEditor;
import uk.ac.gda.client.experimentdefinition.components.ExperimentRunEditor;
import uk.ac.gda.common.rcp.util.EclipseUtils;

public abstract class AbstractExperimentCommandHandler extends AbstractHandler implements IEditorActionDelegate {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractExperimentCommandHandler.class);

	/**
	 * The types of objects which can be operated on by the experiment handlers
	 */
	public enum ACTION_TYPE {
		FOLDER, SCAN, RUN
	}

	protected String actionHints;
	private IExperimentEditorManager controller = null;

	/**
	 * 
	 */
	public AbstractExperimentCommandHandler() {
		super();
	}

	protected IExperimentEditorManager getController() {
		if (controller == null) {
			this.controller = ExperimentFactory.getExperimentEditorManager();
		}
		return controller;
	}

	protected ACTION_TYPE getActionType(final String id) {

		final boolean isFolder = id.endsWith("FolderCommand");
		if (isFolder)
			return ACTION_TYPE.FOLDER;

		final boolean isScan = id.endsWith("ScanCommand");
		if (isScan)
			return ACTION_TYPE.SCAN;

		final boolean isRun = id.endsWith("RunCommand");
		if (isRun)
			return ACTION_TYPE.RUN;

		if (isFolder())
			return ACTION_TYPE.FOLDER;
		if (isScan())
			return ACTION_TYPE.SCAN;
		if (isRun())
			return ACTION_TYPE.RUN;

		return null;
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// Does nothing
	}

	@Override
	public void run(IAction action) {
		try {
			execute(null);
		} catch (ExecutionException e) {
			logger.error("Exception occured executing " + AbstractExperimentCommandHandler.class.getName(), e);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// Does nothing
	}

	/**
	 * @return true if folder
	 */
	public boolean isFolder() {
		final IWorkbenchPage page = EclipseUtils.getActivePage();
		if (page == null)
			return false;

		final IEditorPart ed = page.getActiveEditor();
		if (ed instanceof ExperimentFolderEditor)
			return true;

		if (ed == null) {
			return getController().getSelected() instanceof File;
		}

		return false;
	}

	/**
	 * @return true if scan selected
	 */
	public boolean isScan() {
		final IWorkbenchPage page = EclipseUtils.getActivePage();
		if (page == null)
			return false;

		final IEditorPart ed = page.getActiveEditor();
		if (ed instanceof ExperimentRunEditor)
			return false;

		if (ed == null) {
			return getController().getSelected() instanceof IExperimentObjectManager;
		}

		return false;
	}

	/**
	 * @return true if run selected
	 */
	public boolean isRun() {
		final IWorkbenchPage page = EclipseUtils.getActivePage();
		if (page == null)
			return false;

		final IEditorPart ed = page.getActiveEditor();
		if (ed instanceof ExperimentRunEditor)
			return true;

		return getController().getSelected() instanceof IExperimentObject;
	}

}
