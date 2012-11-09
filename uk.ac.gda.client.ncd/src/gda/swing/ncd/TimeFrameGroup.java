/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.swing.ncd;

/**
 * A class to implement a time frame group
 */
public class TimeFrameGroup {
	private int group;

	private int nframe;

	private int runTime;

	private int waitTime;

	private String runUnit;

	private String waitUnit;

	private String pause;

	private String waitPulse;

	private String runPulse;

	/**
	 * Constructor
	 */
	public TimeFrameGroup() {
	}

	/**
	 * Constructor to create a Time Frame Group with default data
	 * 
	 * @param group
	 *            number
	 */
	public TimeFrameGroup(int group) {
		this.group = group;
		nframe = 1;
		waitTime = 1;
		waitUnit = TimeFrameTableModel.displayUnits[1];
		runTime = 10;
		runUnit = TimeFrameTableModel.displayUnits[2];
		pause = "00";
		waitPulse = "00000000";
		runPulse = "00000000";
	}

	/**
	 * Constructor to create a Time Frame Group with default data
	 * 
	 * @param group
	 *            the group number
	 * @param nframe
	 *            the number of frame in the group
	 * @param waitTime
	 *            the wait time
	 * @param waitUnit
	 *            the wait unit
	 * @param runTime
	 *            the run time
	 * @param runUnit
	 *            the run unit
	 * @param pause
	 *            the pause bits
	 * @param waitPulse
	 *            the wait pulse bits
	 * @param runPulse
	 *            the run pulse bits
	 */
	public TimeFrameGroup(int group, int nframe, int waitTime, String waitUnit, int runTime, String runUnit,
			String pause, String waitPulse, String runPulse) {
		this.group = group;
		this.nframe = nframe;
		this.waitTime = waitTime;
		this.waitUnit = waitUnit;
		this.runTime = runTime;
		this.runUnit = runUnit;
		this.pause = pause;
		this.waitPulse = waitPulse;
		this.runPulse = runPulse;
	}

	/**
	 * @return TimeFrameGroup
	 */
	public TimeFrameGroup copy() {
		return new TimeFrameGroup(group, nframe, waitTime, waitUnit, runTime, runUnit, pause, waitPulse, runPulse);
	}

	/**
	 * sets the group number
	 * 
	 * @param newValue
	 */
	public void setGroup(int newValue) {
		group = newValue;
	}

	/**
	 * Gets the group number
	 * 
	 * @return group number
	 */
	public int getGroup() {
		return group;
	}

	/**
	 * sets the number of frame in group
	 * 
	 * @param newValue
	 */
	public void setFrames(int newValue) {
		nframe = newValue;
	}

	/**
	 * Gets the number of frame in group
	 * 
	 * @return frame count
	 */
	public int getFrames() {
		return nframe;
	}

	/**
	 * sets the wait time
	 * 
	 * @param newValue
	 */
	public void setWaitTime(int newValue) {
		waitTime = newValue;
	}

	/**
	 * Gets the wait time
	 * 
	 * @return wait time
	 */
	public int getWaitTime() {
		return waitTime;
	}

	/**
	 * Gets the wait unit
	 * 
	 * @return wait unit
	 */
	public String getWaitUnit() {
		return waitUnit;
	}

	/**
	 * sets the wait unit
	 * 
	 * @param newValue
	 *            new units
	 */
	public void setWaitUnit(String newValue) {
		waitUnit = newValue;
	}

	/**
	 * sets the run time
	 * 
	 * @param newValue
	 */
	public void setRunTime(int newValue) {
		runTime = newValue;
	}

	/**
	 * Gets the run time
	 * 
	 * @return run time
	 */
	public int getRunTime() {
		return runTime;
	}

	/**
	 * Gets the run unit
	 * 
	 * @return run unit
	 */
	public String getRunUnit() {
		return runUnit;
	}

	/**
	 * sets the run unit
	 * 
	 * @param newValue
	 *            new units
	 */
	public void setRunUnit(String newValue) {
		runUnit = newValue;
	}

	/**
	 * Gets the wait/run pause
	 * 
	 * @return pause
	 */
	public String getPause() {
		return pause;
	}

	/**
	 * sets the wait/run pause
	 * 
	 * @param newValue
	 *            new pause
	 */
	public void setPause(String newValue) {
		pause = newValue;
	}

	/**
	 * Gets the wait pulse
	 * 
	 * @return wait pulses
	 */
	public String getWaitPulses() {
		return waitPulse;
	}

	/**
	 * sets the wait pulse
	 * 
	 * @param newValue
	 *            new wait pulse
	 */
	public void setWaitPulses(String newValue) {
		waitPulse = newValue;
	}

	/**
	 * Gets the run pulse
	 * 
	 * @return pulse
	 */
	public String getRunPulses() {
		return runPulse;
	}

	/**
	 * sets the run pulse
	 * 
	 * @param newValue
	 *            new run pulse
	 */
	public void setRunPulses(String newValue) {
		runPulse = newValue;
	}

	/**
	 * @return actual run time
	 */
	public double getActualRunTime() {
		double factor = 1.0;
		if (runUnit.equals("usec"))
			factor = 0.001;
		else if (runUnit.equals("msec"))
			factor = 1.0;
		else if (runUnit.equals("sec"))
			factor = 1000.0;
		else if (runUnit.equals("min"))
			factor = 60000.0;
		else if (runUnit.equals("hour"))
			factor = 3600000.0;

		return runTime * factor;
	}

	/**
	 * @return actual wait time
	 */
	public double getActualWaitTime() {
		double factor = 1.0;
		if (waitUnit.equals("usec"))
			factor = 0.001;
		else if (waitUnit.equals("msec"))
			factor = 1.0;
		else if (waitUnit.equals("sec"))
			factor = 1000.0;
		else if (waitUnit.equals("min"))
			factor = 60000.0;
		else if (waitUnit.equals("hour"))
			factor = 3600000.0;

		return waitTime * factor;
	}

	/**
	 * @return int
	 */
	public int getWaitPause() {
		return Character.getNumericValue(pause.charAt(0));
	}

	/**
	 * @return int
	 */
	public int getRunPause() {
		return Character.getNumericValue(pause.charAt(1));
	}

	/**
	 * @return wait port
	 */
	public int getWaitPort() {
		int value = 0;
		for (int i = waitPulse.length() - 1; i >= 0; i--) {
			value *= 2;
			value += Character.getNumericValue(waitPulse.charAt(i));
		}
		return value;
	}

	/**
	 * @return run port
	 */
	public int getRunPort() {
		int value = 0;
		for (int i = runPulse.length() - 1; i >= 0; i--) {
			value *= 2;
			value += Character.getNumericValue(runPulse.charAt(i));
		}
		return value;
	}
}
