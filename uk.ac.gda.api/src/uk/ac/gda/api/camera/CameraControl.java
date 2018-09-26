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

package uk.ac.gda.api.camera;

import gda.device.DeviceException;
import gda.factory.Findable;

/**
 * An interface allowing access to basic camera operations
 * <p>
 * It was originally written to allow clients to control camera functions without having to export large objects such as
 * area detectors over RMI.
 */
public interface CameraControl extends Findable {

	/**
	 * Get acquire (exposure) time
	 *
	 * @return Acquire time in seconds
	 * @throws DeviceException
	 */
	double getAcquireTime() throws DeviceException;

	/**
	 * Set acquire (exposire) time
	 *
	 * @param acquiretime
	 *            in seconds
	 * @throws DeviceException
	 */
	void setAcquireTime(double acquiretime) throws DeviceException;

	/**
	 * Start acquiring data
	 *
	 * @throws DeviceException
	 */
	void startAcquiring() throws DeviceException;

	/**
	 * Stop acquiring data
	 *
	 * @throws DeviceException
	 */
	void stopAcquiring() throws DeviceException;

	/**
	 * Get the camera acquire state
	 * <p>
	 * @return acquire state
	 * @throws DeviceException
	 */
	CameraState getAcquireState() throws DeviceException;
}
