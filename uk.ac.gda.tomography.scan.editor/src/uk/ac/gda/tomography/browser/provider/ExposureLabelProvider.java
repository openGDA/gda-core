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

import java.util.Optional;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import gda.rcp.views.ComparableStyledLabelProvider;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.browser.ScanningAcquisitionBrowserBase;

/**
 * Shows the detector exposure in a resource's {@link ScanningParameters}
 */
public class ExposureLabelProvider extends LabelProvider implements ComparableStyledLabelProvider {

	@Override
	public StyledString getStyledText(Object element) {
		double exposure =  getDetectorExposure(element);
		if (exposure == Double.MIN_VALUE) {
			return new StyledString(ClientMessagesUtility.getMessage(ClientMessages.NOT_AVAILABLE));
		}
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
		return Optional.ofNullable(resource)
				.map(ScanningAcquisitionBrowserBase::getAcquisitionParameters)
				.map(ScanningParameters::getDetectors)
				.filter(detectors -> !detectors.isEmpty())
				.map(list -> list.iterator().next())
				.map(DetectorDocument::getExposure)
				.orElseGet(() -> Double.MIN_VALUE);
	}

}
