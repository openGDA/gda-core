/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.exafs.ui.OutputParametersUIEditor;

public class SpreadsheetViewTableColumns {
	private static final Logger logger = LoggerFactory.getLogger(SpreadsheetViewTableColumns.class);
	private FileListWatcher fileWatcher;

	public void setupXmlNameColumn(TableViewerColumn column, int typeIndex) {
		column.setLabelProvider(new XmlNameLabelProvider(typeIndex));
		column.setEditingSupport(new XmlNameEditingSupport(column.getViewer(), typeIndex));
	}

	public void setupCheckboxColumn(TableViewerColumn column,  int typeIndex, int paramIndex) {
		column.setLabelProvider(new CheckboxLabelProvider(typeIndex, paramIndex));
		column.setEditingSupport(new CheckboxEditingSupport(column.getViewer(), typeIndex, paramIndex));
	}

	public void setupEnumColumn(TableViewerColumn column,  int typeIndex, int paramIndex, String[] allowedValues) {
		column.setLabelProvider(new EnumValueLabelProvider(typeIndex, paramIndex, allowedValues));
		column.setEditingSupport(new EnumValueEditingSupport(column.getViewer(), typeIndex, paramIndex, allowedValues));
	}

	public void setupFileBrowseColumn(TableViewerColumn column,  int typeIndex, int paramIndex) {
		column.setLabelProvider(new StringValueLabelProvider(typeIndex, paramIndex));
		column.setEditingSupport(new BrowseForFileEditingSupport(column.getViewer(), typeIndex, paramIndex));
	}

	public void setupStringColumn(TableViewerColumn column,  int typeIndex, int paramIndex) {
		column.setLabelProvider(new StringValueLabelProvider(typeIndex, paramIndex));
		column.setEditingSupport(new StringValueEditingSupport(column.getViewer(), typeIndex, paramIndex));
	}

	public void setupScriptFileColumn(TableViewerColumn column,  int typeIndex, int paramIndex) {
		column.setEditingSupport(new BrowseForFileEditingSupport(column.getViewer(), typeIndex, paramIndex));
		column.setLabelProvider(new StringValueLabelProvider(typeIndex, paramIndex));
	}

	public void setupDetectorFileColumn(TableViewerColumn column,  int typeIndex, int paramIndex) {
		column.setEditingSupport(new DetectorConfigFileEditingSupport(column.getViewer(), typeIndex, paramIndex));
		column.setLabelProvider(new StringValueLabelProvider(typeIndex, paramIndex));
	}

	/**
	 * Label provider for xml filename column
	 */
	public class XmlNameLabelProvider extends ColumnLabelProvider {
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
			String fullPathToXmlFile = Paths.get(fileWatcher.getDirectoryToWatch(), xmlFileNamesForCombo[index]).toString();

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
	 * Label provider support for tickable checkbox. Returns image for ticked, unticked checkbox as can't put
	 * arbitrary widgets into Jface table.
	 */
	private class CheckboxLabelProvider extends  ColumnLabelProvider  {
		private int paramIndex;
		private int typeIndex;

		public CheckboxLabelProvider(int typeIndex, int paramIndex) {
			this.paramIndex = paramIndex;
			this.typeIndex = typeIndex;
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
			FontRegistry reg = new FontRegistry();
			FontDescriptor desc = reg.defaultFontDescriptor();

			Display display = Display.getDefault();
			for(FontData f : display.getFontList(null, true)) {
				if (f.getName().equalsIgnoreCase("serif")) {
					desc = FontDescriptor.createFrom(f);
					break;
				}
			}
			return desc.setHeight(12).setStyle(SWT.BOLD).createFont(display);
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
	 * Textbox and button for editing before/after script name/commands.
	 * Button opens up a file dialog to select a script file.
	 */
	private class BrowseForFileCellEditor extends DialogCellEditor {
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
	 * Editing support to allow detector configuration file for Detector parameter to be changed via a Combo box.
	 */
	private class DetectorConfigFileEditingSupport extends EditingSupport {
		private final int paramIndex;
		private final int typeIndex;
		private List<String> detectorConfigFiles = Collections.emptyList();

		private List<String> detectorConfigClassTypes = Arrays.asList(XspressParameters.class.getSimpleName(),
				Xspress3Parameters.class.getSimpleName(),
				VortexParameters.class.getSimpleName());

		public DetectorConfigFileEditingSupport(ColumnViewer viewer, int typeIndex, int columnNumber) {
			super(viewer);
			this.paramIndex = columnNumber;
			this.typeIndex = typeIndex;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new ComboBoxCellEditor((Composite) getViewer().getControl(), detectorConfigFiles.toArray(new String[0]));
		}

		@Override
		protected boolean canEdit(Object element) {
			detectorConfigFiles = getFileList(detectorConfigClassTypes);
			return !detectorConfigFiles.isEmpty();
		}

		@Override
		protected Object getValue(Object element) {
			ParametersForScan param = (ParametersForScan) element;
			String valueInModel = getDataForColumn(param, typeIndex, paramIndex);
			int index = detectorConfigFiles.indexOf(valueInModel);
			return Math.max(index, 0);
		}

		@Override
		protected void setValue(Object element, Object value) {
			ParametersForScan param = (ParametersForScan) element;
			String selectedItem = detectorConfigFiles.get((int) value);
			setOverrideFromColumnData(param, selectedItem, typeIndex, paramIndex);
			getViewer().refresh();
		}
	}

	/**
	 * Return list of xml filenames matching required class types. (Filename only, not the full path)
	 * The names are sorted alphabetically, with the names corresponding to ones auto generated by the
	 * Spreadsheet view placed at the end of the list.
	 *
	 * @param types - class type
	 * @return List of file names
	 */
	private List<String> getFileList(List<String> types) {
		// Regex to determine if name is for an 'auto generated' file
		// i.e. one created from Spreadsheet view, ending
		Pattern generatedXmlRegex = Pattern.compile(".*_\\d_\\d.xml");
		var stringTester = generatedXmlRegex.asMatchPredicate();

		// Make mutable copy of the file list
		var files = new ArrayList<>(fileWatcher.getFileList(types));

		// ... so it can be sorted into alphabetical order
		Collections.sort(files);

		// Make list of 'auto generated' and non auto generated files.
		List<String> autoGeneratedFiles = files.stream().filter(stringTester).toList();
		List<String> otherFiles = files.stream().filter(f -> !stringTester.test(f)).toList();

		// Complete list of file has auto generated ones at the end
		List<String> allFiles = new ArrayList<>();
		allFiles.addAll(otherFiles);
		allFiles.addAll(autoGeneratedFiles);
		return allFiles;
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

	public FileListWatcher getFileWatcher() {
		return fileWatcher;
	}

	public void setFileWatcher(FileListWatcher fileWatcher) {
		this.fileWatcher = fileWatcher;
	}
}
