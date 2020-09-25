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

package uk.ac.diamond.daq.mapping.ui.browser;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import gda.rcp.views.Browser;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.diamond.daq.mapping.ui.experiment.file.IComparableStyledLabelProvider;

/**
 * Provides a summary of 2D path in {@link ScanningParameters} configurations for a column in a {@link Browser}
 *
 * @author Maurizio Nagni
 */
class DetailsLabelProvider extends LabelProvider implements IComparableStyledLabelProvider {


	@Override
	public StyledString getStyledText(Object element) {
		ScanningParameters parameters = ScanningAcquisitionBrowserBase.getAcquisitionParameters(element);
		String details = getDetailsSummary(parameters.getScanpathDocument());
		return new StyledString(details);
	}

	private String getDetailsSummary(ScanpathDocument scanpathDocument) {
		switch (scanpathDocument.getModelDocument()) {
		case TWO_DIMENSION_GRID:
			return getGridDetails(scanpathDocument);
		case TWO_DIMENSION_LINE:
			return getLineDetails(scanpathDocument);
		case TWO_DIMENSION_POINT:
			return getPointDetails(scanpathDocument);
		default:
			return "Details unavailable";
		}
	}

	private String getGridDetails(ScanpathDocument scanpathDocument) {
		ScannableTrackDocument track1 = scanpathDocument.getScannableTrackDocuments().get(0);
		ScannableTrackDocument track2 = scanpathDocument.getScannableTrackDocuments().get(1);
		String axes = getAxesString(track1, track2);
		return String.format("%s%d x %d points; (%.1f, %.1f) to (%.1f, %.1f)",
				axes,
				track1.getPoints(), track2.getPoints(),
				track1.getStart(), track2.getStart(),
				track1.getStop(), track2.getStop());
	}

	private String getLineDetails(ScanpathDocument scanpathDocument) {
		ScannableTrackDocument track1 = scanpathDocument.getScannableTrackDocuments().get(0);
		ScannableTrackDocument track2 = scanpathDocument.getScannableTrackDocuments().get(1);
		String axes = getAxesString(track1, track2);
		return String.format("%s%d points; (%.1f, %.1f) to (%.1f, %.1f)",
				axes,
				track1.getPoints(),
				track1.getStart(), track2.getStart(),
				track1.getStop(), track2.getStop());
	}

	private String getPointDetails(ScanpathDocument scanpathDocument) {
		ScannableTrackDocument track1 = scanpathDocument.getScannableTrackDocuments().get(0);
		ScannableTrackDocument track2 = scanpathDocument.getScannableTrackDocuments().get(1);
		String axes = getAxesString(track1, track2);
		return String.format("%s(%.1f, %.1f)",
				axes,
				track1.getStart(), track2.getStart());
	}

	private String getAxesString(ScannableTrackDocument track1, ScannableTrackDocument track2) {
		if (track1.getAxis() != null && track2.getAxis()!= null) {
			return String.format("Axes: [%s,%s],", track1.getAxis(), track2.getAxis());
		}
		return "";
	}

	/** Sorts by number of points along first axis */
	@Override
	public ViewerComparator getComparator() {
		return new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object element1, Object element2) {

				int direction = ((TreeViewer) viewer).getTree().getSortDirection() == SWT.UP ? 1 : -1;

				ScanningParameters first = ScanningAcquisitionBrowserBase.getAcquisitionParameters(element1);
				ScanningParameters second = ScanningAcquisitionBrowserBase.getAcquisitionParameters(element2);

				return direction * (first.getScanpathDocument().getScannableTrackDocuments().get(0).getPoints()
						< second.getScanpathDocument().getScannableTrackDocuments().get(0).getPoints() ? 1 : -1);
			}
		};
	}

}
