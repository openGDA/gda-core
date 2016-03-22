package uk.ac.diamond.tomography.reconstruction;

import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.hdf.object.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf.object.HierarchicalDataUtils;
import org.eclipse.dawnsci.hdf5.model.IHierarchicalDataModel;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import uk.ac.diamond.tomography.reconstruction.INexusFilterDescriptor.Operation;

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
	public static final boolean SELECT_WHEN_COMPARE_FAILS_DUE_TO_NUMBERFORMATEXCEPTION = true;
	private INexusFilterPreferencesCache nexusFilterPreferencesCache;
	IHierarchicalDataModel model;

	/**
	 * Zero-argument constructor for extension point instantiation
	 */
	public NexusFilter() {
		this(NexusFilterPreferencesCache.CACHE, org.eclipse.dawnsci.hdf5.model.Activator.getPlugin().getHierarchicalDataModel());
	}

	public NexusFilter(INexusFilterPreferencesCache nexusFilterPreferencesCache, IHierarchicalDataModel model) {
		this.nexusFilterPreferencesCache = nexusFilterPreferencesCache;
		this.model = model;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		INexusFilterDescriptor filter = nexusFilterPreferencesCache.getNexusFilterDescriptor();
		if (filter == null)
			return true;

		if (!(element instanceof IFile))
			return true;
		IFile file = (IFile) element;
		if (!HierarchicalDataFactory.isHDF5(file.getRawLocation().toOSString()))
			return true;

		try {
			Operation operation = filter.getNexusFilterOperation();
			String path = filter.getNexusFilterPath();
			String[] operands = filter.getNexusFilterOperands();
			switch (operation) {
			case CONTAINS:
				return hasPath(file, path);
			case DOES_NOT_CONTAIN:
				return !hasPath(file, path);
			case EQUALS:
				return compare(file, path, operands[0]) == 0;
			case NOT_EQUALS:
				return compare(file, path, operands[0]) != 0;
			case GREATER_THAN:
				return compare(file, path, operands[0]) > 0;
			case GREATER_THAN_OR_EQUAL:
				return compare(file, path, operands[0]) >= 0;
			case LESS_THAN:
				return compare(file, path, operands[0]) < 0;
			case LESS_THAN_OR_EQUAL:
				return compare(file, path, operands[0]) <= 0;
			case CLOSED_INTERVAL:
				return compare(file, path, operands[0]) >= 0 && compare(file, path, operands[1]) <= 0;
			case OPEN_INTERVAL:
				return compare(file, path, operands[0]) > 0 && compare(file, path, operands[1]) < 0;
			case LEFT_CLOSED_RIGHT_OPEN_INTERVAL:
				return compare(file, path, operands[0]) >= 0 && compare(file, path, operands[1]) < 0;
			case LEFT_OPEN_RIGHT_CLOSED_INTERVAL:
				return compare(file, path, operands[0]) > 0 && compare(file, path, operands[1]) <= 0;
			}
		} catch (NumberFormatException nfe) {
			// we can't compare the values
			return SELECT_WHEN_COMPARE_FAILS_DUE_TO_NUMBERFORMATEXCEPTION;
		}
		// unreachable if switch is complete
		return true;
	}

	/**
	 * @return <ul>
	 *         <li>the value 0 if equals</li>
	 *         <li>a value less than 0 if path's value is less than string</li>
	 *         <li>a value greater than 0 if path's value is greater than string</li>
	 *         </ul>
	 * @throws NumberFormatException
	 *             if the comparison cannot be completed
	 */
	private int compare(IFile file, String nexusFilterPath, String string) throws NumberFormatException {
		Object data = model.getFileModel(file).getPath(nexusFilterPath);
		return HierarchicalDataUtils.compareScalarToString(data, string);
	}

	private boolean hasPath(IFile file, String nexusFilterPath) {
		return model.getFileModel(file).hasPath(nexusFilterPath);
	}
}
