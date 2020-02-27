package uk.ac.diamond.daq.experiment.structure;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;

public class ExperimentControllerImplTest {

	private ExperimentController controller;

	private static final String EXPERIMENT_NAME = "MyExperiment";
	private static final String ACQUISITION_NAME = "MyMeasurement";


	/**
	 * This TemporaryFolder ensures that even if ExperimentControllerImpl
	 * attempts to create directories with the exact same name & timestamp
	 * for subsequent tests, their base directory will be unique.
	 *
	 * Also ensures automatic cleanup.
	 */
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void before() throws Exception {
		IFilePathService filePathService = mock(IFilePathService.class);
		Mockito.when(filePathService.getVisitDir()).thenReturn(tempFolder.getRoot().getAbsolutePath());
		controller = new ExperimentControllerImpl(filePathService);
	}

	private String extractBase(URL url) {
		return FilenameUtils.getBaseName(FilenameUtils.getFullPathNoEndSeparator(url.getPath()));
	}

	@Test
	public void testNullExperimentName() throws ExperimentControllerException {
		URL experimentFolder = controller.startExperiment(null);
		assertTrue("Experiment URL is malformed",
				extractBase(experimentFolder).startsWith(controller.getDefaultExperimentName()));
	}

	@Test
	public void testExperimentName() throws ExperimentControllerException {
		URL experimentFolder = controller.startExperiment(EXPERIMENT_NAME);
		assertTrue("Experiment URL is malformed", extractBase(experimentFolder).startsWith(EXPERIMENT_NAME));
	}

	@Test
	public void testAcquisitionName() throws Exception {
		controller.startExperiment(EXPERIMENT_NAME);
		String acquisitionDir = extractBase(controller.createAcquisitionLocation(ACQUISITION_NAME));
		assertThat(acquisitionDir, startsWith(ACQUISITION_NAME));
	}

	@Test
	public void testNullMeasurementIdentifier() throws ExperimentControllerException {
		controller.startExperiment(EXPERIMENT_NAME);
		URL acquisitionFolder = controller.createAcquisitionLocation(null);
		assertTrue("Acquisition URL is malformed",
				extractBase(acquisitionFolder).startsWith(controller.getDefaultAcquisitionName()));
	}

	@Test
	public void nonAlphaNumericCharactersBecomeUnderscores() throws ExperimentControllerException {
		URL experimentFolder = controller.startExperiment("$my experiment &");
		assertTrue("Experiment name is not formatted as requested",
				extractBase(experimentFolder).startsWith("MyExperiment"));
	}

	@Test(expected = ExperimentControllerException.class)
	public void createsTwoExperimentSoThrowException() throws ExperimentControllerException {
		controller.startExperiment(null);
		controller.startExperiment(null);
	}

	@Test(expected = ExperimentControllerException.class)
	public void createsAcquisitionBeforeExperimentSoThrowException() throws ExperimentControllerException {
		controller.createAcquisitionLocation(null);
	}

	@Test(expected = ExperimentControllerException.class)
	public void stopExperimentWhenNoneRunningThrows() throws ExperimentControllerException {
		controller.stopExperiment();
	}

	@Test
	public void absoluteRootDirFromProperty() throws Exception {
		File rootFolder = tempFolder.newFolder("rootfolder");
		String previousRoot = getAndSetRootProperty(rootFolder.getAbsolutePath());
		URL experimentUrl = controller.startExperiment(EXPERIMENT_NAME);
		assertThat(experimentUrl.getPath(), startsWith(rootFolder.getAbsolutePath()));
		getAndSetRootProperty(previousRoot);
	}

	@Test
	public void relativeRootDirFromProperty() throws Exception {
		String relativePath = "some/subpath";
		String previousRoot = getAndSetRootProperty(relativePath);
		String experimentPath = controller.startExperiment(EXPERIMENT_NAME).getPath();
		assertThat(experimentPath, startsWith(tempFolder.getRoot().getAbsolutePath()));
		assertThat(experimentPath, containsString(relativePath));
		getAndSetRootProperty(previousRoot);
	}

	@Test (expected = ExperimentControllerException.class)
	public void startExperimentThrowsIfDirectoryCannotBeCreated() throws Exception {
		String previousRoot = getAndSetRootProperty("/"); // will lack permissions to write here
		try {
			controller.startExperiment(EXPERIMENT_NAME);
		} finally {
			getAndSetRootProperty(previousRoot);
		}
	}

	private String getAndSetRootProperty(String newProperty) {
		String old = LocalProperties.get(ExperimentControllerImpl.EXPERIMENT_CONTROLLER_ROOT);
		LocalProperties.set(ExperimentControllerImpl.EXPERIMENT_CONTROLLER_ROOT, newProperty);
		return old;
	}
}
