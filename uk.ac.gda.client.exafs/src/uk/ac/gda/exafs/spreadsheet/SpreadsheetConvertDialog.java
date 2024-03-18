/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.spreadsheet;

import java.nio.file.Paths;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.util.VisitPath;
import uk.ac.gda.exafs.ui.dialogs.ParameterCollection;

public class SpreadsheetConvertDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(SpreadsheetConvertDialog.class);

	private SpreadsheetData spreadsheetData = new SpreadsheetData();
	private SpreadsheetConverter converter = new SpreadsheetConverter();
	private ParameterCollection collection;

	private String filenamePath = "";

	private Text txtFilenamePath;
	private Text txtTemplatePath;
	private Text txtSampleFile;
	private Text txtTransFile;
	private Text txtFluoFile;
	private Text txtScanFile;
	private Text txtOutputFile;

	public SpreadsheetConvertDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createContents(Composite parent) {
		createComposite(parent);
		return super.createContents(parent);
	}

	private void createComposite(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		layout.marginLeft = 8;
		layout.marginRight = 8;
		comp.setLayout(layout);
		comp.setLayoutData(gd);

		// Create convert file path & parameters composite.
		setDefaultParameters();
		createFilePathComposite(comp);
		createParametersComposite(comp);

		Button btnClear = new Button(comp, SWT.PUSH);
		btnClear.setText("Clear");
		btnClear.setLayoutData(gd);
		btnClear.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> clearAll()));
	}

	private void createFilePathComposite(Composite parent) {

		Group grpFilePath = new Group(parent, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		grpFilePath.setText(" User Spreadsheet File ");
		grpFilePath.setLayout(new GridLayout(2, false));
		grpFilePath.setLayoutData(gd);

		Composite compFilePath = new Composite(grpFilePath, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = 5;
		layout.marginRight = 5;
		compFilePath.setLayout(layout);
		compFilePath.setLayoutData(gd);

		txtFilenamePath = addLabelAndTextBox(compFilePath, "File name: ");
		txtFilenamePath.setText(filenamePath);

		/* Browse file */
		Button btnBrowse = new Button(compFilePath, SWT.PUSH);
		btnBrowse.setText("Browse");
		btnBrowse.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> selectFile()));
	}

	private void createParametersComposite(Composite parent) {

		Group grpParameter = new Group(parent, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		grpParameter.setText(" Parameters ");
		grpParameter.setLayout(new GridLayout(2, false));
		grpParameter.setLayoutData(gd);

		Composite compParameter = new Composite(grpParameter, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = 5;
		layout.marginRight = 5;
		compParameter.setLayout(layout);
		compParameter.setLayoutData(gd);

		// Template Path
		txtTemplatePath = addLabelAndTextBox(compParameter, "Template Directory: ");
		txtTemplatePath.setText(converter.getTemplatePath());

		Button btnBrowse = new Button(compParameter, SWT.PUSH);
		btnBrowse.setText("Browse");
		btnBrowse.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> browseDataDirectory()));

		gd.horizontalSpan = 2;

		// Sample File
		txtSampleFile = addLabelAndTextBox(compParameter, "Sample File: ");
		txtSampleFile.setText(converter.getSampleFilename());
		txtSampleFile.setLayoutData(gd);

		// Transmission File
		txtTransFile = addLabelAndTextBox(compParameter, "Transmission File: ");
		txtTransFile.setText(converter.getTransmissionFilename());
		txtTransFile.setLayoutData(gd);

		// Fluorescence File
		txtFluoFile = addLabelAndTextBox(compParameter, "Fluorescence File: ");
		txtFluoFile.setText(converter.getFluoFilenameTemplate());
		txtFluoFile.setLayoutData(gd);

		// Scan File
		txtScanFile = addLabelAndTextBox(compParameter, "Scan File: ");
		txtScanFile.setText(converter.getScanFilename());
		txtScanFile.setLayoutData(gd);

		// Output File
		txtOutputFile = addLabelAndTextBox(compParameter, "Output File: ");
		txtOutputFile.setText(converter.getOutputFilename());
		txtOutputFile.setLayoutData(gd);

	}

	private boolean convertFile() {
		if ("".equals(filenamePath)) {
			MessageDialog.openWarning(getShell(), "Warning", "Select the file name first.");
			return false;
		}

		boolean canClose = false;
		try {
			updateParameters();
			if (spreadsheetData.dataConvert(filenamePath)) {
				converter.setSpreadsheetData(spreadsheetData);
				collection = converter.getAllParameters();

				MessageDialog.openInformation(getShell(), "Completed", "Completed convert file to spreadsheet!");
				canClose = true;
			} else {
				MessageDialog.openWarning(getShell(), "Warning", "Failed to convert the file - Wrong template format.");
				txtFilenamePath.setText("");
				filenamePath = "";
			}
		} catch (Exception e) {
			logger.error("Problem converting spreadsheet file.", e);
			MessageDialog.openError(getShell(), "Error", "Failed to convert the file.");
			txtFilenamePath.setText("");
			filenamePath = "";
		}
		return canClose;
	}

	private void selectFile() {
		FileDialog dialog = getFileDialog(SWT.OPEN, "");
		String filename = dialog.open();
		if (filename != null) {
			filenamePath = filename;
			txtFilenamePath.setText(filenamePath);
		}
	}

	private FileDialog getFileDialog(int type, String initialPath) {
		FileDialog dialog = new FileDialog(getParentShell(), type);
		dialog.setText("Select file");
		dialog.setFilterNames(new String[] { "XLSX and CSV files", "XLSX files", "CSV files" });
		dialog.setFilterExtensions(new String[] { "*.xlsx;*.csv", "*.xlsx", "*.csv" });
		dialog.setFilterPath(getPathOrVisit(initialPath));
		return dialog;
	}

	private void browseDataDirectory() {
		DirectoryDialog dirDialog = getDirectoryDialog(txtTemplatePath.getText());
		dirDialog.setMessage("Select directory to export xml files to");
		String result = dirDialog.open();
		if (result != null) {
			txtTemplatePath.setText(result);
		}
	}

	private DirectoryDialog getDirectoryDialog(String initialPath) {
		DirectoryDialog dirDialog = new DirectoryDialog(getParentShell());
		dirDialog.setText("Select directory");
		dirDialog.setFilterPath(getPathOrVisit(initialPath));
		return dirDialog;
	}

	private String getPathOrVisit(String initialPath) {
		// Set filterpath to current xml directory, or xml folder in current visit.
		String filterPath = initialPath;
		if (filterPath.isEmpty()) {
			filterPath = Paths.get(VisitPath.getVisitPath(), "xml/Templates/").toString();
		}
		return filterPath;
	}

	/**
	 * Add label and textbox to a composite.
	 *
	 * @param parent
	 *            parent composite
	 * @param label
	 *            String to use for label
	 * @return TextBox added to composite
	 */
	public static Text addLabelAndTextBox(Composite parent, String label) {
		Label lblName = new Label(parent, SWT.NONE);
		lblName.setText(label);
		Text textBox = new Text(parent, SWT.BORDER);
		textBox.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		return textBox;
	}

	private void setDefaultParameters() {
		converter = new SpreadsheetConverter();
		String userDirectory = Paths.get(VisitPath.getVisitPath(), "xml/Templates/").toString();
		converter.setTemplatePath(userDirectory);
		converter.getTemplatePath();
		converter.getSampleFilename();
		converter.getTransmissionFilename();
		converter.getFluoFilenameTemplate();
		converter.getScanFilename();
		converter.getOutputFilename();
	}

	private void updateParameters() {
		converter.setTemplatePath(txtTemplatePath.getText());
		converter.setSampleFilename(txtSampleFile.getText());
		converter.setTransmissionFilename(txtTransFile.getText());
		converter.setFluoFilenameTemplate(txtFluoFile.getText());
		converter.setScanFilename(txtScanFile.getText());
		converter.setOutputFilename(txtOutputFile.getText());
	}

	private void clearAll() {
		setDefaultParameters();

		txtFilenamePath.setText("");
		filenamePath = "";

		txtTemplatePath.setText(converter.getTemplatePath());
		txtSampleFile.setText(converter.getSampleFilename());
		txtTransFile.setText(converter.getTransmissionFilename());
		txtFluoFile.setText(converter.getFluoFilenameTemplate());
		txtScanFile.setText(converter.getScanFilename());
		txtOutputFile.setText(converter.getOutputFilename());
	}

	public ParameterCollection getCollection() {
		return collection;
	}

	public String getFilenamePath() {
		return filenamePath;
	}

	// Overriding this methods to set the title of the custom dialog.
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select file to convert for GDA");
	}

	// Override method to use "Convert" as label for the OK button
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Convert", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		if (convertFile()) {
			super.okPressed();
		}
	}

	@Override
	protected Point getInitialSize() {
		return new Point(650, 600);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
