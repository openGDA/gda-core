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

package uk.ac.gda.tomography.stage;

import java.util.Map;
import java.util.Set;

import uk.ac.gda.tomography.stage.enumeration.Position;
import uk.ac.gda.tomography.stage.enumeration.StageDevice;

/**
 * Defines a minimal set of methods for a stage controller. May change in future.
 *
 * @author Maurizio Nagni
 */
public interface IStageController {

	Set<DevicePosition<Double>> savePosition(Position position);

	Map<String, String> getMetadata();

	CommonStage getStageDescription();

	Map<Position, Set<DevicePosition<Double>>> getMotorsPositions();

	double getMotorPosition(StageDevice device);

	void changeStage(CommonStage stage);

}
