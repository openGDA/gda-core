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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.event.CameraControlSpringEvent;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionEvent;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionPropertiesDocument;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.properties.CameraProperties;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Manages a set of {@link AcquisitionPropertiesDocument} on behalf of a {@link ScanningAcquisitionController}
 *
 * <p>
 * The {@link AcquisitionPropertyType}, required by the controller in the constructor, selects the sets of
 * {@code AcquisitionPropertiesDocument}. Using one {@code AcquisitionPropertiesDocument} this object is able to
 * <ul>
 * <li>identify the acquisition engine to use</li>
 * <li>to read from and write to a number of detectors using the associated {@code CameraControl}s</li>
 * <li>to fill a new {@code ScanningAcquisition} with the {@code AcquisitionEngineDocument} and the associated
 * {@code DetectorDocument}</li>
 * </ul>
 * </p>
 *
 * Please note that
 * <ol>
 * <li>this class is restricted to <i>package</i> as it is supposed to be used only by the
 * {@link ScanningAcquisitionController}</li>
 * <li>{@link ScanningAcquisitionController} instantiates a new instance of this class every time changes its internal
 * {@code ScanningAcquisition}.</li>
 * </ol>
 *
 *
 * @author Maurizio Nagni
 *
 * @see AcquisitionsPropertiesHelper
 */
class ScanningAcquisitionControllerDetectorHelper {
	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionControllerDetectorHelper.class);

	private final List<AcquisitionPropertiesDocument> acquisitionPropertiesDocuments = new ArrayList<>();
	private final Supplier<ScanningAcquisition> acquisitionSupplier;

	/**
	 * These are the camera associated with this acquisition controller. Some specific acquisition may use more than one
	 * camera, as BeamSelector scan in DIAD (K11)
	 */
	private List<CameraControl> camerasControls = new ArrayList<>();

	/**
	 * Constructs an object to handle detectors for specific {@code AcquisitionType}. See
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/K11+GDA+Properties">Confluence</a>
	 *
	 * @param acquisitionType
	 *            the acquisition type for this controller
	 * @param acquisitionSupplier
	 *            a reference to the {@code ScanningAcquisitionController#getAcquisition()}
	 */
	ScanningAcquisitionControllerDetectorHelper(AcquisitionPropertyType acquisitionType,
			Supplier<ScanningAcquisition> acquisitionSupplier) {
		this.acquisitionSupplier = acquisitionSupplier;
		Optional.ofNullable(AcquisitionsPropertiesHelper.getAcquistionPropertiesDocument(acquisitionType))
				.ifPresent(acquisitionPropertiesDocuments::addAll);
		camerasControls = setCamerasControls();
		SpringApplicationContextProxy.addDisposableApplicationListener(this, listenToExposureChange);

		// may happens if the controller still has no acquisition document
		if (getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters() == null)
			return;

		loadDetectorDocument();
		createAcquisitionEngineDocumentIfMissing();
	}

	private void loadDetectorDocument() {
		if (getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters().getDetector() != null) {
			camerasControls.stream().forEach(this::updateDetector);
		} else {
			createDetectorDocument();
		}
	}

	private void createAcquisitionEngineDocumentIfMissing() {
		// This condition usually happens with a new ScanningAcquisition
		if (getAcquisition().getAcquisitionEngine() == null) {
			AcquisitionEngineDocument aed = createNewAcquisitionEngineDocument();
			getAcquisition().setAcquisitionEngine(aed);
		}
	}

	private void createDetectorDocument() {
		camerasControls.stream().forEach(cc -> {
			try {
				ScanningParameters tp = getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
				tp.setDetector(new DetectorDocument(cc.getName(), cc.getAcquireTime()));
			} catch (DeviceException e) {
				logger.warn("Cannot read exposure time.", e);
			}
		});
	}

	private List<CameraControl> setCamerasControls() {
		AcquisitionPropertiesDocument dp = getAcquisitionPropertiesDocument();
		if (dp == null)
			return new ArrayList<>();

		return dp.getCameras().stream()
			.map(CameraHelper::getCameraPropertiesByID)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(CameraProperties::getIndex)
			.map(CameraHelper::getCameraControl)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

	private void updateDetector(CameraControl cameraControl) {
		DetectorDocument detector = getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters()
				.getDetector();
		if (detector == null)
			return;

		if (cameraControl.getName().equals(detector.getName()) && detector.getExposure() > 0) {
			// If the DetectorDocument has name and exposure, sets the CameraControl
			try {
				cameraControl.setAcquireTime(detector.getExposure());
			} catch (DeviceException e) {
				UIHelper.showError("Cannot update detector camera exposure time.", e, logger);
			}
		}
	}

	private AcquisitionEngineDocument createNewAcquisitionEngineDocument() {
		AcquisitionPropertiesDocument dp = getAcquisitionPropertiesDocument();
		AcquisitionEngineDocument.Builder engineBuilder = new AcquisitionEngineDocument.Builder();
		if (dp != null) {
			engineBuilder.withId(dp.getEngine().getId());
			engineBuilder.withType(dp.getEngine().getType());
		}
		return engineBuilder.build();
	}

	private AcquisitionPropertiesDocument getAcquisitionPropertiesDocument() {
		if (acquisitionPropertiesDocuments.isEmpty()) {
			return null;
		}
		int index = 0; // in future may be parametrised
		return acquisitionPropertiesDocuments.get(index);
	}

	// At the moment is not possible to use anonymous lambda expression because it
	// generates a class cast exception
	private ApplicationListener<CameraControlSpringEvent> listenToExposureChange = new ApplicationListener<CameraControlSpringEvent>() {
		@Override
		public void onApplicationEvent(CameraControlSpringEvent event) {
			camerasControls.stream()
				.map(CameraControl::getName)
				.filter(detectorControlName -> detectorControlName.equals(event.getName()))
				.forEach(detectorControlName -> updateDetectorDocument(detectorControlName, event.getAcquireTime()));
		}

		private void updateDetectorDocument(String detectorControlName, double acquireTime) {
			ScanningParameters acquisitionParameters = getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
			// The acquisition configuration may not include this detector
			if (!detectorControlName.equals(acquisitionParameters.getDetector().getName()))
				return;
			acquisitionParameters.setDetector(new DetectorDocument(acquisitionParameters.getDetector().getName(), acquireTime));
			SpringApplicationContextProxy.publishEvent(new ScanningAcquisitionEvent(getAcquisition()));
		}
	};

	private ScanningAcquisition getAcquisition() {
		return acquisitionSupplier.get();
	}
}
