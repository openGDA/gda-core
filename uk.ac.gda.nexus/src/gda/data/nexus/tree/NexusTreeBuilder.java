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

import gda.data.nexus.extractor.INexusDataGetter;
import gda.data.nexus.extractor.INexusTreeProcessor;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusExtractorException;
import gda.data.nexus.extractor.NexusGroupData;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.nexus.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 */
public class NexusTreeBuilder implements INexusTreeProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(NexusTreeBuilder.class);
	NexusTreeNodeSelection currentSelectedNode = null;
	INexusTree parentNode = null;
	final NexusTreeNodeSelection selectionTree;
	INexusTree tree;
	TREE_CONTENTS requiredContents;

	NexusTreeBuilder(TREE_CONTENTS requiredContents) {
		this.requiredContents = requiredContents;
		selectionTree = null;
	}
	
	NexusTreeBuilder(NexusTreeNodeSelection selectionTree) {
		this.selectionTree = selectionTree;
		currentSelectedNode = selectionTree;
	}

	/*
	 * process new group found in the file.
	 * the function needs to return to the caller if the child nodes are to be processed or skipped
	 * if the data for the current point is wanted ( it is an SDS or Attr then read using getDataForCurrentProcessedGroup
	 * 
	 * first check if this item is wanted as expressed in the selection tree - calling match
	 * if not wanted (skip) then return skip 
	 * if wanted created a node and add to the tree. If SDS or Attr the  read the data into the node
	 * if Attr return SKIP as attributes cannot have children
	 */
	@Override
	public RESPONSE beginElement(String name, String nxClass, 
			INexusDataGetter nexusDataGetter) throws NexusException,
			NexusExtractorException {
		INexusTree treeNode = new NexusTreeNode(name, nxClass, parentNode, null);
		NexusTreeNodeSelection thismatch = match(currentSelectedNode, treeNode);
		RESPONSE response = RESPONSE.SKIP_OVER;
		if (!thismatch.isSkip()) {
			response = RESPONSE.GO_INTO;
			//we are keeping this one so add in the attributes
			treeNode = new NexusTreeNode(name, nxClass, parentNode, null);
			boolean isAttr =  nxClass.equals(NexusExtractor.AttrClassName);
			boolean isSDS = nxClass.equals(NexusExtractor.SDSClassName);
			if ( isSDS || isAttr){
				//if SDS or attr then read using getDataForCurrentProcessedGroup.
				if (parentNode == null) {
					throw new NexusExtractorException("NexusTreeProcessor.begin - no parent found for SDS or Attr element.");
				}
				if(isAttr)
					response = RESPONSE.SKIP_OVER; // attributes cannot have children so always skip over
				else 
					response = RESPONSE.SDS_ATTR; //  SDS can have attributes.

				
				
				NexusGroupData nexusGroupData = null;
				String targetVal=null;
				if( !isAttr){
					//Look for target attribute
					NexusGroupData data = nexusDataGetter.getAttributeOfCurrentProcessedGroup("target");
					if( data != null && data.isChar() && data.getBuffer() != null){
						//this is a link so look in targetNodes 
						//note that both ends of the link have the target attribute so if the value has already 
						//been read then it will be in the targetNodes
						Serializable buffer = data.getBuffer();
						targetVal = ((String [])buffer)[0];
						nexusGroupData = targetNodes.get(targetVal);
					}
				}
				if( nexusGroupData == null){
					nexusGroupData = nexusDataGetter.getDataForCurrentProcessedGroup(name, nxClass, isAttr || thismatch.isGetData());
					if( targetVal != null)
						targetNodes.put(targetVal, nexusGroupData);
				}
				treeNode = new NexusTreeNode(name, nxClass, parentNode, nexusGroupData);
			}
			if (parentNode != null) {
				parentNode.addChildNode(treeNode);
			} else {
				try {
					treeNode = new NexusTreeTopNode(treeNode, nexusDataGetter.getSourceId());
					tree = treeNode;
				} catch (Exception e) {
					throw new NexusExtractorException("Error getting source", e);
				}
			}
			if(!isAttr ){
				//attributes are always skipped over ( have no children) so do not set parentNode
				parentNode = treeNode;
			}
		}
		return response;
	}
	
	/* To support the concept of links that are expressed as attributes called target we hold onto all those NexusGroupData items that are to be added into the tree 
	 * more than once. 
	 * note that both ends of the link have the target attribute so if the value has already
	 * been read then it will be in the targetNodes
	 */
	Map<String, NexusGroupData > targetNodes = new HashMap<String, NexusGroupData>();
	
	@Override
	public void endElement() {
		parentNode = parentNode.getParentNode();
	}

	/**
	 * @param fileName
	 * @param selectionTree
	 * @return The tree of selected items from the nexus file @see NexusTreeNode
	 * @throws NexusException
	 * @throws NexusExtractorException
	 */
	static public INexusTree getNexusTree(String fileName, NexusTreeNodeSelection selectionTree)
			throws NexusException, NexusExtractorException {
		return getNexusTree(fileName, selectionTree, null);
	}
	
	/**
	 * NOTE: this is synchronized otherwise the thread test fails.
	 * The addition of synchronized keyword on the static method in NexusTreeBuilder is not the 
	 * correct thing to do as it block concurrent use of that static method on different 
	 * files as well as the same file. If the nexus api does not allow concurrent access to 
	 * the  file then the locking should be done in NexusFile. 
	 * 
	 * @param fileName
	 * @param selectionTree
	 * @param mon 
	 * @return The tree of selected items from the nexus file @see NexusTreeNode
	 * @throws NexusException
	 * @throws NexusExtractorException
	 */
	static public synchronized INexusTree getNexusTree(String fileName, NexusTreeNodeSelection selectionTree, final IMonitor mon) throws NexusException, NexusExtractorException {
		NexusExtractor extractor =  new NexusExtractor(fileName);
		NexusTreeBuilder proc = new NexusTreeBuilder(selectionTree);
		extractor.runLoop(proc, System.getProperty("gda.nexus.instrumentApi") != null, mon);
		return proc.getTree();
	}

	/**
	 * @param fileName
	 * @param nexusSelectionFilename
	 * @return The tree of selected items from the nexus file @see NexusTreeNode
	 * @throws NexusException
	 * @throws NexusExtractorException
	 * @throws Exception
	 */
	static public INexusTree getNexusTree(String fileName, String nexusSelectionFilename) throws NexusException,
			NexusExtractorException, Exception {
		
		return getNexusTree(fileName, nexusSelectionFilename, null);
	}

	/**
	 * @param fileName
	 * @param nexusSelectionFilename
	 * @param mon 
	 * @return The tree of selected items from the nexus file @see NexusTreeNode
	 * @throws NexusException
	 * @throws NexusExtractorException
	 * @throws Exception
	 */
	static public INexusTree getNexusTree(String fileName, String nexusSelectionFilename, final IMonitor mon) throws NexusException,
			NexusExtractorException, Exception {
		NexusTreeNodeSelection selectionTree;
		if (nexusSelectionFilename == null) {
			selectionTree = null; // get it all
		} else if (nexusSelectionFilename.isEmpty()) {
			selectionTree = NexusTreeNodeSelection.createTreeForAllNXEntries();
		} else {
			selectionTree = NexusTreeNodeSelection.createFromXML(nexusSelectionFilename);
		}
		return getNexusTree(fileName, selectionTree, mon);
	}
	/**
	 *
	 */
	public enum TREE_CONTENTS { 
	/**
	 * All data within NXentry
	 */
	ALLNXENTRY,
	/**
	 * All 
	 */
	ALL
	}
	/**
	 * @param fileName
	 * @param requiredContents
	 * @return NExusTree read from the file with contents as required
	 * @throws Exception 
	 */
	static public INexusTree getNexusTree(String fileName, TREE_CONTENTS requiredContents) throws Exception{
		NexusExtractor extractor =  new NexusExtractor(fileName);
		NexusTreeBuilder proc = new NexusTreeBuilder(requiredContents);
		try{
			extractor.runLoop(proc, System.getProperty("gda.nexus.instrumentApi") != null);
		} catch (Exception e){
			String msg = proc.getTree() != null ? proc.getTree().toString() : "tree empty";
			throw new Exception("Error in getNexusTree for file=" + fileName + ". Tree = "+msg,e);
		}
		return proc.getTree();		
	}
	INexusTree getTree() {
		return tree;
	}

	private NexusTreeNodeSelection match(NexusTreeNodeSelection selectionTree, INexusTree treeNode) {
		if(requiredContents != null){
			//if using the requiredContents rather than selectionTree
			NexusTreeNodeSelection match=NexusTreeNodeSelection.SKIP;
			if (requiredContents == TREE_CONTENTS.ALL){
				match = NexusTreeNodeSelection.GET_ALL;
			} else {
				//get top item and check if NXEntry class
				INexusTree node = treeNode;
				INexusTree childOfTop=node;
				while (node.getParentNode()!= null) {
					childOfTop = node;
					node = node.getParentNode();
				}
				if( !(node.getNxClass().equals(NexusExtractor.topClass) && node.getName().equals(NexusExtractor.topName)) ){
					logger.error("top node is not correct");
				} else if( childOfTop == node) {
					match = NexusTreeNodeSelection.GET_ALL;
				} else if(requiredContents == TREE_CONTENTS.ALLNXENTRY && childOfTop.getNxClass().equals(NexusExtractor.NXEntryClassName)){
					match = NexusTreeNodeSelection.GET_ALL;
				} 
			}
			return match;
		}
		if (selectionTree == null)
			return NexusTreeNodeSelection.GET_ALL;
		INexusTree node = treeNode;
		/*
		 * Build up complete path to current node
		 */
		LinkedList<Group> path = new LinkedList<Group>();
		path.add(new Group(node));
		while (node.getParentNode()!= null) {
			node = node.getParentNode();
			path.add(new Group(node));
		}
		Iterator<Group> iter = path.descendingIterator();

		//construct a list of NexusTreeNodeSelection that just contains the top item so that the
		//whole tree can be walked through using
		// for (NexusTreeNodeSelection selectionNode : selectionNodes){
		//  selectionNodes = selectionNode.getChildNodes();
		// }
		List<NexusTreeNodeSelection> selectionNodes;
		{
			Vector<NexusTreeNodeSelection> selectionNodesV = new Vector<NexusTreeNodeSelection>();
			selectionNodesV.add(selectionTree);
			selectionNodes = selectionNodesV;
		}	

		/*
		 * iterate along the path to the current location - at each point check if it is wanted
		 * the iteration ends when the end of selection tree is reached
		 */
		// now look for it in selectionTree
		NexusTreeNodeSelection match = NexusTreeNodeSelection.SKIP; //if not found then skip over
		while (iter.hasNext()) {
			Group item = iter.next();
			if (!match.isGetThisAndBelow())
				match = NexusTreeNodeSelection.SKIP;
			for (NexusTreeNodeSelection selectionNode : selectionNodes) { 
				if (selectionNode.MatchesNXClass(item.NXclass)) { 
					//find a node in selection tree that matches current node in path
					if (selectionNode.MatchesName(item.name)) { 
						//if the names match then set current match to the match at that point 
						// and move down selection tree for next point on the path
						// use this node for the next iteration
						match = selectionNode;
						selectionNodes = selectionNode.getChildNodes();
						break;
					}
				}
				//if no match found for NXClass and name then check next node at this level of the selection tree
			}
			//if no match found for NXClass and name then check next node at this level of the selection tree
			if (match.isSkip()){
				//if the match at this point on the path is skip then escape
				break;
			}
		}
		return match;
	}
}

class Group {

	static public Group getInstance(Group source) {
		return new Group(source.name, source.NXclass);
	}

	final String name;
	final String NXclass;

	public Group(String name, String NXclass) {
		this.name = name;
		this.NXclass = NXclass;
	}
	public Group(INexusTree node) {
		this.name = node.getName();
		this.NXclass = node.getNxClass();
	}
	
	
	public boolean containsSDS() {
		return getNXclass().equals(NexusExtractor.SDSClassName);
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((NXclass == null) ? 0 : NXclass.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Group)) {
			return false;
		}
		Group other = (Group) o;
		return name.equals(other.name) && NXclass.equals(other.NXclass);
	}
	
	public String getName() {
		return name;
	}

	public String getNXclass() {
		return NXclass;
	}

	@Override
	public String toString() {
		return name + "." + NXclass;
	}
}
