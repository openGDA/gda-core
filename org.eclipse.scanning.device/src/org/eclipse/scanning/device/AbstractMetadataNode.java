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

import static java.util.stream.Collectors.toMap;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.api.AbstractNameable;
import org.eclipse.scanning.api.INameable;

/**
 * A node that can be part of a metadata device.
 */
public abstract class AbstractMetadataNode extends AbstractNameable implements MetadataNode {

	private Map<String, MetadataAttribute> attributes = new HashMap<>();

	protected AbstractMetadataNode() {
		// no-arg constructor for spring initialization
	}

	protected AbstractMetadataNode(String nodeName) {
		setName(nodeName);
	}

	@Override
	public final Node createNode() throws NexusException {
		final Node node = doCreateNode();
		createAttributes(node);
		return node;
	}

	protected abstract Node doCreateNode() throws NexusException;

	protected void createAttributes(Node node) throws NexusException {
		for (MetadataAttribute attr : attributes.values()) {
			node.addAttribute(attr.createAttribute());
		}
	}

	public void addAttribute(MetadataAttribute attr) {
		if (attributes.containsKey(attr.getName())) {
			throw new IllegalArgumentException(MessageFormat.format("The group ''{0}'' already contains an attribute with the name ''{1}''.",
					getName(), attr.getName()));
		}
		attributes.put(attr.getName(), attr);
	}

	public void addAttributes(List<MetadataAttribute> attributes) {
		attributes.stream().forEach(this::addAttribute);
	}

	public void removeAttribute(String attrName) {
		attributes.remove(attrName);
	}

	public void clearAttributes() {
		attributes.clear();
	}

	public void setAttributes(List<MetadataAttribute> attributes) {
		this.attributes = attributes.stream()
				.collect(toMap(INameable::getName, Function.identity()));
	}

	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}

}
