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

import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.diamond.daq.experiment.api.structure.NodeInsertionRequest;

/**
 * Tests regarding the {@link NexusExperimentController}'s use of {@link ExperimentTreeCache}.
 */
public class NexusExperimentControllerCachingTest extends NexusExperimentControllerTestBase {

	@Autowired
	private ExperimentTreeCache experimentTreeCache;

	@Autowired
	private NodeFileRequesterService nodeFileRequesterService;

	private ArgumentCaptor<ExperimentTree> treeCaptor;

	@Before
	public void setup() throws Exception {
		/* needed when closing a) multipart acquisition and/or b) experiment */
		doReturn(new NodeInsertionRequest()).when(nodeFileRequesterService)
			.getNodeFileCreationRequestResponse(ArgumentMatchers.any());

		 treeCaptor = ArgumentCaptor.forClass(ExperimentTree.class);
	}

	@BeforeClass
	public static void beforeClass() {
		// This setProperties is necessary to not make ServerSpringProperties to fail.
		System.setProperty(GDA_CONFIG, "test/resources/defaultContext");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/defaultContext/properties/_common/common_instance_java.properties");
	}

	@Test
	public void readCacheWhenControllerCreated() throws Exception {
		verify(experimentTreeCache).restore();
	}

	@Test
	public void cacheStateWhenExperimentStarts() throws Exception {
		URL experiment = getController().startExperiment(EXPERIMENT_NAME);

		verify(experimentTreeCache).store(treeCaptor.capture());

		assertThat(treeCaptor.getValue().getActiveNode().getLocation(), is(equalTo(experiment)));
	}

	@Test
	public void cacheStateWhenAcquisitionPrepared() throws Exception {
		URL experiment = getController().startExperiment(EXPERIMENT_NAME);
		getController().prepareAcquisition(ACQUISITION_NAME);

		verify(experimentTreeCache, times(2)).store(treeCaptor.capture());

		ExperimentTree tree = treeCaptor.getValue();
		assertThat(tree.getActiveNode().getLocation(), is(equalTo(experiment)));
		assertThat(tree.getActiveNode().getChildren().size(), is(1));
	}

	@Test
	public void cacheStateWhenStartingMultipartAcquisition() throws Exception {
		getController().startExperiment(EXPERIMENT_NAME);
		URL multipart = getController().startMultipartAcquisition(ACQUISITION_NAME);

		verify(experimentTreeCache, times(2)).store(treeCaptor.capture());

		assertThat(treeCaptor.getValue().getActiveNode().getLocation(), is(equalTo(multipart)));
	}

	@Test
	public void cacheStateWhenEndingMultipartAcquisition() throws Exception {

		doReturn(new NodeInsertionRequest()).when(nodeFileRequesterService).getNodeFileCreationRequestResponse(ArgumentMatchers.any());

		URL root = getController().startExperiment(EXPERIMENT_NAME);
		getController().startMultipartAcquisition(ACQUISITION_NAME);
		getController().prepareAcquisition(ACQUISITION_NAME + "a");
		getController().prepareAcquisition(ACQUISITION_NAME + "b");
		getController().stopMultipartAcquisition();

		verify(experimentTreeCache, times(5)).store(treeCaptor.capture());

		assertThat(treeCaptor.getValue().getActiveNode().getLocation(), is(equalTo(root)));
	}

	@Test
	public void cacheStateWhenEndingExperiment() throws Exception {
		doReturn(new NodeInsertionRequest()).when(nodeFileRequesterService).getNodeFileCreationRequestResponse(ArgumentMatchers.any());
		getController().startExperiment(EXPERIMENT_NAME);
		getController().stopExperiment();

		verify(experimentTreeCache).store(null);
	}
}
