/*
 * Copyright © 2011 Diamond Light Source Ltd.
 * Contact :  ScientificSoftware@diamond.ac.uk
 * 
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.rcp.nexus;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to contain a table-tree view of a NeXus tree
 */
public class NexusTableTree extends Composite {

	private TreeViewer tViewer = null;
	private Listener slistener,dlistener;
	private TreeFilter treeFilter;

	/**
	 * @param parent
	 * @param slistener for single clicks
	 * @param dlistener for double clicks
	 */
	public NexusTableTree(Composite parent, Listener slistener, Listener dlistener) {
		
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		this.slistener = slistener;
		this.dlistener = dlistener;

		// set up tree filter to omit following node names
		final String[] nodeNames = new String[] { "target" };
		treeFilter = new TreeFilter(nodeNames);

		// set up tree and its columns
		tViewer = new TreeViewer(this, SWT.BORDER|SWT.VIRTUAL);
		tViewer.setUseHashlookup(true);

		Tree tree = tViewer.getTree();
		tree.setHeaderVisible(true);

		String[] titles = { "Name", "Class", "Dims", "Type", "Data" };
		int[] widths = { 250, 120, 80, 60, 300 };

		TreeViewerColumn tVCol;
		for (int i = 0; i < titles.length; i++) {
			tVCol = new TreeViewerColumn(tViewer, SWT.NONE);
			TreeColumn tCol = tVCol.getColumn();
			tCol.setText(titles[i]);
			tCol.setWidth(widths[i]);
			tCol.setMoveable(true);
		}

		tViewer.setContentProvider(new NexusLazyContentProvider(tViewer, treeFilter));
		tViewer.setLabelProvider(new NexusLabelProvider());
		if (slistener != null) tree.addListener(SWT.MouseUp, slistener);
		if (dlistener != null) tree.addListener(SWT.MouseDoubleClick, dlistener);
		addMenu(tViewer);
	}
	
	public Viewer getViewer() {
		return tViewer;
	}
	
	@Override
	public void dispose() {
		if (slistener != null) tViewer.getTree().removeListener(SWT.MouseUp,          slistener);
		if (dlistener != null) tViewer.getTree().removeListener(SWT.MouseDoubleClick, dlistener);
		tViewer.getTree().dispose();
		super.dispose();
	}

	private void addMenu(TreeViewer v) {
		final MenuManager mgr = new MenuManager();
		Action action;
		
		for( int i = 0; i < v.getTree().getColumnCount(); i++ ) {
			final TreeColumn column = v.getTree().getColumn(i);
			
			action = new Action(v.getTree().getColumn(i).getText(),SWT.CHECK) {
				@Override
				public void runWithEvent(Event event) {
					if( ! isChecked() ) {
						int width = column.getWidth();
						column.setData("restoredWidth", new Integer(width));
						column.setWidth(0);
					} else {
						int width = (Integer) column.getData("restoredWidth");
						column.setWidth(width);
					}
				}
			};
			action.setChecked(true);
			mgr.add(action);
		}
		
		v.getControl().setMenu(mgr.createContextMenu(v.getControl()));
	}

	/**
	 * @param tree
	 */
	public void setInput(INexusTree tree) {
		if (tViewer!=null && tViewer.getContentProvider()!=null) {
		    tViewer.setInput(tree);
		    tViewer.getTree().setItemCount(tree.getNumberOfChildNodes());
		}
	}

	/**
	 * @return selection
	 */
	public IStructuredSelection getSelection() {
		return (IStructuredSelection) tViewer.getSelection();
	}

	public void expandAll() {
		tViewer.expandAll();
	}

}

class NexusLazyContentProvider implements ILazyTreeContentProvider {
	private TreeViewer viewer;
	private TreeFilter filter;

	public NexusLazyContentProvider(TreeViewer treeViewer, TreeFilter treeFilter) {
		filter = treeFilter;
		viewer = treeViewer;
	}

