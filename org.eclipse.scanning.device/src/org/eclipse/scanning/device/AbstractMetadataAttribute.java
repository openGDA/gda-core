/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.api.AbstractNameable;

public abstract class AbstractMetadataAttribute extends AbstractNameable implements MetadataAttribute {

	protected AbstractMetadataAttribute() {
		// no-arg constructor for spring instantiation
	}

	protected AbstractMetadataAttribute(String name) {
		setName(name);
	}

	@Override
	public final Attribute createAttribute() throws NexusException {
		return TreeFactory.createAttribute(getName(), getValue());
	}

	public abstract Object getValue() throws NexusException;

}
