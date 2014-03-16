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
public class ScalerTest {
	static String testScratchDirectoryName = null;
	private Scaler memory = new Scaler();
	private DummyDAServer dummyDAServer = new DummyDAServer();

	/**
	 * 
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(ScalerTest.class.getCanonicalName());
		try {
			TestUtils.makeScratchDirectory(testScratchDirectoryName);
		} catch (Exception e) {
			fail(exceptionUtils.getFullStackMsg(e));
		}
	}

	/**
	 * Memory test class constructor
	 */
	public ScalerTest() {
		int sdims[] = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096};
		dummyDAServer.setName("DummyDAServer");
		dummyDAServer.setNonRandomTestData(true);
		memory.setName("ScalerUnderTest");
		memory.setSizeCommand("gdhist get-mem-size");
		memory.setOpenCommand("scaler");
		memory.setStartupScript("startup");
		memory.setDaServer(dummyDAServer);
		memory.setSupportedDimensions(sdims);
		try {
			memory.configure();
			int[] dims = { 512, 1 };
			memory.setDimension(dims);
		} catch (DeviceException e) {
			fail("DeviceException should not happen");
		} catch (FactoryException e) {
			fail("FactoryException should not happen");
		}
	}


	/**
	 * Test method for {@link gda.device.memory.Gdhist#reconfigure()}.
	 */
	@Test
	public void testReconfigure() {
		int count = 0;
		int frame = 0;
		double[] data = null;
		memory.setOpenCommand("scaler");
		assertEquals("scaler", memory.getOpenCommand());
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
			fail("DeviceException should not happen");
		} catch (FactoryException e) {
			fail("FactoryException should not happen");
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
			int[] dims2 = { 16, 1 };
			memory.setDimension(dims2);
			memory.setAttribute("TotalFrames", 6);
			count = 0;
			frame = 0;
			data = memory.read(frame);
			for (int i = 0; i < 6; i++)
				assertEquals((double) i + (frame * 10) + count, data[i], 0);
		} catch (DeviceException e) {
			fail("DeviceException should not happen");
		}
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
		int l = 0;
		try {
			int[] dims2 = { 16, 1 };
			memory.setDimension(dims2);
			memory.setAttribute("TotalFrames", 6);
			count = 0;
			x = 0;
			y = 0;
			t = 0;
			dx = 3;
			dy = 1;
			dt = 6;
			data = memory.read(t, y, x, dt, dy, dx);
			l = 0;
			for (int k = x; k < x + dx; k++) {
				for (int j = y; j < y + dy; j++) {
					for (int i = t; i < t + dt; i++) {
						assertEquals((double) i + j + (k * 10) + count, data[l++], 0);
					}
				}
			}
		} catch (DeviceException e) {
			fail("DeviceException should not happen");
		}
	}

}
