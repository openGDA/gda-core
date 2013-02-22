/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition.wizards;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ImportXMLWizard extends Wizard implements IImportWizard {

	private ImportXMLWizardPage fileChooserPage;
	private TargetFolderPage targetFolderPage;
	private IWorkbench workbench;

	public ImportXMLWizard() {
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		fileChooserPage = new ImportXMLWizardPage("File chooser page");
		addPage(fileChooserPage);
		targetFolderPage = new TargetFolderPage("Target folder page");
		addPage(targetFolderPage);
	}

	@Override
	public boolean performFinish() {
		// do the copying of files listed by fileChooserPage to the location in targetFolderPage
		String[] selectedFilenames = fileChooserPage.selectedFilenames;
		String sourceFolder = fileChooserPage.selectedFolder;
		String targetFolder = targetFolderPage.targetDir;
		File targetDir = new File(targetFolder);

		String renameMessage = "";
		try {
			for (String original : selectedFilenames) {
				String sourceFullPath = FilenameUtils.concat(sourceFolder, original);
				String targetFullPath = FilenameUtils.concat(targetFolder, original);

				File source = new File(sourceFullPath);
				File target = new File(targetFullPath);

				if (target.exists()) {
					target = uk.ac.gda.util.io.FileUtils.getUnique(targetDir, FilenameUtils.getBaseName(original),
							FilenameUtils.getExtension(sourceFullPath));
					renameMessage += original +"  was called " + target + "\n\n";
				}

				FileUtils.copyFile(source, target, false);
			}
			String message = "Copied files from " + sourceFolder + " to " + targetFolder;
			if (!renameMessage.isEmpty()) {
				message += "\n\n\nThere were some clashes of file names:\n\n\n" + renameMessage;
			}
			MessageDialog.openInformation(workbench.getActiveWorkbenchWindow().getShell(), "Files imported",
					message);
		} catch (IOException e) {
			MessageDialog.openError(workbench.getActiveWorkbenchWindow().getShell(), "Import Failed",
					"Failed to copy files from " + sourceFolder + " to " + targetFolder + "\n" + e.getMessage());
		}

		return true;
	}

}
