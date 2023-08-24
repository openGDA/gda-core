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

package uk.ac.gda.devices.odin.control;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

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

	private static final int ODIN_TIMEOUT = 10;

	/**
	 * Datawriter constant
	 */
	private static final String ACTIVE = "Capturing";

	private static final String ACQUIRE = "Acquire";

	private boolean configured;
	private String basePv;
	private String fileWriterDataType;
	private List<OdinDataWriter> dataWriters;
	private int numDataWriters;

	private PV<String> dataDirectory;
	private PV<String> fileName;
	private PV<Integer> odinFramesToCapture;
	private PV<Integer> startDataWriter;
	private PV<String> acquiring;
	private PV<String> imageMode;
	private PV<Integer> numImages;
	private PV<String> triggerMode;
	private PV<Integer> timeoutDataWriter;
	private PV<Integer> timeoutDataWriterPeriod;
	private PV<String> odDataType;
	private ReadOnlyPV<String> odFpFileName;
	private ReadOnlyPV<String> odAcquisitionId;
	private ReadOnlyPV<Integer> odinFramesCaptured;
	private PV<Double> acquireTime;
	private PV<Double> acquirePeriod;
	private ReadOnlyPV<String> dataWriter;
	private PV<String> odCompressionMode;


	private PV<Integer> odinOffset;
	private PV<Integer> odinUid;
	private PV<Integer> framesPerBlock;

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



			// DATA WRITING PVs
			dataDirectory = new PVWithSeparateReadback<>(LazyPVFactory.newStringFromWaveformPV(basePv + "OD:FilePath"),
					LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD:FilePath_RBV"));
			fileName = new PVWithSeparateReadback<>(LazyPVFactory.newStringFromWaveformPV(basePv + "OD:FileName"),
					LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD:FileName_RBV"));
			odFpFileName = LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD:FP:FileName_RBV");
			odAcquisitionId = LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD:AcquisitionID_RBV");
			odinFramesToCapture = new PVWithSeparateReadback<>(LazyPVFactory.newIntegerPV(basePv + "OD:NumCapture"),
					LazyPVFactory.newReadOnlyIntegerPV(basePv + "OD:NumCapture_RBV"));
			startDataWriter = new PVWithSeparateReadback<>(LazyPVFactory.newEnumPV(basePv + "OD:Capture", Integer.class),
					LazyPVFactory.newEnumPV(basePv + "OD:Capture_RBV", Integer.class));
			dataWriter = LazyPVFactory.newReadOnlyEnumPV(basePv + "OD:Capture_RBV", String.class);

			timeoutDataWriter = LazyPVFactory.newIntegerPV(basePv + "OD:StartTimeout");
			timeoutDataWriterPeriod = new PVWithSeparateReadback<>(
					LazyPVFactory.newIntegerPV(basePv + "OD:CloseFileTimeout"),
					LazyPVFactory.newReadOnlyIntegerPV(basePv + "OD:CloseFileTimeout_RBV"));
			odDataType = LazyPVFactory.newEnumPV(basePv + "OD:DataType", String.class);
			odinFramesCaptured = LazyPVFactory.newReadOnlyIntegerPV(basePv + "OD:NumCaptured_RBV");
			framesPerBlock = LazyPVFactory.newIntegerPV(basePv + "OD:BlockSize");
			odinOffset = LazyPVFactory.newIntegerPV(basePv + "OD:OFF:Adjustment");
			odinUid = LazyPVFactory.newIntegerPV(basePv + "OD:PARAM:UID:Adjustment");

			odCompressionMode = LazyPVFactory.newEnumPV(basePv + "OD:CompressionMode", String.class);

			// Create Data Writers
			dataWriters = IntStream.rangeClosed(1, numDataWriters).mapToObj(OdinDataWriter::new).collect(toList());
		}
		configured = true;
	}

	@Override
	public void setDataOutput(String directory, String filePrefix) throws DeviceException {
		logger.debug("Setting output to {} and {}", directory, filePrefix);
		try {
			dataDirectory.putWait(directory);
			fileName.putWait(filePrefix);
			odFpFileName.waitForValue(filePrefix::equals, ODIN_TIMEOUT);
			odAcquisitionId.waitForValue(filePrefix::equals, ODIN_TIMEOUT);
			latestFilename = Paths.get(directory, filePrefix + fileSuffix).toString();
		} catch (IOException | IllegalStateException | TimeoutException e) {
			throw new DeviceException("Could not set data directory or file name", e);
		} catch (InterruptedException e) {
			logger.error("Interrupted waiting for filename PVs");
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void startRecording() throws DeviceException {
		logger.debug("Starting data writer");
		try {
			if (startDataWriter.get() == 1) {
				throw new DeviceException("DataWriter already recording");
			}
			startDataWriter.putNoWait(1);
			dataWriter.waitForValue(ACTIVE::equals, ODIN_TIMEOUT);
			// Check zero frames captured at the start
			if (odinFramesCaptured.get() != 0) {
				throw new DeviceException("Initial frames captures is not zero");
			}
		} catch (IOException e) {
			throw new DeviceException("Couldn't start data writer(s)", e);
		} catch (IllegalStateException | TimeoutException e) {
			throw new DeviceException("Error while waiting for Odin to initialise", e);
		} catch (InterruptedException e) {
			logger.error("Recording interrupted", e);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void startCollection() throws DeviceException {
		logger.debug("Starting acquire");
		try {
			acquiring.putNoWait(ACQUIRE);
			acquiring.waitForValue(ACQUIRE::equals, ODIN_TIMEOUT);
			logger.debug("Acquisition started");
		} catch (IOException e) {
			throw new DeviceException("Timeout setting camera to acquire", e);
		} catch (IllegalStateException | TimeoutException e) {
			throw new DeviceException("Error while waiting for Detector to start acquiring", e);
		} catch (InterruptedException e) {
			logger.error("Collection interrupted", e);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void stopCollection() throws DeviceException {
		try {
			logger.debug("Stopping collection");
			acquiring.putNoWait("Done");
		} catch (IOException e) {
			throw new DeviceException("Timeout stopping collection", e);
		}
	}

	@Override
	public void endRecording() {
		logger.debug("Ending recording");
		try {
			if (startDataWriter.get() != 1) {
				logger.debug("Data writer already stopped");
				checkWriterErrors();
			} else {
				logger.debug("Stopping data writer (via timeout)");
				timeoutDataWriter.putNoWait(1);
				while (startDataWriter.get() == 1) {
					Thread.sleep(200);
				}
				checkWriterErrors();
				int imagesCaptured = odinFramesCaptured.get();
				int imagesExpected = odinFramesToCapture.get();
				if (imagesExpected != imagesCaptured) {
					logger.warn("Did not collect expected number of frames. {} expected, {} written.", imagesExpected,
							imagesCaptured);
				}
			}
			// Reset frames per block and offset as detector may be used by Malcolm subsequently
			framesPerBlock.putWait(1);
			odinOffset.putWait(0);
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

	public int getNumDataWriters() {
		return numDataWriters;
	}

	public void setNumDataWriters(int numDataWriters) {
		this.numDataWriters = numDataWriters;
	}

	private void checkWriterErrors() throws IOException {
		for (var writer : dataWriters) {
			writer.logError();
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
			// Clear any writer errors from previous run
			for (var writer : dataWriters) {
				writer.clearErrors();
			}
			fileWriterDataType = odDataType.get(); // Record this in case we need it later e.g. vds
			// Ensure that the datawriter is set to receive frames of the correct size
			odinFramesToCapture.putWait(frames);
			framesPerBlock.putWait(frames);
			timeoutDataWriterPeriod.putWait(fileWritingTimeout, ODIN_TIMEOUT);
		} catch (IOException e) {
			throw new DeviceException("Could not set data writer frame size", e);
		}
	}

	@Override
	public double getAcquireTime() throws DeviceException {
		try {
			return acquireTime.get();
		} catch (IOException e) {
			logger.warn("Could not get aquire time");
			throw new DeviceException(e);
		}
	}

	@Override
	public void setAcquireTime(double acquireTime)  throws DeviceException {
		try {
			this.acquireTime.putWait(acquireTime);
		} catch (IOException e) {
			throw new DeviceException("Could not set acquire time");
		}
	}

	@Override
	public double getAcquirePeriod() throws DeviceException {
		try {
			return acquirePeriod.get();
		} catch (IOException e) {
			logger.warn("Could not get aquire time");
			throw new DeviceException(e);
		}
	}

	@Override
	public void setAcquirePeriod(double acquirePeriod)  throws DeviceException {
		try {
			this.acquirePeriod.putWait(acquirePeriod);
		} catch (IOException e) {
			throw new DeviceException("Could not set acquire time");
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
			return odinFramesCaptured.get();
		} catch (IOException e) {
			logger.error("Error reading detector PV", e);
			return 0;
		}
	}


	@Override
	public void setNumImages(int numImages) throws DeviceException {
		try {
			this.numImages.putWait(numImages);
		} catch (IOException e) {
			throw new DeviceException("Could not set numImages");
		}
	}

	@Override
	public int getNumImages() throws DeviceException {
		try {
			return numImages.get();
		} catch (IOException e) {
			throw new DeviceException("Could not get numImages");
		}
	}

	@Override
	public void waitForWrittenFrames(int noFrames) {
		try {
			logger.debug("Waiting for {} frames to have been captured", noFrames);
			odinFramesCaptured.waitForValue(value -> value == noFrames, 0);
		} catch (TimeoutException | IOException e) {
			logger.error("Error waiting for captured pv", e);
		} catch (InterruptedException e) {
			logger.error("Wait interrupted", e);
			Thread.currentThread().interrupt();
		}
	}


	/**
	 * Set the compression mode.
	 * Note that this PV has a special value which corresponds to disconnecting the filewriting
	 * @param mode can be "off", "on" or "no_hdf"
	 */
	@Override
	public void setCompressionMode(String mode) throws DeviceException {
		try {
			odCompressionMode.putWait(mode);
		} catch (IOException e) {
			throw new DeviceException("Could not set compression mode", e);
		}
	}

	/**
	 * An Odin detector is deployed with a specific number of filewriters
	 */
	private class OdinDataWriter {
		/** base PV including the ODx suffix e.g. BL07I-EA-EIGER-01:OD1: */
		private String odBasePv;
		private int id;

		private ReadOnlyPV<String> errorState;
		private PV<Integer> clearErrors;

		public OdinDataWriter(int id) {
			this.id = id;
			this.odBasePv = String.format("%sOD%d:", basePv, id);

			errorState = LazyPVFactory.newReadOnlyStringFromWaveformPV(odBasePv + "FPErrorMessage_RBV");
			clearErrors = LazyPVFactory.newIntegerPV(odBasePv + "FPClearErrors");

		}

		public void clearErrors() throws IOException {
			clearErrors.putWait(1);
		}

		public void logError() throws IOException {
			String error = errorState.get();
			if (error != null && !error.isEmpty()) {
				logger.warn("Data writer {} was in error state: {}", id, error);
			}
		}

	}



}