	@Override
	public Object getParent(Object element) {
		if (element == null || !(element instanceof INexusTree)) {
			return null;
		}
		INexusTree ntNode = ((INexusTree) element).getParentNode();
		if (ntNode == null)
			return element;
		return ntNode;
	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		// count number of nodes that will not be filtered out
		int count = 0;
		if (element instanceof INexusTree) {
			INexusTree ntNode = (INexusTree) element;

			for (int i = 0, imax = ntNode.getNumberOfChildNodes(); i < imax; i++) {
				if (filter.select(ntNode.getChildNode(i).getName()))
					count++;
			}
		}
		viewer.setChildCount(element, count);
	}

	@Override
	public void updateElement(Object parent, int index) {
		INexusTree pNode = (INexusTree) parent;
		INexusTree ntNode = pNode.getChildNode(index);

		int i = index;
		int imax = pNode.getNumberOfChildNodes() - 1;
		while (i < imax && !filter.select(ntNode.getName())) { // skip filtered nodes
			ntNode = pNode.getChildNode(++i);
		}
		if (i > imax) {
			return;
		}

		viewer.replace(parent, index, ntNode);
		updateChildCount(ntNode, -1);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}

class NexusLabelProvider implements ITableLabelProvider {
	private static final Logger logger = LoggerFactory.getLogger(NexusLabelProvider.class);
	
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		String msg = "";

		INexusTree nTNode = (INexusTree) element;
		switch(columnIndex) {
		case 0: // name
			msg = nTNode.getName();
			break;
		case 1: // class
			msg = nTNode.getNxClass();
			break;
		}

		NexusGroupData gData = nTNode.getData();
		if (gData != null) {
			switch (columnIndex) {
			case 2: // dimensions
				for (int i : gData.dimensions) {
					msg += i + ", ";
				}
				if (msg.length() > 2)
					msg = msg.substring(0, msg.length()-2);
				break;
			case 3: // type
				switch (gData.type) {
				case NexusFile.NX_CHAR:
					msg = "CHAR";
					break;
				case NexusFile.NX_FLOAT64:
					msg = "FLT64";
					break;
				case NexusFile.NX_FLOAT32:
					msg = "FLT32";
					break;
				case NexusFile.NX_INT64:
					msg = "INT64";
					break;
				case NexusFile.NX_UINT64:
					msg = "UINT64";
					break;
				case NexusFile.NX_INT32:
					msg = "INT32";
					break;
				case NexusFile.NX_UINT32:
					msg = "UINT32";
					break;
				case NexusFile.NX_INT16:
					msg = "INT16";
					break;
				case NexusFile.NX_UINT16:
					msg = "UINT16";
					break;
				case NexusFile.NX_INT8:
					msg = "INT8";
					break;
				case NexusFile.NX_UINT8:
					msg = "UINT8";
					break;
				default:
					msg = "type = " + gData.type;
					break;
				}
				break;
			case 4: // data
				msg = "double-click to view";
				if (gData.type == NexusFile.NX_CHAR) {
					NexusGroupData ngd;
					try {
						ngd = NexusExtractor.getNexusGroupDataWithBuffer(nTNode, true);
						Serializable buf = ngd.getBuffer();
						msg = new String((byte[]) buf, "UTF-8");
						if (msg.length() > 100) // restrict to 100 characters
							msg = msg.substring(0, 100) + "...";
					} catch (Exception e) {
						logger.error("Error getting data",e);
						msg = "Error getting data";
					}
				} else {
					// show a single value
					if (gData.dimensions.length == 1 && gData.dimensions[0] == 1) {
						if (gData.getBuffer() == null) {
							try {
								NexusGroupData ngd = NexusExtractor.getNexusGroupDataWithBuffer(nTNode, true);
								msg = ngd.dataToTxt(false, true, false);
							} catch (Exception e) {
								logger.error("Error getting data",e);
								msg = "Error getting data";
							}
						} else {
							msg = gData.dataToTxt(false, true, false);
						}
					}
				}
				break;
			}
		}
		return msg;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}
	
}

/**
 * Class to act as a filter for nodes of tree
 */
class TreeFilter {
	Collection<String> unwantedNodeNames;

	/**
	 * Constructor that needs an array of the names of unwanted nodes
	 *
	 * @param names
	 */
	public TreeFilter(String[] names) {
		unwantedNodeNames = new HashSet<String>();

		for (String n: names)
			unwantedNodeNames.add(n);
	}

	public boolean select(String node) {
		return !unwantedNodeNames.contains(node);
	}
}
