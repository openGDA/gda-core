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

package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

import java.io.File;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.junit.Before;
import org.junit.Test;

import gda.util.TestUtils;

public class Xspress3FileReaderTest {

	private File nexusFile;

	@Before
	public void setup() {
		var testFileLoc = TestUtils.getGDALargeTestFilesLocation();
		assumeThat("GDALargeTestFilesLocation system property not set", testFileLoc, is(notNullValue()));
		nexusFile = new File(testFileLoc + "uk.ac.gda.devices.xspress3.test/46594_0003.hdf5");
	}

	@Test
	public void testFileUnpackedCorrectly() throws ScanFileHolderException {
		Xspress3FileReader reader = new Xspress3FileReader(nexusFile.getPath(), 10, 4096);
		reader.readFile();
		double[][] data = reader.getFrame(0);
		assertEquals(10, data.length);
		assertEquals(4096, data[0].length);
	}

	@Test
	public void testSingleFrameRead() throws ScanFileHolderException {
		Xspress3FileReader reader = new Xspress3FileReader(nexusFile.getPath(), 10, 4096);
		reader.readFile();
		double[][] data = reader.getFrame(9);
		assertEquals(10, data.length);
		assertEquals(4096, data[0].length);
	}
}
