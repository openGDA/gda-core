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

import static gda.jython.JythonStatus.RUNNING;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyUtils.CALIBRATION_DIRECTORY_PATH;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyUtils.CALIBRATION_FILE_PATH;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyUtils.populateScriptService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
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
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.TomographyCalibrationData;
import uk.ac.diamond.daq.mapping.api.TomographyCalibrationData.TomographyAxisCalibration;
import uk.ac.diamond.daq.mapping.api.TomographyParams;
import uk.ac.diamond.daq.mapping.ui.experiment.MappingExperimentView;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanRequestConverter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Sine;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer;

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

	private static final int TABLE_Y_SIZE = 250;
	private static final int TABLE_COLUMN_WIDTH = 80;
	private static final int CALIBRATION_TITLE_WIDTH = 60;
	private static final int CALIBRATION_VALUE_WIDTH = 100;
	private static final DecimalFormat DF = new DecimalFormat("#.#####");

	private String[] headers;
	private CSVFormat csvFormat;
	private static final String CSV_DELIMITER = ",";

	private final IScannableMotor rotationMotor;
	private final IScannableMotor xMotor;
	private final IScannableMotor yMotor;
	private final IScannableMotor zMotor;

	private final MappingExperimentView mappingView;
	private final String tomoScript;

	private TableViewer positionTableViewer;

	private Button removeButton;
	private Button calibrateButton;
	private Button dryRunButton;
	private Button okButton;
	private Button applyButton;
	private Button saveButton;

	// Entries in the "current calibration" table
	private CalibrationTableEntry xAmp;
	private CalibrationTableEntry xFreq;
	private CalibrationTableEntry xPhase;
	private CalibrationTableEntry xMean;

	private CalibrationTableEntry zAmp;
	private CalibrationTableEntry zFreq;
	private CalibrationTableEntry zPhase;
	private CalibrationTableEntry zMean;

	private List<CalibrationTableEntry> calibrationEntries;

	// Current calibration data
	private TomographyAxisCalibration xCalibration;
	private TomographyAxisCalibration zCalibration;
	private boolean unsavedCalibration = false;

	private List<PositionTableEntry> tableData = new ArrayList<>();

	private enum Motor {
		R("Rotation", "rot"), X("SampleX", "x"), Y("SampleY", "y"), Z("SampleZ", "z");

		private String label;
		private String header;

		private Motor(String label, String header) {
			this.label = label;
			this.header = header;
		}

		public String getLabel() {
			return label;
		}

		public String getHeader() {
			return header;
		}

	}

	private List<Motor> allMotors = List.of(Motor.R, Motor.X, Motor.Y, Motor.Z);

	public TomographyConfigurationDialog(Shell parentShell, String rotationMotor, String xMotor, String yMotor, String zMotor, MappingExperimentView mappingView, String tomoScript) {
		super(parentShell);
		setShellStyle(SWT.RESIZE);

		this.rotationMotor = Finder.find(rotationMotor);
		this.xMotor = Finder.find(xMotor);
		this.yMotor = Finder.find(yMotor);
		this.zMotor = Finder.find(zMotor);
		this.mappingView = mappingView;
		this.tomoScript = tomoScript;

		headers = Arrays.stream(Motor.values()).map(Motor::getHeader).toArray(String[]::new);

		csvFormat = CSVFormat.DEFAULT
				.withHeader(headers)
				.withSkipHeaderRecord()
				.withIgnoreSurroundingSpaces(true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configure tomography parameters");
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
		applyButton = createButton(parent, IDialogConstants.NO_ID, "Apply", false);
		applyButton.addSelectionListener(widgetSelectedAdapter(e -> saveCalibrationToFile()));
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		setButtonStates();
	}

	@Override
	protected void okPressed() {
		saveCalibrationToFile();
		super.okPressed();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite dialogComposite = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().applyTo(dialogComposite);

		final Composite mainComposite = createComposite(dialogComposite, 4);
		// Stage positions
		final Composite positionCompsite = createComposite(mainComposite, 1);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(positionCompsite);
		createPositionTable(positionCompsite, allMotors);
		createPositionButtons(positionCompsite);

		// Sine wave parameters
		final Composite calibrationComposite = createComposite(mainComposite, 1);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(calibrationComposite);
		createCalibrationTable(calibrationComposite);

		return dialogComposite;
	}

	/**
	 * Create a table to capture motor positions
	 *
	 * @param parent
	 *            the parent {@link Composite}
	 */
	private void createPositionTable(Composite parent, List<Motor> motors) {
		positionTableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridDataFactory.swtDefaults().indent(5, 0).hint(SWT.DEFAULT, TABLE_Y_SIZE).applyTo(positionTableViewer.getTable());
		positionTableViewer.setContentProvider(ArrayContentProvider.getInstance());

		motors.stream()
		.forEach(m -> createColumn(positionTableViewer, m.getLabel()).setLabelProvider(new PositionLabelProvider(m)));

		// Set table input
		positionTableViewer.setInput(tableData);
		positionTableViewer.addSelectionChangedListener(e -> handleSelectionChanged());

		// make lines and header visible
		final Table table = positionTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	private static TableViewerColumn createColumn(TableViewer viewer, String title) {
		final TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(TABLE_COLUMN_WIDTH);
		column.getColumn().setText(title);
		return column;
	}

	private void handleSelectionChanged() {
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
		final Composite buttonsComposite = createComposite(parent, 4);

		final Button captureButton = createButton(buttonsComposite, "Capture", "Capture the current stage position");
		captureButton.addSelectionListener(widgetSelectedAdapter(e -> capturePosition()));

		removeButton = createButton(buttonsComposite, "Remove", "Remove the selected position(s)");
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(widgetSelectedAdapter(e -> removeSelectedPositions()));

		calibrateButton = createButton(buttonsComposite, "Calibrate", "Perform calibration based on selected stage positions");
		calibrateButton.addSelectionListener(widgetSelectedAdapter(e -> performCalibration()));

		dryRunButton = createButton(buttonsComposite, "Dry run", "Run a scan using the current calibration");
		dryRunButton.addSelectionListener(widgetSelectedAdapter(e -> Async.execute(this::performDryRun)));

		final Button loadButton = createButton(buttonsComposite, "Load", "Load stage positions from a file");
		loadButton.addSelectionListener(widgetSelectedAdapter(e -> loadPositions()));

		saveButton = createButton(buttonsComposite, "Save", "Save stage positions to a file");
		saveButton.addSelectionListener(widgetSelectedAdapter(e -> savePositions()));
	}

	private static Button createButton(Composite parent, String text, String tooltip) {
		final Button button = new Button(parent, SWT.PUSH);
		GridDataFactory.fillDefaults().applyTo(button);
		button.setText(text);
		button.setToolTipText(tooltip);
		return button;
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
		clearCalibrationData();
		setButtonStates();
	}

	/**
	 * Show the sine wave parameters for the x & z axes
	 */
	private void createCalibrationTable(Composite parent) {
		final Group calibrationGroup = new Group(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(calibrationGroup);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(calibrationGroup);
		calibrationGroup.setText("Current calibration");

		final Composite xValuesComposite = createComposite(calibrationGroup, 3);
		xMean = new CalibrationTableEntry(xValuesComposite, "x_mean");
		xAmp = new CalibrationTableEntry(xValuesComposite, "x_amp");
		xFreq = new CalibrationTableEntry(xValuesComposite, "x_freq");
		xPhase = new CalibrationTableEntry(xValuesComposite, "x_phase");

		final Composite zValuesComposite = createComposite(calibrationGroup, 3);
		zMean = new CalibrationTableEntry(zValuesComposite, "z_mean");
		zAmp = new CalibrationTableEntry(zValuesComposite, "z_amp");
		zFreq = new CalibrationTableEntry(zValuesComposite, "z_freq");
		zPhase = new CalibrationTableEntry(zValuesComposite, "z_phase");

		calibrationEntries = List.of(xMean, xAmp, xFreq, xPhase ,zMean, zAmp, zFreq, zPhase);
	}

	/**
	 * Calculate sine wave parameters for x & z motors based on the focus positions in the table
	 */
	private void performCalibration() {
		final int numRows = tableData.size();
		final double[] rotationPositions = new double[numRows];
		final double[] xPositions = new double[numRows];
		final double[] zPositions = new double[numRows];

		IntStream.range(0, numRows)
		.forEach(idx -> {
			final PositionTableEntry entry = tableData.get(idx);
			rotationPositions[idx] = entry.getrValue();
			xPositions[idx] = entry.getxValue();
			zPositions[idx] = entry.getzValue();

		});

		xCalibration = fitSine(rotationPositions, xPositions);
		xAmp.setValue(xCalibration.getAmplitude());
		xFreq.setValue(xCalibration.getFrequency());
		xPhase.setValue(xCalibration.getPhase());
		xMean.setValue(xCalibration.getMean());

		zCalibration = fitSine(rotationPositions, zPositions);
		zAmp.setValue(zCalibration.getAmplitude());
		zFreq.setValue(zCalibration.getFrequency());
		zPhase.setValue(zCalibration.getPhase());
		zMean.setValue(zCalibration.getMean());

		unsavedCalibration = true;
		setButtonStates();
	}

	private TomographyAxisCalibration fitSine(double[] xData, double[] yData) {
		final double guessAmp = 1;
		final double guessPhase = 0;
		final double guessFreq = 0.02;
		final double guessMean = Arrays.stream(yData).average().orElse(1.0);

		final ApacheOptimizer leastsq = new ApacheOptimizer(ApacheOptimizer.Optimizer.LEVENBERG_MARQUARDT);
		final Sine sineFunction = new Sine(new double[] { guessAmp, guessFreq, guessPhase, guessMean });
		final IDataset[] coordinates = { DatasetFactory.createFromList(Arrays.asList(xData)) };
		final IDataset data = DatasetFactory.createFromList(Arrays.asList(yData));

		try {
			leastsq.optimize(coordinates, data, sineFunction);
		} catch (Exception e) {
			logger.error("Error doing fit", e);
		}

		final double[] params = leastsq.getParameterValues();
		return new TomographyAxisCalibration(params[0], params[1], params[2], params[3]);
	}

	private void clearCalibrationData() {
		xCalibration = null;
		zCalibration = null;
		calibrationEntries.stream().forEach(x -> x.setValue(null));
	}

	private void performDryRun() {
		try {
			// Put calibration data etc. into script context
			final IScriptService scriptService = getService(IScriptService.class);
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			final ScanRequest scanRequest = getScanRequest(mappingView.getBean());
			final TomographyParams tomoParams = new TomographyParams();
			tomoParams.setTomographyCalibration(new TomographyCalibrationData(xCalibration, zCalibration));

			populateScriptService(scriptService, marshallerService, scanRequest, tomoParams);

			// Run script
			final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
			setDryRunEnabled(false);
			logger.info("Running tomography scan");
			jythonServerFacade.runScript(new File(jythonServerFacade.locateScript(tomoScript)));
			while (jythonServerFacade.getScriptStatus() == RUNNING) {
				Thread.sleep(500);
			}
			logger.info("Finished running tomography scan");
		} catch (Exception e) {
			logger.error("Error running tomography scan", e);
		} finally {
			setDryRunEnabled(true);
		}
	}

	private <S> S getService(Class<S> serviceClass) {
		return mappingView.getEclipseContext().get(serviceClass);
	}

	private ScanRequest getScanRequest(final IMappingExperimentBean mappingBean) {
		final ScanRequestConverter converter = getService(ScanRequestConverter.class);
		return converter.convertToScanRequest(mappingBean);
	}

	private void setDryRunEnabled(boolean enabled) {
		Display.getDefault().syncExec(() -> dryRunButton.setEnabled(enabled));
	}

	/**
	 * Save calibration data to a file, so that the Submit button can read it.
	 */
	private void saveCalibrationToFile() {
		CALIBRATION_DIRECTORY_PATH.toFile().mkdirs();
		final File outputFile = CALIBRATION_FILE_PATH.toFile();

		try (BufferedWriter outputWriter = Files.newBufferedWriter(CALIBRATION_FILE_PATH, CREATE)) {
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			final TomographyCalibrationData tomoCalibration = new TomographyCalibrationData(xCalibration, zCalibration);
			outputWriter.write(marshallerService.marshal(tomoCalibration));

			logger.debug("Calibration data written to {}", CALIBRATION_FILE_PATH);
			unsavedCalibration = false;
			setButtonStates();
		} catch (Exception e) {
			final String message = String.format("Error saving tomography calibration %s", outputFile.getName());
			handleException(message, e);
		}
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
			.map(e -> createRow(e.getrValue(), e.getxValue(), e.getyValue(), e.getzValue()))
			.forEach(row -> {
					try {
						writer.write(row);
						writer.newLine();
					} catch (IOException e1) {
						handleException("Save stage positions to a file", e1);
					}
			});


		} catch(IOException e) {
			handleException("Save stage positions to a file", e);
		}
	}

	private String createRow(double r, double x, double y, double z) {
		return String.join(CSV_DELIMITER, DF.format(r), DF.format(x), DF.format(y), DF.format(z));
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
				.forEach(r -> tableData.add(parseData(r.get(Motor.R),r.get(Motor.X), r.get(Motor.Y), r.get(Motor.Z))));
			handlePositionTableChange();
		} catch (Exception e) {
			logger.error("Error reading file {}", selected, e);
		}
	}

	private PositionTableEntry parseData(String r, String x, String y, String z) {
		return new PositionTableEntry(Double.parseDouble(r),
				Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
	}

	private String selectPositionsFile(int style) {
		final FileDialog fileDialog = new FileDialog(getShell(), style);
		fileDialog.setText("Select configuration");
		fileDialog.setFilterPath(InterfaceProvider.getPathConstructor().getClientVisitDirectory());
		fileDialog.setFilterExtensions(new String[] { "*.csv", "*.*" });
        fileDialog.setFilterNames(new String[] { "Stage positions(*.csv)", "All files(*.*)" });

        return fileDialog.open();
	}

	private void setButtonStates() {
		// Buttons that should be active if there is something in the table
		final boolean tableHasData = !tableData.isEmpty();
		calibrateButton.setEnabled(tableHasData);
		saveButton.setEnabled(tableHasData);

		// Buttons that should be active if there is current calibration data
		final boolean calibrationExists = xCalibration != null && zCalibration != null;
		dryRunButton.setEnabled(calibrationExists);
		okButton.setEnabled(calibrationExists);

		// Apply button should be active if there is unsaved calibration
		applyButton.setEnabled(calibrationExists && unsavedCalibration);
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
			final double posValue;
			if (motor == Motor.X) {
				posValue = entry.getxValue();
			} else if (motor == Motor.Y) {
				posValue = entry.getyValue();
			} else if (motor == Motor.Z) {
				posValue = entry.getzValue();
			} else if (motor == Motor.R) {
				posValue = entry.getrValue();
			} else {
				posValue = 0.0;
			}
			return Double.toString(posValue);
		}
	}

	/**
	 * Data backing a row in the configuration table
	 */
	private static class PositionTableEntry {
		private final double rValue;
		private final double xValue;
		private final double yValue;
		private final double zValue;

		public PositionTableEntry(double rValue, double xValue, double yValue, double zValue) {
			this.rValue = rValue;
			this.xValue = xValue;
			this.yValue = yValue;
			this.zValue = zValue;
		}

		public double getrValue() {
			return rValue;
		}

		public double getxValue() {
			return xValue;
		}

		public double getyValue() {
			return yValue;
		}

		public double getzValue() {
			return zValue;
		}

		@Override
		public String toString() {
			return "PositionTableEntry [rValue=" + rValue + ", xValue=" + xValue + ", yValue=" + yValue + ", zValue="
					+ zValue + "]";
		}
	}

	/**
	 * Class that wraps a row in the "current calibration" table
	 * <p>
	 * It writes a "title" label an equals sign and the calibration value itself in 3 labels directly onto the parent
	 * composite.
	 */
	private static class CalibrationTableEntry {
		private Label valueLabel;

		public CalibrationTableEntry(Composite parent, String title) {
			final Label titleLabel = new Label(parent, SWT.NONE);
			GridDataFactory.swtDefaults().hint(CALIBRATION_TITLE_WIDTH, SWT.DEFAULT).applyTo(titleLabel);
			titleLabel.setText(title);

			final Label equalsLabel = new Label(parent, SWT.NONE);
			GridDataFactory.swtDefaults().applyTo(equalsLabel);
			equalsLabel.setText("=");

			valueLabel = new Label(parent, SWT.NONE);
			GridDataFactory.swtDefaults().hint(CALIBRATION_VALUE_WIDTH, SWT.DEFAULT).applyTo(valueLabel);
		}

		public void setValue(Double value) {
			final String text = value != null ? DF.format(value) : "";
			valueLabel.setText(text);
		}
	}

	private Composite createComposite(Composite parent, int numColumns) {
		final Composite composite = CompositeFactory.newComposite(SWT.NONE).create(parent);
		GridDataFactory.swtDefaults().applyTo(composite);
		GridLayoutFactory.swtDefaults().numColumns(numColumns).applyTo(composite);
		return composite;
	}

	private void handleException(String errorMessage, Exception e) {
		final IStatus status = new Status(IStatus.ERROR, "uk.ac.diamond.daq.mapping.ui", errorMessage, e);
		ErrorDialog.openError(getShell(),"Error", errorMessage, status);
		logger.error(errorMessage, e);
	}
}
