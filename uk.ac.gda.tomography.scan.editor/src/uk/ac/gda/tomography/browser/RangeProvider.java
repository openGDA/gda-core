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
import uk.ac.gda.tomography.base.TomographyParameters;

/**
 * Formats the tomography range for a {@link Browser} column.
 *
 * @author Maurizio Nagni
 */
class RangeProvider extends LabelProvider implements ComparableStyledLabelProvider {

	@Override
	public StyledString getStyledText(Object element) {
		TomographyParameters parameters = TomoBrowser.getTomographyParameters(element);
		double start = parameters.getStart().getStart();
		double end;
		switch (parameters.getEnd().getRangeType()) {
		case RANGE_180:
			end = 180.0;
			break;
		case RANGE_360:
			end = 360.0;
			break;
		case CUSTOM:
			end = parameters.getEnd().getCustomAngle();
			break;
		default:
			end = Double.NaN;
		}
		return new StyledString(String.format("%1$.2f : %2$.2f", start, end));
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
