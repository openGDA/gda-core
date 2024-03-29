/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.data.nexus.tree;

import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;

/**
 * Class to represent a Nexus Group - a child is either a DATASET or an ATTRIBUTE
 */
public class NexusTreeNode implements INexusTree, Serializable {

	/**
	 * The actual data read from the element
	 */
	public final NexusGroupData groupData;
	private List<INexusTree> childNodes = new ArrayList<>();

	/**
	 * The name of the element read from the Nexus file
	 */
	public final String name;
	/**
	 * The class of the element
	 */
	public final String nxClass;

	/**
	 * The parent of the current node - null if this is the top node
	 */
	private INexusTree parentNode;

	private boolean isPointDependent = false;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getNxClass() {
		return nxClass;
	}

	/**
	 * @param parentNode
	 */
	@Override
	public void setParentNode(INexusTree parentNode) {
		this.parentNode = parentNode;
	}

	/**
	 * @return The node of which the current object is a child.
	 */
	@Override
	public INexusTree getParentNode() {
		return parentNode;
	}

	/**
	 * @param name
	 * @param nxClass
	 * @param parentNode
	 * @param groupData
	 */
	public NexusTreeNode(String name, String nxClass, INexusTree parentNode,
			NexusGroupData groupData) {
		this.name = name;
		this.nxClass = nxClass;
		this.parentNode = parentNode;
		this.groupData = groupData;
	}


	/**
	 * @param name
	 * @param nxClass
	 * @param parentNode
	 */
	public NexusTreeNode(String name, String nxClass, INexusTree parentNode) {
		this(name, nxClass, parentNode, null);
	}

	@Override
	public String toString() {
		return toText("", ":", "/", "|", false);
	}

	/**
	 * @param newlineAfterEach
	 *            - if true the output is interspersed with newlines to make it more humanly readable
	 * @param dataAsString
	 *            - - if true data array are written as string. NX_CHAR data is always shown as a string
	 * @return A string that represents the tree in xml
	 */
	@Override
	public String toXML(boolean newlineAfterEach, boolean dataAsString) {
		return toXMLbegin(newlineAfterEach, dataAsString).toString()
				+ toXMLend(newlineAfterEach, dataAsString).toString();
	}

	@Override
	public StringBuilder toXMLbegin(boolean newlineAfterEach, boolean dataAsString) {
		final StringBuilder msg = new StringBuilder("<");
		msg.append((parentNode == null) ? "top" : nxClass);
		msg.append(" @name=" + name);

		msg.append(">");
		if (newlineAfterEach) {
			msg.append("\n");
		}

		if (groupData != null) {
			msg.append("<dimensions>");
			for (int i : groupData.getDimensions()) {
				msg.append("<dimension>" + i + "</dimension>");
			}
			msg.append("</dimensions>");
			if (newlineAfterEach) {
				msg.append("\n");
			}
			msg.append("<type>");
			msg.append(groupData.getType());
			msg.append("</type>");
			if (newlineAfterEach) {
				msg.append("\n");
			}
			msg.append(groupData.dataToTxt(newlineAfterEach, dataAsString, true));

		}

		for (INexusTree ntn : childNodes) {
			msg.append(ntn.toXMLbegin(newlineAfterEach, dataAsString));
			msg.append(ntn.toXMLend(newlineAfterEach, dataAsString));
		}
		return msg;
	}

	@Override
	public StringBuilder toXMLend(boolean newlineAfterEach, boolean dataAsString) {
		final StringBuilder msg = new StringBuilder("</");
		msg.append(nxClass.isEmpty() ? "top" : nxClass);
		msg.append(">");
		if (newlineAfterEach) {
			msg.append("\n");
		}
		return msg;
	}

	@Override
	public NexusGroupData getData() {
		return groupData;
	}

	@Override
	public int getNumberOfChildNodes() {
		return childNodes.size();
	}

	@Override
	public void addChildNode(INexusTree e) {
		childNodes.add(e);
		e.setParentNode(this);
	}

	@Override
	public void removeChildNode(INexusTree e) {
		childNodes.remove(e);
		e.setParentNode(null);
	}

	@Override
	public INexusTree getChildNode(int index) {
		return childNodes.get(index);
	}

	public int getChildCount() {
		return childNodes.size();
	}

	@Override
	public void setPriority() {
		this.parentNode.setPrioritised(this);
	}

	@Override
	public void setPrioritised(INexusTree child) {
		if (this.childNodes.contains(child)) {
			childNodes.sort(priorityComparator(child));
		}
	}

	protected Comparator<INexusTree> priorityComparator(INexusTree priority) {
		return Comparator.comparing(x -> !x.equals(priority));
	}

	@Override
	public String toText(String prefix, String keyValueSep, String dataItemSep, String nodeSep) {
		return toText(prefix, keyValueSep, dataItemSep, nodeSep, true);
	}

