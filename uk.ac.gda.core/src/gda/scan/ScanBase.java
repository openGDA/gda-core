/*-
 * Copyright © 2014 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.scan;

import static gda.jython.InterfaceProvider.getCurrentScanInformationHolder;
import static gda.jython.InterfaceProvider.getDefaultScannableProvider;
import static gda.jython.InterfaceProvider.getJythonServerNotifer;
import static gda.jython.InterfaceProvider.getScanStatusHolder;
import static gda.jython.InterfaceProvider.getTerminalPrinter;
import static gda.scan.ScanDataPoint.handleZeroInputExtraNameDevice;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.python.core.PyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.DefaultDataWriterFactory;
import gda.data.scan.datawriter.INexusDataWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.ProcessingRequestProvider;
import gda.device.Scannable;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer.JythonServerThread;
import gda.jython.JythonStatus;
import gda.jython.ScriptBase;
import gda.scan.Scan.ScanStatus;
import gda.scan.ScanEvent.EventType;
import gda.scan.ScanInformation.ScanInformationBuilder;
import gda.util.OSCommandRunner;
import uk.ac.diamond.daq.api.messaging.MessagingService;
import uk.ac.diamond.daq.api.messaging.messages.ScanMessage;
import uk.ac.diamond.daq.api.messaging.messages.SwmrStatus;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.core.GDACoreActivator;

/**
 * Base class for objects using the Scan interface.
 */
public abstract class ScanBase implements NestableScan {


	public static final String GDA_SCANBASE_FIRST_SCAN_NUMBER_FOR_TEST = "gda.scanbase.firstScanNumber";

	public static final String GDA_SCANBASE_PRINT_TIMESTAMP_TO_TERMINAL= "gda.scanbase.printTimestamp";

	private static final Logger logger = LoggerFactory.getLogger(ScanBase.class);

	/**
	 * Return a string representation of an error in the form 'ExceptionTypeName:message'. Useful for
	 * e.g. logging. Works for exceptions thrown from Jython code, which would otherwise always contain
	 * a null message.
	 */
	public static String representThrowable(Throwable e) {
		String message = (e instanceof PyException) ? e.toString() : e.getMessage();
		return e.getClass().getSimpleName() + ":" + message;
	}

	/**
	 * all the detectors being operated in this scan. This vector is generated from detectors in this.allScannables and
	 * DetetcorBase.activeDetectors This list is to be used by DataHandlers when writing out the data.
	 */
	protected Vector<Detector> allDetectors = new Vector<>();

	/**
	 * all the scannables being operated in this scan, but *not* Detectors. for some scan types this may be a single
	 * scannable object.
	 */
	protected Vector<Scannable> allScannables = new Vector<>();

	protected Scan child = null;

	/**
	 * Command line.
	 */
	protected String command = "";

	/**
	 * Counter to get the current point number. 0 based as for ScanDataPoint
	 */
	protected int currentPointCount = -1;

	/**
	 * instrument name.
	 */
	protected String instrument = "";

	/**
	 * to allow nested scans to ignore the baton (as it will have already been taken)
	 */
	protected boolean isChild = false;

	private boolean lineScanNeedsDoing;

	private DataWriter manuallySetDataWriter = null;

	/**
	 * unique identifier for this scan
	 */
	protected String name = "";

	protected int numberOfChildScans = 0;

	protected NestableScan parent = null;

	// attributes relating to the thread which started this scan.
	protected int permissionLevel = 0;

	ScanDataPoint point = null;

	private int pointNumberAtLineBeginning;

	private int positionCallableThreadPoolSize = 3;

	/**
	 * Used to broadcast points and to write them to a DataWriter. Created before a scan is run.
	 */
	protected ScanDataPointPipeline scanDataPointPipeline = null;

	private int scanDataPointQueueLength = 3;

	ScanPlotSettings scanPlotSettings;

	protected IScanStepId stepId = null;

	protected boolean threadHasBeenAuthorised = false;

	protected int TotalNumberOfPoints = 0;

	/**
	 * The unique number for this scan. Set in direct call to prepareScanNumber and in prepareScanForCollection.
	 */
	private int scanNumber = -1;

	protected boolean callCollectDataOnDetectors = true;

	// TODO This should be null for non-parents. For now we make it null in setIsChild(true)
	protected ParentScanComponent parentComponent = new ParentScanComponent(ScanStatus.NOTSTARTED);

	private final boolean isScripted;

	private Map<String, Collection<Object>> procReq;

	@Override
	public int getScanNumber() {
		return scanNumber;
	}

	public void setScanNumber(int scanNumber){
		this.scanNumber = scanNumber;
	}

	public ScanBase() {
		// randomly create the name
		name = generateRandomName();

		instrument = LocalProperties.get(LocalProperties.GDA_INSTRUMENT, "unknown");

		// rbac: you must be the baton holder to be able to create scans. Scan should also run within a Thread which
		// has the same properties as a thread from the Command server so the rbac system works.

		if (Thread.currentThread() instanceof JythonServerThread) {
			JythonServerThread currentThread = (JythonServerThread) Thread.currentThread();
			permissionLevel = currentThread.authorisationLevel;
			threadHasBeenAuthorised = currentThread.hasBeenAuthorised;
			isScripted = currentThread.isScript();
		} else {
			permissionLevel = InterfaceProvider.getAuthorisationHolder().getAuthorisationLevel();
			threadHasBeenAuthorised = false;
			isScripted = false;
		}
	}

	@Override
	public void requestFinishEarly() {
		if (isChild()){
			parent.requestFinishEarly();
		} else {
			parentComponent.requestFinishEarly();
		}
	}

	@Override
	public boolean isFinishEarlyRequested() {
		if (isChild()){
			return parent.isFinishEarlyRequested();
		}
		return parentComponent.isFinishEarlyRequested();
	}

	@Override
	public void setStatus(ScanStatus status) {
		if (isChild()){
			parent.setStatus(status);
		} else {
			parentComponent.setStatus(status);
		}
		sendScanEvent(ScanEvent.EventType.UPDATED);
	}

	@Override
	public ScanStatus getStatus() {
		if (isChild()){
			return parent.getStatus();
		}
		return parentComponent.getStatus();
	}

