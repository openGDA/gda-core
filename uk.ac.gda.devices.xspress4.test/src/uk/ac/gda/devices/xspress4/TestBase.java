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

package uk.ac.gda.devices.xspress4;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.detector.DummyDAServer;
import gda.device.motor.DummyMotor;
import gda.device.scannable.ScannableMotor;
import gda.device.timer.Tfg;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.DummyXspress3Controller;
import uk.ac.gda.devices.detector.xspress4.DummyXspress4Controller;
import uk.ac.gda.devices.detector.xspress4.Xspress4Detector;
import uk.ac.gda.devices.xspress4.HelperClasses.CheckType;

public class TestBase {

	private static Logger logger = LoggerFactory.getLogger(TestBase.class);

	protected Xspress4Detector xspress4detector;
	protected DummyXspress3Controller xsp3controller;
	protected Tfg tfg;
	protected DummyDAServer daserver;
	protected ScannableMotor dummyScannableMotor;
	protected int numElements = 8;
	protected String ROI_NAME = "ROI_1";
	protected String ELEMENT_NAME = "Element";
	protected String simulationFileName = "/scratch/dummyHdfFile.h5";
	protected XspressParameters xspressParams;
	protected DummyXspress4Controller xsp4Controller;

	protected void setupDetectorObjects() throws FactoryException {
		daserver = new DummyDAServer();
		daserver.setName("daserver");
		daserver.configure();

		tfg = new Tfg();
		tfg.setName("tfg");
		tfg.setDaServer(daserver);
		tfg.configure();

		xsp3controller = new DummyXspress3Controller(tfg, daserver);
		xsp3controller.setName("controllerForDetector");
		xsp3controller.setNumFramesToAcquire(1);
		xsp3controller.setNumberOfChannels(numElements); //number of detector elements
		xsp3controller.configure();
		xsp3controller.setSimulationFileName(simulationFileName);

		xsp4Controller = new DummyXspress4Controller();
		xsp4Controller.setNumElements(xsp3controller.getNumberOfChannels());
		xsp4Controller.setNumMcaChannels(128);
		xsp4Controller.setNumScalers(8);

		xspress4detector = new Xspress4Detector();
		xspress4detector.setName("xspress4detector");
		xspress4detector.setXspress3Controller(xsp3controller);
		xspress4detector.setController(xsp4Controller);
		xspress4detector.setWriteHDF5Files(false);
		xspress4detector.configure();
	}

	protected void setupMotor() throws Exception {
		DummyMotor dummyMotor = new DummyMotor() ;
		dummyMotor.setName("dummyMotor");
		dummyMotor.setMaxPosition(10000);
		dummyMotor.setMinPosition(-1000);
		dummyMotor.setSpeed(10000);
		dummyMotor.configure();

		dummyScannableMotor = new ScannableMotor();
		dummyScannableMotor.setName("dummyScannableMotor");
		dummyScannableMotor.setMotor(dummyMotor);
		dummyScannableMotor.configure();
		dummyScannableMotor.moveTo(0);
	}

	protected void setupLocalProperties() {
		LocalProperties.setScanSetsScanNumber(true);
		LocalProperties.set("gda.scan.sets.scannumber", "true");
		LocalProperties.set("gda.scanbase.firstScanNumber", "-1");
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "XasAsciiNexusDataWriter");
		LocalProperties.set("gda.nexus.createSRS", "true");

