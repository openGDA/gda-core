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

import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.xml.sax.InputSource;

import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * This class represents the elements of the nexus file that are to be extracted
 * Using this object one can specify where an element is skipped, accepted and the data to be extracted.
 */
final public class NexusTreeNodeSelection {
	/**
	 * values for dataType
	 */
	static int NAME_ONLY = 0;
	static int NAME_AND_DIMS = 1; //name, attributes and dims
	static int NAME_DIMS_AND_DATA = 2; //name, attributes, dims and data

	/**
	 * values for wanted
	 */
	static int SKIP_THIS_ITEM = 0;
	static int GET_THIS_ITEM = 1;
	static int GET_THIS_AND_BELOW = 2;

	/**
	 * Value to indicate no items are to be selected
	 */
	public final static NexusTreeNodeSelection SKIP;

	/**
	 * Value to indicate all items are to be selected
	 */
	public final static NexusTreeNodeSelection GET_ALL;
	
	static {
		SKIP = new NexusTreeNodeSelection();
		SKIP.wanted = SKIP_THIS_ITEM;
		SKIP.dataType = NAME_ONLY;
		GET_ALL = new NexusTreeNodeSelection();
		GET_ALL.wanted = GET_THIS_AND_BELOW;
		GET_ALL.dataType = NAME_DIMS_AND_DATA;
	}

	static private final URL mappingURL = NexusTreeNodeSelection.class.getResource("NexusTreeMapping.xml");
	static private final URL schemaUrl = NexusTreeNodeSelection.class.getResource("NexusTree.xsd");
	/**
	 * @param filename to an xml file that conforms to NexusTree.xsd describing the selection required
	 * @return @see NexusTreeNodeSelection
	 * @throws Exception
	 */
	public static NexusTreeNodeSelection createFromXML(String filename) throws Exception {
		NexusTreeNodeSelection tree = (NexusTreeNodeSelection) XMLHelpers.createFromXML(mappingURL,
				NexusTreeNodeSelection.class, schemaUrl, filename);
		// ensure top has NexusGroup with name and class blank
		tree.setName(NexusExtractor.topName);
		tree.setNxClass(NexusExtractor.topClass);
		return tree;
	}

	/**
	 * @param inputSource to an xml file that conforms to NexusTree.xsd describing the selection required
	 * @return @see NexusTreeNodeSelection
	 * @throws Exception
	 */
	public static NexusTreeNodeSelection createFromXML(InputSource inputSource) throws Exception {
		NexusTreeNodeSelection tree = (NexusTreeNodeSelection) XMLHelpers.createFromXML(mappingURL,
				NexusTreeNodeSelection.class, schemaUrl, inputSource);
		// ensure top has NexusGroup with name and class blank
		tree.setName(NexusExtractor.topName);
		tree.setNxClass(NexusExtractor.topClass);
		return tree;
	}
	
	/**
	 * @return @see NexusTreeNodeSelection  - a selection to extract all items with NXenty and below, including the data
	 */
	public static NexusTreeNodeSelection createTreeForAllNXEntries() {
		NexusTreeNodeSelection top = new NexusTreeNodeSelection();
		top.setName(NexusExtractor.topName);
		top.setNxClass(NexusExtractor.topClass);
		top.wanted = GET_THIS_ITEM;
		NexusTreeNodeSelection nxEntries = new NexusTreeNodeSelection();
		nxEntries.setName("");
		nxEntries.setNxClass(NexusExtractor.NXEntryClassName);
		nxEntries.wanted = GET_THIS_AND_BELOW;
		nxEntries.dataType = NAME_DIMS_AND_DATA;
		top.addChildNode(nxEntries);
		return top;

	}
	/**
	 * @return @see NexusTreeNodeSelection  - a selection to extract all DataItems with NXenty/NXData and below, including the data
	 */
	public static NexusTreeNodeSelection createTreeForAllNXData() {
		return createTreeForDataSetNames(null, true);
	}
	
	/**
	 * @param dataSetNames - list of names of data to be extracted
	 * @return @see NexusTreeNodeSelection  - a selection to extract all DataItems with NXenty/NXData with names in datSetName
	 * 
	 * Each dataSetName is expected to be of the form nxDataName.SDSName. However for positioner data the same data will be accessed
	 * from each set of detector data and os the nxDataName is not relevant. Instead for such data the dataSetName is a simple single
	 * name that refers to the name of SDS element in the tree within any nxData element.
	 * See the tests in NexusLoaderTest for details of a particular case.  
	 */
	public static NexusTreeNodeSelection createTreeForDataSetNames(List<String> dataSetNames, boolean withData) {
		NexusTreeNodeSelection top = new NexusTreeNodeSelection();
		top.setName(NexusExtractor.topName);
		top.setNxClass(NexusExtractor.topClass);
		top.wanted = GET_THIS_ITEM;
		NexusTreeNodeSelection nxEntry = new NexusTreeNodeSelection();
		nxEntry.setName("");
		nxEntry.setNxClass(NexusExtractor.NXEntryClassName);
		nxEntry.dataType = withData ? NAME_DIMS_AND_DATA : NAME_AND_DIMS;

		if( dataSetNames != null){
			nxEntry.wanted = GET_THIS_ITEM;
			for( String s : dataSetNames){
				String [] parts = s.split("[.]",2);
				String nxDataNodeName = parts.length == 1 ? "" : parts[0];
				String nxChildNodeName = parts.length == 1 ? parts[0] : parts[1]; 

				NexusTreeNodeSelection nxDataNode = null;
				for( NexusTreeNodeSelection sel : nxEntry.getChildNodes()){
					if( sel.MatchesName(nxDataNodeName)){
						nxDataNode = sel;
						break;
					}
						
				}
				if( nxDataNode == null){
					nxDataNode = new NexusTreeNodeSelection();
					nxDataNode.setName(nxDataNodeName);
					nxDataNode.setNxClass( NexusExtractor.NXDataClassName);
					nxDataNode.dataType = withData ? NAME_DIMS_AND_DATA : NAME_AND_DIMS;
					nxDataNode.wanted = GET_THIS_ITEM;
					nxEntry.addChildNode(nxDataNode);
				}

				NexusTreeNodeSelection childNode = new NexusTreeNodeSelection();
				childNode.setName(nxChildNodeName);
				childNode.setNxClass( "SDS");
				childNode.dataType = withData ? NAME_DIMS_AND_DATA : NAME_AND_DIMS;
				childNode.wanted = GET_THIS_AND_BELOW;
				nxDataNode.addChildNode(childNode);
			}
		} else {
			nxEntry.wanted = GET_THIS_AND_BELOW;
		}

		top.addChildNode(nxEntry);
		return top;

	}

