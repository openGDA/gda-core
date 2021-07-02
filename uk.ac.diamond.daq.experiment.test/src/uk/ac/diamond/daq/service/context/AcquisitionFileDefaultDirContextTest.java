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

package uk.ac.diamond.daq.service.context;

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;
import static gda.configuration.properties.LocalProperties.GDA_PROPERTIES_FILE;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;

import org.eclipse.scanning.api.scan.IFilePathService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.data.ServiceHolder;
import uk.ac.gda.core.tool.spring.DiffractionContextFile;
import uk.ac.gda.core.tool.spring.DiffractionFileContext;
import uk.ac.gda.core.tool.spring.ExperimentContextFile;
import uk.ac.gda.core.tool.spring.ExperimentFileContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AcquisitionFileContextTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class AcquisitionFileDefaultDirContextTest extends AcquisitionFileContextCommonTest {

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/defaultContext");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/defaultContext/properties/_common/common_instance_java.properties");
	}

	/**
	 * If {@link IFilePathService#getProcessingDir()} is not absolute, cannot get the experiment directory
	 */
	@Test
	public void acquisitionExperimentDoesNotExistTest() {
		doReturn("dummyProcessingDir").when(getFilePathServiceMock()).getProcessingDir();
		doReturn("dummyVisitDir").when(getFilePathServiceMock()).getVisitConfigDir();
		var sh = new ServiceHolder();
		sh.setFilePathService(getFilePathServiceMock());

		var url = getAcquisitionFileContext().getExperimentContext().getContextFile(ExperimentContextFile.EXPERIMENTS_DIRECTORY);
		Assert.assertNull(url);
		url = getAcquisitionFileContext().getDiffractionContext().getContextFile(DiffractionContextFile.DIFFRACTION_CONFIGURATION_DIRECTORY);
		Assert.assertNull(url);
	}

	/**
	 * When no properties are specified the context uses their default value
	 *
	 * @throws IOException
	 */
	@Test
	public void acquisitionDefaultDirTest() throws IOException {
		prepareFilesystem();

		var url = getAcquisitionFileContext().getExperimentContext().getContextFile(ExperimentContextFile.EXPERIMENTS_DIRECTORY);
		Assert.assertNotNull(url);
		assertTrue(url.getPath().endsWith(ExperimentFileContext.EXPERIMENTS_OPERATIONAL_DIRECTORY_PROPERTY_DEFAULT));

		url = getAcquisitionFileContext().getDiffractionContext().getContextFile(DiffractionContextFile.DIFFRACTION_CONFIGURATION_DIRECTORY);
		Assert.assertNotNull(url);
		assertTrue(url.getPath().endsWith(DiffractionFileContext.DIFFRACTION_CONFIGURATION_DIRECTORY_PROPERTY_DEFAULT));
	}

}
