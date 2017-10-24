/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import static gda.scan.ScanDataPoint.handleZeroInputExtraNameDevice;

import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ContinuousParameters;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.BufferedDetector;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.ScannableUtils;
import gda.jython.InterfaceProvider;

/**
 * Performs a continuous scan using Histogram detectors. NOTE: bypasses the ScanDataPointPipeline mechanism.
 * <p>
 * This extends ConcurrentScan so it can be a child scan of a ConcurrentScan and effectively be another dimension in a
 * multi-dimensional scan.
 * <p>
 * This will not operate any child scans, so must be the lowest dimension in a n-dimensional scan.
 */
public class ContinuousScan extends ConcurrentScanChild {

	private static final Logger logger = LoggerFactory.getLogger(ContinuousScan.class);
	private ContinuouslyScannable qscanAxis;
	private Double start;
	private Double stop;
	private Double time;
	private Integer numberScanpoints;
	private BufferedDetector[] qscanDetectors;
	private Date timeMotionFinished;
	private long timeOfLastUpdatedScanEvent;
	private boolean biDirectional = false;
	private boolean lastCollectionInPositiveDirection = false;

	public ContinuousScan() {
		super();
		setMustBeFinal(true);
	}

	public ContinuousScan(ContinuouslyScannable energyScannable, Double start, Double stop, Integer numberPoints,
			Double time, BufferedDetector[] detectors) {
		setMustBeFinal(true);
		allScannables.add(energyScannable);
		double step = (stop - start) / (numberPoints - 1);
		ImplicitScanObject firstScanObject = new ImplicitScanObject(energyScannable, start, stop, step);
		firstScanObject.calculateScanPoints();
		allScanObjects.add(firstScanObject);
		qscanAxis = energyScannable;
		this.start = start;
		this.stop = stop;
		this.numberScanpoints = numberPoints;
		this.time = time;
		qscanDetectors = detectors;
		for (Detector det : detectors)
			allDetectors.add(det);
		super.setUp();
	}

	@Override
	public boolean isReadoutConcurrent() {
		return false; // should be false even if enabled for beamline
	}

	@Override
	public int getDimension() {
		return numberScanpoints;
	}

	/**
	 * This method is used by the scan base class when preparing for the start of the scan.
	 * <p>
	 * This needs to be overriden by this class to prevent operation of the continuous scannable, but still enable other
	 * Scannables in the scan to be setup for the scan. This is especially useful for when a continuous scan is part of
	 * a multi-dimensional scan.
	 */
	@Override
	protected ScanObject isScannableToBeMoved(Scannable scannable) {
		if (scannable == qscanAxis)
			return null;
		return super.isScannableToBeMoved(scannable);
	}

	@Override
	public void doCollection() throws Exception {
		timeMotionFinished = null;
		checkThreadInterrupted();
		acquirePoint(true, false);
		ContinuousParameters params = createContinuousParameters();
		qscanAxis.setContinuousParameters(params);

		// Prepare the hardware for the continuous move and revise the number of scans points to the actual number which
		// the hardware will do.
		qscanAxis.prepareForContinuousMove();
		checkThreadInterrupted();
		numberScanpoints = Math.abs(qscanAxis.getNumberOfDataPoints());

		params.setNumberDataPoints(numberScanpoints);
		super.setTotalNumberOfPoints(numberScanpoints);

		// prep the detectors
		for (BufferedDetector detector : qscanDetectors) {
			detector.clearMemory();
			detector.setContinuousParameters(params);
			detector.setContinuousMode(true);
			checkThreadInterrupted();
		}

		// for performance, see how many frames to read at any one time
		int maxFrameRead = getMaxFrameRead();

		// wait for the scannable to lined up the move to stop in another thread
		qscanAxis.waitWhileBusy();
		if (!isChild())
			currentPointCount = -1;
		qscanAxis.performContinuousMove();

		// now readout and convert each point to a regular sdp to give it to the datahandler
		int highestFrameNumberRead = -1;

		try {
			while (highestFrameNumberRead < numberScanpoints - 1) {
				checkThreadInterrupted();
				if (isFinishEarlyRequested()) {
					// stop motion now and do one last readout
					qscanAxis.stop();
				}
				checkForMotionTimeout();
				// sleep for a second. For what reason?
				Thread.sleep(200);
				// get lowest number of frames from all detectors
				int frameNumberReached = findNumberOfFramesAvailable(highestFrameNumberRead);
				// do not collect more than 20 frames at any one time
				if (frameNumberReached - highestFrameNumberRead > maxFrameRead) {
					frameNumberReached = highestFrameNumberRead + maxFrameRead;
				}
				// get data from detectors for that frame and create an sdp and send it out
				if (frameNumberReached > -1 && frameNumberReached > highestFrameNumberRead) {
					logger.debug("about to createDataPoints " + (highestFrameNumberRead + 1) + " " + frameNumberReached
							+ " " + qscanAxis.isBusy());
					createDataPoints(highestFrameNumberRead + 1, frameNumberReached);
				}

				highestFrameNumberRead = frameNumberReached;
				logger.debug("number of frames completed:" + new Integer(frameNumberReached + 1));
				if (isFinishEarlyRequested()) {
					qscanAxis.stop();
					return;
				}
			}

		} catch (ContinuousScanTimeoutException e) {
			// scan has been aborted, so stop the motion and let the scan write out the rest of the data point which
			// have been collected so far
			qscanAxis.stop();
			qscanAxis.atCommandFailure();
			logger.error("ContinuousScanTimeoutException so finish early without throwing an exception");
			// return normally
		} catch (InterruptedException e) {
			// scan has been aborted, so stop the motion and let the scan write out the rest of the data point which
			// have been collected so far
			qscanAxis.stop();
			qscanAxis.atCommandFailure();
			throw e;
		} catch (Exception e) {
			// scan has been aborted, so stop the motion and let the scan write out the rest of the data point which
			// have been collected so far
			logger.error("ContinuousScan aborting for an unexpected error!",e);
			qscanAxis.stop();
			qscanAxis.atCommandFailure();
			throw e;
		}
	}

