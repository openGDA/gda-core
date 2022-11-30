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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.epics.CachedLazyPVFactory;
import gda.epics.LazyPVFactory;
import gda.epics.PV;

/**
 * Epics controller for Trajectory scan.
 * Contains getters, setters for accessing PVs for time, position, velocity mode, user mode arrays, etc. needed
 * for setting up, building and executing a trajectory scan and monitoring its progress. <p>
 * @since 3/7/2017
 */
public final class EpicsTrajectoryScanController extends TrajectoryScanControllerBase implements InitializingBean{

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(EpicsTrajectoryScanController.class);

	private CachedLazyPVFactory pvFactory;

	private String pvBase;

	private static final String X_POSITION_ARRAY = "X:Positions";
	private static final String POSITION_ARRAY = ":Positions";

	private static final String PROFILE_NUM_POINTS = "ProfileNumPoints";
	private static final String PROFILE_NUM_POINTS_RBV = "ProfileNumPoints_RBV";

	private static final String PROFILE_NUM_POINTS_TO_BUILD = "ProfilePointsToBuild";
	private static final String PROFILE_NUM_POINTS_TO_BUILD_RBV = "ProfilePointsToBuild_RBV";

	private static final String USER_ARRAY = "UserArray";
	private static final String VELOCITY_MODE_ARRAY = "VelocityMode";
	private static final String PROFILE_TIME_ARRAY = "ProfileTimeArray";

	private static final String PROFILE_BUILD = "ProfileBuild";
	private static final String PROFILE_EXECUTE = "ProfileExecute";
	private static final String PROFILE_APPEND = "ProfileAppend";

	private static final String PROC = ".PROC";
	private static final String STATUS_RBV = "Status_RBV";
	private static final String STATE_RBV = "State_RBV";

	private static final String PROFILE_ABORT = "ProfileAbort";
	private static final String SCAN_PERCENT = "TscanPercent_RBV";

	private static final String PROFILE_CS_NAME = "ProfileCsName";
	private static final String USE_AXIS = ":UseAxis";
	private static final String OFFSET_AXIS = ":Offset";
	private static final String RESOLUTION = ":Resolution";
	private static final String PROFILE_TIME_MODE="ProfileTimeMode";
	private static final String CS_PORT=":CsPort";
	private static final String CS_AXIS=":CsAxis";

	private static final String DRIVE_BUFFER_A_INDEX = "EpicsBufferAPtr_RBV";

	private static final String DRIVER_VERSION = "DriverVersion_RBV";
	private static final String PROGRAM_VERSION = "ProgramVersion_RBV";

	private PV<Integer[]> userArrayPv;
	private PV<Integer[]> velocityModeArrayPv;
	private PV<Double[]> profileTimeArrayPv;
	private PV<Double[]> xPositionArrayPv;

	public EpicsTrajectoryScanController() {
		setupMaps();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( getName() == null || getName().isEmpty())
			throw new Exception("name is not set");
		if (pvBase == null || pvBase.isEmpty())
			throw new Exception("pvBase is not set");
		if (pvFactory == null) {
			pvFactory = new CachedLazyPVFactory(pvBase);
		}
		userArrayPv = LazyPVFactory.newIntegerArrayPV(pvBase + USER_ARRAY);
		velocityModeArrayPv = LazyPVFactory.newIntegerArrayPV(pvBase + VELOCITY_MODE_ARRAY);
		profileTimeArrayPv = LazyPVFactory.newDoubleArrayPV(pvBase + PROFILE_TIME_ARRAY);
		xPositionArrayPv = LazyPVFactory.newDoubleArrayPV(pvBase + X_POSITION_ARRAY);
	}

	public CachedLazyPVFactory getPVFactory() {
		return pvFactory;
	}

	public void setPvBase(String prefix) {
		this.pvBase = prefix;
	}

	public String getPvBase() {
		return this.pvBase;
	}

	private String getPVNameForAxis(int axisIndex, String name) throws Exception {
		if (axisIndex<0 || axisIndex>axisNames.size()) {
			throw new Exception("Axis with index "+axisIndex+" not found");
		}
		String axisPvName = axisNames.get(axisIndex)+name;
		return axisPvName;
	}


	private String getPVNameForMotor(int motorIndex, String name) throws Exception {
		if (motorIndex<0 || motorIndex>motorNames.size()) {
			throw new Exception("Motor with index "+motorIndex+" not found");
		}
		String motorPvName = motorNames.get(motorIndex)+name;
		return motorPvName;
	}

	// Num profile points
	public int getProfileNumPointsRBV() throws Exception {
		return pvFactory.getIntegerPVValueCache(PROFILE_NUM_POINTS_RBV).get();
	}

	public int getProfileNumPoints() throws Exception {
		return pvFactory.getIntegerPVValueCache(PROFILE_NUM_POINTS).get();
	}

