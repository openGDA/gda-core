/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;

import gda.device.Detector;
import gda.device.Scannable;

/**
 * A nexus device that wraps a GDA {@link Scannable}, possibly a {@link Detector}, for which we
 * write one or more scalar values at each point in the scan.
 * These may correspond to the {@link Scannable#getInputNames()} and {@link Scannable#getExtraNames()}
 * for the device. Otherwise, there may be a single field, e.g. 'data.
 *
 * @param <N> the type of nexus object to be created
 */
public interface IGDAScannableNexusDevice<N extends NXobject> extends INexusDevice<N> {

	/**
	 * The names of the fields for this scannable. The resulting array may be empty, never <code>null</code>.
	 * @return field names
	 */
	public String[] getFieldNames();

	/**
	 * Returns the {@link DataNode} for the given field name, or <code>null</code> if there is not such field.
	 *
	 * @param fieldName
	 * @return the data node for the given field name
	 */
	public DataNode getFieldDataNode(String fieldName);

}
