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
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.concurrent.Async;

/**
 * 'Dummy mode' implementation of TrajectoryScan controller.
 * @since 3/7/2017
 */
public class DummyTrajectoryScanController extends TrajectoryScanControllerBase {

	private static final Logger logger = LoggerFactory.getLogger(DummyTrajectoryScanController.class);
	private static final int MAX_AXES = 128; // some "large enough" number

	private volatile Status buildStatus = Status.UNDEFINED;
	private volatile Status appendStatus = Status.UNDEFINED;
	private volatile ExecuteStatus executeStatus = ExecuteStatus.UNDEFINED;

	private volatile State buildState = State.DONE;
	private volatile State appendState = State.DONE;
	private volatile ExecuteState executeState = ExecuteState.DONE;

	private int percentComplete = 0;
	private int driveBufferAIndex = 0;
	private int numberOfPoints = 0;
	private int numberOfPointsToBuild = 0;

	private List<Double> axisResolutions = new ArrayList<>(Collections.nCopies(MAX_AXES, 0.));
	private List<Double> axisOffsets = new ArrayList<>(Collections.nCopies(MAX_AXES, 0.));
	private List<String> motorPorts = new ArrayList<>(Collections.nCopies(MAX_AXES, ""));
	private List<String> csAssignments = new ArrayList<>(Collections.nCopies(MAX_AXES, ""));
	private List<Boolean> useAxes = new ArrayList<>(Collections.nCopies(MAX_AXES, false));

	private Double[] intermediateAxisPositions;
	private Double[] intermediateTimeProfile;
	private Integer[] intermediateVelocityProfile;
	private Integer[] intermediateUserMode;

	private List<Double> axisPositions = new ArrayList<>();
	private List<Double> timeProfile = new ArrayList<>();
	private List<Integer> velocityProfile = new ArrayList<>();

	public DummyTrajectoryScanController() {
		// Empty
	}

	@Override
	public void setBuildProfile() throws Exception {
		buildState = State.BUSY;
		axisPositions.clear();
		timeProfile.clear();
		velocityProfile.clear();
		if (numberOfPointsToBuild > numberOfPoints) {
			logger.error("Specified %d points to build, but total size specified was %d", numberOfPointsToBuild, numberOfPoints);
			buildStatus = Status.FAILURE;
			buildState = State.DONE;
			return;
		}
		if (intermediateAxisPositions.length < numberOfPointsToBuild
				|| intermediateTimeProfile.length < numberOfPointsToBuild
				|| intermediateVelocityProfile.length < numberOfPointsToBuild) {
			logger.error("Provided arrays not large enough to build profile");
			buildStatus = Status.FAILURE;
			buildState = State.DONE;
			return;
		}
		axisPositions.addAll(Arrays.asList(intermediateAxisPositions).subList(0, numberOfPointsToBuild));
		timeProfile.addAll(Arrays.asList(intermediateTimeProfile).subList(0, numberOfPointsToBuild));
		velocityProfile.addAll(Arrays.asList(intermediateVelocityProfile).subList(0, numberOfPointsToBuild));
		buildStatus = Status.SUCCESS;
		buildState = State.DONE;
	}

	@Override
	public void setAppendProfile() throws Exception {
		appendState = State.BUSY;
		if (numberOfPointsToBuild + axisPositions.size() > numberOfPoints) {
			logger.error("Not enough space to append positions");
			appendStatus = Status.FAILURE;
			appendState = State.DONE;
			return;
		}
		if (intermediateAxisPositions.length < numberOfPointsToBuild
				|| intermediateTimeProfile.length < numberOfPointsToBuild
				|| intermediateVelocityProfile.length < numberOfPointsToBuild) {
			logger.error("Provided arrays not large enough to append profile");
			appendStatus = Status.FAILURE;
			appendState = State.DONE;
			return;
		}
		axisPositions.addAll(Arrays.asList(intermediateAxisPositions).subList(0, numberOfPointsToBuild));
		timeProfile.addAll(Arrays.asList(intermediateTimeProfile).subList(0, numberOfPointsToBuild));
		velocityProfile.addAll(Arrays.asList(intermediateVelocityProfile).subList(0, numberOfPointsToBuild));
		appendStatus = Status.SUCCESS;
		appendState = State.DONE;
	}


