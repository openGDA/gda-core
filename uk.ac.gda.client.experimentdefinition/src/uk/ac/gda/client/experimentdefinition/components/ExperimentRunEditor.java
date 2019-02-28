/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.richbeans.widgets.cell.SpinnerCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.ExperimentObjectEvent;
import uk.ac.gda.client.experimentdefinition.IExperimentBeanDescription;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.ui.modifiers.DoubleClickModifier;

/**
 * <link>http://www.eclipse.org/articles/Article-Table-viewer/table_viewer.html</link>
 */
public class ExperimentRunEditor extends EditorPart implements ExperimentObjectListener {
	public ExperimentRunEditor() {
	}

	private final static Logger logger = LoggerFactory.getLogger(ExperimentRunEditor.class);

	public static final String ID = "gda.gui.eclipse.editors.scan.experimentdefinition.ExperimentRunEditor";

	private IExperimentObjectManager runObjectManager;
	private TableViewer tableViewer;

	@Override
	public void createPartControl(final Composite parent) {

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());

		final Table table = new Table(container, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		table.setLayoutData(gridData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		this.tableViewer = new TableViewer(table);
		createColumns(tableViewer);

		tableViewer.setContentProvider(createContentProvider());
		tableViewer.setInput(runObjectManager);

		final List<?> runs = runObjectManager.getExperimentList();
		if (runs != null && runs.size() > 0) {
			Object selection = runs.get(runs.size() - 1);
			if (selection != null) {
				tableViewer.setSelection(new StructuredSelection(selection));
			}
		}

		createRightClickMenu();

		getSite().setSelectionProvider(tableViewer);

	}

	@Override
	public void dispose() {
		super.dispose();
		runObjectManager.removeExperimentObjectListener(this);
		if (getSite().getSelectionProvider() != null) {
			((Viewer) getSite().getSelectionProvider()).getControl().dispose();
		}
	}

	private void createRightClickMenu() {
		final MenuManager menuManager = new MenuManager();
		tableViewer.getControl().setMenu(menuManager.createContextMenu(tableViewer.getControl()));
		getSite().registerContextMenu(menuManager, tableViewer);
	}

	private IContentProvider createContentProvider() {
		return new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return runObjectManager.getExperimentList().toArray(new IExperimentObject[0]);
			}
		};
	}

	private void createColumns(TableViewer table) {

		ColumnViewerToolTipSupport.enableFor(table, ToolTip.NO_RECREATE);

		// Stupidly you have to set width on a column to see it in the table.
		final TableViewerColumn name = new TableViewerColumn(table, SWT.LEFT, 0);
		name.getColumn().setText("Name");
		name.getColumn().setWidth(150);
		name.setLabelProvider(new RunColumnLabelProvider());
		name.setEditingSupport(new RunColumnEditingSupport(table));

		final String sizeColumn = System.getProperty("gda.exafs.run.editor.column.width");
		final int width = sizeColumn != null ? Integer.parseInt(sizeColumn) : 200;

		List<IExperimentBeanDescription> beanTypes = ExperimentBeanManager.INSTANCE.getBeanDescriptions();
		String[] columnNames = runObjectManager.getOrderedColumnBeanTypes();

		int columnsMade = 0;
		for (int index = 0; index < columnNames.length; index++){
			final String columnName = columnNames[index];
			boolean beanDescriptionFound = false;
			for (IExperimentBeanDescription type : beanTypes) {
				if (type.includeInNew() && columnName.equals(type.getBeanType())) {
					logger.debug("Creating table column for {}", type.getBeanType());
					final TableViewerColumn thisColumn = new TableViewerColumn(table, SWT.LEFT, index+1);
					thisColumn.getColumn().setText(type.getBeanType());
					thisColumn.getColumn().setWidth(width);
					thisColumn.setLabelProvider(new XmlNameLabelProvider(type));
					thisColumn.setEditingSupport(new XmlNameEditingSupport(table, type));
					columnsMade++;
					beanDescriptionFound = true;
					break;
				}
			}
			if (!beanDescriptionFound) {
				logger.error("No experiment bean description found for {}", columnName);
			}
		}

		final TableViewerColumn runNum = new TableViewerColumn(table, SWT.LEFT, columnsMade + 1);
		runNum.getColumn().setText("Repetitions");
		runNum.getColumn().setWidth(30);
		runNum.setLabelProvider(new RepetitionsColumnLabelProvider());
		runNum.setEditingSupport(new NumRepetitionsEditingSupport(table));
	}

	private abstract class BaseColumnLabelProvider extends ColumnLabelProvider {

		private final Color black = SWTResourceManager.getColor(SWT.COLOR_BLACK);

		@Override
		public Color getForeground(Object element) {
			return black;
		}

		@Override
		public Point getToolTipShift(Object object) {
			return new Point(5, 5);
		}

		@Override
		public int getToolTipDisplayDelayTime(Object object) {
			return 50;
		}

		@Override
		public int getToolTipTimeDisplayed(Object object) {
			return 10000;
		}
	}

	private class RunColumnLabelProvider extends BaseColumnLabelProvider {
		final Image runFileIcon = SWTResourceManager.getImage(ExperimentRunEditor.class, "/chart_line.png");

		@Override
		public Image getImage(Object element) {
			return runFileIcon;
		}

		@Override
		public String getText(Object element) {
			return ((IExperimentObject) element).getRunName();
		}
	}

	private class RepetitionsColumnLabelProvider extends BaseColumnLabelProvider {
		@Override
		public String getText(Object element) {
			return ((IExperimentObject) element).getNumberRepetitions().toString();
		}
	}


	@Override
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			this.runObjectManager.write();
		} catch (Exception e) {
			logger.error("Cannot save run", e);
		}
	}

	@Override
	public void doSaveAs() {
		// Do the Save As operation
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public void runChangePerformed(ExperimentObjectEvent e) {
		if (tableViewer != null && !tableViewer.getTable().isDisposed()) {
			this.getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					tableViewer.refresh();
				}
			});
		}
	}

	@Override
	public void setInput(final IEditorInput input) {

		super.setInput(input);

		final IFile file = ExperimentFactory.getExperimentEditorManager().getIFile(input);
		setPartName(file.getName());
		try {
			this.runObjectManager = ExperimentFactory.getManager(file);
			runObjectManager.addExperimentObjectListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Returns the folder selected
	 *
	 * @return the folder.
	 */
	public IFolder getCurrentDirectory() {
		return runObjectManager.getContainingFolder();
	}

	public IFile getScanFile() {
		return runObjectManager.getFile();
	}

	public IExperimentObject getSelectedRun() {
		final IStructuredSelection sel = (IStructuredSelection) tableViewer.getSelection();
		if (sel == null)
			return null;
		return (IExperimentObject) tableViewer.getElementAt(tableViewer.getTable().getItemCount()-1);
	}

	public IExperimentObjectManager getRunObjectManager() {
		return runObjectManager;
	}

	public void editRunName(IExperimentObject created) {
		tableViewer.refresh();
		((DoubleClickModifier) tableViewer.getCellModifier()).setEnabled(true);
		tableViewer.editElement(created, 0);
	}

	public void editSelectedElementName() {
		final IExperimentObject sel = getSelectedRun();
		if (sel == null)
			return;

		((DoubleClickModifier) tableViewer.getCellModifier()).setEnabled(true);
		tableViewer.editElement(sel, 0);
	}

	public void editSelectedElementRepetitions(final Object element) {

		((DoubleClickModifier) tableViewer.getCellModifier()).setEnabled(true);
		tableViewer.editElement(element, 5);
	}

	public boolean isFocus() {
		return tableViewer != null && !tableViewer.getTable().isDisposed() &&
				tableViewer.getTable().isFocusControl();
	}

	/**
	 * Label provider for showing the XML filename column
	 */
	private class XmlNameLabelProvider extends BaseColumnLabelProvider {
		private final IExperimentBeanDescription beanDescription;

		public XmlNameLabelProvider(IExperimentBeanDescription beanDescription) {
			this.beanDescription = beanDescription;
		}

		@Override
		public String getText(Object element) {
			IExperimentObject opf = (IExperimentObject) element;
			return opf.getFile(beanDescription.getBeanType()).getName();
		}
	}

	/**
	 *  Editing support for choosing an XML filename using a combo box
	 */
	private class XmlNameEditingSupport extends EditingSupport {

		private final IExperimentBeanDescription beanDescription;
		private String[] xmlFileNamesForCombo;

		public XmlNameEditingSupport(ColumnViewer viewer, IExperimentBeanDescription beanDescription) {
			super(viewer);
			this.beanDescription = beanDescription;
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			IExperimentObject expObject = (IExperimentObject) element;

			// Get list of files matching the bean type for this column
			List<IFile> filesOfBean = new ArrayList<>();
			final List<IExperimentBeanDescription> beanDescriptions = ExperimentBeanManager.INSTANCE.getBeanDescriptions();
			beanDescriptions.forEach(description -> {
				if (description.getBeanType().equals(beanDescription.getBeanType())) {
					filesOfBean.addAll(description.getSortedFileList(expObject.getFolder()));
				}
			});

			// Make array of filenames to be shown in combo box.
			xmlFileNamesForCombo = new String[filesOfBean.size()];
			for (int i = 0; i < xmlFileNamesForCombo.length; i++) {
				xmlFileNamesForCombo[i] = FilenameUtils.getName(filesOfBean.get(i).getName());
			}

			return new ComboBoxCellEditor((Composite) getViewer().getControl(), xmlFileNamesForCombo, SWT.READ_ONLY);
		}

		@Override
		protected boolean canEdit(Object ob) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			// get value from model and convert to int to update combobox
			IExperimentObject param = (IExperimentObject) element;
			String filename = param.getFile(beanDescription.getBeanType()).getName();
			int index = Arrays.asList(xmlFileNamesForCombo).indexOf(filename);
			logger.debug("XmlNameEditingSupport.getValue() : index = {}, name = {}", index, filename);
			return index;
		}

		@Override
		protected void setValue(Object element, Object value) {
			// Update model using selected item in filename combo box
			IExperimentObject param = (IExperimentObject) element;
			int index = (Integer) value; // selected index in combo box
			String xmlName = xmlFileNamesForCombo[index];
			logger.debug("XmlNameEditingSupport.setValue() : index = {}, xmlName = {}", index, xmlName);
			param.setFileName(beanDescription.getBeanType(), xmlName);
			getViewer().update(param, null);
			runObjectManager.write();
		}
	}

	/**
	 * Editing support for altering the number of repetitions (using {@link SpinnerCellEditor})
	 */
	private class NumRepetitionsEditingSupport extends EditingSupport {

		public NumRepetitionsEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new SpinnerCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			// get value from model and convert to int to update spinner widget
			IExperimentObject param = (IExperimentObject) element;
			return param.getNumberRepetitions();
		}

		@Override
		public void setValue(Object element, Object value) {
			// Update model using value from spinner widget
			IExperimentObject param = (IExperimentObject) element;
			int numReps = Integer.parseInt(value.toString());
			param.setNumberRepetitions(numReps);
			getViewer().update(param, null);
			ExperimentFactory.getExperimentEditorManager().refreshViewers();
			runObjectManager.write();
		}
	}

	/**
	 * Editing support for changing the scan run name.
	 */
	private class RunColumnEditingSupport extends EditingSupport {

		public RunColumnEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			IExperimentObject param = (IExperimentObject) element;
			return param.getRunName();
		}

		@Override
		public void setValue(Object element, Object value) {
			// Update model using value from 'run name' widget
			IExperimentObject param = (IExperimentObject) element;
			param.setRunName(value.toString());
			getViewer().update(param, null);
			// refresh the tree view so the displayed scan name is up to date with the model
			ExperimentFactory.getExperimentEditorManager().refreshViewers();
			runObjectManager.write();
		}
	}
}
