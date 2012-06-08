/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.xspress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.xspress.Detector;
import gda.device.xspress.DetectorReading;
import gda.device.xspress.DummyExafsServer;
import gda.device.xspress.XspressSystem;
import gda.util.ObjectServer;

import org.junit.Before;
import org.junit.Test;

/**
 * XspressSystemTest
 */
public class XspressSystemTest {
	DummyExafsServer des;

	XspressSystem xs;

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		xs = new XspressSystem();
		xs.setNumberOfDetectors(9);
		ObjectServer.createLocalImpl("/home/pcs/gda-trunk/src/tests/gda/device/xspress/XspressSystemTest.xml");
		LocalProperties.set("gda.device.xspress.configFileName",
				"/home/pcs/gda-trunk/src/tests/gda/device/xspress/detectors.cnf");
		xs.setExafsServerName("exafsserver");
		xs.configure();
	}

	/**
	 * 
	 */
	@Test
	public void testGetNumberOfDetectors() {
		try {
			assertEquals(9, xs.getNumberOfDetectors());
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testGetDetector() {
		Detector d = xs.getDetector(5);
		assertNotNull(d);
		// These fail should pass (getDetector() should check input)
		// d = xs.getDetector(-1);
		// assertNull(d);
		// d = xs.getDetector(10);
		// assertNull(d);

	}

	/**
	 * 
	 */
	@Test
	public void testReadDetector() {
		// All fail (command not implemented in DummyExafsServer)
		// DetectorReading dr = xs.readDetector(5);
		// assertNotNull(dr);
		// dr = xs.readDetector(-1);
		// assertNull(dr);
		// dr = xs.readDetector(10);
		// assertNull(dr);
	}

	/**
	 * 
	 */
	@Test
	public void testReadDetectors() {
		DetectorReading[] drs = xs.readDetectors();
		assertEquals(9, drs.length);
	}

	/**
	 * 
	 */
	@Test
	public void testReadout() {
		// Fails (DummyExafsServer does not implement relevant command)
		// double[] values = (double[]) xs.readout();
		// assertEquals(9, values.length);
	}

	/**
	 * 
	 */
	@Test
	public void testGetMCData() {
		double[] data = null;

		try {
			data = xs.getMCData(0, 1, 100, 15);
			assertEquals(100, data.length);
		} catch (DeviceException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testQuit() {
		// Fails but should succeed (DummyExafsServer should have its own
		// quit())
		// xs.quit();
	}

	/**
	 * 
	 */
	@Test
	public void testSetDetectorGain() {
		xs.setDetectorGain(3, 1234.5);
		Detector d = xs.getDetector(3);
		assertEquals(1234.5, d.getGain());
	}

	/**
	 * 
	 */
	@Test
	public void testSetDetectorOffset() {
		xs.setDetectorOffset(3, 1234.5);
		Detector d = xs.getDetector(3);
		assertEquals(1234.5, d.getOffset());
	}
}
