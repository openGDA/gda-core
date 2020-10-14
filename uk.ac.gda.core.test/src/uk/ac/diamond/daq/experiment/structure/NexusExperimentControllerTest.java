/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.structure;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.ac.diamond.daq.experiment.structure.NexusExperimentController.DEFAULT_ACQUISITION_PREFIX;
import static uk.ac.diamond.daq.experiment.structure.NexusExperimentController.DEFAULT_EXPERIMENT_PREFIX;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;


public class NexusExperimentControllerTest extends NexusExperimentControllerTestBase {

	@Autowired
	private AcquisitionFileContext context;

	private String extractBase(URL url) {
		return FilenameUtils.getBaseName(FilenameUtils.getFullPathNoEndSeparator(url.getPath()));
	}

	@Test
	public void testNullExperimentName() throws ExperimentControllerException {
		URL experimentFolder = controller.startExperiment(null);
		assertTrue("Experiment URL is malformed",
				extractBase(experimentFolder).startsWith(DEFAULT_EXPERIMENT_PREFIX));
	}

	@Test
	public void testEmptyExperimentName() throws ExperimentControllerException {
		URL experimentFolder = controller.startExperiment("");
		assertTrue("Experiment URL is malformed",
				extractBase(experimentFolder).startsWith(DEFAULT_EXPERIMENT_PREFIX));
	}

	@Test
	public void testExperimentName() throws ExperimentControllerException, IOException {
		URL experimentFolder = controller.startExperiment(EXPERIMENT_NAME);
		assertTrue("Experiment URL is malformed", extractBase(experimentFolder).startsWith(EXPERIMENT_NAME));

		File experimentFile = new File(experimentFolder.getPath());
		File experimentDir = experimentFile.getParentFile();
		File experimentsDir = experimentDir.getParentFile();
		File visitDir = experimentsDir.getParentFile();

		// Experiments directory has to live under $VISIT folder
		assertEquals("Experiment URL is malformed", filePathService.getVisitDir(), visitDir.getPath());
	}

	@Test
	public void experimentUrlIsFile() throws Exception {
		URL experiment = controller.startExperiment(EXPERIMENT_NAME);
		assertTrue("Location of file, not directory, is expected", isFile(experiment));
	}

	@Test
	public void acquisitionUrlIsFile() throws Exception {
		controller.startExperiment(EXPERIMENT_NAME);
		URL acquisition = controller.prepareAcquisition(ACQUISITION_NAME);
		assertTrue("Location of file, not directory, is expected", isFile(acquisition));
	}

	@Test
	public void consecutiveAcquisitionsPreparedAtSameLevel() throws Exception {
		controller.startExperiment(EXPERIMENT_NAME);
		URL acq1 = controller.prepareAcquisition(ACQUISITION_NAME);
		URL acq2 = controller.prepareAcquisition(ACQUISITION_NAME);
		assertTrue(urlsAtSameLevel(acq1, acq2));
	}

	/**
	 * Test that two urls have the same parent node in tree
	 */
	private boolean urlsAtSameLevel(URL url1, URL url2) throws URISyntaxException {
		Path path1 = getExperimentTreeParent(url1);
		Path path2 = getExperimentTreeParent(url2);
		return path1.equals(path2);
	}

	private Path getExperimentTreeParent(URL file) throws URISyntaxException {
		return Paths.get(file.toURI())
				.getParent() // file's directory
				.getParent(); // parent in tree
	}

	@Test
	public void multiPartAcquisitionCreatesSubNode() throws Exception {
		controller.startExperiment(EXPERIMENT_NAME);
		URL standardAcquisition = controller.prepareAcquisition(ACQUISITION_NAME);

		String pointAndShootName = "Point And Shoot";

		URL pointAndShoot = controller.startMultipartAcquisition(pointAndShootName);
		URL child1 = controller.prepareAcquisition(pointAndShootName);
		URL child2 = controller.prepareAcquisition(pointAndShootName);

		assertTrue(urlsAtSameLevel(standardAcquisition, pointAndShoot));
		assertTrue(urlsAtSameLevel(child1, child2));

		assertTrue(isParent(pointAndShoot, child1));
	}

