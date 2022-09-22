/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import org.eclipse.swt.widgets.Shell;

public class NexusFileSortDialog extends BaseNexusPathDialog {

	private String sortPath;

	public NexusFileSortDialog(Shell parentShell, String initialPath, String[] ruPaths) {
		super(parentShell, initialPath, ruPaths);
		setTitle("NeXus File Sorting");
	}

	public String getNexusSortPath() {
		return sortPath;
	}

	@Override
	protected void okPressed() {
		sortPath = getNexusPath();
		super.okPressed();
	}

}
