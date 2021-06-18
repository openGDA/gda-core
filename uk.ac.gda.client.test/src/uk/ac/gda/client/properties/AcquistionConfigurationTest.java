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

package uk.ac.gda.client.properties;

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.gda.api.acquisition.AcquisitionEngineDocument.AcquisitionEngineType;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * Tests for the {@link AcquisitionConfigurationProperties} based on a detectors.properties file.
 *
 * @author Maurizio Nagni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationPropertiesTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class AcquistionConfigurationTest {

	@Autowired
	private ClientSpringProperties cnf;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/configurationPropertiesTest");
	}

	@AfterClass
	public static void afterClass() {
		System.clearProperty(GDA_CONFIG);
	}

	/**
	 * Verifies that {@link AcquisitionPropertyType#DIFFRACTION} has one associated detector
	 */
	@Test
	public void testAcquisitionCondifigurationProperties() {
		Assert.assertEquals(3, cnf.getAcquisitions().size());

		AcquisitionConfigurationProperties acp = cnf.getAcquisitions().get(0);
		Assert.assertEquals("Diffraction engine", acp.getName());
		Assert.assertEquals(AcquisitionPropertyType.DIFFRACTION, acp.getType());
		Assert.assertEquals(1, acp.getCameras().size());
		Assert.assertEquals("localTest-ML-SCAN-01", acp.getEngine().getId());
		Assert.assertEquals(AcquisitionEngineType.MALCOLM, acp.getEngine().getType());
		Assert.assertEquals("diffraction_calibration_appender", acp.getNexusNodeCopyAppender());

		acp = cnf.getAcquisitions().get(1);
		Assert.assertEquals("Tomography engine", acp.getName());
		Assert.assertEquals(AcquisitionPropertyType.TOMOGRAPHY, acp.getType());
		Assert.assertEquals(1, acp.getCameras().size());

		Assert.assertEquals(2, acp.getProcessingRequest().getNexusTemplates().size());
		Assert.assertEquals("file:/a/path/one.yaml", acp.getProcessingRequest().getNexusTemplates().get(0).toString());
		Assert.assertEquals("file:/a/path/two.yaml", acp.getProcessingRequest().getNexusTemplates().get(1).toString());

		Assert.assertEquals("localTest-ML-SCAN-02", acp.getEngine().getId());
		Assert.assertEquals(AcquisitionEngineType.MALCOLM, acp.getEngine().getType());
		Assert.assertEquals(3, acp.getOutOfBeamScannables().size());
		Assert.assertTrue(acp.getOutOfBeamScannables().contains("gts_x"));
		Assert.assertTrue(acp.getOutOfBeamScannables().contains("gts_y"));
		Assert.assertTrue(acp.getOutOfBeamScannables().contains("gts_z"));


		acp = cnf.getAcquisitions().get(2);
		Assert.assertEquals("Beam Selector Scan", acp.getName());
		Assert.assertEquals(AcquisitionPropertyType.BEAM_SELECTOR, acp.getType());
		Assert.assertEquals(2, acp.getCameras().size());
		Assert.assertTrue(acp.getCameras().contains("PILATUS"));
		Assert.assertTrue(acp.getCameras().contains("PCO_CAMERA"));
		Assert.assertEquals("localTest-ML-SCAN-03", acp.getEngine().getId());
		Assert.assertEquals(AcquisitionEngineType.MALCOLM, acp.getEngine().getType());
	}
}
