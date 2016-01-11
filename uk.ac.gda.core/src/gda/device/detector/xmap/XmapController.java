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

package gda.device.detector.xmap;

import gda.device.Device;
import gda.device.DeviceException;
import gda.factory.Configurable;
import gda.factory.Findable;

/**
 *
 */
public interface XmapController  extends Device, Configurable, Findable{

	/**
	 * Clears all MCAs and starts data collection
	 * @throws DeviceException
	 */
	public void clearAndStart() throws DeviceException;

	/**
	 * Start data acquisition in all MCA's
	 * @throws DeviceException
	 */
	public void start() throws DeviceException;
	/**
	 * Stop data acquisition in all MCA's
	 * @throws DeviceException
	 */
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
	 * Returns the twoD data array of the all the MCAs
	 * @return data array
	 * @throws DeviceException
	 */
	public int[][] getData()throws DeviceException;

	/**
	 * Returns the Number of Regions of Interest
	 * @return roi number
	 */
	public int getNumberOfROIs();

	/**
	 * Sets the Number of Regions of Interest for all MCAs
	 * @param numberOfROIs
	 */
	public void setNumberOfROIs(int numberOfROIs);


	/**
	 * Returns the number of MCAs currently connected to the XMap detector
	 * @return number of MCA
	 * @throws DeviceException
	 */
	public int getNumberOfElements()throws DeviceException;

	/**
	 * Sets the number of MCAs currently connected to the XMap detector
	 * @param numberOfElements
	 * @throws DeviceException
	 */
	public void setNumberOfElements(int numberOfElements) throws DeviceException;
	/**
	 * Sets the rate at which status is read in the Epics interface
	 * @param statusRate
	 * @throws DeviceException
	 */
	public void setStatusRate(String statusRate)throws DeviceException;
	/**
	 * Sets the rate at which data is read in the Epics interface
	 * @param readRate
	 * @throws DeviceException
	 */
	public void setReadRate(String readRate)throws DeviceException;

	/**
	 * returns the sum of the regions of interest for each region of all MCAs
	 * @return a double array e.g if there are 4 mcas and 3 ois each , it returns
	 * [region1_sum, region2_sum, region3_sum]
	 * @throws DeviceException
	 */
	public double[] getROIsSum()throws DeviceException;


	/**
	 * Returns the roi count for the specified roiIndex in all MCAs
	 * @param roiIndex
	 * @return a double array, e.g if there are 4 mcas and the specified index is 1, it returns
	 * [mca1_region1_count, mca2_region1_count, mca3_region1_count, mca4_region1_count]
	 * @throws DeviceException
	 */
	public double[] getROICounts(int roiIndex)throws DeviceException;

	/**
	 * Sets the all ROIs of the specified MCA.
	 * @param rois array. Each roi should have a low and high value.
	 * @param mcaIndex
	 * @throws DeviceException
	 */
	public void setROI(double[][] rois, int mcaIndex)throws DeviceException;

	/**
	 * Deletes all the current regions of interest on a give mca detector element.
	 * @param mcaIndex
	 * @throws DeviceException
	 */
	public void deleteROIs(final int mcaIndex) throws DeviceException;

	/**
	 * Sets the all ROIs of all the MCA.  All mcas will be set the same roi
	 * @param rois array. Each roi should have a low and high value.
	 * @throws DeviceException
	 */
	public void setROIs(double[][] rois)throws DeviceException;
	/**
	 * Sets the specified roi for all the mcas
	 * @param rois array. Each roi should have a low and high value.
	 * @param roiIndex
	 * @throws DeviceException
	 */
	public void setNthROI(double[][] rois, int roiIndex)throws DeviceException;

	/**
	 *
	 * @param element
	 * @return the total count for the last element
	 * @throws DeviceException
	 */
	public int getEvents(int element) throws DeviceException;

	/**
	 * Reads the input count rate for an element also known as
	 * the fast filter rate outside EPICS
	 *
	 * @param element
	 * @return rate
	 */
	public double getICR(int element) throws DeviceException;

	/**
	 * Reads the output count rate for an element also known as
	 * the slow filter rate outside EPICS
	 *
	 * @param element
	 * @return rate
	 */
	public double getOCR(int element) throws DeviceException;

	/**
	 * @param mcaNumber
	 * @return double array of regions of interest
	 * @throws DeviceException
	 */
	public double[] getROIs(int mcaNumber) throws DeviceException;
	/**
	 * @param mcaNumber
	 * @return double array of regions of interest
	 * @throws DeviceException
	 */
	public double[] getROIs(int mcaNumber, int[][]data) throws DeviceException;

	public double[][] getROIParameters(int mcaNumber) throws DeviceException;
}
