/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment.file;

import java.util.Comparator;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;


/**
 * Label provider for the Scan description column of the view which extracts its content from the first section
 * of the filename.
 *
 * @since GDA 9.13
 */
public class SavedScanNameLabelProvider extends LabelProvider implements IComparableStyledLabelProvider {

	@Override
	public StyledString getStyledText(Object element) {
		return element instanceof String ?  new StyledString(splitOnDot(element)[0]) : new StyledString();
	}

	@Override
	public ViewerComparator getComparator() {
		return new ViewerComparator() {
		    @Override
			public int compare(Viewer viewer, Object element1, Object element2) {
		    	String first = splitOnDot(element1)[0].toLowerCase();
		    	String second = splitOnDot(element2)[0].toLowerCase();

		    	Comparator<String> c = Comparator.comparing(String::toString);
		    	int direction = ((TreeViewer)viewer).getTree().getSortDirection() == SWT.UP ? 1 : -1;
		    	return direction * c.compare(first, second);
		    }
		};
	}
}
