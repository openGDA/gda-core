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

import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import uk.ac.gda.devices.detector.xspress3.CAPTURE_MODE;
import uk.ac.gda.devices.detector.xspress3.ReadyForNextRow;
import uk.ac.gda.devices.detector.xspress3.TRIGGER_MODE;
import uk.ac.gda.devices.detector.xspress3.UPDATE_CTRL;
import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;

/* This class is used for testing Xspress3 v2 and will replace in the long run Xspress3ScanOperations. A new class was needed in order not to interfere with
 * other beamlines that are using Xspress3.
 */

public class Xspress3ScanOperationsv2 {

	private Xspress3Controller controller;
	private int currentScanNumber;
	private int[] currentDimensions;
	private String detectorName;
	private boolean readDataFromFile;
	private int lengthOfEachScanLine;
	private int lineNumber;
	private static final int MONITOR_FILE_TIMEOUT = 60000;
	// Chunk size is typically 1 MB
	private static final int CHUNK_SIZE = 1024 * 1024;
	// Data Type is Float64 so take 8 bytes
	private static final int DATA_TYPE_SIZE = 8;

	public Xspress3ScanOperationsv2(Xspress3Controller controller, String detectorName) {
		this.controller = controller;
		this.detectorName = detectorName;
		this.lineNumber = 0;
	}

	public void atScanStart(boolean readDataFromFile) throws DeviceException {
		this.readDataFromFile = readDataFromFile;
		ScanInformation currentscan = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		currentScanNumber = currentscan.getScanNumber();
		currentDimensions = currentscan.getDimensions();

		int numDimensions = currentscan.getDimensions().length;
		lengthOfEachScanLine = currentscan.getDimensions()[numDimensions - 1];
		controller.doStop();
		// to improve performance start acquisition at the start of the map and then wait for triggers
		// to be more generic replace by the total size of the scan
		int totalNumberOfPointsInScan = currentscan.getNumberOfPoints();
		setNumberOfFramesToCollect(totalNumberOfPointsInScan);
		// When restarting EPICs IOC the trigger mode is set by default to Internal and the scan fails.
		// In future if needed instead of being hardcoded can give the choice to user to choose the trigger mode.
		controller.setTriggerMode(TRIGGER_MODE.TTl_Veto_Only);

		if (readDataFromFile) {
			prepareFileWriting(currentDimensions);
		}
		lineNumber = 0;
		clearAndStart();
	}

	public void atScanLineStart() throws DeviceException {
		ReadyForNextRow isReadyForNextRow;
		lineNumber++;
		if (lineNumber > 1) {
			savingHDFFiles();
		}
		isReadyForNextRow = controller.monitorReadyForNextRow(ReadyForNextRow.YES);
		if (isReadyForNextRow != ReadyForNextRow.YES) {
			throw new DeviceException("Xspress3 buffered not cleared for next row!");
		}
	}

	public void atScanEnd() throws DeviceException {
		controller.doStop();
		controller.setSavingFiles(false);
		currentScanNumber = -1;
	}

	/*
	 * Must be called after currentScanNumber updated SR it seems that from scanBase the prepareForCollection for detectors is called after prepareScanNumber
	 */
	private void prepareFileWriting(int[] numDimensions) throws DeviceException {
		// make sure that the callback is enable before to start a scan otherwise no data can be saved in the HDF5 file.
		controller.setFileEnableCallBacks(UPDATE_CTRL.Enable);
		// make sure that the Capture Mode is et to Stream otherwise the scan file
		controller.setFileCaptureMode(CAPTURE_MODE.Stream);
		// make sure that it sets otherwise if Array counter is always adding and not resetting EPICs could failed
		controller.setFileArrayCounter(0);
		String scanNumber = Long.toString(currentScanNumber);
		// /dls/iXX/20XX/cm1234-5/tmp/xspress3/12345/0.hdf
		String filePath = PathConstructor.createFromRCPProperties();
		filePath += "tmp" + File.separator + detectorName + File.separator + scanNumber;
		File filePathTester = new File(filePath);
		if (!filePathTester.exists()) {
			filePathTester.mkdirs();
		}
		// make sure that the NDAttribute is off
		//
		controller.setFilePath(filePath);
		controller.setNextFileNumber(0);
		controller.setHDFAttributes(false);
		controller.setHDFPerformance(false);

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
		controller.doStart();
	}

	private void savingHDFFiles() throws DeviceException {
		// we suppose that each line has the same number of points, does not need to set again the number of frames in the file writer
		controller.setSavingFiles(readDataFromFile);
		// check if the EPICS HDF file writer is ready
		if (readDataFromFile) {
			long startTime = System.currentTimeMillis();
			long timeForFileWriterToBePrepared = 0L;
			while (!controller.isSavingFiles()) {
				timeForFileWriterToBePrepared = System.currentTimeMillis() - startTime;
				if (timeForFileWriterToBePrepared > MONITOR_FILE_TIMEOUT)
					throw new DeviceException("Timeout monitoring Xspress3 CAPTURE_RBV PV.");
			}
		}
	}
}