	@Override
	public void setExecuteProfile() throws Exception {
		if (executeState != ExecuteState.DONE) {
			logger.error("Already executing profile");
			setAbortProfile();
			executeStatus = ExecuteStatus.FAILURE;
			return;
		}
		if (buildState != State.DONE || appendState != State.DONE) {
			logger.error("Profile still building");
			executeStatus = ExecuteStatus.FAILURE;
			executeState = ExecuteState.DONE;
			return;
		}
		if (buildStatus != Status.SUCCESS) {
			// we don't check append status since that may never have been called
			logger.error("Cannot execute profile that failed to build");
			executeStatus = ExecuteStatus.FAILURE;
			executeState = ExecuteState.DONE;
			return;
		}
		executeStatus = ExecuteStatus.UNDEFINED;
		executeState = ExecuteState.EXECUTING;
		Async.execute(() -> {
			// don't bother emulating the provided times, just take some time and incrementally update percentage complete
			for (int i = 0; i < 500; i++) {
				if (executeState == ExecuteState.DONE) return;
				percentComplete = (int) ((i / 500.) * 100);
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					executeStatus = ExecuteStatus.FAILURE;
					executeState = ExecuteState.DONE;
				}
			}
			executeStatus = ExecuteStatus.SUCCESS;
			executeState = ExecuteState.DONE;
		});
	}

	@Override
	public void setAbortProfile() throws IOException {
		executeStatus = ExecuteStatus.ABORT;
		executeState = ExecuteState.DONE;
	}

	@Override
	public int getScanPercentComplete() throws IOException {
		return percentComplete;
	}

	@Override
	public State getBuildState() throws IOException {
		return buildState;
	}

	@Override
	public State getAppendState() throws IOException {
		return appendState;
	}

	@Override
	public ExecuteState getExecuteState() throws IOException {
		return executeState;
	}

	@Override
	public ExecuteStatus getExecuteStatus() throws IOException {
		return executeStatus;
	}

	public void setExecuteStatus(ExecuteStatus executeStatus) {
		this.executeStatus = executeStatus;
	}

	@Override
	public Status getAppendStatus() throws IOException {
		return appendStatus;
	}

	@Override
	public Status getBuildStatus() throws IOException {
		return buildStatus;
	}

	@Override
	public void setCoordinateSystem(String pmacName) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCoordinateSystem() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUseAxis(int axis, boolean useAxis) throws IOException, Exception {
		useAxes.set(axis, useAxis);
	}

	@Override
	public boolean getUseAxis(int axis) throws IOException, Exception {
		return useAxes.get(axis);
	}

	@Override
	public void setOffsetForAxis(int axis, double offset) throws IOException, Exception {
		axisOffsets.set(axis, offset);
	}

	@Override
	public double getOffsetForAxis(int axis) throws IOException, Exception {
		return axisOffsets.get(axis);
	}

	@Override
	public void setResolutionForAxis(int axis, double resolution) throws IOException, Exception {
		axisResolutions.set(axis, resolution);
	}

	@Override
	public double getResolutionForAxis(int axis) throws IOException, Exception {
		return axisResolutions.get(axis);
	}

	@Override
	public int getDriveBufferAIndex() throws IOException, Exception {
		return driveBufferAIndex;
	}

	@Override
	public void setProfileNumPoints(int numPoints) throws Exception {
		numberOfPoints = numPoints;
	}

	@Override
	public void setProfileNumPointsToBuild(int numPoints) throws Exception {
		numberOfPointsToBuild = numPoints;
	}

	@Override
	public int getProfileNumPointsToBuild() throws Exception {
		return numberOfPointsToBuild;
	}

	@Override
	public void setProfileVelocityModeArray(Integer[] vals) throws IOException {
		intermediateVelocityProfile = vals;
	}
	@Override
	public Integer[] getProfileVelocityModeArray() throws IOException {
		return intermediateVelocityProfile;
	}

	@Override
	public void setProfileTimeArray(Double[] vals) throws IOException {
		intermediateTimeProfile = vals;
	}

	@Override
	public Double[] getProfileTimeArray() throws IOException {
		return intermediateTimeProfile;
	}

	@Override
	public void setAxisPoints(int axis, Double[] points) throws IOException, Exception {
		intermediateAxisPositions = points;
	}

	@Override
	public Double[] getAxisPoints(int axis) {
		return intermediateAxisPositions;
	}

	@Override
	public void setProfileUserArray(Integer[] vals) throws IOException {
		intermediateUserMode = vals;
	}

	@Override
	public Integer[] getProfileUserArray() throws IOException {
		return intermediateUserMode;
	}

	@Override
	public void setCSPort(int motor, String port) throws IOException, Exception {
		motorPorts.set(motor, port);
	}

	@Override
	public void setCSAssignment(int motor, String port) throws IOException, Exception {
		csAssignments.set(motor, port);
	}
}
