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

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

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

	private String scanFilesDir;


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

	/**
	 * Initialises fields with supplied parameters, builds the controls and adds listeners.
	 *
	 * @param filesDir			The directory to/from which the files can be saved/loaded
	 * @param optionalLoader	The function which will carry out the file loading
	 * @param optionalSaver		The function which will carry out file saving
	 */
	public void populate(final String filesDir, final Optional<Consumer<String>> optionalLoader, final Optional<Consumer<String>> optionalSaver) {
		scanFilesDir = filesDir;

		viewer.setInput(getFileList());

		optionalLoader.ifPresent(loader ->
			viewer.addDoubleClickListener(event -> {
				String filename = ((TreeSelection)event.getSelection()).getFirstElement().toString();
				loader.accept(getFilePath(filename));
			})
		);

		addColumn("Name", NAME_WIDTH, nameLabelProvider).getColumn().notifyListeners(SWT.Selection, null);
		addColumn("Shape", SHAPE_WIDTH, new SavedScanShapeLabelProvider());
		addColumn("Detail", DETAIL_WIDTH, new SavedScanDetailsLabelProvider());
		viewer.refresh();

		optionalSaver.ifPresent(saver -> {

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
			        InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
			                "Save Scan Definition", "Please enter a name for the current Scan Definition", "", null);
		            if (dlg.open() == Window.OK) {
		              saver.accept(getFilePath(dlg.getValue()));
		              viewer.setInput(getFileList());
		              viewer.refresh();
		            }
				}
			});
		});

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

	/**
	 * Retrieves a list of mapping scan files from the set directory
	 *
	 * @return	A list of files with the .map extension
	 */
	private String[] getFileList() {
		return new File(scanFilesDir).list((dir, name) -> name.endsWith(".map"));
	}

	/**
	 * Retrieves the path string for the specified filename
	 *
	 * @param filename	The filename of the requires file within its directory minus the .map extension
	 * @return			The incoming filename prefixed with the scan files directory
	 */
	private String getFilePath(final String filename) {
		return String.join("/", scanFilesDir, filename);
	}
}
