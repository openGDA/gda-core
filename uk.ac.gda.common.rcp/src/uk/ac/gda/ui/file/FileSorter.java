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
import java.util.Comparator;

import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Marks any element that is a File and not a folder as sortable.
 */
public class FileSorter extends ViewerComparator {

	/**
	 * 
	 */
	public FileSorter() {
		super();
	}
	/**
	 * @param sort
	 */
	public FileSorter(Comparator<?> sort) {
		super(sort);
	}

	@Override
	public boolean isSorterProperty(Object element, String property) {
        if (element instanceof File) {
        	return !((File)element).isDirectory();
        }
        return false;
    }
}
