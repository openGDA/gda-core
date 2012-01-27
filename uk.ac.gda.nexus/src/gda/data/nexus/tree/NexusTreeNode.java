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

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a Nexus Group - a child is either a DATASET or an ATTRIBUTE
 */
public class NexusTreeNode implements INexusTree, Serializable {
	
	private static final long serialVersionUID = 1L;

	transient private static final Logger logger = LoggerFactory.getLogger(NexusTreeNode.class);	
	/**
	 * The actual data read from the element
	 */
	public final NexusGroupData groupData;
	private List<INexusTree> childNodes = new Vector<INexusTree>();

	/**
	 * @return Comparator that compares items by comparing the result of getName
	 * 
	 */
	public static Comparator<INexusTree> getNameComparator(){
		return nameComparator;
	}
	
	private static Comparator<INexusTree> nameComparator = new Comparator<INexusTree>() {
		@Override
		public int compare(INexusTree o1, INexusTree o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

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

	protected String dataToTxt(boolean newlineAfterEach, boolean dataAsString, boolean wrap) {
		StringBuffer msg = new StringBuffer();
		if (groupData.getBuffer() != null) {
			if (groupData.type == NexusFile.NX_CHAR && groupData.getBuffer() instanceof byte[]) {
				if (wrap)
					msg.append("<value>");
				String s = new String((byte[]) groupData.getBuffer());
				msg.append(s);
				if (wrap)
					msg.append("</value>");
				if (newlineAfterEach) {
					msg.append("\n");
				}
			} else {
				if (dataAsString) {
					if (wrap)
						msg.append("<value>");
					if (groupData.getBuffer() instanceof double[]) {
						double[] ddata = (double[]) (groupData.getBuffer());
						for (double d : ddata) {
							msg.append(Double.toString(d) + ",");
						}
					} else if (groupData.getBuffer() instanceof int[]) {
						int[] ddata = (int[]) (groupData.getBuffer());
						for (int d : ddata) {
							msg.append(Integer.toString(d) + ",");
						}
					} else if (groupData.getBuffer() instanceof byte[]) {
						byte[] ddata = (byte[]) (groupData.getBuffer());
						for (byte d : ddata) {
							msg.append(Byte.toString(d) + ",");
						}
					} else if (groupData.getBuffer() instanceof float[]) {
						float[] ddata = (float[]) (groupData.getBuffer());
						for (float d : ddata) {
							msg.append(Float.toString(d) + ",");
						}
					} else if (groupData.getBuffer() instanceof long[]) {
						long[] ddata = (long[]) (groupData.getBuffer());
						for (long d : ddata) {
							msg.append(Long.toString(d) + ",");
						}
					} else {
						msg.append(groupData.getBuffer().toString());
					}
					if (wrap)
						msg.append("</value>");
					if (newlineAfterEach) {
						msg.append("\n");
					}
				} else {
					msg.append("<values>");
					if (newlineAfterEach) {
						msg.append("\n");
					}
					if (groupData.getBuffer() instanceof double[]) {
						double[] ddata = (double[]) (groupData.getBuffer());
						for (double d : ddata) {
							msg.append("<value>");
							msg.append(Double.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else if (groupData.getBuffer() instanceof int[]) {
						int[] ddata = (int[]) (groupData.getBuffer());
						for (int d : ddata) {
							msg.append("<value>");
							msg.append(Integer.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else if (groupData.getBuffer() instanceof byte[]) {
						byte[] ddata = (byte[]) (groupData.getBuffer());
						for (byte d : ddata) {
							msg.append("<value>");
							msg.append(Byte.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else if (groupData.getBuffer() instanceof float[]) {
						float[] ddata = (float[]) (groupData.getBuffer());
						for (float d : ddata) {
							msg.append("<value>");
							msg.append(Float.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else if (groupData.getBuffer() instanceof long[]) {
						long[] ddata = (long[]) (groupData.getBuffer());
						for (long d : ddata) {
							msg.append("<value>");
							msg.append(Long.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else {
						msg.append(groupData.getBuffer().toString());
					}
					msg.append("</values>");
					if (newlineAfterEach) {
						msg.append("\n");
					}
				}
			}
		}
		return msg.toString();
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
	public StringBuffer toXMLbegin(boolean newlineAfterEach, boolean dataAsString) {
		StringBuffer msg = new StringBuffer("<");
		msg.append(nxClass.isEmpty() ? "top" : nxClass);
		msg.append(" @name=" + name);

		msg.append(">");
		if (newlineAfterEach) {
			msg.append("\n");
		}

		if (groupData != null) {
			msg.append("<dimensions>");
			for (int i : groupData.dimensions) {
				msg.append("<dimension>" + i + "</dimension>");
			}
			msg.append("</dimensions>");
			if (newlineAfterEach) {
				msg.append("\n");
			}
			msg.append("<type>");
			switch (groupData.type) {
			case NexusFile.NX_CHAR:
				msg.append("NX_CHAR");
				break;
			case NexusFile.NX_FLOAT64:
				msg.append("NX_FLOAT64");
				break;
			default:
				msg.append(groupData.type);
				break;
			}
			msg.append("</type>");
			if (newlineAfterEach) {
				msg.append("\n");
			}
			msg.append(dataToTxt(newlineAfterEach, dataAsString, true));

		}
		
		for (INexusTree ntn : childNodes) {
			msg.append(ntn.toXMLbegin(newlineAfterEach, dataAsString));
			msg.append(ntn.toXMLend(newlineAfterEach, dataAsString));
		}
		return msg;
	}

	@Override
	public StringBuffer toXMLend(boolean newlineAfterEach, boolean dataAsString) {
		StringBuffer msg = new StringBuffer("</");
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
		sort(nameComparator);
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
	public String toText(String prefix, String keyValueSep, String dataItemSep, String nodeSep) {
		return toText(prefix, keyValueSep, dataItemSep, nodeSep, true);
	}
	
	@Override
	public String toText(String prefix, String keyValueSep, String dataItemSep, String nodeSep, boolean includeData) {
		StringBuffer msg = new StringBuffer(prefix + nodeSep + nxClass + keyValueSep + name);
		if (groupData != null) {
			msg.append(dataItemSep + "dimensions");
			for (int i : groupData.dimensions) {
				msg.append(keyValueSep + i);
			}
			msg.append(dataItemSep + "type");
			switch (groupData.type) {
			case NexusFile.NX_CHAR:
				msg.append(keyValueSep + "NX_CHAR");
				break;
			case NexusFile.NX_FLOAT64:
				msg.append(keyValueSep + "NX_FLOAT64");
				break;
			default:
				msg.append(". type - " + groupData.type);
				break;
			}
			if (includeData) {
				msg.append(dataItemSep + "data" + keyValueSep + dataToTxt(false, true, false));
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
		String nodePath = getName();
		INexusTree pnode = parentNode;
		while (pnode != null) {
			nodePath = pnode.getName() + "/" + nodePath;
			pnode = pnode.getParentNode();
		}

		return "/" + nodePath;
	}

	@Override
	public String getNodePathWithClasses(){
		// build reference to current node
		INexusTree node=this;
		StringBuffer totalPath = new StringBuffer("");
		while (node != null) {
			StringBuffer nodePath = new StringBuffer(node.getName());
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
		String [] nodeIds = path.split("/",2);
		for(INexusTree child : this){
			if( child.getName().equals(nodeIds[0])){
				return ( nodeIds.length==1) ? child :child.getNode(nodeIds[1]);
			}
		}
		return null;
	}
	
	/**
	 * Get an attribute of current node
	 * @param name
	 * @return an attribute with given name or null if not found
	 */
	@Override
	public Serializable getAttribute(String name) {
		for (int j = 0; j < getNumberOfChildNodes(); j++) {
			NexusTreeNode c = (NexusTreeNode) getChildNode(j);
			if (c.getNxClass().equals(NexusExtractor.AttrClassName)) {
				if (c.getName().equals(name)) {
					return c.getData().getFirstValue();
				}
			}
		}
		return null;
	}

	/**
	 * Get all attributes of current node
	 * @return a HashMap of attributes
	 */
	@Override
	public HashMap<String,Serializable> getAttributes() {
		HashMap<String,Serializable> attributes = null;
		for (int j = 0; j < getNumberOfChildNodes(); j++) {
			NexusTreeNode c = (NexusTreeNode) getChildNode(j);
			if (c.getNxClass().equals(NexusExtractor.AttrClassName)) {
				if (attributes == null)
					attributes = new HashMap<String, Serializable>();
				attributes.put(c.getName(), c.getData().getFirstValue());
			}
		}
		return attributes;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj,false);
	}

	private boolean childNodesEqual(List<INexusTree> otherChildNodes, boolean reportFalse){
		int ilen = childNodes.size();
		int iotherlen= otherChildNodes.size();
		if(ilen != iotherlen){
			if(reportFalse)
				logger.info("childNode lengths differ");
			return false;
		}
		for(int i=0; i< ilen; i++){
			INexusTree tree = childNodes.get(i);
			INexusTree other = otherChildNodes.get(i);
			if( !tree.equals(other, reportFalse))
				return false;
		}
		return true;
	}

	/**
	 * @param obj
	 * @param reportFalse
	 * @return true is equals
	 */
	@Override
	public boolean equals(Object obj, boolean reportFalse) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NexusTreeNode other = (NexusTreeNode) obj;
		if (childNodes == null) {
			if (other.childNodes != null) {
				return false;
			}
		} else if (!childNodesEqual(other.childNodes, reportFalse)) {
			if(reportFalse)
				logger.info("childNodes differ - "+ name + " " + nxClass);
			return false;
		}
		if (groupData == null) {
			if (other.groupData != null) {
				return false;
			}
		} else if (!groupData.equals(other.groupData, reportFalse)) {
			if(reportFalse)
				logger.info(this.getNodePathWithClasses() + " groupData differ - "+ name + " " + nxClass);
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			if(reportFalse)
				logger.info("name differ - "+ name + " " + nxClass);
			return false;
		}
		if (nxClass == null) {
			if (other.nxClass != null) {
				return false;
			}
		} else if (!nxClass.equals(other.nxClass)) {
			if(reportFalse)
				logger.info("nxClass differ - "+ name + " " + nxClass);
			return false;
		}
		return true;
	}

	@Override
	public void sort(Comparator<INexusTree> comparator) {
		Collections.sort(childNodes, comparator);
		for(INexusTree tree: childNodes){
			tree.sort(comparator);
		}
	}

	@Override
	public INexusTree getChildNode(String name, String className) {
		for(INexusTree tree: childNodes){
			if( tree.getName().equals(name) && tree.getNxClass().equals(className)){
				return tree;
			}
		}
		return null;
	}

	boolean isPointDependent=false;
	
	public void setIsPointDependent(boolean val){
		isPointDependent = val;
	}

	@Override
	public boolean isPointDependent() {
		return isPointDependent;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
