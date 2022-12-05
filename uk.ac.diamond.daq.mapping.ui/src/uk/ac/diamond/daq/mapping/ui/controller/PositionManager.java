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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.mapping.ui.Activator;
import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionSubType;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.client.AcquisitionManager;
import uk.ac.gda.client.properties.acquisition.AcquisitionTemplate;
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

	private AcquisitionManager acquisitionManager;

	private Map<AcquisitionPropertyType, List<ScannablePropertiesValue>> positionsPerScanType = new EnumMap<>(AcquisitionPropertyType.class);
	private Map<AcquisitionSubType, List<ScannablePropertiesValue>> positionsPerSubType = new EnumMap<>(AcquisitionSubType.class);

	/**
	 * Returns the start position for an acquisition, compiled from four configuration sources:
	 *
	 * <ol>
	 * <li>Current position of scannables configured against {@code Position.START}</li>
	 *
	 * <li>Positions specified in {@link AcquisitionTemplate}.
	 *     These positions may be given as absolute or relative to the scannable's current position</li>
	 *
	 * <li>Positions configured against {@link AcquisitionPropertyType} in {@link PositionManager}
	 *
	 * <li>Positions configured against {@link AcquisitionSubType} in {@link PositionManager}
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
	public Set<DevicePositionDocument> getStartPosition(AcquisitionKeys keys) {
		final Set<DevicePositionDocument> overallPosition = new HashSet<>(getGlobalStartPosition());

		final var startPositionFromTemplate = getStartPositionFromAcquisitionTemplate(keys);
		addReplacingPreviousReferences(overallPosition, startPositionFromTemplate);

		final var acquisitionStartPosition = getAcquisitionPropertyStartPosition(keys.getPropertyType());
		addReplacingPreviousReferences(overallPosition, acquisitionStartPosition);

		final var subtypeStartPosition = getSubtypeStartPosition(keys.getSubType());
		addReplacingPreviousReferences(overallPosition, subtypeStartPosition);

		final var devicesToExclude = getDevicesToExclude();
		overallPosition.removeIf(document -> devicesToExclude.contains(document.getDevice()));

		return overallPosition;
	}

	public void configurePosition(AcquisitionPropertyType scanType, List<ScannablePropertiesValue> position) {
		positionsPerScanType.put(scanType, position);
	}

	public List<ScannablePropertiesValue> getConfiguredPosition(AcquisitionPropertyType scanType) {
		return positionsPerScanType.getOrDefault(scanType, Collections.emptyList());
	}

	public void configurePosition(AcquisitionSubType subType, List<ScannablePropertiesValue> position) {
		positionsPerSubType.put(subType, position);
	}

	public List<ScannablePropertiesValue> getConfiguredPosition(AcquisitionSubType subType) {
		return positionsPerSubType.getOrDefault(subType, Collections.emptyList());
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

	private Set<DevicePositionDocument> getStartPositionFromAcquisitionTemplate(AcquisitionKeys keys) {
		var position = getAcquisitionManager().getStartPosition(keys);
		return convertToDevicePositionDocuments(position);
	}

	/**
	 * Scannable/position pairs configured against {@link AcquisitionPropertyType} property
	 */
	private Set<DevicePositionDocument> getAcquisitionPropertyStartPosition(AcquisitionPropertyType type) {
		List<ScannablePropertiesValue> positions = positionsPerScanType.getOrDefault(type, Collections.emptyList());
		return convertToDevicePositionDocuments(positions);
	}

	private Set<DevicePositionDocument> convertToDevicePositionDocuments(List<ScannablePropertiesValue> values) {
		return values.stream()
				.map(stageController::createDevicePositionDocument)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private Set<DevicePositionDocument> getSubtypeStartPosition(AcquisitionSubType subtype) {
		return convertToDevicePositionDocuments(positionsPerSubType.getOrDefault(subtype, Collections.emptyList()));
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

	private AcquisitionManager getAcquisitionManager() {
		if (acquisitionManager == null) {
			acquisitionManager = Activator.getService(AcquisitionManager.class);
		}
		return acquisitionManager;
	}
}