	@Override
	public String toText(String prefix, String keyValueSep, String dataItemSep, String nodeSep, boolean includeData) {
		final StringBuilder msg = new StringBuilder(prefix + nodeSep + nxClass + keyValueSep + name);
		if (groupData != null) {
			msg.append(dataItemSep + "dimensions");
			for (int i : groupData.getDimensions()) {
				msg.append(keyValueSep + i);
			}
			msg.append(dataItemSep + "type");
			msg.append(keyValueSep + groupData.getType());
			if (includeData) {
				msg.append(dataItemSep + "data" + keyValueSep + groupData.dataToTxt(false, true, false));
			}
		}

		prefix = msg.toString();
		msg.append("\n");
		for (INexusTree ntn : childNodes) {
				msg.append(ntn.toText(prefix, keyValueSep, dataItemSep, nodeSep, includeData));
		}
		return msg.toString();
	}

	@Override
	public Iterator<INexusTree> iterator() {
		return childNodes.iterator();
	}

	/**
	 * Recursively find node depth-first
	 *
	 * @param nodeName
	 * @return node with given name
	 */
	public NexusTreeNode findNode(String nodeName) {
		NexusTreeNode node = this;
		if (!node.getName().equals(nodeName)) {
			for (INexusTree c : node) {
				NexusTreeNode n = ((NexusTreeNode) c).findNode(nodeName);
				if (n != null)
					return n;
			}
		} else
			return node;
		return null;
	}

	/**
	 * @return path of node
	 */
	@Override
	public String getNodePath() {
		// build reference to current node
		final StringBuilder nodePath = new StringBuilder(getName());
		INexusTree pnode = parentNode;
		while (pnode != null) {
			nodePath.insert(0, pnode.getName() + "/");
			pnode = pnode.getParentNode();
		}

		nodePath.insert(0, "/");
		return nodePath.toString();
	}

	@Override
	public String getNodePathWithClasses(){
		// build reference to current node
		INexusTree node=this;
		final StringBuilder totalPath = new StringBuilder("");
		while (node != null) {
			final StringBuilder nodePath = new StringBuilder(node.getName());
			nodePath.append("/");
			nodePath.append(node.getNxClass());
			nodePath.append("/");
			totalPath.insert(0, nodePath);
			node = node.getParentNode();
		}
		return totalPath.toString();
	}

	/**
	 * @param path
	 * @return node with given path
	 */
	@Override
	public INexusTree getNode(String path){
		final String[] pathSegments = path.split("/",2);

		final INexusTree child = getChildNode(pathSegments[0]);
		return child == null || pathSegments.length == 1 ? child : child.getNode(pathSegments[1]);
	}

	/**
	 * Get an attribute of current node
	 * @param name
	 * @return an attribute with given name or null if not found
	 */
	@Override
	public Serializable getAttribute(String name) {
		return childNodes.stream()
				.filter(node -> node.getNxClass().equals(NexusExtractor.AttrClassName))
				.filter(node -> node.getName().equals(name))
				.findFirst().orElse(null);
	}

	/**
	 * Get all attributes of current node, never <code>null</code>.
	 * @return a HashMap of attributes
	 */
	@Override
	public Map<String,Serializable> getAttributes() {
		return childNodes.stream()
				.filter(node -> node.getNxClass().equals(NexusExtractor.AttrClassName))
				.collect(toMap(INexusTree::getName, node -> node.getData().getFirstValue()));
	}


	@Override
	public void sort(Comparator<INexusTree> comparator) {
		Collections.sort(childNodes, comparator);
		for (INexusTree tree: childNodes) {
			tree.sort(comparator);
		}
	}

	@Override
	public INexusTree getChildNode(String name, String className) {
		final INexusTree childNode = getChildNode(name);
		return childNode != null && childNode.getNxClass().equals(className) ? childNode : null;
	}

	private INexusTree getChildNode(String name) {
		return childNodes.stream()
				.filter(node -> node.getName().equals(name))
				.findFirst().orElse(null);
	}

	public void setIsPointDependent(boolean val){
		isPointDependent = val;
	}

	@Override
	public boolean isPointDependent() {
		return isPointDependent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((childNodes == null) ? 0 : childNodes.hashCode());
		result = prime * result + ((groupData == null) ? 0 : groupData.hashCode());
		result = prime * result + (isPointDependent ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nxClass == null) ? 0 : nxClass.hashCode());
		result = prime * result + ((parentNode == null) ? 0 : parentNode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NexusTreeNode other = (NexusTreeNode) obj;
		if (childNodes == null) {
			if (other.childNodes != null)
				return false;
		} else if (!childNodes.equals(other.childNodes))
			return false;
		if (groupData == null) {
			if (other.groupData != null)
				return false;
		} else if (!groupData.equals(other.groupData))
			return false;
		if (isPointDependent != other.isPointDependent)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nxClass == null) {
			if (other.nxClass != null)
				return false;
		} else if (!nxClass.equals(other.nxClass))
			return false;
		if (parentNode == null) {
			if (other.parentNode != null)
				return false;
		} else if (!parentNode.equals(other.parentNode))
			return false;
		return true;
	}

}
