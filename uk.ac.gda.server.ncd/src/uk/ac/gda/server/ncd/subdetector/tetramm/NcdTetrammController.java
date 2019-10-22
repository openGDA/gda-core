/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.subdetector.tetramm;

import gda.device.DeviceException;

public interface NcdTetrammController {

	public enum TriggerState {
		/**
		 * The detector is collecting continuously, updating the current
		 * value at the end of each averaging period.
		 */
		FREE_RUN,
		/**
		 * Collect one 'averaging time' of samples as a single frame starting
		 * from the rising edge of each trigger signal.
		 */
		EXT_TRIG,
		/**
		 * Averages samples for the duration of each high value trigger gate.
		 * Can cause different numbers of samples to be in each frame.
		 */
		EXT_BULB,
		/**
		 * Similar to {@link #FREE_RUN} but only used samples while the input
		 * trigger is in its high state
		 */
		EXT_GATE;
	}

	/** Get the number of values that are averaged to provide each reading */
	int getValuesPerReading() throws DeviceException;

	/** Set the values that are averaged to generate each reading */
	void setValuesPerReading(int values) throws DeviceException;

	/** Get the total averaging time for each frame */
	double getAveragingTime() throws DeviceException;

	/** Set the time to average values over for each frame */
	void setAveragingTime(double time) throws DeviceException;

	/** Get the sample time for each value */
	double getSampleTime() throws DeviceException;

	/** Get the number of samples that are averaged for each reading */
	int getSamplesToAverage() throws DeviceException;

	/** Get the number of samples that were averaged to provide the last reading */
	int getNumberOfAveragedSamples() throws DeviceException;

	/** Start or stop the detector acquiring */
	void setAcquire(boolean state) throws DeviceException;

	/** Get the acquire state of the detector */
	boolean isAcquiring() throws DeviceException;

	/** Set the number of channels to record */
	void setNumberOfChannels(int channels) throws DeviceException;

	/** Return the number of values to read out per trigger */
	int getNumberOfChannels() throws DeviceException;

	/** Set the trigger state */
	void setTriggerState(TriggerState state) throws DeviceException;

	/** Get the trigger state of the detector */
	TriggerState getTriggerState() throws DeviceException;

	/** Set the directory and file name for the filewriter */
	void setFilePath(String directory, String name) throws DeviceException;

	/** Get latest file path */
	String getLastFilePath() throws DeviceException;

	/** Set the scan dimensions including the per point frame count */
	void setDimensions(int framesPerPoint, int[] scanDims) throws DeviceException;

	/** Start or stop the file writer collecting */
	void setRecording(boolean state) throws DeviceException;

	/** Get state of file writer */
	boolean isRecording() throws DeviceException;

	/** Set all the non scan settings to where they need to be before a scan */
	void initialise() throws DeviceException;

	/** Reset options to how they were before the scan started */
	void reset() throws DeviceException;
}