	@Override
	public void setProfileNumPoints(int numPoints) throws Exception {
		pvFactory.getIntegerPVValueCache(PROFILE_NUM_POINTS).putWait(numPoints);
	}

	// Num profile points to build
	public int getProfileNumPointsToBuildRBV() throws Exception {
		return pvFactory.getIntegerPVValueCache(PROFILE_NUM_POINTS_TO_BUILD_RBV).get();
	}

	@Override
	public int getProfileNumPointsToBuild() throws Exception {
		return pvFactory.getIntegerPVValueCache(PROFILE_NUM_POINTS_TO_BUILD).get();
	}

	@Override
	public void setProfileNumPointsToBuild(int numPoints) throws Exception {
		pvFactory.getIntegerPVValueCache(PROFILE_NUM_POINTS_TO_BUILD).putWait(numPoints);
	}

	// User mode
	@Override
	public void setProfileUserArray(Integer[] vals) throws IOException {
		userArrayPv.putWait(vals);
	}

	@Override
	public Integer[] getProfileUserArray() throws IOException {
		return userArrayPv.get();
	}

	// Velocity mode
	@Override
	public void setProfileVelocityModeArray(Integer[] vals) throws IOException {
		velocityModeArrayPv.putWait(vals);
	}

	@Override
	public Integer[] getProfileVelocityModeArray() throws IOException {
		return velocityModeArrayPv.get();
	}

	//Time
	@Override
	public void setProfileTimeArray(Double[] vals) throws IOException {
		profileTimeArrayPv.putWait(vals);
	}

	@Override
	public Double[] getProfileTimeArray() throws IOException {
		return profileTimeArrayPv.get();
	}

	// Position
	public void setProfileXPositionArray(Double[] vals) throws IOException {
		xPositionArrayPv.putWait(vals);
	}
	public Double[] getProfileXPositionArray() throws IOException {
		return xPositionArrayPv.get();
	}

	// Don't use value cache for these functions, since always want to send value when building, appending, executing profile.
	// Build profile
	@Override
	public void setBuildProfile() throws Exception {
		pvFactory.getPVInteger(PROFILE_BUILD+PROC).putWait(1);
	}

	// Append profile
	@Override
	public void setAppendProfile() throws Exception {
		pvFactory.getPVInteger(PROFILE_APPEND+PROC).putWait(1);
	}

	// Append profile
	@Override
	public void setExecuteProfile() throws Exception {
		pvFactory.getPVInteger(PROFILE_EXECUTE+PROC).putWait(1);
	}

	@Override
	public void setAbortProfile() throws IOException {
		pvFactory.getPVInteger(PROFILE_ABORT).putWait(1);
	}

	@Override
	public int getScanPercentComplete() throws IOException {
		return pvFactory.getPVInteger(SCAN_PERCENT).get();
	}

	// ... 'State' RBV for build, append, execute ....

	@Override
	public State getBuildState() throws IOException {
		return stateMap.get(pvFactory.getPVInteger(PROFILE_BUILD+STATE_RBV).get());
	}

	@Override
	public State getAppendState() throws IOException {
		return stateMap.get(pvFactory.getPVInteger(PROFILE_APPEND+STATE_RBV).get());
	}

	@Override
	public ExecuteState getExecuteState() throws IOException {
		return executeStateMap.get(pvFactory.getPVInteger(PROFILE_EXECUTE+STATE_RBV).get());
	}

	// ... 'Status' RBV for build, append, execute ...
	@Override
	public Status getBuildStatus() throws IOException {
		return statusMap.get(pvFactory.getPVInteger(PROFILE_BUILD+STATUS_RBV).get());
	}

	@Override
	public Status getAppendStatus() throws IOException {
		return statusMap.get(pvFactory.getPVInteger(PROFILE_APPEND+STATUS_RBV).get());
	}

	@Override
	public ExecuteStatus getExecuteStatus() throws IOException {
		return executeStatusMap.get(pvFactory.getPVInteger(PROFILE_EXECUTE+STATUS_RBV).get());
	}


	@Override
	public void setCoordinateSystem(String pmacName) throws IOException {
		pvFactory.getPVString(PROFILE_CS_NAME).putWait(pmacName);
	}

	@Override
	public String getCoordinateSystem() throws IOException {
		return pvFactory.getPVString(PROFILE_CS_NAME).get();
	}

	// Time mode
	public void setTimeMode(String timeMode) throws IOException {
		pvFactory.getPVString(PROFILE_TIME_MODE).putWait(timeMode);
	}

	public String getTimeMode() throws IOException {
		return pvFactory.getPVString(PROFILE_TIME_MODE).get();
	}


