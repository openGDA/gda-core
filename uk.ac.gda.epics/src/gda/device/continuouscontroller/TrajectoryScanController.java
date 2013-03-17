/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.continuouscontroller;

import gda.device.Device;
import gda.device.DeviceException;
import gda.factory.Configurable;
import gda.factory.Findable;
import gda.util.OutOfRangeException;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

public interface TrajectoryScanController extends Device, Configurable, Findable {
	public enum TrajectoryScanProperty {
		/** */
		BUILD,
		/** */
		EXECUTE,
		/** */
		READ,

	}
	/**
	 * Trajectory build status
	 */
	public enum BuildStatus {
		/**
		 * Undefined status
		 */
		UNDEFINED,
		/**
		 * build successfully completed
		 */
		SUCCESS,
		/**
		 * build failed
		 */
		FAILURE
	}
	/**
	 * trajectory execution status
	 */
	public enum ExecuteStatus {
		/**
		 * Undefined status
		 */
		UNDEFINED,
		/**
		 * execution completed successfully
		 */
		SUCCESS,
		/**
		 * execution failed
		 */
		FAILURE,
		/**
		 * execution aborted or stopped before completion
		 */
		ABORT,
		/**
		 * execution timeout
		 */
		TIMEOUT
	}
	/**
	 * Trajectory data collection read status
	 */
	public enum ReadStatus {
		/**
		 * undefined status
		 */
		UNDEFINED,
		/**
		 * read data collection completed successfully
		 */
		SUCCESS,
		/**
		 * read data collection failed
		 */
		FAILURE
	}


	/**
	 * Specify the trajectory path for a motor.
	 * 
	 * @param motor index (starts at 1)
	 * @throws InterruptedException 
	 */
	void setMTraj(int motor, double[] path) throws DeviceException, InterruptedException;

	/**
	 * Get the specified trajectory path for a motor.
	 * 
	 * @param motor index (starts at 1)
	 * @throws InterruptedException 
	 */
	double[] getMTraj(int motor) throws DeviceException, InterruptedException;

	/**
	 * Enable or disable movement for a motor.
	 * 
	 * @param motor index (starts at 1)
	 */
	void setMMove(int motor, boolean b) throws DeviceException, InterruptedException;

	/**
	 * Query if motor movement is enabled.
	 * 
	 * @param motor index (starts at 1)
	 */
	boolean isMMove(int motor) throws CAException, TimeoutException, InterruptedException;

	/**
	 * Set the number of elements from the configured trajectory to use.
	 * 
	 * @param value
	 * @throws DeviceException
	 * @throws OutOfRangeException
	 */
	void setNumberOfElements(int value) throws DeviceException, OutOfRangeException, InterruptedException;

	/**
	 * Get the number of elements from the configured trajectory to use.
	 * @throws InterruptedException 
	 */
	int getNumberOfElements() throws DeviceException, InterruptedException;

	/**
	 * Set the number of output pulses .
	 * @throws InterruptedException 
	 */
	void setNumberOfPulses(int value) throws DeviceException, OutOfRangeException, InterruptedException;

	/**
	 * Get the number of output pulses
	 * @throws InterruptedException 
	 */
	int getNumberOfPulses() throws DeviceException, InterruptedException;

	/**
	 * Set the element at which to start generating pulses
	 * @throws InterruptedException 
	 */
	void setStartPulseElement(int n) throws DeviceException, InterruptedException;

	/**
	 * Get the element at which to stop generating pulses
	 * @throws InterruptedException 
	 */
	int getStartPulseElement() throws DeviceException, InterruptedException;

	/**
	 * Set the element at which to stop generating pulses
	 * @throws InterruptedException 
	 */
	void setStopPulseElement(int n) throws DeviceException, InterruptedException;

	/**
	 * Get the element at which to stop generating pulses
	 * @throws InterruptedException 
	 */
	int getStopPulseElement() throws DeviceException, InterruptedException;

	/**
	 * Set the time to execute the trajectory.
	 * @throws InterruptedException 
	 */
	void setTrajectoryTime(double seconds) throws DeviceException, InterruptedException;

	/**
	 * Get the time to execute the trajectory.
	 * @throws InterruptedException 
	 */
	double getTrajectoryTime() throws DeviceException, InterruptedException;

	/**
	 * Check if a trajectory is built okay.
	 * 
	 * @returns null if read okay else a reason
	 */
	String checkBuildOkay();

	/**
	 * Execute the trajectory, waiting until complete.
	 * @throws InterruptedException 
	 */
	void execute() throws DeviceException, InterruptedException;

	/**
	 * Check if a trajectory executed okay.
	 * 
	 * @returns null if executed okay else a reason
	 */
	String checkExecuteOkay();

	/**
	 * Reads up the actual positions, waiting until complete.
	 * @throws DeviceException 
	 * @throws InterruptedException 
	 */
	void read() throws DeviceException, InterruptedException;

	/**
	 * Check if a read executed okay.
	 * 
	 * @returns null if read okay else a reason
	 */
	String checkReadOkay();

	/**
	 * Get the actual number of pulses generated
	 * @throws InterruptedException 
	 */
	int getActualPulses() throws DeviceException, InterruptedException;

	/**
	 * Gets the actual trajectory path for a motor.
	 * 
	 * @param motor index (starts at 1)
	 * @throws InterruptedException 
	 */
	double[] getMActual(int motor) throws DeviceException, InterruptedException;

	/**
	 * Get a motors (PV record) name.
	 * 
	 * @param motor index (starts at 1)
	 * @throws InterruptedException 
	 */
	String getMName(int motor) throws DeviceException, InterruptedException;

	/**
	 * Abort the trajectory scan.
	 * 
	 * @throws DeviceException
	 * @throws InterruptedException 
	 */
	void stop() throws DeviceException, InterruptedException;

	int getMaximumNumberElements();

	int getMaximumNumberPulses();

	boolean isBusy();


}
