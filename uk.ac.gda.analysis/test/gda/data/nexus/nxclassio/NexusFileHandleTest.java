/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.data.nexus.nxclassio;

import gda.TestHelpers;

import java.io.File;

import org.junit.Test;

/**
 * Test of NexusFileHandle
 *
 */
public class NexusFileHandleTest {
	final static String TestFileFolder = "testfiles/gda/data/nexus/nxclassio/";
	/**
	 * Test of the helper function makeMetaDataTestFile
	 * 
	 * @throws Exception if the test fails
	 */
	@Test
	public void testNexusFileHandle() throws Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(NexusFileHandleTest.class, "testNexusFileHandle", true) +"/";
		Utils.makeMetaDataTestFile(testScratchDirectoryName+"testNexusFileHandle.nxs", new int [] { 10,10});
		uk.ac.diamond.scisoft.analysis.io.NexusLoader.convertToAscii(testScratchDirectoryName + "testNexusFileHandle.nxs", "","",
				testScratchDirectoryName+"testNexusFileHandle_Actual.txt", null);
		junitx.framework.FileAssert.assertEquals( 
				new File(TestFileFolder+"testNexusFileHandle_Expected.txt"), 
				new File(testScratchDirectoryName+"testNexusFileHandle_Actual.txt"));
		(new File(testScratchDirectoryName+"testNexusFileHandle.nxs")).delete();
		(new File(testScratchDirectoryName+"testNexusFileHandle_Actual.txt")).delete();
	}
}	