	protected void waitIfPaused() throws InterruptedException{
		if (isScripted) {
			if (ScriptBase.isPaused()) {
				logger.debug("Script was paused while scan was running. Pausing scan and clearing script pause flag");
				pause();
				ScriptBase.setPaused(false);
			}
		}
		while (getStatus() == ScanStatus.PAUSED) {
			if (isFinishEarlyRequested()) {
				return;
			}
			Thread.sleep(1000);
		}
	}

	/**
	 * Returns true if the scan baton has been claimed by a scan that has already started.
	 *
	 * @return boolean
	 */
	public boolean scanRunning() {
		return getStatus().asJython() == JythonStatus.RUNNING;
	}

	private static double sortArgs(double start, double stop, double step) {
		// if start > stop then step should be negative
		if (start > stop && step > 0) {
			step = (-step);
			return step;
		}
		// if stop is larger then step must be positive
		else if (start < stop && step < 0) {
			step = (-step);
			return step;
		} else {
			return step;
		}
	}

	private static Object listWrapArray(Object foo) {
		if (foo.getClass().isArray()) {
			return Arrays.asList((Object[]) foo);
		}
		return foo;
	}

	/**
	 * Makes sure the step has the correct sign.
	 *
	 * @param _start
	 *            Object
	 * @param _stop
	 *            Object
	 * @param _step
	 *            Object
	 * @return The correct step value
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object sortArguments(Object _start, Object _stop, Object _step) {

		try {
			_start = listWrapArray(_start);
			_stop = listWrapArray(_stop);
			_step = listWrapArray(_step);

			if (_start instanceof List) {
				// start, top and step must be of the same size

				int size = ((List) _start).size();
				int stosize = ((List) _stop).size();
				int stesize = ((List) _step).size();

				if (!((size == stosize) && (stosize == stesize))) {
					throw new IllegalArgumentException("start, stop and step need to be of same length");
				}

				for (int i = 0; i < size; i++) {
					Object startElement = ((List) _start).get(i);
					Object stopElement = ((List) _stop).get(i);
					Object stepElement = ((List) _step).get(i);

					Double start = Double.valueOf(startElement.toString());
					Double stop = Double.valueOf(stopElement.toString());
					Double step = Double.valueOf(stepElement.toString());

					step = sortArgs(start, stop, step);
					((List) _step).set(i, step);
				}

				return _step;
			}

			// otherwise assume these are single numbers
			// only can do this if we can create doubles
			final double start = Double.parseDouble(_start.toString());
			final double stop = Double.parseDouble(_stop.toString());
			final double step = Double.parseDouble(_step.toString());

			return sortArgs(start, stop, step);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("start, stop and step need to be numeric values", nfe);
		}
	}

	protected void callAtCommandFailureHooks() {
		for (Scannable scannable : this.allScannables) {
			try {
				scannable.atCommandFailure();
			} catch (DeviceException e) {
				String message = "Catching " + e.getClass().getSimpleName() + " during call of " + getName()
						+ ".atCommandFailure() hook:";
				logger.error(message, e);
				getTerminalPrinter().print(message);
			}
		}
		for (Scannable scannable : this.allDetectors) {
			try {
				scannable.atCommandFailure();
			} catch (DeviceException e) {
				String message = "Catching " + e.getClass().getSimpleName() + " during call of " + getName()
						+ ".atCommandFailure() hook:";
				logger.error(message, e);
				getTerminalPrinter().print(message);
			}
		}
	}

	/**
	 * This should be called at each node of the scan. The collectData method is called for all detectors in the
	 * DetectorBase.ActiveDetectors static arraylist. Throws two types of errors as scans may want to handle these
	 * differently.
	 * <p>
	 * NOTE: Used only by a few scans, not ScanBase or ConcurrentScan
	 * @throws Exception
	 */
	protected void collectData() throws Exception {

		checkThreadInterrupted();
		waitIfPaused();
		if (isFinishEarlyRequested()){
			return;
		}

		// collect data
		for (Detector detector : allDetectors) {
			if (callCollectDataOnDetectors) {
				detector.collectData();
			}
		}

		checkThreadInterrupted();

		// check that all detectors have completed data collection
		for (Detector detector : allDetectors) {
			detector.waitWhileBusy();
		}

		readDevicesAndPublishScanDataPoint();

		sendScanEvent(ScanEvent.EventType.UPDATED);

		checkThreadInterrupted();
	}

	protected void setScanIdentifierInScanDataPoint(IScanDataPoint point) {
		//the scanIdentifier returned by getDataWriter().getCurrentScanIdentifier() should match this.scanNumber
		if(LocalProperties.isScanSetsScanNumber() ){
			//the scan number is setup in the outermost scan
			point.setScanIdentifier(getOuterMostScan().getScanNumber());
		} else {
			//otherwise leave to the first datawriter to set the scanIdentifer as it determines the scan number
			point.setScanIdentifier(getDataWriter().getCurrentScanIdentifier());
			//TODO only do this if not already set
		}
	}

	/**
	 * Samples the position of Scannables (via getPosition()), readouts detectors (via readout) and creates a ScanDataPoint
	 * @throws Exception
	 */
	protected void readDevicesAndPublishScanDataPoint() throws Exception {
		// now can collate the data by creating a DataPoint
		waitIfPaused();

		final ScanDataPoint point = createScanDataPoint();
		readoutDetectorsAndPublish(point);
	}

	protected ScanDataPoint createScanDataPoint() throws DeviceException {
		final ScanDataPoint point = new ScanDataPoint();
		point.setUniqueName(name);
		point.setCurrentFilename(getDataWriter().getCurrentFileName());
		point.setHasChild(isChild());
		point.setNumberOfChildScans(getNumberOfChildScans());
		point.setStepIds(getStepIds());
		point.setScanPlotSettings(getScanPlotSettings());
		point.setScanDimensions(getDimensions());
		point.setCurrentPointNumber(currentPointCount);
		point.setNumberOfPoints(getTotalNumberOfPoints());
		point.setInstrument(instrument);
		point.setCommand(command);
		setScanIdentifierInScanDataPoint(point);

		for (Scannable scannable : allScannables) {
			if (scannable.getOutputFormat().length == 0) {
				handleZeroInputExtraNameDevice(scannable);
			} else {
				point.addScannable(scannable);
			}
		}

		for (Detector scannable : allDetectors) {
			point.addDetector(scannable);
		}

		try {
			populateScannablePositions(point);
		} catch (Exception e) {
			throw wrappedException(e);
		}
		return point;
	}

