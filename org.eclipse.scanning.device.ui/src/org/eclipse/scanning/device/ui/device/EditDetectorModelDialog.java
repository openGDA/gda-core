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

package org.eclipse.scanning.device.ui.device;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Optional;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.ProgressMonitorWrapper;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.util.ScanningUiUtils;
import org.eclipse.scanning.event.util.SubmissionQueueUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog providing a simple workflow in configuring detector:
 * <ul><li> set detector exposure (or other parameters)
 * <li> take snapshot to evaluate choice of parameters </ul>
 * Can plot as image or spectrum, and provides statistics particular to those modes
 */
public class EditDetectorModelDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(EditDetectorModelDialog.class);

	private static final int LAYOUT_MARGIN = 5;
	private static final int PLOT_SIZE = 600;

	/**
	 * If first dimension of dataset is greater than this, we are probably dealing with an image,
	 * rather than a series of spectra
	 */
	private static final int MAX_ELEMENTS = 20;

	private final IRunnableDeviceService runnableDeviceService; // the remote service
	private final IDetectorModel detectorModel;
	private final String detectorLabel;
	private IPlottingSystem<Composite> plottingSystem;

	private IDataset dataset;
	private boolean isImage = true;

	private Text snapshotPathText;
	private Composite statisticsComposite;
	private Group statistics;
	private Button plotAsLine;
	private Button plotAsImage;

	private Combo datasetCombo;

	private Optional<MalcolmModelEditor> malcolmModelEditor = null;

	public EditDetectorModelDialog(final Shell parentShell, final IRunnableDeviceService runnableDeviceService,
			final IDetectorModel detectorModel, final String detectorName) {
		super(parentShell);
		this.runnableDeviceService = runnableDeviceService;
		this.detectorModel = detectorModel;
		this.detectorLabel = detectorName;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite dialogComposite = (Composite) super.createDialogArea(parent);

		// Overall layout is a 2-column grid
		GridDataFactory.fillDefaults().grab(true, true).applyTo(dialogComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).margins(LAYOUT_MARGIN, LAYOUT_MARGIN).applyTo(dialogComposite);

		createSnapshotSection(dialogComposite);

		createDetectorSection(dialogComposite);

		return dialogComposite;
	}

	private void createDetectorSection(final Composite dialogComposite) {
		// Detector parameters
		final Composite parametersComposite = new Composite(dialogComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().applyTo(parametersComposite);
		GridLayoutFactory.fillDefaults().margins(LAYOUT_MARGIN, LAYOUT_MARGIN).applyTo(parametersComposite);

		// Detector name
		createStyledTextLabel(parametersComposite, detectorModel.getName());

		// Parameters
		final Group parameters = createGroup(parametersComposite, "Parameters", 1);

		final Control editor = createDetectorModelEditor(parameters);
		GridDataFactory.fillDefaults().applyTo(editor);

		createPlotTypeSection(parametersComposite);

		statisticsComposite = new Composite(parametersComposite, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(statisticsComposite);
		GridLayoutFactory.fillDefaults().applyTo(statisticsComposite);

		createStatsSection();
	}

	private void createPlotTypeSection(final Composite parametersComposite) {
		// Radio buttons to alternate between image and line plot
		final Group plotStyle = createGroup(parametersComposite, "Plot type", 2);
		SelectionListener radioListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				isImage = "Image".equals(( (Button) event.widget).getText());
				createStatsSection();
				if (dataset!=null) {
					updatePlot();
				}
			}
		};

		plotAsImage = new Button(plotStyle, SWT.RADIO);
		plotAsImage.setText("Image");
		plotAsImage.setSelection(true);
		plotAsImage.addSelectionListener(radioListener);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(plotAsImage);

		plotAsLine = new Button(plotStyle, SWT.RADIO);
		plotAsLine.setText("Line");
		plotAsLine.addSelectionListener(radioListener);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(plotAsLine);
	}

	private void createSnapshotSection(final Composite dialogComposite) {
		// Snapshot
		final Composite snapshotComposite = new Composite(dialogComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().minSize(PLOT_SIZE + 100, PLOT_SIZE).grab(true, true).applyTo(snapshotComposite);
		GridLayoutFactory.fillDefaults().margins(LAYOUT_MARGIN, LAYOUT_MARGIN).applyTo(snapshotComposite);

		// Plot: spans all columns
		final Control plot = createPlot(snapshotComposite);
		plottingSystem.getSelectedXAxis().setTitle("");
		GridDataFactory.fillDefaults().minSize(PLOT_SIZE, PLOT_SIZE).grab(true, true).applyTo(plot);

		final boolean isMalcolm = detectorModel instanceof IMalcolmModel;
		Composite plotControlsComposite = new Composite(snapshotComposite, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(plotControlsComposite);
		GridLayoutFactory.fillDefaults().numColumns(isMalcolm ? 3 : 2).applyTo(plotControlsComposite);

		// Snapshot message, either the path of the snapshot or an error message
		snapshotPathText = new Text(plotControlsComposite, SWT.NULL);
		GridDataFactory.fillDefaults().grab(true, false).span(isMalcolm ? 3 : 1, 1).applyTo(snapshotPathText);
		snapshotPathText.setEditable(false);
		snapshotPathText.setBackground(snapshotComposite.getBackground());
		snapshotPathText.setVisible(false);

		if (isMalcolm) {
			// add combo with label to choose dataset
			Label label = new Label(plotControlsComposite, SWT.NONE);
			label.setText("Dataset:");
			GridDataFactory.swtDefaults().applyTo(label);

			datasetCombo = new Combo(plotControlsComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
			final String[] datasetNames = getMalcolmDatasetNames();
			if (datasetNames.length > 0) {
				datasetCombo.setItems(datasetNames);
				datasetCombo.select(0);
			}

			GridDataFactory.fillDefaults().grab(true, false).applyTo(datasetCombo);
		}

		// Snapshot button
		final Button snapshotButton = new Button(plotControlsComposite, SWT.PUSH);
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.FILL).applyTo(snapshotButton);
		snapshotButton.setImage(Activator.getImage("icons/camera.png"));
		snapshotButton.setToolTipText("Take snapshot");
		snapshotButton.addListener(SWT.Selection, event -> takeSnapshot());
	}

	private String[] getMalcolmDatasetNames() {
		IMalcolmDevice malcolmDevice = null;
		try {
			malcolmDevice = (IMalcolmDevice) runnableDeviceService.<IMalcolmModel>getRunnableDevice(detectorModel.getName());
			if (malcolmDevice.getDeviceState() != DeviceState.READY) {
				throw new ScanningException("The malcolm device is not ready. A scan may be running");
			}
			// configure the detector, puts it in 'Armed' state
			malcolmDevice.configure((IMalcolmModel) detectorModel);
			final MalcolmTable datasetsTable = malcolmDevice.getDatasets();
			return datasetsTable.stream()
				.filter(row -> MalcolmDatasetType.fromString((String) row.get(MalcolmConstants.DATASETS_TABLE_COLUMN_TYPE)) == MalcolmDatasetType.PRIMARY) // filter out non-primary dataset
				.map(row -> row.get(MalcolmConstants.DATASETS_TABLE_COLUMN_NAME))
				.map(String.class::cast)
				.filter(name -> name.contains(".")) // sanity check, dataset names have 2 parts, e.g. 'detector.data'
				.map(name -> name.split("\\.")[0]) // get the first part, e.g. 'detector'. This will be the name of the NXdata group in the nexus file
				.toArray(String[]::new);
		} catch (ScanningException e) {
			logger.error("Could not get datasets for malcolm device {}", detectorModel.getName(), e);
		} finally {
			if (malcolmDevice != null) {
				try {
					malcolmDevice.reset();
				} catch (ScanningException e) {
					logger.error("Could not reset malcolm device {}", detectorModel.getName(), e);
				}
			}
		}
		return new String[0];
	}

	private Control createDetectorModelEditor(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(composite);

		final Control modelEditorControl;
		if (detectorModel instanceof IMalcolmModel) {
			// create a MalcolmModelEditor for malcolm devices
			MalcolmModelEditor malcolmModelEditor = new MalcolmModelEditor(runnableDeviceService, (IMalcolmModel) detectorModel);
			modelEditorControl = malcolmModelEditor.createEditorPart(parent);
			this.malcolmModelEditor = Optional.of(malcolmModelEditor);
			validateModel(true); // the malcolm dialog needs an initial validation
		} else {
			// for software detectors use the gui generator to generate a gui to edit the detector model
			final IGuiGeneratorService guiGenerator = Activator.getDefault().getService(IGuiGeneratorService.class);
			modelEditorControl = guiGenerator.generateGui(detectorModel, parent);
		}
		GridDataFactory.fillDefaults().applyTo(modelEditorControl);

		final Button validateButton = new Button(parent, SWT.PUSH);
		validateButton.setText("Validate");
		validateButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> validateModel(false)));

		return composite;
	}

	private void validateModel(boolean initialValidation) {
		final Job validateJob = Job.create("Validate detector model", (ICoreRunnable) monitor -> {
			Object result;
			try {
				result = validate(detectorModel);
			} catch (Exception e) {
				result = e;
			}

			displayValidationResult(result, initialValidation);
		});

		validateJob.schedule();
	}

	private IDetectorModel validate(IDetectorModel model) throws ScanningException {
		final IRunnableDevice<IDetectorModel> detector = runnableDeviceService.getRunnableDevice(model.getName());
		return detector.validateWithReturn(model);
	}

	private void displayValidationResult(Object result, boolean initialValidation) {
		getShell().getDisplay().asyncExec(() -> {
			if (result instanceof ValidationException) {
				MessageDialog.openError(getShell(), "Validation Error",
						"The given configuration is invalid: " + ((Exception) result).getMessage());
			} else if (result instanceof Exception) {
				logger.error("Error getting device '{}'", detectorModel.getName(), (Exception) result);
				MessageDialog.openError(getShell(), "Error", "Could not get device " + detectorLabel);
			} else if (!initialValidation) {
				// only show message for ok if button pressed
				MessageDialog.openInformation(getShell(), "Validation Successful", "The given configuration is valid.");
			}
			malcolmModelEditor.ifPresent(editor -> editor.updateValidatedModel(
					result instanceof IMalcolmModel ? (IMalcolmModel) result : null));
		});
	}

	private void createStatsSection() {

		if (statistics != null) {
			statistics.dispose();
			statistics = null;
		}

		statistics = createGroup(statisticsComposite, "Statistics", 2);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(statistics);

		SnapshotStatsViewer statsViewer = isImage ? new ImageSnapshotStatsViewer(statistics) : new LineSnapshotStatsViewer(statistics);
		if (dataset!=null) statsViewer.update(dataset, detectorModel.getExposureTime());

		statisticsComposite.layout(true, true);
	}

	private static StyledText createStyledTextLabel(final Composite parent, final String text) {
		final StyledText label = new StyledText(parent, SWT.NULL);
		final Font parametersLabelFont = label.getFont();
		final FontDescriptor descriptor = FontDescriptor.createFrom(parametersLabelFont)
				.setStyle(SWT.BOLD).increaseHeight(1);
		GridDataFactory.fillDefaults().applyTo(label);
		label.setFont(descriptor.createFont(label.getDisplay()));
		label.setEditable(false);
		label.setMargins(LAYOUT_MARGIN, LAYOUT_MARGIN, LAYOUT_MARGIN, LAYOUT_MARGIN);
		label.setBackground(parent.getBackground());
		label.setCaret(null);
		label.setText(text);

		return label;
	}

	private static Group createGroup(final Composite parent, final String title, final int columns) {
		final Group group = new Group(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(group);
		GridLayoutFactory.fillDefaults().numColumns(columns).margins(LAYOUT_MARGIN, LAYOUT_MARGIN).applyTo(group);

		final FontDescriptor descriptor = FontDescriptor.createFrom(group.getFont()).setStyle(SWT.BOLD);
		group.setFont(descriptor.createFont(group.getDisplay()));
		group.setText(title);

		return group;
	}

	private Control createPlot(final Composite parent) {
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			return ScanningUiUtils.createDataPlotControl(parent, plottingSystem, "Snapshot");
		} catch (Exception e) {
			final String message = "Could not create plotting system";
			logger.error(message, e);
			return ScanningUiUtils.createErrorLabel(parent, message, e);
		}
	}

	private void takeSnapshot() {
		final String messageTitle = "Snapshot";

		// to prevent AcquireRequest interrupting a running scan,
		// we'll check that no running/submitted scans are currently in the queue
		if (SubmissionQueueUtils.isJobRunningOrPending()) {
			String msg = "Cannot take snapshot while there are submitted or running scans.";
			logger.warn("{}\nAcquireRequest aborted",msg);
			MessageDialog.openInformation(getShell(), messageTitle, msg);
			return;
		}

		final String detectorName = detectorModel instanceof IMalcolmModel ?
				datasetCombo.getItem(datasetCombo.getSelectionIndex()) : detectorModel.getName();

		try {
			final AcquireRequest response = ScanningUiUtils.acquireData(detectorModel);

			if (response.getStatus() == Status.COMPLETE) {
				final String snapshotPath = response.getFilePath();
				loadSnapshot(snapshotPath, detectorName);
				snapshotPathText.setText(snapshotPath);
				snapshotPathText.setToolTipText(snapshotPath);
				snapshotPathText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
				snapshotPathText.setVisible(true);
			} else if (response.getStatus() == Status.FAILED) {
				final String message = MessageFormat.format("Unable to acquire data for detector {0}: {1}",
						detectorLabel, response.getMessage());
				MessageDialog.openError(getShell(), messageTitle, message);
				logger.error(message);
			}
		} catch (Exception e) {
			MessageDialog.openError(getShell(), messageTitle, "Error taking snapshot");
			logger.error("Error taking snapshot", e);
		}
	}

	private void loadSnapshot(final String filePath, String detectorName) throws Exception {
		logger.info("Loading snapshot from {}", filePath);

		try {
			new ProgressMonitorDialog(getShell()).run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						// Wait for file to be available on cluster
						final File file = new File(filePath);
						for (int i = 0; i < 10; i++) {
							if (file.exists()) {
								break;
							}
							Thread.sleep(200);
						}
						if (!file.exists()) {
							throw new FileNotFoundException(MessageFormat.format("The file {0} could not be found.", filePath));
						}

						final ILoaderService loaderService = Activator.getDefault().getService(ILoaderService.class);
						final String datasetPath = ScanningUiUtils.getDatasetPath(detectorName);
						dataset = loaderService.getDataset(filePath, datasetPath, new ProgressMonitorWrapper(monitor)).squeeze();

						if (dataset == null) {
							throw new IllegalArgumentException(MessageFormat.format("No path {0} found in file {1}", datasetPath, filePath));
						}
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			});
			// Guess how the dataset should be plotted
			if (dataset.getShape()[0] > MAX_ELEMENTS) {
				// probably an image
				plotAsImage.setSelection(true);
				plotAsLine.setSelection(false);
				isImage = true;
			} else {
				// probably line(s) plot
				plotAsLine.setSelection(true);
				plotAsImage.setSelection(false);
				isImage = false;
			}
			createStatsSection();
			updatePlot();

		} catch (InvocationTargetException | InterruptedException e) {
			final String errorMessage = MessageFormat.format("Could not load data for detector {0} from file {1}.", detectorModel.getName(), filePath);
			logger.error(errorMessage, e);
			snapshotPathText.setText(errorMessage);
			snapshotPathText.setToolTipText(errorMessage);
			snapshotPathText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			snapshotPathText.setVisible(true);
			throw e;
		}
	}

	private void updatePlot() {

		final Job updatePlotJob = new Job("Plotting...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Display.getDefault().syncExec(() -> {
					try {
						if (isImage) {
							MetadataPlotUtils.plotDataWithMetadata(dataset, plottingSystem);
							plottingSystem.getSelectedXAxis().setTitle("X");
							plottingSystem.getSelectedYAxis().setTitle("Y");
						} else {
							plottingSystem.clear();
							for (int index = 0; index < Math.min(dataset.getShape()[0],MAX_ELEMENTS); index++) {
								MetadataPlotUtils.plotDataWithMetadata(dataset.getSliceView(new Slice(index, index+1, 1)), plottingSystem, false);
							}
							plottingSystem.getSelectedXAxis().setTitle("Channel");
							plottingSystem.getSelectedYAxis().setTitle("Counts");
						}
						plottingSystem.setTitle("");
					} catch (Exception e) {
						logger.error("Could not plot data: " + e);
					}
				});
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};
		updatePlotJob.schedule();
	}

	// Dialog overrides
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(detectorModel.getName());
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create the ok button, but not the cancel button as we edit the model directly
		// (if we want a cancel button we should clone the model and set it on ok pressed)
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

}