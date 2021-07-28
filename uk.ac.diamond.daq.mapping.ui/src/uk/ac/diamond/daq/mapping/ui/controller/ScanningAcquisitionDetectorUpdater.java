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

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.event.CameraControlSpringEvent;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionChangeEvent;
import uk.ac.diamond.daq.mapping.api.document.helper.ImageCalibrationHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.document.ClientPropertiesHelper;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;

/**
 * Updates the active {@link ScanningAcquisitionController#getAcquisition()}
 *
 * <p>
 * Listening to {@link CameraControlSpringEvent}s this class updates the detector document contained in the active acquisition
 * </p>
 *
 * @author Maurizio Nagni
 */
@Component
public class ScanningAcquisitionDetectorUpdater {

	@Autowired
	private ClientPropertiesHelper clientPropertiesHelper;

	@Autowired
	private ScanningAcquisitionTemporaryHelper tempHelper;

	@PostConstruct
	private void postConstruct() {
		SpringApplicationContextFacade.addDisposableApplicationListener(this, listenToExposureChange);
	}

	// At the moment is not possible to use anonymous lambda expression because it
	// generates a class cast exception
	private ApplicationListener<CameraControlSpringEvent> listenToExposureChange = new ApplicationListener<CameraControlSpringEvent>() {
		@Override
		public void onApplicationEvent(CameraControlSpringEvent event) {
			getClientPropertiesHelper().getAcquisitionConfigurationProperties(getAcquisitionType())
				.filter(p -> p.getCameras().contains(event.getCameraId()))
				.ifPresent(p -> updateDetectorDocument(event));
		}

		private Optional<AcquisitionConfigurationProperties> getAcquisitionPropertiesDocument() {
			return getClientPropertiesHelper().getAcquisitionConfigurationProperties(getTempHelper().getAcquisitionType());
		}

		private AcquisitionPropertyType getAcquisitionType() {
			return getAcquisitionPropertiesDocument()
					.map(AcquisitionConfigurationProperties::getType)
					.orElseGet(() -> AcquisitionPropertyType.DEFAULT);
		}

		private void updateDetectorDocument(CameraControlSpringEvent event) {
			CameraHelper.getCameraControlByCameraID(event.getCameraId())
				.ifPresent(cameraControl -> updateDetectorDocument(cameraControl, event.getAcquireTime()));
		}

		private void updateDetectorDocument(CameraControl cameraControl, double acquireTime) {
			double readoutTime = CameraHelper.getCameraConfigurationPropertiesByCameraControlName(cameraControl.getName())
				.map(CameraConfigurationProperties::getReadoutTime)
				.orElse(0.0);

			String malcolmDetectorName = CameraHelper.getCameraConfigurationPropertiesByCameraControlName(cameraControl.getName())
					.map(CameraConfigurationProperties::getMalcolmDetectorName)
					.orElse("NotAvilable");

			boolean isRightCameraControl = getTempHelper().getScanningParameters()
				.map(ScanningParameters::getDetector)
				.map(DetectorDocument::getName)
				.filter(n -> cameraControl.getName().equals(n))
				.isPresent();

			if (!isRightCameraControl)
				return;

			getTempHelper().getAcquisitionConfiguration()
				.ifPresent(c -> {
					var imageCalibrationHelper = new ImageCalibrationHelper(() -> c);
					var detectorDocument = new DetectorDocument.Builder()
						.withName(cameraControl.getName())
						.withExposure(acquireTime)
						.withReadout(readoutTime)
						.withMalcolmDetectorName(malcolmDetectorName)
						.build();

					getTempHelper().getScanningParameters()
						.ifPresent(acquisitionParameters -> {
							acquisitionParameters.setDetector(detectorDocument);
							imageCalibrationHelper.updateDarkDetectorDocument(detectorDocument);
							imageCalibrationHelper.updateFlatDetectorDocument(detectorDocument);
							SpringApplicationContextFacade.publishEvent(new ScanningAcquisitionChangeEvent(this));
						});
				});
		}
	};

	private ScanningAcquisitionTemporaryHelper getTempHelper() {
		return tempHelper;
	}

	private ClientPropertiesHelper getClientPropertiesHelper() {
		return clientPropertiesHelper;
	}
}