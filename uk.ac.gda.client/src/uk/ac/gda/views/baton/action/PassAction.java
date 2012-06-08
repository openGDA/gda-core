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

package uk.ac.gda.views.baton.action;

import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

/**
 *
 */
public class PassAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ClientDetails selectedDetails = getSelectedDetails();
		
		if (selectedDetails==null) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Selection Required",
					                                "Please select a user to pass the baton to.");
			return Boolean.FALSE;
		}
		
		try {
			if (selectedDetails.getIndex()!=InterfaceProvider.getBatonStateProvider().getMyDetails().getIndex()) {
				final boolean ok = MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						                 "Confirm pass",
						                 "Would you like to pass the baton to '"+selectedDetails.getUserID()+"'?");
				if (!ok) return Boolean.FALSE;
			} else {
				MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Cannot Pass",
                                                                                                           "The selected user already has the baton.\n\nPlease select another user to pass the baton to.");
			
			}
		} catch (Exception ne) {
			throw new ExecutionException("Cannot check if current user is the same as selected user.", ne);
		}
		
		InterfaceProvider.getBatonStateProvider().assignBaton(selectedDetails.getIndex());
		return Boolean.TRUE;
	}
	
	private ClientDetails getSelectedDetails() {
		ClientDetails selectedDetails;
		try {
			final StructuredSelection sel = (StructuredSelection)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
			selectedDetails = (ClientDetails)sel.getFirstElement();
		} catch (Exception ne) {
			return null;
		}
		return selectedDetails;
	}

//	@Override
//	public boolean isEnabled() {
//		ClientDetails selectedDetails = getSelectedDetails();
//		if (selectedDetails==null) return false;
//		
//		return true;
//	}  

}
