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

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument;
import uk.ac.gda.api.acquisition.AcquisitionType;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.MultipleScans;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;
import uk.ac.gda.api.acquisition.configuration.calibration.DarkCalibrationDocument;
import uk.ac.gda.api.acquisition.configuration.calibration.FlatCalibrationDocument;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.acquisition.AcquisitionTemplateConfiguration;
import uk.ac.gda.client.properties.acquisition.ScannableTrackDocumentProperty;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;

/**
 * Creates default acquisition documents.
 *
 * @author Maurizio Nagni
 */
@Component
public class DocumentFactory {

	@Autowired
	private ClientPropertiesHelper clientPropertiesHelper;

	public Supplier<ScanningAcquisition> newScanningAcquisition(AcquisitionPropertyType propertyType, AcquisitionTemplateType templateType) {
		return () -> {
			var newAcquisition = new ScanningAcquisition();
			// Does not set UUID as it will be inserted by the save service
			newAcquisition.setType(getType(propertyType));
			var configuration = new ScanningConfiguration();
			newAcquisition.setAcquisitionConfiguration(configuration);

			newAcquisition.setName("Untitled Acquisition");
			var acquisitionParameters = new ScanningParameters();
			configuration.setImageCalibration(new ImageCalibration.Builder().build());

			buildScanpathBuilder(propertyType, templateType)
				.map(ScanpathDocument.Builder::build)
				.ifPresent(acquisitionParameters::setScanpathDocument);

			var multipleScanBuilder = new MultipleScans.Builder();
			multipleScanBuilder.withMultipleScansType(MultipleScansType.REPEAT_SCAN);
			multipleScanBuilder.withNumberRepetitions(1);
			multipleScanBuilder.withWaitingTime(0);
			configuration.setMultipleScans(multipleScanBuilder.build());
			newAcquisition.getAcquisitionConfiguration().setAcquisitionParameters(acquisitionParameters);

			DetectorDocumentHelper.getHelper(newAcquisition, clientPropertiesHelper).apply();
			return newAcquisition;
		};
	}

	public Optional<ScanpathDocument.Builder> buildScanpathBuilder(AcquisitionPropertyType propertyType, AcquisitionTemplateType templateType) {
		return clientPropertiesHelper.getAcquisitionTemplateConfiguration(propertyType, templateType)
			.map(this::buildScanpathBuilder);
	}

	private ScanpathDocument.Builder buildScanpathBuilder(AcquisitionTemplateConfiguration acquisitionTemplate) {
		var builder = new ScanpathDocument.Builder();
		builder.withModelDocument(acquisitionTemplate.getTemplate());
		builder.withScannableTrackDocuments(getScannableTrackDocument(acquisitionTemplate.getTracks()));
		return builder;
	}

	private List<ScannableTrackDocument> getScannableTrackDocument(List<ScannableTrackDocumentProperty> tracks) {
		return tracks.stream()
			.map(this::createScannableTrackDocument)
			.collect(Collectors.toList());
	}

	private ScannableTrackDocument createScannableTrackDocument(ScannableTrackDocumentProperty trackDocumentProperty) {
		var builder = new ScannableTrackDocument.Builder();
		return builder.withAxis(trackDocumentProperty.getAxis())
			.withScannable(trackDocumentProperty.getScannable())
			.withPoints(trackDocumentProperty.getPoints())
			.withStart(trackDocumentProperty.getStart())
			.withStop(trackDocumentProperty.getStop())
			.withStep(trackDocumentProperty.getStep())
			.build();
	}

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

	static class DetectorDocumentHelper {
		private final ScanningAcquisition acquisition;
		private final ClientPropertiesHelper clientPropertiesHelper;

		public static DetectorDocumentHelper getHelper(ScanningAcquisition acquisition, ClientPropertiesHelper clientPropertiesHelper) {
			return new DetectorDocumentHelper(acquisition, clientPropertiesHelper);
		}

		private DetectorDocumentHelper(ScanningAcquisition acquisition, ClientPropertiesHelper clientPropertiesHelper) {
			this.acquisition = acquisition;
			this.clientPropertiesHelper = clientPropertiesHelper;
		}

		public void apply() {
			if (acquisition.getAcquisitionEngine() == null) {
				var aed = createNewAcquisitionEngineDocument();
				acquisition.setAcquisitionEngine(aed);
			}

			createDetectorDocument()
				.ifPresent(acquisition.getAcquisitionConfiguration().getAcquisitionParameters()::setDetector);

			if(acquisition.getAcquisitionConfiguration().getImageCalibration() == null) {
				applyImageCalibrationDocument(acquisition);
			}
		}

		private AcquisitionEngineDocument createNewAcquisitionEngineDocument() {
			AcquisitionConfigurationProperties dp = getAcquisitionPropertiesDocument();
			var engineBuilder = new AcquisitionEngineDocument.Builder();
			if (dp != null) {
				engineBuilder.withId(dp.getEngine().getId());
				engineBuilder.withType(dp.getEngine().getType());
			}
			return engineBuilder.build();
		}

		private void applyImageCalibrationDocument(ScanningAcquisition acquisition) {
			acquisition.getAcquisitionConfiguration().setImageCalibration(createNewImageCalibrationDocument(acquisition));
		}

		private Optional<DetectorDocument> createDetectorDocument() {
			Optional<String> cameraId = getAcquisitionPropertiesDocument().getCameras().stream()
				.findFirst();

			if (cameraId.isPresent()) {
				Optional<CameraConfigurationProperties> cameraProperties = clientPropertiesHelper.getAcquisitionPropertiesDocuments(cameraId.get());
				if (cameraProperties.isPresent()) {
					return Optional.ofNullable(new DetectorDocument.Builder()
							.withName(cameraProperties.get().getCameraControl())
							.withMalcolmDetectorName(cameraProperties.get().getMalcolmDetectorName())
							.build());
				}
			}
			return Optional.empty();
		}

		private ImageCalibration createNewImageCalibrationDocument(ScanningAcquisition acquisition) {
			var imageCalibrationBuilder = new ImageCalibration.Builder();

			var detectorDocument = acquisition.getAcquisitionConfiguration().getAcquisitionParameters().getDetector();

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

		private AcquisitionConfigurationProperties getAcquisitionPropertiesDocument() {
			if (clientPropertiesHelper.getAcquisitionPropertiesDocuments().isEmpty()) {
				return null;
			}
			return clientPropertiesHelper.getAcquisitionPropertiesDocuments().stream()
					.filter(acq -> acq.getType().equals(getAcquisitionType()))
					.findFirst().orElseThrow();
		}

		public AcquisitionPropertyType getAcquisitionType() {
			return clientPropertiesHelper.getAcquisitionConfigurationProperties(acquisition.getType())
					.map(AcquisitionConfigurationProperties::getType)
					.orElseGet(() -> AcquisitionPropertyType.DEFAULT);
		}
	}
}