/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.continuouscontroller;

import gda.device.DeviceException;

/**
 * A continuous move controller represents a motor controller capable of moving one or axes while generating hardware
 * triggers. 
 */
public interface ContinuousMoveController extends HardwareTriggerProvider {

	/**
	 * Prepare the already configured controller to move, waiting until ready.
	 * Normally this should move motors to the start position.
	 * @throws InterruptedException 
	 */
	public void prepareForMove() throws DeviceException, InterruptedException;

	/**
	 * Start the prepared move and return immediately.
	 * @throws DeviceException
	 */
	public void startMove() throws DeviceException;

	/**
	 * Check if the controller is moving axes.
	 * @return true if moving.
	 * @throws DeviceException
	 */
	public boolean isMoving() throws DeviceException;

	/**
	 * Wait until the controller has completed moving axes
	 * @throws InterruptedException
	 */
	public void waitWhileMoving() throws DeviceException, InterruptedException;

	/**
	 * Stop a move if one is in progress and then reset the controller.
	 * @throws DeviceException
	 * @throws InterruptedException 
	 */
	public void stopAndReset() throws DeviceException, InterruptedException;

}
