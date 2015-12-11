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

import gda.data.nexus.extractor.NexusExtractorException;

import org.eclipse.dawnsci.nexus.NexusException;
import org.junit.Test;

public class Xspress3FileReaderTest {

	@Test
	public void testFileUnpackedCorrectly() throws NexusException, NexusExtractorException {
		String nexusFile = Xspress3FileReaderTest.class.getResource("46594_0003.hdf5").getPath();
		Xspress3FileReader reader = new Xspress3FileReader(nexusFile, 10, 4096);
		reader.readFile();
		double[][] data = reader.getFrame(0);
		org.junit.Assert.assertTrue(data.length == 10);
		org.junit.Assert.assertTrue(data[0].length == 4096);
	}

	@Test
	public void testSingleFrameRead() throws NexusException, NexusExtractorException {
		String nexusFile = Xspress3FileReaderTest.class.getResource("46594_0003.hdf5").getPath();
		Xspress3FileReader reader = new Xspress3FileReader(nexusFile, 10, 4096);
		reader.readFile();
		double[][] data = reader.getFrame(9);
		org.junit.Assert.assertTrue(data.length == 10);
		org.junit.Assert.assertTrue(data[0].length == 4096);
	}
}
