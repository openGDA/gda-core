/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.epics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gda.factory.FactoryException;
import gda.util.TestUtils;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("2013/03/07 Test ignored since it requires a resource that is not available")
public class CAClientTest {
	static boolean skipBL11test = false;
	boolean putCompleted = false;
	String pvName = "BL11I-AL-SLITS-02:Y:PLUS";
	private String[] pvNames = { "BL11I-AL-SLITS-02:Y:PLUS", "BL11I-AL-SLITS-02:Y:MINUS" };
	TestPutListener listener = new TestPutListener();

	class TestPutListener implements PutListener {

		@Override
		public void putCompleted(PutEvent arg0) {
			putCompleted = true;
		}

	}

	@BeforeClass
	public static void setUpBeforeClass() {
		System.setProperty("gov.aps.jca.JCALibrary.properties", "src/gda/epics/JCALibrary.properties");
		// determine if we are on a test machine that cannot reach the BL11 IOCs used in testing
		String hostname;
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch (UnknownHostException e) {
			return;
		}
		if (hostname.startsWith("p99-")) {
			skipBL11test = true;
		} else if (hostname.startsWith("pc003.sc")) {
			skipBL11test = true;
		} else if (hostname.startsWith("pc070.sc")) {
			skipBL11test = true;
		}
	}

	@Test
	public void testCagetString() throws CAException, TimeoutException, InterruptedException, FactoryException {
		TestUtils.skipTestIf(skipBL11test,
				this.getClass().getCanonicalName() + ".testCagetString skipped, since test IOC not available");

		CAClient cac = new CAClient();
		assertNotNull(cac);
		cac.configure();
		double expected = 1.0;
		putCompleted = false;
		cac.caput(pvName, expected, listener);
		while (!putCompleted) {
			Thread.sleep(10);
		}
		String actual = cac.caget(pvName);
		cac.clearup();
		assertEquals(expected, Double.parseDouble(actual), 0.01);
	}

	@Test
	public void testGetElementCountString() throws CAException, FactoryException {
		TestUtils.skipTestIf(skipBL11test,
				this.getClass().getCanonicalName() + ".testGetElementCountString skipped, since test IOC not available");

		CAClient cac = new CAClient();
		assertNotNull(cac);
		cac.configure();
		int expected = 1;
		int actual = cac.getElementCount(pvName);
		cac.clearup();
		assertEquals(expected, actual);
	}

	@Test
	@Ignore
	public void testCagetWithTimeStampString() throws CAException, TimeoutException, InterruptedException, FactoryException {
		CAClient cac = new CAClient();
		assertNotNull(cac);
		cac.configure();
		double value = 1.0;
		putCompleted = false;
		cac.caput(pvName, value, listener);
		while (!putCompleted) {
			Thread.sleep(10);
		}
		SimpleDateFormat formatter = new SimpleDateFormat("MMM d,yyyy hh:mm:ss");
		Date today = new Date();
		String result = formatter.format(today);
		String[] expected = { pvName, result, String.valueOf(value) };
		String[] actual = cac.cagetWithTimeStamp(pvName);
		cac.clearup();
		// System.out.println(actual[0]+" "+actual[1] +" " +actual[2]+"\n" + result);
		assertEquals(expected[0], actual[0]);
		assertEquals(expected[2], actual[2]);
		assertTrue(contains(actual[1], result));
	}

	// Returns true if the string ret contains string exp
	private boolean contains(String ret, String exp) {
		return ret.contains(exp);
	}

	@Test
	public void testCagetStringArray() throws CAException, TimeoutException, InterruptedException, FactoryException {
		TestUtils.skipTestIf(skipBL11test,
				this.getClass().getCanonicalName() + ".testCagetStringArray skipped, since test IOC not available");

		CAClient cac = new CAClient();
		assertNotNull(cac);
		cac.configure();
		double[] expected = { 1.0, 1.0 };
		putCompleted = false;
		cac.caput(pvNames, expected, listener);
		while (!putCompleted) {
			Thread.sleep(10);
		}
		String[] actual = cac.caget(pvNames);
		cac.clearup();
		double[] results = { Double.parseDouble(actual[0]), Double.parseDouble(actual[1]) };
		assertEquals(expected[0], results[0], 0.01);
		assertEquals(expected[1], results[1], 0.01);
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCagetArrayString() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringInt() throws CAException, InterruptedException, TimeoutException, FactoryException {
		CAClient cac = new CAClient();
		assertNotNull(cac);
		cac.configure();
		int expected = 1;
		putCompleted = false;
		cac.caput(pvName, expected, listener);
		while (!putCompleted) {
			Thread.sleep(10);
		}
		String actual = cac.caget(pvName);
		cac.clearup();
		assertEquals(expected, Long.parseLong(actual));

	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringShort() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringFloat() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringDouble() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringString() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringByteArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringIntArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringShortArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringFloatArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringDoubleArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringByteArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringIntArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringShortArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringFloatArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringDoubleArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringArrayDoubleArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringArrayByteArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringArrayStringArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringDoubleDouble() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringDoublePutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringStringDouble() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringStringPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringArrayDoubleArrayDouble() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringArrayDoubleArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringArrayStringArrayDouble() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringArrayStringArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringAsWaveformStringString() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringAsWaveformStringStringPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCamonitorMonitorListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCamonitor() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testRemoveMonitor() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaget() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCagetMonitorType() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCagetWithTimeStamp() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCagetArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCagetArrayDouble() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCagetArrayByteInt() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCagetArrayByte() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCagetArrayDoubleInt() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCagetMax() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCagetPeakPosition() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCagetAllChannels() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDouble() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputInt() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputShort() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputFloat() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputByte() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputString() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputIntArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputShortArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputFloatArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputByteArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleDouble() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoublePutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleInt() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputIntPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleShort() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputShortPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleByte() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputBytePutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleFloat() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputFloatPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleString() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleDoubleArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleIntArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputIntArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleShortArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputShortArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleFloatArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputFloatArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleByteArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputByteArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputDoubleStringArray() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringArrayPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringAsWaveformString() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testCaputStringAsWaveformStringPutListener() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testClearup() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testGetElementCount() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore("Test not yet implemented")
	public void testMonitorChanged() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLoop() throws FactoryException, CAException, TimeoutException, InterruptedException {
		TestUtils.skipTestIf(skipBL11test,
				this.getClass().getCanonicalName() + ".testGetLoop skipped, since test IOC not available");
		
		CAClient cac = new CAClient(pvName);
		assertNotNull(cac);
		try {
			for (int i = 0; i < 10000; i++) {
				cac.configure();
				cac.caget();
				cac.clearup();
			}
		} catch (FactoryException e) {
			assertTrue(false);
			throw e;
		} catch (CAException e) {
			assertTrue(false);
			throw e;
		} catch (TimeoutException e) {
			assertTrue(false);
			throw e;
		}
	}

	@Test
	public void testGetLoop2() throws FactoryException, CAException, TimeoutException, InterruptedException {
		TestUtils.skipTestIf(skipBL11test,
				this.getClass().getCanonicalName() + ".testGetLoop2 skipped, since test IOC not available");

		CAClient cac = new CAClient(pvNames[1]);
		assertNotNull(cac);
		try {
			for (int i = 0; i < 1000; i++) {
				cac.configure();
				cac.caget();
				cac.clearup();
			}
		} catch (FactoryException e) {
			assertTrue(false);
			throw e;
		} catch (CAException e) {
			assertTrue(false);
			throw e;
		} catch (TimeoutException e) {
			assertTrue(false);
			throw e;
		}
	}
}
