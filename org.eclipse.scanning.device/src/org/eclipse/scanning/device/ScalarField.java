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

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

/**
 * A field written to the nexus file as a scalar value.
 */
public class ScalarField extends AbstractMetadataNode {

	public ScalarField() {
		// no-arg constructor for spring initialization
	}

	public ScalarField(String fieldName, Object value) {
		super(fieldName);
		setValue(value);
	}

	private Object value;

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public DataNode createNode() throws NexusException {
		final DataNode dataNode = NexusNodeFactory.createDataNode();
		final Dataset dataset = DatasetFactory.createFromObject(getValue());
		dataset.setName(getName());
		dataNode.setDataset(dataset);
		return dataNode;
	}

}
