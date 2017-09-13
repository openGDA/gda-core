/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.trajectoryscancontroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TrajecoryScan base class implementation, with common functions used to set up trajectory scan point lists in memory.
 * @since 3/7/2017
 */
public abstract class TrajectoryScanControllerBase implements TrajectoryScanController {

	private static final Logger logger = LoggerFactory.getLogger(TrajectoryScanControllerBase.class);

	protected List<Double> trajectoryPositions;
	protected List<Double> trajectoryTimes;
	protected List<Integer> trajectoryVelocityModes;

	protected List<String> axisNames = Arrays.asList(new String[]{"A", "B", "C", "U", "V", "W", "X", "Y", "Z"});
	protected List<String> motorNames = Arrays.asList(new String[]{"M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8"});


	@Override
	public void setAxisNames(String[] axisNames) {
		this.axisNames = Arrays.asList(axisNames);
	}

	@Override
	public List<String> getAxisNames() {
		return axisNames;
	}

	@Override
	public void setMotorNames(String[] motorNames) {
		this.motorNames = Arrays.asList(motorNames);
	}

	@Override
	public List<String> getMotorNames() {
		return motorNames;
	}

	public void createTrajectoryLists() {
		trajectoryPositions = new ArrayList<Double>();
		trajectoryTimes = new ArrayList<Double>();
		trajectoryVelocityModes = new ArrayList<Integer>();
	}

	public void checkCreateTrajectoryLists() {
		if (trajectoryPositions==null || trajectoryTimes==null || trajectoryVelocityModes==null) {
			createTrajectoryLists();
		}
	}

	@Override
	public void clearTrajectoryLists() {
		checkCreateTrajectoryLists();
		trajectoryPositions.clear();
		trajectoryTimes.clear();
		trajectoryVelocityModes.clear();
	}

	public boolean profileBuiltOk() throws IOException {
		if ( getBuildStatus()  != Status.SUCCESS ||
			 getAppendStatus() != Status.SUCCESS ||
			 getBuildState()   != State.DONE ||
			 getAppendState()  != State.DONE ) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Add single position, time, velocity mode to trajectory point list.
	 * @param position
	 * @param time
	 * @param velocityMode
	 */
	@Override
	public void addPointToTrajectory(Double position, Double time, Integer velocityMode) {
		checkCreateTrajectoryLists();

		trajectoryPositions.add(position);
		trajectoryTimes.add(time);
		trajectoryVelocityModes.add(velocityMode);
	}

	/**
	 * Add position, time, velocity mode arrays to trajectory point list.
	 * @param positions
	 * @param times
	 * @param velocityMode
	 */
	@Override
	public void addPointsToTrajectory(Double[] positions, Double[] times, Integer[] velocityMode) {
		checkCreateTrajectoryLists();

		if (positions.length != times.length || positions.length != velocityMode.length) {
			logger.warn("Trajectory point arrays to add have different lengths!");
			return;
		}

		for(int i=0; i<positions.length; i++) {
			trajectoryPositions.add(positions[i]);
			trajectoryTimes.add(times[i]);
			trajectoryVelocityModes.add(velocityMode[i]);
		}
	}

	@Override
	public void setTrajectoryPositionList(List<Double> positionProfileValues) {
		this.trajectoryPositions = positionProfileValues;
	}

	@Override
	public void setTrajectoryTimesList(List<Double> timeProfileValues) {
		this.trajectoryTimes = timeProfileValues;
	}

	@Override
	public void setTrajectoryVelocityModesList(List<Integer> velocityModeProfileValues) {
		this.trajectoryVelocityModes = velocityModeProfileValues;
	}

	@Override
	public List<Double> getTrajectoryTimesList() {
		return trajectoryTimes;
	}

	@Override
	public List<Integer> getTrajectoryVelocityModesList() {
		return trajectoryVelocityModes;
	}

	@Override
	public List<Double> getTrajectoryPositionsList() {
		return trajectoryPositions;
	}
}
