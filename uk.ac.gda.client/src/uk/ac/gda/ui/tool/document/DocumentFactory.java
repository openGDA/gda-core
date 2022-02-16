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
import java.util.Objects;
import java.util.Optional;
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
import uk.ac.gda.client.exception.AcquisitionConfigurationException;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionKeys;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.acquisition.AcquisitionTemplate;
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

	public ScanningAcquisition newScanningAcquisition(AcquisitionKeys acquisitionKeys) throws AcquisitionConfigurationException {
		var newAcquisition = new ScanningAcquisition();
		newAcquisition.setType(getType(acquisitionKeys.getPropertyType()));
		var configuration = new ScanningConfiguration();
		newAcquisition.setAcquisitionConfiguration(configuration);

		newAcquisition.setName("Untitled Acquisition");
		var acquisitionParameters = new ScanningParameters();
		configuration.setImageCalibration(new ImageCalibration.Builder().build());

		buildScanpathBuilder(acquisitionKeys)
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
	}

	public Optional<ScanpathDocument.Builder> buildScanpathBuilder(AcquisitionKeys acquisitionKeys) {
		return clientPropertiesHelper.getAcquisitionTemplateConfiguration(acquisitionKeys)
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

	public Optional<DetectorDocument> createDetectorDocument(String cameraId) {
		return createDetectorDocument(cameraId, clientPropertiesHelper);
	}

	private static Optional<DetectorDocument> createDetectorDocument(String cameraId, ClientPropertiesHelper clientPropertiesHelper) {
		if (cameraId == null) {
			return Optional.empty();
		}

		Optional<CameraConfigurationProperties> cameraProperties = clientPropertiesHelper.getAcquisitionPropertiesDocuments(cameraId);
		if (cameraProperties.isPresent()) {
				return Optional.ofNullable(new DetectorDocument.Builder()
						.withName(cameraProperties.get().getCameraControl())
						.withMalcolmDetectorName(cameraProperties.get().getMalcolmDetectorName())
						.build());
		}
		return Optional.empty();
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

		public void apply() throws AcquisitionConfigurationException{
			if (acquisition.getAcquisitionEngine() == null) {
				var aed = createNewAcquisitionEngineDocument();
				acquisition.setAcquisitionEngine(aed);
			}
			acquisition.getAcquisitionConfiguration().getAcquisitionParameters().setDetectors(createDetectorDocuments());
			acquisition.getAcquisitionConfiguration().setImageCalibration(createNewImageCalibrationDocument(acquisition));
		}

		private AcquisitionEngineDocument createNewAcquisitionEngineDocument() throws AcquisitionConfigurationException {
			var engineBuilder = new AcquisitionEngineDocument.Builder();
			populateEngineDocumentBuilder(engineBuilder, getAcquisitionPropertiesDocument());
			return engineBuilder.build();
		}

		private void populateEngineDocumentBuilder(AcquisitionEngineDocument.Builder engineBuilder, AcquisitionConfigurationProperties dp) {
			AcquisitionEngineDocument engineDocument = Objects.requireNonNull(dp.getEngine(), "No engine document is configured");
			engineBuilder.withId(engineDocument.getId());
			engineBuilder.withType(engineDocument.getType());
		}

		private List<DetectorDocument> createDetectorDocuments() throws AcquisitionConfigurationException {

			return getAcquisitionPropertiesDocument().getCameras().stream()
				.map(cameraId -> DocumentFactory.createDetectorDocument(cameraId, clientPropertiesHelper))
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

		private AcquisitionConfigurationProperties getAcquisitionPropertiesDocument() throws AcquisitionConfigurationException {
			return clientPropertiesHelper.getAcquisitionPropertiesDocuments().stream()
					.filter(acq -> acq.getType().equals(getAcquisitionType()))
					.findFirst()
					.orElseThrow(() -> new AcquisitionConfigurationException("No acquisition configuration is present in properties"));
		}

		public AcquisitionPropertyType getAcquisitionType() {
			return clientPropertiesHelper.getAcquisitionConfigurationProperties(acquisition.getType())
					.map(AcquisitionConfigurationProperties::getType)
					.orElseGet(() -> AcquisitionPropertyType.DEFAULT);
		}
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

		try {
			DetectorDocumentHelper.getHelper(acquisition, clientPropertiesHelper).apply();
		} catch (AcquisitionConfigurationException e) {
			// TODO figure out why this would throw
		}
		return acquisition;
	}

	private ScanpathDocument getDefaultScanpathDocument(AcquisitionTemplate template) {
		List<ScannableTrackDocument> paths = template.getScanAxes().entrySet().stream()
			.map(entry -> defaultScannableTrackDocument(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());

		if (paths.isEmpty()) {
			paths = List.of(defaultScannableTrackDocument());
		}

		AcquisitionTemplateType pathType = getDefaultAcquisitionTemplateType(template);

		return new ScanpathDocument.Builder().withModelDocument(pathType).withScannableTrackDocuments(paths).build();
	}

	private AcquisitionTemplateType getDefaultAcquisitionTemplateType(AcquisitionTemplate template) {
		var axes = template.getScanAxes();
		if (axes.isEmpty()) return AcquisitionTemplateType.STATIC_POINT;
		if (axes.size() == 1) return AcquisitionTemplateType.ONE_DIMENSION_LINE;
		return AcquisitionTemplateType.TWO_DIMENSION_POINT;
	}

	private ScannableTrackDocument defaultScannableTrackDocument() {
		return new ScannableTrackDocument.Builder().withPoints(1).build();
	}
	private ScannableTrackDocument defaultScannableTrackDocument(String axis, String scannable) {
		return new ScannableTrackDocument.Builder()
				.withAxis(axis)
				.withScannable(scannable)
				.withStart(0)
				.withStop(1)
				.withStep(5)
				.build();
	}
}