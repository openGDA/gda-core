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

import gda.device.xspress.Detector;
import gda.device.xspress.DetectorReading;

/**
 * Xspress systems must implement this to enable CORBA use
 */
public interface Xspress extends Device {
	/**
	 * Get number of detectors
	 * 
	 * @return the number of detectors
	 * @throws DeviceException
	 */
	public int getNumberOfDetectors() throws DeviceException;

	/**
	 * Read a detector
	 * 
	 * @param which
	 *            detector to read
	 * @return the detector the value read as a DetectorReading
	 * @throws DeviceException
	 */
	public DetectorReading readDetector(int which) throws DeviceException;

	/**
	 * Read an array of detectors (must be previously initialised)
	 * 
	 * @return array of DetectorReading read
	 * @throws DeviceException
	 */
	public DetectorReading[] readDetectors() throws DeviceException;

	/**
	 * Get multi-channel data for a particular detector
	 * 
	 * @param which
	 *            the detector number
	 * @param start
	 *            the starting channel number of the window
	 * @param end
	 *            the ending channel number of the window
	 * @param time
	 *            the time to count for (mS)
	 * @return an array of readings from channels
	 * @throws DeviceException
	 */
	public Object getMCData(int which, int start, int end, int time) throws DeviceException;

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
	public Detector getDetector(int which) throws DeviceException;

	/**
	 * Save detector information to a file
	 * 
	 * @param filename
	 *            the file to save detector information to
	 * @throws DeviceException
	 */
	public void saveDetectors(String filename) throws DeviceException;

	/**
	 * Quit
	 * 
	 * @throws DeviceException
	 */
	public void quit() throws DeviceException;

	/**
	 * Create and initialize detectors specified in a file.
	 * 
	 * @param string
	 *            the file to read detector information from
	 * @return a string indicating the succes or otherwise.
	 * @throws DeviceException
	 */
	public String loadAndInitializeDetectors(String string) throws DeviceException;

	/**
	 * Set the gain for a particular detector
	 * 
	 * @param detector
	 * @param gain
	 * @throws DeviceException
	 */
	public void setDetectorGain(int detector, double gain) throws DeviceException;

	/**
	 * Set the offset for a particular detector
	 * 
	 * @param detector
	 * @param offset
	 * @throws DeviceException
	 */
	public void setDetectorOffset(int detector, double offset) throws DeviceException;

	/**
	 * Read out the detector data
	 * 
	 * @return the data as an object
	 * @throws DeviceException
	 */
	public Object readout() throws DeviceException;

	/**
	 * Read a section of data from the specified frame.
	 * 
	 * @param startChannel
	 *            the start channel
	 * @param channelCount
	 *            the number of channels
	 * @param frame
	 *            the frame
	 * @return the data as an array of doubles
	 * @throws DeviceException
	 */
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException;

	/**
	 * Set the readout mode
	 * 
	 * @param newMode
	 *            the mode
	 * @throws DeviceException
	 */
	public void setReadoutMode(int newMode) throws DeviceException;
}
