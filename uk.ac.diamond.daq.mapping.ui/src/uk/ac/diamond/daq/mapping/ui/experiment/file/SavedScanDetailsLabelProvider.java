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

import java.text.DecimalFormat;
import java.util.Comparator;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import uk.ac.diamond.daq.mapping.ui.experiment.file.DescriptiveFilenameFactory.PathParameterSource;
import uk.ac.diamond.daq.mapping.ui.experiment.file.DescriptiveFilenameFactory.RegionParameterSource;

/**
 * Label provider for the Scan description column of the view which extracts its content from the pre-extension section
 * of the filename.
 *
 * @since GDA 9.13
 */
public class SavedScanDetailsLabelProvider extends LabelProvider implements IComparableStyledLabelProvider {
	private static final int SHAPE_TYPE = 0;
	private static final int SHAPE_PARAMS = 1;
	private static final int PATH_TYPE = 2;
	private static final int PATH_PARAMS = 3;
	private static final int MUTATORS = 4;

	/**
	 * Operates on the text before the .map extension provided that that this is not the only other (dot delimited)
	 * part of the filename.
	 */
	@Override
	public StyledString getStyledText(Object element) {
		StyledString result = new StyledString();
		DecimalFormat decimal = new DecimalFormat("#########.###");
		if (element instanceof String) {
			String[] tokens = splitOnDot(element);
			if (tokens.length > 2) {								// if there is a descriptor section
				String descriptor = penultimateOf(tokens);		// it will be the one before .map
				if (descriptor.startsWith("S0")) {
					result.append(PathParameterSource.getSummary(descriptor.substring(0, 2), new Object[]{}));
				} else {
					String [] dimensions = descriptor.split("\\(|\\)");
					result.append(PathParameterSource.getSummary(
							dimensions[PATH_TYPE],
							dimensions[PATH_PARAMS].split(",")[0],
							RegionParameterSource.getSummary(
									dimensions[SHAPE_TYPE],
									decimal.format(Double.parseDouble(dimensions[SHAPE_PARAMS].split(",")[0])),
									decimal.format(Double.parseDouble(dimensions[SHAPE_PARAMS].split(",")[1]))
									),
							dimensions.length > 4 ? dimensions[MUTATORS] : ""));
				}
			}
		}
		return result;
	}

	/**
	 * Compares the non-shape related part of the descriptor
	 */
	@Override
	public ViewerComparator getComparator() {
		return new ViewerComparator() {
		    @Override
			public int compare(Viewer viewer, Object element1, Object element2) {
		    	String[] first = splitOnDot(element1);
		    	String[] second = splitOnDot(element2);

		    	if (first.length < 3 || second.length < 3) {
			    	return (first.length < second.length) ? 1 : -1;
		    	}
		    	Comparator<String> c = Comparator.comparing(String::toString);
		    	int direction = ((TreeViewer)viewer).getTree().getSortDirection() == SWT.UP ? 1 : -1;
		    	return direction * c.compare(penultimateOf(first).substring(2), penultimateOf(second).substring(2));
		    }
		};
	}
}
