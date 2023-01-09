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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Optional;
import java.util.Set;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import gda.device.DeviceException;
import gda.device.enumpositioner.EpicsPositioner;
import uk.ac.gda.api.acquisition.Acquisition;
import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionSubType;
import uk.ac.gda.api.acquisition.TrajectoryShape;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.exception.AcquisitionControllerException;
import uk.ac.gda.client.exception.GDAClientRestException;

/**
 * Tests how the {@link Acquisition} published by the {@link ScanningAcquisitionController}
 * is received by the Acquisition Service
 *
 * @see #testRunScanningAcquisition()
 *
 * @author Maurizio Nagni
 */
public class ScanningAcquisitionControllerRunRequestTest extends ScanningAcquisitionControllerIntegrationTest {

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/scanningAcquisitionControllerTest");
	}

	/**
	 * Submit an acquisition to the service which includes start positions
	 * @throws AcquisitionControllerException
	 * @throws GDAClientRestException
	 * @throws ScanningException
	 * @throws DeviceException
	 */
	@Test
	public void testRunScanningAcquisition() throws AcquisitionControllerException, GDAClientRestException, ScanningException, DeviceException {
		var cameraControl = mock(CameraControl.class);
		doReturn("imaging_camera_control").when(cameraControl).getName();
		doReturn(Optional.of(cameraControl)).when(finderService).getFindableObject("imaging_camera_control", CameraControl.class);

		var iEventService = mock(IEventService.class);
		var irunnableDeviceService = mock(IRunnableDeviceService.class);
		IRunnableDevice<IDetectorModel> iRunnable = mock(IRunnableDevice.class);
		doReturn(iEventService).when(clientRemoteService).getIEventService();
		doReturn(irunnableDeviceService).when(clientRemoteService).getIRunnableDeviceService();
		doReturn(iRunnable).when(irunnableDeviceService).getRunnableDevice(ArgumentMatchers.any());
		doReturn(true).when(experimentClient).isExperimentInProgress();

		var baseX = spy(new EpicsPositioner());
		doReturn(Optional.of(baseX)).when(finderService).getFindableObject(eq(BASE_X), ArgumentMatchers.any());
		doReturn("DUMMY_STATE").when(baseX).getPosition();
		doReturn(BASE_X).when(baseX).getName();

		var ehShutter = spy(new EpicsPositioner());
		doReturn(Optional.of(ehShutter)).when(finderService).getFindableObject(eq(EH_SHUTTER), ArgumentMatchers.any());
		doReturn("OPEN").when(ehShutter).getPosition();
		doReturn(EH_SHUTTER).when(ehShutter).getName();

		var beamSelector = spy(new EpicsPositioner());
		doReturn(Optional.of(beamSelector)).when(finderService).getFindableObject(eq("beam_selector"), ArgumentMatchers.any());
		doReturn("DIFF").when(beamSelector).getPosition();
		doReturn("beam_selector").when(beamSelector).getName();

		var responseText = "Done";
		RunAcquisitionResponse mockResponse = new RunAcquisitionResponse.Builder()
				.withMessage(responseText)
				.withSubmitted(true)
				.build();
		ResponseEntity<RunAcquisitionResponse> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

		var acquisitionKeys = new AcquisitionKeys(AcquisitionPropertyType.TOMOGRAPHY, AcquisitionSubType.STANDARD, TrajectoryShape.ONE_DIMENSION_LINE);
		var controller = context.getAcquisitionController().orElseThrow();

		injectAcquisitionManager(controller, acquisitionKeys);

		controller.newScanningAcquisition(acquisitionKeys);

		doReturn(mockResponseEntity).when(scanningAcquisitionServer).run(controller.getAcquisition());

		controller.runAcquisition();

		var endPosition = controller.getAcquisition().getAcquisitionConfiguration().getEndPosition();
		Assert.assertTrue(containsPositionDocumentForDevice(endPosition, BASE_X));
		Assert.assertTrue(containsPositionDocumentForDevice(endPosition, EH_SHUTTER));
		Assert.assertTrue(containsPositionDocumentForDevice(endPosition, "beam_selector"));
	}

	private boolean containsPositionDocumentForDevice(Set<DevicePositionDocument> position, String deviceName) {
		return position.stream().anyMatch(document -> document.getDevice().equals(deviceName));
	}
}
