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

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.core.tool.spring.DiffractionContextFile;
import uk.ac.gda.core.tool.spring.ExperimentContextFile;
import uk.ac.gda.core.tool.spring.TomographyContextFile;

public class AcquisitionFileCustomExperimentContextTest extends AcquisitionFileContextTestBase {

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/customExperiment");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/customExperiment/properties/_common/common_instance_java.properties");
	}

	/**
	 * When properties exist the context uses their value
	 *
	 * @throws IOException
	 */
	@Test
	public void acquisitionCustomExperimentTest() throws IOException {
		prepareFilesystem();

		var url = getAcquisitionFileContext().getDiffractionContext().getContextFile(DiffractionContextFile.DIFFRACTION_CONFIGURATION_DIRECTORY);
		Assert.assertNotNull(url);
		assertTrue(url.getPath().endsWith("customConfiguration"));

		url = getAcquisitionFileContext().getDiffractionContext().getContextFile(DiffractionContextFile.DIFFRACTION_CALIBRATION_DIRECTORY);
		Assert.assertNotNull(url);
		assertTrue(url.getPath().endsWith("customCalibration"));

		url = getAcquisitionFileContext().getTomographyContext().getContextFile(TomographyContextFile.TOMOGRAPHY_CONFIGURATION_DIRECTORY);
		Assert.assertNotNull(url);
		assertTrue(url.getPath().endsWith("customConfiguration"));

		url = getAcquisitionFileContext().getTomographyContext().getContextFile(TomographyContextFile.TOMOGRAPHY_SAVU_DIRECTORY);
		Assert.assertNotNull(url);
		assertTrue(url.getPath().endsWith("savu"));

		url = getAcquisitionFileContext().getExperimentContext().getContextFile(ExperimentContextFile.EXPERIMENTS_DIRECTORY);
		Assert.assertNotNull(url);
		assertTrue(url.getPath().endsWith("customExperiment"));
	}
}
