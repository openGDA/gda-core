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

import gda.factory.FindableBase;

/**
 * TrajecoryScan base class implementation, with common functions used to set up trajectory scan point lists in memory.
 * @since 3/7/2017
 */
public abstract class TrajectoryScanControllerBase extends FindableBase implements TrajectoryScanController {

	private static final Logger logger = LoggerFactory.getLogger(TrajectoryScanControllerBase.class);

	protected List<Double> trajectoryPositions;
	protected List<Double> trajectoryTimes;
	protected List<Integer> trajectoryVelocityModes;

	protected List<String> axisNames = Arrays.asList("A", "B", "C", "U", "V", "W", "X", "Y", "Z");
	protected List<String> motorNames = Arrays.asList("M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8");

	/** Conversion factor from seconds to trajectory scan time units */
	private int timeConversionFromSecondsToPmacUnits = 1000000;

	/** Number of points to build/append when sending profile values to Epics (used by {@link #sendAppendProfileValues()}. */
	private int maxPointsPerProfileBuild = 1500;

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

	/**
	 * Return trajectory point time values converted to PMac time units
	 * @return list of converted time values
	 */
	public List<Double> getTrajectoryConvertTimes() {
		List<Double> convertedTime = new ArrayList<Double>();
		for(int i=0; i<trajectoryTimes.size(); i++) {
			convertedTime.add(trajectoryTimes.get(i)*timeConversionFromSecondsToPmacUnits);
		}
		return convertedTime;
	}

	/**
	 *  Check to make sure converted time isn't too large - otherwise bad things happen and have to reboot IOC...
	 * @param convertedTime times (in PMac time units)
	 * @throws Exception if a time value is too large
	 */
	private void checkTimes(List<Double> convertedTime) throws Exception {
		double maxAllowedTimeForPMac = Math.pow(2, 24);
		for(int i=0; i<convertedTime.size(); i++) {
			if (convertedTime.get(i)>maxAllowedTimeForPMac) {
				throw new Exception("Time "+convertedTime.get(i)+" for profile point "+i+" exceeds limit ("+maxAllowedTimeForPMac+")");
			}
		}
	}

	/**
	 * Send values from currently stored trajectory scan list values to Epics.
	 * (i.e. convert from List to array and send to appropriate PV)
	 * @param startIndex index of first point in profile to send
	 * @param endIndex index of last point in profile to send
	 * @throws Exception
	 */
	@Override
	public void sendProfileValues(int startIndex, int endIndex) throws Exception {
		// Limit min, max indices to be within range of currently set arrays
		int xAxisIndex = axisNames.indexOf("X");
		int start = Math.max(0, startIndex);
		start = Math.min(start, trajectoryTimes.size()-1);
		int end = Math.min(endIndex, trajectoryTimes.size()-1);
		int numPoints = end - start + 1;

		Integer[] userMode = new Integer[numPoints];
		Arrays.fill(userMode, 0);

		List<Double> convertedTime = getTrajectoryConvertTimes();
		checkTimes(convertedTime);

		// These are used for class types by toArray function.
		Double []dblArray = new Double[0];
		Integer []intArray = new Integer[0];

		setProfileNumPointsToBuild(numPoints);
		setProfileTimeArray(convertedTime.subList(start, end+1).toArray(dblArray));
		setAxisPoints(xAxisIndex, trajectoryPositions.subList(start, end+1).toArray(dblArray));
		setProfileVelocityModeArray(trajectoryVelocityModes.subList(start, end+1).toArray(intArray) );
		setProfileUserArray(userMode);
	}

	/**
	 * Send currently stored trajectory scan list values to Epics.
	 * (i.e. convert from List to array and send to appropriate PV)
	 * @throws Exception
	 */
	@Override
	public void sendProfileValues() throws Exception {
		sendProfileValues(0, trajectoryTimes.size()-1);
	}

	/**
	 * Send trajectory profile to Epics, building and appending as many times as
	 * necessary to send all the points. See also {@link #sendAppendProfileValues(int)}.
	 * @throws Exception
	 */
	@Override
	public void sendAppendProfileValues() throws Exception {
		int startPoint = 0;
		int numPoints = trajectoryTimes.size();

		while(startPoint < numPoints) {
			startPoint = sendAppendProfileValues(startPoint);
		}
	}

	/**
	 * Build/append profile in Epics; take range of values from trajectory scan list.
	 * Build if startPoint==0; otherwise Append. Use 'maxPointsPerProfileBuild' to set the
	 * number of profile points sent per build/append operation.
	 * @param startPoint index of first point in trajectory profile to send.
	 * @return index of next point to be sent to Epics
	 * @throws Exception
	 */
	public int sendAppendProfileValues(int startPoint) throws Exception {
		int maxPointIndex = trajectoryTimes.size()-1;
		int endPointIndex = Math.min(maxPointIndex, startPoint + maxPointsPerProfileBuild - 1);
		logger.debug("Appending points {} ... {} to trajectory profile", startPoint, endPointIndex);
		sendProfileValues(startPoint, endPointIndex);
		if (startPoint == 0) {
			setBuildProfile();
			if (getBuildStatus() == Status.FAILURE){
				throw new Exception("Failure when building trajectory scan profile - check Epics EDM screen");
			}
		} else {
			setAppendProfile();
			if (getAppendStatus() == Status.FAILURE){
				throw new Exception("Failure when appending to trajectory scan profile - check Epics EDM screen");
			}
		}
		return endPointIndex+1;
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

	public int getMaxPointsPerProfileBuild() {
		return maxPointsPerProfileBuild;
	}

	public void setMaxPointsPerProfileBuild(int maxPointsPerProfileBuild) {
		this.maxPointsPerProfileBuild = maxPointsPerProfileBuild;
	}

	public int getTimeConversionFromSecondsToPmacUnits() {
		return timeConversionFromSecondsToPmacUnits;
	}

	public void setTimeConversionFromSecondsToPmacUnits(int timeConversionFromSecondsToPmacUnits) {
		this.timeConversionFromSecondsToPmacUnits = timeConversionFromSecondsToPmacUnits;
	}
}
