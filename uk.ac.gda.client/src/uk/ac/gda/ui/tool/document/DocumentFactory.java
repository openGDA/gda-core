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

package uk.ac.gda.ui.tool.document;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gda.device.DeviceException;
import gda.factory.Finder;
import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument.AcquisitionEngineType;
import uk.ac.gda.api.acquisition.AcquisitionType;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.MultipleScans;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;
import uk.ac.gda.api.acquisition.configuration.calibration.DarkCalibrationDocument;
import uk.ac.gda.api.acquisition.configuration.calibration.FlatCalibrationDocument;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.acquisition.AcquisitionTemplate;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;

/**
 * Creates default acquisition documents.
 *
 * @author Maurizio Nagni
 */
@Component
public class DocumentFactory {

	private static final Logger logger = LoggerFactory.getLogger(DocumentFactory.class);

	@Autowired
	private ClientPropertiesHelper clientPropertiesHelper;

	private AcquisitionType getType(AcquisitionPropertyType propertyType) {
		switch (propertyType) {
			case DIFFRACTION:
				return AcquisitionType.DIFFRACTION;
			case TOMOGRAPHY:
				return AcquisitionType.TOMOGRAPHY;
			default:
				return AcquisitionType.GENERIC;
			}
	}

	public static AcquisitionPropertyType getType(AcquisitionType acquisitionType) {
		switch (acquisitionType) {
			case DIFFRACTION:
				return AcquisitionPropertyType.DIFFRACTION;
			case TOMOGRAPHY:
				return AcquisitionPropertyType.TOMOGRAPHY;
			default:
				return AcquisitionPropertyType.DEFAULT;
			}
	}

	private Optional<DetectorDocument> createDetectorDocument(String cameraId, ClientPropertiesHelper clientPropertiesHelper) {
		if (cameraId == null) {
			return Optional.empty();
		}

		Optional<CameraConfigurationProperties> cameraProperties = clientPropertiesHelper.getAcquisitionPropertiesDocuments(cameraId);
		if (cameraProperties.isPresent()) {
			var exposure = 0.0;
			var control = (CameraControl) Finder.find(cameraProperties.get().getCameraControl());
			if (control != null) {
				try {
					exposure = control.getAcquireTime();
				} catch (DeviceException e) {
					logger.error("Error reading {} exposure time", cameraId, e);
				}
			}

			return Optional.ofNullable(new DetectorDocument.Builder()
					.withId(cameraProperties.get().getId())
					.withMalcolmDetectorName(cameraProperties.get().getMalcolmDetectorName())
					.withExposure(exposure)
					.build());
		}
		return Optional.empty();
	}

	public ScanningAcquisition newScanningAcquisition(AcquisitionTemplate template) {
		var acquisition = new ScanningAcquisition();
		acquisition.setType(getType(template.getType()));
		acquisition.setAcquisitionEngine(template.getEngine());

		var configuration = new ScanningConfiguration();
		acquisition.setAcquisitionConfiguration(configuration);

		acquisition.setName("Untitled Acquisition");
		var acquisitionParameters = new ScanningParameters();
		configuration.setImageCalibration(new ImageCalibration.Builder().build());

		acquisitionParameters.setScanpathDocument(getDefaultScanpathDocument(template));

		var multipleScanBuilder = new MultipleScans.Builder();
		multipleScanBuilder.withMultipleScansType(MultipleScansType.REPEAT_SCAN);
		multipleScanBuilder.withNumberRepetitions(1);
		multipleScanBuilder.withWaitingTime(0);
		configuration.setMultipleScans(multipleScanBuilder.build());
		acquisition.getAcquisitionConfiguration().setAcquisitionParameters(acquisitionParameters);

		// detector(s)
		acquisition.getAcquisitionConfiguration().getAcquisitionParameters().setDetectors(createDetectorDocuments(template));
		acquisition.getAcquisitionConfiguration().setImageCalibration(createNewImageCalibrationDocument(acquisition));
		return acquisition;
	}

	private List<DetectorDocument> createDetectorDocuments(AcquisitionTemplate template) {
		return template.getDetectors().stream()
				.map(cameraId -> createDetectorDocument(cameraId, clientPropertiesHelper))
				.filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toList());
	}

	private ImageCalibration createNewImageCalibrationDocument(ScanningAcquisition acquisition) {
		var imageCalibrationBuilder = new ImageCalibration.Builder();

		var detectorDocument = acquisition.getAcquisitionConfiguration().getAcquisitionParameters().getDetectors().iterator().next();

		var builderDark = new DarkCalibrationDocument.Builder()
			.withNumberExposures(0)
			.withDetectorDocument(detectorDocument);
		imageCalibrationBuilder.withDarkCalibration(builderDark.build());

		var builderFlat = new FlatCalibrationDocument.Builder()
				.withNumberExposures(0)
				.withDetectorDocument(detectorDocument);
		imageCalibrationBuilder.withFlatCalibration(builderFlat.build());
		return imageCalibrationBuilder.build();
	}

	private ScanpathDocument getDefaultScanpathDocument(AcquisitionTemplate template) {
		AcquisitionTemplateType pathType = getDefaultAcquisitionTemplateType(template);
		Map<Mutator, List<Number>> mutators = new EnumMap<>(Mutator.class);
		if (template.getEngine().getType() == AcquisitionEngineType.MALCOLM) {
			mutators.putAll(Map.of(Mutator.CONTINUOUS, Collections.emptyList()));
		}
		return new ScanpathDocument.Builder()
				.withModelDocument(pathType)
				.withScannableTrackDocuments(template.getDefaultPaths())
				.withMutators(mutators)
				.build();
	}

	private AcquisitionTemplateType getDefaultAcquisitionTemplateType(AcquisitionTemplate template) {
		var axes = template.getDefaultPaths();
		if (axes.size() == 2) return AcquisitionTemplateType.TWO_DIMENSION_POINT;
		if (axes.size() == 1) {
			if (axes.get(0).getScannable() != null) {
				return AcquisitionTemplateType.ONE_DIMENSION_LINE;
			} else {
				return AcquisitionTemplateType.STATIC_POINT;
			}
		}

		throw new UnsupportedOperationException("Only 1 or 2D acquisitions supported");
	}

}