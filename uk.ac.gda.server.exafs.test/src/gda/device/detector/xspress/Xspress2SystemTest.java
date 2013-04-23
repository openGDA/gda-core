/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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
import gda.device.DeviceException;
import gda.device.detector.DUMMY_XSPRESS2_MODE;
import gda.device.detector.DummyDAServer;
import gda.device.timer.Etfg;
import gda.factory.FactoryException;
import gda.util.TestUtils;
import gda.util.exceptionUtils;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.beans.xspress.DetectorDeadTimeElement;
import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.beans.xspress.XspressROI;

/**
 * Test the Xspress2System class against DummyDAServer. So this is more of a test of DummyDAServer really.
 */
public class Xspress2SystemTest {
	private Xspress2System xspress;
	private static String testScratchDirectoryName;
	final String TestFileFolder = "testfiles/gda/device/detector/xspress/";

	@BeforeClass
	public static void setUpBeforeClass() {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(Xspress2SystemTest.class
				.getCanonicalName());
		try {
			TestUtils.makeScratchDirectory(testScratchDirectoryName);
		} catch (Exception e) {
			fail(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Before
	public void setUpEachTest() {
		xspress = new Xspress2System();
		
		DummyDAServer daserver = new DummyDAServer();
		daserver.setName("DummyDAServer");
		daserver.setXspressMode(DUMMY_XSPRESS2_MODE.XSPRESS2_FULL_MCA);
		daserver.connect();
		daserver.setNonRandomTestData(true);
		Etfg tfg = new Etfg();
		tfg.setName("tfg");
		try {
			xspress.setNumberOfDetectors(9);
			xspress.setDaServer(daserver);
			xspress.setTfg(tfg);
			xspress.setConfigFileName(TestFileFolder + "xspressConfig.xml");
			xspress.setDtcConfigFileName(TestFileFolder + "Xspress_DeadTime_Parameters.xml");
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
	 * Test method for {@link gda.device.detector.xspress.Xspress2System#configure()}.
	 */
	@Test
	public void testConfigure() {
		XspressROI roi = new XspressROI("1st_peak", 100, 1122);
		ArrayList<XspressROI> regions = new ArrayList<XspressROI>();
		regions.add(roi);
		DetectorElement[] expected = {
				new DetectorElement("Element0", 0, 0, 4000,  false, regions),
				new DetectorElement("Element1", 1, 85, 2047,  false, regions),
				new DetectorElement("Element2", 2, 34, 2439,  false, regions),
				new DetectorElement("Element3", 3, 31, 2126,  false, regions),
				new DetectorElement("Element4", 4, 0, 4000, true, regions),
				new DetectorElement("Element5", 5, 85, 2047,  false, regions),
				new DetectorElement("Element6", 6, 34, 2439, false, regions),
				new DetectorElement("Element7", 7, 31, 2126, false, regions),
				new DetectorElement("Element8", 8, 31, 2126, false, regions) };
		DetectorDeadTimeElement[] expectedDT = {
				new DetectorDeadTimeElement("Element0", 0,3.4E-7, 0.0, 3.4E-7),
				new DetectorDeadTimeElement("Element1", 1,3.8E-7, 0.0, 3.8E-7),
				new DetectorDeadTimeElement("Element2", 2,3.7E-7, 0.0, 3.7E-7),
				new DetectorDeadTimeElement("Element3", 3,3.0E-7, 0.0, 3.0E-7),
				new DetectorDeadTimeElement("Element4", 4,3.4E-7, 0.0, 3.4E-7),
				new DetectorDeadTimeElement("Element5", 5,3.5E-7, 0.0, 3.5E-7),
				new DetectorDeadTimeElement("Element6", 6, 3.3E-7, 0.0, 3.3E-7),
				new DetectorDeadTimeElement("Element7", 7,3.0E-7, 0.0, 3.0E-7),
				new DetectorDeadTimeElement("Element8", 8, 3.3E-7, 0.0, 3.3E-7)};
		
		for (int i = 0; i < expected.length; i++) {
			try {
				DetectorElement xspressElement = xspress.getDetector(i);
				if (!expected[i].equals(xspressElement))
					fail("Values read are incorrect - " + xspress.getDetector(i).toString());
				DetectorDeadTimeElement xspressDTElement = xspress.getDeadTimeParameters().getDetectorDT(i);
				if (!expectedDT[i].equals(xspressDTElement))
					fail("Values read are incorrect - " + xspress.getDeadTimeParameters().getDetectorDT(i).toString());
			} catch (DeviceException e) {
				fail("Device Exception " + e.getMessage());
			}
		}
	}

	/**
	 * Test method for {@link gda.device.detector.xspress.Xspress2System#clear()}.
	 */
	@Test
	public void testClear() {
		try {
			xspress.clear();
			xspress.close();
			xspress.clear();
		} catch (DeviceException e) {
			fail("DeviceException should not happen");
		}
	}

	/**
	 * Test method for {@link gda.device.detector.xspress.Xspress2System#clear()}.
	 * 
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testClearException() throws DeviceException {
		xspress.setFail();
		xspress.clear();
	}

	/**
	 * Test method for {@link gda.device.detector.xspress.Xspress2System#start()}.
	 */
	@Test
	public void testStart() {
		try {
			xspress.start();
			xspress.close();
			xspress.start();
		} catch (DeviceException e) {
			fail("DeviceException should not happen");
		}
	}

	/**
	 * Test method for {@link gda.device.detector.xspress.Xspress2System#start()}.
	 * 
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testStartException() throws DeviceException {
		xspress.setFail();
		xspress.start();
	}

	/**
	 * Test method for {@link gda.device.detector.xspress.Xspress2System#stop()}.
	 */
	
	//disabled test so that b18 can run scans without memory clear failures.
	
//	@Test
//	public void testStop() {
//		try {
//			xspress.stop();
//			xspress.close();
//			xspress.stop();
//		} catch (DeviceException e) {
//			fail("DeviceException should not happen");
//		}
//	}

	/**
	 * Test method for {@link gda.device.detector.xspress.Xspress2System#stop()}.
	 * 
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testStopException() throws DeviceException {
		xspress.setFail();
		xspress.stop();
	}

	/**
	 * Test method for {@link gda.device.detector.xspress.Xspress2System#saveDetectors(java.lang.String)}.
	 */
	@Test
	public void testSaveDetectors() {
		xspress.saveDetectors(testScratchDirectoryName + "xspressConfig-saved.xml");
		junitx.framework.FileAssert.assertEquals(new File(TestFileFolder + "xspressConfig.xml"), 
				new File(testScratchDirectoryName + "xspressConfig-saved.xml"));
	}
	
	/**
	 * Test method for {@link gda.device.detector.xspress.Xspress2System#stop()}.
	 */
	@Test
	public void testStop() {
		try {
			xspress.stop();
			xspress.close();
			xspress.stop();
		} catch (DeviceException e) {
			fail("DeviceException should not happen");
		}
	}
	
	/**
	 * Test method for {@link gda.device.detector.xspress.Xspress2System#setResGrade(String)}.
	 */
	@Test
	public void testSetResGrade() {
		try {
			xspress.setResGrade("res-min-div-8");
			assertEquals("res-min-div-8", xspress.getResGrade());
			xspress.setResGrade("res-none");
			assertEquals("res-none", xspress.getResGrade());
			xspress.setResGrade("res-thres 10.0");
			assertEquals("res-thres 10.0", xspress.getResGrade());
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link gda.device.detector.xspress.Xspress2System#getMCData(int)}.
	 */
	@Test
	public void testGetMCData() {
		try {
			int[][][] data = xspress.getMCData(1000);
			assertEquals(9, data.length);
			assertEquals(4096, data[0][0].length);
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetChannelLabels() {
		try {
			ArrayList<String> labels = xspress.getChannelLabels();
			assertEquals("Element0", labels.get(0));
			xspress.setReadoutMode(XspressDetector.READOUT_SCALERONLY);
			labels = xspress.getChannelLabels();
			assertEquals("Element0", labels.get(0));
			xspress.setReadoutMode(XspressDetector.READOUT_ROIS);
			labels = xspress.getChannelLabels();
			assertEquals("Element6_1st_peak", labels.get(6));
			xspress.setReadoutMode(XspressDetector.READOUT_MCA);
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testBitSize() {
		assertEquals(0, xspress.getMaxNumberOfFrames());  // 0 when ran against dummy daserver
		assertEquals(8, xspress.getFullMCABits());
		assertEquals(256, xspress.getCurrentMCASize());
		try {
			ArrayList<XspressROI> regionList = new ArrayList<XspressROI>();
			XspressROI roi = new XspressROI("roi1", 50, 100);
			XspressROI roi2 = new XspressROI("roi2", 150, 174);
			regionList.add(roi);
			regionList.add(roi2);
			for (int i = 0; i < xspress.numberOfDetectors; i++) {
				xspress.setRegionOfInterest(i, regionList);
			}
			xspress.setReadoutMode(XspressDetector.READOUT_ROIS);
			xspress.setResGrade(ResGrades.NONE);

			assertEquals(2, xspress.getCurrentMCABits());
			assertEquals(4, xspress.getCurrentMCASize());
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}
}
