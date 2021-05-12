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
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.MultipleScans;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.acquisition.AcquisitionTemplateConfiguration;
import uk.ac.gda.client.properties.acquisition.ScannableTrackDocumentProperty;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * Creates default acquisition documents.
 *
 * @author Maurizio Nagni
 */
@Component
public class DocumentFactory {

	@Autowired
	private ClientSpringProperties properties;

	public Supplier<ScanningAcquisition> newScanningAcquisition(AcquisitionPropertyType propertyType, AcquisitionTemplateType templateType) {
		return () -> {
			var newConfiguration = new ScanningAcquisition();
			var configuration = new ScanningConfiguration();
			newConfiguration.setAcquisitionConfiguration(configuration);

			newConfiguration.setName("Untitled Acquisition");
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
			newConfiguration.getAcquisitionConfiguration().setAcquisitionParameters(acquisitionParameters);

			// --- NOTE---
			// The creation of the acquisition engine and the used detectors documents are delegated to the ScanningAcquisitionController
			// --- NOTE---

			return newConfiguration;
		};
	}

	public Optional<ScanpathDocument.Builder> buildScanpathBuilder(AcquisitionPropertyType propertyType, AcquisitionTemplateType templateType) {
		List<AcquisitionTemplateConfiguration> templates = properties.getAcquisitions().stream()
				.filter(a -> a.getType().equals(propertyType))
				.findFirst()
				.map(AcquisitionConfigurationProperties::getTemplates)
				.orElseGet(Collections::emptyList);

		return templates.stream()
			.filter(a -> templateType.equals(a.getTemplate()))
			.findFirst()
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
			.withStep(trackDocumentProperty.getStep())
			.build();
	}
}