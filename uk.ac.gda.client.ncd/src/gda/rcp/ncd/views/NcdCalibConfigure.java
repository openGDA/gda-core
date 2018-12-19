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

import gda.device.DeviceException;
import gda.observable.IObserver;
import gda.rcp.ncd.NcdController;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.beans.CalibLabel;
import uk.ac.gda.server.ncd.beans.CalibrationLabels;
import uk.ac.gda.server.ncd.detectorsystem.NcdDetector;

public class NcdCalibConfigure extends ViewPart implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(NcdCalibConfigure.class);

	private static final String[] columnNames = { "Channel", "Source" };

	private Table table;
	private TableViewer tableViewer;
	private Composite composite;
	private CalibrationLabels calibLabels = null;
	private NcdDetector ncdDetectorSystem;
	
	@Override
	public void createPartControl(Composite parent) {
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
				return ((List<CalibLabel>) inputElement).toArray();
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {				
			}
		});
		tableViewer.setLabelProvider(new MyLabelProvider());
		tableViewer.setUseHashlookup(true);

		ncdDetectorSystem = NcdController.getInstance().getNcdDetectorSystem();
		// The input for the table viewer is the instance of
		try {
			calibLabels = (CalibrationLabels) ncdDetectorSystem.getAttribute("CalibrationLabels");
			tableViewer.setInput(calibLabels.getCalibrationLabels());
		} catch (DeviceException e) {
			logger.error("Unable to get calibration labels");
		}
	}
	
	private void createTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		table = new Table(parent, style);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
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

	private class CellModifier implements ICellModifier {

		/**
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		@Override
		public boolean canModify(Object element, String property) {
			int columnIndex = getColumnNames().indexOf(property);
			return (columnIndex == 0) ? false : true;
		}

		/**
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
		 */
		@Override
		public Object getValue(Object element, String property) {

			// Find the index of the column
			int columnIndex = getColumnNames().indexOf(property);

			Object result = null;
			CalibLabel calibLabel = (CalibLabel) element;
			switch (columnIndex) {
			case 1:
				result = String.valueOf(calibLabel.getSource());
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
				CalibLabel calibLabel = (CalibLabel) item.getData();
				switch (columnIndex) {
				case 0:
					break;
				case 1:
					calibLabel.setSource((String) value);
					break;
				default:
				}
				tableViewer.refresh(true);
				try {
					ncdDetectorSystem.setAttribute("CalibrationLabels", calibLabels);
				} catch (DeviceException e) {
					logger.error("Unable to set calibration labels");
				}
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
			CalibLabel calibLabel = (CalibLabel) element;
			switch (columnIndex) {
			case 0:
				return String.valueOf(calibLabel.getChannel());
			case 1:
				return String.valueOf(calibLabel.getSource());
			default:
				return "";
			}
		}
	}
	
	@Override
	public void setFocus() {
	}

	@Override
	public void update(Object source, Object arg) {
		Display.getDefault().asyncExec(new Updater(source, arg));
	}
	
	private class Updater implements Runnable {
		private Object source;
		private Object arg;

		/**
		 * @param source
		 * @param arg
		 */
		public Updater(Object source, Object arg) {
			this.source = source;
			this.arg = arg;
		}

		@Override
		public void run() {
			if (source != null && arg != null && arg instanceof CalibrationLabels) {
				logger.debug("got update for calibration labels");
				if (!calibLabels.equals(arg)) {
					logger.debug("calibration labels were not equal");
					calibLabels = (CalibrationLabels) arg;
					tableViewer.setInput(calibLabels.getCalibrationLabels());
				}
				else {
					logger.debug("Calibration labels were equal");
				}
			}
		}
	}
}
