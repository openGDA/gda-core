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

import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.countertimer.BufferedScaler;
import gda.device.memory.Scaler;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.scan.ContinuousScan;
import uk.ac.gda.beans.xspress.ResGrades;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.devices.detector.xspress4.Xspress4BufferedDetector;
import uk.ac.gda.devices.detector.xspress4.XspressHdfWriter;
import uk.ac.gda.server.exafs.epics.device.scannable.QexafsTestingScannable;

public class Xspress4BufferedDetectorTest extends TestBase {

	private static final Logger logger = LoggerFactory.getLogger(Xspress4BufferedDetectorTest.class);
	private BufferedScaler bufferedScaler;
	private QexafsTestingScannable qexafsScannable;
	private Xspress4BufferedDetector bufDetector;

	@Before
	public void setup() throws Exception {
		setupLocalProperties();
		setupMotor();
		setupDetectorObjects();
		setupBufferedScaler();
		setupQexafsScannable();
		setupXspress4();
	}

	private void setupXspress4() {
		bufDetector = new Xspress4BufferedDetector();
		bufDetector.setName("xspress4BufferedDetector");
		bufDetector.setXspress4Detector(xspress4detector);
		bufDetector.setUseSwmrFileReading(true);
		bufDetector.setWriteHDF5Files(true);
	}

	private void setupBufferedScaler() throws DeviceException, FactoryException {
		Scaler memory = new Scaler();
		memory.setName("memory");
		memory.setDaServer(daserver);
		memory.setHeight(1);
		memory.setWidth(3);
		memory.setOpenCommand("tfg open-cc");
		memory.configure();

		bufferedScaler = new BufferedScaler();
		bufferedScaler.setName("qexafs_counterTimer01");
		bufferedScaler.setDaserver(daserver);

		bufferedScaler.setScaler(memory);
		bufferedScaler.setTimer(tfg);
		bufferedScaler.setTFGv2(true);
		bufferedScaler.setOutputLogValues(false);
		bufferedScaler.setTimeChannelRequired(true);
		bufferedScaler.setExtraNames(new String[] { "I0", "It" });
		bufferedScaler.setFirstDataChannel(0);
		bufferedScaler.setNumChannelsToRead(2);
		bufferedScaler.setOutputFormat(new String[] { "%.5g", "%9d", "%9d" });
		bufferedScaler.setDarkCurrentRequired(false); // set to false, or otherwise need to also setup shutter
		bufferedScaler.configure();
	}

	private void setupQexafsScannable() throws MotorException, Exception {
		Motor dummyMotor = dummyScannableMotor.getMotor();
		qexafsScannable = new QexafsTestingScannable();
		qexafsScannable.setName("qexafsScannable");
		qexafsScannable.setMotor(dummyMotor);
		qexafsScannable.setLowerGdaLimits(dummyMotor.getMinPosition());
		qexafsScannable.setUpperGdaLimits(dummyMotor.getMaxPosition());
		qexafsScannable.setOutputFormat(new String[]{"%.4f"});
	}

	@After
	public void tearDown() {
		// Remove factories from Finder so they do not affect other tests
		Finder.getInstance().removeAllFactories();
	}

	// @Test
	public void runBufferedScalerScan() throws InterruptedException, Exception {
		/* String testFolder = */TestHelpers.setUpTest(Xspress4BufferedDetectorTest.class, "runBufferedScalerScan", true);

		ContinuousScan scan = new ContinuousScan(qexafsScannable, 0.0, 10.0, 10, 1.0, new BufferedDetector[] {bufferedScaler});
		scan.runScan();
	}

