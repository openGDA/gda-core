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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;

import uk.ac.gda.client.experimentdefinition.components.ExperimentFolderEditor;

public class CopyHandler extends AbstractExperimentCommandHandler {

	private static List<IFile> currentCopied;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		copySelectedFile();
		return true;
	}

	@Override
	public void run(IAction action) {
		copySelectedFile();
	}

	private void copySelectedFile() {
		final ExperimentFolderEditor ed = getEditorManager().getActiveFolderEditor();
		if (ed == null)
			return;

		currentCopied = ed.getSelected();
	}

	/**
	 * @return true if some selected files have been copied.
	 */
	public static boolean isValidPastePossible() {

		if (currentCopied == null)
			return false;

		final Iterator<IFile> it = currentCopied.iterator();
		while (it.hasNext()) {
			final IFile file = it.next();
			if (!file.exists())
				it.remove();
		}

		if (currentCopied.isEmpty()) {
			currentCopied = null;
			return false;
		}
		return true;
	}

	/**
	 * @return objects currently being copied, if any.
	 */
	public static List<IFile> getCopied() {
		return currentCopied;
	}
}
