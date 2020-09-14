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
import uk.ac.diamond.daq.mapping.ui.browser.ScanningAcquisitionBrowserBase;

/**
 * Shows the detector exposure in a resource's {@link ScanningParameters}
 */
public class ExposureLabelProvider extends LabelProvider implements ComparableStyledLabelProvider {

	@Override
	public StyledString getStyledText(Object element) {
		return new StyledString(String.valueOf(getDetectorExposure(element)));
	}

	@Override
	public ViewerComparator getComparator() {
		return new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object first, Object second) {
				double firstExposure = getDetectorExposure(first);
				double secondExposure = getDetectorExposure(second);

				int direction = ((TreeViewer) viewer).getTree().getSortDirection() == SWT.UP ? 1 : -1;

				return direction * (firstExposure < secondExposure ? 1 : -1);
			}
		};
	}

	private double getDetectorExposure(Object resource) {
		ScanningParameters parameters = ScanningAcquisitionBrowserBase.getAcquisitionParameters(resource);
		return parameters.getDetector().getExposure();
	}

}
