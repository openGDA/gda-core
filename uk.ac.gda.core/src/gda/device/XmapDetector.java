/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import java.util.List;


/**
 * Interface to control the Vortex XMAP detector. This detector has a 
 * collection of MCAs which can all be triggered at the same time.
 */
public interface XmapDetector extends Detector {
	/**
	 * Clears all MCAs and starts data collection
	 * @throws DeviceException
	 */
	public void clearAndStart() throws DeviceException;
	/**
	 * Clear all MCA's
	 * @throws DeviceException
	 */
	public void clear() throws DeviceException;
	/**
	 * Start data acquisition in all MCA's
	 * @throws DeviceException
	 */
	public void start() throws DeviceException;
	/**
	 * Stop data acquisition in all MCA's
	 * @throws DeviceException
	 */
	@Override
	public void stop() throws DeviceException;
	/**
	 * Sets maximum numbers of bins/channels to use for spectrum acquisition  in each MCA
	 * @param numberOfBins
	 * @throws DeviceException
	 */
	public void setNumberOfBins(int numberOfBins)throws DeviceException;
	/**
	 * Returns current number of Bins/channels for spectrum acquisition in each MCA
	 * @return numberOfBins
	 * @throws DeviceException
	 */
	public int getNumberOfBins()throws DeviceException;
	/**
	 * Sets the rate at which status is read in the Epics interface
	 * @param statusRate
	 * @throws DeviceException
	 */
	public void setStatusRate(double statusRate)throws DeviceException;
	/**
	 * Returns the current set rate at which status is read in the Epics interface
	 * @return status rate
	 * @throws DeviceException
	 */
	public double getStatusRate()throws DeviceException;
	/**
	 * Sets the rate at which data is read in the Epics interface
	 * @param readRate
	 * @throws DeviceException
	 */
	public void setReadRate(double readRate)throws DeviceException;
	/**
	 * Returns the current rate at which data is read in the Epics interface
	 * @return readRate
	 * @throws DeviceException
	 */
	public double getReadRate()throws DeviceException;
	/**
	 * Returns the elapsed real time after a read status operation
	 * @return real time
	 * @throws DeviceException
	 */
	public double getRealTime()throws DeviceException;
	/**
	 * Returns the status of the detector
	 * @return Detector.IDLE/BUSY
	 * @throws DeviceException
	 */
	@Override
	public int getStatus()throws DeviceException;
	/**
	 * Tells the detector for how many seconds to acquire data
	 * @param time
	 * @throws DeviceException
	 */
	public void setAcquisitionTime(double time)throws DeviceException;
	/**
	 * Returns the time for which the detector is set to acquire data
	 * @return time
	 * @throws DeviceException
	 */
	public double getAcquisitionTime()throws DeviceException;
	/**
	 * Returns the data array of the specified MCA
	 * @param mcaNumber
	 * @return data array
	 * @throws DeviceException
	 */
	public int[] getData(int mcaNumber)throws DeviceException;
	/**
	 * Returns the data array of all the MCAs
	 * @return array of data arrays
	 * @throws DeviceException
	 */
	public int[][] getData()throws DeviceException;
	/**
	 * Returns the sum of the corresponding ROIs of all the MCA.
	 * For e.g if the MCAs have three ROIs each say R0, R1 and R2. This method sums up the R0 of all the MCA
	 * R1 of all the MCAs and so on
	 * @return ROI sum array
	 * @throws DeviceException
	 */
	public double[] getROIsSum()throws DeviceException;
	/**
	 * Sets the ROIs of all the MCA. Use this method only if you want to set all the MCA with the 
	 * same ROIs.To set different ROI for each of the MCA use setNthROI method.
	 * @param rois array. Each roi should have a low and high value.
	 * @throws DeviceException
	 */
	public void setROIs(double[][] rois)throws DeviceException;
	/**
	 * Returns the total number of ROIs currently set
	 * @return number of ROIs
	 * @throws DeviceException
	 */
	public int getNumberOfROIs()throws DeviceException;
	/**
	 * Returns the number of MCAs currently connected to the XMap detector
	 * @return number of MCA
	 * @throws DeviceException
	 */
	public int getNumberOfMca()throws DeviceException;
	/**
	 * Sets a ROI for all of the MCAs 
	 * @param rois This array length should be same as the number of MCAs
	 * @param roiIndex
	 * @throws DeviceException
	 */
	public void setNthROI(double[][]rois, int roiIndex)throws DeviceException;
	
	/**
	 * Lightly to be only implemented on the server.
	 * @return the labels for the channels defined by the user.
	 */
	public List<String> getChannelLabels();
	
	/**
	 * Gets the count for each mca for a given ROI.
	 * @param iRoi
	 * @return double[] of counts per mca for this roi
	 */
	public double[] getROICounts(int iRoi) throws DeviceException;
	
	/**
	 * Reads out the scaler data of the roi counts, corrects for deadtime
	 * and returns the sum.
	 * 
	 * @return The FF data
	 */
	public double readoutScalerData() throws DeviceException;
}
