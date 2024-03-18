/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.device.models;

import org.eclipse.scanning.api.scan.ScanningException;

/**
 * This interface defines the methods for finding the last good point.
 * Implementing classes should provide methods that find the last good point
 * according to variable parameters like exposure time, chunk size, number of written
 * frames, etc.
 */
public interface SeekStrategy {

	/**
	 * Get the last good point to resume to based on the last point that was scanned.
	 * @param scanPoint
	 * @return last good point
	 */
	public int getPointToSeek(int scanPoint);

	/**
	 * Configures the variable parameters that will be used to get the last point
	 * when the device is called. This will be called by the device configure method.
	 * If an exception is thrown, the scan will not run.
	 * @throws ScanningException if configuration fails
	 */
	@SuppressWarnings("unused")
	default void configure() throws ScanningException {

	}


}
