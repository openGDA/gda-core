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
package org.eclipse.scanning.sequencer;

import static java.util.stream.Collectors.toList;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.AnnotationManager;
import org.eclipse.scanning.api.annotation.scan.FileDeclared;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.ScanAbort;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFault;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanPause;
import org.eclipse.scanning.api.annotation.scan.ScanResume;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.annotation.scan.WriteComplete;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.ScanMode;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.event.IMalcolmEventListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent.MalcolmEventType;
import org.eclipse.scanning.api.malcolm.event.MalcolmStepsCompletedEvent;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.sequencer.nexus.INexusScanFileManager;
import org.eclipse.scanning.sequencer.nexus.NexusScanFileManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This device does a standard GDA scan at each point. If a given point is a
 * MalcolmDevice, that device will be configured and run for its given point.
 *
 * The levels of the scannables at the position will be taken into
 * account and the position reached using an IPositioner then the
 * scanners run.
 *
 * An {@link AcquisitionDevice} instance is created by <code>ScanProcess</code>
 * to run the scan defined by a {@link ScanRequest}. <code>ScanProcess</code>
 * has overall responsiblity for the scan. In particular some tasks may need
 * to be performed by <code>ScanProcess</code> after {@link AcquisitionDevice} has
 * finished the scan, such as running an after script.
 *
 * @author Matthew Gerring
 */
final class AcquisitionDevice extends AbstractRunnableDevice<ScanModel> implements IScanDevice, IMalcolmEventListener {

	// Scanning stuff
	private IPositioner positioner;
	private LevelRunner<IRunnableDevice<?>> runners;
	private LevelRunner<IRunnableDevice<?>> writers;
	private AnnotationManager annotationManager;
	private ExposureTimeManager exposureManager;
	private Set<IPositionListener> positionListeners = new CopyOnWriteArraySet<>();


	// the nexus file
	private INexusScanFileManager nexusScanFileManager = null;

	private static Logger logger = LoggerFactory.getLogger(AcquisitionDevice.class);

	/*
	 * Concurrency design recommended by Keith Ralphs after investigating
	 * how to pause and resume a collection cycle using Reentrant locks.
	 * Design requires these three fields.
	 */
	private ReentrantLock    stateChangeLock;
	private Condition        shouldResumeCondition;
	private volatile boolean awaitPaused;

	/**
	 * Used for clients that would like to wait until the run. Most useful
	 * if the run was started with a start() call then more work is done, then
	 * a latch() will join with the start and return once it is finished.
	 * <p>
	 * If the start hangs, so will calling latch: there is no timeout.
	 *
	 */
	private CountDownLatch scanFinishedLatch;

	private List<Throwable> runExceptions = Collections.synchronizedList(new ArrayList<>(1));

	private ScanBean scanBean;

	/**
	 * Manages the positions we reach in the scan, including
	 * the outer scan location.
	 */
	private LocationManager location;

	/**
	 * The current position iterator we are using
	 * to move over the CPU scan.
	 */
	private Iterator<IPosition> positionIterator;

	private Optional<IMalcolmDevice<?>> malcolmDevice;

	/**
	 * Package private constructor, devices are created by the service.
	 */
	AcquisitionDevice() {
		super(ServiceHolder.getRunnableDeviceService());
		this.stateChangeLock = new ReentrantLock();
		this.shouldResumeCondition = stateChangeLock.newCondition();
		setName("solstice_scan");
		setRole(DeviceRole.VIRTUAL);
		setSupportedScanModes(EnumSet.allOf(ScanMode.class));
	}

	private ScanBean getScanBean() {
		return scanBean;
	}

