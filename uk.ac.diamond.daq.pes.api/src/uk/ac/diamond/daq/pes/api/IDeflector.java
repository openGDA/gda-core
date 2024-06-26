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

public interface IDeflector {
	/**
	 * Gets the deflector X value
	 *
	 * @return The deflector X value
	 * @throws DeviceException If there is a problem with communication
	 */
	public double getDeflectorX() throws DeviceException;

	/**
	 * Sets the deflector X value
	 *
	 * @param deflectorX The deflector X value
	 * @throws DeviceException If there is a problem with communication
	 */
	public void setDeflectorX(double deflectorX) throws DeviceException;

	/**
	 * Gets the deflector range configuration object for the analyser
	 *
	 * @return The deflector range configuration
	 */
	public AnalyserDeflectorRangeConfiguration getDeflectorRangeConfiguration();
}
