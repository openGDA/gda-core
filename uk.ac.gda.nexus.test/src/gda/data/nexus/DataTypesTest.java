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
import gda.data.nexus.extractor.NexusExtractor;
import gda.util.TestUtils;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.junit.BeforeClass;
import org.junit.Test;

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
		new GenericTest<Double>(new Double[] { 0.0, -1.0, Double.MAX_VALUE });
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@Test
	public void testByte() throws Exception {
		new GenericTest<Byte>(new Byte[] { 0, -1, Byte.MAX_VALUE });
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@Test
	public void testLong() throws Exception {
		new GenericTest<Long>(new Long[] { 0l, -1l, Long.MAX_VALUE });
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@Test
	public void testLongUnsigned() throws Exception {
		// this should not work in principle
		new GenericTest<Long>(new Long[] { 0l, -1l, Long.MAX_VALUE });
	}

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@Test
	public void testInteger() throws Exception {
		new GenericTest<Integer>(new Integer[] { 0, -1, Integer.MAX_VALUE });
	}

	class GenericTest<T extends Number> {
		GenericTest(T[] toTest) throws Exception {

			T[] expected = toTest.clone();

			String filename = testScratchDirectoryName + "foo.nxs";
			try (NexusFile file = NexusUtils.createNexusFile(filename)) {
				GroupNode group = file.getGroup(NexusUtils.createAugmentPath("entry1", NexusExtractor.NXEntryClassName), true);
				NexusUtils.write(file, group, "data", toTest);
			}

			try (NexusFile file = NexusUtils.openNexusFileReadOnly(filename)) {
				GroupNode group = file.getGroup(NexusUtils.createAugmentPath("entry1", NexusExtractor.NXEntryClassName), true);
				DataNode data = file.getData(group, "data");
				IDataset d = data.getDataset().getSlice();

				for (int i = 0; i < expected.length; i++) {
					assertEquals(expected[i], d.getObject(i));
				}
			}
		}
	}
}