	@Test
	public void supportForMultiLevelMultiPartAcquisitions() throws Exception {
		controller.startExperiment(EXPERIMENT_NAME);
		URL firstLevel = controller.startMultipartAcquisition(ACQUISITION_NAME);
		URL secondLevel = controller.startMultipartAcquisition(ACQUISITION_NAME);
		URL secondLevelPart = controller.prepareAcquisition(ACQUISITION_NAME);

		assertTrue(isParent(firstLevel, secondLevel));
		assertTrue(isParent(secondLevel, secondLevelPart));
	}

	private boolean isParent(URL parentFile, URL childFile) {
		Path parentFolder = getFolder(parentFile);
		Path childFolder = getFolder(childFile);
		return childFolder.getParent().equals(parentFolder);
	}

	private Path getFolder(URL file) {
		return Paths.get(file.getPath()).getParent();
	}

	private boolean isFile(URL url) throws Exception {
		File file = new File(url.getPath());
		if (!file.exists()) {
			new File(file.getParent()).mkdirs();
			if (!file.createNewFile()) throw new Exception("Couldn't create file: " + url.getPath());
		}

		return file.isFile();
	}

	@Test
	public void testAcquisitionName() throws Exception {
		controller.startExperiment(EXPERIMENT_NAME);
		String acquisitionDir = extractBase(controller.prepareAcquisition(ACQUISITION_NAME));
		assertThat(acquisitionDir, startsWith(ACQUISITION_NAME));
	}

	@Test
	public void testNullMeasurementIdentifier() throws ExperimentControllerException {
		controller.startExperiment(EXPERIMENT_NAME);
		URL acquisitionFolder = controller.prepareAcquisition(null);
		assertThat(extractBase(acquisitionFolder), startsWith(DEFAULT_ACQUISITION_PREFIX));
	}

	@Test
	public void nonAlphaNumericCharactersBecomeUnderscores() throws ExperimentControllerException {
		URL experimentFolder = controller.startExperiment("$my experiment &");
		assertTrue("Experiment name is not formatted as requested",
				extractBase(experimentFolder).startsWith(EXPERIMENT_NAME));
	}

	@Test(expected = ExperimentControllerException.class)
	public void createsTwoExperimentSoThrowException() throws ExperimentControllerException {
		controller.startExperiment(null);
		controller.startExperiment(null);
	}

	@Test(expected = ExperimentControllerException.class)
	public void createsAcquisitionBeforeExperimentSoThrowException() throws ExperimentControllerException {
		controller.prepareAcquisition(null);
	}

	@Test(expected = ExperimentControllerException.class)
	public void stopExperimentWhenNoneRunningThrows() throws ExperimentControllerException {
		controller.stopExperiment();
	}

	@Test
	public void absoluteRootDirFromProperty() throws Exception {
		File file = new File("/tmp/nexusTest");
		if (file.exists()) file.delete();
		loadProperties("test/resources/gdaContext/nexusExperimentAbsoluteContext.properties");
		URL experimentUrl = controller.startExperiment(EXPERIMENT_NAME);
		assertThat(experimentUrl.getPath(), startsWith("/tmp/nexusTest"));
	}

	@Test
	public void relativeRootDirFromProperty() throws Exception {
		loadProperties("test/resources/gdaContext/nexusExperimentRelativeContext.properties");
		String experimentPath = controller.startExperiment(EXPERIMENT_NAME).getPath();
		assertThat(experimentPath, startsWith(context.getContextFile(AcquisitionFileContext.ContextFile.ACQUISITION_EXPERIMENT_DIRECTORY).getPath()));
		assertThat(experimentPath, containsString("some/subpath"));
	}

	private void loadProperties(String resourcePath) {
		File resource = new File(resourcePath);
		System.setProperty(LocalProperties.GDA_PROPERTIES_FILE, resource.getPath());
		LocalProperties.reloadAllProperties();
	}


}
