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

import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.scanning.api.INameable;

public class GroupMetadataNode<N extends NXobject> extends AbstractMetadataNode implements MetadataNode {

	private NexusBaseClass nexusBaseClass;

	/**
	 * A map of the nodes within this device (those without an explicit setter), keyed by name.
	 */
	private Map<String, MetadataNode> nodes = new HashMap<>();

	public GroupMetadataNode() {
		// no-arg constructor for spring initialization
	}

	public GroupMetadataNode(String nodeName, NexusBaseClass nexusBaseClass) {
		super(nodeName);
		setNexusBaseClass(nexusBaseClass);
	}

	public void setNexusBaseClass(NexusBaseClass nexusBaseClass) {
		this.nexusBaseClass = nexusBaseClass;
	}

	public NexusBaseClass getNexusBaseClass() {
		return nexusBaseClass;
	}

	/**
	 * Convenience method to set the nexus class as a string
	 * @param nxClass
	 */
	public void setNexusClass(String nxClass) {
		nexusBaseClass = NexusBaseClass.getBaseClassForName(nxClass);
	}

	public void addChildNode(MetadataNode childNode) {
		if (nodes.containsKey(childNode.getName()) && !nodes.get(childNode.getName()).isDefaultValue()) {
			// only a default value can be overwritten
			throw new IllegalArgumentException(MessageFormat.format("The group ''{0}'' already contains a child group with the name ''{1}''.", getName(), childNode.getName()));
		}
		nodes.put(childNode.getName(), childNode);
	}

	public void addChildNodes(List<MetadataNode> customNodes) {
		customNodes.stream().forEach(this::addChildNode);
	}

	public void setChildNodes(List<MetadataNode> customNodes) {
		this.nodes = customNodes.stream().collect(toMap(INameable::getName, Function.identity()));
	}

	@Override
	public N createNode() throws NexusException {
		if (nexusBaseClass == null) {
			throw new NexusException("The nexus base class is not set for the device " + getName());
		}

		@SuppressWarnings("unchecked")
		final N nexusObject = (N) NexusNodeFactory.createNXobjectForClass(nexusBaseClass);

		appendNodes(nexusObject);

		return nexusObject;
	}

	/**
	 * Appends the configured nodes to an existing nexus object. Use this method
	 * instead of {@link #createNode()} if the group node already exists.
	 *
	 * @param nexusObject
	 * @throws NexusException
	 */
	public void appendNodes(N nexusObject) throws NexusException {
		for (MetadataNode node : nodes.values()) {
			nexusObject.addNode(node.getName(), node.createNode());
		}
	}

	public MetadataNode getChildNode(String nodeName) {
		return nodes.get(nodeName);
	}

	public void removeChildNode(String nodeName) {
		nodes.remove(nodeName);
	}

	public void clearChildNodes() {
		nodes.clear();
	}

	public boolean hasChildNode() {
		return !nodes.isEmpty();
	}

}
