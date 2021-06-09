/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.pes.api;

import gda.device.DeviceException;

public interface IDitherScanning {

	/**
	 * Gets the number of dither steps to be used in dither scanning mode
	 *
	 * @return The number of dither steps
	 * @throws DeviceException
	 */
	int getNumberOfDitherSteps() throws DeviceException;

	/**
	 * Sets the number of dither steps to be used when in dither scanning mode
	 *
	 * @throws DeviceException if there is a problem setting the number of steps
	 */
	void setNumberOfDitherSteps(int ditherSteps) throws DeviceException;
}