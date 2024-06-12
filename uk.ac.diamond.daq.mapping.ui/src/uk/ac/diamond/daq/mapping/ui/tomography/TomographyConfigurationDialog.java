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

package uk.ac.diamond.daq.mapping.ui.tomography;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyFitSine.fitSine;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyUtils.CSV_DELIMITER;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyUtils.DF;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyUtils.createColumn;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyUtils.createComposite;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyUtils.createDialogButton;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyUtils.createInfoLabel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.factory.Finder;
import uk.ac.diamond.daq.mapping.api.TomographyCalibrationData;

/**
 * Dialog box to configure a tomography scan
 * <p>
 * It allows you to:
 * <ul>
 * <li>Capture a series of stage positions at various rotations where the sample is in focus</li>
 * <li>Calculate the sine wave calibration parameters for the x & z motors based on these positions</li>
 * <li>Store these parameters to be used in the tomography scan script</li>
 * </ul>
 * Additionally (and optionally), stage positions can be saved to - and loaded from - a file.
 */
public class TomographyConfigurationDialog extends TitleAreaDialog {
	private static final Logger logger = LoggerFactory.getLogger(TomographyConfigurationDialog.class);

	private static final int DIALOG_MAIN_COMPOSITE_COLUMNS = 2;
	private static final int TABLE_COMPOSITE_COLUMNS = 1;
	private static final int ACTION_BUTTONS_COMPOSITE_COLUMNS = 3;

	private String[] headers;
	private CSVFormat csvFormat;

	private IScannableMotor xMotor;
	private IScannableMotor yMotor;
	private IScannableMotor zMotor;
	private IScannableMotor rotationMotor;

	private Button saveButton;
	private Button removeButton;
	private Button calibrateButton;
	private Button okButton;

	private TableViewer positionTableViewer;
	private List<PositionTableEntry> tableData = new ArrayList<>();

	private final String fileDirectory;

	private Button includeYBox;

	private Date timestamp;
	private Map<String, TomographyCalibrationData> calibrationData;
	private Label calibrationLabel;
	private Label timestampLabel;

	private Composite dialogComposite;

	public enum Motor {
		R("rot", "stage1_rotation"), X("x", "SampleX"), Y("y", "SampleY"), Z("z", "SampleZ");

		private String header;
		private String scannableName;

		private Motor(String header, String scannableName) {
			this.header = header;
			this.scannableName = scannableName;
		}

		public String getScannableName() {
			return scannableName;
		}

		public String getHeader() {
			return header;
		}
	}

	public TomographyConfigurationDialog(Shell parentShell, String fileDirectory) {
		super(parentShell);

		this.xMotor = Finder.find(Motor.X.getScannableName());
		this.yMotor = Finder.find(Motor.Y.getScannableName());
		this.zMotor = Finder.find(Motor.Z.getScannableName());
		this.rotationMotor = Finder.find(Motor.R.getScannableName());

		this.fileDirectory = fileDirectory;

		calibrationData = new HashMap<>();

		headers = Arrays.stream(Motor.values()).map(Motor::getHeader).toArray(String[]::new);
		csvFormat = CSVFormat.DEFAULT.withHeader(headers).withSkipHeaderRecord().withIgnoreSurroundingSpaces(true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configure tomography parameters");
		newShell.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
	}

	@Override
    public void create() {
        super.create();
        setTitle("Calibration for a tomography scan");
        setMessage("Capture the focus position of the sample at different rotations and click 'Calibrate' to calibrate the scan",
        		IMessageProvider.INFORMATION);
    }

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		dialogComposite = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().applyTo(dialogComposite);
		dialogComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		final Composite mainComposite = createComposite(dialogComposite, DIALOG_MAIN_COMPOSITE_COLUMNS);

		final Composite positionComposite = createComposite(mainComposite, TABLE_COMPOSITE_COLUMNS);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(positionComposite);

		createPositionTable(positionComposite);

		createPositionButtons(mainComposite);

		return dialogComposite;
	}

	private void changeColumnColour(Table table) {
		int colourCode = includeYBox.getSelection() ? SWT.COLOR_BLACK : SWT.COLOR_GRAY;
		Color colour = Display.getDefault().getSystemColor(colourCode);
		var index = Motor.valueOf(Motor.Y.name()).ordinal();
		for (TableItem item : table.getItems()) {
			item.setForeground(index, colour);
		}
	}

	public boolean isIncludeY() {
		return includeYBox.getSelection();
	}

