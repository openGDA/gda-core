/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.viewer;

import gda.device.DeviceException;



public interface IRotationSource extends IPositionSource {

	/**
	 * Move the source by adding a relative amount to the
	 * current position
	 * @param value the value to be moved by
	 */
	public double calcMovePlusRelative(double value) throws DeviceException;
	
	/**
	 * Move the source by minusing a relative amount from the
	 * current position
	 * @param value the value to be moved by
	 */	
	public double calcMoveMinusRelative(double value) throws DeviceException;
}
