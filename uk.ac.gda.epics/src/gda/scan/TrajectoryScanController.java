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

package gda.scan;

import gda.device.Device;
import gda.device.DeviceException;
import gda.factory.Configurable;
import gda.factory.Findable;
import gda.util.OutOfRangeException;
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

	public static final  int MAX_TRAJECTORY = 8;

	public void setAccelerationTime(double accelerationTime);
	public double getAccelerationTime();
	public void read() throws DeviceException, InterruptedException;
	public boolean isReading() throws InterruptedException, DeviceException, TimeoutException;
	public ReadStatus getReadStatus();
	public int getActualPulses() throws TimeoutException, DeviceException;
	public double[] getMActual(int motorIndex) throws DeviceException, TimeoutException, InterruptedException;
	public void execute() throws DeviceException, InterruptedException;
	public ExecuteStatus getExecuteStatus() throws DeviceException, InterruptedException;
	public boolean isBusy() throws TimeoutException, InterruptedException, DeviceException;
	public void stop() throws DeviceException, InterruptedException;
	public void setMMove(int motorIndex, boolean b)throws DeviceException, InterruptedException;
	public void setMTraj(int motorIndex, double[] path)throws DeviceException, InterruptedException;
	public void setNumberOfElements(int elementNumbers) throws DeviceException, OutOfRangeException, InterruptedException;
	public void setNumberOfPulses(int pulseNumbers) throws DeviceException, OutOfRangeException, InterruptedException;
	public void setStartPulseElement(int startPulseElement) throws DeviceException, InterruptedException;
	public void setStopPulseElement(int stopPulseElement) throws DeviceException, InterruptedException;
	public int getStopPulseElement() throws TimeoutException, DeviceException;
	public void setTime(double d) throws DeviceException, InterruptedException;
	public void build() throws DeviceException, InterruptedException;
	public boolean isBuilding() throws DeviceException, TimeoutException, InterruptedException;
	public BuildStatus getBuildStatus();

}
