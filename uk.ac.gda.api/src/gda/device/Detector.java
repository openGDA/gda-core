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

import gda.configuration.properties.LocalProperties;

/**
 * Interface used by ScanBase and its descendents to control data collection from Devices. All devices which need to
 * appear in scans must implement this interface.
 */
public interface Detector extends Scannable {
	/** Possible status value, indicates detector is idle. */
	public final int IDLE = 0;

	/** Possible status value, indicates detector is busy. */
	public final int BUSY = IDLE + 1;

	/** Possible status value, indicates detector is busy, but in paused state */
	public final int PAUSED = BUSY + 1;
	/**
	 * Possible status value, indicates detector is not ready for use or needs configuring.
	 */
	public final int STANDBY = PAUSED + 1;

	/** Possible status value, indicates detector in an error state. */
	public final int FAULT = STANDBY + 1;

	/** Possible status value, equals BUSY but indicates something else too */
	public final int MONITORING = FAULT + 1;

	/**
	 * Tells the detector to begin to collect a set of data, then returns immediately. Should cause the hardware to
	 * start collecting immediately: if there is any delay then detectors used in the same scan would collect over
	 * different times when beam conditions may differ.
	 * 
	 * @throws DeviceException
	 */

	public void collectData() throws DeviceException;

	/**
	 * Sets the collection time, in seconds, to be used during the next call of collectData.
	 * 
	 * @param time
	 *            the collection time in seconds
	 * @throws DeviceException
	 */
	public void setCollectionTime(double time) throws DeviceException;

	/**
	 * Returns the time, in seconds, the detector collects for during the next call to collectData()
	 * 
	 * @return double
	 */
	double getCollectionTime() throws DeviceException;

	/**
	 * Returns the current collecting state of the device.
	 * 
	 * @return BUSY if the detector has not finished the requested operation(s), IDLE if in an completely idle state and
	 *         STANDBY if temporarily suspended.
	 * @throws DeviceException
	 */
	public int getStatus() throws DeviceException;

	/**
	 * Returns the latest data collected. The size of the Object returned must be consistent with the values returned by
	 * getDataDimensions and getExtraNames.
	 * <p> 
	 * If {@link LocalProperties#GDA_SCAN_CONCURRENTSCAN_READOUT_CONCURRENTLY} is true then motors may be moved while the detector
	 * readouts. The value returned <b>must</b> not be effected by any concurrent motor or shutter movements. See {@link #waitWhileBusy()}
	 * and {@code ConcurrentScan}. Readout must block until the detector is ready to respond quickly to {@link #collectData()} again.
	 * 
	 * @return the data collected
	 * @throws DeviceException
	 */
	public Object readout() throws DeviceException;

	/**
	 * Wait while the detector collects data. Should return as soon as the exposure completes and it is safe to move motors.
     * i.e. counts <b>must</b> be safely latched either in hardware or software before returning.
	 */
	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException;
	
	/**
	 * Returns the dimensions of the data object returned by the {@link #readout()} method.
	 * 
	 * @return the dimensions of the data object returned by the {@link #readout()} method
	 * @throws DeviceException
	 */
	public int[] getDataDimensions() throws DeviceException;

	/**
	 * Method called before a scan starts. May be used to setup detector for collection, for example MAR345 uses this to
	 * erase.
	 * <p>
	 * Note: it is recommended to not implement this method, but to implement one or both of
	 * {@link Scannable#atScanLineStart()} or {@link Scannable#atScanStart()} instead. Implementing this method may
	 * cause issues when the detector class is used in multi-dimensional scans.
	 * 
	 * @throws DeviceException
	 */
	public void prepareForCollection() throws DeviceException;

	/**
	 * Method called at the end of collection to tell detector when a scan has finished. Typically integrating detectors
	 * used in powder diffraction do not output until the end of the scan and need to be told when this happens.
	 * 
	 * @throws DeviceException
	 */
	public void endCollection() throws DeviceException;

	/**
	 * Returns a value which indicates whether the detector creates its own files. If it does (return true) the
	 * readout() method returns the name of the latest file created as a string. If it does not (return false) the
	 * readout() method will return the data directly.
	 * 
	 * @return true if readout() returns filenames
	 * @throws DeviceException
	 */
	public boolean createsOwnFiles() throws DeviceException;

	/**
	 * @return A description of the detector.
	 * @throws DeviceException
	 */
	public String getDescription() throws DeviceException;

	/**
	 * @return A identifier for this detector.
	 * @throws DeviceException
	 */
	public String getDetectorID() throws DeviceException;

	/**
	 * @return The type of detector.
	 * @throws DeviceException
	 */
	public String getDetectorType() throws DeviceException;

}
