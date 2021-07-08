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

package uk.ac.gda.tomography.browser.provider;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import gda.rcp.views.ComparableStyledLabelProvider;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.gda.ui.tool.browser.ScanningAcquisitionBrowserBase;

/**
 * Shows the number of points in the first {@link ScannableTrackDocument} in a resource's {@link ScanningParameters}.
 */
public class ProjectionsLabelProvider extends LabelProvider implements ComparableStyledLabelProvider {

	@Override
	public StyledString getStyledText(Object element) {
		return new StyledString(String.valueOf(getProjections(element)));
	}

	@Override
	public ViewerComparator getComparator() {
		return new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object scan1, Object scan2) {
				int points1 = getProjections(scan1);
				int points2 = getProjections(scan2);

				int direction = ((TreeViewer) viewer).getTree().getSortDirection() == SWT.UP ? 1 : -1;

				return direction * (points1 < points2 ? 1 : -1);
			}
		};
	}

	/** Assumes scan is a simple 1D tomography */
	private int getProjections(Object resource) {
		ScanningParameters parameters = ScanningAcquisitionBrowserBase.getAcquisitionParameters(resource);
		return parameters.getScanpathDocument().getScannableTrackDocuments().get(0).getPoints();
	}

}
