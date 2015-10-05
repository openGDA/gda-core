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

package uk.ac.gda.devices.pixium;

import gda.device.detector.areadetector.v17.EpicsAreaDetector;

public interface IPixiumDetector extends EpicsAreaDetector{//, NexusDetector{
	/** Possible status value, indicates detector is in readout state */
	public final int READOUT = MONITORING + 1;
	/** Possible status value, indicates detector is in data correcting state */
	public final int CORRECT = READOUT + 1;
	/** Possible status value, indicates detector is in data saving state */
	public final int SAVING = CORRECT + 1;
	/** Possible status value, indicates detector is in aborting state */
	public final int ABORTING = SAVING + 1;
	public final int WAITING = ABORTING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int MODECHANGING = WAITING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int DEFINING = MODECHANGING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int DELETING = DEFINING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int LOADING = DELETING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int UNLOADING = LOADING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int ACTIVATING = UNLOADING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int DEACTIVATING = ACTIVATING + 1;
	public final int INITIALIZING = DEACTIVATING + 1;
	/**
	 * acquire specified number of images without moving any other devices.
	 * @param numberOfImage
	 * @throws Exception
	 */
	public abstract void acquire(int numberOfImage) throws Exception;
	/**
	 * check if data saving to local Windows local storage or not.
	 * @return true if saving to local storage; false if saving to remote storage.
	 */
	public abstract boolean isLocalDataStore();
	/**
	 * change data storage location.
	 * if true, saving to Windows C: driver, users must create data directory hierarchy themselves as GDA cannot see Windows' local driver;
	 * if false, saving to Lustre, GDA will create data directory automatically.
	 * @param localDataStore
	 */
	public abstract void setLocalDataStore(boolean localDataStore);
	/**
	 * starting Offset calibration processing of the detector.
	 * This must be called after changing mode.
	 * @throws Exception
	 */
	void startOffsetCalibration() throws Exception;
	/**
	 * Change detector mode 
	 * @param logicalMode - the logical mode number
	 * @param offsetreferenceNumber - the offset reference number
	 * @return the message returned by the driver
	 * @throws Exception
	 */
	String setMode(int logicalMode, int offsetreferenceNumber) throws Exception;
	/**
	 * connect EPICS driver to device controller hardware.
	 * @throws Exception
	 */
	void connect() throws Exception;
	/**
	 * disconnect EPICS driver to device controller hardware.
	 * This must be called if you want temporarily switch to use TTT software
	 * @throws Exception
	 */
	void disconnect() throws Exception;
	/**
	 * start detector acquiring
	 * @throws Exception
	 */
	void startAcquire() throws Exception;
	/**
	 * start file or data array capturing
	 * @throws Exception
	 */
	void startCapture() throws Exception;
	/**
	 * stop detector acquiring
	 * @throws Exception
	 */
	void stopAcquire() throws Exception;
	/**
	 * stop file or data array capturing
	 * @throws Exception
	 */
	void stopCapture() throws Exception;
	double getAcquirePeriod() throws Exception;
	void startOffsetCalibration(double timeout) throws Exception;

}