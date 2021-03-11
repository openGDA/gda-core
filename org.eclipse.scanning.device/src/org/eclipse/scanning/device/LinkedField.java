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
import org.eclipse.dawnsci.analysis.api.tree.SymbolicNode;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;

/**
 * A {@link MetadataField} that adds a link to a {@link DataNode} at given path within the
 * nexus file.
 */
public class LinkedField extends AbstractMetadataField {

	private String linkPath = null;

	public LinkedField() {
		// no-arg constructor for spring initialization
	}

	public LinkedField(String name, String linkPath) {
		super(name);
		this.linkPath = linkPath;
	}

	public String getLinkPath() {
		return linkPath;
	}

	public void setLinkPath(String linkPath) {
		if (!linkPath.startsWith(Tree.ROOT)) {
			throw new IllegalArgumentException("Link path must be an absolute path.");
		}
		this.linkPath = linkPath;
	}

	@Override
	public void writeField(NXobject nexusObject) throws NexusException {
		final SymbolicNode symbolicNode = NexusNodeFactory.createSymbolicNode(null, linkPath);
		nexusObject.addSymbolicNode(getName(), symbolicNode);
	}

}
