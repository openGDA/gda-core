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

import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.properties.controller.ControllerConfiguration;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * Test ClientSpringProperties capabilities to parse a property file.
 *
 * @author Maurizio Nagni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationPropertiesTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ConfigurationPropertiesTest {

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

	@Test
	public void testControllerWithoutNewScanningAcquisitionSupplier() {
		Assert.assertEquals(3, cnf.getCameras().size());

		CameraConfigurationProperties pco = cnf.getCameras().get(0);
		Assert.assertEquals("pco_cam_config", pco.getConfiguration());
		Assert.assertEquals("PCO_CAMERA", pco.getId());
		Assert.assertEquals("Imaging Camera", pco.getName());
		Assert.assertEquals("imaging_camera_control", pco.getCameraControl());
		Assert.assertEquals("customDriverX", pco.getCameraToBeamMap().getDriver().get(0));
		Assert.assertEquals("customDriverY", pco.getCameraToBeamMap().getDriver().get(1));
		Assert.assertEquals(1.0, pco.getCameraToBeamMap().getMap()[0][0], 0.0);
		Assert.assertEquals(2.0, pco.getCameraToBeamMap().getMap()[0][1], 0.0);
		Assert.assertEquals(-1.0, pco.getCameraToBeamMap().getOffset().getEntry(0), 0.0);
		Assert.assertEquals(-2.0, pco.getCameraToBeamMap().getOffset().getEntry(1), 0.0);
		Assert.assertTrue(pco.getCameraToBeamMap().isActive());
		Assert.assertEquals(2, pco.getMotors().size());

		ControllerConfiguration cameraMotor = pco.getMotors().get(0);
		Assert.assertTrue(cameraMotor.getController().equals("pco_x"));
		Assert.assertEquals("X", cameraMotor.getName());
		cameraMotor = pco.getMotors().get(1);
		Assert.assertEquals("pco_z", cameraMotor.getController());
		Assert.assertEquals("Z", cameraMotor.getName());


		CameraConfigurationProperties pilatus = cnf.getCameras().get(1);
		Assert.assertEquals("pilatus_cam_config", pilatus.getConfiguration());
		Assert.assertEquals("PILATUS", pilatus.getId());
		Assert.assertEquals("Diffraction Camera", pilatus.getName());
		Assert.assertEquals("diffraction_camera_control", pilatus.getCameraControl());
		Assert.assertEquals(2, pilatus.getMotors().size());
		Assert.assertNull(pilatus.getCameraToBeamMap());

		cameraMotor = pilatus.getMotors().get(0);
		Assert.assertTrue(cameraMotor.getController().equals("pco_x"));
		Assert.assertEquals("X", cameraMotor.getName());
		cameraMotor = pco.getMotors().get(1);
		Assert.assertEquals("pco_z", cameraMotor.getController());
		Assert.assertEquals("Z", cameraMotor.getName());

		CameraConfigurationProperties d4 = cnf.getCameras().get(2);
		Assert.assertEquals("d4_cam_config", d4.getConfiguration());
		Assert.assertEquals(null, d4.getId());
		Assert.assertEquals("Diagnostic Camera 4", d4.getName());
		Assert.assertEquals("d4_camera_control", d4.getCameraControl());
		Assert.assertFalse(d4.getCameraToBeamMap().isActive());
	}

}
