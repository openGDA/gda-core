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
import java.util.List;

public interface TrajectoryScanController {

	/** Status enum values for 'Profile Build' and 'Append Points' */
	public enum Status {UNDEFINED, SUCCESS, FAILURE}

	/** State enum values for 'Profile Build' and 'Append Points' */
	public enum State {DONE, BUSY}

	/** Status enum values for 'Profile Execution' */
	public enum ExecuteStatus {UNDEFINED, SUCCESS, FAILURE, ABORT, TIMEOUT}

	/** State enum values for 'Profile Execution' */
	public enum ExecuteState {DONE, MOVE_START, EXECUTING, FLYBACK}

	int getTimeConversionFromSecondsToDeviceUnits();

	int getMaxPointsPerProfileBuild();

	// Methods used to talk to Epics

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


	void setCoordinateSystem(String pmacName) throws IOException;
	String getCoordinateSystem() throws IOException;


	void setAxisNames(String[] axisNames);
	void setMotorNames(String[] motorNames);
	List<String> getAxisNames();
	List<String> getMotorNames();
	void setUseAxis(int axis, boolean useAxis) throws IOException, Exception;
	boolean getUseAxis(int axis) throws IOException, Exception;
	void setOffsetForAxis(int axis, double offset) throws IOException, Exception;
	double getOffsetForAxis(int axis) throws IOException, Exception;
	void setResolutionForAxis(int axis, double resolution) throws IOException, Exception;
	double getResolutionForAxis(int axis) throws IOException, Exception;

	int getDriveBufferAIndex() throws IOException, Exception;

	void setProfileNumPoints(int numPoints) throws Exception;

	void setProfileNumPointsToBuild(int numPoints) throws Exception;
	int getProfileNumPointsToBuild() throws Exception;

	void setProfileVelocityModeArray(Integer[] vals) throws IOException;
	Integer[] getProfileVelocityModeArray() throws IOException;

	void setProfileTimeArray(Double[] vals) throws IOException;
	Double[] getProfileTimeArray() throws IOException;

	void setAxisPoints(int axis, Double [] points)throws IOException, Exception;
	Double[] getAxisPoints(int axis) throws IOException, Exception;

	void setCSPort(int motor,String port) throws IOException, Exception;
	void setCSAssignment(int motor, String port) throws IOException, Exception;

	void setProfileUserArray(Integer[] vals) throws IOException;
	Integer[] getProfileUserArray() throws IOException;


}