		// This will be set by TestHelpers.setUpTest(), but need something set to keep numtracker happy
		LocalProperties.set(LocalProperties.GDA_VAR_DIR, "/scratch/Data");
	}

	protected void setupEnvironment() {
		// Findables the server needs to know about
		Findable[] findables = new Findable[] { xspress4detector, xsp3controller, dummyScannableMotor };

		final Factory factory = TestHelpers.createTestFactory("test");
		for (Findable f : findables) {
			factory.addFindable(f);
			InterfaceProvider.getJythonNamespace().placeInJythonNamespace(f.getName(), f);
		}

		// Need to add objectfactory to Finder if using
		// Finder.getInstance().find(...) to get at scannables.
		Finder.getInstance().addFactory(factory);
	}

	protected void setupForTest(Class<?> classType, String testName) throws Exception {
		setupLocalProperties();
		/* String testFolder = */TestHelpers.setUpTest(classType, testName, true);
		setupEnvironment();
	}

	protected XspressParameters getParameters(String readoutMode, String resGrade, int windowStart, int windowEnd) {
		XspressParameters parameters = new XspressParameters();
		parameters.setReadoutMode(readoutMode);
		parameters.setResGrade(resGrade);
		for(int i = 0; i<numElements; i++) {
			parameters.addDetectorElement(getDetectorElement(ELEMENT_NAME+(i+1), i+1, windowStart, windowEnd));
		}
		return parameters;
	}

	private DetectorElement getDetectorElement(String name, int number, int windowStart, int windowEnd){
		DetectorElement detElement = new DetectorElement();
		detElement.setName(name);;
		detElement.setNumber(number);
		detElement.setWindow(windowStart, windowEnd);
		detElement.addRegion( new DetectorROI(ROI_NAME, windowStart, windowEnd));
		detElement.setExcluded(false);
		return detElement;
	}

	public static IDataset getDataset(String nexusFilename, String groupName, String dataName) throws NexusException {
		try(NexusFile file = NexusFileHDF5.openNexusFileReadOnly(nexusFilename)) {
			GroupNode group = file.getGroup(groupName, false);
			DataNode d = file.getData(group, dataName);
			return d.getDataset().getSlice(null, null, null);
		}catch(NexusException | DatasetException e){
			String msg = "Problem opening nexus data group="+groupName+" data="+dataName;
			throw new NexusException(msg+e);
		}
	}

	private void checkDataset(String filename, String datasetName, int[] expectedShape) throws NexusException {
		checkDatasets(filename, Arrays.asList(datasetName), expectedShape);
	}

	/**
	 * Check shape of dataset in xspress4detector group, and ensure all values are all >= 0
	 * @param filename
	 * @param datasetNames
	 * @param expectedShape
	 * @throws NexusException
	 */
	private void checkDatasets(String filename, List<String> datasetNames, int[] expectedShape) throws NexusException {
		String detectorName = xspress4detector.getName();
		for(String datasetName : datasetNames) {
			logger.info("Checking dataset : /entry1/{}/{} matches expected shape {}", detectorName, datasetName, expectedShape);
			IDataset dataset = HelperClasses.getDataset(filename, detectorName, datasetName);
			HelperClasses.checkDatasetShape(dataset, expectedShape);
			// check values are all >= 0
			HelperClasses.checkDatasetMinValue(dataset, CheckType.GREATER_OR_EQUAL_TO, 0);
		}
	}

	/**
	 * Check Nexus data for scalers and MCA readout mode
	 * @param filename
	 * @throws NexusException
	 */
	protected void checkNexusScalersAndMca(String filename) throws NexusException {
		// get number of scan points from /entry1/scan_dimensions (should be 1-dimensional for these tests...)
		IDataset scanDims = HelperClasses.getDataset(filename, "", "scan_dimensions");
		int numScanPoints = scanDims.getInt(0);
		int[] expectedShape = { numScanPoints, xspress4detector.getNumberOfElements() };

		List<String> datasetNames = new ArrayList<>();
		datasetNames.addAll(xspress4detector.getScalerNameIndexMap().keySet());
		datasetNames.add("dtc factors");
		datasetNames.add("scalers");
		checkDatasets(filename, datasetNames, expectedShape);

		// Check FF dataset (one value per scan point)
		checkDataset(filename, "FF", new int[]{numScanPoints});
	}

	/**
	 * Check Nexus data for 'all resolution grade' readout mode.
	 * @param filename
	 * @throws NexusException
	 */
	protected void checkNexusRoiAllResGrade(String filename) throws NexusException {
		IDataset scanDims = HelperClasses.getDataset(filename, "", "scan_dimensions");
		int numScanPoints = scanDims.getInt(0);
		int[] expectedShape = { numScanPoints, xspress4detector.getNumberOfElements() };

		List<String> datasetNames = new ArrayList<>();
		datasetNames.addAll(xspress4detector.getScalerNameIndexMap().keySet());
		datasetNames.add("dtc factors");
		checkDatasets(filename, datasetNames, expectedShape);

		// Check FF dataset (one value per scan point)
		checkDataset(filename, "FF", new int[]{numScanPoints});

		// check resgrade data for ROI_1
		checkDataset(filename, ROI_NAME+"_resgrade", new int[]{numScanPoints, xspress4detector.getNumberOfElements(), 16});
	}

	/**
	 * Check Nexus data for 'no resolution grade' readout mode.
	 * @param filename
	 * @throws NexusException
	 */
	protected void checkNexusRoiNoGrade(String filename) throws NexusException {
		IDataset scanDims = HelperClasses.getDataset(filename, "", "scan_dimensions");
		int numScanPoints = scanDims.getInt(0);
		int[] expectedShape = { numScanPoints, xspress4detector.getNumberOfElements() };

		List<String> datasetNames = new ArrayList<>();
		datasetNames.addAll(xspress4detector.getScalerNameIndexMap().keySet());
		datasetNames.add("dtc factors");
		checkDatasets(filename, datasetNames, expectedShape);

		// Check FF dataset (one value per scan point)
		checkDataset(filename, "FF", new int[] { numScanPoints });

		// In-window counts summed over all resgrades
		checkDataset(filename, "FF_" + ROI_NAME, new int[] { numScanPoints, xspress4detector.getNumberOfElements() });
	}

	/**
	 * Check Nexus data for 'resolution grade threshold' readout mode.
	 * @param filename
	 * @throws NexusException
	 */
	protected void checkNexusRoiThreshold(String filename) throws NexusException {
		IDataset scanDims = HelperClasses.getDataset(filename, "", "scan_dimensions");
		int numScanPoints = scanDims.getInt(0);
		int[] expectedShape = { numScanPoints, xspress4detector.getNumberOfElements() };

		List<String> datasetNames = new ArrayList<>();
		datasetNames.addAll(xspress4detector.getScalerNameIndexMap().keySet());
		datasetNames.add("dtc factors");

		// in-window counts above ('good') and below ('bad')the resolution grade threshold (for each element)
		datasetNames.add("good_counts");
		datasetNames.add("bad_counts");

		checkDatasets(filename, datasetNames, expectedShape);
	}

	/**
	 * Add deadtime correction column names (for each element)
	 * @param header
	 */
	private void addDtcColumns(List<String> header) {
		for (DetectorElement el : xspressParams.getDetectorList()) {
			if (!el.isExcluded()) {
				for (String val : xspress4detector.getAsciiScalerNameIndexMap().keySet()) {
					header.add(el.getName() + "_" + val);
				}
			}
		}
	}

	/**
	 * Add element column names
	 * @param header
	 */
	private void addElementNameColumns(List<String> header) {
		for(DetectorElement el : xspressParams.getDetectorList()) {
			if (!el.isExcluded()) {
				header.add(el.getName());
			}
		}
	}

	/**
	 * Add element ROI column names
	 * @param header
	 */
	private void addElementRoiNameColumns(List<String> header) {
		for(DetectorElement el : xspressParams.getDetectorList()) {
			if (!el.isExcluded()) {
				header.add(el.getName()+"_"+ROI_NAME);
			}
		}
	}

	/**
	 * Check header string in Ascii file matches expected header
	 * @param asciiFileContents
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void checkAsciiHeader(List<String[]> asciiFileContents, List<String> expectedHeader) throws FileNotFoundException, IOException {
		for(int i=0;i<asciiFileContents.size(); i++) {
			if (asciiFileContents.get(i)[0].equals(dummyScannableMotor.getName())) {
				assertArrayEquals(expectedHeader.toArray(new String[]{}), asciiFileContents.get(i));
				return;
			}
		}
	}

	/**
	 * Check Ascii file contents: correct number of columns in all rows; numbers all >= 0
	 * @param asciiFileContents
	 */
	private void checkAsciiFileColumns(List<String[]> asciiFileContents) {
		int numColumns = 0;
		for(String[] lineWords : asciiFileContents) {
			if (lineWords[0].equals(dummyScannableMotor.getName())) {
				numColumns =  lineWords.length;
			} else if (numColumns > 0) {
				// same number of numbers as in the header
				assertEquals(numColumns, lineWords.length);

				// all numbers > 0
				for(int i=0; i<lineWords.length; i++) {
					assertTrue(Double.parseDouble(lineWords[i]) >= 0);
				}
			}
		}
	}

	/**
	 * Check Ascii data for 'scalers and MCA' readout mode.
	 * @param asciiFilename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void checkAsciiScalersAndMca(String asciiFilename) throws FileNotFoundException, IOException {
		List<String> expectedHeader = new ArrayList<>();
		expectedHeader.add(dummyScannableMotor.getName());
		if (!xspressParams.isOnlyShowFF()) {
			addElementNameColumns(expectedHeader);
		}
		expectedHeader.add("FF");
		if (xspressParams.isShowDTRawValues()) {
			addDtcColumns(expectedHeader);
		}

		List<String[]> asciiFile = HelperClasses.readAsciiFile(asciiFilename);
		checkAsciiHeader(asciiFile, expectedHeader);
		checkAsciiFileColumns(asciiFile);
	}

	/**
	 * Check Ascii data for 'all resolution grade' readout mode
	 * @param asciiFilename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void checkAsciiRoiAllResGrade(String asciiFilename) throws FileNotFoundException, IOException {
		List<String> expectedHeader = new ArrayList<>();
		expectedHeader.add(dummyScannableMotor.getName());
		if (!xspressParams.isOnlyShowFF()) {
			// res_bin_#_norm , #=0 ... 15
			for(int i=0; i<16; i++) {
				expectedHeader.add("res_bin_"+i+"_norm");
			}
			for(DetectorElement el : xspressParams.getDetectorList()) {
				expectedHeader.add(el.getName()+"_best8");
			}
		}
		expectedHeader.add("FF");
		if (xspressParams.isShowDTRawValues()) {
			addDtcColumns(expectedHeader);
		}

		List<String[]> asciiFile = HelperClasses.readAsciiFile(asciiFilename);
		checkAsciiHeader(asciiFile, expectedHeader);
		checkAsciiFileColumns(asciiFile);
	}

	/**
	 * Check Ascii data for 'no resolution grade' readout mode.
	 * @param asciiFilename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void checkAsciiRoiNoGrade(String asciiFilename) throws FileNotFoundException, IOException {
		List<String> expectedHeader = new ArrayList<>();
		expectedHeader.add(dummyScannableMotor.getName());
		addElementRoiNameColumns(expectedHeader);
		expectedHeader.add("FF");
		if (xspressParams.isShowDTRawValues()) {
			addDtcColumns(expectedHeader);
		}

		List<String[]> asciiFile = HelperClasses.readAsciiFile(asciiFilename);
		checkAsciiHeader(asciiFile, expectedHeader);
		checkAsciiFileColumns(asciiFile);

	}

	/**
	 * Check Ascii data for 'resolution grade threshold' readout mode.
	 * @param asciiFilename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void checkAsciiRoiThreshold(String asciiFilename) throws FileNotFoundException, IOException {
		List<String> expectedHeader = new ArrayList<>();
		expectedHeader.add(dummyScannableMotor.getName());
		if (!xspressParams.isOnlyShowFF()) {
			addElementRoiNameColumns(expectedHeader);
		}
		expectedHeader.add("FF");
		expectedHeader.add("FF_bad");
		if (xspressParams.isShowDTRawValues()) {
			addDtcColumns(expectedHeader);
		}

		List<String[]> asciiFile = HelperClasses.readAsciiFile(asciiFilename);
		checkAsciiHeader(asciiFile, expectedHeader);
		checkAsciiFileColumns(asciiFile);
	}

	protected String getAsciiNameFromNexusName(String nexusFilename) {
		return nexusFilename.replace(".nxs", ".dat");
	}
}
