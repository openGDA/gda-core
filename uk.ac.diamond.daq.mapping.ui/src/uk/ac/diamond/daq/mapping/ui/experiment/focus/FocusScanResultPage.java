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

package uk.ac.diamond.daq.mapping.ui.experiment.focus;

import static uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanUtils.displayError;
import static uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanUtils.displayYesNoMessage;
import static uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanUtils.saveConfig;
import static uk.ac.gda.client.UIHelper.createNumberAndUnitsLengthComposite;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.dawnsci.mapping.ui.MappingUtils;
import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.IMapFileEventListener;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.function.ILinearFunction;
import gda.util.QuantityFactory;
import uk.ac.diamond.daq.mapping.api.EnergyFocusBean;
import uk.ac.diamond.daq.mapping.api.FocusScanBean;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;
import uk.ac.diamond.daq.mapping.ui.experiment.MappingExperimentUtils;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanBeanSubmitter;
import uk.ac.gda.client.NumberAndUnitsComposite;

/**
 * The second page of the {@link FocusScanWizard}, allowing the user to run
 * the focus scan as configured in the first page ({@link FocusScanSetupPage}),
 * visualize the results and select a focus (zone plate) setting.
 */
public class FocusScanResultPage extends WizardPage {

	private static final Logger logger = LoggerFactory.getLogger(FocusScanResultPage.class);

	@Inject
	private FocusScanConverter converter;

	@Inject
	private IEventService eventService;

	@Inject
	private IScannableDeviceService scannableService;

	@Inject
	private IMapFileController mapFileController;

	private IMapFileEventListener mapFileEventListener = null;

	@Inject
	private ScanBeanSubmitter submitter;

	@Inject
	private FocusScanBean focusScanBean;

	@Inject
	private IMappingExperimentBeanProvider mappingBeanProvider;

	@Inject
	private UISynchronize uiSync;

	private Button startScanButton;

	private Button stopScanButton;

	private Label messageLabel;

	private ProgressBar scanProgressBar;

	private StatusBean statusBean = null;

	private NumberAndUnitsComposite<Length> focusScannablePosition;

	private ISubscriber<IBeanListener<StatusBean>> statusQueueSubscriber;

	private IBeanListener<StatusBean> statusBeanListener;

	private Label statusLabel;

	private Label percentCompleteLabel;

	private IPlottingSystem<Composite> plottingSystem;

	private ScheduledFuture<?> future;
	private ScheduledExecutorService updateMapExecutor;

	private IScannable<Double> focusScannable;
	private Object focusScannableOriginalPosition;

	public FocusScanResultPage() {
		super(FocusScanResultPage.class.getSimpleName());
		setTitle("Select Focus Setting");
		setDescription("Run the focus scan and select the focus setting from the resulting map");
	}

	@Override
	public void createControl(Composite parent) {
		try {
			focusScannable = scannableService.getScannable(focusScanBean.getFocusScannableName());
			focusScannableOriginalPosition = focusScannable.getPosition();
		} catch (ScanningException e) {
			final String message = "Could not access focus scannable";
			logger.error(message, e);
			displayError(message, "See error log for details", logger);
			return;
		}

		final SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

		createFocusScanPlotControl(sashForm);

		final Composite composite = new Composite(sashForm, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(composite);

		createRunScanControls(composite);
		createSelectFocusPositionControls(composite);

		sashForm.setWeights(new int[] { 3, 2 });
		setControl(sashForm);
		setPageComplete(false);
	}

	private Control createFocusScanPlotControl(Composite composite) {
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			final Control plotControl = MappingExperimentUtils.createDataPlotControl(
					composite, plottingSystem, getTitle());
			plottingSystem.addClickListener(new IClickListener() {
				@Override
				public void doubleClickPerformed(ClickEvent evt) {
					// do nothing on double-click
				}
				@Override
				public void clickPerformed(ClickEvent evt) {
					focusScannablePosition.setValue(evt.getyValue());
					updatePageComplete();
				}
			});
			return plotControl;
		} catch (Exception e) {
			final String message = "Could not create plotting system";
			logger.error(message, e);
			return MappingExperimentUtils.createErrorLabel(composite, message, e);
		}
	}

	private void updatePageComplete() {
		setPageComplete(statusBean != null && statusBean.getStatus().isFinal());
	}

