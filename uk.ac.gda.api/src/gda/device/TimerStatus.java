/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device;

import java.io.Serializable;

/**
 * An class containing information on the status of a timer.
 */
final public class TimerStatus implements Serializable {
	private String currentStatus;

	private long elapsedTime;

	private int currentFrame;

	private int currentCycle;

	private int totalCycles;

	private int percentComplete;

	/**
	 * Construct a new Timer status with all parameters defined.
	 * 
	 * @param elapsedTime
	 * @param currentFrame
	 * @param currentCycle
	 * @param currentStatus
	 * @param totalCycles
	 * @param percentComplete
	 */
	public TimerStatus(long elapsedTime, int currentFrame, int currentCycle, String currentStatus, int totalCycles,
			int percentComplete) {
		this.elapsedTime = elapsedTime;
		this.currentFrame = currentFrame;
		this.currentCycle = currentCycle;
		this.currentStatus = currentStatus;
		this.totalCycles = totalCycles;
		this.percentComplete = percentComplete;
	}

	/**
	 * Get a string representation of the status
	 * 
	 * @return the status
	 */
	public String getCurrentStatus() {
		return currentStatus;
	}

	/**
	 * Get the elapsed time since the start of the initial start
	 * 
	 * @return the elapsed time
	 */
	public long getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * The current time frame
	 * 
	 * @return the current frame
	 */
	public int getCurrentFrame() {
		return currentFrame;
	}

	/**
	 * Get the current time frame cycle since the start
	 * 
	 * @return the current time frame cycle
	 */
	public int getCurrentCycle() {
		return currentCycle;
	}

	/**
	 * The total number of cycles completed for multiple timer starts.
	 * 
	 * @return total number of cycles completed
	 */
	public int getTotalCycles() {
		return totalCycles;
	}

	/**
	 * Get the percentage of completion
	 * 
	 * @return the percentage
	 */
	public int getPercentComplete() {
		return percentComplete;
	}

	@Override
	public String toString() {
		return "TimerStatus: status " + currentStatus + " cycle " + currentCycle + " frame " + currentFrame
				+ " total cycles " + totalCycles + " " + percentComplete + "% complete";
	}
}
