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

import gda.configuration.properties.LocalProperties;

/**
 * A class to implement a time frame group
 */
public class TimeFrameGroup2 {
	private int group;

	private int nframe;

	private int runTime;

	private int waitTime;

	private String runUnit;

	private String waitUnit;

	private String waitPause;

	private String runPause;

	private String waitPulse;

	private String runPulse;

	/**
	 * 
	 */
	public TimeFrameGroup2() {
	}

	/**
	 * Constructor to create a Time Frame Group with default data
	 * 
	 * @param group
	 *            number
	 */
	public TimeFrameGroup2(int group) {
		this.group = group;
		nframe    = 1;
		waitTime  = 100;
		waitUnit  = TimeFrameTableModel2.displayUnits[2];
		runTime   = 2;
		runUnit   = TimeFrameTableModel2.displayUnits[3];
		waitPause = TimeFrameTableModel2.displayPause[1];
		runPause  = TimeFrameTableModel2.displayPause[1];
		waitPulse = LocalProperties.get("gda.ncd.defaultWaitPulse", "00000000");
		runPulse  = LocalProperties.get("gda.ncd.defaultRunPulse", "11111111");
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
	 * @param waitPause
	 *            the wait pause bits
	 * @param runPause
	 *            the run pause bits
	 * @param waitPulse
	 *            the wait pulse bits
	 * @param runPulse
	 *            the run pulse bits
	 */
	public TimeFrameGroup2(int group, int nframe, int waitTime, String waitUnit, int runTime, String runUnit,
			String waitPause, String runPause, String waitPulse, String runPulse) {
		this.group = group;
		this.nframe = nframe;
		this.waitTime = waitTime;
		this.waitUnit = waitUnit;
		this.runTime = runTime;
		this.runUnit = runUnit;
		this.waitPause = waitPause;
		this.runPause = runPause;
		this.waitPulse = waitPulse;
		this.runPulse = runPulse;
	}

	/**
	 * @return a copy of the selected timeframe group.
	 */
	public TimeFrameGroup2 copy() {
		return new TimeFrameGroup2(group, nframe, waitTime, waitUnit, runTime, runUnit, waitPause, runPause, waitPulse,
				runPulse);
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
	 * Gets the wait pause
	 * 
	 * @return wait pause
	 */
	public String getWaitPause() {
		return waitPause;
	}

	/**
	 * sets the wait/run pause
	 * 
	 * @param newValue
	 *            new pause
	 */
	public void setWaitPause(String newValue) {
		waitPause = newValue;
	}

	/**
	 * Gets the run pause
	 * 
	 * @return run pause
	 */
	public String getRunPause() {
		return runPause;
	}

	/**
	 * sets the run pause
	 * 
	 * @param newValue
	 *            new pause
	 */
	public void setRunPause(String newValue) {
		runPause = newValue;
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
	 * @return the numeric value of the run time.
	 */
	public double getActualRunTime() {
		double factor = 1.0;
		if (runUnit.equals(TimeFrameTableModel2.displayUnits[0]))
			factor = 0.000001;
		else if (runUnit.equals(TimeFrameTableModel2.displayUnits[1]))
			factor = 0.001;
		else if (runUnit.equals(TimeFrameTableModel2.displayUnits[2]))
			factor = 1.0;
		else if (runUnit.equals(TimeFrameTableModel2.displayUnits[3]))
			factor = 1000.0;
		else if (runUnit.equals(TimeFrameTableModel2.displayUnits[4]))
			factor = 60000.0;
		else if (runUnit.equals(TimeFrameTableModel2.displayUnits[5]))
			factor = 3600000.0;

		return runTime * factor;
	}

	/**
	 * @return the numeric value of the wait time.
	 */
	public double getActualWaitTime() {
		double factor = 1.0;
		if (waitUnit.equals(TimeFrameTableModel2.displayUnits[0]))
			factor = 0.000001;
		else if (waitUnit.equals(TimeFrameTableModel2.displayUnits[1]))
			factor = 0.001;
		else if (waitUnit.equals(TimeFrameTableModel2.displayUnits[2]))
			factor = 1.0;
		else if (waitUnit.equals(TimeFrameTableModel2.displayUnits[3]))
			factor = 1000.0;
		else if (waitUnit.equals(TimeFrameTableModel2.displayUnits[4]))
			factor = 60000.0;
		else if (waitUnit.equals(TimeFrameTableModel2.displayUnits[5]))
			factor = 3600000.0;

		return waitTime * factor;
	}

	/**
	 * @return the numeric value of the wait pause bits.
	 */
	public int getWaitPauseValue() {
		int pause = -1;
		for (String item : TimeFrameTableModel2.displayPause) {
			if (item.equals(waitPause))
				break;
			pause++;
		}
		if (pause > 16)
			pause = ((pause - 16) | 0x20);
		return pause;
	}

	/**
	 * @return the numeric value of the run pause bits.
	 */
	public int getRunPauseValue() {
		int pause = -1;
		for (String item : TimeFrameTableModel2.displayPause) {
			if (item.equals(runPause))
				break;
			pause++;
		}
		if (pause > 16)
			pause = ((pause - 16) | 0x20);
		return pause;
	}

	/**
	 * @return the numeric value of the wait port.
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
	 * @return the numeric value of the run port.
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
