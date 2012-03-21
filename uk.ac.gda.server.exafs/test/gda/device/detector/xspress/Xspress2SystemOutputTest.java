/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.xspress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.DUMMY_XSPRESS2_MODE;
import gda.device.detector.DummyDAServer;
import gda.device.timer.Etfg;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.util.TestUtils;
import gda.util.exceptionUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the ascii and nexus output of the Xspress2 in its various modes of operation and output options
 * 
 * TODO test nexus, what if more than one ROI?
 */
public class Xspress2SystemOutputTest {

	
	private static Xspress2System xspress = new Xspress2System();
	final static String TestFileFolder = "test/gda/device/detector/xspress/TestFiles/";
	static String testScratchDirectoryName = null;

	/**
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(Xspress2SystemTest.class
				.getCanonicalName());
		try {
			TestUtils.makeScratchDirectory(testScratchDirectoryName);
		} catch (Exception e) {
			fail(exceptionUtils.getFullStackMsg(e));
		}

		DummyDAServer daserver = new DummyDAServer();
		daserver.setName("DummyDAServer");
		daserver.setXspressMode(DUMMY_XSPRESS2_MODE.XSPRESS2_FULL_MCA);
		daserver.connect();
		daserver.setNonRandomTestData(true);
		Etfg tfg = new Etfg();
		tfg.setName("tfg");
		try {
			xspress.setDaServer(daserver);
			xspress.setTfg(tfg);
			xspress.setConfigFileName(TestFileFolder + "/xspressConfig2.xml");
			xspress.setDtcConfigFileName(TestFileFolder + "/Xspress_DeadTime_Parameters.xml");
			xspress.setName("xspressTest");
			xspress.setDaServerName("DummyDAServer");
			xspress.setTfgName("tfg");
			xspress.setMcaOpenCommand("xspress open-mca");
			xspress.setScalerOpenCommand("xspress open-scalers");
			xspress.setStartupScript("xspress2 format-run 'xsp1' res-none");
			xspress.setXspressSystemName("xsp1");
			xspress.setFullMCABits(8);
			xspress.configure();
		} catch (DeviceException e) {
			fail(e.getMessage());
		} catch (FactoryException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Clear out the Finder as this is a singleton instance.
	 */
	@AfterClass
	public static void clearUpAfterClass() {
		Finder.getInstance().clear();
	}
	
	@Test 
	public void testExtraNames(){
		xspress.setAddDTScalerValuesToAscii(false);
		xspress.setOnlyDisplayFF(true);
		assertEquals(1,xspress.getExtraNames().length);
		xspress.setOnlyDisplayFF(false);
		assertEquals(10,xspress.getExtraNames().length);
		xspress.setOnlyDisplayFF(false);
		xspress.setAddDTScalerValuesToAscii(true);
		assertEquals((4*9+9+1),xspress.getExtraNames().length);
	}
	
	@Test
	public void testScalersOnly(){
		try {
			// configure
			xspress.setReadoutMode(XspressDetector.READOUT_SCALERONLY);
			xspress.setAddDTScalerValuesToAscii(false);
			xspress.setOnlyDisplayFF(false);
			// run a collection
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			// readout
			NexusTreeProvider results = xspress.readout();
			String asciiData = results.toString();
			String[] asciiDataParts = asciiData.split("\t");
			// test ascii
			assertEquals(10,asciiDataParts.length);
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);
			
			//test when DTC values added
			xspress.setAddDTScalerValuesToAscii(true);
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			results = xspress.readout();
			asciiData = results.toString();
			asciiDataParts = asciiData.split("\t");
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);
			assertEquals((4*9+9+1),asciiDataParts.length);
			
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testScalersAndMCA(){
		try {
			// configure
			xspress.setReadoutMode(XspressDetector.READOUT_MCA);
			xspress.setAddDTScalerValuesToAscii(false);
			xspress.setOnlyDisplayFF(false);
			// run a collection
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			// readout
			NexusTreeProvider results = xspress.readout();
			String asciiData = results.toString();
			String[] asciiDataParts = asciiData.split("\t");
			// test ascii
			assertEquals(10,asciiDataParts.length);
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);
			
			//test when DTC values added
			xspress.setAddDTScalerValuesToAscii(true);
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			results = xspress.readout();
			asciiData = results.toString();
			asciiDataParts = asciiData.split("\t");
			assertEquals((4*9+9+1),asciiDataParts.length);
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);
			
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testResGradeNONE(){
		try {
			// configure
			xspress.setReadoutMode(XspressDetector.READOUT_ROIS);
			xspress.setResGrade(ResGrades.NONE);
			xspress.setAddDTScalerValuesToAscii(false);
			xspress.setOnlyDisplayFF(false);
			// run a collection
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			// readout
			NexusTreeProvider results = xspress.readout();
			String asciiData = results.toString();
			String[] asciiDataParts = asciiData.split("\t");
			// test ascii
			assertEquals(10,asciiDataParts.length);
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);
			
			//test when DTC values added
			xspress.setAddDTScalerValuesToAscii(true);
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			results = xspress.readout();
			asciiData = results.toString();
			asciiDataParts = asciiData.split("\t");
			assertEquals((4*9+9+1),asciiDataParts.length);
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);
			
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testResGradeThreshold(){
		try {
			// configure
			xspress.setReadoutMode(XspressDetector.READOUT_ROIS);
			xspress.setResGrade(ResGrades.THRESHOLD + " 1.0");
			xspress.setAddDTScalerValuesToAscii(false);
			xspress.setOnlyDisplayFF(false);
			// run a collection
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			// readout
			NexusTreeProvider results = xspress.readout();
			String asciiData = results.toString();
			String[] asciiDataParts = asciiData.split("\t");
			// test ascii
			assertEquals(11,asciiDataParts.length);
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);
			
			//test when DTC values added
			xspress.setAddDTScalerValuesToAscii(true);
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			results = xspress.readout();
			asciiData = results.toString();
			asciiDataParts = asciiData.split("\t");
			assertEquals((4*9+9+1 + 1),asciiDataParts.length);  // 4 *  numElements + good for each element + FF + FF_bad
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);
			
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testResGradeALL(){
		try {
			// configure
			xspress.setReadoutMode(XspressDetector.READOUT_ROIS);
			xspress.setResGrade(ResGrades.ALLGRADES);
			xspress.setAddDTScalerValuesToAscii(false);
			xspress.setOnlyDisplayFF(true);
			// run a collection
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			// readout
			NexusTreeProvider results = xspress.readout();
			String asciiData = results.toString();
			String[] asciiDataParts = asciiData.split("\t");
			// test ascii
			assertEquals(1,asciiDataParts.length);
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);

			//test when all values added
			xspress.setOnlyDisplayFF(false);
			xspress.setAddDTScalerValuesToAscii(false);
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			results = xspress.readout();
			asciiData = results.toString();
			asciiDataParts = asciiData.split("\t");
			assertEquals((16 +9+1),asciiDataParts.length);  // 16 resGrade bins, 9 'best grades', FF
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);

			//test when DTC values added
			xspress.setOnlyDisplayFF(false);
			xspress.setAddDTScalerValuesToAscii(true);
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			results = xspress.readout();
			asciiData = results.toString();
			asciiDataParts = asciiData.split("\t");
			assertEquals((4*9+16 +9+1),asciiDataParts.length); // 4*numElements, 16 resGrade bins, 'best grades' per element ,FF
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);
			
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}



}
