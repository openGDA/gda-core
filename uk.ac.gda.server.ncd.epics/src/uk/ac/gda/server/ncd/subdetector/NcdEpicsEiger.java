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

package uk.ac.gda.server.ncd.subdetector;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import gda.epics.ReadOnlyPV;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.util.OSCommandRunner;
import uk.ac.gda.server.ncd.subdetector.eiger.NcdEigerController;

public class NcdEpicsEiger extends ConfigurableBase implements NcdEigerController {
	private static final String FILE_EXT = ".h5";

	private static final String INVALID_STATE_WARNING = "Error during configuration left detector in inconsistent state "
			+ "- try reconfiguring detector and contact support if problems continue.";

	public enum ParameterState {
		VALID, STALE;
	}

	private static final String ACTIVE = "Active";

	private static final Logger logger = LoggerFactory.getLogger(NcdEpicsEiger.class);

	private static final String IMAGE_MODE = "Multiple";
	private static final String TRIGGER_MODE = "External Enable";
	private static final Map<Integer, String> BIT_DEPTH_MAP = Map.of(
			16, "UInt16",
			32, "UInt32");

	private int eiger_timeout = 5;

	private String basePv;

	private int framesPerPoint = 1;

	private PV<String> dataDirectory;
	private PV<String> filename;
	private PV<Integer> odinFrameCount;
	private PV<Integer> startDataWriter;
	private PV<String> acquiring;
	private PV<Integer> expectedTriggers;
	private PV<String> imageMode;
	private PV<String> triggerMode;
	private PV<String> timeoutDataWriter;
	private PV<Integer> timeoutDataWriterPeriod;
	private PV<String> dataType;
	private PV<Integer> imageHeight;
	private PV<Integer> imageWidth;
	private ReadOnlyPV<Integer> captured;
	private ReadOnlyPV<String> errorState;
	private PV<Integer> clearError;
	private PV<Double> acquireTime;
	private PV<Double> acquirePeriod;
	/** The datatype being read by the detector. The datatype odin expects must be set to match */
	private ReadOnlyPV<Integer> bitDepth;

	private PV<String> odinInitialised;
	private PV<Integer> odinReady;

	private String fileSuffix = "_000001.h5";

	/** The most recent file written by this detector. Possibly a reshaped file */
	private String lastFilename;
	/** Command to run at end of scan to reshape dataset */
	private String reshapeCommand;

	/** The dimensions of the last scan run - used for reshaping */
	private int[] dimensions;

	/** Prefix used for naming files */
	private String prefix;

	/** Data directory used for data writing */
	private String directoryPath;

	/** Timeout period used by the timeout/stop on the datawriter */
	private int fileWritingTimeout = 2000;

	private boolean reshaped;

	/** Maintain a record of errors so that subsequent commands don't run with the detector in an inconsistent state */
	// The exposure times of the detector are configured when they change, not at the start of each collection. This means
	// that if the update fails, the next scan could otherwise continue even though the exposure times are incorrect.
	private String errorMessage;

	@Override
	public int[] getDataDimensions() throws DeviceException {
		try {
			logger.trace("Getting eiger data dimensions");
			int width = imageWidth.get();
			int height = imageHeight.get();
			return new int[] {width, height};
		} catch (IOException e) {
			throw new DeviceException("Couldn't read eiger image dimensions", e);
		}
	}

	@Override
	public void startCollection() throws DeviceException {
		checkErrors();
		reshaped = false;
		logger.trace("Starting collection");
		setImageMode(IMAGE_MODE);
		setTriggerMode(TRIGGER_MODE);
		logger.trace("Set image and trigger modes");
		try {
			logger.trace("Starting acquire");
			acquiring.putNoWait("Acquire");
			logger.trace("Waiting for odin");
			odinReady.waitForValue(i -> i == 1, eiger_timeout);
		} catch (IOException e) {
			throw new DeviceException("Timeout setting eiger to acquire", e);
		} catch (Exception e) {
			throw new DeviceException("Error waiting for Odin", e);
		}
	}

	@Override
	public void stopCollection() throws DeviceException {
		try {
			logger.trace("Stopping collection");
			acquiring.putNoWait("Done");
		} catch (IOException e) {
			throw new DeviceException("Timeout stopping eiger", e);
		}
	}

