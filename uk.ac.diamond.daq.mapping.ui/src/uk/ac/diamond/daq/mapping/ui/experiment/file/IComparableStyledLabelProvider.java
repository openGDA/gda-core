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

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;

import gda.rcp.views.ComparableStyledLabelProvider;


/**
 * Adds comparator retrival to the {@link IStyledLabelProvider} interface to support column
 * sorting in e.g. {@link TreeViewer} controls.
 * This implementation is specific for {@link SavedScanMetaData} elements
 *
 * @since GDA 9.13
 */
public interface IComparableStyledLabelProvider extends ComparableStyledLabelProvider {

	default String[] splitOnDot(Object element) {
		return element.toString().split("\\.");
	}

	default String penultimateOf(String[] array) {
		return array[array.length - 2];
	}
}