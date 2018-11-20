/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress4;

import java.io.IOException;

import gda.device.DeviceException;

public interface Xspress4Controller {

	/**
	 * Return individual scaler value for detector element
	 * @param element
	 * @param scalerNumber index of scaler
	 * @return
	 * @throws DeviceException
	 */
	double getScalerValue(int element, int scalerNumber) throws DeviceException;

	/**
	 * Return array of scaler values for a detector element. These are the SCA0, SCA1 ... SCA8 values.
	 * @param element
	 * @return array [num scalers]
	 * @throws DeviceException
	 */
	double[] getScalerArray(int element) throws DeviceException;

	/**
	 * Return array of resolution grade values for a detector element. These are in-'window counts'
	 * for each resolution grade (normally 16)
	 * @param element
	 * @param window
	 * @return array of data [num resolution grades]
	 * @throws DeviceException
	 */
	double[] getResGradeArrays(int element, int window) throws DeviceException;

	/**
	 * Return array with current values of MCA data for single detector element.
	 * @return array of data [number of MCA channels]
	 * @throws IOException
	 * @throws DeviceException
	 */
	double[] getMcaData(int element) throws DeviceException;

	/**
	 * Return array with current values of MCA data for all detector elements
	 * @return array of data [num detector elements, number of MCA channels]
	 * @throws IOException
	 * @throws DeviceException
	 */
	double[][] getMcaData() throws DeviceException;

	/**
	 * Return deadtime correction factor for each detector element. This is a multiplicative factor
	 * that can be applied to in-window scaler counts to correct for missed photon counts.
	 * @return array of data [num detector elements]
	 * @throws Device
	 */
	double[] getDeadtimeCorrectionFactors() throws DeviceException;

	/**
	 * Set whether the detector to record resolution grade data in the hdf file.
	 * This is done by setting the binning to use for the 'resolution grade' dimension of the MCA
	 * dataset in the hdf writer to either :
	 		<li> 1 == save each grade (used for 'Region of interest' readout mode).
			<li> 16 == don't save the grades, i.e. integrate over grades
				(used for 'MCA', 'MCA + scalers' readout mode).
	 *
	 * @param saveResGradeData
	 * @return true if binning was changed, false otherwise.
	 * @throws DeviceException
	*/
	boolean setSaveResolutionGradeData(boolean saveResGradeData) throws DeviceException;

	/**
	 * Set the deadtime correction energy value (energy in keV).
	 * @param energyKev energy in keV.
	 * @throws DeviceException
	 */
	void setDeadtimeCorrectionEnergy(double energyKev) throws DeviceException;
	double getDeadtimeCorrectionEnergy() throws DeviceException;

	/**
	 * Reset the counter used to indicate the number of frames read out back to zero.
	 * (this counter is not used during data collection).
	 * @throws DeviceException
	 */
	void resetFramesReadOut() throws DeviceException;

	/**
	 * Return total number of frames available to readout (ArrayCounter_RBV)
	 * @return
	 * @throws DeviceException
	 */
	int getTotalFramesAvailable() throws DeviceException;

	/**
	 * Set collection time ('Acquire time') - used when doing software triggered step scans.
	 * @param time frame time in seconds.
	 * @throws DeviceException
	 */
	void setAcquireTime(double time) throws DeviceException;

	/**
	 * Set the trigger mode. The integer is the index in the trigger mode enum PV to select.
	 * @param triggerMode index of the trigger mode.
	 * @throws DeviceException
	 */
	void setTriggerMode(int triggerMode) throws DeviceException;
	int getTriggerMode() throws DeviceException;

	/**
	 * Set the number of detector elements.
	 * @param numElements
	 */
	void setNumElements(int numElements);

	/**
	 *
	 * @return The number of detector elements
	 * (called 'Num Channels' in 'System Configuration' EDM settings)
	 */
	int getNumElements();

	/**
	 * Set the number of scaler values available.
	 * (Does not set value on PV)
	 * @param numScalers
	 */
	void setNumScalers(int numScalers);
	int getNumScalers();

	/**
	 * Block unit 'number of frames acquired counter' increments from specified value
	 * @param numFrames
	 * @param timeoutMillis
	 * @throws InterruptedException
	 * @throws DeviceException
	 */
	void waitForCounterToIncrement(int currentCount, long timeoutMillis) throws DeviceException, InterruptedException;

	/**
	 *
	 * @return Number of MCA channels of data associated with each detector element
	 * (called 'Max MCA Elements' in 'System Configuration' EDM settings)
	 */
	int getNumMcaChannels();

	/**
	 * Set number of MCA channels
	 * @param numChannels
	 */
	void setNumMcaChannels(int numChannels);
}
