/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera;

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;
import static gda.configuration.properties.LocalProperties.GDA_MODE;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.gda.client.properties.ConfigurationPropertiesTestConfiguration;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationPropertiesTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class CameraHelperTest1 {

	@BeforeClass
	public static void beforeClass() {
        System.setProperty(GDA_CONFIG, "test/resources/cameraHelperTest1");
        System.setProperty(GDA_MODE, "custom");
	}

	@AfterClass
	public static void afterClass() {
		System.clearProperty(GDA_CONFIG);
		System.clearProperty(GDA_MODE);
	}

	@Test
	public void simpleDetectorConfigurationTest() {
		List<CameraConfigurationProperties> camerasProperties = CameraHelper.getAllCameraConfigurationProperties();
		Assert.assertEquals(5, camerasProperties.size());
		CameraConfigurationProperties cp = camerasProperties.get(0);
		Assert.assertEquals("PCO_CAMERA", cp.getId());
		Assert.assertEquals(345.0, cp.getReadoutTime(), 0.0);

		Assert.assertEquals("d5_cam_config", camerasProperties.get(3).getConfiguration());
	}

	@Test
	public void readCustomConfigurationTest() {
		Assert.assertEquals("d5_cam_config", CameraHelper.getAllCameraConfigurationProperties().get(3).getConfiguration());
	}

	@Test
	public void readOverrideCustomConfigurationTest() {
		Assert.assertEquals("d11_cam_config", CameraHelper.getAllCameraConfigurationProperties().get(4).getConfiguration());
	}
}
