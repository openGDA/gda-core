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

package gda.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class OSCommandRunnerTest {
	final static String TestFileFolder = "testfiles/gda/util/OSCommandRunnerTest/";
	static String testScratchDirectoryName = null;

	/**
	 * Creates an empty directory for use by test code.
	 *
	 * @throws Exception if setup fails
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(OSCommandRunnerTest.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}

	/**
	 * Test method for {@link gda.util.OSCommandRunner#OSCommandRunner(String[], boolean, String, String)}.
	 */
	@Test
	public void testOSCommandRunner() {
		String[] commands = { "awk", "{if(NF>5) print $1,$4}" };
		OSCommandRunner os = new OSCommandRunner(commands, false, TestFileFolder + "OSCommandRunnerInput.dat",
				testScratchDirectoryName + "OSCommandRunnerOutput.dat");
		assertTrue(os.succeeded);
	}

	/**
	 *
	 */
	@Test
	public void testNoInputFile() {
		String[] commands = { "awk", "{if(NF>5) print $1,$4}" };
		OSCommandRunner os = new OSCommandRunner(commands, false, TestFileFolder + "DoesNotExist.dat",
				testScratchDirectoryName + "OSCommandRunnerOutput.dat");
		assertEquals(TestFileFolder + "DoesNotExist.dat" + " (No such file or directory)", os.exception.getMessage());
	}

	/**
	 *
	 */
	@Test
	public void testNoOutputFile() {
		String[] commands = { "awk", "{if(NF>5) print $1,$4}" };
		OSCommandRunner os = new OSCommandRunner(commands, false, TestFileFolder + "OSCommandRunnerInput.dat",
				testScratchDirectoryName + "/DoesNotExist/1.dat");
		assertEquals(testScratchDirectoryName + "DoesNotExist/1.dat" + " (No such file or directory)", os.exception.getMessage());
	}
}