	private void createRunScanControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);
		GridDataFactory.fillDefaults().applyTo(composite);

		new Label(composite, SWT.NONE).setText("Message:");

		messageLabel = new Label(composite, SWT.NONE);
		messageLabel.setText("Press 'Start Scan' to start a focus scan");
		GridDataFactory fillData = GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER);
		fillData.applyTo(messageLabel);

		new Label(composite, SWT.NONE).setText("Status:");

		statusLabel = new Label(composite, SWT.NONE);
		fillData.applyTo(statusLabel);

		new Label(composite, SWT.NONE).setText("Complete:");

		percentCompleteLabel = new Label(composite, SWT.NONE);
		fillData.applyTo(percentCompleteLabel);

		scanProgressBar = new ProgressBar(parent, SWT.SMOOTH);
		scanProgressBar.setMaximum(10000);
		fillData.applyTo(scanProgressBar);

		Composite scanControlButtonsLine = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(scanControlButtonsLine);
		GridDataFactory.fillDefaults().applyTo(scanControlButtonsLine);

		startScanButton = new Button(scanControlButtonsLine, SWT.PUSH);
		startScanButton.setText("Start Scan");
		startScanButton.addListener(SWT.Selection, event -> startFocusScan());
		GridDataFactory.swtDefaults().align(SWT.TRAIL, SWT.CENTER).grab(true, false).applyTo(startScanButton);

		stopScanButton = new Button(scanControlButtonsLine, SWT.PUSH);
		stopScanButton.setText("Stop Scan");
		stopScanButton.addListener(SWT.Selection, event -> stopFocusScan());
		stopScanButton.setEnabled(false);
		GridDataFactory.swtDefaults().applyTo(stopScanButton);

		addScanTopicListener();
	}

	private void startFocusScan() {
		// set the page as incomplete, disables Finish button while scan is running
		setPageComplete(false);

		// add a listener for map file events
		if (mapFileEventListener == null) {
			mapFileEventListener = this::handleMapEvent;
			mapFileController.addListener(mapFileEventListener);
		}

		// create the ScanBean from the focus scan bean
		final ScanBean scanBean = new ScanBean();
		scanBean.setName(String.format("%s - Focus Scan", getSampleName()));
		scanBean.setBeamline(System.getProperty("BEAMLINE"));

		ScanRequest<?> scanRequest = converter.convertToScanRequest(focusScanBean);
		scanBean.setScanRequest(scanRequest);

		// submit the ScanBean
		try {
			statusBean = scanBean;
			submitter.submitScan(scanBean);
			messageLabel.setText("Waiting for scan to start");
			startScanButton.setEnabled(false);
			stopScanButton.setEnabled(true);
		} catch (EventException e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
		}
	}

	/**
	 * Extract an {@link AbstractMapData} from a {@link MappedDataFile}.
	 * @param mappedDataFile
	 * @return map data
	 */
	private Optional<AbstractMapData> extractMap(MappedDataFile mappedDataFile) {
		final Object[] children = mappedDataFile.getChildren();
		if (children.length == 1 && children[0] instanceof MappedDataBlock) {
			final MappedDataBlock mapBlock = (MappedDataBlock) children[0];
			int count = 0;
			while (!mapBlock.isReady() && count < 10) {
				// Poll until the map file is ready
				try {
					Thread.sleep(500);
					logger.debug("In read loop");
					count++;
				} catch (InterruptedException e) {
					logger.error("Interrupted waiting for map", e); // we were interrupted (unlikely)
					Thread.currentThread().interrupt();
					return Optional.empty();
				}
			}

			if (!mapBlock.isReady()) { // Timed out waiting for the map block
				logger.warn("Map block timed out after 5s");
				return Optional.empty();
			}

			if (!mapBlock.canPlot()) { // can't plot the map block
				logger.warn("Map block not plottable");
				return Optional.empty();
			}

			return Optional.of(mapBlock.getMapObject());
		} else {
			return Arrays.stream(children)
					.filter(AbstractMapData.class::isInstance)
					.map(AbstractMapData.class::cast)
					.filter(AbstractMapData::isLive)
					.findFirst();
		}
	}

	private void handleMapEvent(MappedDataFile mappedDataFile) {
		if (mappedDataFile == null) return;

		if (future != null) return; // update job already running

		//might have been loaded lazily, we need the data so force non-lazy load if empty file
		if (mappedDataFile.getChildren().length == 0) {
			mapFileController.loadLiveFile(mappedDataFile.getPath(), mappedDataFile.getLiveDataBean(), null, false);
			return;
		}

		// clear any previously plotted data
		plottingSystem.clear();

		// extract the map from the file and check that it is present
		final Optional<AbstractMapData> optMapData = extractMap(mappedDataFile);
		if (!optMapData.isPresent()) {
			logger.warn("Could not find map data for scan");
			return;
		}

		// create a new single-thread executor to update the map at a fixed rate
		updateMapExecutor = Executors.newScheduledThreadPool(1);
		final Runnable plotTrace = () -> updateMap(optMapData.get());
		future = updateMapExecutor.scheduleAtFixedRate(plotTrace, 0l, 2, TimeUnit.SECONDS);

	}

	private void updateMap(final AbstractMapData mapData) {
		try {
			logger.debug("Updating focus scan map");
			if (!mapData.isReady()) return; // don't do anything if the map isn't ready

			try {
				mapData.update();
			} catch (Exception e) {
				logger.error("Could not update map data", e);
			}

			final IDataset mapDataset = mapData.getMapForDims(0, 1);
			if (mapDataset == null) return;
			final IImageTrace existingTrace = (IImageTrace) plottingSystem.getTrace(mapDataset.getName());
			plottingSystem.setKeepAspect(false);

			final AxesMetadata axesMetadata = mapDataset.getFirstMetadata(AxesMetadata.class);
			ILazyDataset[] axisDataset = axesMetadata.getAxis(1);

			DoubleDataset lineLength = DatasetFactory.createRange(axisDataset[1].getSize());
			lineLength.setName("Length along line in points");

			//try and calculate hypotenuse for x axis

			try {
				IDataset s0 = axisDataset[0].getSlice();
				IDataset s1 = axisDataset[1].getSlice();

				if (!Arrays.equals(s0.getShape(), s1.getShape())) {
					axesMetadata.setAxis(1, lineLength);
				} else {
					Dataset hypot = Maths.hypot(s0, s1);
					hypot.setName("Distance along line");
					axesMetadata.setAxis(1, hypot);
				}

			} catch (Exception e) {
				logger.error("Couldn't calculate length");
				axesMetadata.setAxis(1, lineLength);
			}

			// update the trace with the range
			final double[] range = getRange(mapDataset);
			if (existingTrace != null) {
				// update the existing trace with the updated dataset
				uiSync.asyncExec(() -> {
					existingTrace.setGlobalRange(range);
					MetadataPlotUtils.switchData(mapDataset.getName(), mapDataset, existingTrace);
					plottingSystem.repaint();
				});
			} else {
				// create a new trace and add it to the plotting system
				IImageTrace newTrace = MetadataPlotUtils.buildTrace(mapDataset, plottingSystem);
				newTrace.setGlobalRange(range);
				uiSync.asyncExec(() -> plottingSystem.addTrace(newTrace));
			}
		} catch (Exception e) {
			logger.error("Could not update focus scan map", e);
		}
	}


	private double[] getRange(IDataset m) {
		IDataset[] ax = MetadataPlotUtils.getAxesAsIDatasetArray(m);
		return MappingUtils.calculateRangeFromAxes(new IDataset[]{ax[0],ax[1]});
	}

	private String getSampleName() {
		final String sampleName = mappingBeanProvider.getMappingExperimentBean().getSampleMetadata().getSampleName();
		return sampleName == null ? "unknown sample" : sampleName;
	}

	private void stopFocusScan() {
		if (statusBean != null && statusBean.getStatus().isActive()) {
			logger.debug("attempting to stopFocusScan, current scan state: {}", statusBean.getStatus());
			// set the status of the bean to REQUEST_TERMINATE
			statusBean.setStatus(Status.REQUEST_TERMINATE);
			statusBean.setMessage("Termination of " + statusBean.getName());

			// then publish the bean to the status topic
			try (final IPublisher<StatusBean> publisher = eventService.createPublisher(
						getActiveMqUri(), EventConstants.STATUS_TOPIC)) {
				publisher.broadcast(statusBean);
			} catch (URISyntaxException | EventException e) {
				logger.error("Could not send terminate request", e);
				MessageDialog.openError(getShell(), "Error", "Could not send terminate request. See error log for details");
			}
		} else {
			// There is no focus scan or it isn't running. Log and display an error message
			String errorMessage = "Cannot stop focus scan. " +
					(statusBean == null ? "No focus scan is currently running" :
						"The focus scan has status " + statusBean.getStatus());
			logger.error(errorMessage);
			MessageDialog.openError(getShell(), "Error", errorMessage);
		}
	}

	private void createSelectFocusPositionControls(Composite parent) {
		GridDataFactory.fillDefaults().applyTo(new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL));

		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		GridDataFactory.swtDefaults().applyTo(composite);

		final Label label = new Label(composite, SWT.NONE);
		label.setText("Focus position:");
		GridDataFactory.swtDefaults().applyTo(label);

		focusScannablePosition = createNumberAndUnitsLengthComposite(composite);
		GridDataFactory.swtDefaults().applyTo(focusScannablePosition);

		new Label(parent, SWT.NONE).setText("Select 'Finish' to move the focus to the selected position.");
	}

	/**
	 * Adds a listener to activemq that is notified when scan been are changed.
	 * We use this to update the UI to show the current status of the scan.
	 */
	private void addScanTopicListener() {
		try {
			statusQueueSubscriber = eventService.createSubscriber(getActiveMqUri(),
				EventConstants.STATUS_TOPIC);
			statusBeanListener = event -> {
				// first check this is the correct bean for our scan by comparing the ids
				if (event.getBean().getUniqueId().equals(statusBean.getUniqueId())) {
					statusBean = event.getBean(); // update the
					uiSync.asyncExec(this::handleStatusBeanUpdate); // update the UI

				}
			};
			statusQueueSubscriber.addListener(statusBeanListener);
		} catch (EventException | URISyntaxException e) {
			logger.error("Could not add listener to status queue", e);
		}
	}

	private void handleStatusBeanUpdate() {
		// update the message label and progress bar
		if (statusBean.getMessage() != null) {
			messageLabel.setText(statusBean.getMessage());
		}
		scanProgressBar.setSelection((int) (statusBean.getPercentComplete() * 100));
		statusLabel.setText(statusBean.getStatus().toString());

		percentCompleteLabel.setText(Integer.toString((int) statusBean.getPercentComplete()) + "%");

		// if the scan is finished (for any reason), update the start/stop scan button enablement
		if (statusBean.getStatus().isFinal()) {
			updatePageComplete();
			startScanButton.setEnabled(true);
			stopScanButton.setEnabled(false);

			// stop the map update thread
			if (future != null) {
				future.cancel(false);
				future = null;
				updateMapExecutor.shutdown();
				updateMapExecutor = null;
			}
		}
	}

	private URI getActiveMqUri() throws URISyntaxException {
		final String uriString = LocalProperties.get(LocalProperties.GDA_ACTIVEMQ_BROKER_URI, "");
		return new URI(uriString);
	}

	/**
	 * Handle a request to close the wizard by checking if a scan is running. If so,
	 * ask the user if they wish to cancel it
	 * @return <code>true</code> if the wizard can be closed, <code>false</code> otherwise
	 */
	protected boolean closeWizard() {
		if (statusBean != null && statusBean.getStatus().isActive()) {
			// display a confirm dialog to check the user wants to close the wizard
			final boolean confirm = MessageDialog.openConfirm(getShell(), "Focus Scan Wizard",
					"A focus scan is currently running. Are you sure you want to close this wizard? This will terminate the focus scan.");
			if (!confirm) {
				return false;
			}

			// stop the focus scan
			stopFocusScan();
		}

		// remove the listener from the status queue
		statusQueueSubscriber.removeListener(statusBeanListener);
		mapFileController.removeListener(mapFileEventListener);

		try {
			statusQueueSubscriber.disconnect();
		} catch (EventException e) {
			logger.error("Could not disconnect from status topic subscriber", e);
		}

		return true;
	}

	/**
	 * If energy focus function modification is configured, ask the user to confirm that they wish to change the
	 * interception.
	 */
	protected void updateInterception() {
		final EnergyFocusBean energyFocusBean = focusScanBean.getEnergyFocusBean();
		if (energyFocusBean != null) {
			final Unit<? extends Quantity> focusScannableUnit = QuantityFactory.createUnitFromString(focusScannable.getUnit());
			final Quantity oldFocusPosition = QuantityFactory.createFromObject(focusScannableOriginalPosition, focusScannableUnit);
			final Quantity newFocusPosition = QuantityFactory.createFromObject(focusScannablePosition.getValue(), focusScannableUnit);
			final Quantity difference = newFocusPosition.minus(oldFocusPosition);

			final ILinearFunction energyFocusFunction = energyFocusBean.getEnergyFocusFunction();
			final Quantity oldInterception = QuantityFactory.createFromString(energyFocusFunction.getInterception());
			final Quantity newInterception = oldInterception.plus(difference);

			final String message = String.format("Do you want to change the interception from %s to %s?",
					oldInterception, newInterception);
			if (displayYesNoMessage("Change interception", message)) {
				energyFocusFunction.setInterception(newInterception.toString());
				saveConfig(energyFocusFunction, energyFocusBean.getEnergyFocusConfigPath(), logger);
			}
		}
	}

	protected void setFocusPosition() {
		final Double newPosition = (Double) focusScannablePosition.getValue();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, false, monitor -> doSetFocusPosition(newPosition, monitor));
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error("Could not set focus scannable {}", focusScanBean.getFocusScannableName(), e);
			MessageDialog.openError(getShell(), "Error",
					MessageFormat.format("Could not set position of zone plate {0}. See error log for details.",
					focusScanBean.getFocusScannableName()));
		}
	}

	private void doSetFocusPosition(final Double newPosition, IProgressMonitor monitor) throws InvocationTargetException {
		// move the focus scannable (zone plate) to the given position
		try {
			final String zonePlateName = focusScanBean.getFocusScannableName();
			monitor.beginTask(String.format("Moving zone plate ''%s'' to %f", zonePlateName, newPosition),
					IProgressMonitor.UNKNOWN);

			focusScannable.setPosition(newPosition);
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
	}
}