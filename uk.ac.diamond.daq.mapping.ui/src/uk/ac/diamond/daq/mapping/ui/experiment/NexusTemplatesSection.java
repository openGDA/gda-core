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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;

import uk.ac.diamond.daq.mapping.api.TemplateFileWrapper;
import uk.ac.diamond.daq.mapping.ui.Activator;

/**
 * An section of the mapping view for choosing template files to be applied to the
 * Nexus file produced from the mapping scan.
 */
public class NexusTemplatesSection extends AbstractMappingSection {

	private String initialPath;

	private static final Image ticked = Activator.getImage("icons/ticked.png");
	private static final Image unticked = Activator.getImage("icons/unticked.gif");

	private Composite fileTableComposite;

	private TableViewer fileTableViewer;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		// create the composite
		final Composite templatesComposite = createComposite(parent, 1, true, true);

		// create the title & Add button
		createTitleRow(templatesComposite);

		// create table containing file names and activation state
		createFileTable(templatesComposite);
	}

	/**
	 * Title and button to add a template file
	 */
	private void createTitleRow(Composite parent) {
		final Composite titleRowComposite = createComposite(parent, 2, false);

		final Label titleLabel = new Label(titleRowComposite, SWT.NONE);
		titleLabel.setText("Template files");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(titleLabel);

		final Button addFileButton = new Button(titleRowComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(addFileButton);
		addFileButton.setText("Add file...");
		addFileButton.setToolTipText("Add a Nexus template file");
		addFileButton.addSelectionListener(widgetSelectedAdapter(e -> addTemplateFile()));
	}

	/**
	 * Table of files that are defined, with a check box to indicate whether each one is active.
	 */
	private void createFileTable(Composite parent) {
		fileTableComposite = createComposite(parent, 1, false, true);

		fileTableViewer = new TableViewer(fileTableComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		final Table fileTable = fileTableViewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fileTable);
		fileTable.setHeaderVisible(true);
		ColumnViewerToolTipSupport.enableFor(fileTableViewer);
		fileTableViewer.setContentProvider(new ArrayContentProvider());

		// Check box column
		final TableViewerColumn checkboxColumn = new TableViewerColumn(fileTableViewer, SWT.CENTER, 0);
		checkboxColumn.setEditingSupport(new CheckBoxEditSupport());
		checkboxColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}

			@Override
			public Image getImage(Object element) {
				return ((TemplateFileWrapper) element).isActive() ? ticked : unticked;
			}

		});
		checkboxColumn.getColumn().setWidth(28);

		// File name column
		final TableViewerColumn fileNameColumn = new TableViewerColumn(fileTableViewer, SWT.LEFT);
		fileNameColumn.getColumn().setText("Name");
		fileNameColumn.getColumn().setWidth(200);
		fileNameColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				final String filePathString = ((TemplateFileWrapper) element).getFilePath();
				return Paths.get(filePathString).getFileName().toString();
			}

			@Override
			public String getToolTipText(Object element) {
				return ((TemplateFileWrapper) element).getFilePath();
			}
		});

		// Context menu to remove a row from the table
		final MenuManager menuManager = new MenuManager();
		menuManager.add(new Action("Remove") {
			@Override
			public void run() {
				final ISelection selection = fileTableViewer.getSelection();
				if (selection instanceof StructuredSelection) {
					final List<TemplateFileWrapper> filesToRemove = new ArrayList<>();
					@SuppressWarnings({ "unchecked" })
					final Iterator<StructuredSelection> iterator = ((StructuredSelection) selection).iterator();
					while (iterator.hasNext()) {
						final Object nextSelection = iterator.next();
						if (nextSelection instanceof TemplateFileWrapper) {
							filesToRemove.add((TemplateFileWrapper) nextSelection);
						}
					}
					if (!filesToRemove.isEmpty()) {
						getBean().getTemplateFiles().removeAll(filesToRemove);
					}

					getView().updateControls();
				}
			}
		});

		final Menu menu = menuManager.createContextMenu(fileTableViewer.getControl());
		fileTableViewer.getTable().setMenu(menu);

		// Initialise from mapping bean
		final List<TemplateFileWrapper> files = getBean().getTemplateFiles();
		fileTableViewer.setInput(files.toArray());
		setTableSize();
	}

	@Override
	public void updateControls() {
		List<TemplateFileWrapper> templateFiles = getBean().getTemplateFiles();
		if (templateFiles == null) {
			templateFiles = Collections.emptyList();
		}

		fileTableViewer.setInput(templateFiles);
		fileTableViewer.refresh();

		setTableSize();
		fileTableComposite.getParent().layout(true, true);
		relayoutView();
	}

	/**
	 * Show a pop-up dialog to add a template file
	 */
	private void addTemplateFile() {
		final FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[] { "*.yaml" });
		dialog.setFilterNames(new String[] { "YAML files(*.yaml)" });
		if (initialPath != null) {
			dialog.setFilterPath(initialPath);
		}

		final String result = dialog.open();
		if (result == null) {
			return;
		}
		initialPath = Paths.get(result).getParent().toString();

		// Add file if it does not already exist
		if (getBean().getTemplateFiles().stream().anyMatch(f -> f.getFilePath().equals(result))) {
			return;
		}
		getBean().addTemplateFile(new TemplateFileWrapper(result, true));
		getView().updateControls();
		getView().showControl(fileTableComposite.getParent());
	}

	/**
	 * Resize the table to fit the data, subject to a maximum size: table will scroll to view all items
	 */
	private void setTableSize() {
		final int maxItems = 5;
		if (fileTableComposite.getLayoutData() instanceof GridData) {
			final int itemCount = Math.min(fileTableViewer.getTable().getItemCount(), maxItems);
			final int itemHeight = fileTableViewer.getTable().getItemHeight();
			final int headerHeight = fileTableViewer.getTable().getHeaderHeight();

			final GridData gd = (GridData) fileTableComposite.getLayoutData();
			final int h = (1 + itemCount) * itemHeight + headerHeight;
			gd.minimumHeight = h;
			gd.heightHint = h;
		}
	}

	public void setInitialPath(String initialPath) {
		this.initialPath = initialPath;
	}

	/**
	 * Control the editing of a file's active state
	 */
	private class CheckBoxEditSupport extends EditingSupport {

		public CheckBoxEditSupport() {
			super(fileTableViewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			final CheckboxCellEditor edit = new CheckboxCellEditor(fileTableViewer.getTable());
			edit.setValue(((TemplateFileWrapper) element).isActive());
			return edit;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof TemplateFileWrapper) {
				return ((TemplateFileWrapper) element).isActive();
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof TemplateFileWrapper && value instanceof Boolean) {
				((TemplateFileWrapper) element).setActive((Boolean) value);
			}
			getViewer().refresh();
		}
	}
}
