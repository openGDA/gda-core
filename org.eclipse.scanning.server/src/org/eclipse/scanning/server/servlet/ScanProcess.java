/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.server.servlet;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.AnnotationManager;
import org.eclipse.scanning.api.annotation.scan.PostConfigure;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.annotation.scan.PrepareScan;
import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IBeanProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.scanning.api.script.ScriptExecutionException;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.api.script.UnsupportedLanguageException;
import org.eclipse.scanning.server.application.Activator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.api.messaging.MessagingService;
import uk.ac.diamond.daq.api.messaging.messages.ScanMessage;
import uk.ac.diamond.daq.api.messaging.messages.SwmrStatus;

/**
 * Object for running a scan.
 *
 * TODO: DAQ-1960 - make ScanProcess solely responsible for updating the ScanBean at present this is split between
 * ScanProcess and AcquisitionDevice
 *
 * @author Matthew Gerring
 *
 */
public class ScanProcess implements IBeanProcess<ScanBean> {

	private static final Logger logger = LoggerFactory.getLogger(ScanProcess.class);
	private final ScanBean bean;
	private final IPublisher<ScanBean> publisher;

	// Services
	private IPositioner positioner;
	private IScriptService scriptService;

	private IDeviceController controller;
	private boolean blocking;

	private volatile boolean scriptRunning = false;
	private volatile boolean terminated = false;
	private volatile boolean settingPosition = false;

	public ScanProcess(ScanBean scanBean, IPublisher<ScanBean> response, boolean blocking) throws EventException {
		this.bean = scanBean;
		this.publisher = response;
		this.blocking = blocking;

		if (bean.getScanRequest().getStartPosition() != null || bean.getScanRequest().getEndPosition() != null) {
			try {
				this.positioner = Services.getRunnableDeviceService().createPositioner(this.getClass().getSimpleName());
			} catch (ScanningException e) {
				throw new EventException(e);
			}
		}

		this.scriptService = Services.getScriptService();

		updateBean(Status.PREPARING, null);
	}

	private void updateBean(Status newStatus, String message) throws EventException {
		if (newStatus == null) {
			bean.setPreviousStatus(bean.getStatus()); // not a status change
		} else if (newStatus != bean.getStatus()) {
			bean.setPreviousStatus(bean.getStatus());
			bean.setStatus(newStatus);
		}

		if (message != null) {
			bean.setMessage(message);
		}

		broadcast(bean);
	}

	@Override
	public void pause() throws EventException {
		logger.trace("pause() {} with controller {}", bean, controller);
		try {
			controller.pause(getClass().getName(), null);
		} catch (ScanningException | InterruptedException e) {
			throw new EventException(e);
		}
	}

	@Override
	public void resume() throws EventException {
		logger.trace("resume() {} with controller {}", bean, controller);
		try {
			controller.resume(getClass().getName());
		} catch (ScanningException | InterruptedException e) {
			throw new EventException(e);
		}
	}

	@Override
	public void terminate() throws EventException {
		logger.trace("terminate() {} with controller {}", bean, controller);

		if (bean.getStatus() == Status.COMPLETE) {
			return; // Nothing to terminate.
		}
		terminated = true;
		try {
			if (controller != null) {
				controller.abort(getClass().getName());
			}
			if (scriptRunning) {
				scriptService.abortScripts();
			}
			if (settingPosition) {
				positioner.abort();
			}
		} catch (ScanningException | InterruptedException e) {
			throw new EventException(e);
		}
	}

