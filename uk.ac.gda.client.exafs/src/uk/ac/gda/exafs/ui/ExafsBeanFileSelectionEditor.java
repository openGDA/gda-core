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

package uk.ac.gda.exafs.ui;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.client.experimentdefinition.components.ExperimentFolderEditor;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

/**
 * Exists purely to select beans back in the original folder, if it is open.
 */
public abstract class ExafsBeanFileSelectionEditor extends RichBeanMultiPageEditorPart {

	@Override
	public void setFocus() {
		super.setFocus();

		// Select this file in any open folder editor.
		final IWorkbenchPage page = getSite().getPage();
		final IFile file = ExperimentFactory.getExperimentEditorManager().getIFile(getEditorInput());
		if (file == null)
			return;
		final IFolder dir = (IFolder) file.getParent();
		final IEditorReference[] refs = page.getEditorReferences();
		for (int i = 0; i < refs.length; i++) {
			if (refs[i].getId().equals(ExperimentFolderEditor.ID)) {
				final ExperimentFolderEditor ed = (ExperimentFolderEditor) refs[i].getEditor(true);
				if (ed.getCurrentDirectory().equals(dir)) {
					ed.setSelected(file);
				}
			}
		}
	}

	@Override
	protected boolean confirmFileNameChange(final File oldName, final File newName) throws Exception {
		
		// Save as is not allowed to change the directory for these editors
		if (!oldName.getParent().equals(newName.getParent())) {
			if (oldName.getParentFile().getAbsolutePath() != newName.getParentFile().getAbsolutePath()) {
				MessageDialog.openError(getSite().getShell(), "Cannot save file.", "The file '" + newName.getName()
						+ "' cannot be created in:\n'" + newName.getParentFile().getAbsolutePath() + "'.\n\n"
						+ "You must save the file as an XML in the original experiment directory:\n'"
						+ oldName.getParentFile().getAbsolutePath() + "'");
				return false;
			}
		}

		// Make sure that the IExperimentObject is aware of the name change
		IExperimentObject ob = ExperimentFactory.getExperimentEditorManager().getSelectedScan();
		if (ob == null) {
			MessageDialog.openError(getSite().getShell(), "Cannot save file.", "The file '" + newName.getName()
					+ "' cannot be saved in '" + newName.getParentFile().getName() + "'.\n\n"
					+ "No scan has been selected. Please enter multi-scan mode and select a scan you wish to edit.");
		} else {
			ob.renameFile(oldName.getName(), newName.getName());
			IExperimentObjectManager man = ExperimentFactory.getManager(ob);
			man.write();
		}

		return true;
	}

	@Override
	protected String validateFileName(final String newFile) {
		final File file = new File(newFile);
		final String name = file.getName();
		if (name == null || "".equals(name) || !name.matches("\\w+\\.xml")) {
			MessageDialog.openError(getSite().getShell(), "Cannot save file.", "The file '" + name
					+ "' is not a legal file name'.\n\n"
					+ "Please save the file name without spaces and alphanumeric characters only.");
			return null;
		}
		return newFile;
	}
}
