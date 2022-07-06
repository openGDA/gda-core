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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.ac.diamond.daq.experiment.api.structure.NodeInsertionRequest;

/**
 * Tests regarding the {@link NexusExperimentController}'s use of {@link NodeFileRequesterService}.
 */
public class NexusExperimentControllerNodeFileRequestTest extends NexusExperimentControllerTestBase {

	private ArgumentCaptor<NodeInsertionRequest> jobCaptor = ArgumentCaptor.forClass(NodeInsertionRequest.class);

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/defaultContext");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/defaultContext/properties/_common/common_instance_java.properties");
	}

	@Test
	public void prepareAcquisitionTriggersNodeFileCreationRequest() throws Exception {
		URL experimentRoot = getController().startExperiment(EXPERIMENT_NAME);
		var acquisitionUrl = getController().prepareAcquisition(ACQUISITION_NAME);

		verify(nodeFileRequesterService).getNodeFileCreationRequestResponse(jobCaptor.capture());

		NodeInsertionRequest job = jobCaptor.getValue();

		assertThat(job.getNodeLocation(), is(equalTo(experimentRoot)));
		assertThat(job.getChildren().size(), is(1));
		var child = job.getChildren().entrySet().iterator().next();
		assertThat(child.getKey(), containsString(ACQUISITION_NAME));
		assertThat(child.getValue(), is(equalTo(acquisitionUrl)));
	}

	@Test
	public void nullAcquisitionNameReplacedByDefaultPrefix() throws Exception {
		getController().startExperiment(EXPERIMENT_NAME);
		getController().prepareAcquisition(null);

		verify(nodeFileRequesterService).getNodeFileCreationRequestResponse(jobCaptor.capture());
		NodeInsertionRequest job = jobCaptor.getValue();

		assertThat(job.getChildren().size(), is(1));
		var child = job.getChildren().entrySet().iterator().next();
		assertThat(child.getKey(), containsString(NexusExperimentController.DEFAULT_ACQUISITION_PREFIX));
	}

	@Test
	public void multipartAcquisitionTriggersNodeFileCreationRequest() throws Exception {
		var experimentUrl = getController().startExperiment(EXPERIMENT_NAME);

		var multipartUrl = getController().startMultipartAcquisition(ACQUISITION_NAME);

		verify(nodeFileRequesterService).getNodeFileCreationRequestResponse(jobCaptor.capture());

		NodeInsertionRequest job = jobCaptor.getValue();

		assertThat(job.getNodeLocation(), is(equalTo(experimentUrl)));
		assertThat(job.getChildren().size(), is(1));
		var child = job.getChildren().entrySet().iterator().next();
		assertThat(child.getKey(), containsString(ACQUISITION_NAME));
		assertThat(child.getValue(), is(equalTo(multipartUrl)));
	}

	@Test
	public void eachAcquisitionWithinMultipartAcquisitionTriggersRequest() throws Exception {
		var experimentUrl = getController().startExperiment(EXPERIMENT_NAME);

		var multipartAcquisitionUrl = getController().startMultipartAcquisition(ACQUISITION_NAME);
		var scan1Url = getController().prepareAcquisition(ACQUISITION_NAME+1);
		var scan2Url = getController().prepareAcquisition(ACQUISITION_NAME+2);

		// 3 requests in total...
		verify(nodeFileRequesterService, times(3)).getNodeFileCreationRequestResponse(jobCaptor.capture());

		// first to link multipart acquisition to experiment file
		verifyRequest(jobCaptor.getAllValues().get(0), experimentUrl, multipartAcquisitionUrl);

		// second to link scan 1 to multipart acquisition file
		verifyRequest(jobCaptor.getAllValues().get(1), multipartAcquisitionUrl, scan1Url);

		// third to link scan 2 to multipart acquisition file
		verifyRequest(jobCaptor.getAllValues().get(2), multipartAcquisitionUrl, scan2Url);
	}

	@Test
	public void testTreeAfterStoppingMultipartAcquisition() throws Exception {
		var experimentUrl = getController().startExperiment(EXPERIMENT_NAME);

		getController().startMultipartAcquisition(ACQUISITION_NAME);
		getController().prepareAcquisition(ACQUISITION_NAME+1);
		getController().stopMultipartAcquisition();
		var scan2Url = getController().prepareAcquisition(ACQUISITION_NAME+2);

		verify(nodeFileRequesterService, times(3)).getNodeFileCreationRequestResponse(jobCaptor.capture());
		/* first two requests identical to those in eachAcquisitionWithinMultipartAcquisitionTriggersRequest()
		   but scan2 is child of experiment */
		verifyRequest(jobCaptor.getAllValues().get(2), experimentUrl, scan2Url);
	}

	private void verifyRequest(NodeInsertionRequest request, URL parent, URL child) {
		assertThat(request.getNodeLocation(), is(equalTo(parent)));
		assertThat(request.getChildren().values().iterator().next(), is(equalTo(child)));
	}

}
