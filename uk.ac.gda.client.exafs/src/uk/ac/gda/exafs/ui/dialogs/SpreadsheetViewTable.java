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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.jface.window.Window;
import org.eclipse.richbeans.widgets.cell.SpinnerCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.gda.beans.exafs.ISampleParametersWithMotorPositions;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.SampleParameterMotorPosition;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.exafs.ui.OutputParametersUIEditor;
import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean.ParameterValue;

public class SpreadsheetViewTable {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpreadsheetViewTable.class);

	private TableViewer viewer;
	private SpreadsheetViewConfig viewConfig;
	private String xmlDirectoryName = "";
	private Optional<Point> selectedTableIndex = Optional.empty();
	private List<ParametersForScan> parameterValuesForScanFiles;
	private FileListWatcher fileWatcher = new FileListWatcher();

	/** Regex to determine if a filename is for an 'auto generated' file
	 * i.e. one created from Spreadsheet view, ending in 2 numbers separated by underscore. */
	private static final Predicate<String> autogenerateFilenameTester = Pattern.compile(".*_\\d_\\d.xml").asMatchPredicate();

	public SpreadsheetViewTable(Composite parent, int style) {
		viewer = new TableViewer(parent, style);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		// set layout on the Table so it fills rest of composite
		viewer.getTable().setLayout(new FillLayout());
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		setupForCursorNavigation(viewer);
		addContextMenu(viewer);
		viewer.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				selectedTableIndex = getSelectedCellIndices(new Point(e.x, e.y));
			}
		});

		viewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				logger.debug("Key down");
				if (viewer.isCellEditorActive() &&
					(e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) ) {
					viewer.applyEditorValue();
					viewer.cancelEditing();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				ViewerCell cell = viewer.getColumnViewerEditor().getFocusCell();
				TableItem[] row = viewer.getTable().getSelection();
				if (row != null && row.length > 0) {
					ParametersForScan p = (ParametersForScan) row[0].getData();
					int rowIndex = viewer.getTable().getSelectionIndex();
					int columnIndex = cell.getColumnIndex();
					logger.debug("Key up : row = {}, column = {}", rowIndex, columnIndex);
					if (viewer.isCellEditorActive()) {
						logger.debug("Cell editor active");
						return;
					}
					if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
						viewer.editElement(p, columnIndex);
					}
				}
			}
		});
	}

	/**
	 * Return index of item in the table that coordinate lies in.
	 * @param coordinate
	 * @return
	 */
	private Optional<Point> getSelectedCellIndices(Point coordinate) {
		TableItem selectedItem = viewer.getTable().getItem(coordinate);
		if (selectedItem == null) {
			logger.debug("No table item under mouse click");
			return Optional.empty();
		}
		int numColumns = viewer.getTable().getColumnCount();
		for(int i=0; i<numColumns; i++) {
			if (selectedItem.getBounds(i).contains(coordinate)) {
				Point tableIndex = new Point(i, viewer.getTable().getSelectionIndex());
				logger.debug("Selected row = {}, selected column = {}", tableIndex.y, tableIndex.x);
				return Optional.of(tableIndex);
			}
		}
		return Optional.empty();
	}

	private void addContextMenu(TableViewer tableViewer) {
		MenuManager contextMenu = new MenuManager("ViewerMenu");
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(mgr -> {
			// Only show the context menu if last mouse click was on cell of the table
			if (!selectedTableIndex.isPresent()) {
				return;
			}
			int selectedColumn = selectedTableIndex.get().x;
			int selectedRow = selectedTableIndex.get().y;
			if (selectedColumn == 0) {
				return; // don't show menu for 1st column - it's the row index.
			}
			contextMenu.add(new Action("Copy values to rows...") {
				@Override
				public void run() {
					copySettingsToRows(selectedColumn, selectedRow);
				}
			});

			// Get ParameterValue object for current selected cell :
			ParameterValue value = getParameterValueFromIndex(selectedColumn, selectedRow);
			if (value == null) {
				// i.e. filename or repetitions column is selected.
				logger.debug("No ParameterValue found for selected indices");
				return;
			}
			logger.debug("Selected ParameterValue : {}", value.getFullPathToGetter());
			Optional<Scannable> scn = getScannable(value);

			if (scn.isPresent()) {
				contextMenu.add(new Action("Get latest position") {
					@Override
					public String getText() {
						return "Get latest position from "+scn.get().getName();
					}
					@Override
					public void run() {
						getLatestPosition(value, scn);
					}
				});

				contextMenu.add(new Action("Go to selected position") {
					@Override
					public String getText() {
						return "Move "+scn.get().getName()+" to selected position";
					}
					@Override
					public void run() {
						goToSelectedPosition(value, scn);
					}
				});
			}
		});

		Menu menu = contextMenu.createContextMenu(tableViewer.getControl());
		tableViewer.getControl().setMenu(menu);
	}

	/**
	 * Get position from scannable and set the newValue of the ParameterValue to it
	 * @param value
	 * @param scannable
	 */
	private void getLatestPosition(ParameterValue value, Optional<Scannable> scannable) {
		scannable.ifPresent(scn -> {
			try {
				value.setNewValue(scn.getPosition());
				viewer.refresh();
			} catch (DeviceException e) {
				logger.error("Problem setting parameter {} to current position of {}", value.getFullPathToGetter(), scn.getName(), e);
			}
		});
	}

	/**
	 * Move the scannable to position of the ParameterValue
	 * @param value
	 * @param scannable
	 */
	private void goToSelectedPosition(ParameterValue value, Optional<Scannable> scannable) {
		scannable.ifPresent( scn -> {
			try {
				Object newPosition = value.getNewValue();
				scn.moveTo(newPosition);
			} catch (DeviceException e) {
				logger.error("Problem moving {} to {}", scn.getName(), value.getNewValue(), e);
			}
		});
	}

	private ParameterValue getParameterValueFromIndex(int column, int row) {
		ParametersForScan scanParams = parameterValuesForScanFiles.get(row);
		Pair<Integer, Integer> paramIndices = scanParams.getParameterValueByIndex(column-1); // Data start at index=1 due to row index column...
		if (paramIndices != null && paramIndices.getSecond() != null) {
			ParameterValuesForBean beanParams = scanParams.getParameterValuesForScanBeans().get(paramIndices.getFirst());
			return beanParams.getParameterValue(paramIndices.getSecond());
		}
		return null;
	}

	/**
	 * Infers from a {@code ParameterValue} a {@code Scannable} to be moved by a {@link SampleParameterMotorPosition} setting
	 * @param ParameterValue value from which the Scannable is inferred
	 * @return Scannable to be moved (optional)
	 */
	private Optional<Scannable> getScannable(ParameterValue v) {

		// Split the getter string at () and . symbols
		// (getter string format is : getSampleParameterMotorPosition(<name of scannable>).getDemandPosition() )
		String[] splitString = v.getFullPathToGetter().split("[().]+");

		if(splitString == null || splitString.length != 3) return Optional.empty();

		boolean criteriaMet =
				ISampleParametersWithMotorPositions.MOTOR_POSITION_GETTER_NAME.equals(splitString[0]) &&
				SampleParameterMotorPosition.DEMAND_POSITION_GETTER_NAME.equals(splitString[2]);

		if (!criteriaMet) {
			return Optional.empty();
		}
		String scannableName = splitString[1];
		logger.debug("Getting position of scannable {}", scannableName);
		return Finder.findOptionalOfType(scannableName,Scannable.class);
	}

	/**
	 * Copy value from cell in table at specified cell indices across multiple rows (same column)
	 * @param selectedColumn
	 * @param selectedRow
	 */
	private void copySettingsToRows(int selectedColumn, int selectedRow) {
		// Get Indices in the model corresponding to selected parameter
		ParametersForScan scanParams = parameterValuesForScanFiles.get(selectedRow);
		Pair<Integer, Integer> paramIndices = scanParams.getParameterValueByIndex(selectedColumn-1); // Data start at index=1 due to row index column...

		// Check if selected column is for number of repetitions
		boolean isRepetitions = selectedColumn == viewer.getTable().getColumnCount()-1;

		if (!isRepetitions && paramIndices == null) {
			logger.warn("No parameter found for position ({}, {}) in the table", selectedColumn, selectedColumn);
			return;
		}

		// Present dialog box for user to select range of rows the value should be copied to
		ParameterSetterDialog dialog = new ParameterSetterDialog(viewer.getControl().getShell());
		dialog.setBlockOnOpen(true);
		dialog.setInitialRow(selectedRow);
		dialog.setMaxRow(parameterValuesForScanFiles.size()-1);
		dialog.create();

		if (dialog.open() != Window.OK) {
			return;
		}

		if (isRepetitions) {
			// Set the number of repetitions
			int newNumReptitions = scanParams.getNumberOfRepetitions();
			for(int i=dialog.getStartRow(); i<=dialog.getEndRow(); i++) {
				logger.debug("Applying number of reptitions {} to row {}", newNumReptitions, i);
				parameterValuesForScanFiles.get(i).setNumberOfRepetitions(newNumReptitions);
			}
		} else {
			Integer paramTypeIndex = paramIndices.getFirst();
			Integer paramIndex = paramIndices.getSecond();

			// Get the value of parameter to be copied
			ParameterValuesForBean paramsToCopyFrom = scanParams.getParameterValuesForScanBeans().get(paramTypeIndex);
			logger.debug("Parameter type : {}", paramsToCopyFrom.getBeanType());
			Object valueToCopy = null;
			if (paramIndex != null) {
				valueToCopy = paramsToCopyFrom.getParameterValue(paramIndex).getNewValue();
			} else {
				valueToCopy = paramsToCopyFrom.getBeanFileName();
			}

			// Apply the value to the selected rows
			for(int i=dialog.getStartRow(); i<=dialog.getEndRow(); i++) {
				ParameterValuesForBean valsForBean = parameterValuesForScanFiles.get(i).getParameterValuesForScanBeans().get(paramTypeIndex);
				if (paramIndex == null) {
					logger.debug("Applying filename {} to row {}", valueToCopy, i);
					valsForBean.setBeanFileName((String)valueToCopy);
				} else {
					logger.debug("Setting value {} = {} to row {}", valsForBean.getParameterValue(paramIndex).getFullPathToGetter(), valueToCopy, i);
					valsForBean.getParameterValue(paramIndex).setNewValue(valueToCopy);
				}
			}
		}
		viewer.refresh();
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
				for (int i = 0; i < tableColumns.length; i++) {
					maxDataWidth[i] = Math.max(maxDataWidth[i], gc.stringExtent(rowData.getText(i)).x);
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

	// Add a column to show the row number
	private void addRowIndexColumn() {
		TableColumn rcolumn = new TableColumn(viewer.getTable(), SWT.NONE);
		rcolumn.setWidth(50);
		rcolumn.setText("Row index");
		TableViewerColumn rowColumn = new TableViewerColumn(viewer, rcolumn);
		rowColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(viewer.getTable().indexOf((TableItem)cell.getItem())+"");
			}
		});
	}

	/**
	 * Add columns to table : for each scan file add column with combo box for file selection, followed
	 * by zero or more columns to contain values of selected parameters modifiers.
	 * @param parametersForScanBeans
	 */
	public void addColumnsToTable(List<ParameterValuesForBean> parametersForScanBeans) {
		int typeIndex=0;
		int minWidth = 75;

		addRowIndexColumn();

		for(ParameterValuesForBean parametersForBean : parametersForScanBeans) {

			TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
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

		TableViewerColumn columnViewer = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column = columnViewer.getColumn();
		column.setText(title);
		column.setWidth(100);

		if (paramConfig != null) {
			if (paramConfig.getAllowedValues().length>0) {
				// Pre-defined list of allowed values

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
			} else if (paramConfig.getFullPathToGetter().matches(SpreadsheetViewHelperClasses.SCRIPT_NAME_GETTER_REGEX)) {
				//For getBeforeScriptName, getAfterScriptName, getBeforeFirstRepetition
				// Use textbox and a 'browse for jython script file' button
				columnViewer.setEditingSupport(new BrowseForFileEditingSupport(viewer, typeIndex, paramIndex));
				columnViewer.setLabelProvider(new StringValueLabelProvider(typeIndex, paramIndex));
				column.setAlignment(SWT.RIGHT); // so name of script is visible, rather than just first part of path...
			} else if (paramConfig.getFullPathToGetter().endsWith(SpreadsheetViewHelperClasses.GETTER_FOR_DETECTOR_FILE)) {
				// Name of fluorescence detector xml configuration file
				columnViewer.setEditingSupport(new FilenameEditingSupport(viewer, typeIndex, paramIndex));
				columnViewer.setLabelProvider(new StringValueLabelProvider(typeIndex, paramIndex));

			} else {
				// Text box
				columnViewer.setEditingSupport(new StringValueEditingSupport(viewer, typeIndex, paramIndex));
				columnViewer.setLabelProvider(new StringValueLabelProvider(typeIndex, paramIndex));
			}
		} else {
			// Text box
			columnViewer.setEditingSupport(new StringValueEditingSupport(viewer, typeIndex, paramIndex));
			columnViewer.setLabelProvider(new StringValueLabelProvider(typeIndex, paramIndex));
		}
		columnViewer.getColumn().setWidth(100);
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
		private List<String> scanClassTypes = Arrays.asList(QEXAFSParameters.class.getSimpleName(), XanesScanParameters.class.getSimpleName(),
				XasScanParameters.class.getSimpleName(), XesScanParameters.class.getSimpleName());

		public XmlNameEditingSupport(ColumnViewer viewer, int typeIndex) {
			super(viewer);
			this.typeIndex = typeIndex;
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			ParametersForScan opf = (ParametersForScan) element;

			// Generate a list of files suitable for this particular combo box (i.e. scan , detector, sample or output xml files) :
			ParameterValuesForBean parameterValuesForBean = opf.getParameterValuesForScanBeans().get(typeIndex);
			List<String> classTypes = parameterValuesForBean.isScanBean() ? scanClassTypes : Arrays.asList(parameterValuesForBean.getBeanType());

			// Get list of files to go in the combo box (convert from full path to just the file name).
			List<String> suitableFiles = getFileList(classTypes).stream().map(FilenameUtils::getName).toList();

			// Make file name list to show in combo (just the filename, not full path).
			xmlFileNamesForCombo = suitableFiles.toArray(new String[0]);

			return new ComboBoxCellEditor((Composite) getViewer().getControl(), xmlFileNamesForCombo, SWT.READ_ONLY);
		}

		@Override
		protected boolean canEdit(Object ob) {
			return !fileWatcher.getFilenameList().isEmpty();
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
			if (index < 0) {
				return;
			}
			// Convert selected file name back to the full path
			String fullPathToXmlFile = Paths.get(xmlDirectoryName, xmlFileNamesForCombo[index]).toString();

			// Update the parameter value bean with the new file path
			ParameterValuesForBean parameterValuesForBean = param.getParameterValuesForScanBeans().get(typeIndex);
			parameterValuesForBean.setBeanFileName(fullPathToXmlFile);

			// For scan parameters combo box, there may be several different types of scan file (e.g. Qexafs, Xas, Xanes etc).
			// -> try to update the class type in the parameter value bean using class type extracted from the selected XML file
			if ( parameterValuesForBean.isScanBean() ) {
				try {
					String classTypeFromFile = SpreadsheetViewHelperClasses.getFirstXmlElementNameFromFile(fullPathToXmlFile);
					// Scan beans are in uk.ac.gda.beans.exafs package
					String className = "uk.ac.gda.beans.exafs."+classTypeFromFile;
					Class<?> clazz = Class.forName(className);
					if (clazz != null) {
						parameterValuesForBean.setBeanType(clazz.getCanonicalName());
					}
					logger.debug("Setting class type for scan xml file {} to {}", fullPathToXmlFile, className);
				} catch (IOException | ClassNotFoundException e) {
					logger.error("Problem updating class for scan xml file {}", fullPathToXmlFile, e);
				}
			}
			getViewer().refresh();
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
	private String getDataForColumn(ParametersForScan param, int typeIndex, int index) {
		Object newValue = param.getParameterValuesForScanBeans().get(typeIndex).getParameterValues().get(index).getNewValue();
		if (newValue == null) {
			return "";
		}
		return newValue.toString();
	}

	/**
	 * Label provider for string parameter value
	 */
	private class StringValueLabelProvider extends ColumnLabelProvider  {
		private final int paramIndex;
		private final int typeIndex;

		public StringValueLabelProvider(int typeIndex, int paramIndex) {
			this.paramIndex = paramIndex;
			this.typeIndex = typeIndex;
		}

		@Override
		public String getText(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			return getDataForColumn(param, typeIndex, paramIndex);
		}
	}

	/**
	 *  Editing support for entering parameter value as text string
	 */
	private class StringValueEditingSupport extends EditingSupport {

		private final int typeIndex;
		private final int paramIndex;

		public StringValueEditingSupport(ColumnViewer viewer, int typeIndex, int columnNumber) {
			super(viewer);
			this.paramIndex = columnNumber;
			this.typeIndex = typeIndex;
		}

		// Called to update value in model from value in edited cell of table
		@Override
		public void setValue(Object element, Object value) {
			ParametersForScan param = (ParametersForScan) element;
			setOverrideFromColumnData(param, value, typeIndex, paramIndex);
			getViewer().refresh();
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			return param.getParameterValuesForScanBeans().get(typeIndex).getParameterValues().get(paramIndex).isEditable();
		}

		@Override
		protected Object getValue(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			return getDataForColumn(param, typeIndex, paramIndex);
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
			String val = getDataForColumn(param, typeIndex, paramIndex);
			if (val.trim().isEmpty()) {
				return comboItems[0];
			} else {
				return val;
			}
		}
	}

	/**
	 *  Editing support for choosing from a fixed set of values via combo box
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
			getViewer().refresh();
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			ComboBoxCellEditor ce = new ComboBoxCellEditor((Composite) getViewer().getControl(), comboItems);
			ce.getControl().addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.character == '\n') {
						ce.deactivate();
					}
					if (e.character == '\r') {
						ce.deactivate();
					}
				}
			});
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
			String valueInModel = getDataForColumn(param, typeIndex, paramIndex);
			int index = ArrayUtils.indexOf(comboItems, valueInModel);
			return Math.max(index, 0);
		}
	}

	/**
	 * Editing support to allow detector configuration file for Detector parameter to be changed via a Combo box.
	 */
	private class FilenameEditingSupport extends EditingSupport {
		private final int paramIndex;
		private final int typeIndex;
		private List<String> detectorConfigFiles = Collections.emptyList();

		private List<String> detectorConfigClassTypes = Arrays.asList(XspressParameters.class.getSimpleName(),
				Xspress3Parameters.class.getSimpleName(),
				VortexParameters.class.getSimpleName());

		public FilenameEditingSupport(ColumnViewer viewer, int typeIndex, int columnNumber) {
			super(viewer);
			this.paramIndex = columnNumber;
			this.typeIndex = typeIndex;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			var fileList = detectorConfigFiles.toArray(new String[] {});
			return new ComboBoxCellEditor((Composite) getViewer().getControl(), fileList, SWT.READ_ONLY);
		}

		@Override
		protected boolean canEdit(Object element) {
			detectorConfigFiles = getFileList(detectorConfigClassTypes).stream().map(FilenameUtils::getName).toList();
			return !detectorConfigFiles.isEmpty();
		}

		@Override
		protected Object getValue(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			String valueInModel = getDataForColumn(param, typeIndex, paramIndex);
			int index =  detectorConfigFiles.indexOf(valueInModel);
			return Math.max(index, 0);
		}

		@Override
		protected void setValue(Object element, Object value) {
			ParametersForScan param = (ParametersForScan) element;
			int index = Math.max(0, (int) value);
			String selectedItem = FilenameUtils.getName(detectorConfigFiles.get(index));
			setOverrideFromColumnData(param, selectedItem, typeIndex, paramIndex);
			getViewer().refresh();
		}
	}

	/**
	 * Textbox and button for editing before/after script name/commands.
	 * Button opens up a file dialog to select a script file.
	 */
	public class BrowseForFileCellEditor extends DialogCellEditor {
		private Text textBox;

		public BrowseForFileCellEditor(Composite parent) {
			super(parent);
		}

		@Override
		protected Control createContents(Composite cell) {
			textBox = new Text(cell, SWT.LEFT);
			textBox.addListener(SWT.FocusOut, focusEvent -> setValueToModel());
			textBox.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent event) {
					keyReleaseOccured(event);
				}
			});
			return textBox;
		}

		@Override
		protected void keyReleaseOccured(KeyEvent keyEvent) {
			if (keyEvent.keyCode == SWT.CR || keyEvent.keyCode == SWT.KEYPAD_CR) { // Enter key
				setValueToModel();
			}
			super.keyReleaseOccured(keyEvent);
		}

		protected void setValueToModel() {
		 	String newValue = textBox.getText();
	        boolean newValidState = isCorrect(newValue);
	        if (newValidState) {
	            markDirty();
	            doSetValue(newValue);
	        }
		}

		@Override
	    protected Button createButton(Composite parent) {
	        Button button = new Button(parent, SWT.DOWN);
	        button.setText("...");
	        button.setToolTipText("Browse for Jython script file");
	        return button;
	    }

		@Override
		protected Object openDialogBox(Control cellEditorWindow) {
			FileDialog dialog = OutputParametersUIEditor.getJythonScriptFileBrowser();
			final String filename = dialog.open();
			if (filename != null) {
				textBox.setText(filename);
				setValueToModel();
			}
			return null;
		}

		@Override
		protected void doSetFocus() {
			// Override so we can set focus to the Text widget instead of the Button.
			textBox.setFocus();
			textBox.selectAll();
		}

		@Override
		protected void updateContents(Object value) {
			String label = "";
			if (value != null) {
				label = value.toString();
			}
			textBox.setText(label);
			textBox.setFocus();
			textBox.forceFocus();
		}
	}

	private class BrowseForFileEditingSupport extends EditingSupport {
		private final int typeIndex;
		private final int paramIndex;

		public BrowseForFileEditingSupport(ColumnViewer viewer, int typeIndex, int paramIndex) {
			super(viewer);
			this.typeIndex = typeIndex;
			this.paramIndex = paramIndex;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new BrowseForFileCellEditor((Composite)getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			return getDataForColumn(param, typeIndex, paramIndex);
		}

		@Override
		protected void setValue(Object element, Object value) {
			ParametersForScan param = (ParametersForScan) element;
			String selectedFilename = (String)value;
			setOverrideFromColumnData(param, selectedFilename, typeIndex, paramIndex);
			getViewer().refresh();
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
			return new BooleanCellEditor((Composite) getViewer().getControl(), SWT.NONE);
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			ParametersForScan param = (ParametersForScan)element;
			return Boolean.parseBoolean(getDataForColumn(param, typeIndex, paramIndex));
		}

		@Override
		protected void setValue(Object element, Object value) {
			ParametersForScan param = (ParametersForScan)element;
			setOverrideFromColumnData(param, value.toString(), typeIndex, paramIndex);
			getViewer().refresh();
		}
	}

	private class BooleanCellEditor extends CellEditor {
		private Button b;

		public BooleanCellEditor(Composite parent, int style) {
			super(parent, style);
		}
		@Override
		protected Control createControl(Composite parent) {
			b = new Button(parent, getStyle()|SWT.CHECK);
			b.setBackground(parent.getBackground());
			addListeners();
			return b;
		}

		protected void addListeners() {
			b.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.character == '\n') {
						BooleanCellEditor.this.focusLost();
					}
					if (e.character == '\r') {
						BooleanCellEditor.this.focusLost();
					}
				}
			});

			b.addFocusListener( new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					BooleanCellEditor.this.focusLost();
				}
			});
		}

		@Override
		public void focusLost() {
			logger.info("Focus lost");
			super.focusLost();
		}

		@Override
		protected Object doGetValue() {
			return b.getSelection();
		}

		@Override
		protected void doSetValue(Object value) {
			b.setSelection(Boolean.valueOf(value.toString().toLowerCase()));
		}

		@Override
		protected void doSetFocus() {
			// Override so we can set focus to the Button widget.
			b.setFocus();
		}

		@Override
		public void activate(ColumnViewerEditorActivationEvent activationEvent) {
			ViewerCell cell = (ViewerCell)activationEvent.getSource();
			int index = cell.getColumnIndex();
			ViewerRow row = (ViewerRow) cell.getViewerRow().clone();
			row.setImage(index, null);
			row.setText(index, "");
			super.activate(activationEvent);
		}
	}

	/**
	 * Label provider support for tickable checkbox. Returns image for ticked, unticked checkbox as can't put
	 * arbitrary widgets into Jface table.
	 */
	private class CheckboxLabelProvider extends  ColumnLabelProvider  {
		private int paramIndex;
		private int typeIndex;
		private Font font;

		public CheckboxLabelProvider(int typeIndex, int paramIndex) {
			this.paramIndex = paramIndex;
			this.typeIndex = typeIndex;
			font = createFont();
		}

		@Override
		public String getText(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			String val = getDataForColumn(param, typeIndex, paramIndex);
			if (Boolean.parseBoolean(val)) {
				return "\u2611";
			} else {
				return "\u2610";
			}
		}

		@Override
		public Font getFont(Object element) {
			return font;
		}

		private Font createFont() {
			Display display = Display.getDefault();
			return Stream.of(display.getFontList(null, true))
				.filter(f -> f.getName().equalsIgnoreCase("serif"))
				.map(FontDescriptor::createFrom)
				.map(desc -> desc.setHeight(12).setStyle(SWT.BOLD).createFont(display))
				.findFirst()
				.orElse(null);
		}

		@Override
		public void dispose() {
			if (font != null) {
				font.dispose();
			}
		}
	}

	public void setXmlDirectoryName(String xmlDirectoryName) {
		if (!StringUtils.equals(this.xmlDirectoryName, xmlDirectoryName)) {
			this.xmlDirectoryName = xmlDirectoryName;
			startUpdateFileWatcher();
		}
	}

	private void startUpdateFileWatcher() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
		dialog.setBlockOnOpen(false);
		try {
			dialog.run(true, false, this::updateFileWatcher);
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error("Problem showing XML file update dialog box", e);
		}
	}

	/**
	 * Runnable passed to ProgressMonitorDialog to update the list of files in the fileWatcher
	 *
	 * @param monitor passed from ProgressMonitorDialog
	 */
	private void updateFileWatcher(IProgressMonitor monitor) {
		// Observer to notify progress monitor of file update progress.
		IObserver progressObserver = (src, evnt) -> {
			if (evnt instanceof FileListWatcher.Event) {
				// Update the monitor with the total number of 'work units' to be done - i.e. number of files to be processed
				if (evnt == FileListWatcher.Event.LIST_UDDATE_START) {
					monitor.beginTask("Updating XML file list...", fileWatcher.getFilenameList().size());
				}
				// Notify monitor each time a file is updated
				if (evnt == FileListWatcher.Event.LIST_UPDATE_PROGRESSS) {
					monitor.worked(1);
				}
			}
		};
		try {
			logger.info("Updating list of files and types...");
			fileWatcher.startWatchService(xmlDirectoryName);
			fileWatcher.addIObserver(progressObserver);
			fileWatcher.updateFileTypeMap();
		} catch (Exception ne) {
			logger.error("Problem updating file list", ne);
		} finally {
			fileWatcher.deleteIObserver(progressObserver);
			logger.info("Finished updating file list");
		}
		monitor.done();
	}

	/**
	 * Return list of xml filenames matching required class types. (Filename only, not the full path)
	 * The names are sorted alphabetically, with the names corresponding to ones auto generated by the
	 * Spreadsheet view placed at the end of the list.
	 *
	 * @param types - class type
	 * @return List of file names
	 */
	public List<String> getFileList(List<String> types) {
		// Make mutable copy of the file list
		var files = new ArrayList<>(fileWatcher.getFileList(types));

		// ... so it can be sorted into alphabetical order
		Collections.sort(files);

		// Put the autogenerated files at the end of the list
		files.sort((s1, s2) -> autogenerateFilenameTester.test(s1) ? 1 : 0);

		return files;
	}

	public void setViewConfig(SpreadsheetViewConfig viewConfig) {
		this.viewConfig = viewConfig;
	}

	public void setInput(List<ParametersForScan> parameterValuesForScanFiles) {
		viewer.setInput(parameterValuesForScanFiles);
		this.parameterValuesForScanFiles = parameterValuesForScanFiles;
	}
}
