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
import static org.junit.Assert.assertNotNull;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.january.dataset.IDataset;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gda.TestHelpers;
import gda.data.nexus.tree.INexusTree;
import gda.data.swmr.SwmrFileReader;
import gda.device.detector.NXDetectorData;
import uk.ac.gda.beans.xspress.ResGrades;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.devices.detector.xspress4.Xspress4BufferedDetector;
import uk.ac.gda.devices.detector.xspress4.Xspress4NexusTree;
import uk.ac.gda.devices.detector.xspress4.XspressHdfWriter;

public class XspressHdfWriterTest extends TestBase {

	@Before
	public void setup() throws Exception {
		setupLocalProperties();
	}

	/**
	 * Test XspressHdfWriter produces swmr file in background thread, and that SwmrFileReader can
	 * read it, reporting latest frame number written by interrogating datasets in the hdf file.
	 * @throws Exception
	 */
	@Ignore("Run locally to test simultaneous file writing and reading are working correctly.")
	public void testWriter() throws Exception {
		String testFolder = TestHelpers.setUpTest(XspressHdfWriter.class, "testWriter", true);
		String hdfFile = Paths.get(testFolder, "xspress4DetectorFile.hdf").toAbsolutePath().toString();

		XspressHdfWriter writer = new XspressHdfWriter();
		writer.setDefaultNames();
		writer.setFileName(hdfFile);
		writer.setNumFrames(5);
		writer.setNumFramesToAppendEachTime(1);
		writer.writeData();

		// Wait a bit for file to be created
		Thread.sleep(1000);

		// Open Hdf file using Swmr file read :
		SwmrFileReader fileReader = new SwmrFileReader();
		fileReader.openFile(writer.getFileName());

		// Setup reader with names of datasets to be read and their path
		writer.getDatasetNames()
			.stream()
			.forEach(name -> fileReader.addDatasetToRead(name, Paths.get(writer.getPathToGroup(), name).toString()));

		System.out.print("busy = "+writer.isBusy());
		while (writer.isBusy()) {
			System.out.println("Current frame = " + writer.getCurrentFrameNumber());
			System.out.println("Num. available frames = " + fileReader.getNumAvailableFrames());

			Thread.sleep(1000);
		}
		fileReader.releaseFile();
	}

	@Test
	public void testHdfWriter() throws Exception {
		String testFolder = TestHelpers.setUpTest(XspressHdfWriter.class, "testHdfWriter", true);
		String hdfFile = Paths.get(testFolder, "xspress4DetectorFile.hdf").toAbsolutePath().toString();

		// Write Hdf file of data
		int numFrames = 100;
		int numElements = 6;
		int numScalers = 8;

		XspressHdfWriter writer = new XspressHdfWriter();
		writer.setNumElements(numElements);
		writer.setNumScalers(numScalers);
		writer.setNumFrames(numFrames);
		writer.setDefaultNames();

		assertEquals(writer.getDatasetNames().size(), numElements*numScalers);

		writer.setTimePerFrame(0);
		writer.setFileName(hdfFile);
		writer.writeHdfFile();

		int[] expectedShape = { numFrames };
		for(String dataset : writer.getDatasetNames()) {
			IDataset d = getDataset(writer.getFileName(), writer.getPathToGroup(), dataset);
			assertNotNull("Problem getting dataset "+dataset+" from file", d);
			assertArrayEquals(dataset+" does not match expected shape", expectedShape, d.getShape());
		}
	}

	@Test
	public void testNexusTree() throws Exception {
		String testFolder = TestHelpers.setUpTest(XspressHdfWriter.class, "testNexusTree", true);
		String hdfFile = Paths.get(testFolder, "xspress4DetectorFile.hdf").toAbsolutePath().toString();

		setupDetectorObjects();
		int numFrames = 50;
		numElements = 36;
		xsp4Controller.setNumElements(numElements);
		xsp4Controller.setNumScalers(8);
		XspressParameters parameters = getParameters(XspressParameters.READOUT_MODE_SCALERS_AND_MCA, ResGrades.NONE, 1, 101);
		xspress4detector.applyConfigurationParameters(parameters);

		// Write Hdf file of data
		XspressHdfWriter writer = new XspressHdfWriter();
		writer.setNumElements(xspress4detector.getController().getNumElements());
		writer.setNumScalers(xspress4detector.getController().getNumScalers());
		writer.setNumFrames(numFrames);
		writer.setDefaultNames();
		writer.setTimePerFrame(0);
		writer.setFileName(hdfFile);
		writer.writeHdfFile();

		// Open Hdf file using Swmr file reader
		SwmrFileReader fileReader = new SwmrFileReader();
		fileReader.openFile(writer.getFileName());
		// Set full path to the scaler datasets
		writer.getDatasetNames().stream()
			.forEach(name -> fileReader.addDatasetToRead(name, Paths.get(writer.getPathToGroup(), name).toString()));


		// Xspress4NexusTree uses Xspress4BufferedDetector to get the names of the datasets to be read from the hdf file
		Xspress4BufferedDetector xspress4BufferedDetector = new Xspress4BufferedDetector();
		xspress4BufferedDetector.setName("xspress4BufferedDetector");
		xspress4BufferedDetector.setXspress4Detector(xspress4detector);

		// Generate NXDetectorData containing the detector data.
		Xspress4NexusTree nexusTree = new Xspress4NexusTree(xspress4BufferedDetector);
		nexusTree.setSwmrFileReader(fileReader);
		NXDetectorData[] detectorData = nexusTree.getDetectorData(0, numFrames-1);

		// Names of scaler datasets to be checked.
		List<String> scalerDataNames = new ArrayList<String>();
		scalerDataNames.addAll(xspress4detector.getScalerNameIndexMap().keySet());
		scalerDataNames.add("dtc factors");

		// check nexus tree to make sure data was extracted correctly
		assertEquals(detectorData.length, numFrames);

		// Check the datasets have been stored in Nexus tree and have correct shape
		for(int i=0; i<numFrames; i++) {
			INexusTree detTree = detectorData[i].getNexusTree().getNode(xspress4detector.getName());
			for(String name : scalerDataNames) {
				assertNotNull(detTree.getNode(name));
				IDataset dataset = detTree.getNode(name).getData().toDataset();
				assertEquals(dataset.getShape()[0], xspress4detector.getNumberOfElements());
			}
			assertNotNull(detTree.getNode("FF"));
			assertEquals(detTree.getNode("FF").getData().toDataset().getShape()[0], 1);
		}
	}
}