	/**
	 * Readout detectors into ScanDataPoint and add to pipeline for possible completion and publishing.
	 * @param point
	 * @throws Exception
	 */
	protected void readoutDetectorsAndPublish(final ScanDataPoint point) throws Exception {
		try {
			populateDetectorData(point);
		} catch (Exception e) {
			throw wrappedException(e);
		}
		scanDataPointPipeline.put(point);
	}


	/**
	 * Blocks while detectors are readout and point is added to pipeline (for the previous point).
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unused")
	public void waitForDetectorReadoutAndPublishCompletion() throws InterruptedException, ExecutionException {
		// Do nothing as readoutDetectorsAndPublish blocks until complete.
	}

	protected void cancelReadoutAndPublishCompletion () {
		// Do nothing as readoutDetectorsAndPublish blocks until complete.
	}

	protected static DeviceException wrappedException(Throwable e) {
		String message = (e instanceof PyException) ? e.toString() : e.getMessage();
		if (message == null) {
			message = e.getClass().getSimpleName();
		}
		return new DeviceException(message , e);
	}

	static void populateScannablePositions(IScanDataPoint point) throws DeviceException {
		for (Scannable scannable : point.getScannables()) {
			Object position;
			if (scannable instanceof PositionCallableProvider) {
				Callable<?> positionCallable = ((PositionCallableProvider<?>) scannable).getPositionCallable();
				position = positionCallable;
			} else {
				position = scannable.getPosition();
			}
			point.addScannablePosition(position, scannable.getOutputFormat());
		}
	}

	static void populateDetectorData(IScanDataPoint point) throws DeviceException, InterruptedException {
		for (Detector detector : point.getDetectors()) {
			if (Thread.interrupted()) {
				throw new InterruptedException(); // in case a device will ignore or has ignored an interrupt request
			}
			Object data;
			if (detector instanceof PositionCallableProvider) {
				Callable<?> positionCallable = ((PositionCallableProvider<?>) detector).getPositionCallable();
				data = positionCallable;
			} else {
				data = detector.readout();
			}
			point.addDetectorData(data, ScannableUtils.getExtraNamesFormats(detector));
		}
	}

	protected void createScanDataPointPipeline() throws Exception {
		DataWriter dataWriter = (manuallySetDataWriter == null) ? DefaultDataWriterFactory
				.createDataWriterFromFactory() : manuallySetDataWriter;
		createScanDataPointPipeline(dataWriter);
	}

	protected void createScanDataPointPipeline(DataWriter dataWriter) {

		float estimatedPointsToComputeSimultaneousely;
		if ((getPositionCallableThreadPoolSize() == 0) || (numberOfScannablesThatCanProvidePositionCallables() == 0)) {
			estimatedPointsToComputeSimultaneousely = 0;
		} else {
			estimatedPointsToComputeSimultaneousely = (float) getPositionCallableThreadPoolSize()
					/ (float) numberOfScannablesThatCanProvidePositionCallables();
		}
		logger.info("Creating MultithreadedScanDataPointPipeline which can hold {} points before blocking"
						+ ", and that will on average process {} points simultaneously using {} threads.",
				getScanDataPointQueueLength(), estimatedPointsToComputeSimultaneousely,
				getPositionCallableThreadPoolSize());

		scanDataPointPipeline = new MultithreadedScanDataPointPipeline(
				new ScanDataPointPublisher(dataWriter, this, point -> sendJsonScanMessage(EventType.UPDATED, point)),
				getPositionCallableThreadPoolSize(), getScanDataPointQueueLength(), getName());
	}

	@Override
	public abstract void doCollection() throws Exception;

	/**
	 * This should be called by all scans when they have finished, including when an exception has been raised.
	 *
	 * @throws DeviceException
	 */
	@SuppressWarnings("unused")
	protected void endScan() throws DeviceException, InterruptedException {

		// if the interrupt was set
		try{
			if (getStatus().isAborting()) {
				// stop all scannables
				try {
					logger.info("ScanBase stopping {} Scannables involved in interupted Scan", allScannables.size());
					for (Scannable scannable : allScannables) {
						scannable.stop();
					}
					logger.info("ScanBase stopping {} Detectors involved in interupted Scan", allDetectors.size());
					for (Scannable scannable : allDetectors) {
						scannable.stop();
					}
				} finally {
					// disengage with the data handler, in case this scan is
					// restarted
					shutdownScandataPipeline(false);
				}

			} else { // NOTE: Code will come through here even if there has been an exception in the run method.
				if (getChild() == null) {
					// wait for the last point to readout
					try {
						waitForDetectorReadoutAndPublishCompletion();
					} catch (Exception e) {
						throw new DeviceException(e);
					}
					callScannablesAtScanLineEnd();
				}

				// if a standalone scan, or the top-level scan in a nest of scans
				if (!isChild() ) { // FIXME: Move all !isChild() logic up into runScan
					if (LocalProperties.check("gda.scan.endscan.neworder", true)) {
						// a work around for GDA-6083
						shutdownScandataPipeline(true);
						callScannablesAtScanEnd();
						callDetectorsEndCollection();
					} else { //the original designed order
						callScannablesAtScanEnd();
						callDetectorsEndCollection();
						shutdownScandataPipeline(true);
					}
					signalScanComplete();

				}
			}
			if (!isChild()) {  // FIXME: Move all !isChild() logic up into runScan

				// See if we want to kick-off an end-of-scan process
				String endOfScanName = LocalProperties.get("gda.scan.executeAtEnd");
				if (endOfScanName != null) {
					final String command = endOfScanName + " " + getDataWriter().getCurrentFileName();
					logger.info("running gda.scan.executeAtEnd {}", command);

					final String[] commands = command.split(" ");
					Async.execute(() -> {
							logger.debug("Running command (scan end) - '{}'", command);
							OSCommandRunner os = new OSCommandRunner(commands, true, null, null);
							os.logOutput();
						});
				}
			}
		} catch( DeviceException th){
			/*
			 * If any of the above throws an exception such as an InterruptedException due
			 * to the thread being interrupted as a result of the user requesting an abort
			 * due to the position providers hanging then we need to ensure the pipeline is closed down.
			 */
			if( !(th instanceof RedoScanLineThrowable)){
				shutdownScandataPipeline(false);
			}
			throw th;
		}
	}

