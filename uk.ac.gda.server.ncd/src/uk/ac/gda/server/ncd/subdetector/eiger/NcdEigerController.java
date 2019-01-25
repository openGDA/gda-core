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

package uk.ac.gda.server.ncd.subdetector.eiger;

import gda.device.DeviceException;

public interface NcdEigerController {

	/**
	 * Get the dimensions of each image
	 * @throws DeviceException If image size cannot be read
	 */
	int[] getDataDimensions() throws DeviceException;

	/**
	 * Start the camera acquiring
	 * @throws DeviceException
	 */
	void startCollection() throws DeviceException;

	/**
	 * Stop the camera acquiring
	 * @throws DeviceException
	 */
	void stopCollection() throws DeviceException;

	/**
	 * Start the data writer
	 * @throws DeviceException
	 */
	void startRecording() throws DeviceException;

	/** Stop the data writer */
	void endRecording();

	void setDataOutput(String createFromDefaultProperty, String format) throws DeviceException;

	/** Set the dimensions of the scan */
	void setScanDimensions(int[] dims) throws DeviceException;

	/** Set the exposure time and wait time in ms */
	void setExposureTimes(int frames, double requestedLiveTime, double requestedDeadTime) throws DeviceException;

	/** Get the full path to the most recent file written */
	String getLastFile();
}
