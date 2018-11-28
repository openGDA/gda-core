/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.energyfocus.editor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.diamond.daq.mapping.api.FocusScanBean;
import gda.factory.Finder;
import gda.function.ILinearFunction;

public class EditEnergyFocusFunctionHandler extends AbstractHandler {
	private static final Logger logger = LoggerFactory.getLogger(EditEnergyFocusFunctionHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		try {
			final ILinearFunction energyFocusFunction = Finder.getInstance().find("energyFocusFunction");
			final FocusScanBean focusScanBean = PlatformUI.getWorkbench().getService(FocusScanBean.class);
			final String energyFocusConfigPath = focusScanBean.getEnergyFocusBean().getEnergyFocusConfigPath();
			final EditEnergyFocusDialog dialog = new EditEnergyFocusDialog(activeShell, energyFocusFunction, energyFocusConfigPath);
			dialog.open();
		} catch (Exception e) {
			logger.error("Exception opening editor for energy focus function", e);
			ErrorDialog.openError(activeShell, "Error editing energy focus function",
					"Please contact your support representative.",
					new Status(IStatus.ERROR, "uk.ac.gda.beamline.i08.commandhandlers", e.getMessage(), e));
		}
		return null;
	}
}
