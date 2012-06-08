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

package uk.ac.gda.client;

import gda.plots.ISelectableNode;
import gda.plots.Marker;
import gda.plots.ScanLine;
import gda.plots.ScanTree;
import gda.plots.ScanTreeM;
import gda.plots.Selected;
import gda.plots.XYDataHandler;
import gda.plots.XYDataHandlerLegend;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

/**
 *
 */
public class SWTXYDataHandlerLegend extends Composite implements XYDataHandlerLegend {

	private static final String VISIBLE = "Visible";
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(XYDataHandlerLegend.class);
	private ScanTree model;
	private TreeViewer tv;
	private CellEditor[] cellEditors;

	/**
	 * @param parent
	 * @param style
	 * @param simplePlot
	 */
	public SWTXYDataHandlerLegend(Composite parent, int style, XYDataHandler simplePlot) {
		super(parent, style);
		setLayout(new FillLayout());
		// Create the tree viewer to display the file tree
		tv = new TreeViewer(this, SWT.MULTI);
		tv.getTree().setEnabled(true);
		tv.setContentProvider(new ScanTreeContentProvider());
		tv.setLabelProvider(new ScanTreeLabelProvider());
		tv.setColumnProperties(new String[] { VISIBLE });
		model = new ScanTree(new ScanTreeM(), simplePlot);
		tv.setInput(model); // pass a non-null that will be ignored
		CheckboxCellEditor chk = new CheckboxCellEditor(tv.getTree());
		cellEditors = new CellEditor[] { chk };
		tv.setCellEditors(cellEditors);
		tv.setCellModifier(new ICellModifier() {

			@Override
			public boolean canModify(Object element, String property) {
				return property.equals(VISIBLE);
			}

			@Override
			public Object getValue(Object element, String property) {
				if (property.equals(VISIBLE) && element instanceof ISelectableNode) {
					return ((ISelectableNode) element).getSelected() == Selected.All;
				}
				return null;
			}

			@Override
			public void modify(Object element, String property, Object value) {
				if (element instanceof TreeItem) {
					TreeItem item = (TreeItem) element;
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) item.getData();
					if (node instanceof ISelectableNode && property.equals(VISIBLE)) {
						setSelectedFlag(node,(Boolean) value);
					}
				}
			}

		});
	}

	
	
	/**
	 * @param node
	 * @param value
	 */
	public void setSelectedFlag(DefaultMutableTreeNode node, boolean value){
		if (node instanceof ISelectableNode) {
			((ISelectableNode) node).setSelectedFlag(value);
			model.valueForPathChanged(new TreePath(node.getPath()), node);		
		}
	}
	/**
	 * @param path
	 * @param newValue
	 */
	public void valueForPathChanged(TreePath path, Object newValue){
		model.valueForPathChanged(path, newValue);
	}
    /**
     * Returns the tree viewer which shows the resource hierarchy.
     * @return the tree viewer
     * @since 2.0
     */
    public TreeViewer getTreeViewer() {
        return tv;
    }

	
	@Override
	public void addScan(final String currentFilename, final String topGrouping, final String [] subGrouping, 
			String itemName, boolean visible, String id, int lineNumber,
			Color color, Marker marker, final boolean onlyOne, String xLabel, final boolean reloadLegendModel){
		if (isDisposed())
			return;
		final boolean menuAutoCollapseTreeOnAdd = false;
		final ScanLine scanLine = new ScanLine(itemName, !autoHideNewScan & visible, id, lineNumber, color, marker, xLabel);
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				model.addScanLine(currentFilename, topGrouping, subGrouping, scanLine, onlyOne,
						menuAutoCollapseTreeOnAdd, autoHideLastScan, autoHideNewScan, reloadLegendModel);
			}
		});

	}

	/**
	 * update the model to cause the legend to be re-displayed
	 */
	public void reload(){
		model.reload();
	}
	/**
	 * remove the item from the tree whose filename equals the one specified
	 * @param filename 
	 */
	public void removeScanGroup(String filename)
	{
		model.removeScanGroup(filename);
	}
	public void removeScanTreeObjects(Object []  selectedItems) {
		model.removeScanTreeObjects(selectedItems);
		
	}	
	
	@Override
	public void removeAllItems() {
		if (isDisposed())
			return;
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
		while (rootNode.getChildCount() > 0) {
			model.removeNodeFromParent((DefaultMutableTreeNode) rootNode.getChildAt(0));
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	boolean autoHideNewScan = false;

	/**
	 * @param value
	 *            True if new scans are to be hidden automatically
	 */
	public void setAutoHideNewScan(Boolean value) {
		autoHideNewScan = value;
	}

	/**
	 * @return true if new scans are not made visible
	 */
	public Boolean getAutoHideNewScan() {
		return autoHideNewScan;
	}

	boolean autoHideLastScan = false;

	/**
	 * @param value
	 *            True if last scans is to be hidden automatically
	 */
	public void setAutoHideLastScan(Boolean value) {
		autoHideLastScan = value;
	}

	/**
	 * @return true if last scans are made invisible
	 */
	public Boolean getAutoHideLastScan() {
		return autoHideLastScan;
	}

	/**
	 * Hide all scans
	 */
	public void hideAll() {
		if (isDisposed())
			return;
		model.makeAllVisible(false);
	}



	@Override
	public Vector<String> getNamesOfLinesInPreviousScan( boolean visibility) {
		return model.getNamesOfLinesInPreviousScan(visibility);
	}



	public ScanTree getModel() {
		return model;
	}
	
}

/**
 * This class provides the content for the tree in FileTree
 */

class ScanTreeContentProvider implements ITreeContentProvider, TreeModelListener {

	private static final Logger logger = LoggerFactory.getLogger(ScanTreeContentProvider.class);

	private ScanTree tree;
	private Viewer viewer;

	ScanTreeContentProvider() {
	}

	/**
	 * Gets the children of the specified object
	 * 
	 * @param parent
	 *            the parent object
	 * @return Object[]
	 */
	@Override
	public Object[] getChildren(Object parent) {
		Vector<Object> children = new Vector<Object>();

		for (int index = 0; index < tree.getChildCount(parent); index++) {
			children.add(tree.getChild(parent, index));
		}
		return children.toArray(new Object[] {});
	}

	/**
	 * Gets the parent of the specified object
	 * 
	 * @param arg0
	 *            the object
	 * @return Object
	 */
	@Override
	public Object getParent(Object arg0) {
		if ( arg0 instanceof TreeNode)
			return ((TreeNode)arg0).getParent();
		return null;
	}

	/**
	 * Returns whether the passed object has children
	 * 
	 * @param parent
	 *            the parent object
	 * @return boolean
	 */
	@Override
	public boolean hasChildren(Object parent) {
		return tree.getChildCount(parent) > 0;
	}

	/**
	 * Gets the root element(s) of the tree
	 * 
	 * @param arg0
	 *            the input data
	 * @return Object[]
	 */
	@Override
	public Object[] getElements(Object arg0) {
		return getChildren(((ScanTree) arg0).getRoot());
	}

	/**
	 * Disposes any created resources
	 */
	@Override
	public void dispose() {
		if (tree != null) {
			tree.removeTreeModelListener(this);
			tree = null;
		}
		if (viewer != null) {
			this.viewer = null;
		}
	}

	/**
	 * Called when the input changes
	 * 
	 * @param viewer
	 *            the viewer
	 * @param arg1
	 *            the old input
	 * @param arg2
	 *            the new input
	 */
	@Override
	public void inputChanged(Viewer viewer, Object arg1, Object arg2) {
		if (tree != null && arg1 == tree) {
			tree.removeTreeModelListener(this);
			tree = null;
			this.viewer = null;
		}
		if (arg2 != null) {
			if (arg2 instanceof ScanTree) {
				tree = (ScanTree) arg2;
				this.viewer = viewer;
				tree.addTreeModelListener(this);
			} else {
				logger.error("arg2 != ScanTree");
			}
		}
	}

	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		if (viewer != null)
			viewer.refresh();

	}

	@Override
	public void treeNodesInserted(TreeModelEvent e) {
		if (viewer != null)
			viewer.refresh();
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e) {
		if (viewer != null)
			viewer.refresh();
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {
		if (viewer != null)
			viewer.refresh();
	}

}

/**
 * This class provides the labels for the file tree
 */

class ScanTreeLabelProvider implements ILabelProvider, ITreePathLabelProvider {
	// The listeners
	private static final Image someSelected = SWTResourceManager.getImage(ScanTreeLabelProvider.class,
			"/partialSelection.gif");
	private static final Image allSelected = SWTResourceManager.getImage(ScanTreeLabelProvider.class, "/tick.png");
	private static final Image noneSelected = SWTResourceManager.getImage(ScanTreeLabelProvider.class,
			"/noSelection.gif");
	private List<ILabelProviderListener> listeners;

	/**
	 * Constructs a FileTreeLabelProvider
	 */
	public ScanTreeLabelProvider() {
		listeners = new ArrayList<ILabelProviderListener>();
	}

	@Override
	public Image getImage(Object arg0) {
		if (arg0 instanceof ISelectableNode) {
			Selected sel = ((ISelectableNode) arg0).getSelected();
			return sel == Selected.All ? allSelected : (sel == Selected.Some ? someSelected : noneSelected);
		}
		return null;
	}

	@Override
	public String getText(Object arg0) {
		if (arg0 instanceof ISelectableNode) {
			return ((ISelectableNode) arg0).toLabelString(250);
		}
		return arg0.toString();
	}

	private org.eclipse.swt.graphics.Color getColor(Object arg0) {
		if (arg0 instanceof ISelectableNode) {
			Color col = ((ISelectableNode) arg0).getColor();
			return new org.eclipse.swt.graphics.Color(null, col.getRed(), col.getGreen(), col.getBlue());
		}
		return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	}

	/**
	 * Adds a listener to this label provider
	 * 
	 * @param arg0
	 *            the listener
	 */
	@Override
	public void addListener(ILabelProviderListener arg0) {
		listeners.add(arg0);
	}

	/**
	 * Called when this LabelProvider is being disposed
	 */
	@Override
	public void dispose() {
		// Dispose the images
		// if (someSelected != null)
		// someSelected.dispose();
		// if (allSelected != null)
		// allSelected.dispose();
		// if (noneSelected != null)
		// noneSelected.dispose();
	}

	/**
	 * Returns whether changes to the specified property on the specified element would affect the label for the element
	 * 
	 * @param arg0
	 *            the element
	 * @param arg1
	 *            the property
	 * @return boolean
	 */
	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	/**
	 * Removes the listener
	 * 
	 * @param arg0
	 *            the listener to remove
	 */
	@Override
	public void removeListener(ILabelProviderListener arg0) {
		listeners.remove(arg0);
	}

	@Override
	public void updateLabel(ViewerLabel label, org.eclipse.jface.viewers.TreePath elementPath) {

		label.setText(getText(elementPath.getLastSegment()));
		label.setImage(getImage(elementPath.getLastSegment()));
		label.setForeground(getColor(elementPath.getLastSegment()));
		label.setTooltipText(elementPath.getLastSegment().toString());
	}

}


