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

package gda.device.trajectoryscan;

import java.io.IOException;
import java.util.List;

public interface TrajectoryScan {

	/** Status enum values for 'Profile Build' and 'Append Points' */
	public enum Status {UNDEFINED, SUCCESS, FAILURE};

	/** State enum values for 'Profile Build' and 'Append Points' */
	public enum State {DONE, BUSY};

	/** Status enum values for 'Profile Execution' */
	public enum ExecuteStatus {UNDEFINED, SUCCESS, FAILURE, ABORT, TIMEOUT};

	/** State enum values for 'Profile Execution' */
	public enum ExecuteState {DONE, MOVE_START, EXECUTING, FLYBACK};

	// Methods to setup, clear list of values making up the trajectory (in memory)

	void clearTrajectoryLists();

	void addPointToTrajectory(Double position, Double time, Integer velocityMode);

	void addPointsToTrajectory(Double[] positions, Double[] times, Integer[] velocityMode);

	void setTrajectoryPositionList(List<Double> positionProfileValues);

	void setTrajectoryTimesList(List<Double> timeProfileValues);

	void setTrajectoryVelocityModesList(List<Integer> velocityModeProfileValues);

	List<Double> getTrajectoryTimesList();

	List<Integer> getTrajectoryVelocityModesList();

	List<Double> getTrajectoryPositionsList();

	// Methods used to talk to Epics

	/**
	 * Send currently stored trajectory scan list values to Epics.
	 * (i.e. convert from List to array and send to appropriate PVs)
	 * @throws Exception
	 */
	void sendProfileValues() throws Exception;

	void setBuildProfile() throws Exception;

	void setAppendProfile() throws Exception;

	void setExecuteProfile() throws Exception;

	void setAbortProfile() throws IOException;

	int getScanPercentComplete() throws IOException;

	State getBuildState() throws IOException;

	State getAppendState() throws IOException;

	ExecuteState getExecuteState() throws IOException;

	ExecuteStatus getExecuteStatus() throws IOException;

	Status getAppendStatus() throws IOException;

	Status getBuildStatus() throws IOException;
}