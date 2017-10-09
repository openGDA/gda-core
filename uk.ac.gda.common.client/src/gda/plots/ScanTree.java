/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.plots;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanTree extends DefaultTreeModel {
	private static final Logger logger = LoggerFactory.getLogger(ScanTree.class);
	private XYDataHandler simplePlot;
	private boolean hideOldestScan = false;
	private int numberOfScansBeforeHiding = 0;

	/**
	 * @param root
	 * @param simplePlot
	 */
	public ScanTree(ScanTreeM root, XYDataHandler simplePlot) {
		super(root);
		this.simplePlot = simplePlot;
	}

	private void setScanTreeItemVisibility(SelectableNode item, boolean visibility) {
		if(item instanceof ScanPair){
			setScanPairVisibility((ScanPair) item, visibility);
		} else {
			item.setSelectedFlag(visibility);
		}

		@SuppressWarnings("unchecked")
		Enumeration<TreeNode> e = item.children();
		while (e.hasMoreElements()) {
			TreeNode n = e.nextElement();
			if (n instanceof ScanPair) {
				setScanPairVisibility((ScanPair) n, visibility);
			}
			if (n instanceof SelectableNode) {
				setScanTreeItemVisibility((SelectableNode) n, visibility);
			}
		}
	}

	private void setScanPairVisibility(ScanPair item, boolean visibility) {
		item.setSelectedFlag(visibility);
		simplePlot.setLineVisibility(item.scanLine.line, visibility);
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		if (newValue instanceof ScanPair) {
			ScanPair node = (ScanPair) path.getLastPathComponent();
			ScanPair nodeVal = (ScanPair) newValue;
			setScanPairVisibility(node, nodeVal.getSelectedFlag());
			reload(node.getParent());
		} else if (newValue instanceof SelectableNode) {
			SelectableNode nodeVal = (SelectableNode) newValue;
			SelectableNode node = (SelectableNode) path.getLastPathComponent();
			setScanTreeItemVisibility(node, nodeVal.getSelectedFlag());
			reload(nodeVal);
		} else if( newValue instanceof ScanLine){
			ScanPair node = (ScanPair) path.getLastPathComponent();
			node.scanLine.lineColor = ((ScanLine)newValue).lineColor;
			node.scanLine.marker = ((ScanLine)newValue).marker;
			simplePlot.setLineColor(node.scanLine.line, node.scanLine.lineColor);
			simplePlot.setLineMarker(node.scanLine.line, node.scanLine.marker);
			reload(node.getParent());
		}
		super.valueForPathChanged(path, newValue);
	}

	public Vector<String> getNamesOfLinesInPreviousScan(boolean visibility){
		Vector<String> names = new Vector<String>();
		ScanTreeM scanTreeM = (ScanTreeM) getRoot();
		if(scanTreeM.getChildCount()>0){
			SelectableNode selNode = (SelectableNode)scanTreeM.getChildAt(0);
			getNamesOfVisibleLinesInNode(names, selNode, visibility);
		}
		return names;
	}

	private void getNamesOfVisibleLinesInNode(Vector<String> names, SelectableNode node, boolean visibility){
		if(node instanceof ScanPair){
			if( node.getSelectedFlag() == visibility){
				String name = ((ScanPair) node).toString();
				if(!names.contains(name)){
					names.add(name);
				}
			}
			return;
		}
		//only look in the first node at each level the other nodes should have duplicate information
		//unless child is ScanPair when you go over all scan lines
		if(node.getChildCount()>0){
			TreeNode nextLevelNode = node.getChildAt(0);
			if( nextLevelNode instanceof ScanPair){

				@SuppressWarnings("unchecked")
				Enumeration<TreeNode> e = node.children();
				while (e.hasMoreElements()) {
					TreeNode n = e.nextElement();
					getNamesOfVisibleLinesInNode(names, (SelectableNode) n, visibility);
				}
			} else if (nextLevelNode instanceof SelectableNode) {
				getNamesOfVisibleLinesInNode(names, (SelectableNode)nextLevelNode, visibility);
			}
		}
	}

	private void hideLastScan(ScanTreeM scanTreeM){
		if(scanTreeM.getChildCount()>1){
			SelectableNode selNode = (SelectableNode)scanTreeM.getChildAt(1);
			setScanTreeItemVisibility(selNode, false);
			reload(selNode);
		}
	}

	private void hideOldestScan(ScanTreeM scanTreeM){
		int numberOfScans = scanTreeM.getChildCount();
		if(numberOfScans>numberOfScansBeforeHiding && numberOfScansBeforeHiding!=0){
			for(int i=numberOfScansBeforeHiding;i<numberOfScans;i++){
				SelectableNode selNode = (SelectableNode)scanTreeM.getChildAt(i);
				setScanTreeItemVisibility(selNode, false);
			}
		}
	}

	/**
	 * @param currentFilename
	 * @param topGrouping
	 * @param subGrouping
	 * @param scanLine
	 * @param onlyOne
	 * @param autoCollapseTree
	 * @param unshowLastScan
	 * @param menuUnshowNewVal
	 * @return node
	 */
	public DefaultMutableTreeNode addScanLine(String currentFilename, String topGrouping, String [] subGrouping, ScanLine scanLine,
			boolean onlyOne, boolean autoCollapseTree, boolean unshowLastScan, boolean menuUnshowNewVal, boolean reloadLegendModel) {
		ScanTreeM scanTreeM = (ScanTreeM) getRoot();
		DefaultMutableTreeNode node = onlyOne ? scanTreeM.addSingleScanLine(currentFilename, topGrouping, scanLine) : scanTreeM
				.addGroupedScanLine(currentFilename, topGrouping, subGrouping, scanLine);

		if(unshowLastScan)
			hideLastScan(scanTreeM);


		if(hideOldestScan){
			hideOldestScan(scanTreeM);
		}

		if(reloadLegendModel){
			try {
				if (autoCollapseTree)
					reload();
				else
					reload(node.getParent());
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
				// do nothing - a null pointer exception can be seen if the model is changed during editing.
			}

		}

		if(menuUnshowNewVal){
			simplePlot.setLineVisibility(scanLine.line, false);
		}

		return node;
	}

	/**
	 * remove the item from the tree whose filename equals the one specified
	 * @param filename
	 */
	public void removeScanGroup(String filename){
		ScanTreeM scanTreeM = (ScanTreeM) getRoot();

		@SuppressWarnings("unchecked")
		Enumeration<TreeNode> e = scanTreeM.children();
		while (e.hasMoreElements()) {
			TreeNode n = e.nextElement();
			if (n instanceof SingleScanLine && ((SingleScanLine)n).getCurrentFilename().equals(filename)){
				scanTreeM.remove((SingleScanLine)n);
//				simplePlot.deleteLine(((SingleScanLine)n).getLineNumber());
				removeNodeFromPlot(n);
				reload();
				return;
			}
			if (n instanceof ScanTreeItem && ((ScanTreeItem)n).getCurrentFilename().equals(filename)){
				scanTreeM.remove((ScanTreeItem)n);
				removeNodeFromPlot(n);
				reload();
				return;
			}
		}
	}

	public void removeScanTreeObjects(Object []  objects) {
		for(Object obj : objects){
			removeScanTreeObject(obj);
		}
		reload();
	}

	private void removeScanTreeObject(Object obj){
		DefaultMutableTreeNode treeNode = null;
		if (obj instanceof DefaultMutableTreeNode )
		{
			treeNode = (DefaultMutableTreeNode)obj;
			TreeNode parent = treeNode.getParent();
			if (parent != null &&  parent instanceof DefaultMutableTreeNode){
				((DefaultMutableTreeNode)parent).remove(treeNode);
			}
		}
		if ( treeNode != null ){
			removeNodeFromPlot(treeNode);
		}
	}

	private void removeNodeFromPlot(TreeNode item) {

		@SuppressWarnings("unchecked")
		Enumeration<TreeNode> e = item.children();
		while (e.hasMoreElements()){
			removeNodeFromPlot( e.nextElement());
		}
		if (item instanceof ScanPair){
			simplePlot.deleteLine(((ScanPair)item).scanLine.line);
		}
	}

	/**
	 * @param visibleFLag
	 */
	public void makeAllVisible(boolean visibleFLag){
		setScanTreeItemVisibility((SelectableNode)getRoot(), visibleFLag);
		reload();
	}

	public void setHideOldestScan(boolean hideOldestScan) {
		this.hideOldestScan = hideOldestScan;
	}

	public boolean getHideOldestScan() {
		return this.hideOldestScan;
	}

	public int getNumberOfScansBeforeHiding() {
		return numberOfScansBeforeHiding;
	}

	public void setNumberOfScansBeforeHiding(int numberOfScansBeforeHiding) {
		this.numberOfScansBeforeHiding = numberOfScansBeforeHiding;
	}

}