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

package uk.ac.gda.devices.odin;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import gda.epics.ReadOnlyPV;
import gda.factory.FactoryException;

public class OdinDetectorControllerEpics extends DeviceBase implements OdinDetectorController {

	private static final Logger logger = LoggerFactory.getLogger(OdinDetectorControllerEpics.class);

	private static final int ODIN_TIMEOUT = 5;

	/**
	 * Datawriter constant
	 */
	private static final String ACTIVE = "Capturing";

	private boolean configured;
	private String basePv;

	private String fileWriterDataType;

	private PV<String> dataDirectory;
	private PV<String> filename;
	private PV<Integer> odinFrameCount;
	private PV<Integer> startDataWriter;
	private PV<String> acquiring;
	private PV<String> imageMode;
	private PV<Integer> numImages;
	private PV<String> triggerMode;
	private PV<String> timeoutDataWriter;
	private PV<Integer> timeoutDataWriterPeriod;
	private PV<String> odDataType;
	private ReadOnlyPV<Integer> captured;
	private ReadOnlyPV<String> errorState;
	private PV<Double> acquireTime;
	private PV<Double> acquirePeriod;
	private ReadOnlyPV<String> dataWriter;
	private PV<String> counterDepth;
	private PV<Integer> odinOffset;
	private PV<Integer> odinUid;
	private PV<Integer> framesPerBlock;
	private PV<String> odinCompression;

	/** The most recent file written by this detector */
	private String latestFilename;

	private String fileSuffix = "_000001.h5";

	/** Timeout period used by the timeout/stop on the datawriter */
	private int fileWritingTimeout = 2000;

