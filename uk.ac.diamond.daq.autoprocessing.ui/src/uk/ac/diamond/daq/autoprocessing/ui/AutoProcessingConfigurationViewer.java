/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.autoprocessing.ui;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.richbeans.widgets.cell.CComboCellEditor;
import org.eclipse.richbeans.widgets.cell.NumberCellEditor;
import org.eclipse.richbeans.widgets.file.FileDialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.autoprocessing.ui.AutoProcessingField.AutoProcEditorHint;

public class AutoProcessingConfigurationViewer extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(AutoProcessingConfigurationViewer.class);

	private TableViewer viewer;

	public AutoProcessingConfigurationViewer(Composite parent) {
		super(parent, SWT.NONE);
		createViewer();
	}

	private void createViewer() {

		this.setLayout(new FillLayout());
		Composite tableComposite = new Composite(this, SWT.NONE);
		tableComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		this.viewer = new TableViewer(tableComposite, SWT.SINGLE | SWT.BORDER);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		ColumnViewerToolTipSupport.enableFor(viewer);

		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewer,
				new FocusCellOwnerDrawHighlighter(viewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(viewer) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return super.isEditorActivationEvent(event)
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
								&& (event.keyCode == KeyLookupFactory.getDefault()
										.formalKeyLookup(IKeyLookup.ENTER_NAME)));
			}
		};

		TableViewerEditor.create(viewer, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

		TableColumnLayout columnLayout = new TableColumnLayout();
		createColumns(viewer, columnLayout);

		tableComposite.setLayout(columnLayout);
	}

	private void createColumns(TableViewer viewer, TableColumnLayout columnLayout) {

		TableViewerColumn name = new TableViewerColumn(viewer, SWT.LEFT, 0);
		name.getColumn().setText("Name");
		name.getColumn().setWidth(200);
		name.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((AutoProcessingField<?>) element).getLabel();
			}

			@Override
			public String getToolTipText(Object element) {
				return ((AutoProcessingField<?>) element).getDescription();
			}
		});

		columnLayout.setColumnData(name.getColumn(), new ColumnWeightData(25, 20));

		TableViewerColumn val = new TableViewerColumn(viewer, SWT.LEFT, 1);
		val.getColumn().setText("Value");
		val.getColumn().setWidth(200);
		val.setLabelProvider(new AutoProcFieldLabelProvider());
		val.setEditingSupport(new AutoProcFieldEditingSupport(viewer));

		columnLayout.setColumnData(val.getColumn(), new ColumnWeightData(75, 20));

		TableViewerColumn unit = new TableViewerColumn(viewer, SWT.LEFT, 2);
		unit.getColumn().setText("Unit");
		unit.getColumn().setWidth(200);
		unit.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((AutoProcessingField<?>) element).getUnit();
			}
		});

		columnLayout.setColumnData(unit.getColumn(), new ColumnWeightData(25, 20));

	}

	public void setInput(AutoProcessingField<?>[] fields) {
		viewer.setInput(fields);
	}

	class AutoProcFieldEditingSupport extends EditingSupport {

		public AutoProcFieldEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return createEditor((AutoProcessingField<?>) element, viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((AutoProcessingField<?>) element).getValue();
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected void setValue(Object element, Object value) {
			try {
				AutoProcessingField field = (AutoProcessingField) element;
				field.setValue(value);
				viewer.refresh();
			} catch (Exception e) {
				logger.error("Could not set field value", e);
			}
		}
	}

	public CellEditor createEditor(AutoProcessingField<?> field, Composite parent) {

		Object value = field.getValue();

		Class<? extends Object> clazz = value.getClass();

		if (clazz == Boolean.class) {

			return new CheckboxCellEditor(parent, SWT.NONE);

		} else if (Number.class.isAssignableFrom(clazz)) {

			return getNumberEditor(clazz, parent);

		} else if (field.getOptions() != null && clazz == String.class) {

			return getChoiceEditor(field, parent);

		} else if (field.getEditorHint() == AutoProcEditorHint.FILE && clazz == String.class) {

			FileDialogCellEditor fe = new FileDialogCellEditor(parent);
			fe.setValueClass(clazz);
			fe.setDirectory(false);
			return fe;

		} else if (field.getEditorHint() == AutoProcEditorHint.XRF && clazz == String.class) {

			return new XRFLinesCellEditor(parent);

		} else {

			return new TextCellEditor(parent);
		}


	}

	private CellEditor getChoiceEditor(AutoProcessingField<?> field, Composite parent) {

		final String[] items = field.getOptions();

		return this.new EnumCellEditor(parent, items);
	}

	private CellEditor getNumberEditor(final Class<? extends Object> clazz, Composite parent) {

		NumberCellEditor textEd = new NumberCellEditor(parent, clazz, SWT.NONE);
		textEd.setAllowInvalidValues(true);

		return textEd;
	}

	class AutoProcFieldLabelProvider extends ColumnLabelProvider {

		private Image ticked;
		private Image unticked;

		@Override
		public Image getImage(Object ofield) {

			if (ofield == null)
				return null;

			AutoProcessingField<?> field = (AutoProcessingField<?>) ofield;
			Object element = field.getValue();
			if (element instanceof Boolean) {
				if (ticked == null)
					ticked = Activator.getImageDescriptor("icons/ticked.png").createImage();
				if (unticked == null)
					unticked = Activator.getImageDescriptor("icons/unticked.gif").createImage();
				return (Boolean) element ? ticked : unticked;
			}
			return null;
		}

		@Override
		public String getText(Object ofield) {

			if (ofield == null)
				return "";

			AutoProcessingField<?> field = (AutoProcessingField<?>) ofield;
			Object element = field.getValue();
			if (element == null)
				return "";
			if (element instanceof Boolean)
				return "";

			return element.toString();
		}

		@Override
		public void dispose() {
			if (ticked != null)
				ticked.dispose();
			if (unticked != null)
				unticked.dispose();
			super.dispose();
		}
	}

	private class EnumCellEditor extends CComboCellEditor {

		public EnumCellEditor(Composite parent, String[] items) {
			super(parent, items);
		}


		@Override
		protected void doSetValue(Object value) {
			if (value instanceof Enum)
				value = ((Enum<?>) value).ordinal();
			super.doSetValue(value);
		}

		@Override
		protected Object doGetValue() {
			Integer ordinal = (Integer) super.doGetValue();
			return items[ordinal];
		}

	}
}
