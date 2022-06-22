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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.data.ServiceHolder;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.NodeInsertionRequest;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.test.helpers.ClassLoaderInitializer;

/**
 * Base class for {@link NexusExperimentController} tests,
 * handling initialisation and mocking of all necessary components.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { NexusExperimentControllerTestConfiguration.class }, initializers = {ClassLoaderInitializer.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class NexusExperimentControllerTestBase {

	protected static final String EXPERIMENT_NAME = "MyExperiment";
	protected static final String ACQUISITION_NAME = "MyMeasurement";

	@Autowired
	private AcquisitionFileContext context;

	@Autowired
	private ExperimentController controller;

	@Autowired
	protected NodeFileRequesterService nodeFileRequesterService;

	@Rule
	public TemporaryFolder testDirectory = new TemporaryFolder();

	protected IFilePathService filePathService;

	@Before
	public void prepareFileSystem() throws Exception {
		filePathService = mock(IFilePathService.class);

		var visitDir = testDirectory.newFolder("visit").getAbsolutePath();
		when(filePathService.getVisitDir()).thenReturn(visitDir);
		when(filePathService.getNextPath(null)).thenReturn(visitDir+"/scan.nxs");
		when(filePathService.getProcessingDir()).thenReturn(testDirectory.newFolder("processing").getAbsolutePath());
		when(filePathService.getVisitConfigDir()).thenReturn(testDirectory.newFolder("xml").getAbsolutePath());
		when(filePathService.getPersistenceDir()).thenReturn(testDirectory.newFolder("var").getAbsolutePath());

		var sh = new ServiceHolder();
		sh.setFilePathService(filePathService);
	}

	@Before
	public void mockNodeFileRequestResponse() throws Exception {
		var response = new NodeInsertionRequest();
		response.setStatus(Status.COMPLETE);
		doReturn(response).when(nodeFileRequesterService).getNodeFileCreationRequestResponse(ArgumentMatchers.any());
	}

	protected AcquisitionFileContext getContext() {
		return context;
	}

	protected ExperimentController getController() {
		return controller;
	}
}
