/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector.pco4000;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import gda.util.TestUtils;
import gda.device.DeviceException;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 */
public class PCO4000Test {
	static String testScratchDirectoryName = null;

	PCO4000 pco4000;

	/**
	 * Creates an empty directory for use by test code.
	 * 
	 * @throws Exception if setup fails
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(PCO4000Test.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}	

	/**
	 * Initial test which checks out the basic methods to make the detector read out
	 * 
	 * @throws Exception if the test fails
	 */
	@Test
	public void simpleMainTest() throws Exception {

		try {
			pco4000 = new PCO4000();
		} catch (Exception e) {
			fail("Failed to create instance of PCO4000 class\n" + e);
			return;
		}

		// generate a test PCO to run with
		PCO4000Sim pcoSim = new PCO4000Sim();

		// try to set some information set up the simulator
		try {
			pco4000.setAttribute(PCO4000.SET_HARDWARE, pcoSim);
		} catch (DeviceException e) {
			fail("Failed to set the attribute of the device");
			return;
		}

		// set the aquisition times and the filelocation
		try {
			pco4000.setAttribute(PCO4000.SET_EXPOSURE_TIME, new Double(0.1));
			pco4000.setAttribute(PCO4000.FILENAME, "Test");
			pco4000.setAttribute(PCO4000.PATHNAME, testScratchDirectoryName);
		} catch (DeviceException e) {
			fail("Failed to set exposure time, filename or pathname");
			return;
		}

		// now try to readout the detector
		try {
			pco4000.readout();
		} catch (DeviceException e) {
			fail("Fails to attempt to write out the data");
		}

		// check to see if the detector actualy read out, and produced a file
		File checkfile = new File(testScratchDirectoryName + "Test0000.tif");
		if (!checkfile.exists()) {
			fail("Failed to create file " + testScratchDirectoryName + "Test0000.tif");
		}

	}

	/**
	 * This test simply calls all the Nexus routines which set up the metadata
	 * 
	 * @throws Exception if the test fails
	 */
	@Test
	public void nexusFunctionalityTest() throws Exception {

		try {
			pco4000 = new PCO4000();
		} catch (Exception e) {
			fail("Failed to create instance of PCO4000 class\n" + e);
			return;
		}

		// generate a test PCO to run with
		PCO4000Sim pcoSim = new PCO4000Sim();

		// try to set some informationset up the simulator
		try {
			pco4000.setAttribute(PCO4000.SET_HARDWARE, pcoSim);
		} catch (DeviceException e) {
			fail("Failed to set the attribute of the device");
			return;
		}

		try {
			assertEquals("Dimentionality correct", pco4000.getDataDimensions()[0], 1);
		} catch (DeviceException e) {
			e.printStackTrace();
		}

		try {
			if (pco4000.getDescription().compareTo("PCO4000 14bit CCD detector.") == 1) {
				fail("Description is not as expected");
			}
		} catch (DeviceException e) {
			e.printStackTrace();
		}

		try {
			if (pco4000.getDetectorID().compareTo("PCO4000 Simulator") != 0) {
				fail("Detector ID not as expected");
			}
		} catch (DeviceException e) {
			e.printStackTrace();
		}

		try {
			if (pco4000.getDetectorType().compareTo("CCD") != 0) {
				fail("Detector type is not as is expected");
			}
		} catch (DeviceException e) {
			e.printStackTrace();
		}

		try {
			if (pco4000.createsOwnFiles() == false) {
				fail("this should be true for the moment");
			}
		} catch (DeviceException e) {
			e.printStackTrace();
		}

	}

	/**
	 * This test should go through all the set attribute methods and make sure they return the right errors when the
	 * wrong objects are passed in
	 * 
	 * @throws Exception if the test fails
	 */
	@Test
	public void setValuesTest() throws Exception {

		try {
			pco4000 = new PCO4000();
		} catch (Exception e) {
			fail("Failed to create instance of PCO4000 class\n" + e);
			return;
		}

		// generate a test PCO to run with
		PCO4000Sim pcoSim = new PCO4000Sim();

		// try to set some informationset up the simulator
		try {
			pco4000.setAttribute(PCO4000.SET_HARDWARE, pcoSim);
		} catch (DeviceException e) {
			fail("Failed to set the attribute of the device");
			return;
		}

		// now try to set some values incorrectly
		try {
			pco4000.setAttribute(PCO4000.FILENAME, new Integer(2));
			fail("method should have given an exception");
		} catch (DeviceException e) {
		} catch (Exception e) {
			fail("process gave out the wrong exception");
		}
		try {
			pco4000.setAttribute(PCO4000.PATHNAME, new Double(5.2));
			fail("method should have given an exception");
		} catch (DeviceException e) {
		} catch (Exception e) {
			fail("process gave out the wrong exception");
		}
		try {
			pco4000.setAttribute(PCO4000.SET_COUNT, new String("test"));
			fail("method should have given an exception");
		} catch (DeviceException e) {
		} catch (Exception e) {
			fail("process gave out the wrong exception");
		}
		try {
			pco4000.setAttribute(PCO4000.SET_ROI, new Double(3.4));
			fail("method should have given an exception");
		} catch (DeviceException e) {
		} catch (Exception e) {
			fail("process gave out the wrong exception");
		}
		try {
			pco4000.setAttribute(PCO4000.SET_EXPOSURE_TIME, new Integer(5));
			fail("method should have given an exception");
		} catch (DeviceException e) {
		} catch (Exception e) {
			fail("process gave out the wrong exception");
		}
		try {
			pco4000.setAttribute(PCO4000.SET_DYNAMIC_RANGE, new String("15"));
			fail("method should have given an exception");
		} catch (DeviceException e) {
		} catch (Exception e) {
			fail("process gave out the wrong exception");
		}
		try {
			pco4000.setAttribute(PCO4000.SET_BINNING, new String("Test"));
			fail("method should have given an exception");
		} catch (DeviceException e) {
		} catch (Exception e) {
			fail("process gave out the wrong exception");
		}
		try {
			pco4000.setAttribute(PCO4000.SET_HARDWARE, new Integer(4));
			fail("method should have given an exception");
		} catch (DeviceException e) {
		} catch (Exception e) {
			fail("process gave out the wrong exception");
		}

	}

}
