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

package gda.device.timer;

import static gda.device.timer.Etfg.DEBOUNCE;
import static gda.device.timer.Etfg.THRESHOLD;
import static gda.device.timer.Tfg.AUTO_CONTINUE_ATTR_NAME;
import static gda.device.timer.Tfg.ENDIAN;
import static gda.device.timer.Tfg.EXT_INHIBIT_ATTR_NAME;
import static gda.device.timer.Tfg.EXT_START_ATTR_NAME;
import static gda.device.timer.Tfg.HOST;
import static gda.device.timer.Tfg.PASSWORD;
import static gda.device.timer.Tfg.USER;
import static gda.device.timer.Tfg.VME_START_ATTR_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DummyDAServer;
import gda.util.TestUtils;

/**
 */
public class EtfgTest {
	static String testScratchDirectoryName = null;
	private Etfg tfg = new Etfg();
	private DummyDAServer daserver = new DummyDAServer();

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(EtfgTest.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}

	/**
	 *
	 */
	public EtfgTest() {
		tfg.setDaServer(daserver);
		tfg.configure();
	}

	/**
	 * Test method for {@link gda.device.timer.Tfg#setAttribute(java.lang.String, java.lang.Object)} and for
	 * {@link gda.device.timer.Tfg#getAttribute(java.lang.String)}.
	 * @throws DeviceException
	 */
	@Test
	public void testSetAttribute() throws DeviceException {
		tfg.setAttribute(EXT_START_ATTR_NAME, true);
		assertTrue((Boolean) tfg.getAttribute(EXT_START_ATTR_NAME));
		tfg.setAttribute(EXT_INHIBIT_ATTR_NAME, true);
		assertTrue((Boolean) tfg.getAttribute(EXT_INHIBIT_ATTR_NAME));
		tfg.setAttribute(VME_START_ATTR_NAME, true);
		assertTrue((Boolean) tfg.getAttribute(VME_START_ATTR_NAME));
		tfg.setAttribute(AUTO_CONTINUE_ATTR_NAME, true);
		assertTrue((Boolean) tfg.getAttribute(AUTO_CONTINUE_ATTR_NAME));

		tfg.setAttribute(EXT_START_ATTR_NAME, false);
		assertFalse((Boolean) tfg.getAttribute(EXT_START_ATTR_NAME));
		tfg.setAttribute(EXT_INHIBIT_ATTR_NAME, false);
		assertFalse((Boolean) tfg.getAttribute(EXT_INHIBIT_ATTR_NAME));
		tfg.setAttribute(VME_START_ATTR_NAME, false);
		assertFalse((Boolean) tfg.getAttribute(VME_START_ATTR_NAME));
		tfg.setAttribute(AUTO_CONTINUE_ATTR_NAME, false);
		assertFalse((Boolean) tfg.getAttribute(AUTO_CONTINUE_ATTR_NAME));

		tfg.setAttribute(USER, "gm");
		tfg.setAttribute(PASSWORD, "test");
		tfg.setAttribute(HOST, "testvig");
		tfg.setAttribute(ENDIAN, "intel");

		assertEquals("gm", tfg.getAttribute(USER));
		assertEquals("test", tfg.getAttribute(PASSWORD));
		assertEquals("testvig", tfg.getAttribute(HOST));
		assertEquals("intel", tfg.getAttribute(ENDIAN));
		assertEquals(0, tfg.getAttribute("TotalFrames"));

		assertEquals(tfg.getAttribute("rubbish"), null);
		tfg.setAttribute(null, null);
	}

	/**
	 * Test method for {@link gda.device.timer.Tfg#getStatus()}.
	 * @throws DeviceException
	 */
	@Test
	public void testGetStatus() throws DeviceException {
		assertEquals(Timer.IDLE, tfg.getStatus());
		tfg.setFail();
		assertEquals(Timer.IDLE, tfg.getStatus());
	}

	/**
	 * Test method for {@link gda.device.timer.Tfg#stop()}.
	 * @throws DeviceException
	 */
	@Test
	public void testStop() throws DeviceException {
		tfg.stop();
	}

	/**
	 * Test method for {@link gda.device.timer.Tfg#getMaximumFrames()}.
	 */
	@Test
	public void testGetMaximumFrames() {
		assertEquals(32767, tfg.getMaximumFrames());
	}

	/**
	 * Test method for {@link gda.device.timer.Tfg#getTotalFrames()},
	 * {@link gda.device.timer.Tfg#addFrameSet(int, double, double)},
	 * {@link gda.device.timer.Tfg#addFrameSet(int, double, double, int, int, int, int)}
	 * {@link gda.device.timer.Tfg#clearFrameSets()}, {@link gda.device.timer.Tfg#loadFrameSets()}.
	 * @throws DeviceException
	 */
	@Test
	public void testGetTotalFrames() throws DeviceException {
		tfg.setCycles(2);
		tfg.setAttribute(EXT_START_ATTR_NAME, true);
		tfg.setAttribute(EXT_INHIBIT_ATTR_NAME, true);
		tfg.addFrameSet(1, 1000, 2000);
		tfg.addFrameSet(2, 1000, 2000, 0, 0, 0, 0);
		tfg.addFrameSet(4, 1000, 2000, 0, 0, 0, 0);
		tfg.addFrameSet(8, 1000, 2000, 0, 0, 0, 0);
		try {
			tfg.loadFrameSets();
		} catch (DeviceException e) {
			fail("DeviceException should not happen");
		}
		assertEquals(15, tfg.getTotalFrames());
		tfg.clearFrameSets();
		assertEquals(0, tfg.getTotalFrames());
	}

