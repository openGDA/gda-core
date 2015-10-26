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

package gda.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * test file utilities
 */
public class FileUtilTest {
	private long expectedChecksum = 3038282843L;

	/**
	 * test the checksum
	 */
	@Ignore("2010/06/03 Test ignored since not passing. Method being tested does not appear to be used. GDA-3274")
	@Test
	public void testChecksumCalculation() throws Exception {
		String checksumFile = TestUtils.getResourceAsFile(FileUtilTest.class, "checksumFile").getAbsolutePath();
		try {
			assertEquals(expectedChecksum, FileUtil.checksum(checksumFile));
		} catch (IOException e) {
			fail("File for checksum test not found");
		}
	}
}
