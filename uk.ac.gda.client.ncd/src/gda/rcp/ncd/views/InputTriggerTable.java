/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.rcp.ncd.views;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.server.ncd.beans.InputTriggerParameters;

public class InputTriggerTable {
	private static final String[] columnNames = { "Trigger", "Debounce", "Threshhold"};
	private Table table;
	private TableViewer tableViewer;
	private Composite composite;
	List<InputTriggerParameters> inputTriggerParameters;
	
	public InputTriggerTable(Composite parent, List<InputTriggerParameters> inputTriggerParametersIn) {
		inputTriggerParameters = inputTriggerParametersIn;
		composite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.makeColumnsEqualWidth = true;
		composite.setLayout(compositeLayout);
		// Create the table
		createTable(composite);
		// Create and setup the TableViewer
		createTableViewer();
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object[] getElements(Object inputElement) {
				return ((List<InputTriggerParameters>) inputElement).toArray();
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		tableViewer.setLabelProvider(new MyLabelProvider());
		tableViewer.setUseHashlookup(true);

		// The input for the table viewer is the instance of
		tableViewer.setInput(inputTriggerParameters);
	}
	
	public List<InputTriggerParameters> getInputTriggerParameters() {
		return inputTriggerParameters;
	}
	
	private void createTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		table = new Table(parent, style);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		table.setLayoutData(gridData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// Create columns
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn column = new TableColumn(table, SWT.LEFT, i);
			column.setText(columnNames[i]);
			column.setWidth(100);
		}
	}

	private void createTableViewer() {

		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(false);
		tableViewer.setColumnProperties(columnNames);

		// Create the cell editors
		CellEditor[] editors = new CellEditor[columnNames.length];
		CellEditor editor;
		for (int i = 0; i < columnNames.length; i++) {
			editor = new TextCellEditor(table);
			((Text) editor.getControl()).setTextLimit(60);
			editors[i] = editor;
		}
		// Assign the cell editors to the viewer
		tableViewer.setCellEditors(editors);
		// We are not allowing the table to be sorted
		tableViewer.setSorter(null);
		// Set the cell modifier for the viewer
		tableViewer.setCellModifier(new CellModifier());
	}

	public java.util.List<String> getColumnNames() {
		return Arrays.asList(columnNames);
	}
	
	public Table getTable() {
		return table;
	}

	private class CellModifier implements ICellModifier {

		/**
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		@Override
		public boolean canModify(Object element, String property) {
			int columnIndex = getColumnNames().indexOf(property);
			// We could check for debounce value == NaN and return false as well!
			if (columnIndex == 0) {
				return false;
			}
			if (columnIndex == 2) {
				InputTriggerParameters trigger = (InputTriggerParameters) element;
				if (!trigger.getName().equals("Var thrshld")) {
					return false;
				}
			}
			return true;
		}

		/**
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
		 */
		@Override
		public Object getValue(Object element, String property) {

			// Find the index of the column
			int columnIndex = getColumnNames().indexOf(property);

			Object result = null;
			InputTriggerParameters trigger = (InputTriggerParameters) element;
			switch (columnIndex) {
			case 1:
				result = trigger.getDebounce().toString();
				break;
			case 2:
				result = trigger.getThreshold().toString();
				break;
			default:
				result = "";
			}
			return result;
		}

		/**
		 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
		 */
		@Override
		public void modify(Object element, String property, Object value) {
			int columnIndex = getColumnNames().indexOf(property);
			TableItem item = (TableItem) element;
			if (item != null) {
				InputTriggerParameters trigger = (InputTriggerParameters) item.getData();

				switch (columnIndex) {
				case 0:
					break;
				case 1:
					trigger.setDebounce(Double.valueOf((String)value));
					break;
				case 2:
					trigger.setThreshold(Double.valueOf((String)value));
					break;
				default:
				}
				tableViewer.refresh(true);
			}
		}
	}

	private class MyLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			InputTriggerParameters trigger = (InputTriggerParameters) element;
			switch (columnIndex) {
			case 0:
				return trigger.getName();
			case 1:
				return String.valueOf(trigger.getDebounce());
			case 2:
				if (!trigger.getName().equals("Var thrshld")) {
					return "-- --";
				}
				return String.valueOf(trigger.getThreshold());
			default:
				return "";
			}
		}
	}
}
