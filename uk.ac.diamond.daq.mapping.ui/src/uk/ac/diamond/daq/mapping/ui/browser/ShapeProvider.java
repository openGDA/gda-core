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
import org.eclipse.swt.graphics.Image;

import gda.rcp.views.Browser;
import uk.ac.diamond.daq.mapping.ui.experiment.file.IComparableStyledLabelProvider;
import uk.ac.diamond.daq.mapping.ui.experiment.file.SavedScanMetaData;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Extracts from the SavedScanMetaData a value for the shape for a {@link Browser} column.
 *
 * @author Maurizio Nagni
 */
class ShapeProvider extends LabelProvider implements IComparableStyledLabelProvider {

	private final ClientImages[] ICONS = { ClientImages.POINT, ClientImages.CENTERED_RECTAGLE, ClientImages.CENTERED_RECTAGLE, ClientImages.LINE };

	/**
	 * Return the standard image representing the region shape of the scan or null if there is no matching icon file
	 */
	@Override
	public Image getImage(Object element) {
		String[] tokens = splitOnDot(extractSavedScanMetaData(element).toString());

		if (tokens.length > 2 && tokens[tokens.length - 2].matches("^S[0-9].*")) {
			int i = Integer.valueOf(tokens[1].substring(1, 2));
			return ClientSWTElements.getImage(ICONS[i]);
		}
		return null;
	}

	/**
	 * Dummy implementation returning and empty object
	 */
	@Override
	public StyledString getStyledText(Object element) {
		return new StyledString();
	}

	/**
	 * Compares based on the first two characters of the scan definition description string, which are S followed by
	 * the shape number and the direction indicated by the viewer column
	 */
	@Override
	public ViewerComparator getComparator() {
		return new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object element1, Object element2) {
				String[] first = splitOnDot(extractSavedScanMetaData(element1));
				String[] second = splitOnDot(extractSavedScanMetaData(element2));

				if (first.length < 3 || second.length < 3) {
					return (first.length < second.length) ? 1 : -1;
				}
				Comparator<String> c = Comparator.comparing(String::toString);
				int direction = ((TreeViewer) viewer).getTree().getSortDirection() == SWT.UP ? 1 : -1;
				return direction
						* c.compare(penultimateOf(first).substring(0, 2), penultimateOf(second).substring(0, 2));
			}
		};
	}

	private SavedScanMetaData extractSavedScanMetaData(Object element) {
		return ((AcquisitionConfigurationResource<SavedScanMetaData>) element).getResource();
	}

}