	protected void signalScanStarted() {
		sendScanEvent(ScanEvent.EventType.STARTED);
	}

	protected void signalScanComplete() {
		try {
			logger.info("Scan '{}' complete: {}", getName(), getDataWriter().getCurrentFileName());
		} catch (IllegalStateException e) {
			logger.info("Scan '{}' complete", getName());

		}

		getTerminalPrinter().print("Scan complete.");
		if (LocalProperties.check(GDA_SCANBASE_PRINT_TIMESTAMP_TO_TERMINAL)) {
			java.util.Date date= new java.util.Date();
			getTerminalPrinter().print("=== Scan ended at "+new Timestamp(date.getTime()).toString()+" ===");
		}
		sendScanEvent(ScanEvent.EventType.FINISHED);
		// Don't need !isChild() check here as this method is only called for non child scans
		sendJsonScanMessage(EventType.FINISHED, getScanInformation().getNumberOfPoints() - 1);
	}

	protected void sendScanEvent(ScanEvent.EventType reason){
		getJythonServerNotifer().notifyServer(this, new ScanEvent(reason, getScanInformation(),getStatus(),currentPointCount));
	}

	/**
	 * <p>
	 * Send scan status updates. Note that these Json messages are decoupled from the scanning event system
	 * which is routed via the Jython server (e.g. sendScanEvent).
	 * <p>
	 * The primary purpose of the Json events is to allow analysis/visualisation software to be notified
	 * when new data is available to read. For this reason the update messages are required to be driven
	 * by the {@link ScanDataPointPublisher}, since data writing is asynchronous to the main scan control
	 * loop.
	 * <p>
	 * <b>Note:</b> It is important to not use this object's data variables here as this is called async to the
	 * scan loop (the exception is procReq which is initialised at start of scan).
	 */
	private void sendJsonScanMessage(EventType reason, int currentPoint) {
		// Convert between status enums
		final ScanMessage.ScanStatus status;
		switch (reason) {
		case STARTED:
			status = ScanMessage.ScanStatus.STARTED;
			break;
		case UPDATED:
			if (getStatus().isAborting()) {
				status = ScanMessage.ScanStatus.ABORTED;
			} else {
				status = ScanMessage.ScanStatus.UPDATED;
			}
			break;
		case FINISHED:
			status = ScanMessage.ScanStatus.FINISHED;
			break;
		default:
			throw new IllegalArgumentException("Unreconized EventType: " + reason);
		}
		final ScanInformation info = getScanInformation();

		//In some cases (unit tests) the visit directory may not be set,
		//this field is not critical in the scan message so we should not fail on it
		String visit = null;

		try {
			visit = InterfaceProvider.getPathConstructor().getVisitDirectory();
		} catch (IllegalArgumentException e) {
			logger.warn("Visit directory not accessible from PathConstructor");
		}

		// Build the message object
		ScanMessage message = new ScanMessage(status,
				info.getFilename(),
				visit,
				getSwmrStatus(),
				info.getScanNumber(),
				info.getDimensions(),
				Arrays.asList(info.getScannableNames()),
				Arrays.asList(info.getDetectorNames()),
				(100.0 * (currentPoint + 1)) / info.getNumberOfPoints(),// Progress in %
				procReq);

		// If the optional is missing probably running in a unit test
		Optional<MessagingService> optionalJms = GDACoreActivator.getService(MessagingService.class);
		optionalJms.ifPresent(jms -> jms.sendMessage(message));
	}

	private SwmrStatus getSwmrStatus() {
		if (isDataWriterAvaliable()) {
			final DataWriter dataWriter = getDataWriter();
			if (dataWriter instanceof INexusDataWriter) {
				return ((INexusDataWriter) dataWriter).getSwmrStatus();
			}
		}

		return SwmrStatus.DISABLED;
	}

	@Override
	public ScanInformation getScanInformation() {
		ScanInformationBuilder currentInfo = new ScanInformationBuilder().dimensions(getDimensions());
		// might not be defined at start of scan
		if (scanDataPointPipeline != null || manuallySetDataWriter != null) {
			currentInfo
				.scanNumber(scanNumber)
				.filename(getDataWriter().getCurrentFileName());
		}
		return currentInfo.instrument(instrument)
				.numberOfPoints(getTotalNumberOfPoints()) // TODO is this correct??
				.scannableNames(ScannableUtils.getScannableNames(getScannables()))
				.detectorNames(ScannableUtils.getScannableNames(getDetectors()))
				.build();
	}

	protected void shutdownScandataPipeline(boolean waitForProcessingCompletion) throws DeviceException {
		// shutdown the ScanDataPointPipeline (will close DataWriter)
		try {
			if (scanDataPointPipeline != null) {
				scanDataPointPipeline.shutdown(waitForProcessingCompletion);
				//note we cannot set scanDataPointPipeline to null as
				//code using it to get the datawriter after scan completion
			}

		} catch (DeviceException e) {
			setStatus(ScanStatus.TIDYING_UP_AFTER_FAILURE);
			throw e;
		} catch (Exception e) {
			setStatus(ScanStatus.TIDYING_UP_AFTER_FAILURE);
			throw new DeviceException( "Error seen shutting down scan pipeline", e);
		}
	}

	protected void callDetectorsEndCollection() throws DeviceException {
		// tell detectors that collection is over
		for (Detector detector : allDetectors) {
			try {
				detector.endCollection();
			} catch (DeviceException ex) {
				logger.error("Error ending collection on {}", detector, ex);
				throw ex;
			}
		}
	}

