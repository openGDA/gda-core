package uk.ac.diamond.daq.experiment.structure;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.NodeFileCreationRequest;
import uk.ac.diamond.daq.experiment.structure.requester.NodeFileRequesterServiceTestConfiguration;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.core.tool.spring.AcquisitionFileContextTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { NodeFileRequesterServiceTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class NodeFileRequesterServiceTest {

	@Autowired
	private ExperimentController controller;

	@Autowired
	private NodeFileRequesterService nodeFileRequesterService;

	private IFilePathService filePathService;

	private static final String EXPERIMENT_NAME = "MyExperiment";
	private static final String ACQUISITION_NAME = "MyMeasurement";

	@Before
	public void before() throws Exception {
		filePathService = mock(IFilePathService.class);
		NodeFileCreationRequest response = new NodeFileCreationRequest();
		response.setStatus(Status.COMPLETE);
		doReturn(response).when(nodeFileRequesterService).getNodeFileCreationRequestResponse(ArgumentMatchers.any());

		LocalProperties.clearProperty(AcquisitionFileContext.ACQUISITION_EXPERIMENT_DIRECTORY_PROPERTY);
		prepareFilesystem();
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
		verify(nodeFileRequesterService).getNodeFileCreationRequestResponse(jobCaptor.capture());

		NodeFileCreationRequest job = jobCaptor.getValue();

		assertThat(job.getNodeLocation(), is(equalTo(experimentRoot)));
		assertThat(job.getChildren(), is(equalTo(nxsUrls)));
	}

	@Test
	public void multiPartAcquisitionRequestsNodeFileCreation() throws Exception {
		controller.startExperiment(EXPERIMENT_NAME);

		URL acquisition = controller.startMultipartAcquisition(ACQUISITION_NAME);
		URL scan1 = controller.prepareAcquisition(ACQUISITION_NAME+1);
		URL scan2 = controller.prepareAcquisition(ACQUISITION_NAME+2);

		controller.stopMultipartAcquisition();

		ArgumentCaptor<NodeFileCreationRequest> jobCaptor = ArgumentCaptor.forClass(NodeFileCreationRequest.class);
		verify(nodeFileRequesterService).getNodeFileCreationRequestResponse(jobCaptor.capture());

		NodeFileCreationRequest job = jobCaptor.getValue();

		assertThat(job.getNodeLocation(), is(equalTo(acquisition)));

		Set<URL> scans = new HashSet<>();
		scans.add(scan1);
		scans.add(scan2);
		assertThat(job.getChildren(), is(equalTo(scans)));
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
		verify(nodeFileRequesterService, times(3)).getNodeFileCreationRequestResponse(jobCaptor.capture());

		Set<URL> nodeFiles = jobCaptor.getAllValues().stream()
								.map(NodeFileCreationRequest::getNodeLocation)
								.collect(Collectors.toSet());

		assertThat(nodeFiles, is(equalTo(expectedNodeFiles)));
	}

	@Test
	public void multiPartIsAbleToCloseWithoutChildren() throws Exception {
		controller.startExperiment(EXPERIMENT_NAME);

		// start multipart acquisition...
		controller.startMultipartAcquisition(ACQUISITION_NAME);

		// on second thoughts... stop it (without generating acquisitions inside it)
		controller.stopMultipartAcquisition();

		verifyNoMoreInteractions(nodeFileRequesterService);
	}

	private void prepareFilesystem() throws IOException {
		Path experimentDir = Files.createTempDirectory(AcquisitionFileContextTest.class.getName());
		doReturn(experimentDir.toAbsolutePath().toString()).when(filePathService).getProcessingDir();
		ServiceHolder sh = new ServiceHolder();
		sh.setFilePathService(filePathService);
	}
}
