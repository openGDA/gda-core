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

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.scanning.api.INameable;

/**
 * An instance of this class creates a nexus object (some sub-interface of {@link NXobject}),
 * with {@link DataNode}s and child {@link GroupNode}s configured according to the child
 * {@link MetadataNode}s that this object is configured with.
 *
 * @param <N> type of nexus object produces by this node
 */
public class GroupMetadataNode<N extends NXobject> extends AbstractMetadataNode implements MetadataNode {

	/**
	 * The type of nexus object to create for this node.
	 */
	private NexusBaseClass nexusBaseClass;

	/**
	 * A map of the nodes within this group keyed by name.
	 */
	private Map<String, MetadataNode> childNodes = new HashMap<>();

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

	/**
	 * Adds the given {@link MetadataNode} to this object.
	 * @param childNode
	 */
	public void addChildNode(MetadataNode childNode) {
		if (childNodes.containsKey(childNode.getName()) && !childNodes.get(childNode.getName()).isDefaultValue()) {
			// only a default value can be overwritten
			throw new IllegalArgumentException(MessageFormat.format("The group ''{0}'' already contains a child group with the name ''{1}''.", getName(), childNode.getName()));
		}
		setChildNode(childNode);
	}

	/**
	 * Adds all the given {@link MetadataNode} to this object.
	 * @param childNodes
	 */
	public void addChildNodes(List<MetadataNode> childNodes) {
		childNodes.stream().forEach(this::addChildNode);
	}

	/**
	 * Sets the child {@link MetadataNode}s of this object to those given. Any existing
	 * child nodes are removed.
	 * @param childNodes nodes to add
	 */
	public void setChildNodes(List<MetadataNode> childNodes) {
		this.childNodes = childNodes.stream().collect(toMap(INameable::getName, Function.identity()));
	}

	/**
	 * Adds given {@link MetadataNode} to this object, overwriting any existing node with the same name.
	 * @param childNode child node to add
	 */
	public void setChildNode(MetadataNode childNode) {
		childNodes.put(childNode.getName(), childNode);
	}

	/**
	 * Returns the child {@link MetadataNode} with the given name.
	 * @param nodeName name of child node
	 * @return child node with given name
	 */
	public MetadataNode getChildNode(String nodeName) {
		return childNodes.get(nodeName);
	}

	/**
	 * Removes the child {@link MetadataNode} with the given name
	 * @param nodeName
	 */
	public void removeChildNode(String nodeName) {
		childNodes.remove(nodeName);
	}

	/**
	 * Removes all child nodes.
	 */
	public void clearChildNodes() {
		childNodes.clear();
	}

	/**
	 * Returns whether this object has any child {@link MetadataNode}s.
	 *
	 * @return <code>true</code>if this object has child nodes, <code>false</code> otherwise
	 */
	public boolean hasChildNodes() {
		return !childNodes.isEmpty();
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
		for (MetadataNode node : childNodes.values()) {
			nexusObject.addNode(node.getName(), node.createNode());
		}
	}

}
