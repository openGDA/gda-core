/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.odin.control;

import gda.device.Detector;
import gda.device.DeviceException;

/**
 * Controller for an Odin detector. This is designed for an Odin deployment with a single file writer (frame receiver
 * and processor) only.
 */
public interface OdinDetectorController {

	/**
	 * Set the directory and the file prefix for the Odin datawriter
	 * @throws DeviceException
	 */
	void setDataOutput(String directory, String filePrefix) throws DeviceException;

	/**
	 * Start the camera acquiring
	 *
	 * @throws DeviceException
	 */
	void startCollection() throws DeviceException;

	/**
	 * Stop the camera acquiring
	 *
	 * @throws DeviceException
	 */
	void stopCollection() throws DeviceException;

	/**
	 * Start the data writer
	 *
	 * @throws DeviceException
	 */
	void startRecording() throws DeviceException;

	/**
	 * Stop the data writer
	 */
	void endRecording();

	/**
	 * Get camera ready for acquisition e.g set the exposure time and wait time (in ms). Things that only need to be set
	 * at the beginning of the scan
	 */
	void prepareCamera(int frames, double requestedLiveTime, double requestedDeadTime, String imageMode, String triggerMode) throws DeviceException;

	/**
	 * Get the full path to the most recent file written
	 */
	String getLatestFilename();

	/**
	 * Get status, this will return {@link Detector#BUSY} if either the datawriter or the detector acquisition is busy
	 * @return integer as defined on the {@link Detector} interface
	 */
	int getStatus();


	/**
	 * Wait until the detector acquisition (exposure) is 'Done'
	 */
	void waitWhileAcquiring();

	/**
	 * Wait until the datawriter is 'Done'
	 */
	void waitWhileWriting();


	/**
	 * Prepare the Odin Data writer this may include setting the number of expected frames
	 * or ensuring that the compression settings are as required
	 * @param frames number of frames Odin Data will be expecting
	 * @throws DeviceException
	 */
	void prepareDataWriter(int frames) throws DeviceException;

	double getAcquireTime() throws DeviceException;

	double getAcquirePeriod() throws DeviceException;

	/**
	 * Set the offet and uid for the filewriter. This may be required between frames depending on collection strategy.
	 * @param offset
	 * @param uid
	 * @throws DeviceException
	 */
	void setOffsetAndUid(int offset, int uid) throws DeviceException;

	int getNumFramesCaptured();

	void waitForWrittenFrames(int noFrames);

	void setNumImages(int numImages) throws DeviceException;

	void setCompressionMode(String mode) throws DeviceException;

	void setAcquireTime(double acquireTime)  throws DeviceException;

	void setAcquirePeriod(double acquirePeriod)  throws DeviceException;

}
