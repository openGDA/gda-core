/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.eclipse.scanning.device;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NexusException;

/**
 * A {@link MetadataNode} that knows how to create a {@link DataNode}. If the units property is set
 * (see {@link #setUnits(String)}), a {@code units} {@link Attribute} will be added to the trade
 */
public interface MetadataField extends MetadataNode {

	public String getUnits() throws NexusException;

	/**
	 * The value to set the {@code units} {@link Attribute} to.
	 * @param units
	 */
	public void setUnits(String units);

	/**
	 * Overrides {@link MetadataNode#createNode()} to always return a {@link DataNode}
	 */
	@Override
	public DataNode createNode() throws NexusException;

}
