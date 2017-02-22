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

import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_REQUEST_TOPIC;
import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_RESPONSE_TOPIC;
import static uk.ac.diamond.daq.mapping.ui.experiment.ProcessingSection.NEXUS_FILE_EXTENSION;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.dawnsci.slicing.api.util.ProgressMonitorWrapper;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.richbeans.widgets.file.FileSelectionDialog;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * A wizard page to acquire data for the selected detector for configuring processing.
 */
class AcquireDataWizardPage extends AbstractOperationSetupWizardPage {

	private static final Logger logger = LoggerFactory.getLogger(AcquireDataWizardPage.class);

	public static final String DEFAULT_ENTRY_PATH = "/entry/";
	public static final String DEFAULT_DATASET_NAME = "data"; // NXdetector.data field

	private IDetectorModel detectorModel = null;

	private IPlottingSystem<Composite> plottingSystem;

	private IDataset dataset = null;

	private static String lastFilePath = null;

	private final IEclipseContext context;

	private static IRequester<AcquireRequest> acquireRequestor = null;

	private Job update;

	private Composite detectorComposite;

	private StackLayout detectorAreaStackLayout;

	protected AcquireDataWizardPage(IEclipseContext context) {
		super(AcquireDataWizardPage.class.getName());
		setTitle("Acquire Data");
		setDescription("Acquire data from the detector to select the region to process.");

		this.context = context;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

		Composite detectorAndButtonsComposite = new Composite(sashForm, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(detectorAndButtonsComposite);

		Control detectorConfigComposite = createDetectorControl(detectorAndButtonsComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(detectorConfigComposite);

		Control buttonRow = createButtonRow(detectorAndButtonsComposite);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(buttonRow);

		createDataPlotControl(sashForm);

		sashForm.setWeights(new int[] { 2, 3 });
		setControl(sashForm);
		setPageComplete(false);
	}

	private Control createButtonRow(Composite parent) {
		Composite rowComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(rowComposite);

		Button acquireButton = new Button(rowComposite, SWT.PUSH);
		acquireButton.setText("Acquire");
		setButtonLayoutData(acquireButton);
		acquireButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				acquireData();
			}
		});

