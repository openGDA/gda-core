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
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.mapping.api.document.exception.ScanningAcquisitionServiceException;
import uk.ac.diamond.daq.mapping.api.document.helper.ImageCalibrationHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Position;
import uk.ac.gda.api.acquisition.Acquisition;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument.ValueType;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.ui.tool.rest.ScanningAcquisitionRestServiceClient;
import uk.ac.gda.ui.tool.spring.FinderService;

/**
 * Tests how the {@link Acquisition} published by the {@link ScanningAcquisitionController}
 * is received by the Acquisition Service
 *
 * @see #testRunScanningAcquisition()
 *
 * @author Maurizio Nagni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ScanningAcquisitionControllerConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ScanningAcquisitionControllerRunRequestTest {

	private static final String MOTOR_X = "motor_x";

	private ScanningAcquisitionController controller;

	@Autowired
	private ScanningAcquisitionRestServiceClient scanningAcquisitionServer;

	@Autowired
	private FinderService finderService;

	@Autowired
	DocumentMapper documentMapper;

	@Autowired
	StageController stageController;

	@Before
	public void before() {
		controller = ScanningAcquisitionController.class
				.cast(getBean("scanningAcquisitionController",AcquisitionsPropertiesHelper.AcquisitionPropertyType.TOMOGRAPHY));
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

	private Set<DevicePositionDocument> createDevicePositionDocuments() {
		Set<DevicePositionDocument> devicePositionDocuments = new HashSet<>();
		devicePositionDocuments.add(createDevicePositionDocument());
		return devicePositionDocuments;
	}

	private DevicePositionDocument createDevicePositionDocument() {
		DevicePositionDocument.Builder builder = new DevicePositionDocument.Builder();
		builder.withValueType(ValueType.NUMERIC);
		builder.withDevice(MOTOR_X);
		builder.withPosition(2.3);
		return builder.build();
	}

	/**
	 * Submit an acquisition to the service which includes start positions
	 * @throws AcquisitionControllerException
	 * @throws ScanningAcquisitionServiceException
	 */
	@Test
	public void testRunScanningAcquisition() throws AcquisitionControllerException, ScanningAcquisitionServiceException {
		CameraControl cameraControl = mock(CameraControl.class);
		doReturn("imaging_camera_control").when(cameraControl).getName();
		doReturn(Optional.of(cameraControl)).when(finderService).getFindableObject("imaging_camera_control");

		doReturn(createDevicePositionDocument()).when(stageController).createShutterOpenRequest();
		doReturn(createDevicePositionDocuments()).when(stageController)
			.getPositionDocuments(ArgumentMatchers.any(Position.START.getClass()), ArgumentMatchers.anySet());

		String responseText = "Done";
		RunAcquisitionResponse mockResponse = new RunAcquisitionResponse.Builder()
				.withMessage(responseText)
				.withSubmitted(true)
				.build();
		ResponseEntity<RunAcquisitionResponse> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);


		File properties = new File("test/resources/acquisitionsAndCameras.properties");
		readProperties(properties);

		Supplier<ScanningAcquisition> newScanningAcquisitionSupplier = getScanningAcquisitionSupplier();
		controller.setDefaultNewAcquisitionSupplier(newScanningAcquisitionSupplier);
		controller.createNewAcquisition();

		doReturn(mockResponseEntity).when(scanningAcquisitionServer).run(controller.getAcquisition());

		RunAcquisitionResponse response = controller.runAcquisition();
		Assert.assertEquals(responseText, response.getMessage());
		Assert.assertTrue(response.isSubmitted());
	}

	/**
	 * Request a flat calibration without OutOfBeam, consequently throws a {@link AcquisitionConfigurationException}
	 * @throws AcquisitionControllerException
	 */
	@Test(expected = AcquisitionConfigurationException.class)
	public void testRunScanningAcquisitionNoOutOfBeam() throws AcquisitionControllerException {
		CameraControl cameraControl = mock(CameraControl.class);
		doReturn("imaging_camera_control").when(cameraControl).getName();
		doReturn(Optional.of(cameraControl)).when(finderService).getFindableObject("imaging_camera_control");

		doReturn(new HashSet<>()).when(stageController)
			.getPositionDocuments(ArgumentMatchers.any(Position.START.getClass()), ArgumentMatchers.anySet());

		File properties = new File("test/resources/acquisitionsAndCameras.properties");
		readProperties(properties);

		Supplier<ScanningAcquisition> newScanningAcquisitionSupplier = getScanningAcquisitionSupplier();
		controller.setDefaultNewAcquisitionSupplier(newScanningAcquisitionSupplier);
		controller.createNewAcquisition();
		ImageCalibrationHelper imageCalibrationHelper = new ImageCalibrationHelper(controller.getAcquisition()::getAcquisitionConfiguration);
		imageCalibrationHelper.updateFlatAfterAcquisitionExposures(true);

		controller.runAcquisition();
	}
}
