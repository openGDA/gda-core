/*
 * Copyright © 2011 Diamond Light Source Ltd.
 * Contact :  ScientificSoftware@diamond.ac.uk
 * 
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.rcp.results.navigator.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.rcp.results.navigator.DataNavigator;

/**
 * This class currently asks the user for a different root file to 
 * set as the file for the nexus tree.
 */
public class SwitchDataSourceAction extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final DirectoryDialog dialog = new DirectoryDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN);
		dialog.setFilterPath(System.getProperty("gda.data"));
		final String          path   = dialog.open();
		if (path!=null) {
			final DataNavigator view = (DataNavigator)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DataNavigator.ID);
			if (view == null) return Boolean.FALSE;
            view.setSelectedPath(path);
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

}
