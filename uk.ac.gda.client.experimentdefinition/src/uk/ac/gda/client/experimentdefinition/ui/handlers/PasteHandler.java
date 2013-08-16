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
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.action.IAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.components.ExperimentFolderEditor;
import uk.ac.gda.util.io.FileUtils;

public class PasteHandler extends AbstractExperimentCommandHandler {

	private static Logger logger = LoggerFactory.getLogger(PasteHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		pasteSelectedFile();
		return true;
	}

	@Override
	public void run(IAction action) {
		pasteSelectedFile();
	}

	private void pasteSelectedFile() {

		final IFolder folder = getEditorManager().getSelectedFolder();

		final List<IFile> sel = CopyHandler.getCopied();
		for (IFile file : sel) {
			final String name = file.getName();
			final String temp = name.substring(0, name.lastIndexOf('.'));
			final String ext = name.substring(name.lastIndexOf('.'));

			final File t = FileUtils.getUnique(folder.getLocation().toFile(), temp, ext);
			try {
				final IFile to = folder.getFile(t.getName());
				file.copy(to.getFullPath(), true, null);
			} catch (Exception e) {
				logger.error("Cannot copy " + file, e);
			}

		}

		final ExperimentFolderEditor ed = getEditorManager().getActiveFolderEditor();
		if (ed != null)
			ed.refresh();
	}

	@Override
	public boolean isEnabled() {
		return CopyHandler.isValidPastePossible();
	}

}