	@Override
	public void configure() throws FactoryException {
		if (basePv == null) {
			throw new IllegalStateException("Cannot configure eiger detector with out base PV");
		}
		if (!isConfigured()) {
			// CAMERA PVs
			acquireTime = new PVWithSeparateReadback<>(
					LazyPVFactory.newDoublePV(basePv + "CAM:AcquireTime"),
					LazyPVFactory.newReadOnlyDoublePV(basePv + "CAM:AcquireTime_RBV"));
			acquirePeriod = new PVWithSeparateReadback<>(
					LazyPVFactory.newDoublePV(basePv + "CAM:AcquirePeriod"),
					LazyPVFactory.newReadOnlyDoublePV(basePv + "CAM:AcquirePeriod_RBV"));
			acquiring = LazyPVFactory.newEnumPV(basePv + "CAM:Acquire", String.class);
			expectedTriggers = new PVWithSeparateReadback<>(
					LazyPVFactory.newIntegerPV(basePv + "CAM:NumTriggers"),
					LazyPVFactory.newReadOnlyIntegerPV(basePv + "CAM:NumTriggers_RBV"));
			imageMode = LazyPVFactory.newEnumPV(basePv + "CAM:ImageMode", String.class);
			triggerMode = LazyPVFactory.newEnumPV(basePv + "CAM:TriggerMode", String.class);

			imageWidth = LazyPVFactory.newIntegerPV(basePv + "CAM:MaxSizeX_RBV");
			imageHeight = LazyPVFactory.newIntegerPV(basePv + "CAM:MaxSizeY_RBV");

			bitDepth = LazyPVFactory.newReadOnlyIntegerPV(basePv + "CAM:BitDepthImage_RBV");

			// DATA WRITING PVs
			dataDirectory = new PVWithSeparateReadback<>(
					LazyPVFactory.newStringFromWaveformPV(basePv + "OD:FilePath"),
					LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD:FilePath_RBV"));
			filename = new PVWithSeparateReadback<>(
					LazyPVFactory.newStringFromWaveformPV(basePv + "OD:FileName"),
					LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD:FileName_RBV"));
			odinFrameCount = new PVWithSeparateReadback<>(
					LazyPVFactory.newIntegerPV(basePv + "OD:NumCapture"),
					LazyPVFactory.newReadOnlyIntegerPV(basePv + "OD:NumCapture_RBV"));
			startDataWriter = LazyPVFactory.newEnumPV(basePv + "OD:Capture", Integer.class);

			timeoutDataWriter = LazyPVFactory.newEnumPV(basePv + "OD:StartTimeout", String.class);
			timeoutDataWriterPeriod = new PVWithSeparateReadback<>(
					LazyPVFactory.newIntegerPV(basePv + "OD:CloseFileTimeout"),
					LazyPVFactory.newReadOnlyIntegerPV(basePv + "OD:CloseFileTimeout_RBV"));
			dataType = LazyPVFactory.newEnumPV(basePv + "OD:DataType", String.class);
			captured = LazyPVFactory.newReadOnlyIntegerPV(basePv + "OD:NumCaptured_RBV");
			errorState = LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD1:FPErrorMessage_RBV");
			clearError = LazyPVFactory.newEnumPV(basePv + "OD1:FPClearErrors", Integer.class);
			odinInitialised = LazyPVFactory.newEnumPV(basePv + "OD:META:AcquisitionActive_RBV", String.class);
			odinReady = LazyPVFactory.newEnumPV(basePv + "OD:FAN:StateReady_RBV", Integer.class);
		}
		setConfigured(true);
	}

	@Override
	public void endRecording() throws DeviceException {
		logger.trace("Ending recording");
		try {
			if (startDataWriter.get() != 1) {
				logger.debug("Data writer already stopped");
				// TODO: only call reshape once
				reshape();
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
			int imagesCaptured = captured.get();
			int imagesExpected = odinFrameCount.get();
			if (imagesExpected != imagesCaptured) {
				logger.warn("Did not collect expected number of frames. {} expected, {} written.",
						imagesExpected, imagesCaptured);
				throw new DeviceException("Did not collect expected number of frames. "
						+ imagesExpected + " expected, "
						+ imagesCaptured + " written.");
			} else {
				reshape();
			}
		} catch (IOException e) {
			logger.warn("Could not stop data writer", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Thread interrupted while waiting for data writing to end", e);
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		endRecording();
		stopCollection();
		checkWriterErrors();
	}

	private void reshape() {
		if (reshaped) {
			logger.debug("Data has already been reshaped");
			return;
		}
		logger.debug("Reshaping data");
		if (reshapeCommand != null)  {
			try {
				// dls-vds-gen.py /dls/b21/data/2023/sm33575-1/ --files b21-442877-eiger_000001.h5 --data-type uint32 --shape 20 2167 2070 --output /tmp/reshaped.h5 --mode reshape --new-shape 4 5
				String dataType = this.dataType.get().toLowerCase();
				String originalShape = odinFrameCount.get()+" "+imageHeight.get()+" "+imageWidth.get();
				String outputFile = prefix + FILE_EXT;
				String dimensionString = stream(dimensions).mapToObj(String::valueOf).collect(joining(" ")) + " " + framesPerPoint;
				logger.debug("Reshaping dataset {}* to {} in directory {}. Writing to {}", prefix, dimensionString, directoryPath, outputFile);
				OSCommandRunner reshape = new OSCommandRunner(new String[] {reshapeCommand, directoryPath, prefix + "_", dataType, originalShape, outputFile, dimensionString}, true, null, null);
				if (TRUE.equals(reshape.succeeded)) {
					logger.debug("Reshaped file written to {}", outputFile);
					waitForReshapedFile(outputFile);
				} else {
					logger.error("Reshape failed");
					reshape.logOutput();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("Couldn't reshape data file", e);
			} catch (IOException e) {
				logger.error("Couldn't read parameter to reshape", e);
			}
		} else {
			logger.debug("No reshape command given - using original dataset");
		}
	}

	private void checkWriterErrors() throws DeviceException {
		try {
			String error = errorState.get();
			if (error != null && !error.isBlank()) {
				logger.warn("Data writer was in error state: {}", error);
				throw new DeviceException("Data writer was in error state: " + error);
			}
		} catch (IOException e) {
			throw new DeviceException("Couldn't check writer errors", e);
		}
	}

	@Override
	public void startRecording() throws DeviceException {
		checkErrors();
		logger.trace("Starting data writer");
		updateDataType();
		reshaped = false;
		try {
			if (startDataWriter.get() == 1) {
				throw new DeviceException("DataWriter already recording");
			}
			timeoutDataWriterPeriod.putWait(fileWritingTimeout, eiger_timeout);
			startDataWriter.putNoWait(1);
			odinInitialised.waitForValue(ACTIVE::equals, eiger_timeout);
		} catch (IOException e) {
			throw new DeviceException("Couldn't start eiger data writers", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Interrupted while starting recording", e);
		} catch (IllegalStateException | TimeoutException e) {
			throw new DeviceException("Error while waiting for Odin", e);
		}
	}

	@Override
	public void setDataOutput(String directory, String filePrefix) throws DeviceException {
		logger.trace("Setting output to {} and {}", directory, filePrefix);
		try {
			dataDirectory.putWait(directory, eiger_timeout);
			logger.debug("(1/2) Setting eiger directory to {}", directory);
			filename.putWait(filePrefix, eiger_timeout);
			logger.debug("(2/2) Setting eiger prefix to {}", filePrefix);
			directoryPath = directory;
			prefix = filePrefix;
			lastFilename = Paths.get(directory, filePrefix + FILE_EXT).toString();
			logger.debug("Last eiger filename: {}", lastFilename);
		} catch (IOException e) {
			throw new DeviceException("Could not set data directory or file name for eiger", e);
		}
	}

	public void setExposures(int frameCount) throws DeviceException {
		logger.info("Setting camera to collect {} frames", frameCount);
		try {
			expectedTriggers.putWait(frameCount, eiger_timeout);
			odinFrameCount.putWait(frameCount, eiger_timeout);
			clearError.putWait(1, eiger_timeout);
		} catch (IOException e) {
			logger.error("Timeout setting camera triggers", e);
			throw new DeviceException("Could not set expected triggers for eiger");
		}
	}

	@Override
	public void setScanDimensions(int[] dims) throws DeviceException {
		if (logger.isTraceEnabled()) {
			logger.trace("Setting scan dimensions: {}", Arrays.toString(dims));
		}
		setExposures(getFrameCount(dims));
		dimensions = dims;
	}

	private int getFrameCount(int[] scanDimensions) {
		int scanPoints = Arrays.stream(scanDimensions)
				.reduce((a, b) -> a*b)
				.orElseThrow(() -> new IllegalArgumentException("Empty scan dimensions given to eiger controller"));
		if (scanPoints == 0) {
			throw new IllegalArgumentException("Scan dimensions for eiger cannot include 0");
		}
		return scanPoints * framesPerPoint;
	}

	@Override
	public void setExposureTimes(int frames, double requestedLiveTime, double requestedDeadTime) throws DeviceException {
		logger.trace("Setting exposure times: {} x {}/{}ms", frames, requestedLiveTime, requestedDeadTime);
		// If anything fails here, leave error message so inconsistent state can be fixed.
		errorMessage = INVALID_STATE_WARNING;
		framesPerPoint = frames;
		double acquisition = requestedLiveTime + requestedDeadTime;
		try {
			acquireTime.putWait(requestedLiveTime / 1000);
			acquirePeriod.putWait(acquisition / 1000);
		} catch (IOException  e) {
			throw new DeviceException("Could not set up eiger data recording", e);
		}
		errorMessage = null;
	}

	public void setTriggerMode(String trigger) throws DeviceException {
		logger.trace("Setting trigger mode to {}", trigger);
		try {
			triggerMode.putWait(trigger);
		} catch (IOException e) {
			throw new DeviceException("Could not set eiger trigger mode", e);
		}
	}

	public void setImageMode(String image) throws DeviceException {
		logger.trace("Setting image mode to {}", image);
		try {
			imageMode.putWait(image);
		} catch (IOException e) {
			throw new DeviceException("Could not set eiger image mode", e);
		}
	}

	public String getBasePv() {
		return basePv;
	}

	public void setBasePv(String basePv) {
		this.basePv = basePv;
	}

	@Override
	public String toString() {
		return String.format("EigerController(%s)", basePv);
	}

	@Override
	public String getLastFile() {
		return lastFilename;
	}

	public String getFileSuffix() {
		return fileSuffix;
	}

	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

	public int getEiger_timeout() {
		return eiger_timeout;
	}

	public void setEiger_timeout(int eiger_timeout) {
		this.eiger_timeout = eiger_timeout;
	}

	public String getReshapeCommand() {
		return reshapeCommand;
	}

	public void setReshapeCommand(String reshapeCommand) {
		this.reshapeCommand = reshapeCommand;
	}

	public void setFileWritingTimeout(int ms) {
		fileWritingTimeout = ms;
	}

	private void waitForReshapedFile(String filepath) throws InterruptedException {
		File target = Paths.get(directoryPath, filepath).toFile();
		logger.trace("Waiting for file: {}", target);
		int i = 0;
		while (!target.exists() && i < 10) {
			logger.trace("Waiting for {}", filepath);
			Thread.sleep(500);
			i++;
		}
		reshaped = true;
		logger.trace("End of waiting. File {}found", i == 10 ? "not " : "");
	}

	/**
	 * Update the datatype expected by odin to match the bit depth of the detector
	 * @throws DeviceException if current data type or image can't be read or datatype can't be set
	 */
	private void updateDataType() throws DeviceException {
		errorMessage = INVALID_STATE_WARNING;
		try {
			int depth = bitDepth.get();
			String type = BIT_DEPTH_MAP.get(depth);
			if (type == null) {
				throw new DeviceException("Unrecognised bit depth (" + depth + ") - detector in inconsistent state");
			}
			if (type.equals(dataType.get())) {
				logger.trace("Odin datatype already set to {}", type);
			} else {
				logger.trace("Setting odin data type to {}", type);
				dataType.putWait(type);
			}
		} catch (IOException e) {
			throw new DeviceException("Could not ensure correct datatype set for odin datawriter", e);
		}
		errorMessage = null;
	}

	/** Check if detector has been left in inconsistent state by previous errors */
	private void checkErrors() throws DeviceException {
		if (errorMessage != null) {
			throw new DeviceException(errorMessage);
		}
	}
}