	/**
	 * Create a table to capture motor positions
	 *
	 * @param parent
	 *            the parent {@link Composite}
	 */
	private void createPositionTable(Composite parent) {
		positionTableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridDataFactory.swtDefaults().indent(5, 0).hint(SWT.DEFAULT, 250).applyTo(positionTableViewer.getTable());
		positionTableViewer.setContentProvider(ArrayContentProvider.getInstance());

		Arrays.asList(Motor.values()).stream()
			.forEach(m -> createColumn(positionTableViewer, m.getScannableName()).setLabelProvider(new PositionLabelProvider(m)));

		// Set table input
		positionTableViewer.setInput(tableData);
		positionTableViewer.addSelectionChangedListener(e -> enableRemoveButton());

		// make lines and header visible
		final Table table = positionTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	private void enableRemoveButton() {
		// Remove button should be enabled only if the user has selected one or more rows
		final IStructuredSelection selection = positionTableViewer.getStructuredSelection();
		removeButton.setEnabled(!selection.isEmpty());
	}

	/**
	 * Create buttons to capture the current motor positions into the table, delete an entry from the table and use the
	 * data to calculate calibration parameters
	 *
	 * @param parent
	 *            the parent {@link Composite}
	 */
	private void createPositionButtons(Composite parent) {
		final Composite buttonsComposite = createComposite(parent, ACTION_BUTTONS_COMPOSITE_COLUMNS);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(buttonsComposite);

		LabelFactory.newLabel(SWT.NONE).create(buttonsComposite).setText("Capture stage positions");

		final Button captureButton = createDialogButton(buttonsComposite, "Capture", "Capture the current stage position");
		captureButton.addSelectionListener(widgetSelectedAdapter(e -> capturePosition()));

		removeButton = createDialogButton(buttonsComposite, "Remove", "Remove the selected position(s)");
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(widgetSelectedAdapter(e -> removeSelectedPositions()));

		LabelFactory.newLabel(SWT.NONE).create(buttonsComposite).setText("Load or save positions to file");

		final Button loadButton = createDialogButton(buttonsComposite, "Load", "Load stage positions from a file");
		loadButton.addSelectionListener(widgetSelectedAdapter(e -> loadPositions()));

		saveButton = createDialogButton(buttonsComposite, "Save", "Save stage positions to a file");
		saveButton.addSelectionListener(widgetSelectedAdapter(e -> savePositions()));

		LabelFactory.newLabel(SWT.NONE).create(buttonsComposite).setText("Calibrate positions");

		var checkBoxComposite = createComposite(buttonsComposite, 2);
		LabelFactory.newLabel(SWT.NONE).create(checkBoxComposite).setText("Include Y");
		includeYBox = ButtonFactory.newButton(SWT.CHECK).create(checkBoxComposite);
		includeYBox.addSelectionListener(SelectionListener.widgetSelectedAdapter(selection -> {
			var table = positionTableViewer.getTable();
			if (table.getItemCount() > 0) {
				changeColumnColour(table);
			}
		}));
		includeYBox.setSelection(true);
		includeYBox.notifyListeners(SWT.Selection, new Event());

		calibrateButton = createDialogButton(buttonsComposite, "Calibrate", "Calibrate positions");
		calibrateButton.setEnabled(false);
		calibrateButton.addSelectionListener(widgetSelectedAdapter(e -> calibratePositions()));

		var labelComposite = createComposite(buttonsComposite, 1);
		GridLayoutFactory.swtDefaults().margins(1, 70).applyTo(labelComposite);
		calibrationLabel = createInfoLabel(labelComposite);
		timestampLabel = createInfoLabel(labelComposite);
		if (!calibrationData.isEmpty()) {
			updateCalibrationLabel();
		}
	}

	private void calibratePositions() {
		var rotationEntries = tableData.stream()
				.mapToDouble(entry -> entry.rValue()).toArray();

		Arrays.asList(Motor.values()).stream()
			.filter(motor -> !motor.equals(Motor.R))
			.forEach(motor -> setEntries(rotationEntries, motor));
	}

	private void setEntries(double[] rotationEntries, Motor motor) {
		double[] entries = switch(motor) {
		    case X -> tableData.stream().mapToDouble(e -> e.xValue()).toArray();
		    case Y -> tableData.stream().mapToDouble(e -> e.yValue()).toArray();
		    case Z -> tableData.stream().mapToDouble(e -> e.zValue()).toArray();
		    default -> null;
		};

		// fit sine
		var fit = fitSine(rotationEntries, entries);
		var calibrationName = String.format("%sCalibration", motor.getHeader());

		// save calibration data and timestamp
		timestamp = new java.util.Date();
		calibrationData.put(calibrationName, fit);

		// update label with calibration data timestamp
		updateCalibrationLabel();

		updateButtons();
	}

	private void updateCalibrationLabel() {
		calibrationLabel.setText("Last time data was calibrated:");
		String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(timestamp);
		timestampLabel.setText(timeStamp);
	}

	/**
	 * Put the current stage positions into the positions table
	 */
	private void capturePosition() {
		try {
			final PositionTableEntry entry = new PositionTableEntry(
					(double) rotationMotor.getPosition(), (double) xMotor.getPosition(),
					(double) yMotor.getPosition(), (double) zMotor.getPosition());
			tableData.add(entry);
			handlePositionTableChange();
		} catch (DeviceException e) {
			logger.error("Error getting motor positions", e);
		}
	}

	/**
	 * Remove selected entry/entries from the position table
	 */
	private void removeSelectedPositions() {
		Arrays.stream(positionTableViewer.getStructuredSelection().toArray())
			.forEach(entry -> tableData.remove(entry));
		handlePositionTableChange();
	}

	/**
	 * Update positions table and associated buttons when rows are added/deleted
	 */
	private void handlePositionTableChange() {
		positionTableViewer.refresh();
		updateButtons();
	}


	/**
	 * Save the current position table to a file
	 * <p>
	 * For file format see {@link #loadPositions()}
	 */
	private void savePositions() {
		final String selected = selectPositionsFile(SWT.SAVE);
		if (selected == null || selected.isEmpty()) {
			return;
		}

		final File outputFile = new File(selected);
		if (outputFile.exists()) {
			final String title = "Confirm overwrite";
			final String message = String.format("File %s already exists: do you want to overwrite it?", outputFile.getName());
			if (!MessageDialog.openConfirm(getShell(), title, message)) {
				return;
			}
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
			writer.write(String.join(CSV_DELIMITER, headers));
			writer.newLine();

			tableData.stream()
			.map(this::createRow)
			.forEach(row -> writeRow(writer, row));

		} catch(IOException e) {
			handleException("Error saving positions in CSV file", e);
		}
	}

	private String createRow(PositionTableEntry entry){
		return List.of(entry.rValue(), entry.xValue(), entry.yValue(), entry.zValue()).stream()
				.map(DF::format).collect(Collectors.joining(CSV_DELIMITER));
	}


	private void writeRow(BufferedWriter writer, String row) {
		try {
			writer.write(row);
			writer.newLine();
		} catch (IOException e1) {
			logger.error("Error writing row", e1);
		}
	}

	/**
	 * Load a set of stage positions from a file
	 * <p>
	 * The positions must be in a series of rows: <code>rotation, x, y, z</code> delimited by spaces and/or commas
	 * and/or semicolons<br>
	 * Lines beginning with a hash will be treated as comments.
	 * <p>
	 * The default extension for the file is <code>.pos</code>
	 */
	private void loadPositions() {
		final String selected = selectPositionsFile(SWT.OPEN);
		if (selected == null || selected.isEmpty()) {
			return;
		}

        try (BufferedReader reader = new BufferedReader(new FileReader(selected))) {
			tableData.clear();
			CSVParser.parse(reader, csvFormat).getRecords().stream()
				.forEach(r -> tableData.add(parseData(r)));
			handlePositionTableChange();
		} catch (Exception e) {
			logger.error("Error reading file {}", selected, e);
		}
	}

	private PositionTableEntry parseData(CSVRecord row) {
		var r = Double.parseDouble(row.get(Motor.R.getHeader()));
		var x = Double.parseDouble(row.get(Motor.X.getHeader()));
		var y = Double.parseDouble(row.get(Motor.Y.getHeader()));
		var z = Double.parseDouble(row.get(Motor.Z.getHeader()));
		return new PositionTableEntry(r, x, y, z);
	}

	private void updateButtons() {
		// Buttons that should be active if there is something in the table
		final boolean tableHasData = !tableData.isEmpty();
		saveButton.setEnabled(tableHasData);
		calibrateButton.setEnabled(tableHasData);

		final boolean calibratedData = !calibrationData.isEmpty();
		okButton.setEnabled(calibratedData);
	}

	public Map<String, TomographyCalibrationData> getTomographyCalibrationData() {
		return calibrationData;
	}

	/**
	 * Format the position of the table entry for the given motor
	 */
	private static class PositionLabelProvider extends ColumnLabelProvider {
		private final Motor motor;

		public PositionLabelProvider(Motor motor) {
			this.motor = motor;
		}

		@Override
		public String getText(Object element) {
			final PositionTableEntry entry = (PositionTableEntry) element;
			return switch(motor) {
				case X -> DF.format(entry.xValue());
				case Y -> DF.format(entry.yValue());
				case Z -> DF.format(entry.zValue());
				case R -> DF.format(entry.rValue());
			};
		}
	}

	record PositionTableEntry(double rValue, double xValue, double yValue, double zValue) {}

	private String selectPositionsFile(int style) {
		final FileDialog fileDialog = new FileDialog(getShell(), style);
		fileDialog.setText("Select configuration");
		fileDialog.setFilterPath(fileDirectory);
		fileDialog.setFilterExtensions(new String[] { "*.csv", "*.*" });
        fileDialog.setFilterNames(new String[] { "Stage positions(*.csv)", "All files(*.*)" });

        return fileDialog.open();
	}

	private void handleException(String errorMessage, Exception e) {
		final IStatus status = new Status(IStatus.ERROR, "uk.ac.diamond.daq.mapping.ui", errorMessage, e);
		ErrorDialog.openError(getShell(),"Error", errorMessage, status);
		logger.error(errorMessage, e);
	}

	@Override
	public boolean close() {
		if (dialogComposite != null) {
			Arrays.stream(dialogComposite.getChildren()).forEach(Widget::dispose);
		}
		return super.close();
	}
}
