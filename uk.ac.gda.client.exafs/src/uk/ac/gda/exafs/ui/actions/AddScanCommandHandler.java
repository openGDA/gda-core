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

package uk.ac.gda.exafs.ui.actions;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.client.experimentdefinition.ui.handlers.AbstractExperimentCommandHandler;
import uk.ac.gda.exafs.ui.detectorviews.wizards.AddScanWizard;

/**
 * I do not think this class is used.
 */
@Deprecated(since="GDA 8.40")
public class AddScanCommandHandler extends AbstractExperimentCommandHandler {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(AddScanCommandHandler.class);

	public AddScanCommandHandler() {
		logger.deprecatedClass();
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {

		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		AddScanWizard wizard = new AddScanWizard();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.open();

		return null;
	}

}
