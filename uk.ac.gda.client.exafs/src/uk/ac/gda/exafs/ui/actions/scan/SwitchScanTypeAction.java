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

package uk.ac.gda.exafs.ui.actions.scan;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentBeanDescription;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.experimentdefinition.components.XMLFileDialog;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;

public class SwitchScanTypeAction extends AbstractHandler implements IWorkbenchWindowActionDelegate,
		IEditorActionDelegate {

	private static final Logger logger = LoggerFactory.getLogger(SwitchScanTypeAction.class);

	@Override
	public void init(final IWorkbenchWindow window) {
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		doSwitch();
		return true;
	}

	@Override
	public void run(IAction action) {
		doSwitch();
	}

	private List<IExperimentBeanDescription> getScanBeanTypes() {

		List<IExperimentBeanDescription> beanTypes = new ArrayList<IExperimentBeanDescription>();
		List<IExperimentBeanDescription> allBeanTypes = ExperimentBeanManager.INSTANCE.getBeanDescriptions();
		for (IExperimentBeanDescription type : allBeanTypes) {
			if (ScanObjectManager.isXESOnlyMode() && type.getName().equals("XES Scan")) {
				beanTypes.add(type);
			} else if (!ScanObjectManager.isXESOnlyMode() && type.getBeanType().equals(ScanObject.SCANBEANTYPE)
					&& !type.getName().equals("XES Scan")) {
				beanTypes.add(type);
			}
		}
		return beanTypes;
	}

	private void doSwitch() {

		final IExperimentEditorManager controller = ExperimentFactory.getExperimentEditorManager();
		final ScanObject selected = (ScanObject) controller.getSelectedScan();
		if (selected == null)
			return;

		final XMLFileDialog dialog = new XMLFileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				getScanBeanTypes(), "Change Scan Type", "Choose a file type to change the current scan to. "
						+ "This file is then used to configure the scan required.");

		IFile choice = dialog.open(selected.getRunFileManager().getContainingFolder());
		if (choice == null) {
			return;
		}

		switchScanFile(choice);
	}

	private void switchScanFile(IFile newFile) {
		final IExperimentEditorManager controller = ExperimentFactory.getExperimentEditorManager();
		final ScanObject selected = (ScanObject) controller.getSelectedScan();
		selected.setScanFileName(newFile.getName());
		try {
			selected.getRunFileManager().write();
		} catch (Exception e) {
			logger.error("Cannot write: " + selected.getRunFileManager().getFile(), e);
		}
		controller.openDefaultEditors(selected, true);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
	}

}
