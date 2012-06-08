/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17;

import gda.device.DeviceException;

/**
 * Interface to Prosilica Area Detector Driver see
 * http://cars9.uchicago.edu/software/epics/prosilicaDoc.html#Driver_parameters
 */
public interface ADDriverProsilica {
	public enum SYNCIN_LEVEL {
		LOW, HIGH
	}

	SYNCIN_LEVEL getSyncIn1Level() throws DeviceException;

	SYNCIN_LEVEL getSyncIn2Level() throws DeviceException;

	public enum SYNCOUT_MODE {
		GPO, ACQ_TRIG_READY, FRAME_TRIG_READY, FRAME_TRIGGER, EXPOSING, FRAME_READOUT, IMAGING, ACQUIRING, SYNCIN1, SYNCIN2, SYNCIN3, SYNCIN4, STROBE1, STROBE2, STROBE3, STROBE4
	}

	void setSyncOut1Mode(SYNCOUT_MODE mode) throws DeviceException;

	SYNCOUT_MODE getSyncOut1Mode() throws DeviceException;

	void setSyncOut2Mode(SYNCOUT_MODE mode) throws DeviceException;

	SYNCOUT_MODE getSyncOut2Mode() throws DeviceException;

	void setSyncOut3Mode(SYNCOUT_MODE mode) throws DeviceException;

	SYNCOUT_MODE getSyncOut3Mode() throws DeviceException;

	void setSyncOut1Level(boolean level) throws DeviceException;

	boolean getSyncOut1Level() throws DeviceException;

	void setSyncOut1Invert(boolean invert) throws DeviceException;

	boolean getSyncOut1Invert() throws DeviceException;

	void setSyncOut2Level(boolean level) throws DeviceException;

	boolean getSyncOut2Level() throws DeviceException;

	void setSyncOut2Invert(boolean invert) throws DeviceException;

	boolean getSyncOut2Invert() throws DeviceException;

	void setSyncOut3Level(boolean level) throws DeviceException;

	boolean getSyncOut3Level() throws DeviceException;

	void setSyncOut3Invert(boolean invert) throws DeviceException;

	boolean getSyncOut3Invert() throws DeviceException;

	enum STROBE_MODE {
		ACQ_TRIG_READY, FRAME_TRIG_READY, FRAME_TRIGGER, EXPOSING, FRAME_READOUT, ACQUIRING, SYNCIN1, SYNCIN2, SYNCIN3, SYNCIN4
	}

	void setStrobeMode(STROBE_MODE mode) throws DeviceException;

	STROBE_MODE getStrobeMode() throws DeviceException;

	void setStrobeCtlDuration(boolean yes_no) throws DeviceException;

	boolean getStrobeCtlDuration() throws DeviceException;

	void setStrobeDuration(double duration) throws DeviceException;

	double getStrobeDuration() throws DeviceException;

	void setStrobeDelay(double delay) throws DeviceException;

	double getStrobeDelay() throws DeviceException;

	String getDriverType() throws DeviceException;

	String getFilterVersion() throws DeviceException;

	double getFrameRate() throws DeviceException;

	double getFramesCompleted() throws DeviceException;

	double getFramesDropped() throws DeviceException;

	double getPacketsErroneuos() throws DeviceException;

	double getPacketsMissed() throws DeviceException;

	double getPacketsReceived() throws DeviceException;

	double getPacketsRequested() throws DeviceException;

	double getPacketsResent() throws DeviceException;

	double getBadFrameCounter() throws DeviceException;

}