	@Override
	public void execute() throws EventException {
		logger.debug("Starting to run : {}", bean);

		// some initial tasks to possibly tweak and validate the scan bean and request
		final ScanModel scanModel = prepareScan();
		try {
			// Move to a position if they set one
			setPosition(bean.getScanRequest().getStartPosition(), "start");

			// Run a script, if any has been requested
			runScript(bean.getScanRequest().getBeforeScript(), scanModel, false);

			createAnnotationManager(scanModel).invoke(PrepareScan.class, scanModel);

			// Run the actual scan. If this process is blocking, also runs after script and moves to end position, if set
			runScan(scanModel);

			if (!bean.getStatus().isFinal() || bean.getStatus() == Status.COMPLETE) {
				// set status to COMPLETE and percent complete to 100, unless status was a final status other than COMPLETE
				bean.setPercentComplete(100);
				updateBean(Status.COMPLETE, "Scan Complete");
			}
			logger.info("Completed run normally {} {} {}", bean.getName(), bean.getUniqueId(), bean.getFilePath());
		} catch (Exception e) {
			handleException(e);
		} finally {
			logger.debug("Completed ScanProcess.execute() {}", bean);
		}
	}

	private AnnotationManager createAnnotationManager(ScanModel scanModel) {
		AnnotationManager manager = new AnnotationManager(Activator.createResolver());
		manager.addDevices(scanModel.getScannables());
		manager.addDevices(scanModel.getMonitorsPerPoint());
		manager.addDevices(scanModel.getMonitorsPerScan());
		manager.addDevices(scanModel.getDetectors());
		manager.addDevices(scanModel.getAnnotationParticipants());
		return manager;
	}
	private void handleException(Exception e) throws EventException {
		if (e instanceof InterruptedException && terminated) {
			// Scan was aborted by user - ok to swallow InterrutpedException as execute() immediately returns
			logger.info("Scan terminated", e);
			updateBean(Status.TERMINATED, e.getMessage());
		} else {
			logger.error("Cannot execute run {}", bean, e);
			updateBean(Status.FAILED, e.getMessage());
		}
	}

	private ScanModel prepareScan() throws EventException {
		try {
			setFilePath(bean);
			final IPointGenerator<?> pointGenerator = createPointGenerator();
			checkAndFixMonitors(pointGenerator); // removes monitors that are also in the scan as scannables
			return createScanModel(pointGenerator);
		} catch (Exception e) {
			// throw an exception when something goes wrong preparing the scan
			final String errorMessage = "Could not run scan: " + e.getMessage();
			updateBean(Status.FAILED, errorMessage);
			throw new EventException(errorMessage, e);
		}
	}

	private void checkTerminated() throws InterruptedException {
		// Don't continue to run the scan if it has been terminated from another thread
		// This is a back-up incase any part of the scan doesn't itself throw an InterruptedException
		if (terminated) {
			throw new InterruptedException("Scan terminated by user");
		}
	}

	@Override
	public boolean isPaused() {
		try {
			return controller != null && controller.getDevice().getDeviceState() == DeviceState.PAUSED;
		} catch (ScanningException e) {
			logger.error("TODO put description of error here", e);
			return false;
		}
	}

	private void runScan(final ScanModel scanModel) throws Exception {
		checkTerminated();

		this.controller = createRunnableDevice(scanModel);
		validateScanRequest();

		if (blocking) { // Normally the case
			runScanBlocking(controller, scanModel);
		} else {
			runScanNonBlocking(controller);
		}
	}

	private void runScanNonBlocking(IDeviceController controller) throws ScanningException, InterruptedException, TimeoutException, ExecutionException {
		logger.debug("Running non-blocking device {}", controller.getDevice().getName());
		controller.getDevice().start(null);

		final long latchTime = Long.getLong("org.eclipse.scanning.server.servlet.asynchWaitTime", 500);
		logger.debug("Latching on device {} for {}", controller.getDevice().getName(), latchTime);
		controller.getDevice().latch(latchTime, TimeUnit.MILLISECONDS); // Wait for it to do a bit in case of errors.

		logger.warn("Cannot run end script when scan is async. (Scan has not been cancelled, after script has been ignored.)");
		logger.warn("Cannot perform end position when scan is async. (Scan has not been cancelled, end has been ignored.)");
	}