	protected void callScannablesAtScanEnd() throws DeviceException {
		// call the atScanGroupEnd method of all the scannables
		for (Scannable scannable : this.allScannables) {
			scannable.atScanEnd();
		}
		for (Scannable scannable : this.allDetectors) {
			scannable.atScanEnd();
		}
	}

	protected void callScannablesAtScanLineEnd() throws DeviceException {
		for (Scannable scannable : this.allScannables) {
			scannable.atScanLineEnd();
		}

		for (Scannable scannable : this.allDetectors) {
			scannable.atScanLineEnd();
		}
	}

	protected String generateRandomName() {
		return UUID.randomUUID().toString();
	}

	@Override
	public Scan getChild() {
		return child;
	}



	/**
	 * Gets the reference to the dataHandler object which this scan uses.
	 *
	 * @return DataWriter
	 */
	@Override
	public DataWriter getDataWriter() {
		if (scanDataPointPipeline == null) {
			if (manuallySetDataWriter == null) {
				throw new IllegalStateException(
						"Could not get datawriter from data pipeline as there is no pipeline or "
								+ "manually set datawriter");
			}
			return manuallySetDataWriter;
		}
		return scanDataPointPipeline.getDataWriter();
	}

	private boolean isDataWriterAvaliable() {
		return scanDataPointPipeline != null || manuallySetDataWriter != null;
	}

	@Override
	public Vector<Detector> getDetectors() {
		return this.allDetectors;
	}

	/**
	 * default implementation. Classes that derive from ScanBase which want to support the reporting of scan dimensions
	 * -@see getDimensions need to override this method
	 *
	 * @see ConcurrentScan
	 * @return the number of points of this scan object - the whole scan execution can be a hierarchy of parent scan
	 *         objects and layers of child scan objects
	 */
	@Override
	public int getDimension() {
		return -1;
	}

	/**
	 * @return the dimensions of the hierarchy of scan and child scans that together constitute an individual scan
	 *         execution For a 1d scan of 10 points the return value is new int[]{10} For a 2d scan of 10 x 20 points
	 *         the return value is new int[]{10,20}
	 */
	// if one of the child scans does not support the reporting of scan dimensions then simply return
	// as if a 1d scan
	protected int[] getDimensions() {

		// ContiguousScan scans should appear one dimensional and have their own interface for working out the dimension
		// TODO: is it really appropriate for ScanBase to have to be aware of ContiguousScans?
		Scan outerMostScan = getOuterMostScan();
		if( outerMostScan != null && outerMostScan instanceof ContiguousScan)
			return new int[]{ ((ContiguousScan)outerMostScan).getNumberOfContiguousPoints()};
		Vector<Integer> dim = new Vector<>();
		Scan scan = outerMostScan;
		while (scan != null) {
			int numberPoints = scan.getDimension();
			if (numberPoints == -1) {
				return new int[] { -1 }; // escape if child does not support this concept
			}
			dim.add(numberPoints);
			scan = scan.getChild();
		}
		int[] dims = new int[dim.size()];
		for (int i = 0; i < dim.size(); i++) {
			dims[i] = dim.get(i);
		}
		return dims;
	}

	Scan getInnerMostScan() {
		Scan scan = this;
		while (scan.getChild() != null) {
			scan = scan.getChild();
		}
		return scan;
	}

	/**
	 * Returns the unique identifier for this scan. Nested (child) scans share the same identifier as their parents.
	 *
	 * @return String
	 */
	@Override
	public String getName() {
		return name;
	}

	public int getNumberOfChildScans() {
		return numberOfChildScans;
	}

	Scan getOuterMostScan() {
		NestableScan scan = this;
		while (scan.getParent() != null) {
			scan = scan.getParent();
		}
		return scan;
	}

	@Override
	public NestableScan getParent() {
		return parent;
	}

	public int getPositionCallableThreadPoolSize() {
		return positionCallableThreadPoolSize;
	}

	@Override
	public ScanDataPointPipeline getScanDataPointPipeline() {
		return scanDataPointPipeline;
	}

	public int getScanDataPointQueueLength() {
		return scanDataPointQueueLength;
	}

	@Override
	public Vector<Scannable> getScannables() {
		return this.allScannables;
	}

	@Override
	public ScanPlotSettings getScanPlotSettings() {
		Scan scan = getInnerMostScan();
		return (scan == this) ? scanPlotSettings : scan.getScanPlotSettings();
	}

	@Override
	public IScanStepId getStepId() {
		return stepId;
	}

	protected List<IScanStepId> getStepIds() {
		Vector<IScanStepId> stepsIds = new Vector<>();
		NestableScan scan = this;
		while (scan != null) {
			IScanStepId stepId = scan.getStepId();
			// order is parent->child so insert at the front
			stepsIds.add(0, stepId);
			scan = scan.getParent();
		}
		return stepsIds;
	}

	@Override
	public int getTotalNumberOfPoints() {
		Scan outerMostScan = getOuterMostScan();
		if( outerMostScan != null && outerMostScan instanceof ContiguousScan)
			return ((ContiguousScan)outerMostScan).getNumberOfContiguousPoints();

		return TotalNumberOfPoints;
	}

	@Override
	public boolean isChild() {
		// FIXME: isChild boolean not required. Could we return (parent != null) instead?
		return isChild;
	}

	public boolean isLineScanNeedsDoing() {
		return lineScanNeedsDoing;
	}

	/**
	 * Give the command server the latest data object to fan out to its observers.
	 *
	 * @param data
	 * @deprecated Behaviour now in {@link ScanDataPointPipeline} implementations
	 */
	@Deprecated
	public void notifyServer(Object data) {
		getJythonServerNotifer().notifyServer(this, data);
	}

	/**
	 * A better way to notify the observer which allows users to specify source of the data, not like the one above.
	 *
	 * @param source
	 * @param data
	 * @deprecated Behaviour now in {@link ScanDataPointPipeline} implementations
	 */
	@Deprecated
	public void notifyServer(Object source, Object data) {
		getJythonServerNotifer().notifyServer(source, data);
	}

