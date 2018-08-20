/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.richbeans.widgets.cell.SpinnerCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import gda.rcp.GDAClientActivator;
import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean.ParameterValue;

public class SpreadsheetViewTable {

	private TableViewer viewer;
	private List<String> xmlFiles = new ArrayList<>();
	private SpreadsheetViewConfig viewConfig;

	public SpreadsheetViewTable(Composite parent, int style) {
		viewer = new TableViewer(parent, style);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		// set layout on the Table so it fills rest of composite
		viewer.getTable().setLayout(new FillLayout());
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		setupForCursorNavigation(viewer);
	}

	public TableViewer getTableViewer() {
		return viewer;
	}

	/**
	 * Refresh the view with latest information from the model.
	 */
	public void refresh() {
		viewer.refresh();
	}

	/**
	 * Adjust column widths so that column titles and values in each column of each row are fully visible.
	 */
	public void adjustColumnWidths() {
		GC gc = new GC(viewer.getControl());

		TableItem[] rowItems = viewer.getTable().getItems();
		TableColumn[] tableColumns = viewer.getTable().getColumns();
		int[] maxDataWidth = new int[tableColumns.length];

		// Determine the maximum width of each column across values in all rows.
		if (rowItems != null) {
			for (TableItem rowData : rowItems) {
				ParametersForScan param = ((ParametersForScan) rowData.getData());
				List<String> columnText = param.getTextForTableColumns();
				for (int i = 0; i < columnText.size(); i++) {
					maxDataWidth[i] = Math.max(maxDataWidth[i], gc.stringExtent(columnText.get(i)).x);
				}
			}
		}

		// Set column width to max of title width, and data column width
		for(int i=0; i<tableColumns.length; i++) {
			tableColumns[i].pack();
			int titleWidth = gc.stringExtent(tableColumns[i].getText()).x;
			int widthForColumn = Math.max(titleWidth, maxDataWidth[i]);

			tableColumns[i].setWidth(widthForColumn+20);
		}
		gc.dispose();
	}

	/**
	 * Add columns to table : for each scan file add column with combo box for file selection, followed
	 * by zero or more columns to contain values of selected parameters modifiers.
	 * @param parametersForScanBeans
	 */
	public void addColumnsToTable(List<ParameterValuesForBean> parametersForScanBeans) {
		int typeIndex=0;
		int minWidth = 75;
		for(ParameterValuesForBean parametersForBean : parametersForScanBeans) {

			TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
			parametersForBean.getBeanTypeNiceName();
			column.setText(parametersForBean.getBeanTypeNiceName());
			column.setWidth(minWidth);
			TableViewerColumn columnViewer = new TableViewerColumn(viewer, column);
			columnViewer.setLabelProvider(new XmlNameLabelProvider(typeIndex));
			columnViewer.setEditingSupport(new XmlNameEditingSupport(viewer, typeIndex));

			// Add column for each selected parameter modifier
			int paramIndex=0;
			for(ParameterValue parameter : parametersForBean.getParameterValues() ) {
				addColumnForParameter(parametersForBean, parameter, typeIndex, paramIndex);
				paramIndex++;
			}
			typeIndex++;
		}

		TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
		column.setText("Number of repetitions");
		column.setWidth(minWidth);
		TableViewerColumn columnViewer = new TableViewerColumn(viewer, column);
		columnViewer.setLabelProvider(new NumRepetitionsLabelProvider());
		columnViewer.setEditingSupport(new NumRepetitionsEditingSupport(viewer));
	}

