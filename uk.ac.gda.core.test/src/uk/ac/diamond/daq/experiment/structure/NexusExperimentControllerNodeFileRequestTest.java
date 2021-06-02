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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.event.status.Status;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.diamond.daq.experiment.api.structure.NodeFileCreationRequest;

/**
 * Tests regarding the {@link NexusExperimentController}'s use of {@link NodeFileRequesterService}.
 */
public class NexusExperimentControllerNodeFileRequestTest extends NexusExperimentControllerTestBase {

	@Autowired
	private NodeFileRequesterService nodeFileRequesterService;

	private ArgumentCaptor<NodeFileCreationRequest> jobCaptor = ArgumentCaptor.forClass(NodeFileCreationRequest.class);

	@Before
	public void mockServiceResponse() throws Exception {
		var response = new NodeFileCreationRequest();
		response.setStatus(Status.COMPLETE);
		doReturn(response).when(nodeFileRequesterService).getNodeFileCreationRequestResponse(ArgumentMatchers.any());
	}

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/defaultContext");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/defaultContext/properties/_common/common_instance_java.properties");
	}


	@Test
	public void stopExperimentCallsIndexFileCreator() throws Exception {

		URL experimentRoot = getController().startExperiment(EXPERIMENT_NAME);
		var firstUrl = getController().prepareAcquisition(ACQUISITION_NAME + "1");
		var secondUrl = getController().prepareAcquisition(ACQUISITION_NAME + "2");

		getController().stopExperiment();

		var nxsUrls = new HashSet<>();
		nxsUrls.add(firstUrl);
		nxsUrls.add(secondUrl);

		verify(nodeFileRequesterService).getNodeFileCreationRequestResponse(jobCaptor.capture());

		NodeFileCreationRequest job = jobCaptor.getValue();

		assertThat(job.getNodeLocation(), is(equalTo(experimentRoot)));
		assertThat(job.getChildren(), is(equalTo(nxsUrls)));
	}

	@Test
	public void multiPartAcquisitionRequestsNodeFileCreation() throws Exception {
		getController().startExperiment(EXPERIMENT_NAME);

		URL acquisition = getController().startMultipartAcquisition(ACQUISITION_NAME);
		URL scan1 = getController().prepareAcquisition(ACQUISITION_NAME+1);
		URL scan2 = getController().prepareAcquisition(ACQUISITION_NAME+2);

		getController().stopMultipartAcquisition();

		verify(nodeFileRequesterService).getNodeFileCreationRequestResponse(jobCaptor.capture());

		NodeFileCreationRequest job = jobCaptor.getValue();

		assertThat(job.getNodeLocation(), is(equalTo(acquisition)));

		var scans = new HashSet<>();
		scans.add(scan1);
		scans.add(scan2);
		assertThat(job.getChildren(), is(equalTo(scans)));
	}

	@Test
	public void stopExperimentClosesAllOpenMultipartAcquisitions() throws Exception {
		URL experiment = getController().startExperiment(EXPERIMENT_NAME);

		URL firstLevel = getController().startMultipartAcquisition(ACQUISITION_NAME);
		URL secondLevel = getController().startMultipartAcquisition(ACQUISITION_NAME);
		getController().prepareAcquisition(ACQUISITION_NAME); // need a concrete child for the multipart to actually close

		var expectedNodeFiles = new HashSet<>();
		expectedNodeFiles.add(experiment);
		expectedNodeFiles.add(firstLevel);
		expectedNodeFiles.add(secondLevel);

		getController().stopExperiment();

		verify(nodeFileRequesterService, times(3)).getNodeFileCreationRequestResponse(jobCaptor.capture());

		Set<URL> nodeFiles = jobCaptor.getAllValues().stream()
								.map(NodeFileCreationRequest::getNodeLocation)
								.collect(Collectors.toSet());

		assertThat(nodeFiles, is(equalTo(expectedNodeFiles)));
	}

	@Test
	public void multiPartIsAbleToCloseWithoutChildren() throws Exception {
		getController().startExperiment(EXPERIMENT_NAME);

		// start multipart acquisition...
		getController().startMultipartAcquisition(ACQUISITION_NAME);

		// on second thoughts... stop it (without generating acquisitions inside it)
		getController().stopMultipartAcquisition();

		verifyNoMoreInteractions(nodeFileRequesterService);
	}

}