		Button loadFromFileButton = new Button(rowComposite, SWT.PUSH);
		loadFromFileButton.setText("Load from file...");
		setButtonLayoutData(loadFromFileButton);
		loadFromFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadDataFromFile();
			}
		});

		return rowComposite;
	}

	private void acquireData() {
		setPageComplete(false);
		AcquireRequest request = new AcquireRequest();
		request.setDetectorName(detectorModel.getName());
		request.setDetectorModel(detectorModel);

		try {
			AcquireRequest response = getRequestor().post(request);
			if (response.getStatus() == Status.COMPLETE) {
				loadDataFromFile(response.getFilePath());
			} else if (response.getStatus() == Status.FAILED){
				MessageDialog.openError(getShell(), "Acquire Data", "Unable to acquire data for detector. Reason: " + response.getMessage());
			} else {
				throw new IllegalArgumentException("Unknown status: " + response.getStatus());
			}
		} catch (Exception e) {
			handleException("Unable to acquire data for detector " + detectorModel.getName(), e);
		}
	}

	private IRequester<AcquireRequest> getRequestor() throws URISyntaxException, EventException {
		if (acquireRequestor == null) {
			final IEventService eventService = context.get(IEventService.class);
			final URI uri = new URI(LocalProperties.getActiveMQBrokerURI());
			acquireRequestor = eventService.createRequestor(uri, ACQUIRE_REQUEST_TOPIC, ACQUIRE_RESPONSE_TOPIC);
			acquireRequestor.setTimeout(15, TimeUnit.SECONDS);
		}

		return acquireRequestor;
	}

	private void loadDataFromFile() {
		FileSelectionDialog dialog = new FileSelectionDialog(getShell());
		dialog.setFolderSelector(false);
		dialog.setExtensions(new String[] { NEXUS_FILE_EXTENSION, "*.*" });
		dialog.setFiles(new String[] { "Nexus files", "All Files" });
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
		String datasetPath = DEFAULT_ENTRY_PATH + detectorModel.getName() + "/" + DEFAULT_DATASET_NAME;

		try {
			getWizard().getContainer().run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						// the file may not be visable yet as it is on lustre
						// TODO: it may be better to use the remote dataset service. If so, Jacob
						// would need add to the remote dataset service the ability to read the
						// axes, which is already implemented in the loader service
						File file = new File(filePath);
						int count = 0;
						while (!file.exists() && count < 10) {
							Thread.sleep(200);
							count++;
						}
						if (!file.exists()) {
							throw new FileNotFoundException("The file " + filePath + " could not be found.");
						}

						ILoaderService loaderService = context.get(ILoaderService.class);
						dataset = loaderService.getDataset(filePath, datasetPath, new ProgressMonitorWrapper(monitor)).squeeze();
						update();
						final Display display = getShell().getDisplay();
						// in the UI thread, execute setPageComplete to be called in 100ms
						// this is required as wizard page resets buttons when this runnable is finished
						display.asyncExec(() -> { display.timerExec(100, () -> { setPageComplete(true); }); });
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			String errorMessage = MessageFormat.format("Could not load data for detector {0} from file {1}.",
					detectorModel.getName(), filePath);
			handleException(errorMessage, e);
		}
	}

	private void handleException(String errorMessage, Throwable e) {
		if (e instanceof InvocationTargetException) e = e.getCause();
		ErrorDialog.openError(getShell(), "Acquire Data", errorMessage,
				new org.eclipse.core.runtime.Status(IStatus.ERROR,
						"uk.ac.diamond.daq.mapping.ui.experiment", errorMessage, e));
		logger.error(errorMessage, e);
	}

	@Override
	protected void update() {
		SliceInformation s = new SliceInformation(new SliceND(dataset.getShape()),
				new SliceND(dataset.getShape()), new SliceND(dataset.getShape()), dataset.getRank() == 1 ? new int[]{0} : new int[]{0,1}, 1, 1);

		SourceInformation source =  new SourceInformation("/", DEFAULT_ENTRY_PATH + detectorModel.getName() + "/" + DEFAULT_DATASET_NAME, dataset);

		SliceFromSeriesMetadata m = new SliceFromSeriesMetadata(source,s);
		dataset.setMetadata(m);

		if (update == null) {
			update = new Job("calculate...") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							try {
								MetadataPlotUtils.plotDataWithMetadata(dataset, plottingSystem);
							} catch (Exception e) {
								logger.warn("Could not plot data: " + e.getMessage());
							}
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
		Label detectorPlaceholder = new Label(detectorComposite, SWT.NONE);
		detectorPlaceholder.setText("No detector selected");
		detectorAreaStackLayout.topControl = detectorPlaceholder;

		if (detectorModel != null) {
			createDetectorUIControls(detectorModel);
		}

		return detectorComposite;
	}

	private Control createDataPlotControl(Composite parent) {
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();

			Composite plotAndToolbarComposite = new Composite(parent, SWT.NONE);
			GridLayoutFactory.fillDefaults().applyTo(plotAndToolbarComposite);

			// TODO: toolbar?
			plottingSystem.createPlotPart(plotAndToolbarComposite, "Acquire Data", null,
					PlotType.IMAGE, null);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingSystem.getPlotComposite());
			return plotAndToolbarComposite;
		} catch (Exception e) {
			logger.error("Could not create plotting system", e);

			Label label = new Label(parent, SWT.NONE);
			label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
			label.setText("Could not create plotting system: " + e.getMessage());
			return label;
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

	public void setDetectorModel(IDetectorModel detectorModel) {
		if (detectorModel != this.detectorModel) {
			this.detectorModel = detectorModel;

			// if we've already created the UI we create a new composite for this detector
			if (detectorComposite != null) {
				createDetectorUIControls(detectorModel);
			}
		}
	}

	private void createDetectorUIControls(IDetectorModel detectorModel) {
		IGuiGeneratorService guiGenerator = context.get(IGuiGeneratorService.class);
		Control detectorControl = guiGenerator.generateGui(detectorModel, detectorComposite);
		detectorAreaStackLayout.topControl = detectorControl;
		detectorComposite.layout();
	}

}