	private void addColumnForParameter(ParameterValuesForBean scanFile, ParameterValue valueForBean, int typeIndex, int paramIndex) {

		// Set title for the column
		String title = valueForBean.getFullPathToGetter().replace("get", "").replace(".is",".").replace(".", "\n");
		ParameterConfig paramConfig = null;
		if (viewConfig != null) {
			paramConfig = viewConfig.getParameter(scanFile.getBeanType(), valueForBean.getFullPathToGetter());
			if (paramConfig != null) {
				title = paramConfig.getDescription();
			}
		}

		TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
		column.setText(title);
		column.setWidth(100);
		TableViewerColumn columnViewer = new TableViewerColumn(viewer, column);

		if (paramConfig != null && paramConfig.getAllowedValues().length>0) {
			// Use combo box/check box to set the value
			String[] allowedValues = paramConfig.getAllowedValues();
			boolean booleanParam = Arrays.equals(allowedValues, new String[] {"true", "false"});
			if (booleanParam) {
				// Use tickable checkbox
				columnViewer.setEditingSupport(new CheckboxEditingSupport(viewer, typeIndex, paramIndex));
				columnViewer.setLabelProvider(new CheckboxLabelProvider(typeIndex, paramIndex));
			} else {
				// Use combo box with list of allowed values
				columnViewer.setEditingSupport(new EnumValueEditingSupport(viewer, typeIndex, paramIndex, paramConfig.getAllowedValues()) );
				columnViewer.setLabelProvider(new EnumValueLabelProvider(typeIndex, paramIndex, paramConfig.getAllowedValues()));
			}
		} else {
			// Text box
			columnViewer.setEditingSupport(new ParameterValueEditingSupport(viewer, typeIndex, paramIndex));
			columnViewer.setLabelProvider(new ParameterLabelProvider(typeIndex, paramIndex));
		}
	}

	/**
	 * Remove all columns from table (i.e. completely clear it)
	 */
	public void removeAllColumnsFromTable() {
		for(TableColumn t : viewer.getTable().getColumns() ) {
			 t.dispose();
		}
	}

	/**
	 * Label provider for xml filename column
	 */
	private class XmlNameLabelProvider extends ColumnLabelProvider {
		private final int typeIndex;

		public XmlNameLabelProvider(int typeIndex) {
			this.typeIndex = typeIndex;
		}

		@Override
		public String getText(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			String fileFullPath = param.getParameterValuesForScanBeans().get(typeIndex).getBeanFileName();
			return FilenameUtils.getName(fileFullPath);
		}
	}

	/**
	 *  Editing support for choosing xml filename editing via combo box selection
	 */
	private class XmlNameEditingSupport extends EditingSupport {

		private final int typeIndex;
		private String[] xmlFileNamesForCombo;

		public XmlNameEditingSupport(ColumnViewer viewer, int typeIndex) {
			super(viewer);
			this.typeIndex = typeIndex;
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			ParametersForScan opf = (ParametersForScan) element;
			// Make new of combo box filenames each time, in case list of all files (xmlFiles) has changed.
			List<String> suitableFiles = SpreadsheetViewHelperClasses.getListOfFilesMatchingType(xmlFiles, opf.getParameterValuesForScanBeans().get(typeIndex).getBeanType());
			int i=0;
			xmlFileNamesForCombo = new String[suitableFiles.size()];
			for(String fullFilePath : suitableFiles) {
				xmlFileNamesForCombo[i++]=FilenameUtils.getName(fullFilePath);
			}

			ComboBoxCellEditor ce = new ComboBoxCellEditor((Composite) getViewer().getControl(), xmlFileNamesForCombo, SWT.READ_ONLY);
			return ce;
		}

		@Override
		protected boolean canEdit(Object ob) {
			if (xmlFiles == null || xmlFiles.size() == 0) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		protected Object getValue(Object element) {
			// get value from model and convert to int to update combobox
			ParametersForScan param = (ParametersForScan) element;
			String filename = param.getParameterValuesForScanBeans().get(typeIndex).getBeanFileName();
			int index = ArrayUtils.indexOf(xmlFileNamesForCombo, FilenameUtils.getName(filename));
			return Math.max(index, 0);
		}

		@Override
		protected void setValue(Object element, Object value) {
			// update model from table
			ParametersForScan param = (ParametersForScan) element;
			int index = (Integer) value;
			param.getParameterValuesForScanBeans().get(typeIndex).setBeanFileName(xmlDirectoryName+"/"+xmlFileNamesForCombo[index]);
			getViewer().update(param, null);
		}
	}

	/**
	 * Set new parameter override value using supplied string from column in table.
	 *
	 * @param param
	 * @param value
	 * @param columnNumber
	 */
	private void setOverrideFromColumnData(ParametersForScan paramForScan, Object value, int typeIndex, int paramIndex) {
		String strValue = (String) value;
		ParameterValuesForBean params = paramForScan.getParameterValuesForScanBeans().get(typeIndex);
		params.getParameterValues().get(paramIndex).setNewValue(strValue);
	}

