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

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import uk.ac.diamond.daq.mapping.ui.stage.CommonStage;
import uk.ac.diamond.daq.mapping.ui.stage.DevicePosition;
import uk.ac.diamond.daq.mapping.ui.stage.IStageController;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageDevice;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageType;
import uk.ac.diamond.daq.mapping.ui.stage.event.UpdateStagePositionEvent;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.client.properties.stage.DevicePositionDocumentHelper;
import uk.ac.gda.client.properties.stage.ManagedScannable;
import uk.ac.gda.client.properties.stage.ScannableProperties;
import uk.ac.gda.client.properties.stage.ScannablesPropertiesHelper;
import uk.ac.gda.client.properties.stage.position.Position;
import uk.ac.gda.client.properties.stage.position.ScannableKeys;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;
import uk.ac.gda.client.properties.stage.services.DevicePositionDocumentService;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 *
 */
@Controller("stageController")
public class StageController implements IStageController {

	@Autowired
	private DevicePositionDocumentService devicePositionDocumentService;

	@Autowired
	private DevicePositionDocumentHelper devicePositionHelper;

	@Autowired
	private ScannablesPropertiesHelper helper;

	private CommonStage commonStage;

	private final Map<Position, Set<DevicePosition<Double>>> motorsPosition = new EnumMap<>(Position.class);

	/**
	 * These pairs may be used when an acquisition is submitted. This map allows the client to persist a
	 * {@link Position.OUT_OF_BEAM} and inject it later before the request is saved or run.
	 */
	private final Map<Position, Set<DevicePositionDocument>> devicesPosition = new EnumMap<>(Position.class);

	public StageController() {
		super();
	}

	@Override
	public CommonStage getStageDescription() {
		return Optional.ofNullable(commonStage).orElse(StageType.GTS.getCommonStage());
	}

	@Override
	public Set<DevicePosition<Double>> savePosition(Position position) {
		devicesPosition.put(position, reportPositions(position));
		motorsPosition.put(position, getStageDescription().getMotorsPosition());
		SpringApplicationContextFacade.publishEvent(new UpdateStagePositionEvent(this, position));
		return motorsPosition.get(position);
	}

	@Override
	public Set<DevicePositionDocument> removePosition(Position position) {
		Set<DevicePositionDocument> deleted = devicesPosition.remove(position);
		Optional.ofNullable(deleted)
			.ifPresent(d -> SpringApplicationContextFacade.publishEvent(new UpdateStagePositionEvent(this, position)));
		return deleted;
	}

	@Override
	public void moveToPosition(Position position) {
		Set<DevicePositionDocument> positionDocuments = devicesPosition.get(position);
		positionDocuments.forEach(devicePositionHelper::moveTo);
	}

	@Override
	public Map<String, String> getMetadata() {
		return getStageDescription().getMetadata();
	}

	@Override
	public double getMotorPosition(StageDevice device) {
		Set<DevicePosition<Double>> start = savePosition(Position.START);
		return start.stream()
				.filter(dp -> dp.getStageDevice().name().equals(device.name()))
				.findFirst()
				.orElse(new DevicePosition<>(device, 0.0)).getPosition();
	}

	@Override
	public void changeStage(CommonStage commonStage) {
		this.commonStage = commonStage;
	}

	public Set<DevicePositionDocument> getPositionDocuments(Position position, Set<String> scannables) {
		if (scannables == null)
			return new HashSet<>();
		return getPositionDocuments(position).stream()
				.filter(d -> scannables.contains(d.getDevice()))
				.collect(Collectors.toSet());
	}

	/**
	 * Returns a copy of the saves {@link Position}
	 * @param position
	 * @return a new set, eventually empty
	 */
	public Set<DevicePositionDocument> getPositionDocuments(Position position) {
		return new HashSet<>(devicesPosition.getOrDefault(position, new HashSet<>()));
	}

	/**
	 * Returns {@link DevicePositionDocument} from a {@link ManagedScannable}
	 * @param managedScannable the scannable manager
	 * @return the related document with the actual position
	 */
	public DevicePositionDocument createDevicePositionDocument(ManagedScannable<?> managedScannable) {
		return devicePositionDocumentService.devicePositionAsDocument(managedScannable.getScannablePropertiesDocument().getScannable());
	}

	public ScannableProperties getScannablePropertiesDocument(ScannableKeys scannableKeys) {
		return helper.getScannablePropertiesDocument(scannableKeys);
	}

	/**
	 * Creates a {@link DevicePositionDocument} using a {@code ScannablePropertiesValue#getGroupId()}
	 * and  {@code ScannablePropertiesValue#getScannableId()}
	 * @param scannablePropertiesValue property to use
	 * @return a new, optional, {@link DevicePositionDocument}
	 */
	public DevicePositionDocument createDevicePositionDocument(ScannablePropertiesValue scannablePropertiesValue) {
		return devicePositionDocumentService.devicePositionAsDocument(scannablePropertiesValue);
	}

	/**
	 * Returns a set of {@link DevicePositionDocument} relative to the actual value of the
	 * scannables cofigured as defining {@link Position}.
	 *
	 * @param position The requested position
	 *
	 * @return a set of documents, otherwise an empty set if not defined in {@link ClientSpringProperties#getPositions()}
	 */
	public Set<DevicePositionDocument> reportPositions(Position position) {
		return helper.getPositionScannableProperties(position).stream()
			.map(ScannableProperties::getScannable)
			.map(devicePositionDocumentService::devicePositionAsDocument)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}
}
