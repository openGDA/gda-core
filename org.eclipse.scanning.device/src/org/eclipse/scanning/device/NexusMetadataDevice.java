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

import java.util.List;

import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;

/**
 * A generic metadata device that adds creates a nexus object depending on the {@link MetadataNode}s
 * that it is configured with.
 *
 * <p>Implementation note: This class adds no additional behaviour to {@link AbstractNexusMetadataDevice}.
 * It adds the method {@link #setChildNodes(List)} as a synonym for {@link #setCustomNodes(List)} as that
 * is the only kind of field this class has - there are no predetermined fields such as
 * {@link SourceNexusDevice#setCurrentScannableName(String)}.
 *
 * @param <N> the type of nexus object created by this device
 */
public class NexusMetadataDevice<N extends NXobject> extends AbstractNexusMetadataDevice<N> {

	public NexusMetadataDevice() {
		// no-arg constructor for spring initialization
	}

	public NexusMetadataDevice(NexusBaseClass nexusBaseClass) {
		super(nexusBaseClass);
	}

	public void setChildNodes(List<MetadataNode> childNodes) {
		setCustomNodes(childNodes);
	}

	@Override
	public void setNexusBaseClass(NexusBaseClass nexusClass) {
		// override to set visibility to public
		super.setNexusBaseClass(nexusClass);
	}

	@Override
	public void setNexusClass(String nxClass) {
		// override to set visibility to public
		super.setNexusClass(nxClass);
	}

}