	private void runScanBlocking(IDeviceController controller, ScanModel scanModel)
			throws ScanningException, InterruptedException, TimeoutException, ExecutionException, UnsupportedLanguageException, ScriptExecutionException, EventException {
		logger.debug("Running blocking scan {}", scanModel.getFilePath());
		updateBean(Status.RUNNING, "Starting scan");
		sendJsonScanStartMessage(scanModel);
		final ScanRequest scanRequest = bean.getScanRequest();
		boolean afterScriptRun = false;

		try {
			controller.getDevice().run(null); // Runs until done

			if (scanRequest.getAfterScript() != null || scanRequest.getEndPosition() != null) {
				updateBean(Status.FINISHING, null);
				// Run a script, if any has been requested
				runScript(scanRequest.getAfterScript(), scanModel, false);
				afterScriptRun = true;
				// move to the end position, if one is set
				setPosition(scanRequest.getEndPosition(), "end");
			}

			sendJsonScanEndedMessage(scanModel);
		} finally {
			if (!afterScriptRun && scanRequest.isAlwaysRunAfterScript()) {
				runScript(scanRequest.getAfterScript(), scanModel, true);
			}
			logger.debug("Finished running blocking scan {}", scanModel.getFilePath());
		}
	}

	private void sendJsonScanStartMessage(ScanModel scanModel) {
		buildAndSendJsonScanMessage(ScanMessage.ScanStatus.STARTED, scanModel);
	}

	private void sendJsonScanEndedMessage(ScanModel scanModel) {
		buildAndSendJsonScanMessage(ScanMessage.ScanStatus.FINISHED, scanModel);
	}

	private void buildAndSendJsonScanMessage(final ScanMessage.ScanStatus status, ScanModel scanModel) {
		try {
			final int[] scanShape = scanModel.getPointGenerator().getShape();

			Map<String, Object> processingRequest = null;

			if (bean.getScanRequest().getProcessingRequest() != null) {
				processingRequest = bean.getScanRequest().getProcessingRequest().getRequest();
			}

			final IFilePathService fservice = Services.getFilePathService();
			String visitDir = null;
			if (fservice != null) {
				visitDir = fservice.getVisitDir();
			}

			// Build the message object
			final ScanMessage message = new ScanMessage(status, bean.getFilePath(), visitDir, SwmrStatus.ACTIVE, // SWMR is always active once the scan starts
					bean.getScanNumber(), scanShape,
					scanModel.getScannables().stream().map(IScannable::getName).collect(toList()),
					scanModel.getDetectors().stream().map(IRunnableDevice::getName).collect(toList()),
					bean.getPercentComplete(), // Progress in %
					processingRequest);

			// Send the message
			final MessagingService jms = Services.getGdaMessagingService();
			if (jms == null) {
				return; // Probably running in a unit test
			}
			jms.sendMessage(message);

		} catch (Exception e) {
			logger.error("Failed to send JSON scan message", e);
		}
	}

	private void setPosition(IPosition pos, String location) throws ScanningException, InterruptedException, EventException {
		if (pos != null) {
			checkTerminated();

			updateBean(null, String.format("Moving to %s position", location));
			settingPosition = true;
			try {
				positioner.setPosition(pos);
			} finally {
				settingPosition = false;
			}

			logger.debug("The {} position {} is reached.", location, pos);
		}
	}

	private void validateScanRequest() {
		logger.debug("Validating scan: {}", bean);
		final ScanRequest scanRequest = bean.getScanRequest();
		if (scanRequest.getDetectors() != null && scanRequest.getDetectors().isEmpty()) {
			scanRequest.setDetectors(null);
		}
		Services.getValidatorService().validate(scanRequest);
		logger.debug("Validating passed : {}", bean);
	}

	/**
	 * Checks the monitors in the scan request. This removes from the collection of monitor names the name of any
	 * monitor that is a scannable in the scan.
	 *
	 * @param gen
	 *            point generator
	 * @throws Exception
	 */
	private void checkAndFixMonitors(IPointGenerator<?> gen) {
		Collection<String> monitorNamesPerPoint = bean.getScanRequest().getMonitorNamesPerPoint();
		Collection<String> monitorNamesPerScan = bean.getScanRequest().getMonitorNamesPerScan();
		final List<String> scannableNames = gen.getNames();

		if (monitorNamesPerPoint != null) {
			// remove any monitors
			monitorNamesPerPoint = monitorNamesPerPoint.stream().filter(mon -> !scannableNames.contains(mon)).collect(Collectors.toList());
			bean.getScanRequest().setMonitorNamesPerPoint(monitorNamesPerPoint);
		}
		if (monitorNamesPerScan != null) {
			// remove any monitors
			monitorNamesPerScan = monitorNamesPerScan.stream().filter(mon -> !scannableNames.contains(mon)).collect(Collectors.toList());
			bean.getScanRequest().setMonitorNamesPerScan(monitorNamesPerScan);
		}
	}

