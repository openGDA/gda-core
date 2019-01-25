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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import gda.epics.ReadOnlyPV;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.util.OSCommandRunner;
import uk.ac.gda.server.ncd.subdetector.eiger.NcdEigerController;

public class NcdEpicsEiger implements NcdEigerController, Configurable {
	private static final String ACTIVE = "Active";

	private static final Logger logger = LoggerFactory.getLogger(NcdEpicsEiger.class);

	private static final String SLOW_DATA_TYPE = "UInt32";
	private static final String FAST_DATA_TYPE = "UInt16";
	private static final String IMAGE_MODE = "Multiple";
	private static final String TRIGGER_MODE = "External Enable";

	private static final int ODIN_TIMEOUT = 5;

	private boolean configured;

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
	private PV<String> dataType;
	private PV<Integer> imageHeight;
	private PV<Integer> imageWidth;
	private ReadOnlyPV<Integer> captured;
	private ReadOnlyPV<String> errorState;
	private PV<Integer> clearError;
	private PV<Double> acquireTime;
	private PV<Double> acquirePeriod;

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

	private boolean reshaped;

	@Override
	public int[] getDataDimensions() throws DeviceException {
		try {
			logger.trace("Getting eiger data dimensions");
			int width = imageWidth.get();
			int height = imageHeight.get();
			return new int[] {width, height};
		} catch (IOException e) {
			logger.error("Couldn't read image size", e);
			throw new DeviceException("Couldn't read image dimensions", e);
		}
	}