	/**
	 * Get parameter override value to go in column of table
	 *
	 * @param param
	 * @param paramIndex
	 * @return
	 */
	private Object getDataForColumn(ParametersForScan param, int typeIndex, int index) {
		return param.getParameterValuesForScanBeans().get(typeIndex).getParameterValues().get(index).getNewValue();
	}

	/**
	 * Label provider for string parameter value
	 */
	private class ParameterLabelProvider extends ColumnLabelProvider  {
		private final int paramIndex;
		private final int typeIndex;

		public ParameterLabelProvider(int typeIndex, int paramIndex) {
			this.paramIndex = paramIndex;
			this.typeIndex = typeIndex;
		}

		@Override
		public String getText(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			return getDataForColumn(param, typeIndex, paramIndex).toString();
		}
	}

	/**
	 *  Editing support for entering parameter value as text string
	 */
	private class ParameterValueEditingSupport extends EditingSupport {

		private final int typeIndex;
		private final int paramIndex;

		public ParameterValueEditingSupport(ColumnViewer viewer, int typeIndex, int columnNumber) {
			super(viewer);
			this.paramIndex = columnNumber;
			this.typeIndex = typeIndex;
		}

		// Called to update value in model from value in edited cell of table
		@Override
		public void setValue(Object element, Object value) {
			ParametersForScan param = (ParametersForScan) element;
			setOverrideFromColumnData(param, value, typeIndex, paramIndex);
			getViewer().update(param, null);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			boolean editable = param.getParameterValuesForScanBeans().get(typeIndex).getParameterValues().get(paramIndex).isEditable();
			return editable;
		}

		@Override
		protected Object getValue(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			return getDataForColumn(param, typeIndex, paramIndex).toString();
		}
	}

	/**
	 * Label provider for enum values.
	 */
	private class EnumValueLabelProvider extends ColumnLabelProvider  {
		private final int paramIndex;
		private final int typeIndex;
		private final String[] comboItems;


		public EnumValueLabelProvider(int typeIndex, int paramIndex, String[] comboItems) {
			this.paramIndex = paramIndex;
			this.typeIndex = typeIndex;
			this.comboItems = comboItems;
		}

		@Override
		public String getText(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			Object val = getDataForColumn(param, typeIndex, paramIndex).toString();
			if (val == null || val.toString().trim().isEmpty()) {
				return comboItems[0];
			} else {
				return val.toString();
			}
		}
	}

	/**
	 *  Editing support for choosing values via combo box
	 */
	private class EnumValueEditingSupport extends EditingSupport {
		private final int typeIndex;
		private final int paramIndex;
		private final String[] comboItems;

		public EnumValueEditingSupport(ColumnViewer viewer, int typeIndex, int columnNumber, String[] comboItems) {
			super(viewer);
			this.paramIndex = columnNumber;
			this.typeIndex = typeIndex;
			this.comboItems = comboItems;
		}

		// Called to update value in model from value in edited cell of table
		@Override
		public void setValue(Object element, Object value) {
			ParametersForScan param = (ParametersForScan) element;
			String selectedItem = comboItems[(int) value];
			setOverrideFromColumnData(param, selectedItem, typeIndex, paramIndex);
			getViewer().update(param, null);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			ComboBoxCellEditor ce = new ComboBoxCellEditor((Composite) getViewer().getControl(), comboItems);
			return ce;
		}

		@Override
		protected boolean canEdit(Object element) {
			return comboItems.length > 0;
		}

		@Override
		protected Object getValue(Object element) {
			// get value from model and convert to int to update combobox
			ParametersForScan param = (ParametersForScan) element;
			String valueInModel = getDataForColumn(param, typeIndex, paramIndex).toString();
			int index = ArrayUtils.indexOf(comboItems, valueInModel);
			return Math.max(index, 0);
		}
	}

