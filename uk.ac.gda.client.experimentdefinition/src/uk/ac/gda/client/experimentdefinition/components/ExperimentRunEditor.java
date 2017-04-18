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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.richbeans.widgets.cell.SpinnerCellEditor;
import org.eclipse.richbeans.xml.cell.XMLChooserEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.model.WorkbenchLabelProvider;
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
import uk.ac.gda.common.rcp.CommonRCPActivator;
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

		tableViewer.setUseHashlookup(true);
		 tableViewer.setColumnProperties(new String[] { "Run Name", "Scan File Name",
		 "Detector File Name", "Sample File Name", "Output File Name", "Number Repetitions" });
		tableViewer.setCellEditors(createCellEditors(tableViewer));
		tableViewer.setCellModifier(createModifier(tableViewer));
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

	private ICellModifier createModifier(final TableViewer tableViewer) {
		return new DoubleClickModifier(tableViewer) {
//			@Override
//			public boolean canModify(Object element, String property) {
//				if (element instanceof IExperimentObject) {
//					if (((IExperimentObject) element).getExperimentStatus() == ExperimentStatus.RUNNING)
//						return false;
//				}
//				return super.canModify(element, property);
//			}

			@Override
			public Object getValue(Object element, String property) {
				try {
					property = property.replace(" ", "");
					final Method method = element.getClass().getMethod("get" + property);
					return method.invoke(element);
				} catch (Exception e) {
					logger.error("Cannot get " + property, e);
					return null;
				}
			}

			@Override
			public void modify(Object item, String property, Object value) {
				try {
					if (item == null)
						return;
					final TableItem tableItem = (TableItem) item;
					final Object element = tableItem.getData();
					final Object existVal = getValue(element, property);
					if (existVal != null && existVal.equals(value))
						return; // No change

					// No spaces
					if (property.equals("Run Name") && value != null && ((String) value).indexOf(' ') > -1) {
						MessageDialog.openError(getSite().getShell(), "Contains a space", property
								+ " must not contain a space.");
						return;
					}

					property = property.replace(" ", "");
					if (!property.equals("RunName") && !property.equals("NumberRepetitions")) {
						if (value == null)
							return;
						if ("".equals(value))
							return;
					} else {
						if (value == null || "".equals(value) || !value.toString().matches("\\w+"))
							return;
					}

					final Method method = element.getClass().getMethod("set" + property, value.getClass());
					method.invoke(element, value);
					tableViewer.refresh();

					runObjectManager.write();

					if (property.equals("NumberRepetitions") || property.equals("RunName")) {
						try {
							ExperimentFactory.getExperimentEditorManager().refreshViewers();
						} catch (Exception ne) {
							logger.error("Cannot estimate time of scan - server error.", ne);
						}
					}
				} catch (Exception e) {
					logger.error("Cannot set " + property, e);
				} finally {
					setEnabled(false);
				}
			}
		};
	}

	private CellEditor[] createCellEditors(final TableViewer tableViewer) {
		CellEditor[] editors = new CellEditor[1];
		Table table = tableViewer.getTable();
		TextCellEditor nameEd = new TextCellEditor(table);
		((Text) nameEd.getControl()).setTextLimit(60);
		// NOTE Must not add verify listener - it breaks things.
		editors[0] = nameEd;
		try {
			IFolder containingFolder = runObjectManager.getContainingFolder();
			final List<IExperimentBeanDescription> beanTypes = ExperimentBeanManager.INSTANCE.getBeanDescriptions();
			String[] columnNames = runObjectManager.getOrderedColumnBeanTypes();
			for (int index = 0; index < columnNames.length; index++){
				String columnName = columnNames[index];
				for (IExperimentBeanDescription type : beanTypes) {
					if (type.includeInNew() && columnName.equals(type.getBeanType())) {

					IExperimentBeanDescription[] beanTypesToFilterOn = new IExperimentBeanDescription[0];
					for (IExperimentBeanDescription beanType : beanTypes) {
						if (beanType.getBeanType().equals(type.getBeanType())) {
							beanTypesToFilterOn = (IExperimentBeanDescription[]) ArrayUtils.add(beanTypesToFilterOn,
									beanType);
						}
					}

					editors = (CellEditor[]) ArrayUtils.add(editors, new XMLChooserEditor(table, containingFolder,
							beanTypesToFilterOn));
					break;
				}
				}
			}
		} catch (Exception e1) {
			logger.error("Could not create editors for Run Editor View.", e1);
		}

		SpinnerCellEditor repEd = new SpinnerCellEditor(table);
		repEd.setMaximum(999);
		repEd.setMinimum(1);
		editors = (CellEditor[]) ArrayUtils.add(editors, repEd);

		return editors;
	}

	private void createColumns(TableViewer table) {

		ColumnViewerToolTipSupport.enableFor(table, ToolTip.NO_RECREATE);

		// Stupidly you have to set width on a column to see it in the table.
		final TableViewerColumn name = new TableViewerColumn(table, SWT.LEFT, 0);
		name.getColumn().setText("Name");
		name.getColumn().setWidth(150);
		name.setLabelProvider(new RunColumnLabelProvider());

		final String sizeColumn = System.getProperty("gda.exafs.run.editor.column.width");
		final int width = sizeColumn != null ? Integer.parseInt(sizeColumn) : 200;

		List<IExperimentBeanDescription> beanTypes = ExperimentBeanManager.INSTANCE.getBeanDescriptions();
		String[] columnNames = runObjectManager.getOrderedColumnBeanTypes();

		int columnsMade = 0;
		for (int index = 0; index < columnNames.length; index++){
			String columnName = columnNames[index];
			for (IExperimentBeanDescription type : beanTypes) {
				if (type.includeInNew() && columnName.equals(type.getBeanType())) {
					TableViewerColumn thisColumn = new TableViewerColumn(table, SWT.LEFT,
							index+1);
					thisColumn.getColumn().setText(type.getBeanType());
					thisColumn.getColumn().setWidth(width);
					thisColumn.setLabelProvider(new BeanColumnLabelProvider(type));
					columnsMade++;
					break;
				}
			}
		}

		final TableViewerColumn runNum = new TableViewerColumn(table, SWT.LEFT, columnsMade + 1);
		runNum.getColumn().setText("Repetitions");
		runNum.getColumn().setWidth(30);
		runNum.setLabelProvider(new RepetitionsColumnLabelProvider());
	}

	private abstract class BaseColumnLabelProvider extends ColumnLabelProvider {

//		@Override
//		public String getToolTipText(Object element) {
//			if (element instanceof IExperimentObject) {
//				final IExperimentObject ob = (IExperimentObject) element;
//				if (ob.getExperimentStatus() == ExperimentStatus.RUNNING) {
//					return "'" + ob.getRunName() + "' is currently being run and cannot be edited.";
//				}
//			}
//			return null;
//		}

		private final Color black = SWTResourceManager.getColor(SWT.COLOR_BLACK);
//		private final Color grey = SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY);

		@Override
		public Color getForeground(Object element) {
//			if (element instanceof IExperimentObject) {
//				final IExperimentObject ob = (IExperimentObject) element;
//				if (ob.getExperimentStatus() == ExperimentStatus.RUNNING) {
//					return grey;
//				}
//			}
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

	private class BeanColumnLabelProvider extends BaseColumnLabelProvider {

		final Image errorIcon = SWTResourceManager.getImage(ExperimentRunEditor.class, "/page_error.png");
		private ILabelProvider workbenchLabelProvider;
		private IExperimentBeanDescription type;
		ILabelProviderListener listener;

		BeanColumnLabelProvider(final IExperimentBeanDescription type) {
			Assert.isNotNull(type);
			this.type = type;
			workbenchLabelProvider = new DecoratingLabelProvider(new WorkbenchLabelProvider(), CommonRCPActivator
					.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator());

			listener = new ILabelProviderListener() {
				@Override
				public void labelProviderChanged(LabelProviderChangedEvent event) {
					Set<IExperimentObject> scanObjectsToUpdate = new HashSet<IExperimentObject>();
					Object[] elements = event.getElements();
					for (Object object : elements) {
						if (object instanceof IFile) {
							for (IExperimentObject scanObject : runObjectManager.getExperimentList()) {
								IFile file = scanObject.getFile(type.getBeanType());
								if (file != null && file.equals(object)) {
									scanObjectsToUpdate.add(scanObject);
								}
							}
						}
					}
					tableViewer.update(scanObjectsToUpdate.toArray(), null);
				}
			};
			workbenchLabelProvider.addListener(listener);
		}

		@Override
		public Image getImage(Object element) {
			IFile file = ((IExperimentObject) element).getFile(type.getBeanType());
			Image image = workbenchLabelProvider.getImage(file);
			if (image == null) {
				image = errorIcon;
			}
			return image;
		}

		@Override
		public String getText(Object element) {
			final IExperimentObject ob = (IExperimentObject) element;
			final IExperimentObjectManager man = ExperimentFactory.getManager(ob);
			final int index = man.getExperimentList().indexOf(ob);
			IExperimentObject prev = null;
			if (index > 0) {
				prev = man.getExperimentList().get(index - 1);
			}

			String label = getName(prev, ob, type);

			if (label == null)
				return "";
			return label;
		}

		@Override
		public String getToolTipText(Object element) {
			String toolTipText = super.getToolTipText(element);
			if (toolTipText == null) {
				toolTipText = type.getBeanType() + " File";
			}
//			if (element instanceof IExperimentObject) {
//				final IExperimentObject ob = (IExperimentObject) element;
//				if (ob.getExperimentStatus() == ExperimentStatus.RUNNING) {
//					return "'" + ob.getRunName() + "' is currently being run and cannot be edited.";
//				}
//			}

			return toolTipText;
		}

		@Override
		public void dispose() {
			workbenchLabelProvider.removeListener(listener);
			workbenchLabelProvider.dispose();
			super.dispose();
		}

	}

	private class RepetitionsColumnLabelProvider extends BaseColumnLabelProvider {
		@Override
		public String getText(Object element) {
			return ((IExperimentObject) element).getNumberRepetitions().toString();
		}
	}

	private String getName(IExperimentObject prev, IExperimentObject ob, IExperimentBeanDescription type) {
		final String value = ob.getFileName(type.getBeanType());
		if (value == null)
			return "";
		if (prev == null)
			return value;

		if (System.getProperty("gda.exafs.use.quotes.for.repeated.runs") == null)
			return value;
		final String prevValue = prev.getFileName(type.getBeanType());
		if (prevValue != null) {
			if (!prevValue.equals(value))
				return value;
		}
		return "\"";

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

}