	private void setupScanBean(ScanModel model) throws ScanningException {
		scanBean = model.getBean();
		if (scanBean == null) {
			scanBean = new ScanBean();
			model.setBean(scanBean);
		}
		try {
			scanBean.setHostName(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			throw new ScanningException("Unable to read name of host!");
		}
	}

	/**
	 * Method to configure the device. It also will check if the
	 * declared devices in the scan are INexusDevice. If they are,
	 * it will hook them up to the file writing if the ScanModel
	 * file is set. If there is no file set in the model, the scan
	 * will proceed but not write to a nexus file.
	 */
	@Override
	public void configure(ScanModel model) throws ScanningException {
		logger.debug("Configuring with model: {}", model);
		long before = System.currentTimeMillis();

		setupScanBean(model);
		setDeviceState(DeviceState.CONFIGURING);
		setModel(model);
		getScanBean().setPreviousStatus(getScanBean().getStatus());
		getScanBean().setStatus(Status.PREPARING);
		malcolmDevice = findMalcolmDevice(model);

		// set the scannables on the scan model if not already set
		setScannables(model);

		positioner = createPositioner(model);

		// Create the manager and populate it
		if (annotationManager!=null) annotationManager.dispose(); // It is allowed to configure more than once.
		annotationManager = createAnnotationManager(model);

		// create the location manager
		location = new LocationManager(getScanBean(), model, annotationManager);

		// add the scan information to the context - it is created if not set on the scan model
		annotationManager.addContext(getScanInformation());
		annotationManager.addContext(getPublisher());
		exposureManager = new ExposureTimeManager(this);
		exposureManager.addDevices(model.getDetectors());

		// create the nexus file, if appropriate
		nexusScanFileManager = NexusScanFileManagerFactory.createNexusScanFileManager(this);
		nexusScanFileManager.configure(model);
		nexusScanFileManager.createNexusFile(Boolean.getBoolean("org.eclipse.scanning.sequencer.nexus.async"));

		// create the runners and writers
		if (model.getDetectors().isEmpty()) {
			runners = LevelRunner.createEmptyRunner();
			writers = LevelRunner.createEmptyRunner();
		} else {
			runners = new DeviceRunner(this, model.getDetectors());
			if (nexusScanFileManager.isNexusWritingEnabled()) {
				writers = new DeviceWriter(this, model.getDetectors());
			} else {
				writers = LevelRunner.createEmptyRunner();
			}
		}

		// notify that the device is now armed, this publishes the scan bean
		setDeviceState(DeviceState.ARMED);

		// record the time taken to configure the device
		long after = System.currentTimeMillis();
		setConfigureTime(after-before);
	}

	private void setScannables(ScanModel model) throws ScanningException {
		List<IScannable<?>> scannables = model.getScannables();
		if (scannables == null) {
			final List<String> malcolmControlledAxisNames =
					malcolmDevice.isPresent() ? malcolmDevice.get().getAvailableAxes() : Collections.emptyList();
			final List<String> allScannableNames = getScannableNames(model.getPointGenerator());
			final List<String> scannableNames = allScannableNames.stream()
					.filter(axisName -> !malcolmControlledAxisNames.contains(axisName))
					.collect(toList());
			model.setScannables(connectorService.getScannables(scannableNames));
		}
	}

	@SuppressWarnings("unchecked")
	private Optional<IMalcolmDevice<?>> findMalcolmDevice(ScanModel scanModel) {
		return (Optional<IMalcolmDevice<?>>) (Optional<?>) scanModel.getDetectors().stream() // why is the double-cast necessary?
				.filter(IMalcolmDevice.class::isInstance)
				.findFirst();
	}

	private IPositioner createPositioner(ScanModel model) throws ScanningException {
		IPositioner poser = runnableDeviceService.createPositioner(this);

		// We allow monitors which can block a position until a setpoint is
		// reached or add an extra record to the NeXus file.
		poser.setMonitorsPerPoint(model.getMonitorsPerPoint());
		poser.setScannables(model.getScannables());

		return poser;
	}

	private AnnotationManager createAnnotationManager(ScanModel model) {
		Collection<Object> globalParticipants = ((IScanService)runnableDeviceService).getScanParticipants();
		AnnotationManager manager = new AnnotationManager(SequencerActivator.getInstance());
		manager.addDevices(model.getScannables());
		manager.addDevices(model.getMonitorsPerPoint());
		manager.addDevices(model.getMonitorsPerScan());
		manager.addDevices(model.getAnnotationParticipants());
		manager.addDevices(globalParticipants);
		manager.addDevices(model.getDetectors());

		return manager;
	}

	@Override
	public void start(IPosition parent) throws ScanningException, InterruptedException, TimeoutException, ExecutionException {
		logger.debug("start() called with position: {}", parent);
		createScanLatch();
		super.start(parent);
	}

	@Override
	public void run(IPosition parent) throws ScanningException, InterruptedException {
		// parent is usually null for and is not used in this class
		logger.debug("run() called with position: {}", parent);

		if (getDeviceState()!=DeviceState.ARMED) throw new ScanningException("The device '"+getName()+"' is not armed. It is in state "+getDeviceState());
		createScanLatch();

		ScanModel model = getModel();
		if (model.getPointGenerator()==null) throw new ScanningException("The model must contain some points to scan!");

		annotationManager.addContext(getScanBean());
		annotationManager.addContext(model);

		boolean errorFound = false;
		IPosition pos = null;
		try {
			this.positionIterator = location.createPositionIterator();

			RunnableDeviceServiceImpl.setCurrentScanningDevice(this); // Alows Jython to get and pause/seek.

			// TODO Should we validate the position iterator that all
			// the positions are valid before running the scan?
			// It was called limit checking in GDA.
			// Sometimes logic is needed to implement collision avoidance

			// Set the size and declare a count
			fireStart(location.getTotalSize());

			// Add the malcolm listners so that progress on inner malcolm scans can be reported
			addMalcolmListeners();

			// The scan loop
			boolean firedFirst = false;
			while (positionIterator.hasNext()) {

				pos = positionIterator.next();
				pos.setStepIndex(location.getStepNumber());

				if (!firedFirst) {
					fireFirst(pos);
					firedFirst = true;
				}

				// Check if we are paused, if blocks until pause is clear, returns if we should continue
				if (!checkShouldContinue())
					return; // finally block performed

				// Run to the position
				annotationManager.invoke(PointStart.class, pos);
				positioner.setPosition(pos); // moveTo in GDA8
				firePositionMoveComplete(pos); // notify listers that the move is complete

				// Check again if we are paused, as this may have been received during an positioner move
				if (!checkShouldContinue())
					return;

				exposureManager.setExposureTime(pos); // most of the time this does nothing.

				IPosition written = writers.await(); // Wait for the previous write out to return, if any
				if (written != null)
					annotationManager.invoke(WriteComplete.class, written);

				runners.run(pos); // GDA8: collectData() / GDA9: run() for Malcolm
				writers.run(pos, false); // Do not block on the readout, move to the next position immediately.

				// Send an event about where we are in the scan
				annotationManager.invoke(PointEnd.class, pos);
				positionComplete(pos);

				logger.info("Scanning completed step {} . Position was {}", location.getStepNumber(), pos);
			}

			// On the last iteration we must wait for the final readout.
			IPosition written = writers.await(); // Wait for the previous write out to return, if any
			annotationManager.invoke(WriteComplete.class, written);

		} catch (CancellationException e) {
			// this can be thrown from LevelRunner if abort() is called. Exit normally
			logger.info("Scan aborted, exiting normally");
		} catch (ScanningException | InterruptedException i) {
			errorFound = true;
			processException(i);
			throw i;
		} catch (Exception ne) {
			errorFound = true;
			processException(ne);
			throw new ScanningException(ne);
		} finally {
			close(errorFound, pos);
			logger.debug("Scan completed with status {}", getScanBean().getStatus());
			RunnableDeviceServiceImpl.setCurrentScanningDevice(null); // TODO fix this to not use a static method
		}
	}

	private void positionComplete(IPosition pos) throws EventException, ScanningException {
		int count = location.getOuterCount();
		int size = location.getOuterSize();
		firePositionComplete(pos);

		final ScanBean bean = getScanBean();
		bean.setPoint(count);
		bean.setPosition(pos);
		bean.setPreviousDeviceState(bean.getDeviceState());
		if (size>-1) bean.setPercentComplete(((double)(count)/size)*100);
		if (bean.getDeviceState()==DeviceState.RUNNING) { // Only set this message if we are still running.
			bean.setMessage("Point " + (pos.getStepIndex() + 1) +" of " + size);
		}

		IPublisher<ScanBean> publisher = getPublisher();
		if (publisher != null) {
			publisher.broadcast(bean);
		}
	}

	private void fireFirst(IPosition firstPosition) throws ScanningException {
		// Notify that we will do a run and provide the first position.
		annotationManager.invoke(ScanStart.class, firstPosition);
		if (model.getFilePath()!=null) annotationManager.invoke(FileDeclared.class, model.getFilePath(), firstPosition);

		final Set<String> otherFiles = nexusScanFileManager.getExternalFilePaths();
		if (otherFiles != null && !otherFiles.isEmpty()) {
			for (String path : otherFiles) { // can't use java 8 stream with lambdas due to checked exceptions
				if (path!=null) {
					annotationManager.invoke(FileDeclared.class, path, firstPosition);
				}
			}
		}

		fireRunWillPerform(firstPosition);
	}

	/**
	 * Add this to the list of position listeners for any Malcolm Device
	 */
	private void addMalcolmListeners() {
		malcolmDevice.ifPresent(dev -> dev.addMalcolmListener(this));
	}

	/**
	 * Remove this from the list of position listeners for any Malcolm Device
	 */
	private void removeMalcolmListeners() {
		malcolmDevice.ifPresent(dev -> dev.removeMalcolmListener(this));
	}

	@Override
	public void eventPerformed(MalcolmEvent event) {
		// We don't actually need to do anything when the malcolm state changes, except log it
		// See DAQ-1498 and DAQ-1499
		if (event.getEventType() == MalcolmEventType.STATE_CHANGED) {
			// We don't actually need to do anything when the malcolm state changes, except log it
			// See DAQ-1498 and DAQ-1499
			logger.info("Received malcolm state change event {}", event);
		} else if (event.getEventType() == MalcolmEventType.STEPS_COMPLETED) {
			logger.trace("Received malcolm steps completed event {}", event);
			getScanBean().setMessage(event.getMessage());
			location.setStepNumber(((MalcolmStepsCompletedEvent) event).getStepsCompleted());
			innerScanStepsCompleted();
		}
	}

	private void close(boolean errorFound, IPosition last) throws ScanningException {
		try {
			try {
				try {
					removeMalcolmListeners();
				} catch (Exception ex) {
					logger.warn("Error during removing Malcolm listeners", ex);
				}
				positioner.close();
				runners.close();
				writers.close();

				nexusScanFileManager.scanFinished(); // writes scanFinished and closes nexus file

				// We should not fire the run performed until the nexus file is closed.
				// Tests wait for this step and reread the file.
				fireRunPerformed(last); // Say that we did the overall run using the position we stopped at.
			} finally {
				// only fire end if finished normally
				if (!errorFound && getDeviceState() != DeviceState.ABORTED)
					fireEnd(last);
			}
		} finally {
			try {
				annotationManager.invoke(ScanFinally.class, last);
			} finally {
				if (scanFinishedLatch != null) {
					scanFinishedLatch.countDown();
				}
			}
		}
	}

	private void createScanLatch() {
		if (scanFinishedLatch==null || scanFinishedLatch.getCount()<1) {
			scanFinishedLatch = new CountDownLatch(1);
			runExceptions.clear();
		}
	}

	@Override
	public void latch() throws ScanningException, InterruptedException, TimeoutException, ExecutionException {
		if (scanFinishedLatch==null) return;
		logger.debug("latch() called");
		scanFinishedLatch.await();
		createException(runExceptions);
	}

	@Override
	public boolean latch(long time, TimeUnit unit) throws ScanningException, InterruptedException, TimeoutException, ExecutionException {
		if (scanFinishedLatch==null) return true;
		logger.debug("latch() called with timeout {} {}", time, unit);
		boolean ok = scanFinishedLatch.await(time, unit);
		createException(runExceptions);
		return ok;
	}

	private void processException(Exception ne) throws ScanningException {
		if (ne instanceof InterruptedException) {
			logger.warn("A device may have timed out", ne);
		} else {
			logger.debug("An error happened in the scan", ne);
		}
		runExceptions.add(ne);
		if (!getScanBean().getStatus().isFinal()) getScanBean().setStatus(Status.FAILED);
		getScanBean().setMessage(ne.getMessage());
		try {
			annotationManager.invoke(ScanFault.class, ne);
		} finally {
			setDeviceState(DeviceState.FAULT);

			if (!getScanBean().getStatus().isFinal()) getScanBean().setStatus(Status.FAILED);
			getScanBean().setMessage(ne.getMessage());
		}
	}

	private ScanInformation getScanInformation() throws ScanningException {
		ScanInformation scanInfo = getModel().getScanInformation();
		if (scanInfo == null) {
			try {
				List<Object> detectorModels = getModel().getDetectors().stream()
						.map(IRunnableDevice::getModel).collect(toList());
				scanInfo = new ScanInformation(getModel().getPointGenerator(), detectorModels, getModel().getFilePath());
			} catch (GeneratorException e) {
				throw new ScanningException("Could not create ScanInformation", e);
			}
			getModel().setScanInformation(scanInfo);
		}

		return scanInfo;
	}

	private void fireStart(int size) throws ScanningException {
		logger.debug("publishing scan bean for scan start");
		// Setup the bean to sent
		getScanBean().setSize(size);
		ScanInformation scanInfo = getModel().getScanInformation();
		getScanBean().setStartTime(System.currentTimeMillis());
		getScanBean().setEstimatedTime(scanInfo.getEstimatedScanTime());
		getScanBean().setPreviousStatus(getScanBean().getStatus());
		getScanBean().setStatus(Status.RUNNING);

		// Will send the state of the scan off.
		setDeviceState(DeviceState.RUNNING); // Fires!

		// Leave previous state as running now that we have notified of the start.
		getScanBean().setPreviousStatus(Status.RUNNING);
	}

	private void fireEnd(IPosition lastPosition) throws ScanningException {
		logger.debug("updating and publishing scan bean for scan end");

		// Setup the bean to show the scan is finished.
		// Note: we don't set the scan state to COMPLETED as ScanProcess may have
		// to perform some final tasks, e.g. an 'after scan' script
		getScanBean().setPercentComplete(100);
		getScanBean().setMessage("Scan Complete");

		// Will send the state of the scan off.
		annotationManager.invoke(ScanEnd.class, lastPosition);
		setDeviceState(DeviceState.ARMED); // publishes the scan bean

	}

	@Override
	protected void setDeviceState(DeviceState newDeviceState) throws ScanningException {
		try {
			// The bean must be set in order to change state.
			ScanBean bean = getScanBean();
			bean.setDeviceName(getName());
			bean.setPreviousDeviceState(bean.getDeviceState());
			bean.setDeviceState(newDeviceState);

			super.setDeviceState(newDeviceState);

			IPublisher<ScanBean> publisher = getPublisher();
			if (publisher!=null) {
				publisher.broadcast(bean);
			}
		} catch (ScanningException e) {
			throw e;
		} catch (Exception ne) {
			throw new ScanningException(this, ne);
		}
	}

	@Override
	public void reset() throws ScanningException {
		logger.error("reset() unsupported");
		// Cannot reset an Acquisition Device. A new one must be created for each scan.
		throw new UnsupportedOperationException("reset not supported");
	}

	/**
	 * This method performs two functions:<ol>
	 * <li>It checks if the scan has been put in a final state, e.g. {@link DeviceState#ABORTED},
	 * if so it returns <code>false</code></li>
	 * <li>It checks if the {@link #awaitPaused} flag has been set, if so it waits
	 * on the {@link #shouldResumeCondition} {@link Condition}.</li>
	 *
	 * </ol>
	 *
	 * @return true if state has not been set to a rest one, i.e. we are still scanning.
	 * @throws Exception
	 */
	private boolean checkShouldContinue() throws Exception {

		// return false if the scan has been set to a rest state (i.e. ABORTED) or ABORTING
		if (!getDeviceState().isRunning() && getDeviceState() != DeviceState.ARMED) {
			if (getDeviceState().isRestState() || getDeviceState() == DeviceState.ABORTING) {
				return false;
			}
			throw new ScanningException("The scan state is " + getDeviceState());
		}

		// Check the locking using a condition
		if (!stateChangeLock.tryLock(10, TimeUnit.SECONDS)) { // FIXME Calls to Malcolm can take up to 10 seconds to return!
			throw new ScanningException(this, "Internal Error - Could not obtain lock to run device!");
		}
		try {
			if (!(getDeviceState().isRunning() || getDeviceState() == DeviceState.ARMED)) {
				throw new IllegalStateException("Unexpected scan state: " + getDeviceState());
			}
			if (awaitPaused) {
				// the await paused is the flag set to indicate we should pause
				// set state to paused and run any methods annotated with 'ScanPause'
				if (getDeviceState() != DeviceState.PAUSED)
					setDeviceState(DeviceState.PAUSED);
				annotationManager.invoke(ScanPause.class);
				logger.info("Scan paused");

				// Do the pause. This blocks until we are signalled and the awaitPaused flag is cleared
				// the loop is required due to the change of spurious wake-ups, see javadoc of
				// Condition and Condition.await()
				while (awaitPaused) {
					shouldResumeCondition.await();
				}

				getScanBean().setPreviousStatus(getScanBean().getStatus());
				if (getDeviceState().isRestState()) {
					getScanBean().setStatus(Status.TERMINATED);
					logger.info("Scan set to terminated");
				} else {
					// Set the status to resumed and run any methods annotated with 'ScanResume'
					getScanBean().setStatus(Status.RESUMED);
					setDeviceState(DeviceState.RUNNING);
					annotationManager.invoke(ScanResume.class);
					logger.info("Scan resumed");
				}
			}
		} finally {
			stateChangeLock.unlock();
		}

		// check again that the scan hasn't been set to a rest state, this can happen after we resume from pausing
		// Armed is excluded as this is the state malcolm puts the scan in when it has finished an inner scan
		return !getDeviceState().isRestState() || getDeviceState() == DeviceState.ARMED;
	}

	@Override
	public void abort() throws ScanningException, InterruptedException {
		logger.debug("abort() called");
		doWorkWithStateChangeLock(this::abortInternal, null, "abort", false, true);
		logger.debug("abort() exiting");
	}

	private void abortInternal() throws ScanningException, InterruptedException{
		setDeviceState(DeviceState.ABORTING);
		positioner.abort();
		writers.abort();
		runners.abort();

		for (IRunnableDevice<?> device : getModel().getDetectors()) {
			device.abort();
		}
		setDeviceState(DeviceState.ABORTED);
		try {
			annotationManager.invoke(ScanAbort.class);
		} catch (ScanningException e) {
			throw e;
		} catch (Exception other) {
			throw new ScanningException(other);
		}
		RunnableDeviceServiceImpl.setCurrentScanningDevice(null);
	}

	@Override
	public void pause() throws ScanningException, InterruptedException {
		logger.debug("pause() called");
		doWorkWithStateChangeLock(this::pauseInternal, DeviceState.RUNNING, "pause", true, false);
		logger.debug("pause() exiting");
	}

	private void pauseInternal() throws ScanningException, InterruptedException {
		getScanBean().setPreviousStatus(getScanBean().getStatus());
		getScanBean().setStatus(Status.PAUSED);
		setDeviceState(DeviceState.SEEKING);
		for (IRunnableDevice<?> device : getModel().getDetectors()) {
			DeviceState currentState = device.getDeviceState();
			if (currentState.isRunning()) {
				if (device instanceof IPausableDevice) {
					((IPausableDevice<?>) device).pause();
				}
			} else {
				logger.info("Device {} wasn't running to pause. Was {}", device.getName(), currentState);
			}
		}
		setDeviceState(DeviceState.PAUSED);
	}

	@Override
	public void seek(final int stepNumber) throws ScanningException, InterruptedException {
		logger.debug("seek() called to step number {}", stepNumber);
		// This is the values of all motors at this global (including malcolm) scan
		// position. Therefore we do not need a subscan moderator but can run the iterator
		// to the point
		doWorkWithStateChangeLock(()-> seekInternal(stepNumber), DeviceState.PAUSED, "seek", true, false);
		logger.debug("seek() exiting");
	}

	private void seekInternal(int stepNumber) throws ScanningException, InterruptedException {
		if (stepNumber<0) throw new ScanningException("Seek position is invalid "+stepNumber);
		if (stepNumber>location.getTotalSize()) throw new ScanningException("Seek position is invalid "+stepNumber);
		this.positionIterator = location.createPositionIterator();
		IPosition pos = location.seek(stepNumber, positionIterator);
		positioner.setPosition(pos);
		for (IRunnableDevice<?> device : getModel().getDetectors()) {
			if (device instanceof IPausableDevice)
				((IPausableDevice<?>) device).seek(stepNumber);
		}
	}

	@Override
	public void resume() throws ScanningException, InterruptedException {
		logger.debug("resume() called");
		doWorkWithStateChangeLock(this::resumeInternal, DeviceState.PAUSED, "resume", false, true);
		logger.debug("resume() exit");
	}

	private void resumeInternal() throws ScanningException, InterruptedException {
		for (IRunnableDevice<?> device : getModel().getDetectors()) {
			DeviceState currentState = device.getDeviceState();
			if (currentState == DeviceState.PAUSED) {
				if (device instanceof IPausableDevice) {
					((IPausableDevice<?>)device).resume();
				}
			} else {
				logger.info("Device {} wasn't paused to resume. Was {}", device.getName(), currentState);
			}
		}

		getScanBean().setStatus(Status.RESUMED);
		setDeviceState(DeviceState.RUNNING);
	}

	@FunctionalInterface
	private interface IStateChangeWork {
		void doStateChangeWork() throws ScanningException, InterruptedException;
	}

	/**
	 * Does some pause lock safe work. This is designed to make the
	 * device thread safe for running/pausing/aborting/seeking etc.
	 * That is important because we expose the device to a web service
	 * which could allow multiple requests to come in simultaneously.
	 * This design is intentionally overkill for the scenario in which
	 * scanning is used because we want over-design as scanning is reused
	 * in unpredictable scenarios.
	 *
	 * @param work
	 * @param required
	 * @param actionName
	 * @param pauseFlag
	 * @param signal
	 * @throws ScanningException
	 * @throws InterruptedException
	 */
	private void doWorkWithStateChangeLock(IStateChangeWork work, DeviceState required, String actionName, boolean pauseFlag, boolean signal) throws ScanningException, InterruptedException {
		try {
			tryLock(required, actionName);
			awaitPaused = pauseFlag; // sets the flag to pause the scan on the next call to checkPaused
			work.doStateChangeWork();
			if (signal) shouldResumeCondition.signalAll(); // Wake up any thread waiting on the lock (in checkPaused)
		} finally {
			stateChangeLock.unlock();
		}
	}

	/**
	 *
	 * @param required - if null then just the lock is performed
	 * @param actionName
	 * @throws ScanningException
	 */
	private void tryLock(DeviceState required, String actionName) throws ScanningException, InterruptedException {
		try {
			stateChangeLock.lockInterruptibly(); // We want to lock before checking state or it may change...
			DeviceState currentDeviceState = getDeviceState();
			if (required!=null && currentDeviceState != required) {
				throw new ScanningException(this, getName()+" cannot complete action '"+actionName+"'! State was " + currentDeviceState);
			}
		} catch (ScanningException | InterruptedException sne) {
			throw sne;
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
	}

	/**
	 * Calculate and set the position complete value on the scan bean based on an inner position
	 * @param innerCount The count representing the progress of of the inner scan
	 * @throws Exception
	 */
	private void innerScanStepsCompleted() {

		final ScanBean bean = getScanBean();
		bean.setMessage("Point " + location.getOverallCount() + " of " + location.getTotalSize());
		bean.setPercentComplete(location.getOuterPercent());
		bean.setPoint(location.getStepNumber());
		bean.setPreviousDeviceState(bean.getDeviceState()); // this bean doesn't represent a state
		bean.setPreviousStatus(bean.getStatus()); // or status change

		if (getPublisher() != null) {
			try {
				getPublisher().broadcast(bean);
			} catch (EventException e) {
				logger.warn("An error occurred publishing percent complete event ", e);
			}
		}
	}

	private List<String> getScannableNames(Iterable<IPosition> gen) {
		return model.getPointGenerator().getNames();
	}

	@Override
	public IPositioner getPositioner() {
		return positioner;
	}

	@Override
	public void addPositionListener(IPositionListener posListener) {
		positionListeners.add(posListener);
	}

	@Override
	public void removePositionListener(IPositionListener posListener) {
		positionListeners.remove(posListener);
	}

	private void firePositionComplete(IPosition position) throws ScanningException {
		final PositionEvent event = new PositionEvent(position, this);
		for (IPositionListener listener : positionListeners) {
			listener.positionPerformed(event);
		}
	}

	private void firePositionMoveComplete(IPosition position) throws ScanningException {
		final PositionEvent event = new PositionEvent(position, this);
		for (IPositionListener listener : positionListeners) {
			listener.positionMovePerformed(event);
		}
	}



}
