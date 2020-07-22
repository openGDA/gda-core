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

import static uk.ac.diamond.daq.mapping.ui.browser.ScanningAcquisitionBrowserBase.getAcquisitionParameters;

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import gda.rcp.views.Browser;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanning.ShapeType;
import uk.ac.diamond.daq.mapping.ui.experiment.file.IComparableStyledLabelProvider;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Extracts from the {@link ScanningParameters} a value for the shape for a {@link Browser} column.
 *
 * @author Maurizio Nagni
 */
class ShapeLabelProvider extends LabelProvider implements IComparableStyledLabelProvider {

	private static final Map<ShapeType, ClientImages> ICONS = new EnumMap<>(ShapeType.class);

	static {
		ICONS.put(ShapeType.POINT, ClientImages.POINT);
		ICONS.put(ShapeType.LINE, ClientImages.LINE);
		ICONS.put(ShapeType.CENTRED_RECTANGLE, ClientImages.CENTERED_RECTAGLE);
	}

	/**
	 * Return the standard image representing the region shape of the scan or null if there is no matching icon file
	 */
	@Override
	public Image getImage(Object element) {
		ScanningParameters parameters = getAcquisitionParameters(element);
		return ClientSWTElements.getImage(ICONS.get(parameters.getShapeType()));
	}

	/**
	 * No text required for this column
	 */
	@Override
	public StyledString getStyledText(Object element) {
		return new StyledString();
	}

	@Override
	public ViewerComparator getComparator() {
		return new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object element1, Object element2) {
				ScanningParameters first = getAcquisitionParameters(element1);
				ScanningParameters second = getAcquisitionParameters(element2);

				int direction = ((TreeViewer) viewer).getTree().getSortDirection() == SWT.UP ? 1 : -1;
				return direction * (first.getShapeType().ordinal() < second.getShapeType().ordinal() ? 1 : -1);
			}
		};
	}

}
