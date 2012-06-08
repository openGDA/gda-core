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

package gda.device.detector.areadetector.v17;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import gda.device.detector.areadetector.v17.impl.ADBaseImpl;
import gda.util.TestUtils;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for ADBase
 */
@Ignore("2011/08/04 Test ignored since it requires resources that are not generally available")
public class ADBaseImplTest {

	private ADBaseImpl adbase;

	/**
	 * Skip entire test class if PV used for testing is not reachable from this machine
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		String hostname;
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch (UnknownHostException e) {
			return;
		}
		if (hostname.startsWith("p99-ws100") || hostname.startsWith("ws018")) {
			TestUtils.skipTest(
				"NDFileTest requires access to PV EXCALIBUR:NODE, which does not appear to be accessible from this machine (" + hostname + ")");
		}
	}

	@Before
	public void setup() {
		adbase = new ADBaseImpl();
		adbase.setBasePVName("EXCALIBUR:NODE:");
	}

	@Test
	public void testSetColorMode() throws Exception {
		adbase.setColorMode((short) 4);
		short colorMode = adbase.getColorMode();
		assertEquals(colorMode, 4);
	}

	@Test
	public void testSetBinX() throws Exception {
		adbase.setBinX(5);
		assertEquals(adbase.getBinX(), 5);

	}

	@Test
	public void testSetBinY() throws Exception {
		adbase.setBinY(5);
		assertEquals(adbase.getBinY(), 5);
	}

	@Test
	public void testSetReverseX() throws Exception {
		adbase.setReverseX((short) 1);
		assertEquals(adbase.getReverseX(), 1);
		assertEquals(adbase.getReverseX_RBV(), 1);
	}

	@Test
	public void testSetReverseY() throws Exception {
		adbase.setReverseY((short) 1);
		assertEquals(adbase.getReverseY(), 1);
		assertEquals(adbase.getReverseY_RBV(), 1);
	}

	@Test
	public void testSetAcquirePeriod() throws Exception {
		adbase.setAcquirePeriod(15);
		assertEquals(15, adbase.getAcquirePeriod(), 0);
		assertEquals(15, adbase.getAcquirePeriod_RBV(), 0);
	}

	@Test
	public void testSetFrameType() throws Exception {
		adbase.setFrameType((short) 2);
		assertEquals(2, adbase.getFrameType());
		assertEquals(2, adbase.getFrameType_RBV());
	}

	@Test
	public void testSetImageMode() throws Exception {
		adbase.setImageMode((short) 2);
		assertEquals(2, adbase.getImageMode());
		assertEquals(2, adbase.getImageMode_RBV());
	}

	@Test
	public void testSetTriggerMode() throws Exception {
		adbase.setTriggerMode((short) 1);
		assertEquals(1, adbase.getTriggerMode());
		assertEquals(1, adbase.getTriggerMode_RBV());
	}

	@Test
	public void testStartAcquiring() throws Exception {
		adbase.startAcquiring();
		assertEquals("1", adbase.getAcquire_RBV());
	}

	@Test
	public void testStopAcquiring() throws Exception {
		adbase.stopAcquiring();
		Thread.sleep(500);		// allow time for stopAcquiring to complete
		assertEquals("0", adbase.getAcquire_RBV());
	}

	@Test
	public void testSetArrayCounter() throws Exception {
		adbase.setArrayCounter(6);
		assertEquals(6, adbase.getArrayCounter());
		assertEquals(6, adbase.getArrayCounter_RBV());

	}

	@Test
	public void testSetArrayCallbacks() throws Exception {
		adbase.setArrayCallbacks((short) 1);
		assertEquals(adbase.getArrayCallbacks(), 1);
		assertEquals(adbase.getArrayCallbacks_RBV(), 1);
	}

	@Test
	public void testSetNDAttributesFile() throws Exception {
		adbase.setNDAttributesFile("ndattributesfile.xml");
		assertEquals(adbase.getNDAttributesFile(), "ndattributesfile.xml");
	}

	@Test
	public void testSetReadStatus() throws Exception {
		adbase.setReadStatus((short) 1);
		assertEquals(adbase.getReadStatus(), 1);
	}

	@Test
	public void testSetShutterMode() throws Exception {
		adbase.setShutterMode((short) 1);
		assertEquals(adbase.getShutterMode(), 1);
		assertEquals(adbase.getShutterMode_RBV(), 1);
	}

	@Test
	public void testSetShutterControl() throws Exception {
		adbase.setShutterControl((short) 1);
		assertEquals(adbase.getShutterControl(), 1);
		assertEquals(adbase.getShutterControl_RBV(), 1);

	}

	@Test
	public void testSetShutterOpenDelay() throws Exception {
		adbase.setShutterOpenDelay(12);
		assertEquals(12, adbase.getShutterOpenDelay(), 0);
	}

	@Test
	public void testSetShutterCloseDelay() throws Exception {
		adbase.setShutterCloseDelay(12);
		assertEquals(12, adbase.getShutterCloseDelay(), 0);
	}

	@Test
	public void testSetShutterOpenEPICSPV() throws Exception {
		adbase.setShutterOpenEPICSPV("Open");
		assertEquals("Open NPP NMS", adbase.getShutterOpenEPICSPV());
	}

	@Test
	public void testSetShutterOpenEPICSCmd() throws Exception {
		adbase.setShutterOpenEPICSCmd("OpenCmd");
		assertEquals("OpenCmd", adbase.getShutterOpenEPICSCmd());
	}

	@Test
	public void testSetShutterCloseEPICSPV() throws Exception {
		adbase.setShutterCloseEPICSPV("ClosePV");
		assertEquals("ClosePV NPP NMS", adbase.getShutterCloseEPICSPV());
	}

	@Test
	public void testSetShutterCloseEPICSCmd() throws Exception {
		adbase.setShutterCloseEPICSCmd("ShutterCloseEpicsCmd");
		assertEquals("ShutterCloseEpicsCmd", adbase.getShutterCloseEPICSCmd());
	}


	@Test
	public void testSetTemperature() throws Exception {
		adbase.setTemperature(24);
		assertEquals(24, adbase.getTemperature(), 0);
	}

}
