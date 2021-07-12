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

import java.io.IOException;

import org.eclipse.scanning.api.scan.IFilePathService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.data.ServiceHolder;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
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

	@Rule
	public TemporaryFolder testDirectory = new TemporaryFolder();

	protected IFilePathService filePathService;

	@Before
	public void prepareFileSystem() throws IOException {
		filePathService = mock(IFilePathService.class);

		doReturn(testDirectory.newFolder("visit").getAbsolutePath()).when(filePathService).getVisitDir();
		doReturn(testDirectory.newFolder("processing").getAbsolutePath()).when(filePathService).getProcessingDir();
		doReturn(testDirectory.newFolder("xml").getAbsolutePath()).when(filePathService).getVisitConfigDir();
		var sh = new ServiceHolder();
		sh.setFilePathService(filePathService);
	}

	protected AcquisitionFileContext getContext() {
		return context;
	}

	protected ExperimentController getController() {
		return controller;
	}
}
