/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

public interface ConstantVelocityRasterMoveController extends ConstantVelocityMoveController {

	/**
	 * Set the outer dimension's start location for the move in hardware units/offset/scale.
	 * @param start
	 * @throws DeviceException
	 */
	void setOuterStart(double start) throws DeviceException;

	/**
	 * Set the outer dimension's end location for the move in hardware units/offset/scale.
	 * @param end
	 * @throws DeviceException
	 */
	void setOuterEnd(double end) throws DeviceException;


	/**
	 * Set the outer dimension's step size for the move in hardware units/scale.
	 * @param step
	 * @throws DeviceException
	 */
	void setOuterStep(double step) throws DeviceException;


}
