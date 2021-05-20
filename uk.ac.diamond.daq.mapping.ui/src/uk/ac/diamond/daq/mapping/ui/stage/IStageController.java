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

package uk.ac.diamond.daq.mapping.ui.stage;

import java.util.Map;
import java.util.Set;

import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Position;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageDevice;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;

/**
 * Defines a minimal set of methods for a stage controller. May change in future.
 *
 * @author Maurizio Nagni
 */
public interface IStageController {

	/**
	 * Stores a sets of position documents and associates it to a {@link Position}
	 * @param position the {@code Position} associated with the documents collection to set
	 * @return the removed document set, eventually an empty {@code Set}
	 */
	Set<DevicePosition<Double>> savePosition(Position position);

	/**
	 * Removes a sets of position documents previously associated with a {@link Position}
	 * @param position the {@code Position} associated with the documents set to remove
	 * @return the removed document set, eventually an empty {@code Set}
	 */
	Set<DevicePositionDocument> removePosition(Position position);

	boolean hasPosition(Position position);

	/**
	 * Moves the devices to the position specified by the documents associated with {@link Position}
	 * @param position the {@code Position} associated with the documents set to use for the movement
	 */
	void moveToPosition(Position position);

	Map<String, String> getMetadata();

	CommonStage getStageDescription();

	Map<Position, Set<DevicePosition<Double>>> getMotorsPositions();

	double getMotorPosition(StageDevice device);

	void changeStage(CommonStage stage);

}
