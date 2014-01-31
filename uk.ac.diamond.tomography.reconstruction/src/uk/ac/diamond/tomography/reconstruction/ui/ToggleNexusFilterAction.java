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
import org.eclipse.jface.action.IAction;

import uk.ac.diamond.tomography.reconstruction.INexusPathProvider;

public class ToggleNexusFilterAction extends Action {

	private INexusPathProvider pathProvider;
	private String filterPath;
	
	public ToggleNexusFilterAction(String path, INexusPathProvider pathProvider) {
		super(path+'@', IAction.AS_CHECK_BOX); //$NON-NLS-1$
		setToolTipText("Filter on "+ path);
		this.filterPath = path;
		this.pathProvider = pathProvider;
		String defaultPath = pathProvider.getNexusPath();
		if (defaultPath != null)setChecked(defaultPath.equals(path));
		
	}
	
	@Override
	public void run() {
		if (isChecked()) pathProvider.setNexusPath(filterPath);
	}
}