	/**
	 * @return @see NexusTreeNodeSelection  - a selection to extract all data
	 */
	public static NexusTreeNodeSelection createTreeForAllData() {
		NexusTreeNodeSelection top = new NexusTreeNodeSelection();
		top.setName(NexusExtractor.topName);
		top.setNxClass(NexusExtractor.topClass);
		top.wanted = GET_THIS_AND_BELOW;
		NexusTreeNodeSelection entries = new NexusTreeNodeSelection();
		entries.setName("");
		entries.setNxClass("");
		entries.wanted = GET_THIS_AND_BELOW;
		entries.dataType = NAME_DIMS_AND_DATA;
		top.addChildNode(entries);
		return top;

	}
	
	
	/**
	 * @return @see NexusTreeNodeSelection  - a selection to extract all items but for Scannable and Detector data
	 * @throws Exception 
	 */
	public static NexusTreeNodeSelection createTreeForAllMetaData() throws Exception {
		String xml = "<?xml version='1.0' encoding='UTF-8'?>" +
		"<nexusTreeNodeSelection>" +
		"<nexusTreeNodeSelection><nxClass>NXentry</nxClass><wanted>2</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>NXdata</nxClass><wanted>0</wanted><dataType>2</dataType>" +
		"</nexusTreeNodeSelection>" +
		"<nexusTreeNodeSelection><nxClass>NXinstrument</nxClass><wanted>2</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>NXdetector</nxClass><wanted>2</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>SDS</nxClass><name>data</name><wanted>2</wanted><dataType>1</dataType>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>";
		return NexusTreeNodeSelection.createFromXML(new InputSource(new StringReader(xml)));

	}

	private List<NexusTreeNodeSelection> childNodes = new Vector<NexusTreeNodeSelection>();

	private int dataType = NAME_ONLY;
	private String nxName_UseSetNxName = NexusExtractor.topName;
	private String nxClass_UseSetNxClass = NexusExtractor.topClass;
	private int wanted = GET_THIS_ITEM;

	private Pattern nxNamePattern=Pattern.compile(nxName_UseSetNxName);
	private Pattern nxClassPattern=Pattern.compile(nxClass_UseSetNxClass);

	/**
	 * Constructor used by Castor when constructing the object from an XML file
	 */
	public NexusTreeNodeSelection() {
	}

	/**
	 * @param name - name of the element, leave
	 * @param nxClass
	 * @param wanted
	 * @param dataType
	 */
	public NexusTreeNodeSelection(String name, String nxClass, int wanted, int dataType) {
		this.wanted = wanted;
		this.dataType = dataType;
		setName(name);
		setNxClass(nxClass);
		nxNamePattern=Pattern.compile(name);
		nxClassPattern=Pattern.compile(nxClass);
		
	}

	/**
	 * @param childNode
	 */
	public void addChildNode(NexusTreeNodeSelection childNode) {
		childNodes.add(childNode);
	}

	/**
	 * @return childNodes
	 */
	public List<NexusTreeNodeSelection> getChildNodes() {
		return childNodes;
	}

	/**
	 * @return Type of data to be extracted . See values for dataType
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * @return name of the item (group or data)
	 */
	public String getName() {
		return nxName_UseSetNxName;
	}

	/**
	 * @return class of the item (group or data) e.g. NXentry or SDS
	 */
	public String getNxClass() {
		return nxClass_UseSetNxClass;
	}

	/**
	 * @return see value for wanted
	 */
	public int getWanted() {
		return wanted;
	}

	boolean isGetData() {
		return dataType == NAME_DIMS_AND_DATA;
	}
	
	boolean isGetThisAndBelow() {
		return wanted == GET_THIS_AND_BELOW;
	}

	boolean isSkip() {
		return wanted == SKIP_THIS_ITEM;
	}

	boolean MatchesName(String name) {
		return nxName_UseSetNxName.isEmpty() || nxNamePattern.matcher(name).matches();
	}

	boolean MatchesNXClass(String NXclass) {
		return nxClass_UseSetNxClass.isEmpty() || nxClassPattern.matcher(NXclass).matches();
	}

	/**
	 * @param dataType
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		nxName_UseSetNxName = name;
		nxNamePattern=Pattern.compile(nxName_UseSetNxName);
		
	}

	/**
	 * @param nxClassName
	 */
	public void setNxClass(String nxClassName) {
		nxClass_UseSetNxClass = nxClassName;
		nxClassPattern=Pattern.compile(nxClass_UseSetNxClass);
	}

	/**
	 * @param wanted
	 */
	public void setWanted(int wanted) {
		this.wanted = wanted;
	}
}