	/**
	 * Test method for {@link gda.device.timer.Tfg#loadFrameSets()}.
	 */
	@Test
	public void testloadFrameSets() {
		try {
			tfg.setAttribute(DEBOUNCE, null);
			tfg.addFrameSet(1, 1000, 2000);
			tfg.loadFrameSets();

			ArrayList<Double> debounceValues = new ArrayList<Double>();
			tfg.setAttribute(DEBOUNCE, debounceValues);
			tfg.addFrameSet(1, 1000, 2000);
			tfg.loadFrameSets();

			debounceValues.add(Double.NaN);
			debounceValues.add(3.0);
			tfg.setAttribute(DEBOUNCE, debounceValues);
			tfg.addFrameSet(1, 1000, 2000);
			tfg.loadFrameSets();

			ArrayList<Double> thresholdValues = new ArrayList<Double>();
			tfg.setAttribute(THRESHOLD, thresholdValues);
			tfg.addFrameSet(1, 1000, 2000);
			tfg.loadFrameSets();

			thresholdValues.add(Double.NaN);
			thresholdValues.add(1.0);
			tfg.setAttribute(THRESHOLD, thresholdValues);
			tfg.addFrameSet(1, 1000, 2000);
			tfg.loadFrameSets();

			tfg.setAttribute(THRESHOLD, null);
			tfg.addFrameSet(1, 1000, 2000);
			tfg.loadFrameSets();
		} catch (DeviceException e) {
			fail("DeviceException should not happen");
		}
	}

	/**
	 * Test method for {@link gda.device.timer.Tfg#getCurrentFrame()}.
	 * @throws DeviceException
	 */
	@Test
	public void testGetCurrentFrame() throws DeviceException {
		assertEquals(0, tfg.getCurrentFrame());
		tfg.setFail();
		assertEquals(-1, tfg.getCurrentFrame());
	}

	/**
	 * Test method for {@link gda.device.timer.Tfg#getCurrentCycle()}.
	 * @throws DeviceException
	 */
	@Test
	public void testGetCurrentCycle() throws DeviceException {
		assertEquals(0, tfg.getCurrentCycle());
		tfg.setFail();
		assertEquals(-1, tfg.getCurrentCycle());
	}

	/**
	 * Test method for {@link gda.device.timer.Tfg#output(java.lang.String)}.
	 * @throws DeviceException
	 */
	@Test
	public void testOutput() throws DeviceException {
		tfg.setAttribute(USER, "gm");
		tfg.setAttribute(PASSWORD, null);
		tfg.setAttribute(HOST, "testvig");
		tfg.setAttribute(ENDIAN, "intel");
		tfg.output(testScratchDirectoryName + "test01");

		tfg.setAttribute(USER, "gm");
		tfg.setAttribute(PASSWORD, "test");
		tfg.setAttribute(HOST, "testvig");
		tfg.setAttribute(ENDIAN, "intel");
		tfg.output(testScratchDirectoryName + "test02");
	}

	/**
	 * Test method for {@link gda.device.timer.Tfg#start()}.
	 * @throws DeviceException
	 */
	@Test
	public void testStart() throws DeviceException {

		tfg.setAttribute(VME_START_ATTR_NAME, true);
		tfg.addFrameSet(2, 100, 200, 0, 0, 0, 0);
		tfg.addFrameSet(1, 100, 300, 0, 0, 0, 1);
		tfg.addFrameSet(1, 100, 200, 0, 0, 1, 0);
		try {
			tfg.loadFrameSets();
		} catch (DeviceException e) {
			fail("DeviceException should not happen");
		}
		tfg.start();
		while (Timer.IDLE == tfg.getStatus()) {
			synchronized (this) {
				try {
					wait(10); // wait for Tfg thread to become active
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		assertEquals(Timer.ACTIVE, tfg.getStatus());
		while (Timer.ACTIVE == tfg.getStatus()) {
			synchronized (this) {
				try {
					wait(10); // wait for Tfg thread to becomes idle
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		assertEquals(Timer.PAUSED, tfg.getStatus());
		tfg.restart();
		while (Timer.PAUSED == tfg.getStatus()) {
			synchronized (this) {
				try {
					wait(10); // wait until pause goes active complete
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		assertEquals(Timer.ACTIVE, tfg.getStatus());
		while (Timer.ACTIVE == tfg.getStatus()) {
			synchronized (this) {
				try {
					wait(10); // wait until frames complete
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		assertEquals(Timer.PAUSED, tfg.getStatus());
		tfg.restart();
		while (Timer.PAUSED == tfg.getStatus()) {
			synchronized (this) {
				try {
					wait(10); // wait until pause goes active complete
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		assertEquals(Timer.ACTIVE, tfg.getStatus());
		while (Timer.ACTIVE == tfg.getStatus()) {
			synchronized (this) {
				try {
					wait(10); // wait until frames complete
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		assertEquals(Timer.IDLE, tfg.getStatus());

		tfg.setAttribute(VME_START_ATTR_NAME, false);
		tfg.countAsync(36000);
		while (Timer.IDLE == tfg.getStatus()) {
			synchronized (this) {
				try {
					wait(10); // wait for Tfg thread to become active
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		assertEquals(Timer.ACTIVE, tfg.getStatus());
		tfg.stop();
		while (Timer.ACTIVE == tfg.getStatus()) {
			synchronized (this) {
				try {
					wait(10); // wait for Tfg thread to becomes idle
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		assertEquals(Timer.IDLE, tfg.getStatus());
	}

	@AfterEach
	public void cleanupTfg() {
		tfg.shutdown();
	}

}