	//'Use' axis
	@Override
	public void setUseAxis(int axis, boolean useAxis) throws IOException, Exception {
		int val = useAxis ? 1 : 0;
		String axisPvName = getPVNameForAxis(axis, USE_AXIS);
		pvFactory.getIntegerPVValueCache(axisPvName).putWait(val);
	}

	@Override
	public boolean getUseAxis(int axis) throws IOException, Exception {
		String axisPvName = getPVNameForAxis(axis, USE_AXIS);
		return pvFactory.getIntegerPVValueCache(axisPvName).get() > 0;
	}


	@Override
	public void setAxisPoints(int axis, Double [] points)throws IOException, Exception{

		String positionPvName = getPVNameForAxis(axis, POSITION_ARRAY);
		pvFactory.getPVDoubleArray(positionPvName).putWait(points);
	}

	@Override
	public Double[] getAxisPoints(int axis) throws IOException, Exception{
		String positionPvName = getPVNameForAxis(axis, POSITION_ARRAY);
		return pvFactory.getPVDoubleArray(positionPvName).get();
	}

	//Axis offset
	@Override
	public void setOffsetForAxis(int axis, double offset) throws IOException, Exception {

		String axisPvName = getPVNameForAxis(axis, OFFSET_AXIS);
		pvFactory.getDoublePVValueCache(axisPvName).putWait(offset);
	}

	@Override
	public double getOffsetForAxis(int axis) throws IOException, Exception {
		String axisPvName = getPVNameForAxis(axis, OFFSET_AXIS);
		return pvFactory.getDoublePVValueCache(axisPvName).get();
	}

	//Axis resolution
	@Override
	public void setResolutionForAxis(int axis, double resolution) throws IOException, Exception {
		String axisPvName = getPVNameForAxis(axis, RESOLUTION);
		pvFactory.getDoublePVValueCache(axisPvName).putWait(resolution);
	}

	@Override
	public double getResolutionForAxis(int axis) throws IOException, Exception {
		String axisPvName = getPVNameForAxis(axis, RESOLUTION);
		return pvFactory.getDoublePVValueCache(axisPvName).get();
	}

	@Override
	public void setCSPort(int motor,String port) throws IOException, Exception {
		String axisPvName = getPVNameForMotor(motor,CS_PORT);
		pvFactory.getPVString(axisPvName).putNoWait(port);
	}

	public String getCSPort(int motor) throws IOException, Exception {
		String axisPvName = getPVNameForMotor(motor,CS_PORT);
		return pvFactory.getPVString(axisPvName).get();
	}

	@Override
	public void setCSAssignment(int motor, String port) throws IOException, Exception {
		String axisPvName = getPVNameForMotor(motor,CS_AXIS);
		pvFactory.getPVString(axisPvName).putNoWait(port);
	}

	public String getCSAssignment(int motor) throws IOException, Exception {
		String axisPvName = getPVNameForMotor(motor,CS_AXIS);
		return pvFactory.getPVString(axisPvName).get();
	}

	public double getDriverVersion() throws IOException {
		return pvFactory.getPVDouble(DRIVER_VERSION).get();
	}

	public double getProgramVersion() throws IOException {
		return pvFactory.getPVDouble(PROGRAM_VERSION).get();
	}

	@Override
	public int getDriveBufferAIndex() throws IOException, Exception {
		return pvFactory.getPVInteger(DRIVE_BUFFER_A_INDEX).get();
	}

	private Map<Integer,State> stateMap;
	private Map<Integer,Status> statusMap;
	private Map<Integer,ExecuteState> executeStateMap;
	private Map<Integer,ExecuteStatus> executeStatusMap;

	/**
	 * Create map to go from Epics integer status/state value to Java enum
	 * This must be called in constructor!
	 */
	private void setupMaps() {
		statusMap = new HashMap<Integer, Status>();
		statusMap.put(0, Status.UNDEFINED);
		statusMap.put(1, Status.SUCCESS);
		statusMap.put(2, Status.FAILURE);

		stateMap = new HashMap<Integer, State>();
		stateMap.put(0, State.DONE);
		stateMap.put(1, State.BUSY);

		executeStatusMap = new HashMap<Integer, ExecuteStatus>();
		executeStatusMap.put(0, ExecuteStatus.UNDEFINED);
		executeStatusMap.put(1, ExecuteStatus.SUCCESS);
		executeStatusMap.put(2, ExecuteStatus.FAILURE);
		executeStatusMap.put(2, ExecuteStatus.ABORT);
		executeStatusMap.put(2, ExecuteStatus.TIMEOUT);

		executeStateMap = new HashMap<Integer, ExecuteState>();
		executeStateMap.put(0, ExecuteState.DONE);
		executeStateMap.put(1, ExecuteState.MOVE_START);
		executeStateMap.put(2, ExecuteState.EXECUTING);
		executeStateMap.put(3, ExecuteState.FLYBACK);
	}

}
