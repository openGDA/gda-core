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
public class CameraHelperTest2 {

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/cameraHelperTest2");
	}

	@AfterClass
	public static void afterClass() {
		System.clearProperty(GDA_CONFIG);
	}

	@Test
	public void stringReadoutDetectorConfigurationTest() {
		List<CameraConfigurationProperties> camerasProperties = CameraHelper.getAllCameraConfigurationProperties();
		Assert.assertEquals(1, camerasProperties.size());
		CameraConfigurationProperties cp = camerasProperties.get(0);
	}
}
