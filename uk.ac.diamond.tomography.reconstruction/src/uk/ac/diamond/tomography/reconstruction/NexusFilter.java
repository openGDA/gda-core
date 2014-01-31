package uk.ac.diamond.tomography.reconstruction;

import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.model.IHierarchicalDataModel;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

public class NexusFilter extends ViewerFilter {

	private String filterPath;
	private boolean reverse;

	public NexusFilter() {
		this.filterPath = "";
	}

	public NexusFilter(String filterPath, boolean reverse) {
		this.filterPath = filterPath;
		this.reverse = reverse;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (filterPath != null && !"".equals(filterPath)) {
			if (element instanceof IFile) {
				IFile file = (IFile) element;
				if (HierarchicalDataFactory.isHDF5(file.getRawLocation().toOSString())) {
					boolean hasValue = hasValue(file);
					// Object data = getValueFromFile((IFile) element);
					// if (data != null) {
					// PLACEHOLDER to do condition
					// nexusfiltercondition
					// nfc.op(data)
					// get value
					return reverse ? hasValue : !hasValue;
				}
			}
		}
		return true;
	}

	private boolean hasValue(IFile file) {
		IHierarchicalDataModel model = org.dawb.hdf5.Activator.getPlugin().getHierarchicalDataModel();
		return model.getFileModel(file).hasPath(filterPath);
	}
}