	// Magic tweaks so to allow cursor keys to be used to navigate between elements in the table...
	// (from Snippet035TableCursorCellHighlighter)
	private void setupForCursorNavigation(TableViewer v) {
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(v, new FocusCellOwnerDrawHighlighter(v));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(v) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				// TODO see AbstractComboBoxCellEditor for how list is made visible
				return super.isEditorActivationEvent(event)
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
						&& (event.keyCode == KeyLookupFactory.getDefault().formalKeyLookup(IKeyLookup.ENTER_NAME)));
			}
		};

		int features = ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
					 | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION;

		TableViewerEditor.create(v, focusCellManager, actSupport, features);
	}


	/**
	 * Label provider for the 'number of repetitions' column
	 */
	private class NumRepetitionsLabelProvider extends ColumnLabelProvider  {
		public NumRepetitionsLabelProvider() {
		}

		@Override
		public String getText(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			return Integer.toString(param.getNumberOfRepetitions());
		}
	}

	/**
	 * Editing support of 'number of repetition' column (uses {@link SpinnerCellEditor}).
	 */
	private class NumRepetitionsEditingSupport extends EditingSupport {

		public NumRepetitionsEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		// Called to update value in model from value in edited cell of table
		@Override
		public void setValue(Object element, Object value) {
			ParametersForScan param = (ParametersForScan) element;
			int numReps = Integer.parseInt(value.toString());
			param.setNumberOfRepetitions(numReps);
			getViewer().update(param, null);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			SpinnerCellEditor ce = new SpinnerCellEditor((Composite) getViewer().getControl());
			return ce;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			// get value from model and convert to int to update combobox
			ParametersForScan param = (ParametersForScan) element;
			return param.getNumberOfRepetitions();
		}
	}

	/**
	 * Editing support for setting boolean true/false using checkbox
	 */
	private class CheckboxEditingSupport extends EditingSupport {
		private int paramIndex;
		private int typeIndex;

		public CheckboxEditingSupport(ColumnViewer viewer, int typeIndex, int columnNumber) {
			super(viewer);
			this.paramIndex = columnNumber;
			this.typeIndex = typeIndex;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			ParametersForScan param = (ParametersForScan)element;
			String val = getDataForColumn(param, typeIndex, paramIndex).toString();
			return Boolean.parseBoolean(val);
		}

		@Override
		protected void setValue(Object element, Object value) {
			ParametersForScan param = (ParametersForScan)element;
			setOverrideFromColumnData(param, value.toString(), typeIndex, paramIndex);
			getViewer().update(param, null);
		}
	}

	/**
	 * Label provider support for tickable checkbox. Returns image for ticked, unticked checkbox as can't put
	 * arbitrary widgets into Jface table.
	 */
	private class CheckboxLabelProvider extends  ColumnLabelProvider  {
		private int paramIndex;
		private int typeIndex;
		private Image CHECKED = GDAClientActivator.getImageDescriptor("icons/checked.gif").createImage();
		private Image UNCHECKED = GDAClientActivator.getImageDescriptor("icons/unchecked.gif").createImage();

		public CheckboxLabelProvider(int typeIndex, int paramIndex) {
			this.paramIndex = paramIndex;
			this.typeIndex = typeIndex;
		}

		@Override
		public Image getImage(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			String val = getDataForColumn(param, typeIndex, paramIndex).toString();
			if (val.equalsIgnoreCase("true")) {
				return CHECKED;
			} else {
				return UNCHECKED;
			}
		}

		@Override
		public String getText(Object element) {
			return null;
		}
	}

	public List<String> getXmlFiles() {
		return xmlFiles;
	}

	public void setXmlFiles(List<String> xmlFiles) {
		this.xmlFiles = xmlFiles;
	}

	private String xmlDirectoryName;

	public String getXmlDirectoryName() {
		return xmlDirectoryName;
	}

	public void setXmlDirectoryName(String xmlDirectoryName) {
		this.xmlDirectoryName = xmlDirectoryName;
		xmlFiles = SpreadsheetViewHelperClasses.getListOfFilesMatchingExtension(xmlDirectoryName, ".xml");
	}

	public SpreadsheetViewConfig getViewConfig() {
		return viewConfig;
	}

	public void setViewConfig(SpreadsheetViewConfig viewConfig) {
		this.viewConfig = viewConfig;
	}
}