	private void setFilePath(ScanBean bean) throws EventException {
		final ScanRequest req = bean.getScanRequest();

		// Set the file path to the next scan file path from the service which manages scan names.
		if (req.getFilePath() == null) {
			final IFilePathService fservice = Services.getFilePathService();
			if (fservice != null) {
				try {
					final String template = req.getSampleData() != null ? req.getSampleData().getName() : null;
					bean.setFilePath(fservice.getNextPath(template));
				} catch (Exception e) {
					throw new EventException(e);
				}
			} else {
				bean.setFilePath(null); // It is allowable to run a scan without a nexus file
			}
		} else {
			bean.setFilePath(req.getFilePath());
		}
		logger.debug("Nexus file path set to {}", bean.getFilePath());
	}

	private void runScript(ScriptRequest req, ScanModel scanModel, boolean runEvenIfScanTerminated) throws UnsupportedLanguageException, ScriptExecutionException, EventException, InterruptedException {
		if (req == null) {
			return; // Nothing to do
		}
		if (scriptService == null) {
			throw new ScriptExecutionException("No script service is available, cannot run script request " + req);
		}
		if (!runEvenIfScanTerminated) {
			checkTerminated();
		}

		final String scriptName = new File(req.getFile()).getName();
		updateBean(null, String.format("Running script %s", scriptName));

		scriptService.setNamedValue(IScriptService.VAR_NAME_SCAN_BEAN, bean);
		scriptService.setNamedValue(IScriptService.VAR_NAME_SCAN_REQUEST, bean.getScanRequest());
		scriptService.setNamedValue(IScriptService.VAR_NAME_SCAN_MODEL, scanModel);
		scriptService.setNamedValue(IScriptService.VAR_NAME_SCAN_PATH, scanModel.getPointGenerator());

		scriptRunning = true;
		try {
			scriptService.execute(req);
		} finally {
			scriptRunning = false;
		}
	}

	private IDeviceController createRunnableDevice(ScanModel scanModel) throws ScanningException, EventException {

		final ScanRequest scanRequest = bean.getScanRequest();
		if (scanRequest == null) {
			throw new ScanningException("There must be a scan request to run a scan!");
		}

		try {
			configureDetectors(scanRequest.getDetectors(), scanModel);

			final IPausableDevice<ScanModel> device = (IPausableDevice<ScanModel>) Services.getRunnableDeviceService().createRunnableDevice(scanModel, publisher, false);
			final IDeviceController theController = Services.getWatchdogService().create(device, bean);
			if (theController.getObjects() != null) {
				scanModel.setAnnotationParticipants(theController.getObjects());
			}

			logger.debug("Configuring {} with {}", device.getName(), scanModel);
			device.configure(scanModel);
			logger.debug("Configured {}", device.getName());
			return theController;

		} catch (EventException e) {
			updateBean(Status.FAILED, e.getMessage());
			throw e;
		} catch (Exception e) {
			updateBean(Status.FAILED, e.getMessage());
			throw new EventException(e);
		}
	}

