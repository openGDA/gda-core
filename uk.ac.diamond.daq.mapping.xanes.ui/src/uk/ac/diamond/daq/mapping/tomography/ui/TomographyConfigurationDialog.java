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

package uk.ac.diamond.daq.mapping.tomography.ui;

import static gda.jython.JythonStatus.RUNNING;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.tomography.ui.TomographyUtils.CALIBRATION_DIRECTORY_PATH;
import static uk.ac.diamond.daq.mapping.tomography.ui.TomographyUtils.CALIBRATION_FILE_PATH;
import static uk.ac.diamond.daq.mapping.tomography.ui.TomographyUtils.getClientMessage;
import static uk.ac.diamond.daq.mapping.tomography.ui.TomographyUtils.populateScriptService;
import static uk.ac.gda.ui.tool.ClientMessages.CONFIRM_FILE_OVERWRITE;
import static uk.ac.gda.ui.tool.ClientMessages.CONFIRM_FILE_OVERWRITE_TITLE;
import static uk.ac.gda.ui.tool.ClientMessages.ERROR;
import static uk.ac.gda.ui.tool.ClientMessages.LOAD;
import static uk.ac.gda.ui.tool.ClientMessages.SAVE;
import static uk.ac.gda.ui.tool.ClientMessages.SELECT_CONFIGURATION;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_CALIBRATE;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_CALIBRATE_APPLY;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_CALIBRATE_DRY_RUN;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_CALIBRATE_DRY_RUN_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_CALIBRATE_MESSAGE;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_CALIBRATE_SAVE_FILE_ERROR;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_CALIBRATE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_CAPTURE;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_CAPTURE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_CONFIGURE_PARAMS;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_LOAD_POSITIONS_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_REMOVE;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_REMOVE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_SAVE_POSITIONS_TOOLTIP;

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
import uk.ac.gda.ui.tool.ClientMessages;

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

	private static final String X_AMP = "x_amp";
	private static final String X_FREQ = "x_freq";
	private static final String X_PHASE = "x_phase";
	private static final String X_MEAN = "x_mean";

	private static final String Z_AMP = "z_amp";
	private static final String Z_FREQ = "z_freq";
	private static final String Z_PHASE = "z_phase";
	private static final String Z_MEAN = "z_mean";

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

	// Current calibration data
	private TomographyAxisCalibration xCalibration;
	private TomographyAxisCalibration zCalibration;
	private boolean unsavedCalibration = false;

	private List<PositionTableEntry> tableData = new ArrayList<>();

	private enum TomoMotor {
		R, X, Y, Z
	}

	public TomographyConfigurationDialog(Shell parentShell, String rotationMotor, String xMotor, String yMotor, String zMotor, MappingExperimentView mappingView, String tomoScript) {
		super(parentShell);
		setShellStyle(SWT.RESIZE);

		final Finder finder = Finder.getInstance();
		this.rotationMotor = finder.find(rotationMotor);
		this.xMotor = finder.find(xMotor);
		this.yMotor = finder.find(yMotor);
		this.zMotor = finder.find(zMotor);
		this.mappingView = mappingView;
		this.tomoScript = tomoScript;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(getClientMessage(TOMO_CONFIGURE_PARAMS));
	}

	@Override
    public void create() {
        super.create();
        setTitle(getClientMessage(ClientMessages.TOMO_CALIBRATE_TITLE));
        setMessage(getClientMessage(TOMO_CALIBRATE_MESSAGE), IMessageProvider.INFORMATION);
    }

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		applyButton = createButton(parent, IDialogConstants.NO_ID, getClientMessage(TOMO_CALIBRATE_APPLY), false);
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
		final Composite mainComposite = new Composite(dialogComposite, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(mainComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(mainComposite);

		// Stage positions
		final Composite positionCompsite = new Composite(mainComposite, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(positionCompsite);
		GridLayoutFactory.swtDefaults().applyTo(positionCompsite);
		createPositionTable(positionCompsite);
		createPositionButtons(positionCompsite);

		// Sine wave parameters
		final Composite calibrationComposite = new Composite(mainComposite, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(calibrationComposite);
		GridLayoutFactory.swtDefaults().applyTo(calibrationComposite);
		createCalibrationTable(calibrationComposite);

		return dialogComposite;
	}

	/**
	 * Create a table to capture motor positions
	 *
	 * @param parent
	 *            the parent {@link Composite}
	 */
	private void createPositionTable(Composite parent) {
		positionTableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridDataFactory.swtDefaults().indent(5, 0).hint(SWT.DEFAULT, TABLE_Y_SIZE).applyTo(positionTableViewer.getTable());
		positionTableViewer.setContentProvider(ArrayContentProvider.getInstance());

		// Create columns
		final TableViewerColumn sampleRColumn = createColumn(positionTableViewer, "SampleR");
		sampleRColumn.setLabelProvider(new PositionLabelProvider(TomoMotor.R));

		final TableViewerColumn sampleXColumn = createColumn(positionTableViewer, "SampleX");
		sampleXColumn.setLabelProvider(new PositionLabelProvider(TomoMotor.X));

		final TableViewerColumn sampleYColumn = createColumn(positionTableViewer, "SampleY");
		sampleYColumn.setLabelProvider(new PositionLabelProvider(TomoMotor.Y));

		final TableViewerColumn sampleZColumn = createColumn(positionTableViewer, "SampleZ");
		sampleZColumn.setLabelProvider(new PositionLabelProvider(TomoMotor.Z));

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
		final Composite buttonsComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(buttonsComposite);
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(buttonsComposite);

		final Button captureButton = createButton(buttonsComposite, getClientMessage(TOMO_CAPTURE), getClientMessage(TOMO_CAPTURE_TOOLTIP));
		captureButton.addSelectionListener(widgetSelectedAdapter(e -> capturePosition()));

		removeButton = createButton(buttonsComposite, getClientMessage(TOMO_REMOVE), getClientMessage(TOMO_REMOVE_TOOLTIP));
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(widgetSelectedAdapter(e -> removeSelectedPositions()));

		calibrateButton = createButton(buttonsComposite, getClientMessage(TOMO_CALIBRATE), getClientMessage(TOMO_CALIBRATE_TOOLTIP));
		calibrateButton.addSelectionListener(widgetSelectedAdapter(e -> performCalibration()));

		dryRunButton = createButton(buttonsComposite, getClientMessage(TOMO_CALIBRATE_DRY_RUN), getClientMessage(TOMO_CALIBRATE_DRY_RUN_TOOLTIP));
		dryRunButton.addSelectionListener(widgetSelectedAdapter(e -> Async.execute(this::performDryRun)));

		final Button loadButton = createButton(buttonsComposite, getClientMessage(LOAD), getClientMessage(TOMO_LOAD_POSITIONS_TOOLTIP));
		loadButton.addSelectionListener(widgetSelectedAdapter(e -> loadPositions()));

		saveButton = createButton(buttonsComposite, getClientMessage(SAVE), getClientMessage(TOMO_SAVE_POSITIONS_TOOLTIP));
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
		for (Object entry : positionTableViewer.getStructuredSelection().toArray()) {
			tableData.remove(entry);
		}
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

		final Composite xValuesComposite = new Composite(calibrationGroup, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(xValuesComposite);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(xValuesComposite);
		xMean = new CalibrationTableEntry(xValuesComposite, X_MEAN);
		xAmp = new CalibrationTableEntry(xValuesComposite, X_AMP);
		xFreq = new CalibrationTableEntry(xValuesComposite, X_FREQ);
		xPhase = new CalibrationTableEntry(xValuesComposite, X_PHASE);

		final Composite zValuesComposite = new Composite(calibrationGroup, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(zValuesComposite);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(zValuesComposite);
		zMean = new CalibrationTableEntry(zValuesComposite, Z_MEAN);
		zAmp = new CalibrationTableEntry(zValuesComposite, Z_AMP);
		zFreq = new CalibrationTableEntry(zValuesComposite, Z_FREQ);
		zPhase = new CalibrationTableEntry(zValuesComposite, Z_PHASE);
	}

	/**
	 * Calculate sine wave parameters for x & z motors based on the focus positions in the table
	 */
	private void performCalibration() {
		final int numRows = tableData.size();
		final double[] rotationPositions = new double[numRows];
		final double[] xPositions = new double[numRows];
		final double[] zPositions = new double[numRows];

		for (int i = 0; i < numRows; i++) {
			final PositionTableEntry entry = tableData.get(i);
			rotationPositions[i] = entry.getrValue();
			xPositions[i] = entry.getxValue();
			zPositions[i] = entry.getzValue();
		}

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
		xAmp.setValue(null);
		xFreq.setValue(null);
		xPhase.setValue(null);
		xMean.setValue(null);

		zCalibration = null;
		zAmp.setValue(null);
		zFreq.setValue(null);
		zPhase.setValue(null);
		zMean.setValue(null);
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
			jythonServerFacade.runScript(tomoScript);
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
			final String message = String.format(getClientMessage(TOMO_CALIBRATE_SAVE_FILE_ERROR), outputFile.getName());
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
			final String title = getClientMessage(CONFIRM_FILE_OVERWRITE_TITLE);
			final String message = String.format(getClientMessage(CONFIRM_FILE_OVERWRITE), outputFile.getName());
			if (!MessageDialog.openConfirm(getShell(), title, message)) {
				return;
			}
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
			writer.write("# rotation, x, y, z");
			writer.newLine();
			for (PositionTableEntry entry : tableData) {
				writer.write(Double.toString(entry.getrValue()) + ", ");
				writer.write(Double.toString(entry.getxValue()) + ", ");
				writer.write(Double.toString(entry.getyValue()) + ", ");
				writer.write(Double.toString(entry.getzValue()));
				writer.newLine();
			}
		} catch (IOException e) {
			handleException(getClientMessage(ClientMessages.TOMO_SAVE_POSITIONS_ERROR), e);
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
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.charAt(0) != '#') {
					final PositionTableEntry entry = parsePositions(line);
					if (entry != null) {
						tableData.add(entry);
					}
				}
			}
			handlePositionTableChange();
		} catch (Exception e) {
			logger.error("Error reading file {}", selected, e);
		}
	}

	private String selectPositionsFile(int style) {
		final FileDialog fileDialog = new FileDialog(getShell(), style);
		fileDialog.setText(getClientMessage(SELECT_CONFIGURATION));
		fileDialog.setFilterExtensions(new String[] { "*.pos", "*.*" });
        fileDialog.setFilterNames(new String[] { "Stage positions(*.pos)", "All files(*.*)" });

        return fileDialog.open();
	}

	private PositionTableEntry parsePositions(String line) {
		final String[] motorPositions = line.split("(\\s|,|;|:)+");
		if (motorPositions.length != 4) {
			logger.warn("Invalid positions entry ignored: {}", line);
			return null;
		}
		return new PositionTableEntry(Double.parseDouble(motorPositions[0]),
				Double.parseDouble(motorPositions[1]),
				Double.parseDouble(motorPositions[2]),
				Double.parseDouble(motorPositions[3]));
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

	private void handleException(String errorMessage, Exception e) {
		final IStatus status = new Status(IStatus.ERROR, "uk.ac.diamond.daq.mapping.xanes.ui", errorMessage, e);
		ErrorDialog.openError(getShell(), getClientMessage(ERROR), errorMessage, status);
		logger.error(errorMessage, e);
	}

	/**
	 * Format the position of the table entry for the given motor
	 */
	private static class PositionLabelProvider extends ColumnLabelProvider {
		private final TomoMotor motor;

		public PositionLabelProvider(TomoMotor motor) {
			this.motor = motor;
		}

		@Override
		public String getText(Object element) {
			final PositionTableEntry entry = (PositionTableEntry) element;
			final double posValue;
			if (motor == TomoMotor.X) {
				posValue = entry.getxValue();
			} else if (motor == TomoMotor.Y) {
				posValue = entry.getyValue();
			} else if (motor == TomoMotor.Z) {
				posValue = entry.getzValue();
			} else if (motor == TomoMotor.R) {
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
		private final DecimalFormat format = new DecimalFormat("#.##########");
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
			if (value == null) {
				valueLabel.setText("");
			} else {
				valueLabel.setText(format.format(value));
			}
		}
	}
}
