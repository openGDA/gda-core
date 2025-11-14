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

/**
 * An interface for a distributed Analyser class
 */
public interface Analyser extends Detector {
	/**
	 * Method to turn on acquisition of the analyser
	 *
	 * @throws DeviceException
	 */
	void startAcquisition() throws DeviceException;

	/**
	 * Method to turn off acquisition of the analyser
	 *
	 * @throws DeviceException
	 */
	void stopAcquisition() throws DeviceException;

	/**
	 * Method to add a new region of interest to the analyser
	 *
	 * @param regionIndex
	 *            the region number
	 * @param regionLow
	 *            the start value of region
	 * @param regionHigh
	 *            the end value of region
	 * @param regionBackground
	 *            the region background
	 * @param regionPreset
	 *            the region preset
	 * @param regionName
	 *            the region name
	 * @throws DeviceException
	 */
	void addRegionOfInterest(int regionIndex, double regionLow, double regionHigh, int regionBackground,
			double regionPreset, String regionName) throws DeviceException;

	/**
	 * Method to remove a region of interest from the analyser
	 *
	 * @param regionIndex
	 *            the region number
	 * @throws DeviceException
	 */
	void deleteRegionOfInterest(int regionIndex) throws DeviceException;

	/**
	 * Method to erase the analyzer data, sets all channels to zero
	 *
	 * @throws DeviceException
	 */
	void clear() throws DeviceException;

	/**
	 * Returns calibration parameters for the analyser. Return calibration offset, calibration slope, calibration
	 * quadratic , two theta angle of the detector.
	 *
	 * @return calibration parameters
	 * @throws DeviceException
	 */
	Object getCalibrationParameters() throws DeviceException;

	/**
	 * Method to get data from the analyser
	 *
	 * @return the analyser data
	 * @throws DeviceException
	 */
	Object getData() throws DeviceException;

	/**
	 * Method to get the elapsed parameters for the analyser Not sure if it is specific to MCA
	 *
	 * @return the elapsed paramters
	 * @throws DeviceException
	 */
	Object getElapsedParameters() throws DeviceException;

	/**
	 * Method to read the preset parameters for the analyser
	 *
	 * @return the preset parameters
	 * @throws DeviceException
	 */
	Object getPresets() throws DeviceException;

	/**
	 * Method to read the number of regions for the analyser
	 *
	 * @return the number of regions
	 * @throws DeviceException
	 */
	int getNumberOfRegions() throws DeviceException;

	/**
	 * Method to read the number of channels for the analyser
	 *
	 * @return the number of channels
	 * @throws DeviceException
	 */
	long getNumberOfChannels() throws DeviceException;

	/**
	 * Method to get regions of interest for the analyser
	 *
	 * @return the regions of interest
	 * @throws DeviceException
	 */
	Object getRegionsOfInterest() throws DeviceException;

	/**
	 * Method to return net and total counts of each region of interest in the analyser
	 *
	 * @return two dimensional array of net and total counts
	 * @throws DeviceException
	 */
	double[][] getRegionsOfInterestCount() throws DeviceException;

	/**
	 * Method to return the current sequence number of the analyser. Might be specific to MCAs
	 *
	 * @return the current sequence number
	 * @throws DeviceException
	 */
	long getSequence() throws DeviceException;

	/**
	 * Sets the calibration parameters for the analyser.
	 *
	 * @param calibrate
	 *            the calibration parameters to set
	 * @throws DeviceException
	 */
	void setCalibration(Object calibrate) throws DeviceException;

	/**
	 * Writes data to the Analyser
	 *
	 * @param data
	 *            the data to write back to the analyser
	 * @throws DeviceException
	 */
	void setData(Object data) throws DeviceException;

	/**
	 * Sets the preset parameters for the Analyser
	 *
	 * @param data
	 *            the preset parameters
	 * @throws DeviceException
	 */
	void setPresets(Object data) throws DeviceException;

	/**
	 * Sets the ADC parameters for the Analyser
	 *
	 * @param data
	 * @throws DeviceException
	 */
	// public void setAdcParameters(Object adc) throws DeviceException;
	/**
	 * Sets the regions of interest for the Analyser
	 *
	 * @param lowHigh
	 *            the upper and lower bounds of the region
	 * @throws DeviceException
	 */
	void setRegionsOfInterest(Object lowHigh) throws DeviceException;

	/**
	 * Sets sequence for the Analyser
	 *
	 * @param sequence
	 *            the sequence
	 * @throws DeviceException
	 */
	void setSequence(long sequence) throws DeviceException;

	/**
	 * Set number of regions
	 *
	 * @param regions
	 *            the number of regions to set
	 * @throws DeviceException
	 */
	void setNumberOfRegions(int regions) throws DeviceException;

	/**
	 * Set number of Channels
	 *
	 * @param channels
	 *            the number of channels to set
	 * @throws DeviceException
	 */
	void setNumberOfChannels(long channels) throws DeviceException;
}
