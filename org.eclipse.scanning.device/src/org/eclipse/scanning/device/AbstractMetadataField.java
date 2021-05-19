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
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

public abstract class AbstractMetadataField extends AbstractMetadataNode implements MetadataField {

	private static final String ATTRIBUTE_NAME_UNITS = "units";

	private String units = null;

	protected AbstractMetadataField() {
		// no-arg constructor for spring initialization
	}

	protected AbstractMetadataField(String name) {
		super(name);
	}

	@Override
	public String getUnits() throws NexusException {
		return units;
	}

	@Override
	public void setUnits(String units) {
		this.units = units;
	}

	protected DataNode createDataNode(final Object value) {
		final DataNode dataNode = NexusNodeFactory.createDataNode();
		final Dataset dataset = DatasetFactory.createFromObject(value);
		dataset.setName(getName());
		dataNode.setDataset(dataset);
		return dataNode;
	}

	@Override
	public final DataNode createNode() throws NexusException {
		final DataNode dataNode = createDataNode();
		addUnitsAttribute(dataNode);

		return dataNode;
	}

	private void addUnitsAttribute(final DataNode dataNode) throws NexusException {
		final String units = getUnits();
		if (units != null) {
			final Attribute attr = TreeFactory.createAttribute(ATTRIBUTE_NAME_UNITS, units);
			dataNode.addAttribute(attr);
		}
	}

	protected abstract DataNode createDataNode() throws NexusException;

}
