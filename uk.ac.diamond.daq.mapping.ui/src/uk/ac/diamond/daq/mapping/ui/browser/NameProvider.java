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

import java.util.Comparator;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import gda.rcp.views.Browser;
import uk.ac.diamond.daq.mapping.ui.experiment.file.IComparableStyledLabelProvider;
import uk.ac.diamond.daq.mapping.ui.experiment.file.SavedScanMetaData;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;

/**
 * Formats the SavedScanMetaData name for a {@link Browser} column.
 *
 * @author Maurizio Nagni
 */
class NameProvider extends LabelProvider implements IComparableStyledLabelProvider {

	@Override
	public StyledString getStyledText(Object element) {
		AcquisitionConfigurationResource<SavedScanMetaData> el = (AcquisitionConfigurationResource<SavedScanMetaData>) element;
		return new StyledString(splitOnDot(MapBrowser.extractSavedScanMetaData(element))[0]);
	}

	@Override
	public ViewerComparator getComparator() {
		return new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object element1, Object element2) {
				String first = splitOnDot(MapBrowser.extractSavedScanMetaData(element1).toString())[0].toLowerCase();
				String second = splitOnDot(MapBrowser.extractSavedScanMetaData(element2).toString())[0].toLowerCase();

				Comparator<String> c = Comparator.comparing(String::toString);
				int direction = ((TreeViewer) viewer).getTree().getSortDirection() == SWT.UP ? 1 : -1;
				return direction * c.compare(first, second);
			}
		};
	}

}