	public int numberOfScannablesThatCanProvidePositionCallables() {
		int n = 0;
		for (Scannable scn : allScannables) {
			if (scn instanceof PositionCallableProvider) {
				n++;
			}
		}
		for (Detector det : allDetectors) {
			if (det instanceof PositionCallableProvider) {
				n++;
			}
		}

		return n;
	}

	@Override
	public void pause() {
		if (getStatus().possibleFollowUps().contains(ScanStatus.PAUSED)) {
			setStatus(ScanStatus.PAUSED);
		}
	}

	protected void prepareDevicesForCollection() throws Exception {

		// Deliberately do *not* complete scan early if requested, once preparation has begun

		// prepare to collect data
		for (Detector detector : allDetectors) {
			detector.prepareForCollection();
		}

		// then loop through all the Scannables and call their atStart method
		if (!isChild()) {
			callScannablesAtScanStart();
		}
		if (getChild() == null) {

			callScannablesAtScanLineStart();
		}
	}

	protected void callScannablesAtScanStart() throws DeviceException {
		for (Scannable scannable : this.allScannables) {
			scannable.atScanStart();
		}
		for (Scannable scannable : this.allDetectors) {
			scannable.atScanStart();
		}
	}

	protected void callScannablesAtScanLineStart() throws DeviceException {
		for (Scannable scannable : this.allScannables) {
			scannable.atScanLineStart();
		}

		for (Scannable scannable : this.allDetectors) {
			scannable.atScanLineStart();
		}
	}

	/**
	 * This should called by all scans just before they start to collect data. It resets the static variable which the
	 * scan classes use and creates a dataHandler if one has not been created yet.
	 *
	 * @throws Exception
	 */
	@Override
	public void prepareForCollection() throws Exception {
		try {

			prepareScanForCollection();

			prepareDevicesForCollection();
		} catch (Exception e) {
			String message = createMessage(e) + " during prepare for collection";
			logger.info(message);
			getTerminalPrinter().print(message);
			throw e;
		}
	}

	protected void prepareScanForCollection() throws Exception {

		prepareScanNumber();
		prepareStaticVariables();

		// unless it has already been defined, create a new datahandler
		// for this scan
		if (scanDataPointPipeline == null) {
			createScanDataPointPipeline();
		}
		DataWriter dw = getDataWriter();
		dw.configureScanNumber(getScanNumber());
		if (scanNumber < 0) {
			scanNumber = dw.getCurrentScanIdentifier();
		}

		// At this point the DataWriter knows the scan number so can provide info on the file it will write.
		// This allows the update to contain the path information of the data files from the start of the scan
		sendScanEvent(EventType.UPDATED);
		if (!isChild()) {
			sendJsonScanMessage(EventType.STARTED, currentPointCount);
		}
	}


	protected void prepareScanNumber() throws IOException {
		if (getScanNumber() <= 0 && !isChild()) {
			if (LocalProperties.isScanSetsScanNumber()) {
				NumTracker runNumber = new NumTracker("scanbase_numtracker");
				// Allow tests to set the scanNumber
				int int1 = LocalProperties.getInt(GDA_SCANBASE_FIRST_SCAN_NUMBER_FOR_TEST, -1);
				if (int1 != -1) {
					runNumber.setFileNumber(int1 - 1L);
				}
				scanNumber = runNumber.incrementNumber();
			}
		}  else if (isChild()){
			// assume that the outermost scan in a nest of scans would have its scan number defined before
			// prepareScanForCollection() called on any of the inner scans
			scanNumber = getOuterMostScan().getScanNumber();
		}
	}

	protected synchronized void prepareStaticVariables() {
		getCurrentScanInformationHolder().setCurrentScan(this);
	}

	private void removeDuplicateScannables() {
		Vector<Scannable> newAllScannables = new Vector<>();

		for (Scannable thisScannable : allScannables) {
			if (!newAllScannables.contains(thisScannable)) {
				newAllScannables.add(thisScannable);
			}
		}

		allScannables = newAllScannables;
	}

	/**
	 * Order the allScannables vector using the 'level' attribute.
	 */
	protected void reorderScannables() {
		Collections.sort(allScannables, Comparator.comparingInt(Scannable::getLevel));
	}

	@Override
	public void resume() {
		if (getStatus() == ScanStatus.PAUSED){
			setStatus(ScanStatus.RUNNING);
		}
	}