	@Test(timeout=10000)
	public void runBufferedXspress4ScanWithSwmr() throws InterruptedException, Exception {
		setupForTest(Xspress4BufferedDetectorTest.class, "runBufferedXspress4ScanWithSwmr");

		String testFolder = LocalProperties.get(LocalProperties.GDA_DATA);
		String hdfFile = Paths.get(testFolder, "xspress4DetectorFile.hdf").toAbsolutePath().toString();
		int numFrames = 1000;
		double timeForScan = 1;

		bufDetector.setUseSwmrFileReading(true);
		bufDetector.setWriteHDF5Files(true);
		xsp3controller.setSimulationFileName(hdfFile);
		xsp4Controller.setTotalNumFramesAvailable(numFrames);

		xspressParams = getParameters(XspressParameters.READOUT_MODE_SCALERS_AND_MCA, ResGrades.NONE, 1, 101);
		bufDetector.applyConfigurationParameters(xspressParams);

		XspressHdfWriter writer = new XspressHdfWriter();
		writer.setFileName(hdfFile);
		writer.setNumFrames(numFrames);
		writer.setNumElements(xsp4Controller.getNumElements());
		writer.setNumScalers(xsp4Controller.getNumScalers());
		writer.setDefaultNames();
		writer.setTimePerFrame(timeForScan/numFrames);
		writer.setFileName(xsp3controller.getFullFileName());
		writer.writeData();
//		writer.writeHdfFile();
		while(writer.getCurrentFrameNumber() < 5) {
			Thread.sleep(250);
		}

		ContinuousScan scan = new ContinuousScan(qexafsScannable, 0.0, 10.0, numFrames, timeForScan, new BufferedDetector[] {bufDetector});
		scan.runScan();
		writer.waitWhileBusy();
		String filename = scan.getDataWriter().getCurrentFileName();
		checkNexusScalersAndMca(filename);
		checkAsciiScalersAndMca(getAsciiNameFromNexusName(filename));
	}

	@Test(timeout=10000)
	public void runBufferedXspress4ScanNoSwmr() throws InterruptedException, Exception {
		setupForTest(Xspress4BufferedDetectorTest.class, "runBufferedXspress4ScanNoSwmr");

		String testFolder = LocalProperties.get(LocalProperties.GDA_DATA);
		String hdfFile = Paths.get(testFolder, "xspress4DetectorFile.hdf").toAbsolutePath().toString();
		int numFrames = 1000;
		double timeForScan = 1;

		bufDetector.setUseSwmrFileReading(false);
		bufDetector.setWriteHDF5Files(false);
		xsp3controller.setSimulationFileName(hdfFile);
		xsp4Controller.setTotalNumFramesAvailable(numFrames);

		xspressParams = getParameters(XspressParameters.READOUT_MODE_SCALERS_AND_MCA, ResGrades.NONE, 1, 101);
		bufDetector.applyConfigurationParameters(xspressParams);
		xsp4Controller.setTimeSeriesNumPoints(numFrames);

		ContinuousScan scan = new ContinuousScan(qexafsScannable, 0.0, 10.0, numFrames, timeForScan, new BufferedDetector[] {bufDetector});
		scan.runScan();

		String filename = scan.getDataWriter().getCurrentFileName();
		checkNexusScalersAndMca(filename);
		checkAsciiScalersAndMca(getAsciiNameFromNexusName(filename));
	}

	@Test(timeout=10000)
	public void runBufferedXspress4ScanNoSwmrTreeWriter() throws InterruptedException, Exception {
		setupForTest(Xspress4BufferedDetectorTest.class, "runBufferedXspress4ScanNoSwmrTreeWriter");

		String testFolder = LocalProperties.get(LocalProperties.GDA_DATA);
		String hdfFile = Paths.get(testFolder, "xspress4DetectorFile.hdf").toAbsolutePath().toString();
		int numFrames = 10000;
		double timeForScan = 1;

		bufDetector.setUseSwmrFileReading(false);
		bufDetector.setWriteHDF5Files(false);
		bufDetector.setUseNexusTreeWriter(true);
		bufDetector.setMaximumReadFrames(1000);
		bufDetector.setDetectorNexusFilename(hdfFile);
		xsp3controller.setSimulationFileName(hdfFile);
		xsp4Controller.setTotalNumFramesAvailable(numFrames);

		xspressParams = getParameters(XspressParameters.READOUT_MODE_SCALERS_AND_MCA, ResGrades.NONE, 1, 101);
		bufDetector.applyConfigurationParameters(xspressParams);
		xsp4Controller.setTimeSeriesNumPoints(numFrames);

		long startTime = System.currentTimeMillis();
		ContinuousScan scan = new ContinuousScan(qexafsScannable, 0.0, 10.0, numFrames, timeForScan, new BufferedDetector[] {bufDetector});
		scan.runScan();
		long endTime = System.currentTimeMillis();
		logger.info("Scan took {} ms to run", System.currentTimeMillis() - startTime);
		String filename = scan.getDataWriter().getCurrentFileName();
		checkNexusScalersAndMca(filename);
		checkAsciiScalersAndMca(getAsciiNameFromNexusName(filename));
	}
}
