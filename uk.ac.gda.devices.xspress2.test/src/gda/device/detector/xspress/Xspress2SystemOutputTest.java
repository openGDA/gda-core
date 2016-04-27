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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.DUMMY_XSPRESS2_MODE;
import gda.device.detector.DummyDAServer;
import gda.device.detector.xspress.xspress2data.Xspress2DAServerController;
import gda.device.timer.Etfg;
import gda.factory.FactoryException;
import gda.factory.Finder;
import uk.ac.gda.beans.xspress.ResGrades;
import uk.ac.gda.beans.xspress.XspressDetector;

/**
 * Tests the ascii and nexus output of the Xspress2 in its various modes of operation and output options
 *
 * TODO test nexus, what if more than one ROI?
 */
public class Xspress2SystemOutputTest {


	private static Xspress2Detector xspress = new Xspress2Detector();
	private static Xspress2DAServerController controller;
	private static DummyDAServer daserver;
	final static String TestFileFolder = "testfiles/gda/device/detector/xspress/";

	private static final int NUM_ENABLED_ELEMENTS = 8;
	private static final int SIZE_SCALER_DATA = NUM_ENABLED_ELEMENTS + 1; // for FF

	@BeforeClass
	public static void setUpBeforeClass() {
		xspress = new Xspress2Detector();
		controller = new Xspress2DAServerController();
		xspress.setController(controller);

		daserver = new DummyDAServer();
		daserver.setName("DummyDAServer");
		daserver.setXspressMode(DUMMY_XSPRESS2_MODE.XSPRESS2_FULL_MCA);
		daserver.connect();
		daserver.setNonRandomTestData(true);
		Etfg tfg = new Etfg();
		tfg.setName("tfg");

		try {
			controller.setDaServer(daserver);
			controller.setTfg(tfg);
			xspress.setConfigFileName(TestFileFolder + "xspressConfig.xml");
			xspress.setDtcConfigFileName(TestFileFolder + "Xspress_DeadTime_Parameters.xml");
			xspress.setName("xspressTest");
			controller.setDaServer(daserver);
			controller.setTfg(tfg);
			controller.setMcaOpenCommand("xspress open-mca");
			controller.setScalerOpenCommand("xspress open-scalers");
			controller.setStartupScript("xspress2 format-run 'xsp1' res-none");
			controller.setXspressSystemName("xsp1");
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
		Finder.getInstance().removeAllFactories();
	}

	@Test
	public void testExtraNames() throws DeviceException{
		xspress.setReadoutMode(XspressDetector.READOUT_SCALERONLY);
		xspress.setAddDTScalerValuesToAscii(false);
		xspress.setOnlyDisplayFF(true);
		assertEquals(1,xspress.getExtraNames().length);
		xspress.setOnlyDisplayFF(false);
		String[] extraNames = xspress.getExtraNames();
		assertEquals(SIZE_SCALER_DATA,extraNames.length);
		xspress.setOnlyDisplayFF(false);
		xspress.setAddDTScalerValuesToAscii(true);
		assertEquals((4*NUM_ENABLED_ELEMENTS+SIZE_SCALER_DATA),xspress.getExtraNames().length);
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
			assertEquals(SIZE_SCALER_DATA,asciiDataParts.length);
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);

			//test when DTC values added
			xspress.setAddDTScalerValuesToAscii(true);
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			results = xspress.readout();
			asciiData = results.toString();
			asciiDataParts = asciiData.split("\t");
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);
			assertEquals((4*NUM_ENABLED_ELEMENTS+SIZE_SCALER_DATA),asciiDataParts.length);

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
			assertEquals(SIZE_SCALER_DATA,asciiDataParts.length);
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);
			// make sure we are not sending zeroes
			assertFalse(asciiDataParts[SIZE_SCALER_DATA - 3].compareTo("0") == 0);

			//test when DTC values added
			xspress.setAddDTScalerValuesToAscii(true);
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			results = xspress.readout();
			asciiData = results.toString();
			asciiDataParts = asciiData.split("\t");
			assertEquals((4*NUM_ENABLED_ELEMENTS+SIZE_SCALER_DATA),asciiDataParts.length);
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
			assertEquals(SIZE_SCALER_DATA,asciiDataParts.length);
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);

			//test when DTC values added
			xspress.setAddDTScalerValuesToAscii(true);
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			results = xspress.readout();
			asciiData = results.toString();
			asciiDataParts = asciiData.split("\t");
			String[] extraNames = xspress.getExtraNames();
			assertEquals(extraNames.length,asciiDataParts.length);
			assertEquals((NUM_ENABLED_ELEMENTS + 1 + 4 * NUM_ENABLED_ELEMENTS),asciiDataParts.length);

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
			assertEquals(SIZE_SCALER_DATA + 1,asciiDataParts.length);  // +1 for ff_bad
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);

			//test when DTC values added
			xspress.setAddDTScalerValuesToAscii(true);
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			results = xspress.readout();
			asciiData = results.toString();
			asciiDataParts = asciiData.split("\t");
			assertEquals((NUM_ENABLED_ELEMENTS + 4*NUM_ENABLED_ELEMENTS+1 + 1),asciiDataParts.length);  // 4 *  numElements + good for each element + FF + FF_bad
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
			assertEquals((16 +NUM_ENABLED_ELEMENTS+1),asciiDataParts.length);  // 16 resGrade bins, 9 'best grades', FF
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);

			//test when DTC values added
			xspress.setOnlyDisplayFF(false);
			xspress.setAddDTScalerValuesToAscii(true);
			xspress.setCollectionTime(0.05);
			xspress.collectData();
			results = xspress.readout();
			asciiData = results.toString();
			asciiDataParts = asciiData.split("\t");
			assertEquals((4*NUM_ENABLED_ELEMENTS+16 +NUM_ENABLED_ELEMENTS+1),asciiDataParts.length); // 4*numElements, 16 resGrade bins, 'best grades' per element ,FF
			assertEquals(xspress.getExtraNames().length,asciiDataParts.length);

		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}



}
