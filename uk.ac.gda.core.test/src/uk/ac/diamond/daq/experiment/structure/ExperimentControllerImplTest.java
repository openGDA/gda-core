package uk.ac.diamond.daq.experiment.structure;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.ac.diamond.daq.experiment.structure.ExperimentControllerImpl.DEFAULT_ACQUISITION_PREFIX;
import static uk.ac.diamond.daq.experiment.structure.ExperimentControllerImpl.DEFAULT_EXPERIMENT_PREFIX;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.experiment.api.structure.IndexFileCreationRequest;
public class ExperimentControllerImplTest {

	private ExperimentController controller;

	@Mock
	private IRequester<IndexFileCreationRequest> jobRequestor;

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
		MockitoAnnotations.initMocks(this);
		IFilePathService filePathService = mock(IFilePathService.class);
		Mockito.when(filePathService.getVisitDir()).thenReturn(tempFolder.getRoot().getAbsolutePath());
		Mockito.when(jobRequestor.post(any())).thenReturn(mock(IndexFileCreationRequest.class));
		controller = new ExperimentControllerImpl(filePathService, jobRequestor);
	}

	private String extractBase(URL URL) {
		return FilenameUtils.getBaseName(FilenameUtils.getFullPathNoEndSeparator(URL.getPath()));
	}

	@Test
	public void testNullExperimentName() throws ExperimentControllerException {
		URL experimentFolder = controller.startExperiment(null);
		assertTrue("Experiment URL is malformed",
				extractBase(experimentFolder).startsWith(DEFAULT_EXPERIMENT_PREFIX));
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
		assertThat(extractBase(acquisitionFolder), startsWith(DEFAULT_ACQUISITION_PREFIX));
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

	@Test
	public void closingExperimentCallsIndexFileCreator() throws Exception {

		URL experimentRoot = controller.startExperiment("My experiment!");
		URL firstUrl = controller.createAcquisitionLocation(ACQUISITION_NAME + "1");
		URL secondUrl = controller.createAcquisitionLocation(ACQUISITION_NAME + "2");
		URL firstNxs = createNxs(firstUrl, ACQUISITION_NAME + "1");
		URL secondNxs = createNxs(secondUrl, ACQUISITION_NAME + "2");

		controller.stopExperiment();

		Set<URL> nxsUrls = new HashSet<>();
		nxsUrls.add(firstNxs);
		nxsUrls.add(secondNxs);

		ArgumentCaptor<IndexFileCreationRequest> jobCaptor = ArgumentCaptor.forClass(IndexFileCreationRequest.class);
		verify(jobRequestor).post(jobCaptor.capture());

		IndexFileCreationRequest job = jobCaptor.getValue();

		assertThat(job.getExperimentName(), is(equalTo("MyExperiment")));
		assertThat(job.getExperimentLocation(), is(equalTo(experimentRoot)));
		assertThat(job.getAcquisitions(), is(equalTo(nxsUrls)));
	}

	private URL createNxs(URL rootUrl, String name) throws Exception {
		URL nxsUrl = new URL(rootUrl, name + ".nxs");
		File nxs = new File(nxsUrl.getFile());
		if (!nxs.createNewFile()) {
			throw new Exception("Could not create test NeXus file");
		}
		return nxs.toURI().toURL();
	}

	private String getAndSetRootProperty(String newProperty) {
		String old = LocalProperties.get(ExperimentControllerImpl.EXPERIMENT_CONTROLLER_ROOT);
		LocalProperties.set(ExperimentControllerImpl.EXPERIMENT_CONTROLLER_ROOT, newProperty);
		return old;
	}
}
