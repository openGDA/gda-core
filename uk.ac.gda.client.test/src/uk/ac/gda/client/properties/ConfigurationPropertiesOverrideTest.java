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
import static gda.configuration.properties.LocalProperties.GDA_MODE;

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

import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument.AcquisitionEngineType;
import uk.ac.gda.api.camera.ImageMode;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.properties.controller.ControllerConfiguration;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * Test ClientSpringProperties capabilities to override multiple properties files
 *
 * @see ClientSpringProperties
 *
 * @author Maurizio Nagni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationPropertiesTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ConfigurationPropertiesOverrideTest {

	@Autowired
	private ClientSpringProperties cnf;

	@BeforeClass
	public static void beforeClass() {
		// Setting the "gda.mode" allows ClientSpringProperties to load the second @PropertySource
		// which otherwise would have been unavailable
		System.setProperty(GDA_MODE, "dummy");
		System.setProperty(GDA_CONFIG, "test/resources/configurationPropertiesTest");
	}

	@AfterClass
	public static void afterClass() {
		System.clearProperty(GDA_MODE);
		System.clearProperty(GDA_CONFIG);
	}

	@Test
	public void testControllerWithoutNewScanningAcquisitionSupplier() {
		Assert.assertEquals(3, cnf.getCameras().size());

		CameraConfigurationProperties pco = cnf.getCameras().get(0);
		//----OVERRIDE FROM DUMMY----
		Assert.assertFalse(pco.getId().equals("PCO_CAMERA"));
		Assert.assertTrue(pco.getId().equals("DummyPCO"));
		//----OVERRIDE FROM DUMMY----
		Assert.assertEquals("Imaging Camera", pco.getName());
		Assert.assertEquals("imaging_camera_control", pco.getCameraControl());
		Assert.assertEquals("customDriverX", pco.getCameraToBeamMap().getDriver().get(0));
		Assert.assertEquals("customDriverY", pco.getCameraToBeamMap().getDriver().get(1));
		Assert.assertEquals(1.0, pco.getCameraToBeamMap().getMap().getEntry(0, 0), 0.0);
		Assert.assertEquals(2.0, pco.getCameraToBeamMap().getMap().getEntry(0, 1), 0.0);
		Assert.assertEquals(-1.0, pco.getCameraToBeamMap().getOffset().getEntry(0), 0.0);
		Assert.assertEquals(-2.0, pco.getCameraToBeamMap().getOffset().getEntry(1), 0.0);

		Assert.assertEquals(0, pco.getStreamingConfiguration().getTriggerMode(), 0.0);
		Assert.assertEquals(ImageMode.CONTINUOUS, pco.getStreamingConfiguration().getImageMode());
		Assert.assertTrue(pco.getStreamingConfiguration().isActive());

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
		Assert.assertEquals(4, pilatus.getStreamingConfiguration().getTriggerMode(), 0.0);
		Assert.assertEquals(null, pilatus.getStreamingConfiguration().getImageMode());
		Assert.assertFalse(pilatus.getStreamingConfiguration().isActive());

		cameraMotor = pilatus.getMotors().get(0);
		Assert.assertTrue(cameraMotor.getController().equals("pco_x"));
		//----OVERRIDE FROM DUMMY----
		Assert.assertFalse(cameraMotor.getName().equals("X"));
		Assert.assertTrue(cameraMotor.getName().equals("DummyMotor"));
		//----OVERRIDE FROM DUMMY----
		cameraMotor = pco.getMotors().get(1);
		Assert.assertEquals("pco_z", cameraMotor.getController());
		Assert.assertEquals("Z", cameraMotor.getName());

		CameraConfigurationProperties d4 = cnf.getCameras().get(2);
		Assert.assertEquals("d4_cam_config", d4.getConfiguration());
		Assert.assertEquals(null, d4.getId());
		//----OVERRIDE FROM DUMMY----
		Assert.assertFalse(d4.getName().equals("Diagnostic Camera 4"));
		Assert.assertTrue(d4.getName().equals("DummyCamera"));
		//----OVERRIDE FROM DUMMY----
		Assert.assertEquals("d4_camera_control", d4.getCameraControl());
		Assert.assertFalse(d4.getCameraToBeamMap().isActive());

		AcquisitionConfigurationProperties acquisition = cnf.getAcquisitions().get(0);
		Assert.assertEquals("Diffraction engine", acquisition.getName());
		//----OVERRIDE FROM DUMMY----
		Assert.assertFalse(AcquisitionPropertyType.DIFFRACTION.equals(acquisition.getType()));
		Assert.assertTrue(AcquisitionPropertyType.CALIBRATION.equals(acquisition.getType()));
		//----OVERRIDE FROM DUMMY----

		Assert.assertEquals(1, acquisition.getCameras().size());
		Assert.assertTrue(acquisition.getCameras().contains("PILATUS"));
		Assert.assertEquals("localTest-ML-SCAN-01", acquisition.getEngine().getId());
		Assert.assertTrue(acquisition.getEngine().getType().equals(AcquisitionEngineType.MALCOLM));

		//----OVERRIDE FROM DUMMY----
		acquisition = cnf.getAcquisitions().get(2);
		Assert.assertFalse(acquisition.getCameras().size() == 2);
		Assert.assertFalse(acquisition.getCameras().contains("PILATUS"));
		Assert.assertFalse(acquisition.getCameras().contains("PCO_CAMERA"));

		Assert.assertEquals(1, acquisition.getCameras().size());
		Assert.assertTrue(acquisition.getCameras().contains("DummyCamera"));
		//----OVERRIDE FROM DUMMY----
	}

}
