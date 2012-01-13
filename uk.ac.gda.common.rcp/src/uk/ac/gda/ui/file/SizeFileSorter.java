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

package uk.ac.gda.ui.file;

import java.io.File;

import org.eclipse.jface.viewers.Viewer;


/**
 *
 */
public class SizeFileSorter extends FileSorter {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (!(e1 instanceof File)||!(e2 instanceof File)) {
			return super.compare(viewer, e1, e2);
		}
		final File f1 = (File)e1;
		final File f2 = (File)e2;
		return (int)(f1.length()-f2.length());
	}
}
