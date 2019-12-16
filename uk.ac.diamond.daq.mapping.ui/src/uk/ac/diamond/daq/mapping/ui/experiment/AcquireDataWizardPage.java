/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.dawnsci.processing.ui.model.AbstractOperationSetupWizardPage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.ProgressMonitorWrapper;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.richbeans.widgets.file.FileSelectionDialog;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.device.ui.util.ScanningUiUtils;
import org.eclipse.scanning.event.util.SubmissionQueueUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.ui.MappingUIConstants;

/**
 * A wizard page to acquire data for the selected detector for configuring processing.
 */
class AcquireDataWizardPage extends AbstractOperationSetupWizardPage {

	private static final Logger logger = LoggerFactory.getLogger(AcquireDataWizardPage.class);

	private static final String PAGE_TITLE = "Acquire Data";

	/**
	 * In the case of a software acquire this will be the model of the software detector
	 * whose data we want to acquire;
	 * in the case of a hardware acquire this will be the model of the malcolm device.
	 */
	private IDetectorModel acquireDetectorModel;

	/**
	 * The name of the detector whose data we want to acquire. This is the name of the
	 * {@code NXdata} group in the nexus file we want to process. In the case of a hardware
	 * (i.e. malcolm) acquire this be the name of a malcolm contolled detector.
	 */
	private String detectorDataGroupName;

	private IPlottingSystem<Composite> plottingSystem;

	private static String lastFilePath = null;

	private final IEclipseContext context;

	private Job update;

	private Composite detectorComposite;

	private StackLayout detectorAreaStackLayout;

	protected AcquireDataWizardPage(IEclipseContext context) {
		super(AcquireDataWizardPage.class.getName());
		setTitle(PAGE_TITLE);
		setDescription("Acquire data from the detector to select the region to process.");

		this.context = context;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		final SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

		final Composite detectorAndButtonsComposite = new Composite(sashForm, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(detectorAndButtonsComposite);

		final Control detectorConfigComposite = createDetectorControl(detectorAndButtonsComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(detectorConfigComposite);

		final Control buttonRow = createButtonRow(detectorAndButtonsComposite);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(buttonRow);

		createDataPlotControl(sashForm);

		sashForm.setWeights(new int[] { 2, 3 });
		setControl(sashForm);
		setPageComplete(false);
	}

	private Control createButtonRow(Composite parent) {
		final Composite rowComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(rowComposite);

		final Button acquireButton = new Button(rowComposite, SWT.PUSH);
		acquireButton.setText("Acquire");
		setButtonLayoutData(acquireButton);
		acquireButton.addSelectionListener(widgetSelectedAdapter(e -> acquireData()));

		final Button loadFromFileButton = new Button(rowComposite, SWT.PUSH);
		loadFromFileButton.setText("Load from file...");
		setButtonLayoutData(loadFromFileButton);
		loadFromFileButton.addSelectionListener(widgetSelectedAdapter(e -> loadDataFromFile()));

		return rowComposite;
	}

	private void acquireData() {
		setPageComplete(false);

		// to prevent AcquireRequest interrupting a running scan,
		// we'll check that no running/submitted scans are currently in the queue
		if (SubmissionQueueUtils.isJobRunningOrPending()) {
			final String msg = "Cannot take snapshot while there are submitted or running scans.";
			logger.warn("{}\nAcquireRequest aborted",msg);
			MessageDialog.openInformation(getShell(), "Snapshot", msg);
			return;
		}

		try {
			final AcquireRequest response = ScanningUiUtils.acquireData(acquireDetectorModel);
			if (response.getStatus() == Status.COMPLETE) {
				loadDataFromFile(response.getFilePath());
			} else if (response.getStatus() == Status.FAILED) {
				MessageDialog.openError(getShell(), PAGE_TITLE, "Unable to acquire data for detector. Reason: " + response.getMessage());
			} else {
				throw new IllegalArgumentException("Unknown status: " + response.getStatus());
			}
		} catch (Exception e) {
			handleException("Unable to acquire data for detector " + acquireDetectorModel.getName(), e);
		}
	}

	private void loadDataFromFile() {
		final FileSelectionDialog dialog = new FileSelectionDialog(getShell());
		dialog.setFolderSelector(false);
		dialog.setExtensions(MappingUIConstants.NEXUS_FILE_EXTENSION, "*.*");
		dialog.setFiles("Nexus files", "All Files");
		dialog.setHasResourceButton(false);
		dialog.setNewFile(false);
		if (lastFilePath != null) {
			dialog.setPath(lastFilePath);
		}

		dialog.create();
		if (dialog.open() != Window.OK) {
			return;
		}

		lastFilePath = dialog.getPath();
		loadDataFromFile(lastFilePath);
	}

	private void loadDataFromFile(String filePath) {
		final String datasetPath = ScanningUiUtils.getDatasetPath(detectorDataGroupName);

		try {
			getWizard().getContainer().run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						// the file may not be visible yet as it is on lustre
						// TODO: it may be better to use the remote dataset service. If so, Jacob
						// would need add to the remote dataset service the ability to read the
						// axes, which is already implemented in the loader service
						final File file = new File(filePath);
						int count = 0;
						while (!file.exists() && count < 10) {
							Thread.sleep(200);
							count++;
						}
						if (!file.exists()) {
							throw new FileNotFoundException("The file " + filePath + " could not be found.");
						}

						final ILoaderService loaderService = context.get(ILoaderService.class);
						final IDataset dataset = loaderService.getDataset(filePath, datasetPath, new ProgressMonitorWrapper(monitor)).squeeze();
						update(dataset);
						final Display display = getShell().getDisplay();
						// in the UI thread, execute setPageComplete to be called in 100ms
						// this is required as wizard page resets buttons when this runnable is finished
						display.asyncExec(() -> display.timerExec(100, () -> setPageComplete(true)));
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			final String errorMessage = MessageFormat.format("Could not load data for detector {0} from file {1}.",
					acquireDetectorModel.getName(), filePath);
			handleException(errorMessage, e);
		}
	}

	private void handleException(String errorMessage, Throwable e) {
		if (e instanceof InvocationTargetException) e = e.getCause();
		ErrorDialog.openError(getShell(), PAGE_TITLE, errorMessage,
				new org.eclipse.core.runtime.Status(IStatus.ERROR,
						"uk.ac.diamond.daq.mapping.ui.experiment", errorMessage, e));
		logger.error(errorMessage, e);
	}

	@Override
	protected void update() {
		// Not implemented: use update(IDataset dataset)
		throw new UnsupportedOperationException("Use update(IDataset dataset)");
	}

	private void update(IDataset dataset) {
		final SliceInformation s = ScanningUiUtils.getDatasetSlice(dataset);
		final SourceInformation source = ScanningUiUtils.getSourceInformation(detectorDataGroupName, dataset);

		final SliceFromSeriesMetadata m = new SliceFromSeriesMetadata(source,s);
		dataset.setMetadata(m);

		if (update == null) {
			update = new Job("calculate...") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					Display.getDefault().syncExec(() -> {
						try {
							MetadataPlotUtils.plotDataWithMetadata(dataset, plottingSystem);
						} catch (Exception e) {
							logger.warn("Could not plot data", e);
						}
					});
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}
			};
		}

		update.cancel();
		update.schedule();
		od = new OperationData(dataset);
	}

