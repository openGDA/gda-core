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

package uk.ac.gda.ui.viewer;

import java.io.File;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


/**
 * A class to hold common patterns for viewer filters such as File names or
 * String tests - etc.
 */
public class ViewerFilterFactory {

	/**
	 * A filter whom returns true unless the element is a file (not directory) and its extension is
	 * the same as 'extension'. NOTE the '.' is not added. Case is not sensitive.
	 * 
	 * @param extension
	 * @return ViewerFilter
	 */
	public static ViewerFilter createFileExtensionFilter(final String extension) {
		return new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof File) {
					final File file = (File)element;
					if (file.isFile()) {
						return file.getName().toLowerCase().endsWith(extension.toLowerCase());
					}
				}
				return true;
			}
		};
	}

}
