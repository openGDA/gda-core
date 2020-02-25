/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.browser;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import gda.rcp.views.Browser;
import gda.rcp.views.ComparableStyledLabelProvider;

/**
 * Formats the tomography type for a {@link Browser} column.
 *
 * @author Maurizio Nagni
 */
class ScanTypeProvider extends LabelProvider implements ComparableStyledLabelProvider {

	@Override
	public StyledString getStyledText(Object element) {
		return new StyledString(TomoBrowser.getTomographyParameters(element).getScanType().toString());
	}

	@Override
	public ViewerComparator getComparator() {
		return new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object element1, Object element2) {
				return -1;
			}
		};
	}

}
