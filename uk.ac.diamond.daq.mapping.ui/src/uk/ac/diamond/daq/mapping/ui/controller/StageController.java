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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Controller;

import uk.ac.diamond.daq.mapping.ui.stage.CommonStage;
import uk.ac.diamond.daq.mapping.ui.stage.DevicePosition;
import uk.ac.diamond.daq.mapping.ui.stage.IStageController;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Position;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageDevice;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageType;

@Controller("stageController")
public class StageController implements IStageController {

	private final Map<Position, Set<DevicePosition<Double>>> motorsPosition = new EnumMap<>(Position.class);
	private CommonStage commonStage;

	public StageController() {
		super();
	}

	@Override
	public CommonStage getStageDescription() {
		return Optional.ofNullable(commonStage).orElse(StageType.GTS.getCommonStage());
	}

	@Override
	public Set<DevicePosition<Double>> savePosition(Position position) {
		motorsPosition.put(position, getStageDescription().getMotorsPosition());
		return motorsPosition.get(position);
	}

	@Override
	public Map<String, String> getMetadata() {
		return getStageDescription().getMetadata();
	}

	@Override
	public Map<Position, Set<DevicePosition<Double>>> getMotorsPositions() {
		return Collections.unmodifiableMap(motorsPosition);
	}

	@Override
	public double getMotorPosition(StageDevice device) {
		Set<DevicePosition<Double>> start = savePosition(Position.START);
		return start.stream().filter(dp -> dp.getStageDevice().name().equals(device.name())).findFirst()
				.orElse(new DevicePosition<>(device, 0.0)).getPosition();
	}

	@Override
	public void changeStage(CommonStage commonStage) {
		this.commonStage = commonStage;
	}
}
