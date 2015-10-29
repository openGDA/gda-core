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
 * A constant velocity move controller represents a motor controller that can move a single axis between two pots at fixed
 * speed while generating hardware triggers.
 */
public interface ConstantVelocityMoveController extends ContinuousMoveController {

	/**
	 * Set the start location for the move in hardware units/offset/scale.
	 * @param start
	 * @throws DeviceException 
	 */
	void setStart(double start) throws DeviceException;

	double getStart();

	/**
	 * Set the end location for the move in hardware units/offset/scale.
	 * @param end
	 * @throws DeviceException 
	 */
	void setEnd(double end) throws DeviceException;

	double getEnd();

	/**
	 * Set the step size for the move in hardware units/scale.
	 * @param step
	 * @throws DeviceException 
	 */
	void setStep(double step) throws DeviceException;

	double getStep();
}
