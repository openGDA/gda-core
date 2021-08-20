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

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;
import static gda.configuration.properties.LocalProperties.GDA_PROPERTIES_FILE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
import gda.device.DeviceException;
import gda.device.enumpositioner.EpicsPositioner;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.gda.api.acquisition.Acquisition;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument.ValueType;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.exception.AcquisitionControllerException;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.client.properties.acquisition.AcquisitionKeys;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.stage.position.Position;
import uk.ac.gda.test.helpers.ClassLoaderInitializer;
import uk.ac.gda.ui.tool.rest.ExperimentControllerServiceClient;
import uk.ac.gda.ui.tool.rest.ScanningAcquisitionRestServiceClient;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;
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
@ContextConfiguration(classes = { ScanningAcquisitionControllerConfiguration.class }, initializers = {ClassLoaderInitializer.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ScanningAcquisitionControllerRunRequestTestModeTest {

	private static final String MOTOR_X = "motor_x";
	private static final String EH_SHUTTER = "eh_shutter";

	@Autowired
	private ScanningAcquisitionRestServiceClient scanningAcquisitionServer;

	@Autowired
	private ExperimentControllerServiceClient experimentClient;

	@Autowired
	private FinderService finderService;

	@Autowired
	DocumentMapper documentMapper;

	@Autowired
	StageController stageController;

	@Autowired
	private ClientSpringContext context;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/scanningAcquisitionControllerTestModeTest");
	}

	@AfterClass
	public static void afterClass() {
		System.clearProperty(GDA_CONFIG);
        System.clearProperty(GDA_PROPERTIES_FILE);
	}

	@Before
	public void before() {
		LocalProperties.reloadAllProperties();
	}

	private DevicePositionDocument createShutterOpenRequest() {
		var builder = new DevicePositionDocument.Builder();
		builder.withValueType(ValueType.LABELLED);
		builder.withDevice(EH_SHUTTER);
		builder.withLabelledPosition(Position.Open.toString());
		return builder.build();
	}

	/**
	 * Submit an acquisition to the service which includes start positions but excludes eh_shutter
	 * as required by the testMode properties configuration
	 *
	 * @throws AcquisitionControllerException
	 * @throws GDAClientRestException
	 * @throws DeviceException
	 */
	@Test
	public void testRunScanningAcquisition() throws AcquisitionControllerException, GDAClientRestException, DeviceException {
		var cameraControl = mock(CameraControl.class);
		doReturn("imaging_camera_control").when(cameraControl).getName();
		doReturn(Optional.of(cameraControl)).when(finderService).getFindableObject("imaging_camera_control", CameraControl.class);
		doReturn(true).when(experimentClient).isExperimentInProgress();

		var ehShutter = spy(new EpicsPositioner());
		doReturn(Optional.of(ehShutter)).when(finderService).getFindableObject(eq(EH_SHUTTER), ArgumentMatchers.any());
		doReturn("OPEN").when(ehShutter).getPosition();
		doReturn(EH_SHUTTER).when(ehShutter).getName();

		var responseText = "Done";
		RunAcquisitionResponse mockResponse = new RunAcquisitionResponse.Builder()
				.withMessage(responseText)
				.withSubmitted(true)
				.build();
		ResponseEntity<RunAcquisitionResponse> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

		var acquisitionKeys = new AcquisitionKeys(AcquisitionPropertyType.TOMOGRAPHY, AcquisitionTemplateType.ONE_DIMENSION_LINE);
		var controller = context.getAcquisitionController().orElseThrow();
		controller.newScanningAcquisition(acquisitionKeys);

		doReturn(mockResponseEntity).when(scanningAcquisitionServer).run(controller.getAcquisition());

		RunAcquisitionResponse response = controller.runAcquisition();
		Assert.assertEquals(responseText, response.getMessage());
		Assert.assertTrue(response.isSubmitted());

		Assert.assertFalse(controller.getAcquisition().getAcquisitionConfiguration()
				.getAcquisitionParameters().getStartPosition().contains(createShutterOpenRequest()));

	}
}