	@Override
	public final void run() throws Exception {
		Exception exceptionFromMainTryClause = null;
		logger.debug("ScanBase.run() for scan: '{}'", getName());
		do {
			lineScanNeedsDoing = false;
			pointNumberAtLineBeginning = currentPointCount;
			try {
				// validate scannables
				try {
					for (Scannable scannable : getScannables()) {
						ScannableBase.validateScannable(scannable);
					}
				} catch (Exception e) {
					setStatus(ScanStatus.TIDYING_UP_AFTER_FAILURE);
					throw new Exception("Exception while validating Scannables: " + createMessage(e), e);
				}

				// run the child scan, based on innerscanstatus
				try {
					prepareForCollection();
				} catch (InterruptedException e) {
					setStatus(ScanStatus.TIDYING_UP_AFTER_STOP);
					throw new ScanInterruptedException(e.getMessage(),e.getStackTrace());
				} catch (RedoScanLineThrowable e){
					logger.info("Redoing scan line because: {}", e.getMessage());
					logger.trace("Cause of redo exception", e);
					lineScanNeedsDoing = true;
					currentPointCount = pointNumberAtLineBeginning;
					continue;
				} catch (Exception e) {
					setStatus(ScanStatus.TIDYING_UP_AFTER_FAILURE);
					throw new Exception("Exception while preparing for scan collection: " + createMessage(e), e);
				}

				if (getScannables().isEmpty() && getDetectors().isEmpty()) {
					setStatus(ScanStatus.TIDYING_UP_AFTER_FAILURE);
					throw new IllegalStateException("ScanBase: No scannables, detectors or monitors to be scanned!");
				}

				try {
					if (isFinishEarlyRequested()){
						return;
					}
					waitIfPaused();
					doCollection();
				} catch (InterruptedException e) {
					setStatus(ScanStatus.TIDYING_UP_AFTER_STOP);
					// need the correct exception type so wrapping code know its an interrupt
					String message = "Scan aborted on request.";
					logger.info(message);
					exceptionFromMainTryClause = e;
					throw new ScanInterruptedException(message,e.getStackTrace());
				} catch (RedoScanLineThrowable e){
					logger.info("Redoing scan line because: {}", e.getMessage());
					logger.trace("Cause of redo exception", e);
					lineScanNeedsDoing = true;
					currentPointCount = pointNumberAtLineBeginning;
					continue;
				} catch (Exception e) {
					String message = e.getMessage();
					// If the scan was aborted whilst in a waitWhileBusy() the interrupted exception is converted
					// to a device exception, so we use this kludge to signal that it wasn't a failure but a user
					// aborting the scan. Thus preventing major stacktraces in the log file.
					if ("sleep interrupted".equals(message)) {
						// need the correct exception type so wrapping code know its an interrupt
						logger.info("Scan aborted on request. {}", message);
						throw new ScanInterruptedException(message,e.getStackTrace());
					}
					setStatus(ScanStatus.TIDYING_UP_AFTER_FAILURE);
					exceptionFromMainTryClause = e;
					throw new Exception("during scan collection: " + createMessage(e), e);
				}

			} catch (ScanInterruptedException e) {
				throw e;
			} catch (RedoScanLineThrowable e){
				logger.info("Redoing scan line because: {}", e.getMessage());
				logger.trace("Cause of redo exception", e);
				lineScanNeedsDoing = true;
				currentPointCount = pointNumberAtLineBeginning;
				continue;
			} catch (Exception e) {
				InterfaceProvider.getTerminalPrinter().print("====================================================================================================");
				logger.error(createMessage(e) + ": calling atCommandFailure hooks and then interrupting scan.",e);
				report(createMessage(e));

				logger.info("Calling stop() on Scannables and Detectors used in scan, followed by atCommandFailure().");
				cancelReadoutAndPublishCompletion();
				callAtCommandFailureHooks();
				for (Scannable scn : allScannables) {
					logger.info("Stopping {}", scn.getName());
					scn.stop();
				}
				for (Scannable det : allDetectors) {
					logger.info("Stopping {}", det.getName());
					det.stop();
				}

				cancelReadoutAndPublishCompletion();

				logger.info("Ending scan and rethrowing exception");

				// normally this exception would be thrown at the end of the following finally clause.
				//However if the call to endScan() below results in an exception, it will be lost. So save it:
				exceptionFromMainTryClause = e;
				throw e;
			} finally {
				try {
					// TODO: endScan now duplicates some of the exception handling performed above.
					// I do not understand the paths that result inScanBase.interupted being set well enough to
					// change the logic here. RobW
					endScan();
				} catch (DeviceException e) {
					if ((e instanceof RedoScanLineThrowable) && (getChild() == null)) {
						logger.info("Redoing scan line because: {}", e.getMessage());
						logger.trace("Cause of redo exception", e);
						lineScanNeedsDoing = true;
						currentPointCount = pointNumberAtLineBeginning;
					} else {
						setStatus(ScanStatus.TIDYING_UP_AFTER_FAILURE);
						logger.error(createMessage(e) + " Calling atCommandFailure hooks.",e);
						callAtCommandFailureHooks();
						if (exceptionFromMainTryClause != null) {
							logger.error("There has been a problem with the scan and while ending the scan a second problem occured");
							logger.error("This second exception is logged here, and the original exception that stopped the scan will be thrown instead of it");
							logger.error("", e);
							logger.info("Throwing original exception that stopped scan");
							throw exceptionFromMainTryClause;
						}
						throw e;
					}
				}
			}

		} while (lineScanNeedsDoing && !isFinishEarlyRequested());
	}

	private void report(String msg) {
		InterfaceProvider.getTerminalPrinter().print(msg);
		logger.info(msg);
	}


	@Override
	public void runScan() throws InterruptedException, Exception {

		JythonStatus currentStatus = getScanStatusHolder().getScanStatus();
		if (currentStatus != JythonStatus.IDLE) {
			throw new Exception("Scan not started as there is already a scan running (could be paused).");
		}
		signalScanStarted();
		setStatus(ScanStatus.RUNNING);
		if (LocalProperties.check(GDA_SCANBASE_PRINT_TIMESTAMP_TO_TERMINAL)) {
			java.util.Date date= new java.util.Date();
			getTerminalPrinter().print("=== Scan started at "+new Timestamp(date.getTime()).toString()+" ===");
		}

		try {
			// check if a scan or script is currently running.
			if (this.isChild()) {
				return;
			}
			// Note: some subclasses override the run method so its code cannot
			// be simply pulled into this method
			run();
		} finally {

			switch (getStatus()) {
			case PAUSED: // if paused here then must have been paused too late to have any effect e.g. fly/continuous scan or in last point of a step scan
				setStatus(ScanStatus.RUNNING); // set RUNNING status to update UI that we are no longer paused
				// Deliberately fall-through to RUNNING case to handle normal scan completion
				//$FALL-THROUGH$
			case RUNNING:
				setStatus(ScanStatus.COMPLETED_OKAY);
				break;
			case TIDYING_UP_AFTER_FAILURE:
				setStatus(ScanStatus.COMPLETED_AFTER_FAILURE);
				break;
			case TIDYING_UP_AFTER_STOP:
				setStatus(ScanStatus.COMPLETED_AFTER_STOP);
				break;
			case FINISHING_EARLY:
				setStatus(ScanStatus.COMPLETED_EARLY);
				break;
			default:
				throw new AssertionError("Unexpected status at the end of scan:" + getStatus().toString());
			}
		}
	}


	@Override
	public void setChild(Scan child) {
		this.child = child;
	}

	@Override
	public void setDataWriter(DataWriter dataWriter) {
		this.manuallySetDataWriter = dataWriter;
	}

	@Override
	public void setDetectors(Vector<Detector> allDetectors) {
		this.allDetectors = allDetectors;
	}

	@Override
	public void setIsChild(boolean child) {
		this.isChild = child;
		if (child) {
			// TODO it would be better not to have made this in the first place!
			parentComponent = null;  // To keep anyone using it instead
		}
	}

