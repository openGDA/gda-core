/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class OpenPreferenceCommandHandler extends AbstractHandler implements IHandler {

	public static final String COMMAND_PARAM_PREF_PAGE_ID = "reconPrefPage";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String parameter = event.getParameter(COMMAND_PARAM_PREF_PAGE_ID);
		if (parameter != null) {
			PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), parameter, null, null);
			if (pref != null) {
				pref.open();
			}
		}

		return null;
	}

}