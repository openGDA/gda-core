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

import gda.jython.IJythonContext;
import gda.jython.JythonServerFacade;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.client.experimentdefinition.components.ExperimentFolderEditor;
import uk.ac.gda.util.io.FileUtils;

public class CopyFilesToScriptsCommandHandler extends AbstractExperimentCommandHandler {

	private final static Logger logger = LoggerFactory.getLogger(CopyFilesToScriptsCommandHandler.class);
	private IJythonContext jythonContextForTesting;

	@Override
	public void run(IAction action) {
		doCopy();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return doCopy();
	}

	private Object doCopy() {

		final ExperimentFolderEditor editor = getController().getActiveFolderEditor();

		final Collection<IFile> sel;

		if (editor != null) {
			sel = editor.getSelected();
		} else if (getController().getSelected() instanceof IExperimentObjectManager) {
			final IExperimentObjectManager man = (IExperimentObjectManager) getController().getSelected();
			sel = man.getReferencedFiles();
		} else if (getController().getSelected() instanceof IExperimentObject) {
			final IExperimentObject ob = (IExperimentObject) getController().getSelected();
			sel = ob.getFiles();
		} else {
			return false;
		}

		try {
			final File scripts = new File(getDestinationFolderForCopy());
			for (Object o : sel) {
				final File file = (File) o;
				if (file.isDirectory())
					continue;
				final File to = new File(scripts, file.getName());

				if (to.exists()) {
					final Shell shell = editor != null ? editor.getSite().getShell() : PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell();
					final boolean copyAnyway = MessageDialog.openQuestion(shell, "File Existing",
							"The file '" + to.getPath() + "' already exists.\n\nWould you like to overwrite?");
					if (!copyAnyway)
						return false;
				}

				FileUtils.copy(file, to);

			}
		} catch (Exception e) {
			logger.error("Cannot copy", e);// Not expected.
		}

		return true;
	}

	private IJythonContext getJythonContext() {
		return jythonContextForTesting != null ? jythonContextForTesting : JythonServerFacade.getInstance();
	}

	public void setJythonContextForTesting(IJythonContext context) {
		logger.warn("Overriding jython context for testing purposes only");
		jythonContextForTesting = context;
	}

	public String getDestinationFolderForCopy() {
		return getJythonContext().getDefaultScriptProjectFolder();
	}
}
