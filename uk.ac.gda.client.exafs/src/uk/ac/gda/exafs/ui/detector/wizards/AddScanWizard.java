/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.exafs.ui.data.ScanObject;

public class AddScanWizard extends Wizard implements INewWizard{
	
	AddScanWizardPageOne page1;
	AddScanWizardPageTwo page2;
	
	@SuppressWarnings("unused")
	private IStructuredSelection initialSelection;

	private IExperimentEditorManager controller = null;
	
	@Override
	public void init(IWorkbench arg0, IStructuredSelection selection) {
		initialSelection = selection;
	}
	
	protected IExperimentEditorManager getController() {
		if (controller == null) {
			this.controller = ExperimentFactory.getExperimentEditorManager();
		}
		return controller;
	}
	
	@Override
	public boolean performFinish() {
		final IExperimentObjectManager man = getController().getSelectedMultiScan();
		if (man == null)
			return false;

		final IExperimentObject ob = getController().getSelectedScan();

		final ScanObject created = (ScanObject) man.insertNewExperimentAfter(ob);
		
		if (getController().getActiveRunEditor() != null) {
			getController().getActiveRunEditor().editRunName(created);
		} else if (getController().getViewer() != null) {
			getController().refreshViewers();
			getController().setSelected(created);
		}
		
		IFile newScanFile = page2.getNewScanFile();
		
		created.setScanFileName(newScanFile.getName());
		
		
		try {
			ExperimentFactory.getManager(ob).write();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		controller.openDefaultEditors(created, true);
		
		return true;
	}
	
	@Override
	public void addPages() {
		setWindowTitle("Add Scan");
		page1 = new AddScanWizardPageOne();
		addPage(page1);
		page2 = new AddScanWizardPageTwo();
		addPage(page2);
	}

}
