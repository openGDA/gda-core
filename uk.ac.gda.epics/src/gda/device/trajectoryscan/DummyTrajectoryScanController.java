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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Findable;

/**
 * 'Dummy mode' implementation of TrajectoryScan controller.
 * @since 3/7/2017
 */
public class DummyTrajectoryScanController extends TrajectoryScanBase implements TrajectoryScan, Findable {

	private static final Logger logger = LoggerFactory.getLogger(DummyTrajectoryScanController.class);

	private String name;

	private Thread trajectoryExecutionThread;
	private volatile boolean trajectoryScanInProgress;
	private int percentComplete;
	private volatile ExecuteStatus executeStatus;

	public DummyTrajectoryScanController() {
		setName("DummyTrajectoryScanController");
		trajectoryScanInProgress = false;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setBuildProfile() throws Exception {
	}

	@Override
	public void setAppendProfile() throws Exception {
	}

	private void runTrajectoryScan() {
		logger.info("Trajectory scan run thread started");
		trajectoryScanInProgress = true;
		percentComplete = 0;
		int numPoints = trajectoryTimes.size();
		int lastPointProcessed = 0;
		for(int i=0; i<numPoints && (executeStatus!=ExecuteStatus.ABORT && executeStatus!=ExecuteStatus.FAILURE); i++) {
			logger.info("Point {} of {} : Position = {}, time = {}, velocityMode = {}", i+1, numPoints, trajectoryPositions.get(i), trajectoryTimes.get(i), trajectoryVelocityModes.get(i));
			percentComplete = 100*i/(numPoints-1);
			try {
				long pauseTimeMillis = (long) (1000.0 * trajectoryTimes.get(i));
				logger.info("Wait for {} ms...", pauseTimeMillis);
				Thread.sleep(pauseTimeMillis);
				lastPointProcessed = i;
			} catch (InterruptedException e) {
				logger.error("Trajectory scan interrupted", e);
				break; // exit loop over points
			}
		}
		// Set execute status if scan completed successfully.
		if (lastPointProcessed==numPoints-1) {
			executeStatus = ExecuteStatus.SUCCESS;
		}
		trajectoryScanInProgress = false;
		logger.info("Trajectory scan run thread finished. ExecuteStatus = {}", executeStatus);
	}

	@Override
	public void setExecuteProfile() throws Exception {
		// This should 'execute' the profile in a thread, update the scan percent counter and execute state as necessary.
		logger.info("Execute profile called");
		if (trajectoryScanInProgress) {
			// What happens in Epics if execute while scan is running?
			logger.warn("Trajectory scan already running");
			return;
		}
		trajectoryScanInProgress = true;
		percentComplete = 0;
		executeStatus = ExecuteStatus.SUCCESS;
		trajectoryExecutionThread = new Thread(new Runnable() {
			@Override
			public void run() {
				runTrajectoryScan();
			}
		});
		trajectoryExecutionThread.start();
	}

	@Override
	public void sendProfileValues() throws Exception {
	}

	@Override
	public void setAbortProfile() throws IOException {
		// This should stop the execute profile thread if its running and update execute state if necessary.
		executeStatus = ExecuteStatus.ABORT;
	}

	@Override
	public int getScanPercentComplete() throws IOException {
		return percentComplete;
	}

	@Override
	public State getBuildState() throws IOException {
		return State.DONE;
	}

	@Override
	public State getAppendState() throws IOException {
		return State.DONE;
	}

	@Override
	public ExecuteState getExecuteState() throws IOException {
		return trajectoryScanInProgress ? ExecuteState.EXECUTING : ExecuteState.DONE;
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
		return Status.SUCCESS;
	}

	@Override
	public Status getBuildStatus() throws IOException {
		return Status.SUCCESS;
	}
}
