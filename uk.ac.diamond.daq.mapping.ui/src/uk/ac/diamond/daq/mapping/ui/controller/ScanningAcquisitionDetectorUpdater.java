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
import uk.ac.gda.api.acquisition.configuration.processing.FrameCaptureRequest;
import uk.ac.gda.api.acquisition.parameters.FrameRequestDocument;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;

/**
 * Creates a {@link CameraControlSpringEvent} listener which updates the detector documents in the active acquisition
 */
@Component
public class ScanningAcquisitionDetectorUpdater {

	@Autowired
	private ScanningAcquisitionTemporaryHelper acquisitionHelper;

	@PostConstruct
	private void postConstruct() {
		SpringApplicationContextFacade.addDisposableApplicationListener(this, new DetectorUpdatesListener());
	}

	private class DetectorUpdatesListener implements ApplicationListener<CameraControlSpringEvent> {

		@Override
		public void onApplicationEvent(CameraControlSpringEvent event) {

			var cameraControl = CameraHelper.getCameraControlByCameraID(event.getCameraId()).orElseThrow();
			var acquireTime = event.getAcquireTime();

			getFrameCaptureRequest().ifPresent(request -> updateFrameCaptureRequest(request, cameraControl, acquireTime));
		}

		private Optional<FrameCaptureRequest> getFrameCaptureRequest() {
			var processingRequest = acquisitionHelper.getProcessingRequest();
			if (processingRequest.isEmpty()) return Optional.empty();
			return processingRequest.get().stream()
				.filter(FrameCaptureRequest.class::isInstance).map(FrameCaptureRequest.class::cast)
				.findFirst();
		}

		private void updateFrameCaptureRequest(FrameCaptureRequest request, CameraControl cameraControl, double acquireTime) {
			request.getValue().stream()
				// do you have a detector document corresponding to this camera?
				.filter(document -> cameraControl.getName().equals(document.getDetectorController()))
				.findFirst().ifPresent(document -> {
					// if so: remove it...
					request.getValue().remove(document);

					// ...and replace it with a copy with the updated acquire time
					var updated = new FrameRequestDocument.Builder()
						.withMalcolmDetectorName(document.getMalcolmDetectorName())
						.withDetectorController(document.getDetectorController())
						.withName(document.getName())
						.withExposure(acquireTime)
						.build();
					request.getValue().add(updated);

					// let interested parties know this has changed
					SpringApplicationContextFacade.publishEvent(new ScanningAcquisitionChangeEvent(this));
				});
		}

	}

}