	@Override
	public void startCollection() throws DeviceException {
		reshaped = false;
		logger.trace("Starting collection");
		setImageMode(IMAGE_MODE);
		setTriggerMode(TRIGGER_MODE);
		logger.trace("Set image and trigger modes");
		try {
			logger.trace("Starting acquire");
			acquiring.putNoWait("Acquire");
			logger.trace("Waiting for odin");
			odinReady.waitForValue(i -> i == 1, ODIN_TIMEOUT);
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
	public void configure() throws FactoryException {
		if (basePv == null) {
			throw new IllegalStateException("Cannot configure eiger detector with out base PV");
		}
		if (!configured) {

			// CAMERA PVs
			acquireTime = new PVWithSeparateReadback<Double>(
					LazyPVFactory.newDoublePV(basePv + "CAM:AcquireTime"),
					LazyPVFactory.newReadOnlyDoublePV(basePv + "CAM:AcquireTime_RBV"));
			acquirePeriod = new PVWithSeparateReadback<Double>(
					LazyPVFactory.newDoublePV(basePv + "CAM:AcquirePeriod"),
					LazyPVFactory.newReadOnlyDoublePV(basePv + "CAM:AcquirePeriod_RBV"));
			acquiring = LazyPVFactory.newEnumPV(basePv + "CAM:Acquire", String.class);
			expectedTriggers = new PVWithSeparateReadback<Integer>(
					LazyPVFactory.newIntegerPV(basePv + "CAM:NumTriggers"),
					LazyPVFactory.newReadOnlyIntegerPV(basePv + "CAM:NumTriggers_RBV"));
			imageMode = LazyPVFactory.newEnumPV(basePv + "CAM:ImageMode", String.class);
			triggerMode = LazyPVFactory.newEnumPV(basePv + "CAM:TriggerMode", String.class);

			imageWidth = LazyPVFactory.newIntegerPV(basePv + "CAM:MaxSizeX_RBV");
			imageHeight = LazyPVFactory.newIntegerPV(basePv + "CAM:MaxSizeY_RBV");

			// DATA WRITING PVs
			dataDirectory = new PVWithSeparateReadback<String>(
					LazyPVFactory.newStringFromWaveformPV(basePv + "OD:FilePath"),
					LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD:FilePath_RBV"));
			filename = new PVWithSeparateReadback<String>(
					LazyPVFactory.newStringFromWaveformPV(basePv + "OD:FileName"),
					LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD:FileName_RBV"));
			odinFrameCount = new PVWithSeparateReadback<Integer>(
					LazyPVFactory.newIntegerPV(basePv + "OD:NumCapture"),
					LazyPVFactory.newReadOnlyIntegerPV(basePv + "OD:NumCapture_RBV"));
			startDataWriter = LazyPVFactory.newEnumPV(basePv + "OD:Capture", Integer.class);

			timeoutDataWriter = LazyPVFactory.newEnumPV(basePv + "OD:StartTimeout", String.class);
			dataType = LazyPVFactory.newEnumPV(basePv + "OD:DataType", String.class);
			captured = LazyPVFactory.newReadOnlyIntegerPV(basePv + "OD:NumCaptured_RBV");
			errorState = LazyPVFactory.newReadOnlyStringFromWaveformPV(basePv + "OD1:FPErrorMessage_RBV");
			clearError = LazyPVFactory.newEnumPV(basePv + "OD1:FPClearErrors", Integer.class);
			odinInitialised = LazyPVFactory.newEnumPV(basePv + "OD:META:AcquisitionActive_RBV", String.class);
			odinReady = LazyPVFactory.newEnumPV(basePv + "OD:FAN:StateReady_RBV", Integer.class);
		}
		configured = true;
	}

	@Override
	public boolean isConfigured() {
		return configured;
	}

	@Override
	public void endRecording() {
		logger.trace("Ending recording");
		try {
			if (startDataWriter.get() != 1) {
				logger.debug("Data writer already stopped");
				checkWriterErrors();
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
			checkWriterErrors();
			int imagesCaptured = captured.get();
			int imagesExpected = odinFrameCount.get();
			if (imagesExpected != imagesCaptured) {
				logger.warn("Did not collect expected number of frames. {} expected, {} written.",
						imagesExpected, imagesCaptured);
			} else {
				reshape();
			}
		} catch (IOException e) {
			logger.warn("Could not stop data writer", e);
		} catch (InterruptedException e) {
			logger.warn("Thread interrupted while waiting for data writing to end", e);
		}
	}

	private void reshape() {
		if (reshaped) {
			logger.debug("Data has already been reshaped");
			return;
		}
		logger.debug("Reshaping data");
		if (reshapeCommand != null)  {
			try {
				// dls-vds-gen.py -l 1 -f b21_testing2_000001.h5 b21_testing2_000002.h5 --mode reshape --new-shape 1, 10 -o b21_vds.h5 .
				String outputFile = prefix + ".h5";
				String dimensionString = stream(dimensions).mapToObj(String::valueOf).collect(joining(" ")) + " " + String.valueOf(framesPerPoint);
				logger.debug("Reshaping dataset {}* to {} in directory {}. Writing to {}", prefix, dimensionString, directoryPath, outputFile);
				OSCommandRunner reshape = new OSCommandRunner(new String[] {reshapeCommand, directoryPath, prefix + "_", outputFile, dimensionString}, true, null, null);
				if (reshape.succeeded) {
					logger.debug("Reshaped file written to {}", outputFile);
					waitForReshapedFile(outputFile);
					lastFilename = Paths.get(directoryPath, outputFile).toString();
				} else {
					logger.error("Reshape failed");
					reshape.logOutput();
				}
			} catch (Exception e) {
				logger.error("Couldn't reshape data file", e);
			}
		} else {
			logger.debug("No reshape command given - using original dataset");
		}
	}

	private void checkWriterErrors() throws IOException {
		String error = errorState.get();
		if (error != null && !error.isEmpty()) {
			logger.warn("Data writer was in error state: {}", error);
		}
	}

	@Override
	public void startRecording() throws DeviceException {
		logger.trace("Starting data writer");
		reshaped = false;
		try {
			if (startDataWriter.get() == 1) {
				throw new DeviceException("DataWriter already recording");
			}
			startDataWriter.putNoWait(1);
			odinInitialised.waitForValue(s -> ACTIVE.equals(s), ODIN_TIMEOUT);
		} catch (IOException e) {
			logger.error("Couldn't start data writer", e);
			throw new DeviceException("Couldn't start data writers", e);
		} catch (IllegalStateException | TimeoutException | InterruptedException e) {
			logger.error("Error while waiting for odin to initialise", e);
			throw new DeviceException("Error while waiting for Odin", e);
		}
	}

	@Override
	public void setDataOutput(String directory, String filePrefix) throws DeviceException {
		logger.trace("Setting output to {} and {}", directory, filePrefix);
		try {
			dataDirectory.putWait(directory);
			filename.putWait(filePrefix);
			directoryPath = directory;
			prefix = filePrefix;
			lastFilename = Paths.get(directory, filePrefix + fileSuffix).toString(); //default if reshaping fails
		} catch (IOException e) {
			throw new DeviceException("Could not set data directory or file name", e);
		}
	}

	public void setExposures(int frameCount) throws DeviceException {
		logger.info("Setting camera to collect {} frames", frameCount);
		try {
			expectedTriggers.putWait(frameCount, 3);
			odinFrameCount.putWait(frameCount, 3);
			clearError.putWait(1, 3);
		} catch (IOException e) {
			logger.error("Timeout setting camera triggers", e);
			throw new DeviceException("Could not set expected triggers");
		}
	}

	@Override
	public void setScanDimensions(int[] dims) throws DeviceException {
		logger.trace("Setting scan dimensions: {}", Arrays.toString(dims));
		setExposures(getFrameCount(dims));
		dimensions = dims;
	}

	private int getFrameCount(int[] scanDimensions) {
		int scanPoints = Arrays.stream(scanDimensions)
				.reduce((a, b) -> a*b)
				.orElseThrow(() -> new IllegalArgumentException("Empty scan dimensions given to eiger controller"));
		if (scanPoints == 0) {
			throw new IllegalArgumentException("Scan dimensions cannot include 0");
		}
		return scanPoints * framesPerPoint;
	}

	@Override
	public void setExposureTimes(int frames, double requestedLiveTime, double requestedDeadTime) throws DeviceException {
		logger.trace("Setting exposure times: {} x {}/{}ms", frames, requestedLiveTime, requestedDeadTime);
		framesPerPoint = frames;
		double acquisition = requestedLiveTime + requestedDeadTime;
		try {
			if (acquisition < 20) {// faster than 50 Hz
				logger.trace("Setting data type to {}", FAST_DATA_TYPE);
				dataType.putWait(FAST_DATA_TYPE);
			} else {
				logger.trace("Setting data type to {}", SLOW_DATA_TYPE);
				dataType.putWait(SLOW_DATA_TYPE);
			}
			acquireTime.putWait(requestedLiveTime / 1000);
			acquirePeriod.putWait(acquisition / 1000);
		} catch (IOException e) {
			throw new DeviceException("Could not set up data recording", e);
		}

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

	public String getReshapeCommand() {
		return reshapeCommand;
	}

	public void setReshapeCommand(String reshapeCommand) {
		this.reshapeCommand = reshapeCommand;
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
}
