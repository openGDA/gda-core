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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.dawnsci.slicing.api.util.ProgressMonitorWrapper;
import org.eclipse.e4.core.contexts.IEclipseContext;
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
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;

/**
 * Dialog providing a simple workflow in configuring detector:
 * <ul><li> set detector exposure (or other parameters)
 * <li> take snapshot to evaluate choice of parameters </ul>
 * Can plot as image or spectrum, and provides statistics particular to those modes
 */
public class EditDetectorParametersDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(EditDetectorParametersDialog.class);

	private static final int LAYOUT_MARGIN = 5;
	private static final int PLOT_SIZE = 600;
	private static final int PLOT_VIEW_COLUMNS = 2;

	/**
	 * If first dimension of dataset is greater than this, we are probably dealing with an image,
	 * rather than a series of spectra
	 */
	private static final int MAX_ELEMENTS = 20;

	private final IEclipseContext context;
	private final IDetectorModelWrapper detectorModel;
	private IPlottingSystem<Composite> plottingSystem;

	private IDataset dataset;
	private boolean isImage = true;

	private Text txtSnapshotPath;
	private Composite statisticsComposite;
	private Group statistics;
	private Button plotAsLine;
	private Button plotAsImage;

	protected EditDetectorParametersDialog(final Shell parentShell, final IEclipseContext context, final IDetectorModelWrapper detectorModel) {
		super(parentShell);
		this.context = context;
		this.detectorModel = detectorModel;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite dialogComposite = (Composite) super.createDialogArea(parent);

		// Overall layout is a 2-column grid
		GridDataFactory.fillDefaults().grab(true, true).applyTo(dialogComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).margins(LAYOUT_MARGIN, LAYOUT_MARGIN).applyTo(dialogComposite);

		// Snapshot
		final Composite snapshotComposite = new Composite(dialogComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().minSize(PLOT_SIZE + 100, PLOT_SIZE).grab(true, true).applyTo(snapshotComposite);
		GridLayoutFactory.fillDefaults().numColumns(PLOT_VIEW_COLUMNS).margins(LAYOUT_MARGIN, LAYOUT_MARGIN).applyTo(snapshotComposite);

		// Plot: spans all columns
		final Control plot = createPlot(snapshotComposite);
		plottingSystem.getSelectedXAxis().setTitle("");
		GridDataFactory.fillDefaults().span(PLOT_VIEW_COLUMNS, 1).minSize(PLOT_SIZE, PLOT_SIZE).grab(true, true).applyTo(plot);

		// Snapshot button & message
		final Button snapshotButton = new Button(snapshotComposite, SWT.PUSH);
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.FILL).applyTo(snapshotButton);
		snapshotButton.setImage(MappingExperimentUtils.getImage("icons/camera.png"));
		snapshotButton.setToolTipText("Take snapshot");
		snapshotButton.addListener(SWT.Selection, event -> takeSnapshot(detectorModel));

		txtSnapshotPath = new Text(snapshotComposite, SWT.NULL);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtSnapshotPath);
		txtSnapshotPath.setEditable(false);
		txtSnapshotPath.setBackground(snapshotComposite.getBackground());
		txtSnapshotPath.setVisible(false);

		// Detector parameters
		final Composite parametersComposite = new Composite(dialogComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().applyTo(parametersComposite);
		GridLayoutFactory.fillDefaults().margins(LAYOUT_MARGIN, LAYOUT_MARGIN).applyTo(parametersComposite);

		// Detector name
		createStyledTextLabel(parametersComposite, detectorModel.getName());

		// Parameters
		final Group parameters = createGroup(parametersComposite, "Parameters", 1);

		// The parameters themselves: generated from model
		final IGuiGeneratorService guiGenerator = context.get(IGuiGeneratorService.class);
		final Control editor = guiGenerator.generateGui(detectorModel.getModel(), parameters);
		GridDataFactory.fillDefaults().applyTo(editor);

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

		statisticsComposite = new Composite(parametersComposite, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(statisticsComposite);
		GridLayoutFactory.fillDefaults().applyTo(statisticsComposite);

		createStatsSection();

		return dialogComposite;
	}

	private void createStatsSection() {

		if (statistics != null) {
			statistics.dispose();
			statistics = null;
		}

		statistics = createGroup(statisticsComposite, "Statistics", 2);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(statistics);

		SnapshotStatsViewer statsViewer = isImage ? new ImageSnapshotStatsViewer(statistics) : new LineSnapshotStatsViewer(statistics);
		if (dataset!=null) statsViewer.update(dataset, detectorModel.getModel().getExposureTime());

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
			return MappingExperimentUtils.createDataPlotControl(parent, plottingSystem, "Snapshot");
		} catch (Exception e) {
			final String message = "Could not create plotting system";
			logger.error(message, e);
			return MappingExperimentUtils.createErrorLabel(parent, message, e);
		}
	}

	private void takeSnapshot(final IDetectorModelWrapper detectorParameters) {
		final String messageTitle = "Snapshot";
		try {
			final IRequester<AcquireRequest> acquireRequestor = MappingExperimentUtils.getAcquireRequestor(context);
			acquireRequestor.setTimeout(5, TimeUnit.SECONDS);
			final AcquireRequest response = MappingExperimentUtils.acquireData(detectorParameters.getModel(), acquireRequestor);

			if (response.getStatus() == Status.COMPLETE) {
				final String snapshotPath = response.getFilePath();
				loadSnapshot(snapshotPath);
				txtSnapshotPath.setText(snapshotPath);
				txtSnapshotPath.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
				txtSnapshotPath.setVisible(true);
			} else if (response.getStatus() == Status.FAILED) {
				final String message = MessageFormat.format("Unable to acquire data for detector {0}: {1}",
						detectorParameters.getName(), response.getMessage());
				MessageDialog.openError(getShell(), messageTitle, message);
				logger.error(message);
			}
		} catch (Exception e) {
			MessageDialog.openError(getShell(), messageTitle, "Error taking snapshot");
			logger.error("Error taking snapshot", e);
		}
	}

	private void loadSnapshot(final String filePath) throws Exception {
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

						final ILoaderService loaderService = context.get(ILoaderService.class);
						final String datasetPath = MappingExperimentUtils.getDatasetPath(detectorModel.getModel());
						dataset = loaderService.getDataset(filePath, datasetPath, new ProgressMonitorWrapper(monitor));

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
			txtSnapshotPath.setText(errorMessage);
			txtSnapshotPath.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			txtSnapshotPath.setVisible(true);
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
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		if (id == IDialogConstants.CANCEL_ID) {
			return null;
		}
		return super.createButton(parent, id, label, defaultButton);
	}
}