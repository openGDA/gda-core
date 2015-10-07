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
import gda.data.nexus.extractor.NexusGroupData;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

public interface INexusTree extends Iterable<INexusTree>, Serializable  {

	/**
	 * @return List of child nodes
	 */
	public int getNumberOfChildNodes();

	/**
	 * Add a tree as a child node of the current item
	 * @param e
	 */
	public void addChildNode(INexusTree e);

	/**
	 *  Remove tree from collection of child nodes of  the current item
	 * @param e
	 */
	public void removeChildNode(INexusTree e);

	/**
	 * Get child tree node at position index
	 * @param index - between 0 and getNumberOfChildNodes-1
	 * @return child node of the current item
	 */
	public INexusTree getChildNode(int index);

	/**
	 * Get child tree node with given name and class
	 * @param name
	 * @param className
	 * @return child node of the current item
	 */
	public INexusTree getChildNode(String name, String className);

	/**
	 * @return The name of the element as stored as a group name in Nexus
	 */
	public String getName();

	/**
	 * @return The class of the element as stored as a group class in Nexus
	 */
	public String getNxClass();

	/**
	 * @return Nexus data associated with the node - null if not present
	 */
	public NexusGroupData getData();
	/**
	 * @param parentNode
	 */
	public void setParentNode(INexusTree parentNode);

	/**
	 * @return The node of which the current object is a child.
	 */
	public INexusTree getParentNode();

	/**
	 * @param prefix Text that begins each line e.g. ""
	 * @param keyValueSep Text between key and value e.g. ":"
	 * @param dataItemSep  Text between key and data e.g. "="
	 * @param nodeSep  Text between nodes e.g. "|"
	 * @return A string that represents the tree in text format e.g. NXextry:NXinstrument:name=
	 */
	public String toText(String prefix, String keyValueSep, String dataItemSep, String nodeSep) ;

	/**
	 * @param prefix Text that begins each line e.g. ""
	 * @param keyValueSep Text between key and value e.g. ":"
	 * @param dataItemSep  Text between key and data e.g. "="
	 * @param nodeSep  Text between nodes e.g. "|"
	 * @param includeData  Whether to include the data in the output
	 * @return A string that represents the tree in text format e.g. NXextry:NXinstrument:name=
	 */
	public String toText(String prefix, String keyValueSep, String dataItemSep, String nodeSep, boolean includeData) ;


	/**
	 * @param newlineAfterEach - if true the output is interspersed with newlines to make it more humanly readable
	 * @param dataAsString - - if true data array are written as string. NX_CHAR data is always shown as a string
	 * @return A string that represents the tree in xml
	 */
	public String toXML(boolean newlineAfterEach, boolean dataAsString) ;

	/**
	 * @param newlineAfterEach
	 * @param dataAsString
	 * @return xml representation of the node and children up to final </ > item
	 */
	public StringBuffer toXMLbegin(boolean newlineAfterEach, boolean dataAsString);

	/**
	 * @param newlineAfterEach
	 * @param dataAsString
	 * @return final </ > part of an xml representation of the node and children
	 */
	public  StringBuffer toXMLend(boolean newlineAfterEach, boolean dataAsString);

	/**
	 * @param comparator
	 */
	public void sort(Comparator<INexusTree> comparator);

	/**
	 * @return true if the data can vary during a scan
	 */
	public boolean isPointDependent();

	/**
	 * @return unique id of item in the tree - name_of_top/.../name
	 */
	public String getNodePath();


	/**
	 * @return unique id of item in the tree - name_of_top/class_of_top/.../name/class
	 */
	public String getNodePathWithClasses();

	/**
	 * @param nodePath - format as returned by getNodePath  - name_of_top/.../name
	 * @return node at given path in the tree.
	 */
	public INexusTree getNode(String nodePath);

	/**
	 * Get an attribute of current node
	 * @param name
	 * @return an attribute with given name or null if not found
	 */
	public Serializable getAttribute(String name);

	/**
	 * Get all attributes of current node
	 * 
	 * @return a Map of attributes
	 */
	public Map<String, Serializable> getAttributes();
}