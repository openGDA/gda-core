/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector;

import gda.device.ContinuousParameters;
import gda.device.Detector;
import gda.device.DeviceException;

/**
 * Interface for detectors who can hold a series of data points (frames) in memory which can be read out once a scan has
 * finished. Such detectors are triggered by hardware and not by the GDA software. This mean the detector can be used in
 * fast scans where the readout will be at a slower rate than the data collection.
 */
public interface BufferedDetector extends Detector {

	/**
	 * Clears the detector memory
	 * 
	 * @throws DeviceException
	 */
	public void clearMemory() throws DeviceException;

	/**
	 * When in slave mode the detector will be triggered by an external signal to collect each frame of data.
	 * 
	 * @param on
	 */
	public void setContinuousMode(boolean on) throws DeviceException;

	/**
	 * @return true if the detector is ready to accept trigger pulses
	 */
	public boolean isContinuousMode() throws DeviceException;

	/**
	 * Sets the parameters which define the continuous movement to use
	 * 
	 * @param parameters
	 */
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException;

	/**
	 * @return ContinuousParameters
	 */
	public ContinuousParameters getContinuousParameters() throws DeviceException;

	/**
	 * @return number of frames of data in memory which have been collected
	 * @throws DeviceException
	 */
	public int getNumberFrames() throws DeviceException;

	/**
	 * An array of the data from the detector. Each element is one frame of data. The first frame is 0.
	 * 
	 * @param startFrame
	 * @param finalFrame
	 * @return Object
	 * @throws DeviceException
	 */
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException;

	/**
	 * An array of the data from the detector. Each element is one frame of data.
	 * 
	 * @return Object
	 * @throws DeviceException
	 */
	public Object[] readAllFrames() throws DeviceException;

	/**
	 * As certain detectors may cause memory issues if too many frames are attempted to be read in one go, this is the
	 * maximum for this detector based on its current configuration (i.e. after setContinuousMode(True) has been called.
	 * <p>
	 * The lowest value returned from any of the detectors in a continuous scan will be the limit set.
	 * <p>
	 * If there is no limit then this method should return Integer.MAX_VALUE.
	 * 
	 * @return int - the maximum number of frames which should be read at any one time from this detector
	 * @throws DeviceException
	 */
	public int maximumReadFrames() throws DeviceException;

}
