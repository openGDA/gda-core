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
import gda.device.gpib.DummyGpib;
import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To change the template for this generated type comment go to Window - Preferences - Java - Code Generation - Code and
 * Comments
 */
public class DummyKeithleyTest extends TestCase {
	private static final Logger logger = LoggerFactory.getLogger(DummyKeithleyTest.class);

	DummyGpib gpib = new DummyGpib();

	Keithley keithley = new Keithley();

	/**
	 * 
	 */
	public void testKeithley() {
		try {
			gpib.findDevice("dummyDevice");
			keithley.autoZeroCorrect();
			assertEquals("C2X", gpib.read("dummyDevice"));
			keithley.setAutoFilter(true);
			assertEquals("Z1X", gpib.read("dummyDevice"));
			keithley.setAutoFilter(false);
			assertEquals("Z0X", gpib.read("dummyDevice"));
			keithley.setEnlargeGain(true);
			assertEquals("W1X", gpib.read("dummyDevice"));
			keithley.setEnlargeGain(false);
			assertEquals("W0X", gpib.read("dummyDevice"));
			keithley.setFilter(true);
			assertEquals("P1X", gpib.read("dummyDevice"));
			keithley.setFilter(false);
			assertEquals("P0X", gpib.read("dummyDevice"));
			keithley.setFilterRiseTime(0);
			assertEquals("T0X", gpib.read("dummyDevice"));
			keithley.setFilterRiseTime(9);
			assertEquals("T9X", gpib.read("dummyDevice"));
			try {
				keithley.setFilterRiseTime(-1);
				fail();
			} catch (DeviceException de) {
			}
			try {
				keithley.setFilterRiseTime(10);
				fail();
			} catch (DeviceException de) {
			}
			keithley.setGain(0);
			assertEquals("R0X", gpib.read("dummyDevice"));
			keithley.setGain(10);
			assertEquals("R10X", gpib.read("dummyDevice"));
			try {
				keithley.setGain(-1);
				fail();
			} catch (DeviceException de) {
			}
			try {
				keithley.setGain(11);
				fail();
			} catch (DeviceException de) {
			}

			keithley.setVoltageBias(true);
			assertEquals("B1X", gpib.read("dummyDevice"));
			keithley.setVoltageBias(false);
			assertEquals("B0X", gpib.read("dummyDevice"));
			keithley.setZeroCheck(true);
			assertEquals("C1X", gpib.read("dummyDevice"));
			keithley.setZeroCheck(false);
			assertEquals("C0X", gpib.read("dummyDevice"));
			keithley.setVoltageBias(true);
			assertEquals("B1X", gpib.read("dummyDevice"));

		} catch (DeviceException e) {
			logger.debug(e.getStackTrace().toString());
		}

		try {
			keithley.setVoltageBias(5.0);
			assertEquals("V5E0X", gpib.read("dummyDevice"));
			keithley.setVoltageBias(-5.0);
			assertEquals("V-5E0X", gpib.read("dummyDevice"));
			keithley.setVoltageBias(0.0025);
			assertEquals("V25E-4X", gpib.read("dummyDevice"));
			keithley.setVoltageBias(-0.01);
			assertEquals("V-100E-4X", gpib.read("dummyDevice"));
			keithley.setVoltageBias(-2.5);
			assertEquals("V-2.5E0X", gpib.read("dummyDevice"));
			keithley.setVoltageBias(-0.225);
			assertEquals("V-2250E-4X", gpib.read("dummyDevice"));
		} catch (DeviceException de) {
			fail();
		}

		try {
			keithley.getGain();
			assertEquals("U3X", gpib.read("dummyDevice"));
			keithley.getVoltageBias();
			assertEquals("U2X", gpib.read("dummyDevice"));

		} catch (DeviceException e) {
			logger.error(e.getMessage());
			logger.debug(e.getStackTrace().toString());
		}

	}

}
