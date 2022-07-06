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

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;
import static gda.configuration.properties.LocalProperties.GDA_PROPERTIES_FILE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;


public class NexusExperimentControllerTest extends NexusExperimentControllerTestBase {

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/defaultContext");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/defaultContext/properties/_common/common_instance_java.properties");
	}

	@Test
	public void nullExperimentNameReplacedByDefaultPrefix() throws ExperimentControllerException {
		URL experimentFolder = getController().startExperiment(null);
		assertThat(experimentFolder.getFile(), containsString(NexusExperimentController.DEFAULT_EXPERIMENT_PREFIX));
	}

	@Test
	public void emptyExperimentNameReplacedByDefaultPrefix() throws ExperimentControllerException {
		URL experimentFolder = getController().startExperiment("");
		assertThat(experimentFolder.getFile(), containsString(NexusExperimentController.DEFAULT_EXPERIMENT_PREFIX));
	}

	@Test
	public void testExperimentDirectoryStructure() throws ExperimentControllerException {
		URL experimentFolder = getController().startExperiment(EXPERIMENT_NAME);

		var experimentFile = new File(experimentFolder.getPath());
		var experimentDir = experimentFile.getParentFile();
		var experimentsDir = experimentDir.getParentFile();
		var visitDir = experimentsDir.getParentFile();

		// Experiments directory has to live under $VISIT folder
		assertThat("Experiment URL is malformed", visitDir.getPath(), is(equalTo(filePathService.getVisitDir())));
	}

	@Test
	public void experimentUrlIsFile() throws Exception {
		URL experiment = getController().startExperiment(EXPERIMENT_NAME);
		assertTrue("Location of file, not directory, is expected", isFile(experiment));
	}

	@Test
	public void acquisitionUrlIsFile() throws Exception {
		getController().startExperiment(EXPERIMENT_NAME);
		URL acquisition = getController().prepareAcquisition(ACQUISITION_NAME);
		assertTrue("Location of file, not directory, is expected", isFile(acquisition));
	}

	@Test
	public void consecutiveAcquisitionsPreparedAtSameLevel() throws Exception {
		getController().startExperiment(EXPERIMENT_NAME);
		URL acq1 = getController().prepareAcquisition(ACQUISITION_NAME);
		URL acq2 = getController().prepareAcquisition(ACQUISITION_NAME);
		assertTrue(urlsAtSameLevel(acq1, acq2));
	}

	@Test
	public void acquisitionUrlsCreatedViaFilePathService() throws Exception {
		getController().startExperiment(EXPERIMENT_NAME);
		URL expectedUrl = Paths.get(filePathService.getNextPath(null)).toUri().toURL();
		URL acquisition = getController().prepareAcquisition(ACQUISITION_NAME);
		assertThat(acquisition, is(equalTo(expectedUrl)));
	}

	/**
	 * Test that two urls have the same parent node in tree
	 */
	private boolean urlsAtSameLevel(URL url1, URL url2) throws URISyntaxException {
		var path1 = getExperimentTreeParent(url1);
		var path2 = getExperimentTreeParent(url2);
		return path1.equals(path2);
	}

	private Path getExperimentTreeParent(URL file) throws URISyntaxException {
		return Paths.get(file.toURI())
				.getParent() // file's directory
				.getParent(); // parent in tree
	}

	@Test
	public void supportForMultiLevelMultiPartAcquisitions() throws Exception {
		getController().startExperiment(EXPERIMENT_NAME);
		URL firstLevel = getController().startMultipartAcquisition(ACQUISITION_NAME);
		URL secondLevel = getController().startMultipartAcquisition(ACQUISITION_NAME);

		assertTrue(isParent(firstLevel, secondLevel));
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
		var file = new File(url.getPath());
		if (!file.exists()) {
			new File(file.getParent()).mkdirs();
			if (!file.createNewFile()) throw new Exception("Couldn't create file: " + url.getPath());
		}

		return file.isFile();
	}

	@Test
	public void nonAlphaNumericCharactersReplaced() throws ExperimentControllerException {
		URL experimentFolder = getController().startExperiment("$my experiment &");
		assertThat(experimentFolder.getFile(), containsString("MyExperiment"));
	}

	@Test
	public void createsTwoExperimentSoThrowException() throws ExperimentControllerException {
		getController().startExperiment(null);
		assertThrows(ExperimentControllerException.class, () -> getController().startExperiment(null));
	}

	@Test(expected = ExperimentControllerException.class)
	public void createsAcquisitionBeforeExperimentSoThrowException() throws ExperimentControllerException {
		getController().prepareAcquisition(null);
	}

	@Test(expected = ExperimentControllerException.class)
	public void stopExperimentWhenNoneRunningThrows() throws ExperimentControllerException {
		getController().stopExperiment();
	}
}
