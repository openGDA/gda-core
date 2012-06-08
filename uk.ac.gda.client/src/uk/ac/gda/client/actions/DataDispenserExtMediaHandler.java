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

package uk.ac.gda.client.actions;

import gda.rcp.GDAClientActivator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.service.prefs.Preferences;

public class DataDispenserExtMediaHandler extends DataDispenserAbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Shell shell = HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell();

		Preferences preferences = new DefaultScope().getNode(GDAClientActivator.PLUGIN_ID);
		String string = preferences.get(DATA_DISPENSER_MEDIA_URL_PREF, DATA_DISPENSER_DEFAULT_PREF);
		
		if (string.equals(DATA_DISPENSER_DEFAULT_PREF)){
			MessageDialog.openError(shell, "Error Opening Data Dispenser", "No value for the data dispenser external media url has been set (in plugin_customization.ini)");
		} else {
			openInBrowser(string);
		}
		
		return null;

	}

}