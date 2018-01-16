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

package uk.ac.gda.beans.xspress;

import java.util.ArrayList;

import gda.device.Detector;
import gda.device.DeviceException;
import uk.ac.gda.beans.vortex.DetectorElement;

/**
 * Xspress systems must implement this to enable CORBA use
 */
public interface XspressDetector extends Detector {

	/**
	 * readoutMode option - when this used Xspress will not use ROIs but return hardware scalers only
	 */
	public static final String READOUT_SCALERONLY = "Scalers only";
	/**
	 * readoutMode option - when this used Xspress will not use ROIs but return hardware scalers and full MCA
	 */
	public static final String READOUT_MCA = "Scalers and MCA";
	/**
	 * readoutMode option - when this used Xspress will not return data from ROIs and not use hardware scalers
	 */
	public static final String READOUT_ROIS = "Regions Of Interest";

	/**
	 * Enable the xspress system for data collection
	 *
	 * @throws DeviceException
	 */
	public void start() throws DeviceException;

	/**
	 * Clear the xspress system memory
	 *
	 * @throws DeviceException
	 */
	public void clear() throws DeviceException;

	/**
	 * Get number of detectors
	 *
	 * @return the number of detectors
	 * @throws DeviceException
	 */
	public int getNumberOfDetectors() throws DeviceException;

	/**
	 * Get multi-channel data for all elements
	 *
	 * @TODO remove this from interface (only needed for {@link XspressParametersUIEditor} which is no longer used).
	 * @param time
	 *            the time to count for (mS)
	 * @return an array of readings from channels
	 * @throws DeviceException
	 */
	@Deprecated
	public int[][][] getMCData(int time) throws DeviceException;

	/**
	 * Gets the raw hardware scaler values from the first frame of memory. This does not trigger any data collection.
	 * This for monitoring the Xspress status.
	 * <p>
	 * Will return an int array of size 4 * number of detectors (elements).
	 *
	 * @return int[] 4 values for each element in order: total number of events, TFG reset counts, in window events,TFG
	 *         clock counts
	 * @throws DeviceException
	 */
	public int[] getRawScalerData() throws DeviceException;

	/**
	 * Set the channel range window for a particular detector
	 *
	 * @param which
	 *            the detector number
	 * @param start
	 *            the start channel number
	 * @param end
	 *            the end channel number
	 * @throws DeviceException
	 */
	public void setDetectorWindow(int which, int start, int end) throws DeviceException;

	/**
	 * Get the detector information
	 *
	 * @param which
	 *            the detector number to get information from
	 * @return detector information as a Detector object
	 * @throws DeviceException
	 */
	public DetectorElement getDetector(int which) throws DeviceException;

	/**
	 * Save detector information to a file
	 *
	 * @param filename
	 *            the file to save detector information to
	 * @throws DeviceException
	 */
	public void saveDetectors(String filename) throws DeviceException;

	/**
	 * Create and initialize detectors specified in a file.
	 *
	 * @param string
	 *            the file to read detector information from
	 * @throws DeviceException
	 * @throws Exception
	 */
	public void loadAndInitializeDetectors(String string) throws Exception;

	/**
	 * Read out MCA data
	 *
	 * @return the data as an object
	 * @throws DeviceException
	 */
	@Override
	public Object readout() throws DeviceException;

	/**
	 * Read out corrected scaler data
	 *
	 * @return the data as an object
	 * @throws DeviceException
	 */
	public Object readoutScalerData() throws DeviceException;

	/**
	 * get the current channel label list;
	 *
	 * @return the channel labels
	 */
	public ArrayList<String> getChannelLabels();

	/**
	 * @return the number of resolution bins
	 */
	public int getNumberofGrades();

	/**
	 * @return the res grade
	 * @throws DeviceException
	 */
	public String getResGrade() throws DeviceException;

	/**
	 * @param grade
	 * @throws DeviceException
	 */
	public void setResGrade(final String grade) throws DeviceException;

	/**
	 * @return the readoutMode
	 * @throws DeviceException
	 */
	public String getReadoutMode() throws DeviceException;

	/**
	 * Sets the type of data which will be collected from the detector and returned by the readout and
	 * readoutCorrectData methods.
	 *
	 * @param readoutMode
	 * @throws DeviceException
	 */
	public void setReadoutMode(final String readoutMode) throws DeviceException;

	/**
	 * The energy to use when calculating the energy-dependent deadtime correction factors. This value is used
	 * internally and affects the values returned by the readout and readoutScalerData methods. This will not affect the
	 * results of the getRawScalerData method.
	 * <p>
	 * It is recommended that the energy used in EXAFS experiments, for example, is the edge energy. It should be near
	 * the peak counts in the spectrum being studied.
	 * <p>
	 * If the value null or 0.0 is entered then the deadtime calculation used will not be energy dependent.
	 *
	 * @param energy
	 * @throws DeviceException
	 */
	public void setDeadtimeCalculationEnergy(final Double energy) throws DeviceException;

	/**
	 * @return Double - the energy used in energy-dependent deadtime calculations.
	 * @throws DeviceException
	 */
	public Double getDeadtimeCalculationEnergy() throws DeviceException;

}
