/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import gda.util.TestUtils;

public class FluoCompositeDataStoreTest {

	private double[][] theData;
	private File originalDataFile;

	@Before
	public void setup() throws FileNotFoundException {
		originalDataFile = TestUtils.getResourceAsFile(FluoCompositeDataStoreTest.class, "xspress3_snapshot.mca");
		FluoCompositeDataStore dataStore = new FluoCompositeDataStore(originalDataFile.getAbsolutePath());
		theData = dataStore.readDataFromFile();
	}

	@Test
	public void testReadDataFile() {
		assertEquals(10, theData.length);
		assertEquals(4096, theData[0].length);
		assertEquals(7947694, theData[0][0], Double.MIN_VALUE);
	}

	@Test
	public void testWriteDataFile() throws Exception {
		String testOutputDirectory = TestUtils.setUpTest(FluoCompositeDataStoreTest.class, "testWriteDataFile", true);
		String outputFilename = testOutputDirectory + File.separator + "xspress3_test_snapshot.mca";

		FluoCompositeDataStore testDataStore = new FluoCompositeDataStore(outputFilename);
		testDataStore.writeDataToFile(theData);

		// ASCII file compare
		assertEquals(Files.readAllLines(originalDataFile.toPath()), Files.readAllLines(Paths.get(outputFilename)));
	}
}
