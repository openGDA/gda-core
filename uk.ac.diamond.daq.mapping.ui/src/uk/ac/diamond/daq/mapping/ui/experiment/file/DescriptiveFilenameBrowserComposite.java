/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment.file;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import uk.ac.diamond.daq.mapping.ui.experiment.saver.ScanSaver;

/**
 * Composite for use with {@link DescriptiveFilenameFactory}. Will display filenames encoded using this in 3 columns
 * showing the name, shape and shape/path parameters in a sortable searchable tree view. Filenames without the
 * descriptor section will just show in the name column. Double clicking an entry loads the file in question.
 *
 * @since GDA 9.13
 */
public class DescriptiveFilenameBrowserComposite extends Composite {
	private static final int FOUR_ROWS = 165;
	private static final int NAME_WIDTH = 250;
	private static final int SHAPE_WIDTH = 70;
	private static final int DETAIL_WIDTH = 200;

	private final TreeViewer viewer;
	private final IComparableStyledLabelProvider nameLabelProvider;
	private SavedScanMetaData selectedScan = null;

	/**
	 * Initialises the members that do not require external params.
	 */
	public DescriptiveFilenameBrowserComposite(final Composite parent, final int style) {
		super (parent, style);
		FilteredTree tree = new FilteredTree(this, SWT.V_SCROLL, new PatternFilter(), true);
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, FOUR_ROWS).applyTo(tree);
		viewer = tree.getViewer();
		viewer.getTree().setHeaderVisible(true);
		viewer.setContentProvider(new SavedScansContentProvider());
		nameLabelProvider = new SavedScanNameLabelProvider();
	}

	public void populate (ScanSaver scanSaver) {
		viewer.setInput(scanSaver.listScans());

		viewer.addDoubleClickListener(event -> {
			SavedScanMetaData scan = (SavedScanMetaData)((TreeSelection)event.getSelection()).getFirstElement();
			scanSaver.load(scan);
		});

		addColumn("Name", NAME_WIDTH, nameLabelProvider).getColumn().notifyListeners(SWT.Selection, null);
		addColumn("Shape", SHAPE_WIDTH, new SavedScanShapeLabelProvider());
		addColumn("Detail", DETAIL_WIDTH, new SavedScanDetailsLabelProvider());
		viewer.refresh();

		GridDataFactory.fillDefaults().grab(true, false).applyTo(new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL));

		final Composite buttonsComposite = new Composite(this, SWT.NONE);
		buttonsComposite.setLayout(new GridLayout(3, false));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(buttonsComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(new Label(buttonsComposite,SWT.NONE));

		Button saveButton = new Button(buttonsComposite, SWT.PUSH);
		saveButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(
				"uk.ac.diamond.daq.mapping.ui", "icons/save.png").createImage());
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				scanSaver.save();
				viewer.setInput(scanSaver.listScans());
				viewer.refresh();
			}
		});

		viewer.addSelectionChangedListener(event -> {
			selectedScan = (SavedScanMetaData)event.getStructuredSelection().getFirstElement();
		});
	    MenuManager contextMenu = new MenuManager("#ViewerMenu"); //$NON-NLS-1$
	    contextMenu.setRemoveAllWhenShown(true);
	    contextMenu.addMenuListener(mgr -> {
        	if (selectedScan != null) {
	        	mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

	            mgr.add(new Action("Load Scan") {
	                @Override
	                public void run() {
	                    if (selectedScan != null) {
	                    	scanSaver.load(selectedScan);
	            			viewer.setInput(scanSaver.listScans());
	            			viewer.refresh();
	                    }
	                }
	            });

	            mgr.add(new Action("Delete Scan") {
	                @Override
	                public void run() {
	                    if (selectedScan != null) {
	                    	scanSaver.delete(selectedScan);
	            			viewer.setInput(scanSaver.listScans());
	            			viewer.refresh();
	                    }
	                }
	            });
        	}
	    });

	    Menu menu = contextMenu.createContextMenu(viewer.getControl());
	    viewer.getControl().setMenu(menu);
	}

	/**
	 * @return the string in the Name column for the selected entry
	 */
	public String getSelectedName() {
		return nameLabelProvider.getStyledText(viewer.getStructuredSelection().getFirstElement()).toString();
	}

	/**
	 * Adds a column to the main {@link TreeViewer} using the supplied {@link IComparableStyledLabelProvider} and
	 * settings.
	 *
	 * @param name		The displayed name of the column
	 * @param width		The preferred width of the column
	 * @param provider	The provider of formatted content for cells in the column
	 * @return			The constructed {@link TreeViewerColumn}
	 */
	private TreeViewerColumn addColumn(final String name, final int width, final IComparableStyledLabelProvider provider) {
		TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(name);
		column.getColumn().setWidth(width);
		column.setLabelProvider(new DelegatingStyledCellLabelProvider(provider));
		column.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
		        viewer.setComparator(provider.getComparator());
		        sorter(column.getColumn());
			}
		});
		return column;
	}

	/**
	 * Set the currently sorted column and its sort direction
	 *
	 * @param sortColumn	The column selected as the currently sorted column
	 */
	private void sorter(TreeColumn sortColumn) {
		Tree tree = viewer.getTree();
		int direction = tree.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
		tree.setSortDirection(direction);
		tree.setSortColumn(sortColumn);
		viewer.refresh();
	}
}
