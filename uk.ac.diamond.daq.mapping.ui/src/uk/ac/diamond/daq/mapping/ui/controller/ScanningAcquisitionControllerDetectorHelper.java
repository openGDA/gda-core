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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.CameraControlSpringEvent;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionEvent;
import uk.ac.diamond.daq.mapping.api.document.helper.ImageCalibrationHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.calibration.DarkCalibrationDocument;
import uk.ac.gda.api.acquisition.configuration.calibration.FlatCalibrationDocument;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * Manages a set of {@code AcquisitionPropertiesDocument} on behalf of a {@link ScanningAcquisitionController}
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
 */
class ScanningAcquisitionControllerDetectorHelper {
	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionControllerDetectorHelper.class);

	private final Supplier<ScanningAcquisition> acquisitionSupplier;

	/**
	 * These are the camera associated with this acquisition controller. Some specific acquisition may use more than one
	 * camera, as BeamSelector scan in DIAD (K11)
	 */
	private List<CameraControl> camerasControls = new ArrayList<>();
	private final AcquisitionPropertyType acquisitionType;

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
		this.acquisitionType = acquisitionType;
		applyAcquisitionPropertiesDocument();
		SpringApplicationContextFacade.addDisposableApplicationListener(this, listenToExposureChange);
	}

	private List<AcquisitionConfigurationProperties> getAcquisitionPropertiesDocuments() {
		return getClientProperties().getAcquisitions();
//		if (acquisitionPropertiesDocuments.isEmpty()) {
//			Optional.ofNullable(AcquisitionsPropertiesHelper.getAcquistionPropertiesDocument(getAcquisitionType()))
//				.ifPresent(acquisitionPropertiesDocuments::addAll);
//		}
//		return acquisitionPropertiesDocuments;
	}

	private ClientSpringProperties getClientProperties() {
		return SpringApplicationContextFacade.getBean(ClientSpringProperties.class);
	}

	Set<String> getOutOfBeamScannables() {
		return Optional.ofNullable(getAcquisitionPropertiesDocument())
				.map(AcquisitionConfigurationProperties::getOutOfBeamScannables)
				.orElseGet(HashSet::new);
	}

	public AcquisitionPropertyType getAcquisitionType() {
		return acquisitionType;
	}

	/**
	 * Used by the parent {@code ScanningAcquisitionController} to set in a new {@code ScanningAcquisition},
	 * {@code AcquisitionEngineDocument} and {@code DetectorDocument}
	 */
	private void applyAcquisitionPropertiesDocument() {
		if (getAcquisition().getAcquisitionEngine() == null) {
			AcquisitionEngineDocument aed = createNewAcquisitionEngineDocument();
			getAcquisition().setAcquisitionEngine(aed);
		}
		if (getCamerasControls().isEmpty()) {
			return;
		}
		if (getAcquisition().getAcquisitionConfiguration().getImageCalibration() == null) {
			applyImageCalibrationDocument();
		}
	}

	private void applyImageCalibrationDocument() {
		getAcquisition().getAcquisitionConfiguration().setImageCalibration(createNewImageCalibrationDocument());
	}

	private List<CameraControl>  getCamerasControls() {
		if (!camerasControls.isEmpty())
			return camerasControls;

		AcquisitionConfigurationProperties dp = getAcquisitionPropertiesDocument();
		if (dp == null)
			return camerasControls;

		camerasControls = dp.getCameras().stream()
				.map(CameraHelper::getCameraConfigurationPropertiesByID)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(CameraHelper::createICameraConfiguration)
				.map(ICameraConfiguration::getCameraControl)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());

		return camerasControls;
	}

	private AcquisitionEngineDocument createNewAcquisitionEngineDocument() {
		AcquisitionConfigurationProperties dp = getAcquisitionPropertiesDocument();
		AcquisitionEngineDocument.Builder engineBuilder = new AcquisitionEngineDocument.Builder();
		if (dp != null) {
			engineBuilder.withId(dp.getEngine().getId());
			engineBuilder.withType(dp.getEngine().getType());
		}
		return engineBuilder.build();
	}

	private ImageCalibration createNewImageCalibrationDocument() {
		ImageCalibration.Builder imageCalibrationBuilder = new ImageCalibration.Builder();

		DetectorDocument detectorDocument = getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters().getDetector();

		DarkCalibrationDocument.Builder builderDark = new DarkCalibrationDocument.Builder()
			.withNumberExposures(0)
			.withDetectorDocument(detectorDocument);
		imageCalibrationBuilder.withDarkCalibration(builderDark.build());

		FlatCalibrationDocument.Builder builderFlat = new FlatCalibrationDocument.Builder()
				.withNumberExposures(0)
				.withDetectorDocument(detectorDocument);
		imageCalibrationBuilder.withFlatCalibration(builderFlat.build());
		return imageCalibrationBuilder.build();
	}

	private AcquisitionConfigurationProperties getAcquisitionPropertiesDocument() {
		if (getAcquisitionPropertiesDocuments().isEmpty()) {
			return null;
		}
		return getAcquisitionPropertiesDocuments().stream()
				.filter(acquisition -> acquisition.getType().equals(getAcquisitionType()))
				.findFirst().orElseThrow();
	}

	// At the moment is not possible to use anonymous lambda expression because it
	// generates a class cast exception
	private ApplicationListener<CameraControlSpringEvent> listenToExposureChange = new ApplicationListener<CameraControlSpringEvent>() {
		@Override
		public void onApplicationEvent(CameraControlSpringEvent event) {
			getCamerasControls().stream()
				.filter(camera -> CameraHelper.cameraIdMatchesCameraControl(event.getCameraId(), Optional.ofNullable(camera)))
				.forEach(cameraName -> updateDetectorDocument(cameraName, event.getAcquireTime()));
		}

		private void updateDetectorDocument(CameraControl cameraControl, double acquireTime) {
			double readoutTime = CameraHelper.getCameraConfigurationPropertiesByCameraControlName(cameraControl.getName())
				.map(CameraConfigurationProperties::getReadoutTime)
				.orElse(0.0);

			String malcolmDetectorName = CameraHelper.getCameraConfigurationPropertiesByCameraControlName(cameraControl.getName())
					.map(CameraConfigurationProperties::getMalcolmDetectorName)
					.orElse("NotAvilable");
			ScanningParameters acquisitionParameters = getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
			// The acquisition configuration may not include this detector
			if (acquisitionParameters.getDetector() != null && !cameraControl.getName().equals(acquisitionParameters.getDetector().getName()))
				return;

			ImageCalibrationHelper imageCalibrationHelper = new ImageCalibrationHelper(() -> getAcquisition().getAcquisitionConfiguration());
			DetectorDocument detectorDocument = new DetectorDocument.Builder()
				.withName(cameraControl.getName())
				.withExposure(acquireTime)
				.withReadout(readoutTime)
				.withMalcolmDetectorName(malcolmDetectorName)
				.build();

			acquisitionParameters.setDetector(detectorDocument);
			imageCalibrationHelper.updateDarkDetectorDocument(detectorDocument);
			imageCalibrationHelper.updateFlatDetectorDocument(detectorDocument);
			SpringApplicationContextFacade.publishEvent(new ScanningAcquisitionEvent(getAcquisition()));
		}
	};

	private ScanningAcquisition getAcquisition() {
		return acquisitionSupplier.get();
	}
}