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

package gda.device.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import gda.device.DeviceException;
import gda.device.detector.DummyDAServer;
import gda.factory.FactoryException;
import gda.util.TestUtils;
import gda.util.exceptionUtils;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Memory test case
 */
public class GdhistTest {
	static String testScratchDirectoryName = null;
	private Gdhist memory = new Gdhist();
	private DummyDAServer dummyDAServer = new DummyDAServer();

	/**
	 * 
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(GdhistTest.class.getCanonicalName());
		try {
			TestUtils.makeScratchDirectory(testScratchDirectoryName);
		} catch (Exception e) {
			fail(exceptionUtils.getFullStackMsg(e));
		}
	}

	/**
	 * Memory test class constructor
	 */
	public GdhistTest() {
		int sdims[] = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096};
		dummyDAServer.setName("DummyDAServer");
		dummyDAServer.setNonRandomTestData(true);
		memory.setName("GdhistUnderTest");
		memory.setSizeCommand("gdhist get-mem-size");
		memory.setOpenCommand("gdhist open");
		memory.setStartupScript("startup");
		memory.setDaServer(dummyDAServer);
		memory.setSupportedDimensions(sdims);
		memory.setName("gdhist-test-device");
		try {
			memory.configure();
			int[] dims = { 512, 1 };
			memory.setDimension(dims);
		} catch (DeviceException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		} catch (FactoryException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		}
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#configure()}.
	 */
	@Test
	public void testConfigure() {
		assertEquals("gdhist get-mem-size", memory.getSizeCommand());
		assertEquals("gdhist open", memory.getOpenCommand());
		assertEquals("startup", memory.getStartupScript());
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#close()}.
	 * @throws DeviceException 
	 */
	@Test
	public void testClose() throws DeviceException {
		memory.close();
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#setAttribute(java.lang.String, java.lang.Object)}.
	 * {@link gda.device.memory.Gdhist#getAttribute(java.lang.String)}.
	 */
	@Test
	public void testSetAttribute() {
		memory.setAttribute("User", "gm");
		memory.setAttribute("Password", "test");
		memory.setAttribute("Host", "testvig");
		memory.setAttribute("TotalFrames", 14);
		memory.setAttribute("Endian", "intel");

		assertEquals("gm", memory.getAttribute("User"));
		assertEquals("test", memory.getAttribute("Password"));
		assertEquals("testvig", memory.getAttribute("Host"));
		assertEquals(14, memory.getAttribute("TotalFrames"));
		assertEquals("intel", memory.getAttribute("Endian"));

		assertEquals(memory.getAttribute("rubbish"), null);
		memory.setAttribute(null, null);
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#getSupportedDimensions()}.
	 */
	@Test
	public void testSupportedDimensions() {
		int sdims[] = { 128, 256, 512, 1024, 2048, 4096 };
		memory.setSupportedDimensions(sdims);
		int j = 0;
		for (int i : memory.getSupportedDimensions())
			assertEquals(i, sdims[j++]);

	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#clear()}.
	 */
	@Test
	public void testClear() {
		try {
			memory.clear();
			memory.close();
			memory.clear();
		} catch (DeviceException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		}
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#clear()}.
	 * 
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testClearException() throws DeviceException {
		memory.setFail();
		memory.clear();
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#clear()}.
	 * 
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testClearException2() throws DeviceException {
		memory.close();
		memory.setFail();
		memory.clear();
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#clear(int, int)}.
	 */
	@Test
	public void testClearIntInt() {
		try {
			memory.clear(0, 512);
			memory.close();
			memory.clear(0, 512);
		} catch (DeviceException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		}
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#clear(int, int)}.
	 * 
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testClearIntIntException() throws DeviceException {
		memory.setFail();
		memory.clear(0, 512);
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#clear(int, int, int, int, int, int)}.
	 */
	@Test
	public void testClearIntIntIntIntIntInt() {
		try {
			memory.clear(0, 512, 1, 1, 0, 127);
			memory.close();
			memory.clear(0, 512, 1, 1, 0, 127);
		} catch (DeviceException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		}
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#clear(int, int, int, int, int, int)}.
	 * 
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testClearIntIntIntIntIntIntException() throws DeviceException {
		memory.setFail();
		memory.clear(0, 512, 0, 1, 0, 127);
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#start()}.
	 */
	@Test
	public void testStart() {
		try {
			memory.start();
			memory.close();
			memory.start();
		} catch (DeviceException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		}
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#start()}.
	 * 
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testStartException() throws DeviceException {
		memory.setFail();
		memory.start();
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#stop()}.
	 */
	@Test
	public void testStop() {
		try {
			memory.stop();
			memory.close();
			memory.stop();
		} catch (DeviceException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		}
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#stop()}.
	 * 
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testStopException() throws DeviceException {
		memory.setFail();
		memory.stop();
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#read(int, int, int, int, int, int)}.
	 */
	@Test
	public void testReadIntIntIntIntIntInt() {
		double[] data;
		int count = 0;
		int x = 0;
		int y = 256;
		int t = 0;
		int dx = 1;
		int dy = 1;
		int dt = 3;
		try {
			int[] dims = {512, 512};
			memory.setDimension(dims);
			data = memory.read(x, y, t, dx, dy, dt);
			int l = 0;
			for (int k = t; k < t + dt; k++) {
				for (int j = y; j < y + dy; j++) {
					for (int i = x; i < x + dx; i++) {
						double value = (double) i + j + (k * 10) + count;
						assertEquals(value, data[l++], 0);
					}
				}
			}
			memory.close();
			data = memory.read(x, y, t, dx, dy, dt);
			l = 0;
			for (int k = t; k < t + dt; k++) {
				for (int j = y; j < y + dy; j++) {
					for (int i = x; i < x + dx; i++) {
						double value = (double) i + j + (k * 10) + count;
						assertEquals(value, data[l++], 0);
					}
				}
			}

		} catch (DeviceException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		}
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#read(int)}.
	 */
	@Test
	public void testReadInt() {
		double[] data;
		int count = 0;
		int frame = 0;
		try {
			int[] dims = { 512, 1 };
			memory.setDimension(dims);
			data = memory.read(frame);
			for (int i = 0; i < 512; i++)
				assertEquals((double) i + (frame * 10) + count, data[i], 0);
			//count += 10;
			data = memory.read(frame);
			for (int i = 0; i < 512; i++)
				assertEquals((double) i + (frame * 10) + count, data[i], 0);
			memory.clear();
			count = 0;
			data = memory.read(frame);
			for (int i = 0; i < 512; i++)
				assertEquals((double) i + (frame * 10) + count, data[i], 0);
			memory.clear();
			count = 0;
			frame = 1;
			data = memory.read(frame);
			for (int i = 0; i < 512; i++)
				assertEquals((double) i + (frame * 10) + count, data[i], 0);
			//count += 10;
			data = memory.read(frame);
			for (int i = 0; i < 512; i++)
				assertEquals((double) i + (frame * 10) + count, data[i], 0);

		} catch (DeviceException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		}
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#setDimension(int[])}.
	 */
	@Test
	public void testSetDimension() {
		try {
			int[] dims = { 512, 1 };
			memory.setDimension(dims);
			assertEquals(512, memory.getDimension()[0]);
			assertEquals(1, memory.getDimension()[1]);
		} catch (DeviceException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		}
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#write(double[], int, int, int, int, int, int)}.
	 */
	@Test
	public void testWriteDoubleArrayIntIntIntIntIntInt() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#write(double[], int)}.
	 */
	@Test
	public void testWriteDoubleArrayInt() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#output(java.lang.String)}.
	 */
	@Test
	public void testOutput() {
		try {
			memory.output(testScratchDirectoryName + "testOutput");
			memory.setAttribute("Password", "");
			memory.output(testScratchDirectoryName + "testScalerOutput");
			memory.setAttribute("Password", "test");
			memory.close();

		} catch (DeviceException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		}
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#output(java.lang.String)}.
	 * 
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testOutputException() throws DeviceException {
		memory.setAttribute("Password", "test");
		memory.setFail();
		memory.output(testScratchDirectoryName + "testOutput2");
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#getMemorySize()}.
	 */
	@Test
	public void testGetMemorySize() {
		try {
			memory.setSizeCommand("gdhist get-mem-size");
			assertEquals(dummyDAServer.getMemorySize(), memory.getMemorySize());
			memory.close();
			assertEquals(dummyDAServer.getMemorySize(), memory.getMemorySize());
		} catch (DeviceException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		}
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#getMemorySize()}.
	 * 
	 * @throws DeviceException
	 */
	@Test(expected = DeviceException.class)
	public void testGetMemorySizeException() throws DeviceException {
		memory.setFail();
		memory.getMemorySize();
	}

	/**
	 * Test method for {@link gda.device.memory.Gdhist#reconfigure()}.
	 */
	@Test
	public void testReconfigure() {
		int count = 0;
		int frame = 0;
		double[] data = null;
		memory.setOpenCommand("gdhist open");
		assertEquals("gdhist open", memory.getOpenCommand());
		try {
			memory.reconfigure();
			memory.output(testScratchDirectoryName + "testScalerOutput2");
			memory.setAttribute("Password", "");
			memory.output(testScratchDirectoryName + "testScalerOutput3");
			memory.setAttribute("Password", "test");
			int[] dims = { 16, 1 };
			memory.setDimension(dims);
			memory.setAttribute("TotalFrames", 6);
			count = 0;
			frame = 0;
			data = memory.read(frame);
			for (int i = 0; i < 6; i++)
				assertEquals((double) i + (frame * 10) + count, data[i], 0);
		} catch (DeviceException e) {
			fail("DeviceException should not happen "  + e.getMessage());
		} catch (FactoryException e) {
			fail("FactoryException should not happen "  + e.getMessage());
		}
	}
}
