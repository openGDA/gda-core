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

package gda.device.amplifier;

import gda.device.DeviceException;
import gda.device.amplifier.Keithley;
import gda.factory.Finder;
import junit.framework.TestCase;

/**
 * Test case for Keithley amplifier
 */
public class KeithleyTest extends TestCase {

	/**
	 * 
	 */
	public void testKeithley() {

		TestObjectAssistant toa = new TestObjectAssistant();
		toa.createLocalObjects();
		Keithley keithley = (Keithley) Finder.getInstance().find("keithley");
		// DummyKeithley keithley =
		// (DummyKeithley) Finder.getInstance().find("dummykeithley");
		// Amplifier keithley = (Amplifier)
		// Finder.getInstance().find("keithley");
		try {
			keithley.setZeroCheck(true);
			assertTrue(keithley.getStatus("C"));
			keithley.setZeroCheck(false);
			assertFalse(keithley.getStatus("C"));
			keithley.setEnlargeGain(true);
			assertTrue(keithley.getStatus("W"));
			keithley.setEnlargeGain(false);
			assertFalse(keithley.getStatus("W"));
			keithley.setFilter(true);
			assertTrue(keithley.getStatus("P"));
			keithley.setFilter(false);
			assertFalse(keithley.getStatus("P"));
			keithley.setZeroCheck(true);
			assertTrue(keithley.getStatus("C"));
			keithley.setVoltageBias(true);
			assertTrue(keithley.getStatus("B"));
			keithley.setVoltageBias(false);
			assertFalse(keithley.getStatus("B"));
			// //keithley.sendError();
			keithley.autoZeroCorrect();
			assertTrue(keithley.getStatus("C"));
			keithley.setCurrentSuppress(false);
			assertFalse(keithley.getStatus("N"));
			keithley.setCurrentSuppress(true);
			assertTrue(keithley.getStatus("N"));
			// keithley.autoCurrentSuppress();
			assertEquals("ready", keithley.getStatus());
		} catch (DeviceException e4) {
			System.out.println(e4.getMessage());
		}

		try {
			keithley.setGain(5);
			assertEquals(1E05, keithley.getGain(), 0.0);
			keithley.setGain(10);
			assertEquals(1E10, keithley.getGain(), 0.0);
			keithley.setEnlargeGain(true);
			keithley.setGain(10);
			assertEquals(1E11, keithley.getGain(), 0.0);
			keithley.setGain(20);
			fail();
		} catch (DeviceException e) {
			System.out.println(e.getMessage());
		}
		try {
			keithley.setAutoFilter(true);
			assertTrue(keithley.getStatus("Z"));
			keithley.setAutoFilter(false);
			assertFalse(keithley.getStatus("Z"));
		} catch (DeviceException e5) {
			System.out.println(e5.getMessage());
		}

		try {
			keithley.setFilter(false);
			keithley.setAutoFilter(false);
			keithley.setFilterRiseTime(0);
			assertEquals(1.0E-5, keithley.getFilterRiseTime(), 0.0);
			keithley.setFilterRiseTime(8);
			assertEquals(1.0E-1, keithley.getFilterRiseTime(), 0.0);
			keithley.setFilterRiseTime(9);
			assertEquals(3.0E-1, keithley.getFilterRiseTime(), 0.0);
			keithley.setFilterRiseTime(10);
			fail();
		} catch (DeviceException e1) {
			System.out.println(e1.getMessage());
		}
		try {
			keithley.setVoltageBias(2.5);
			assertEquals(+2.5000, keithley.getVoltageBias(), 0.0);
			keithley.setVoltageBias(00.25);
			assertEquals(+0.2500, keithley.getVoltageBias(), 0.0);
			keithley.setVoltageBias(+5);
			assertEquals(+5.0000, keithley.getVoltageBias(), 0.0);
			keithley.setVoltageBias(-5);
			assertEquals(-5.0000, keithley.getVoltageBias(), 0.0);
			keithley.setVoltageBias(0.75);
			assertEquals(+0.7500, keithley.getVoltageBias(), 0.0);
			keithley.setVoltageBias(-0.0025);
			assertEquals(-0.0025, keithley.getVoltageBias(), 0.0);
			keithley.setVoltageBias(-5.25);
			fail();
		} catch (DeviceException e2) {
			System.out.println(e2.getMessage());
		}
		try {

			keithley.setCurrentSuppressionParams(0.0000001, 3);
			assertEquals(+100.0E-09, keithley.getCurrentSuppressValue(), 0.0);
			assertFalse(keithley.getStatus("S"));
			keithley.setCurrentSuppressionParams(0.0006);
			assertEquals(+0.600E-03, keithley.getCurrentSuppressValue(), 0.0);
			keithley.setCurrentSuppressionParams(0.00061);
			assertEquals(+0.610E-03, keithley.getCurrentSuppressValue(), 0.0);
			keithley.setCurrentSuppressionParams(0.0006011);
			assertEquals(+0.601E-03, keithley.getCurrentSuppressValue(), 0.0002E-03);
			keithley.setCurrentSuppressionParams(-0.000000001, 1);
			assertEquals(-1.000E-09, keithley.getCurrentSuppressValue(), 0.0);
			keithley.setCurrentSuppressionParams(0.0000045, 6);
			assertEquals(+004.5E-06, keithley.getCurrentSuppressValue(), 0.0);
			keithley.setCurrentSuppressionParams(-0.000000006, 1);
			fail();

		} catch (DeviceException e3) {
			System.out.println(e3.getMessage());
		}

		try {
			keithley.getStatus("H");
			fail();
		} catch (DeviceException de) {
			System.out.println(de.getMessage());
		}
	}

}
