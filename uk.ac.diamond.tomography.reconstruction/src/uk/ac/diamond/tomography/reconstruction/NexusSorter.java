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

package uk.ac.diamond.tomography.reconstruction;

import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.hdf.object.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf.object.HierarchicalDataUtils;
import org.eclipse.dawnsci.hdf5.model.IHierarchicalDataModel;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Sorts nexus files
 */
public class NexusSorter extends ViewerSorter {
	private INexusSorterPreferencesCache nexusSorterPreferencesCache;
	IHierarchicalDataModel model;

	/**
	 * Zero-argument constructor for extension point instantiation
	 */
	public NexusSorter() {
		this(NexusSorterPreferencesCache.CACHE, org.eclipse.dawnsci.hdf5.model.Activator.getPlugin().getHierarchicalDataModel());
	}

	public NexusSorter(INexusSorterPreferencesCache nexusSorterPreferencesCache, IHierarchicalDataModel model) {
		this.nexusSorterPreferencesCache = nexusSorterPreferencesCache;
		this.model = model;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int cat1 = category(e1);
		int cat2 = category(e2);

		if (cat1 != cat2) {
			return cat1 - cat2;
		} else if (cat1 != 1) {
			// this is the case where HierarchicalDataFactory.isHDF5 returned false
			return super.compare(viewer, e1, e2);
		}

		String nexusSortPath = nexusSorterPreferencesCache.getNexusSortPath();
		if (nexusSortPath != null && !"".equals(nexusSortPath)) {
			if ((e1 instanceof IFile) && (e2 instanceof IFile)) {
				Object data1 = getValueFromFile((IFile) e1);
				Object data2 = getValueFromFile((IFile) e2);

				if (data1 == null) {
					if (data2 == null) {
						return super.compare(viewer, e1, e2);
					}
					return 1;
				}
				if (data2 == null) {
					return -1;
				}
				int compare = HierarchicalDataUtils.compareScalars(data1, data2);
				if (compare == 0) {
					// stabilise the sort by using the label as second order sort key
					return super.compare(viewer, e1, e2);
				}
				return compare;

			}
		}
		return super.compare(viewer, e1, e2);
	}

	private Object getValueFromFile(IFile file) {
		String nexusSortPath = nexusSorterPreferencesCache.getNexusSortPath();
		return model.getFileModel(file).getPath(nexusSortPath);
	}

	@Override
	public int category(Object element) {
		if (element instanceof IFile) {
			IFile file = (IFile) element;
			if (HierarchicalDataFactory.isHDF5(file.getRawLocation().toOSString())) {
				return 1;
			}
		}
		return 2;
	}
}