	public void setLineScanNeedsDoing(boolean lineScanNeedsDoing) {
		this.lineScanNeedsDoing = lineScanNeedsDoing;
	}

	public void setNumberOfChildScans(int numberOfChildScans) {
		this.numberOfChildScans = numberOfChildScans;
	}

	@Override
	public void setParent(NestableScan parent) {
		this.parent = parent;
	}

	public void setPositionCallableThreadPoolSize(int positionCallableThreadPoolSize) {
		this.positionCallableThreadPoolSize = positionCallableThreadPoolSize;
	}

	@Override
	public void setScanDataPointPipeline(ScanDataPointPipeline scanDataPointPipeline) {
		this.scanDataPointPipeline = scanDataPointPipeline;
	}

	public void setScanDataPointQueueLength(int scanDataPointQueueLength) {
		this.scanDataPointQueueLength = scanDataPointQueueLength;
	}

	@Override
	public void setScannables(Vector<Scannable> allScannables) {
		this.allScannables = allScannables;
	}

	@Override
	public void setScanPlotSettings(ScanPlotSettings scanPlotSettings) {
		Scan scan = getInnerMostScan();
		if (scan == this) {
			this.scanPlotSettings = scanPlotSettings;
		} else {
			scan.setScanPlotSettings(scanPlotSettings);
		}
	}

	@Override
	public void setStepId(IScanStepId stepId) {
		this.stepId = stepId;
	}

	/**
	 * This should be called by all scans during their constructor. In this method the objects to scan over and the
	 * detectors to use are identified, and the data handlers objects are created and setup.
	 */
	protected synchronized void setUp() {
		// first add to the list of scannables all those items which are
		// in the list of defaults
		Collection<Scannable> defaultScannables = getDefaultScannableProvider().getDefaultScannables();
		for (Scannable scannable : defaultScannables) {
			if (scannable instanceof Detector && !allDetectors.contains(scannable)) {
				this.allDetectors.add((Detector) scannable);
			} else if (!allScannables.contains(scannable)) {
				allScannables.add(scannable);
			}
		}
		// look to see if any of the scannables was a detector
		// and add it to the list of detectors
		/*
		 * A detector may be specified as a scannable in the constructor to ScanBase class. e.g. within a Jython
		 * script we may want to execute a scan passing in a non-default detector as: gda.scan.ConcurrentScan( [
		 * dof, parameters.start.getValue(), parameters.end.getValue(), parameters.step.getValue(), detector
		 * ]).runScan(); Such a detector would be an instance of Scannable and also DetectorAdapter. Such objects
		 * need to be added to the list of detectors to be removed.
		 */
		ArrayList<Scannable> detectorsToRemove = new ArrayList<>();
		for (Scannable scannable : allScannables) {
			if (scannable instanceof Detector) {
				// recast
				Detector det = (Detector) scannable;
				// add the detector to the list of detectors.
				if (!allDetectors.contains(det)) {
					this.allDetectors.add(det);
				}
				detectorsToRemove.add(scannable);
			}
		}

		// detectors are to be treated differently from scannables,
		// so remove anything just added to the list of detectors from
		// the list of scannables
		for (Object detector : detectorsToRemove) {
			allScannables.remove(detector);
		}

		// ensure that there are no duplications in the list of scannables
		removeDuplicateScannables();
		extractProcessingInformation();
	}

	private void extractProcessingInformation() {
		procReq = Stream.of(allScannables, allDetectors)
				.flatMap(List::stream)
				.filter(ProcessingRequestProvider.class::isInstance)
				.map(ProcessingRequestProvider.class::cast)
				.map(ProcessingRequestProvider::getProcessingRequest)
				.flatMap(req -> req.entrySet().stream())
				.collect(toMap(
						Entry::getKey,
						Entry::getValue,
						(a, b) -> Stream.of(a, b).flatMap(Collection::stream).collect(toList())
				));
	}

	/**
	 * True if the scan was asked to complete early
	 */
	public boolean wasScanExplicitlyHalted() {
		return (getStatus() == ScanStatus.COMPLETED_EARLY);
	}

	/**
	 * Returns for example "ErrorType: message"
	 * @param e
	 * @return message
	 */
	private String createMessage(Throwable e) {
		if (e.getMessage() != null) {
			return e.getClass().getSimpleName() + ": " + e.getMessage();
		} else if (e instanceof PyException) {
			return e.getClass().getSimpleName() + ": " + e.toString();
		} else {
			return e.getClass().getSimpleName();
		}
	}

	protected void checkThreadInterrupted() throws InterruptedException {
		if (Thread.interrupted()) {
			InterruptedException interruptedException = new InterruptedException("Scan thread has been interrupted");
			logger.info("Scan thread has been interrupted", interruptedException);
			throw interruptedException;
		}
	}

}

class ParentScanComponent implements ScanParent{

	public static boolean throwExceptionForUnexpectedStateTransition = true;

	private static final Logger logger = LoggerFactory.getLogger(ParentScanComponent.class);

	ScanStatus status;

	/**
	 * When set to true, the scan should complete the current data point, and then exit normally going through the same
	 * code, but skip the remaining data points.
	 */
	private boolean finishEarlyRequested;

	public ParentScanComponent(ScanStatus initialStatus) {
		super();
		this.status = initialStatus;
	}

	public void requestFinishEarly() {
		finishEarlyRequested = true;
		if (this.status.possibleFollowUps().contains(ScanStatus.FINISHING_EARLY)){
			setStatus(ScanStatus.FINISHING_EARLY);
		}
	}

	public boolean isFinishEarlyRequested() {
		return finishEarlyRequested;
	}

	public ScanStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(ScanStatus newStatus) {
		if (this.status.possibleFollowUps().contains(newStatus)) {
			this.status = newStatus;
			// notify Command (Jython) Server that the status has changed
			InterfaceProvider.getJythonServerNotifer().notifyServer(this, this.getStatus());
		} else {
			logger.error("Scan status change from '{}' to '{}' is not expected", this.status.name(),
					newStatus.name());
		}

	}
}
