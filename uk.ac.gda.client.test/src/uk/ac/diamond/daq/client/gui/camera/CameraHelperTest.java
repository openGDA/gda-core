/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.properties.CameraProperties;
import uk.ac.gda.ui.tool.spring.FinderService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CameraHelperTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class CameraHelperTest {

	@Autowired
	private FinderService finder;

	@Test
	public void emptyDetectorConfigurationTest() {
		List<CameraProperties> camerasProperties = CameraHelper.getAllCameraProperties();
		Assert.assertEquals(0, camerasProperties.size());
	}

	@Test
	public void simpleDetectorConfigurationTest() throws Exception {
		File properties = new File("test/resources/detectorConfiguration_1.properties");
		readProperties(properties);

		List<CameraProperties> camerasProperties = CameraHelper.getAllCameraProperties();
		Assert.assertEquals(1, camerasProperties.size());
		CameraProperties cp = camerasProperties.get(0);
		String id = cp.getId().orElseThrow(Exception::new);
		Assert.assertEquals("PCO_CAMERA", id);
		Assert.assertEquals(0, cp.getReadoutTime());

		removeProperties(properties);
	}

	@Test
	public void valuedReadoutDetectorConfigurationTest() throws Exception {
		File properties = new File("test/resources/valuedReadoutDetectorConfiguration.properties");
		readProperties(properties);

		List<CameraProperties> camerasProperties = CameraHelper.getAllCameraProperties();
		Assert.assertEquals(1, camerasProperties.size());
		CameraProperties cp = camerasProperties.get(0);
		Assert.assertEquals(345, cp.getReadoutTime());

		removeProperties(properties);
	}

	@Test
	public void stringReadoutDetectorConfigurationTest() throws Exception {
		when(finder.getFindableObject("pco_cam_config")).thenReturn(Optional.of(mock(CameraControl.class)));

		File properties = new File("test/resources/stringReadoutDetectorConfiguration.properties");
		readProperties(properties);

		readProperties(new File("test/resources/stringReadoutDetectorConfiguration.properties"));
		List<CameraProperties> camerasProperties = CameraHelper.getAllCameraProperties();
		Assert.assertEquals(1, camerasProperties.size());
		CameraProperties cp = camerasProperties.get(0);
		Assert.assertEquals(0, cp.getReadoutTime());

		removeProperties(properties);
	}

	private void readProperties(File resource) {
		Optional.ofNullable(resource)
				.ifPresent(r -> System.setProperty(LocalProperties.GDA_PROPERTIES_FILE, resource.getPath()));
		LocalProperties.reloadAllProperties();
		CameraHelper.loadAllProperties();
	}

	/**
	 * This method is required
	 * - Junit does not reinstatiate LocalProperties on each test
	 * - LocalProperties appends each new test properties file
	 * What worst new tests see old properties, consequently this method is necessary to remove the OLD properties and
	 * as such is appended after each test
	 *
	 * @param properties
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void removeProperties(File properties) throws IOException {
		Properties testProps = new Properties();
		testProps.load(new FileInputStream(properties));
		testProps.keySet().stream()
			.map(String.class::cast)
		.forEach(LocalProperties::clearProperty);
	}
}
