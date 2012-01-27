/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.data.nexus;

import static org.junit.Assert.assertEquals;

import gda.util.TestUtils;

import org.junit.BeforeClass;
import org.junit.Test;
import org.nexusformat.NexusFile;

/**
 * Just a short test class to check writing of file types using the nexus java api 
 * 
 * There is no need to include this into the standard test suite as it only tests
 */
public class DataTypesTest {
	static String testScratchDirectoryName;

	/**
	 * Creates an empty directory for use by test code.
	 * 
	 * @throws Exception if setup fails
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(DataTypesTest.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@Test
	public void testDouble() throws Exception {
		new GenericTest<Double>(NexusFile.NX_FLOAT64, new Double[] { 0.0, -1.0, Double.MAX_VALUE });
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@Test
	public void testByte() throws Exception {
		new GenericTest<Byte>(NexusFile.NX_INT8, new Byte[] { 0, -1, Byte.MAX_VALUE });
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@Test
	public void testLong() throws Exception {
		new GenericTest<Long>(NexusFile.NX_INT64, new Long[] { 0l, -1l, Long.MAX_VALUE });
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@Test
	public void testLongUnsigned() throws Exception {
		// this should not work in principle
		new GenericTest<Long>(NexusFile.NX_UINT64, new Long[] { 0l, -1l, Long.MAX_VALUE });
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@Test
	public void testInteger() throws Exception {
		new GenericTest<Integer>(NexusFile.NX_INT32, new Integer[] { 0, -1, Integer.MAX_VALUE });
	}

	class GenericTest<T extends Number> {
		GenericTest(int type, T[] toTest) throws Exception {

			T[] expected = toTest.clone();
			T[] received = toTest.clone();

			String filename = testScratchDirectoryName + "foo.nxs";
			NexusFile file = new NexusFile(filename, NexusFile.NXACC_CREATE5);
			file.makegroup("entry1", "NXentry");
			file.opengroup("entry1", "NXentry");
			file.makedata("data", type, 1, new int[] { toTest.length });
			file.opendata("data");

			file.putdata(toTest);

			file.closedata();
			file.closegroup();
			file.close();

			file = new NexusFile(filename, NexusFile.NXACC_READ);
			file.opengroup("entry1", "NXentry");
			file.opendata("data");

			file.getdata(received);

			file.closedata();
			file.closegroup();
			file.close();

			for (int i = 0; i < expected.length; i++) {
				assertEquals(expected[i], received[i]);
			}
		}
	}
}