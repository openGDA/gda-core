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

package uk.ac.gda.exafs.ui.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.client.experimentdefinition.ui.handlers.AbstractExperimentCommandHandler;
import uk.ac.gda.exafs.ui.detector.wizards.ImportExperimentWizard;

public class ImportExperimentCommandHandler extends AbstractExperimentCommandHandler {

//	private final static Logger logger = LoggerFactory.getLogger(ImportExperimentCommandHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		ImportExperimentWizard wizard = new ImportExperimentWizard();
		//wizard.init(workbench, new StructuredSelection());
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.open(); 
		return null;
	}

}
