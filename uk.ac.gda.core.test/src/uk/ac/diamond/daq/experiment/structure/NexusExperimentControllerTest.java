package uk.ac.diamond.daq.experiment.structure;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.ac.diamond.daq.experiment.structure.NexusExperimentController.DEFAULT_ACQUISITION_PREFIX;
import static uk.ac.diamond.daq.experiment.structure.NexusExperimentController.DEFAULT_EXPERIMENT_PREFIX;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
import uk.ac.diamond.daq.experiment.api.structure.NodeFileCreationRequest;
public class NexusExperimentControllerTest {

	private ExperimentController controller;

	@Mock
	private IRequester<NodeFileCreationRequest> jobRequestor;

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
		Mockito.when(jobRequestor.post(any())).thenReturn(mock(NodeFileCreationRequest.class));
		controller = new NexusExperimentController(filePathService, jobRequestor);
	}

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
	public void testExperimentName() throws ExperimentControllerException {
		URL experimentFolder = controller.startExperiment(EXPERIMENT_NAME);
		assertTrue("Experiment URL is malformed", extractBase(experimentFolder).startsWith(EXPERIMENT_NAME));
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

	@Test
	public void multiPartAcquisitionRequestsNodeFileCreation() throws Exception {
		controller.startExperiment(EXPERIMENT_NAME);

		URL acquisition = controller.startMultipartAcquisition(ACQUISITION_NAME);
		URL scan1 = controller.prepareAcquisition(ACQUISITION_NAME+1);
		URL scan2 = controller.prepareAcquisition(ACQUISITION_NAME+2);

		controller.stopMultipartAcquisition();

		ArgumentCaptor<NodeFileCreationRequest> jobCaptor = ArgumentCaptor.forClass(NodeFileCreationRequest.class);
		verify(jobRequestor).post(jobCaptor.capture());

		NodeFileCreationRequest job = jobCaptor.getValue();

		assertThat(job.getNodeLocation(), is(equalTo(acquisition)));

		Set<URL> scans = new HashSet<>();
		scans.add(scan1);
		scans.add(scan2);
		assertThat(job.getChildren(), is(equalTo(scans)));
	}

	@Test
	public void multiPartIsAbleToCloseWithoutChildren() throws Exception {
		controller.startExperiment(EXPERIMENT_NAME);

		// start multipart acquisition...
		controller.startMultipartAcquisition(ACQUISITION_NAME);

		// on second thoughts... stop it (without generating acquisitions inside it)
		controller.stopMultipartAcquisition();

		verifyNoMoreInteractions(jobRequestor);
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

	@Test
	public void stopExperimentCallsIndexFileCreator() throws Exception {

		URL experimentRoot = controller.startExperiment("My experiment!");
		URL firstUrl = controller.prepareAcquisition(ACQUISITION_NAME + "1");
		URL secondUrl = controller.prepareAcquisition(ACQUISITION_NAME + "2");

		controller.stopExperiment();

		Set<URL> nxsUrls = new HashSet<>();
		nxsUrls.add(firstUrl);
		nxsUrls.add(secondUrl);

		ArgumentCaptor<NodeFileCreationRequest> jobCaptor = ArgumentCaptor.forClass(NodeFileCreationRequest.class);
		verify(jobRequestor).post(jobCaptor.capture());

		NodeFileCreationRequest job = jobCaptor.getValue();

		assertThat(job.getNodeLocation(), is(equalTo(experimentRoot)));
		assertThat(job.getChildren(), is(equalTo(nxsUrls)));
	}


	@Test
	public void stopExperimentClosesAllOpenMultipartAcquisitions() throws Exception {
		URL experiment = controller.startExperiment(EXPERIMENT_NAME);

		URL firstLevel = controller.startMultipartAcquisition(ACQUISITION_NAME);
		URL secondLevel = controller.startMultipartAcquisition(ACQUISITION_NAME);
		controller.prepareAcquisition(ACQUISITION_NAME); // need a concrete child for the multipart to actually close

		Set<URL> expectedNodeFiles = new HashSet<>();
		expectedNodeFiles.add(experiment);
		expectedNodeFiles.add(firstLevel);
		expectedNodeFiles.add(secondLevel);

		controller.stopExperiment();

		ArgumentCaptor<NodeFileCreationRequest> jobCaptor = ArgumentCaptor.forClass(NodeFileCreationRequest.class);

		verify(jobRequestor, times(3)).post(jobCaptor.capture());

		Set<URL> nodeFiles = jobCaptor.getAllValues().stream()
								.map(NodeFileCreationRequest::getNodeLocation)
								.collect(Collectors.toSet());

		assertThat(nodeFiles, is(equalTo(expectedNodeFiles)));
	}

	private String getAndSetRootProperty(String newProperty) {
		String old = LocalProperties.get(NexusExperimentController.EXPERIMENT_CONTROLLER_ROOT);
		LocalProperties.set(NexusExperimentController.EXPERIMENT_CONTROLLER_ROOT, newProperty);
		return old;
	}
}
