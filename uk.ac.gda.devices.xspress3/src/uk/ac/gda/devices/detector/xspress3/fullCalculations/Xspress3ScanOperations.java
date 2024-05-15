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

package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import java.io.File;

import gda.device.DeviceException;
import gda.device.Timer;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import uk.ac.gda.devices.detector.xspress3.CAPTURE_MODE;
import uk.ac.gda.devices.detector.xspress3.TRIGGER_MODE;
import uk.ac.gda.devices.detector.xspress3.UPDATE_CTRL;
import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;
import uk.ac.gda.devices.detector.xspress3.Xspress3Detector.XspressHelperMethods;

/* This class is used for testing Xspress3 v2 and will replace in the long run Xspress3ScanOperations. A new class was needed in order not to interfere with
 * other beamlines that are using Xspress3.
 */

public class Xspress3ScanOperations {

	private Xspress3Controller controller;

	private boolean readDataFromFile;
	private int lengthOfEachScanLine;
	private int lineNumber;

	private String filePath = "";
	private String defaultSubDirectory = "";
	private String filePrefix = "";
	private String fileTemplate = "%s%s%d.hdf";
	private boolean saveHdfAttributes = true;

	private static final int MONITOR_FILE_TIMEOUT = 60000;
	// Chunk size is typically 1 MB
	private static final int CHUNK_SIZE = 1024 * 1024;
	// Data Type is Float64 so take 8 bytes
	private static final int DATA_TYPE_SIZE = 8;

	public Xspress3ScanOperations(Xspress3Controller controller) {
		this.controller = controller;
		this.lineNumber = 0;
	}

	public void atScanStart(boolean readDataFromFile) throws DeviceException {
		this.readDataFromFile = readDataFromFile;
		ScanInformation currentscan = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();

		int[] currentDimensions = currentscan.getDimensions();
		lengthOfEachScanLine = currentDimensions[currentDimensions.length - 1];
		controller.doStop();

		// to improve performance start acquisition at the start of the map and then wait for triggers
		// to be more generic replace by the total size of the scan
		int totalNumberOfPointsInScan = currentscan.getNumberOfPoints();
		setNumberOfFramesToCollect(totalNumberOfPointsInScan);
		// When restarting EPICs IOC the trigger mode is set by default to Internal and the scan fails.
		// In future if needed instead of being hardcoded can give the choice to user to choose the trigger mode.
		controller.setTriggerMode(TRIGGER_MODE.TTl_Veto_Only);

		if (readDataFromFile) {
			prepareFileWriting();
		}
		lineNumber = 0;
		clearAndStart();
	}

	public void atScanLineStart() throws DeviceException {
		lineNumber++;
		if (lineNumber > 1) {
			savingHDFFiles();
		}
	}

	public void atScanEnd() throws DeviceException {
		controller.doStop();
		// Stop the hdf file writer
		if (controller.isSavingFiles()) {
			controller.doStopSavingFiles();
		}
	}

	/*
	 * Must be called after currentScanNumber updated SR it seems that from scanBase the prepareForCollection for detectors is called after prepareScanNumber
	 */
	private void prepareFileWriting() throws DeviceException {
		// make sure that the callback is enable before to start a scan otherwise no data can be saved in the HDF5 file.
		controller.setFileEnableCallBacks(UPDATE_CTRL.Enable);
		// make sure that the Capture Mode is et to Stream otherwise the scan file
		controller.setFileCaptureMode(CAPTURE_MODE.Stream);
		// make sure that it sets otherwise if Array counter is always adding and not resetting EPICs could failed
		controller.setFileArrayCounter(0);

		setupHdfFilePath();

		controller.setNextFileNumber(0);
		controller.setHDFAttributes(saveHdfAttributes);
		controller.setHDFPerformance(false);
		controller.setHDFLazyOpen(true);

		int framesPerChunk = (CHUNK_SIZE / (controller.getNumberOfChannels() * controller.getMcaSize() * DATA_TYPE_SIZE));
		controller.setHDFNumFramesChunks(framesPerChunk);
		controller.setHDFFileAutoIncrement(true);
		controller.setHDFNumFramesToAcquire(lengthOfEachScanLine);
		controller.setNextFileNumber(0);
		savingHDFFiles();
	}

	private void setNumberOfFramesToCollect(int numberOfFramesToCollect) throws DeviceException {
		controller.setNumFramesToAcquire(numberOfFramesToCollect);
		controller.setPointsPerRow(lengthOfEachScanLine);
	}

	public void clearAndStart() throws DeviceException {
		controller.doErase();
		controller.setArrayCounter(0);
		setHDFPVs();
		controller.doStart();
	}

	protected double[][] getMcaData(double time) throws DeviceException {
		controller.setTriggerMode(TRIGGER_MODE.TTl_Veto_Only);
		clearAndStart();

		Timer tfg = Finder.find("tfg");
		tfg.clearFrameSets(); // we only want to collect a frame at a time
		tfg.countAsync(time); // run tfg for time
		do {
			synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
				}
			}
		} while (tfg.getStatus() == Timer.ACTIVE);

		// Wait a bit to give change MCA data PV records chance to update
		try {
			Thread.sleep(200L);
		} catch (InterruptedException e) {
		}

		controller.doStop();

		return controller.readoutDTCorrectedLatestMCA(0, controller.getNumberOfChannels() - 1);
	}

	private void setHDFPVs() throws DeviceException  {
		controller.setHDFNDArrayPort("XSP3.DTC");
		controller.setDeadTimeCorrectionInputArrayPort("XSP3");
		controller.setFileTemplate(fileTemplate);
		controller.setHDFXML("");
		controller.setHDFExtraDimensions(0);
		controller.setHDFNumFramesToAcquire(0);
		controller.setHDFNumFramesChunks(10);
		controller.setHDFNDAttributeChunk(10);
		controller.setHDFPositionMode(false);
		controller.setHDFNumFramesToAcquire(controller.getNumFramesToAcquire());
	}

	private void savingHDFFiles() throws DeviceException {
		// we suppose that each line has the same number of points, does not need to set again the number of frames in the file writer
		controller.setSavingFiles(readDataFromFile);
		// check if the EPICS HDF file writer is ready
		if (readDataFromFile) {
			long startTime = System.currentTimeMillis();
			while (!controller.isSavingFiles()) {
				long timeForFileWriterToBePrepared = System.currentTimeMillis() - startTime;
				if (timeForFileWriterToBePrepared > MONITOR_FILE_TIMEOUT)
					throw new DeviceException("Timeout monitoring Xspress3 CAPTURE_RBV PV.");
			}
		}
	}

	private void setupHdfFilePath() throws DeviceException {
		String hdfDir = XspressHelperMethods.getFilePath(filePath, defaultSubDirectory);
		// make any parent directories
		File file = new File(hdfDir);
		file.mkdirs();
		controller.setFilePath(hdfDir);
		controller.setFilePrefix(XspressHelperMethods.getFilePrefix(filePrefix));
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getDefaultSubDirectory() {
		return defaultSubDirectory;
	}

	public void setDefaultSubDirectory(String defaultSubDirectory) {
		this.defaultSubDirectory = defaultSubDirectory;
	}

	public String getFilePrefix() {
		return filePrefix;
	}

	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	public String getFileTemplate() {
		return fileTemplate;
	}

	public void setFileTemplate(String fileTemplate) {
		this.fileTemplate = fileTemplate;
	}

	public boolean isSaveHdfAttributes() {
		return saveHdfAttributes;
	}

	public void setSaveHdfAttributes(boolean saveHdfAttributes) {
		this.saveHdfAttributes = saveHdfAttributes;
	}
}
