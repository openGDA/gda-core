package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

import java.io.File;

import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;

public class Xspress3ScanOperations {

	private Xspress3Controller controller;
	private int currentScanNumber;
	private int[] currentDimensions;
	private String detectorName;
	private boolean readDataFromFile;

	public Xspress3ScanOperations(Xspress3Controller controller, String detectorName) {
		this.controller = controller;
		this.detectorName = detectorName;
	}

	public void atScanStart(boolean readDataFromFile) throws DeviceException {
		this.readDataFromFile = readDataFromFile;
		ScanInformation currentscan = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		currentScanNumber = currentscan.getScanNumber();
		currentDimensions = currentscan.getDimensions();

		int numDimensions = currentscan.getDimensions().length;
		int lengthOfEachScanLine = currentscan.getDimensions()[numDimensions - 1];
		setNumberOfFramesToCollect(lengthOfEachScanLine);
		controller.doStop();
		
		if (!readDataFromFile) {
			prepareFileWriting(currentDimensions);
		}

	}

	public void atScanLineStart() throws DeviceException {
		controller.doErase();
		controller.doStart();
		controller.setSavingFiles(readDataFromFile);
	}

	public void atScanEnd() throws DeviceException {
		currentScanNumber = -1;
	}

	/*
	 * Must be called after currentScanNumber updated
	 */
	private void prepareFileWriting(int[] numDimensions) throws DeviceException {

		String scanNumber = Long.toString(currentScanNumber);

		// /dls/iXX/20XX/cm1234-5/tmp/xspress3/12345/0.hdf
		String filePath = PathConstructor.createFromRCPProperties();
		filePath += "tmp" + File.separator + detectorName + File.separator + scanNumber;
		File filePathTester = new File(filePath);
		if (!filePathTester.exists()) {
			filePathTester.mkdirs();
		}

		controller.setFilePath(filePath);
		controller.setNextFileNumber(0);
		controller.setHDFFileAutoIncrement(true);
		controller.setHDFNumFramesToAcquire(currentDimensions[currentDimensions.length - 1]);
	}

	private void setNumberOfFramesToCollect(int numberOfFramesToCollect) throws DeviceException {
		controller.setNumFramesToAcquire(numberOfFramesToCollect);
	}
}