	@Override
	public void configure() throws FactoryException {
		if (basePv == null) {
			throw new IllegalStateException("Cannot configure Odin detector without base PV");
		}
		if (!configured) {

			// CAMERA PVs
			acquireTime = new PVWithSeparateReadback<>(LazyPVFactory.newDoublePV(basePv + "CAM:AcquireTime"),
					LazyPVFactory.newReadOnlyDoublePV(basePv + "CAM:AcquireTime_RBV"));
			acquirePeriod = new PVWithSeparateReadback<>(LazyPVFactory.newDoublePV(basePv + "CAM:AcquirePeriod"),
					LazyPVFactory.newReadOnlyDoublePV(basePv + "CAM:AcquirePeriod_RBV"));
			acquiring = LazyPVFactory.newEnumPV(basePv + "CAM:Acquire", String.class);
			imageMode = LazyPVFactory.newEnumPV(basePv + "CAM:ImageMode", String.class);
			numImages = LazyPVFactory.newIntegerPV(basePv + "CAM:NumImages");
			triggerMode = LazyPVFactory.newEnumPV(basePv + "CAM:TriggerMode", String.class);
			counterDepth = LazyPVFactory.newEnumPV(basePv + "CAM:CounterDepth", String.class);

			// DATA WRITING PVs
			dataDirectory = new PVWithSeparateReadback<>(LazyPVFactory.newStringFromWaveformPV(basePv + "OD:FilePath"),
					LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD:FilePath_RBV"));
			filename = new PVWithSeparateReadback<>(LazyPVFactory.newStringFromWaveformPV(basePv + "OD:FileName"),
					LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD:FileName_RBV"));
			odinFrameCount = new PVWithSeparateReadback<>(LazyPVFactory.newIntegerPV(basePv + "OD:NumCapture"),
					LazyPVFactory.newReadOnlyIntegerPV(basePv + "OD:NumCapture_RBV"));
			startDataWriter = LazyPVFactory.newEnumPV(basePv + "OD:Capture", Integer.class);
			dataWriter = LazyPVFactory.newReadOnlyEnumPV(basePv + "OD:Capture_RBV", String.class);

			timeoutDataWriter = LazyPVFactory.newEnumPV(basePv + "OD:StartTimeout", String.class);
			timeoutDataWriterPeriod = new PVWithSeparateReadback<>(
					LazyPVFactory.newIntegerPV(basePv + "OD:CloseFileTimeout"),
					LazyPVFactory.newReadOnlyIntegerPV(basePv + "OD:CloseFileTimeout_RBV"));
			odDataType = LazyPVFactory.newEnumPV(basePv + "OD:DataType", String.class);
			captured = LazyPVFactory.newReadOnlyIntegerPV(basePv + "OD:NumCaptured_RBV");
			framesPerBlock = LazyPVFactory.newIntegerPV(basePv + "OD:BlockSize_RBV");
			errorState = LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD1:FPErrorMessage_RBV");
			odinOffset = LazyPVFactory.newIntegerPV(basePv + "OD:OFF:Adjustment");
			odinUid = LazyPVFactory.newIntegerPV(basePv + "OD:PARAM:UID:Adjustment");
			odinCompression = LazyPVFactory.newEnumPV(basePv + "OD:CompressionMode", String.class);
		}
		configured = true;
	}

	@Override
	public void setDataOutput(String directory, String filePrefix) throws DeviceException {
		logger.trace("Setting output to {} and {}", directory, filePrefix);
		try {
			dataDirectory.putWait(directory);
			filename.putWait(filePrefix);
			latestFilename = Paths.get(directory, filePrefix + fileSuffix).toString();
		} catch (IOException e) {
			throw new DeviceException("Could not set data directory or file name", e);
		}
	}

	@Override
	public void startRecording() throws DeviceException {
		logger.trace("Starting data writer");
		try {
			if (startDataWriter.get() == 1) {
				throw new DeviceException("DataWriter already recording");
			}
			startDataWriter.putNoWait(1);
			dataWriter.waitForValue(ACTIVE::equals, ODIN_TIMEOUT);
		} catch (IOException e) {
			logger.error("Couldn't start data writer", e);
			throw new DeviceException("Couldn't start data writers", e);
		} catch (IllegalStateException | TimeoutException e) {
			logger.error("Error while waiting for odin to initialise", e);
			throw new DeviceException("Error while waiting for Odin", e);
		} catch (InterruptedException e) {
			logger.error("Recording interrupted", e);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void startCollection() throws DeviceException {
		logger.debug("Starting collection");
		try {
			logger.debug("Starting acquire");
			acquiring.putNoWait("Acquire");
			logger.debug("Waiting for odin to start");
			acquiring.waitForValue(state -> state.equals("Acquire"), ODIN_TIMEOUT);
			logger.debug("Acq started");
		} catch (IOException e) {
			logger.error("Timeout setting camera to acquire", e);
			throw new DeviceException("Timeout setting camera to acquire", e);
		} catch (Exception e) {
			logger.error("Error waiting for odin to be ready", e);
			throw new DeviceException("Error waiting for Odin", e);
		}
	}

	@Override
	public void stopCollection() throws DeviceException {
		try {
			logger.trace("Stopping collection");
			acquiring.putNoWait("Done");
		} catch (IOException e) {
			logger.error("Timeout stopping camera", e);
			throw new DeviceException("Timeout stopping camera", e);
		}
	}

	@Override
	public void endRecording() {
		logger.trace("Ending recording");
		try {
			if (startDataWriter.get() != 1) {
				logger.debug("Data writer already stopped");
				checkWriterErrors();
				return;
			}
		} catch (IOException e) {
			// Warn to prevent scan falling over. Scan is over at this point anyway
			logger.warn("Could not read data writer state", e);
		}

		try {
			logger.trace("Stopping data writer (via timeout)");
			timeoutDataWriter.putNoWait("Capture");
			while (startDataWriter.get() == 1) {
				Thread.sleep(200);
			}
			checkWriterErrors();
			int imagesCaptured = captured.get();
			int imagesExpected = odinFrameCount.get();
			if (imagesExpected != imagesCaptured) {
				logger.warn("Did not collect expected number of frames. {} expected, {} written.", imagesExpected,
						imagesCaptured);
			}
		} catch (IOException e) {
			logger.warn("Could not stop data writer", e);
		} catch (InterruptedException e) {
			logger.warn("Thread interrupted while waiting for data writing to end", e);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void prepareCamera(int frames, double requestedLiveTime, double requestedDeadTime, String imageMode,
			String triggerMode) throws DeviceException {

		setImageMode(imageMode);
		setTriggerMode(triggerMode);

		double acquisition = requestedLiveTime + requestedDeadTime;
		try {
			numImages.putWait(1);
			acquireTime.putWait(requestedLiveTime);
			acquirePeriod.putWait(acquisition);
			logger.debug("Set exposure times: {} x {}/{}ms", frames, requestedLiveTime, requestedDeadTime);
		} catch (IOException e) {
			throw new DeviceException("Could not set up data recording", e);
		}

	}

	@Override
	public String getLatestFilename() {
		return latestFilename;
	}

	private void setTriggerMode(String trigger) throws DeviceException {
		logger.debug("Setting trigger mode to {}", trigger);
		try {
			triggerMode.putWait(trigger);
		} catch (IOException e) {
			throw new DeviceException("Could not set Odin trigger mode", e);
		}
	}

	private void setImageMode(String image) throws DeviceException {
		logger.debug("Setting image mode to {}", image);
		try {
			imageMode.putWait(image);
		} catch (IOException e) {
			throw new DeviceException("Could not set Odin image mode", e);
		}
	}

	private void checkWriterErrors() throws IOException {
		String error = errorState.get();
		if (error != null && !error.isEmpty()) {
			logger.warn("Data writer was in error state: {}", error);
		}
	}

	public void setBasePv(String basePv) {
		this.basePv = basePv;
	}

	public String getBasePv() {
		return this.basePv;
	}

	@Override
	public int getStatus() {
		try {
			if (acquiring.get().equals("Done") && dataWriter.get().equals("Done")) {
				return Detector.IDLE;
			}
		} catch (IOException e) {
			logger.warn("Couldn't get acquire/datawriter PVs");
		}
		return Detector.BUSY;
	}

	@Override
	public void waitWhileAcquiring() {
		try {
			acquiring.waitForValue(state -> state.equals("Done"), 0);
		} catch (TimeoutException | IOException e) {
			logger.error("Error waiting for acquire pv", e);
		} catch (InterruptedException e) {
			logger.error("Wait interrupted", e);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void waitWhileWriting() {
		try {
			dataWriter.waitForValue(state -> state.equals("Done"), 0);
		} catch (TimeoutException | IOException e) {
			logger.error("Error waiting for data writer pv", e);
		} catch (InterruptedException e) {
			logger.error("Wait interrupted", e);
			Thread.currentThread().interrupt();
		}

	}

	public String getFileWriterDataType() {
		return fileWriterDataType;
	}

	@Override
	public void prepareDataWriter(int frames) throws DeviceException {

		try {
			fileWriterDataType = odDataType.get(); // Record this in case we need it later e.g. vds
			// Ensure that the datawriter is set to receive frames of the correct size
			odinFrameCount.putWait(frames);
			framesPerBlock.putWait(frames);
			timeoutDataWriterPeriod.putWait(fileWritingTimeout, ODIN_TIMEOUT);
			// disable compression
			odinCompression.putWait("off");
		} catch (IOException e) {
			throw new DeviceException("Could not set data writer frame size", e);
		}
		// TODO need to set BL07I-EA-EXCBR-01:OD:Mode and BL07I-EA-EXCBR-01:OD:Compression as well?
	}

	@Override
	public Double getAcquireTime() throws DeviceException {
		try {
			return acquireTime.get();
		} catch (IOException e) {
			logger.warn("Could not get aquire time");
			throw new DeviceException(e);
		}
	}

	public void setAcquireTime(PV<Double> acquireTime) {
		this.acquireTime = acquireTime;
	}

	/**
	 * TODO This is Excalibur specific
	 */
	public String getCounterDepth() {
		try {
			return counterDepth.get();
		} catch (IOException e) {
			logger.error("Could not get Counter Depth from detector {}", e);
			return null;
		}
	}

	/**
	 * TODO This is Excalibur specific
	 *
	 * @param depth
	 *            e.g "12 bit"
	 */
	public void setCounterDepth(String depth) {
		try {
			counterDepth.putWait(depth);
		} catch (IOException e) {
			logger.error("Could not set Counter Depth to {}", depth);
		}
	}

	public int getOdinOffset() throws IOException {
		return odinOffset.get();
	}

	public void setOdinOffset(int offset) throws IOException {
		odinOffset.putWait(offset);
	}

	public int getOdinUid() throws IOException {
		return odinUid.get();
	}

	public void setOdinUid(int uid) throws IOException {
		odinUid.putWait(uid);
	}

	@Override
	public void setOffsetAndUid(int offset, int uid) throws DeviceException {
		try {
			setOdinOffset(offset);
			setOdinUid(uid);
		} catch (IOException e) {
			throw new DeviceException("Could not set Odin offset/uid");
		}

	}

	@Override
	public int getNumFramesCaptured() {
		try {
			return captured.get();
		} catch (IOException e) {
			logger.error("Error reading detector PV", e);
			return 0;
		}
	}

	@Override
	public void waitForWrittenFrames(int noFrames) {
		try {
			logger.debug("Waiting for {} frames to have been captured", noFrames);
			captured.waitForValue(value -> value == noFrames, 0);
		} catch (TimeoutException | IOException e) {
			logger.error("Error waiting for captured pv", e);
		} catch (InterruptedException e) {
			logger.error("Wait interrupted", e);
			Thread.currentThread().interrupt();
		}
	}



}
