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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.SymbolicNode;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;

/**
 * A {@link MetadataNode} that adds a link to a {@link DataNode} at given path within the same
 * nexus file, or to an node in an external file.
 */
public class LinkedField extends AbstractMetadataNode {

	private String externalFilePath = null;

	private String linkPath = null;

	public LinkedField() {
		// no-arg constructor for spring initialization
	}

	public LinkedField(String fieldName, String linkPath) {
		this(fieldName, null, linkPath);
	}

	public LinkedField(String fieldName, String externalFilePath, String linkPath) {
		super(fieldName);
		this.externalFilePath = externalFilePath;
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

	public String getExternalFilePath() {
		return externalFilePath;
	}

	public void setExternalFilePath(String externalFilePath) {
		this.externalFilePath = externalFilePath;
	}

	@Override
	public SymbolicNode createNode() throws NexusException {
		final Optional<URI> uri = getExternalFileUri();
		return NexusNodeFactory.createSymbolicNode(uri.orElse(null), linkPath);
	}

	private Optional<URI> getExternalFileUri() throws NexusException {
		if (externalFilePath != null) {
			try {
				return Optional.of(new URI(externalFilePath));
			} catch (URISyntaxException e) {
				throw new NexusException("external filename cannot be converted to a URI", e);
			}
		}

		return Optional.empty();
	}

}
