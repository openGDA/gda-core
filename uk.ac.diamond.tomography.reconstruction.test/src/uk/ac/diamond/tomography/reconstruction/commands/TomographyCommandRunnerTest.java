/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.january.dataset.IDataset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test harness for testing an ITomographyCommandRunner
 */
public class TomographyCommandRunnerTest {

	ITomographyCommandRunner commandRunner;
	private File inputFile;
	private File outputFile;

	@Before
	public void before() throws IOException {
		commandRunner = new TestCommandRunner();

		File golden = new File("src/uk/ac/diamond/tomography/reconstruction/commands/25531.nxs");
		assertTrue(golden.exists());
		inputFile = File.createTempFile("TomographyCommandRunnerTestInput", "nxs");
		FileUtils.copyFile(golden, inputFile);

		outputFile = File.createTempFile("TomographyCommandRunnerTestOutput", "nxs");
		assertTrue(outputFile.exists());
		outputFile.delete();
		assertFalse(outputFile.exists());
	}

	@After
	public void after() {
		inputFile.delete();
		outputFile.delete();
	}

	@Test
	public void testMakeReduced() {
		List<Integer> makeReduced = commandRunner.makeReduced(inputFile, outputFile, 0);
		assertNotNull(makeReduced);
		assertTrue(outputFile.exists());
		assertTrue(outputFile.length() > 0);
	}

	@Test
	public void testMapPreviewRecon() {
		IDataset mapPreviewRecon = commandRunner.mapPreviewRecon(inputFile, null);
		assertNotNull(mapPreviewRecon);
	}

	@Test
	public void testFullRecon() {
		File fullRecon = commandRunner.fullRecon(inputFile, null);
		assertNotNull(fullRecon);
	}

	@Test(expected = NullPointerException.class)
	public void testParameterReconWithNullParameters() {
		commandRunner.parameterRecon(null, inputFile, 0, null, null);
	}

	@Test
	public void testParameterRecon() {
		double[] parameters = new double[] { 1.1, 2.2, 3.3 };
		IDataset parameterRecon = commandRunner.parameterRecon(null, inputFile, 0, parameters, null);
		assertNotNull(parameterRecon);
	}

	@Test
	public void testGetTomographyParameters() {
		ITomographyParameter[] tomographyParameters = commandRunner.getTomographyParameters();
		assertNotNull(tomographyParameters);
	}
}
