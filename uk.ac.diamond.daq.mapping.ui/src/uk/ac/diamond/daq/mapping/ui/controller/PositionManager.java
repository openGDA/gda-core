/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionTemplateType;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionTemplateConfiguration;
import uk.ac.gda.client.properties.mode.TestMode;
import uk.ac.gda.client.properties.mode.TestModeElement;
import uk.ac.gda.client.properties.stage.ScannableProperties;
import uk.ac.gda.client.properties.stage.position.Position;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;
import uk.ac.gda.ui.tool.document.ClientPropertiesHelper;

@Component
public class PositionManager {

	@Autowired
	private StageController stageController;

	@Autowired
	private ClientPropertiesHelper clientPropertiesHelper;

	/**
	 * Returns the start position for an acquisition, compiled from several configuration sources:
	 *
	 * <ol>
	 * <li>Current position of scannables configured against {@code Position.START}</li>
	 *
	 * <li>Scannable/position pairs configured against acquisition type e.g. diffraction, tomography.
	 *     These positions may be given as absolute or relative to the scannable's current position</li>
	 *
	 * <li>Scannable/position pairs configured against 'template' type e.g. 2D line, flat-field.</li>
	 *
	 * <li>Positions set in the given {@code acquisition} instance</li>
	 * </ol>
	 *
	 * Later sources override earlier ones if referencing the same devices.
	 * e.g. if {@code eh_shutter} is configured as {@code OPEN} for all tomography scans (acquisition type)
	 * but {@code CLOSE} for dark-fields (acquisition template), the resulting position for {@code eh_shutter}
	 * will be {@code CLOSE}.
	 *
	 * <p>
	 *
	 * Additionally, if {@link TestMode} is active and devices are configured for exclusion,
	 * these will be filtered out.
	 */
	public Set<DevicePositionDocument> getStartPosition(ScanningAcquisition acquisition, AcquisitionKeys keys) {
		final Set<DevicePositionDocument> overallPosition = new HashSet<>(getGlobalStartPosition());

		final var acquisitionStartPosition = getAcquisitionPropertyStartPosition(keys.getPropertyType());
		addReplacingPreviousReferences(overallPosition, acquisitionStartPosition);

		final var templateStartPosition = getAcquisitionTemplateStartPosition(keys);
		addReplacingPreviousReferences(overallPosition, templateStartPosition);

		final var instanceStartPosition = getInstanceStartPosition(acquisition);
		addReplacingPreviousReferences(overallPosition, instanceStartPosition);

		final var devicesToExclude = getDevicesToExclude();
		overallPosition.removeIf(document -> devicesToExclude.contains(document.getDevice()));

		return overallPosition;
	}

	/**
	 * Adds all the contents of {@code subset} to {@code overall}.
	 * Documents in {@code overall} relating to devices referenced by documents in {@code subset} are replaced by the latter.
	 *
	 * @param overall the cumulative set of positions
	 * @param subset a new source of positions with higher importance than the existing documents
	 */
	private void addReplacingPreviousReferences(Set<DevicePositionDocument> overall, Set<DevicePositionDocument> subset) {
		overall.removeIf(document -> referencedInOtherSet(document, subset));
		overall.addAll(subset);
	}

	private boolean referencedInOtherSet(DevicePositionDocument document, Set<DevicePositionDocument> other) {
		return other.stream().anyMatch(doc -> doc.getDevice().equals(document.getDevice()));
	}

	/**
	 * Current position of scannables configured against {@link Position.START}
	 */
	private Set<DevicePositionDocument> getGlobalStartPosition() {
		return stageController.reportPositions(Position.START);
	}

	/**
	 * Scannable/position pairs configured against {@link AcquisitionPropertyType} property
	 */
	private Set<DevicePositionDocument> getAcquisitionPropertyStartPosition(AcquisitionPropertyType type) {
		return convertToDevicePositionDocuments(clientPropertiesHelper.getAcquisitionConfigurationProperties(type)
			.map(AcquisitionConfigurationProperties::getStartPosition)
			.orElse(Collections.emptyList()));
	}

	/**
	 * Scannable/position pairs configured against {@link AcquisitionTemplateType} property
	 */
	private Set<DevicePositionDocument> getAcquisitionTemplateStartPosition(AcquisitionKeys key) {
		return convertToDevicePositionDocuments(clientPropertiesHelper.getAcquisitionTemplateConfiguration(key)
				.map(AcquisitionTemplateConfiguration::getStartPosition)
				.orElse(Collections.emptyList()));
	}

	private Set<DevicePositionDocument> convertToDevicePositionDocuments(List<ScannablePropertiesValue> values) {
		return values.stream()
				.map(stageController::createDevicePositionDocument)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	/**
	 * Start position defined in this {@link ScanningAcquisition} instance
	 */
	private Set<DevicePositionDocument> getInstanceStartPosition(ScanningAcquisition acquisition) {
		return acquisition.getAcquisitionConfiguration().getAcquisitionParameters().getStartPosition();
	}

	/**
	 * When {@link TestMode} is active, returns list of devices configured for exclusion.
	 */
	private List<String> getDevicesToExclude() {
		var testMode = clientPropertiesHelper.getModes().getTest();
		if (testMode.isActive()) {
			return testMode.getElements().stream()
					.filter(TestModeElement::isExclude)
					.map(TestModeElement::getDevice)
					.map(stageController::getScannablePropertiesDocument)
					.filter(Objects::nonNull)
					.map(ScannableProperties::getScannable)
					.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}
}