	private ScanModel createScanModel(IPointGenerator<?> generator)
			throws GeneratorException, EventException {
		// converts the ScanBean to a ScanModel
		final ScanModel scanModel = new ScanModel();
		scanModel.setFilePath(bean.getFilePath());
		scanModel.setPointGenerator(generator);
		bean.setSize(generator.size());

		final ScanRequest req = bean.getScanRequest();
		scanModel.setDetectors(getDetectors(req.getDetectors()));

		// Note: no need to set the scannables as AcquisitionDevice can determine them from the point generator
		scanModel.setMonitorsPerPoint(getScannables(req.getMonitorNamesPerPoint()));
		scanModel.setMonitorsPerScan(getScannables(req.getMonitorNamesPerScan()));
		scanModel.setScanMetadata(req.getScanMetadata());
		scanModel.setTemplateFilePath(req.getTemplateFilePaths());
		scanModel.setBean(bean);

		final ScanInformation scanInfo = new ScanInformation(generator, req.getDetectors().values(), bean.getFilePath());
		scanModel.setScanInformation(scanInfo);
		return scanModel;
	}

	private void configureDetectors(Map<String, Object> detectorModels, ScanModel model) throws Exception {
		if (detectorModels == null || detectorModels.isEmpty()) {
			logger.debug("No detectors to configure");
			return;
		}

		logger.debug("Configuring detectors {}", detectorModels.keySet());
		for (IRunnableDevice<?> device : model.getDetectors()) {

			final AnnotationManager manager = new AnnotationManager(Activator.createResolver());
			manager.addDevices(device);
			manager.addContext(model.getScanInformation());

			@SuppressWarnings("unchecked")
			final IRunnableDevice<Object> odevice = (IRunnableDevice<Object>) device;

			if (!detectorModels.containsKey(odevice.getName())) {
				continue; // Nothing to configure
			}
			final Object dmodel = detectorModels.get(odevice.getName());

			final IPointGenerator<?> generator = model.getPointGenerator();
			manager.invoke(PreConfigure.class, dmodel, generator, model, bean, publisher);
			odevice.configure(dmodel);
			manager.invoke(PostConfigure.class, dmodel, generator, model, bean, publisher);
		}
		logger.debug("Configured detectors {}", detectorModels.keySet());
	}

	private IPointGenerator<?> createPointGenerator() throws GeneratorException {
		final IPointGeneratorService service = Services.getGeneratorService();
		final ScanRequest scanRequest = bean.getScanRequest();
		if (scanRequest.getDetectors() != null) {
			// if theres a malcolm device, set the duration of the compound model to its exposure time
			scanRequest.getDetectors().values().stream()
				.filter(IMalcolmModel.class::isInstance).map(IMalcolmModel.class::cast)
				.findFirst().ifPresent(model -> scanRequest.getCompoundModel().setDuration(model.getExposureTime()));
		}

		return service.createCompoundGenerator(scanRequest.getCompoundModel());
	}

	private List<IRunnableDevice<?>> getDetectors(Map<String, ?> detectors) throws EventException {

		if (detectors == null) {
			return null;
		}
		try {
			final List<IRunnableDevice<?>> ret = new ArrayList<>(3);
			final IRunnableDeviceService service = Services.getRunnableDeviceService();

			for (Entry<String, ?> detectorEntry : detectors.entrySet()) {
				IRunnableDevice<Object> detector = service.getRunnableDevice(detectorEntry.getKey());
				if (detector == null) {
					detector = service.createRunnableDevice(detectorEntry.getValue(), false);
					detector.setName(detectorEntry.getKey()); // Not sure whether this is ok. For now name must match that in table
				}
				ret.add(detector);
			}
			return ret;

		} catch (ScanningException ne) {
			throw new EventException(ne);
		}
	}

	private List<IScannable<?>> getScannables(Collection<String> scannableNames) throws EventException {
		// used to get the monitors and the metadata scannables
		if (scannableNames == null) {
			return null;
		}
		try {
			final List<IScannable<?>> ret = new ArrayList<>(3);
			for (String name : scannableNames) {
				ret.add(Services.getConnector().getScannable(name));
			}
			return ret;
		} catch (ScanningException ne) {
			throw new EventException(ne);
		}
	}

	private void broadcast(ScanBean bean) throws EventException {
		if (publisher != null) {
			publisher.broadcast(bean);
		}
	}

	@Override
	public ScanBean getBean() {
		return bean;
	}

	@Override
	public IPublisher<ScanBean> getPublisher() {
		return publisher;
	}

}