/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.analyser;

import gda.device.DeviceException;

/**
 * An interface to devices that support the EPICS MCA record
 */
public interface IEpicsMCASimple extends IEpicsMCA {
	/**
	 * Get specified region of interest
	 *
	 * @param regionIndex
	 *            Index of the ROI required
	 * @return a representation of the ROI
	 * @throws DeviceException
	 */
	EpicsMCARegionOfInterest getNthRegionOfInterest(int regionIndex) throws DeviceException;

	/**
	 * Set the regions of interest for the MCA
	 *
	 * @param epicsMcaRois
	 *            The ROIs to set
	 * @throws DeviceException
	 */
	void setRegionsOfInterest(EpicsMCARegionOfInterest[] epicsMcaRois) throws DeviceException;

	/**
	 * sets calibration fields for MCA
	 *
	 * @param calibrate
	 *            The calibration fields
	 * @throws DeviceException
	 */
	void setCalibration(EpicsMCACalibration calibrate) throws DeviceException;

	/**
	 * Counts for the specified channel
	 *
	 * @param index
	 *            Index of the channel
	 * @return counts for this channel
	 * @throws DeviceException
	 */
	double getRoiCount(int index) throws DeviceException;

	/**
	 * Net counts for the specified ROI
	 *
	 * @param index
	 *            Index of the ROI required
	 * @return Counts for this ROI
	 * @throws DeviceException
	 */
	double getRoiNetCount(int index) throws DeviceException;

	/**
	 * Set PV for the MCA
	 *
	 * @param mcaPV
	 */
	void setMcaPV(String mcaPV);
}
