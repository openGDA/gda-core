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

import uk.ac.diamond.tomography.reconstruction.INexusFilterInfoProvider;

public class NexusNewFilterAction extends Action {

	private INexusFilterInfoProvider filterPathProvider;
	private String[] history;

	public NexusNewFilterAction(INexusFilterInfoProvider filterProvider, String[] history) {
		super("&New Filter..."); //$NON-NLS-1$
		setImageDescriptor(NexusNavigatorUiImages.filterIcon);
		this.filterPathProvider = filterProvider;
		this.history = history;
	}

	@Override
	public void run() {
		NexusFileFilterDialog createFilter = new NexusFileFilterDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), filterPathProvider.getSuggestedPath(), history);
		if (createFilter.open() == Window.OK) {
			filterPathProvider.setFilterDescriptor(createFilter.getFilterDescriptor());
		}
	}
}