	private Control createDetectorControl(Composite parent) {
		detectorComposite = new Composite(parent, SWT.NONE);
		detectorAreaStackLayout = new StackLayout();
		detectorComposite.setLayout(detectorAreaStackLayout);

		// The detector model may not have been set yet, so just create a placeholder
		final Label detectorPlaceholder = new Label(detectorComposite, SWT.NONE);
		detectorPlaceholder.setText("No detector selected");
		detectorAreaStackLayout.topControl = detectorPlaceholder;

		if (acquireDetectorModel != null) {
			createDetectorUIControls(acquireDetectorModel);
		}

		return detectorComposite;
	}

	private Control createDataPlotControl(Composite parent) {
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			return ScanningUiUtils.createDataPlotControl(parent, plottingSystem, PAGE_TITLE);
		} catch (Exception e) {
			final String message = "Could not create plotting system";
			logger.error(message, e);
			return ScanningUiUtils.createErrorLabel(parent, message, e);
		}
	}

	@Override
	public void wizardTerminatingButtonPressed(int buttonId) {
		// nothing to do
	}

	@Override
	public void setInputData(OperationData od) {
		// since this wizard page is before the operations pages and will have no initial data, this method does nothing
	}

	public void setAcquireDetectorModel(IDetectorModel detectorModel) {
		if (detectorModel != this.acquireDetectorModel) {
			this.acquireDetectorModel = detectorModel;

			// if we've already created the UI we create a new composite for this detector
			if (detectorComposite != null) {
				createDetectorUIControls(detectorModel);
			}
		}
	}

	public void setDetectorDataGroupName(String detectorName) {
		this.detectorDataGroupName = detectorName;
	}

	private void createDetectorUIControls(IDetectorModel detectorModel) {
		final IGuiGeneratorService guiGenerator = context.get(IGuiGeneratorService.class);
		final Control detectorControl = guiGenerator.generateGui(detectorModel, detectorComposite);
		detectorAreaStackLayout.topControl = detectorControl;
		detectorComposite.layout();
	}

}