	protected ContinuousParameters createContinuousParameters() {
		ContinuousParameters params = new ContinuousParameters();
		params.setStartPosition(start);
		params.setEndPosition(stop);

		if (biDirectional && lastCollectionInPositiveDirection){
			// shift by one pixel when going in the reverse direction (seen to be necessary on I18)
			double stepSize = (stop- start) / numberScanpoints;
			params.setStartPosition(stop - stepSize);
			params.setEndPosition(start - stepSize);
			lastCollectionInPositiveDirection = false;
		} else {
			lastCollectionInPositiveDirection = true;
		}

		params.setNumberDataPoints(numberScanpoints);
		params.setTotalTime(time);
		params.setContinuouslyScannableName(qscanAxis.getName());
		return params;
	}

	private int findNumberOfFramesAvailable(int highestFrameNumberRead) throws InterruptedException {
		int frameNumberReached = highestFrameNumberRead;
		int framesReachedArray[] = new int[qscanDetectors.length];
		fillArray(framesReachedArray, highestFrameNumberRead);
		for (int k = 0; k < qscanDetectors.length; k++) {
			try {
				int thisNumberFrames = qscanDetectors[k].getNumberFrames();
				if (thisNumberFrames - 1 > framesReachedArray[k]) {
					framesReachedArray[k] = thisNumberFrames - 1;
				}
				logger.debug("Frame number for  " + qscanDetectors[k].getName() + " " + framesReachedArray[k]);
			} catch (DeviceException e) {
				checkThreadInterrupted();
				logger.warn("Problem getting number of frames from " + qscanDetectors[k].getName());
			}
		}
		frameNumberReached = findLowest(framesReachedArray);
		logger.debug("the lowest frame of all the detectors is " + frameNumberReached);
		return frameNumberReached;
	}

	private void checkForMotionTimeout() throws ContinuousScanTimeoutException, DeviceException {
		if (qscanAxis.isBusy()) return;

		Date now = new Date();
		if (timeMotionFinished == null){
			logger.info("Motion has finished, now waiting for detectors to readout");
			timeMotionFinished = now;
		} else if (now.getTime() - timeMotionFinished.getTime() > 30000){
			// timeout after 5 minutes
			String msg = "Timeout waiting for detectors to readout after continuous scan motion has completed";
			logger.error(msg);
			throw new ContinuousScanTimeoutException(msg);
		}

	}

	private int findLowest(int[] framesReachedArray) {
		int lowest = framesReachedArray[0];
		for (int i = 0; i < framesReachedArray.length; i++) {
			if (framesReachedArray[i] < lowest)
				lowest = framesReachedArray[i];
		}
		return lowest;
	}

	private void fillArray(int[] framesReachedArray, int highestFrameNumberRead) {
		for (int i = 0; i < framesReachedArray.length; i++)
			framesReachedArray[i] = highestFrameNumberRead;
	}

	private int getMaxFrameRead() throws DeviceException {
		int smallestFrameLimit = Integer.MAX_VALUE;
		for (BufferedDetector detector : qscanDetectors) {
			int thisDetMax = detector.maximumReadFrames();
			if (thisDetMax < smallestFrameLimit)
				smallestFrameLimit = thisDetMax;
		}
		return smallestFrameLimit;
	}

	@Override
	protected void endScan() throws DeviceException, InterruptedException {
		try {
			qscanAxis.continuousMoveComplete();
			for (BufferedDetector detector : qscanDetectors) {
				detector.setContinuousMode(false);
			}
		} finally {
			super.endScan();
		}
	}

	@Override
	public String getCommand() {
		if (command == null || command.equals("")) {
			command = qscanAxis.getName() + " " + start + " " + stop + " " + numberScanpoints + " " + time;
			for (BufferedDetector detector : qscanDetectors)
				command += " " + detector.getName();
		}
		return command;
	}

	@Override
	public int getTotalNumberOfPoints() {
		if (!isChild())
			return numberScanpoints;
		return getParent().getTotalNumberOfPoints();
	}

