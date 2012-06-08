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

package uk.ac.gda.client.experimentdefinition.ui.wizards;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.experimentdefinition.components.ExperimentExperimentView;
import uk.ac.gda.client.experimentdefinition.components.ExperimentFolderEditor;

public class ImportExperimentWizard extends Wizard implements IWizard {

	ImportExperimentWizardPage page;
	
	@Override
	public boolean performFinish() {


		String source = page.getRootPath() + "/" + page.getYears().getSelection()[0] + "/"
				+ page.getVisits().getSelection()[0] + "/xml/" + page.getExperiments().getSelection()[0];

		String currentDir = PathConstructor.createFromProperty(LocalProperties.GDA_DATAWRITER_DIR);
		String destination = currentDir + "xml/" + page.getExperiments().getSelection()[0] + "_imported";

		File src = new File(source);
		File dst = new File(destination);
		try {
			copyDirectory(src, dst);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}

		final IExperimentEditorManager man = ExperimentFactory.getExperimentEditorManager();
		if (man == null)
			return false;

		final IProject project = man.getCurrentProject();
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
		}

		// If ExperimentExperimentView selected, refresh that.
		final ExperimentExperimentView ev = man.getViewer();
		if (ev != null)
			ev.refreshTree();

		final ExperimentFolderEditor fe = man.getActiveFolderEditor();
		if (fe != null)
			fe.refresh();

		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		page = new ImportExperimentWizardPage();
		addPage(page);
	}
	
	public void copyDirectory(File srcPath, File dstPath) throws IOException {

		if (srcPath.isDirectory()) {
			if (!dstPath.exists()) {
				dstPath.mkdir();
			}
			String files[] = srcPath.list();
			for (int i = 0; i < files.length; i++) {
				copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
			}
		}

		else {
			if (!srcPath.exists()) {
				
			}
			
			else{
				InputStream in = new FileInputStream(srcPath);
				OutputStream out = new FileOutputStream(dstPath);
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0)
					out.write(buf, 0, len);

				in.close();
				out.close();
			}
		}
		System.out.println("Directory copied.");
	}
}
