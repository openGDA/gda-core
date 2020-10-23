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

package uk.ac.diamond.daq.mapping.ui.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.service.IScanningAcquisitionService;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.FinderService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ScanningAcquisitionControllerConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ScanningAcquisitionControllerTest {

	private ScanningAcquisitionController controller;

	@Autowired
	FinderService finderService;

	@Autowired
	IScanningAcquisitionService scanningAcquisitionService;

	@Before
	public void before() {
		controller = Optional.ofNullable(SpringApplicationContextFacade.getBean("scanningAcquisitionController",
													AcquisitionsPropertiesHelper.AcquisitionPropertyType.TOMOGRAPHY))
				.map(ScanningAcquisitionController.class::cast)
				.orElseGet(() -> null);
	}


	@Test
	public void testControllerWithoutNewScanningAcquisitionSupplier() {
		controller.createNewAcquisition();
		Assert.assertNotNull(controller.getAcquisition());
		Assert.assertNull(controller.getAcquisition().getAcquisitionConfiguration());
	}

	/**
	 * The new scanning acquisition without configured acquisitions types does not create the ImageCalibration document
	 */
	@Test
	public void testControllerWithNewScanningAcquisitionSupplierNoAcquistionProperties() {
		Supplier<ScanningAcquisition> newScanningAcquisitionSupplier = getScanningAcquisitionSupplier();
		controller.setDefaultNewAcquisitionSupplier(newScanningAcquisitionSupplier);
		controller.createNewAcquisition();
		Assert.assertNull(controller.getAcquisition().getAcquisitionConfiguration().getImageCalibration());
	}

	/**
	 * The new scanning acquisition without configured cameras does not create the ImageCalibration document
	 * @throws IOException
	 */
	@Test
	public void testControllerWithNewScanningAcquisitionSupplierNoCameras() throws IOException {
		File properties = new File("test/resources/acquisitions.properties");
		readProperties(properties);

		Supplier<ScanningAcquisition> newScanningAcquisitionSupplier = getScanningAcquisitionSupplier();
		controller.setDefaultNewAcquisitionSupplier(newScanningAcquisitionSupplier);
		controller.createNewAcquisition();
		Assert.assertNull(controller.getAcquisition().getAcquisitionConfiguration().getImageCalibration());

		removeProperties(properties);
	}

	/**
	 * The new scanning acquisition without configured cameras has an ImageCalibration document
	 * @throws IOException
	 */
	@Test
	public void testControllerWithNewScanningAcquisitionSupplier() throws IOException {
		CameraControl cameraControl = mock(CameraControl.class);
		doReturn("imaging_camera_control").when(cameraControl).getName();
		doReturn(Optional.of(cameraControl)).when(finderService).getFindableObject("imaging_camera_control");

		File properties = new File("test/resources/acquisitionsAndCameras.properties");
		readProperties(properties);

		Supplier<ScanningAcquisition> newScanningAcquisitionSupplier = getScanningAcquisitionSupplier();
		controller.setDefaultNewAcquisitionSupplier(newScanningAcquisitionSupplier);
		controller.createNewAcquisition();
		ImageCalibration ic = controller.getAcquisition().getAcquisitionConfiguration().getImageCalibration();
		Assert.assertNotNull(ic);
		Assert.assertNotNull(ic.getDarkCalibration());
		Assert.assertNotNull(ic.getDarkCalibration());
		Assert.assertNotNull(ic.getFlatCalibration());

		removeProperties(properties);
	}

	private Supplier<ScanningAcquisition> getScanningAcquisitionSupplier() {
		return () -> {
			ScanningAcquisition newConfiguration = new ScanningAcquisition();
			newConfiguration.setUuid(UUID.randomUUID());
			ScanningConfiguration configuration = new ScanningConfiguration();
			newConfiguration.setAcquisitionConfiguration(configuration);

			ScanningParameters acquisitionParameters = new ScanningParameters();
			newConfiguration.getAcquisitionConfiguration().setAcquisitionParameters(acquisitionParameters);
			return newConfiguration;
		};
	}

	private void readProperties(File resource) {
		Optional.ofNullable(resource)
				.ifPresent(r -> System.setProperty(LocalProperties.GDA_PROPERTIES_FILE, resource.getPath()));
		LocalProperties.reloadAllProperties();
		CameraHelper.loadAllProperties();
		AcquisitionsPropertiesHelper.reloadProperties();
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