	/**
	 * @param lowFrame
	 *            - where 0 is the first frame
	 * @param highFrame
	 *            - where number scan points -1 is the last frame
	 * @throws Exception
	 */
	private void createDataPoints(int lowFrame, int highFrame) throws Exception {
		// readout the correct frame from the detectors
		HashMap<String, Object[]> detData = new HashMap<String, Object[]>();
		logger.info("reading data from detectors from frames " + lowFrame + " to " + highFrame);
		try {
			for (BufferedDetector detector : qscanDetectors) {
				checkThreadInterrupted();
				Object[] data = detector.readFrames(lowFrame, highFrame);
				detData.put(detector.getName(), data);
			}
		} catch (DeviceException e1) {
			throw new DeviceException("Exception while reading out frames " + lowFrame + " to " + highFrame, e1);
		}
		logger.info("data read successfully");

		// thisFrame <= highFrame. this was thisFrame < highFrame which caused each frame to lose a point at the end
		for (int thisFrame = lowFrame; thisFrame <= highFrame; thisFrame++) {
			checkThreadInterrupted();
			currentPointCount++;
			this.stepId = new ScanStepId(qscanAxis.getName(), currentPointCount);
			ScanDataPoint thisPoint = new ScanDataPoint();
			thisPoint.setUniqueName(name);
			thisPoint.setCurrentFilename(getDataWriter().getCurrentFileName());
			thisPoint.setStepIds(getStepIds());
			thisPoint.setScanPlotSettings(getScanPlotSettings());
			thisPoint.setScanDimensions(getDimensions());
			thisPoint.setNumberOfPoints(numberScanpoints);

			// add the scannables. For the qscanAxis scannable calculate the position.
			double stepSize = (stop - start) / (numberScanpoints - 1);

			for (Scannable scannable : allScannables) {
				if (scannable.equals(qscanAxis)) {
					thisPoint.addScannable(qscanAxis);
					try {
						thisPoint.addScannablePosition(qscanAxis.calculateEnergy(thisFrame),
								qscanAxis.getOutputFormat());
					} catch (DeviceException e) {
						thisPoint.addScannablePosition(start + (thisFrame - 1) * stepSize, qscanAxis.getOutputFormat());
					}
				} else {
					if (scannable.getOutputFormat().length == 0)
						handleZeroInputExtraNameDevice(scannable);
					else {
						thisPoint.addScannable(scannable);
						thisPoint.addScannablePosition(scannable.getPosition(), scannable.getOutputFormat());
					}
				}

			}
			// readout the correct frame from the detectors
			for (BufferedDetector detector : qscanDetectors) {
				Object[] dataArray = detData.get(detector.getName());
				Object data = dataArray[thisFrame - lowFrame];
				if (data != null) {
					thisPoint.addDetector(detector);
					thisPoint.addDetectorData(data, ScannableUtils.getExtraNamesFormats(detector));
				}
			}

			// Set some parameters in the data point.
			// (This is implemented as setters at the moment, as I didn't want to risk changing the constructor
			// statement above and risk breaking the scanning system!)
			thisPoint.setCurrentPointNumber(this.currentPointCount);
			thisPoint.setInstrument(instrument);
			thisPoint.setCommand(getCommand());
			setScanIdentifierInScanDataPoint(thisPoint);

			// then write data to data handler
			getDataWriter().addData(thisPoint);

			// update the filename (if this was the first data point and so
			// filename would never be defined until first data added
			thisPoint.setCurrentFilename(getDataWriter().getCurrentFileName());

			// then notify IObservers of this scan (e.g. GUI panels)
			InterfaceProvider.getJythonServerNotifer().notifyServer(this,thisPoint); // for the CommandQueue
			notifyScanEvent();
			//FIXME GDA should not need two messages sent out here. This needs resolving. The UI should also have to resolve its own updating.
		}
	}

	private void notifyScanEvent() {
		// as this can happen very frequently for ContinuousScans, only notify every second
		long now = new Date().getTime();
		if (now - timeOfLastUpdatedScanEvent  > 1000) {
			sendScanEvent(ScanEvent.EventType.UPDATED); // for the ApplicationActionToolBar
			timeOfLastUpdatedScanEvent = now;
		}
	}

	public boolean isBiDirectional() {
		return biDirectional;
	}

	/**
	 * When true, when this scan is used as the innermost dimension of a multidimensional scan (nest of scans), then
	 * each repetition will be in the opposite direction.
	 * <p>
	 * NB: as the data will be coming out in the reversed direction for every other scan then a custom DataWriter will
	 * need to be used to perform the corrections to the data.
	 *
	 * TODO move the functionality in XasAsciiNexusDatapointCompletingDataWriter in the exafs.datawriter plugin to core.
	 *
	 * @param biDirectional
	 */
	public void setBiDirectional(boolean biDirectional) {
		this.biDirectional = biDirectional;
	}

	public ContinuouslyScannable getScanAxis() {
		return qscanAxis;
	}

	public BufferedDetector[] getScanDetectors() {
		return qscanDetectors;
	}
}



/**
 *  For timeout while waiting for detectors to see all their expected data points.
 */
class ContinuousScanTimeoutException extends Exception {

	public ContinuousScanTimeoutException(String msg) {
		super(msg);
	}

}
