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

package uk.ac.gda.devices.odin;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

public interface OdinStrategy {


	/**
	 * To be called during the detector's readout method
	 * @return the appropriate Nexus structure
	 */
	NXDetectorData getNXDetectorData(String detName, double acquireTime, int scanPoint);

	/**
	 * Perform any required changes to the controller to prepare it for the start of a scan
	 * For example setting the file path and name.
	 */
	void prepareWriterForScan(String detName, int scanNumber, double collectionTime) throws DeviceException;

	/**
	 * Perform any required changes to the controller to prepare it for the point it is about
	 * to collect at
	 */
	void prepareWriterForPoint(int pointNumber) throws DeviceException;

	String[] getInputNames();

	String[] getExtraNames();

	String[] getOutputFormat();

	/**
	 * Return status of collection (for the current point). This depends on the strategy
	 */
	int getStatus();

	void waitWhileBusy(int scanPointNumber);


}

