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

package uk.ac.diamond.tomography.reconstruction.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.tomography.reconstruction.INexusPathProvider;

public class NexusNewSortAction extends Action {

	private INexusPathProvider sortProvider;
	private String[] history;
	
	public NexusNewSortAction(INexusPathProvider sortProvider, String[] history) {
		super("&Sort..."); //$NON-NLS-1$
		setImageDescriptor(NexusNavigatorUiImages.sortIcon);
		this.sortProvider = sortProvider;
		this.history = history;
	}
	
	@Override
	public void run() {
		NexusFileSortDialog sortDialog = new NexusFileSortDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(),sortProvider.getSuggestedPath(), history);
		if (sortDialog.open() == Window.OK) {
			sortProvider.setNexusPath(sortDialog.getNexusPath());
		}
	}
}
