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

package uk.ac.gda.exafs.ui.detector.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.exafs.ui.data.ScanObject;

public class SwitchScanWizard extends Wizard implements INewWizard{
	
	@SuppressWarnings("unused")
	private IStructuredSelection initialSelection;
	
	SwitchScanWizardPageOne page1;
	SwitchScanWizardPageTwo page2;
	
	@Override
	public boolean performFinish() {
		
		IFile newScanFile = page2.getNewScanFile();
		IFile newSampleFile = page2.getNewSampleFile();
		IFile newDetectorFile = page2.getNewDetectorFile();
		IFile newOutputFile = page2.getNewOutputFile();
		
		ScanObject selected = page2.getSelected();
		IExperimentEditorManager controller = page2.getController();
		
		selected.setScanFileName(newScanFile.getName());
		selected.setSampleFileName(newSampleFile.getName());
		selected.setDetectorFileName(newDetectorFile.getName());
		selected.setOutputFileName(newOutputFile.getName());
		
		try {
			selected.getRunFileManager().write();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		controller.openDefaultEditors(selected, true);
		
		return true;
	}
	
	@Override
	public void addPages() {
		setWindowTitle("Switch Scan Type");
		page1 = new SwitchScanWizardPageOne();
		addPage(page1);
		page2 = new SwitchScanWizardPageTwo();
		addPage(page2);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		initialSelection = selection;
	}
}
