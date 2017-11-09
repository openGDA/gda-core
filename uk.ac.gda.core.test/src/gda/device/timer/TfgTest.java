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

import static gda.device.timer.Tfg.AUTO_CONTINUE_ATTR_NAME;
import static gda.device.timer.Tfg.ENDIAN;
import static gda.device.timer.Tfg.EXT_INHIBIT_ATTR_NAME;
import static gda.device.timer.Tfg.EXT_START_ATTR_NAME;
import static gda.device.timer.Tfg.HOST;
import static gda.device.timer.Tfg.PASSWORD;
import static gda.device.timer.Tfg.TOTAL_FRAMES;
import static gda.device.timer.Tfg.USER;
import static gda.device.timer.Tfg.VME_START_ATTR_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DummyDAServer;
import gda.util.TestUtils;
import gda.util.exceptionUtils;

/**
 *
 */
public class TfgTest {
	static String testScratchDirectoryName = null;
	private Tfg tfg = new Tfg();
	private DummyDAServer daserver = new DummyDAServer();

	/**
	 *
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(TfgTest.class.getCanonicalName());
		try {
			TestUtils.makeScratchDirectory(testScratchDirectoryName);
		} catch (Exception e) {
			fail(exceptionUtils.getFullStackMsg(e));
		}
	}

	/**
	 *
	 */
	public TfgTest() {
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
		assertEquals("gm", tfg.getAttribute(USER));
		assertEquals("test", tfg.getAttribute(PASSWORD));
		assertEquals("testvig", tfg.getAttribute(HOST));
		assertEquals("intel", tfg.getAttribute(ENDIAN));
		assertEquals(0, tfg.getAttribute(TOTAL_FRAMES));

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
		assertEquals(1024, tfg.getMaximumFrames());
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
		} catch (DeviceException e1) {
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

	/**
	 * Test method for {@link gda.device.timer.Tfg#close()}. This is this last test as it kills the internal
	 * TimeFrameGenerator in DAServer.
	 */
	@Test
	public void testClose() {
		tfg.close();
	}

/**
 * Test getCurrentFrames getCurrentDeadTime, getCurrentLiveTime,
 * getAcqStatus...
 * @throws DeviceException
 */
	@Test
	public void testGetCurrentFrames()  throws DeviceException {
		int currentFrame;
		int frame1 = 12;
		int frame2 = 13;
		int frame3 = 1;
		int frame4 = 49;
		int frame5 = 1;
		int frame6 = 19;
		tfg.setAttribute(VME_START_ATTR_NAME, true);
		tfg.addFrameSet(frame1, 1, 200, 0, 0, 0, 0);
		tfg.addFrameSet(frame2, 1, 200, 0, 0, 0, 0);
		tfg.addFrameSet(frame3,  2, 100, 0, 0, 1, 0);
		tfg.addFrameSet(frame4, 2, 100, 0, 0, 0, 0);
		tfg.addFrameSet(frame5 , 3, 50,  0, 0, 1, 0);
		tfg.addFrameSet(frame6, 3, 50, 0, 0, 0, 0);
		try {
			tfg.loadFrameSets();
		} catch (DeviceException e1) {
			fail("DeviceException should not happen");
		}
		currentFrame = tfg.getCurrentFrame() / 2 + 1;
		assertEquals(frame1+frame2, tfg.getCurrentFrames(currentFrame));
		assertEquals(0.001, tfg.getCurrentDeadTime(currentFrame), 0.0);
		assertEquals(0.2, tfg.getCurrentLiveTime(currentFrame), 0.0);
		int first = tfg.getCurrentFrames(currentFrame);
		assertEquals("IDLE", tfg.getAcqStatus());
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
		assertEquals("RUNNING", tfg.getAcqStatus());
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
		assertEquals("PAUSED", tfg.getAcqStatus());
		assertEquals(Timer.PAUSED, tfg.getStatus());
		currentFrame = tfg.getCurrentFrame() / 2 + 1;
		assertEquals(frame1+frame2+1, currentFrame);
		assertEquals(frame3+frame4, tfg.getCurrentFrames(currentFrame));
		assertEquals(0.002, tfg.getCurrentDeadTime(currentFrame), 0.0);
		assertEquals(0.1, tfg.getCurrentLiveTime(currentFrame), 0.0);
		int second = tfg.getCurrentFrames(currentFrame);
		tfg.restart();
		while (Timer.PAUSED == tfg.getStatus()) {
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
		currentFrame = tfg.getCurrentFrame() / 2 + 1;
		assertEquals(frame1+frame2+frame3+frame4+1, currentFrame);
		int third = tfg.getCurrentFrames(currentFrame);
		assertEquals(frame5+frame6, tfg.getCurrentFrames(currentFrame));
		assertEquals(0.003, tfg.getCurrentDeadTime(currentFrame), 0.0);
		assertEquals(0.05, tfg.getCurrentLiveTime(currentFrame), 0.0);
		System.out.println("first group " +  first);
		System.out.println("second group " + second);
		System.out.println("third group " + third);
	}
}
