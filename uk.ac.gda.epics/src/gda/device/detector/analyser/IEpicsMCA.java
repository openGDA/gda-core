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

package gda.device.detector.analyser;

import gda.device.Analyser;
import gda.device.DeviceException;
import gda.factory.Configurable;

/**
 * Interface to a class that communicates with an epics MCA record.<br>
 * The MCA record controls and acquires data from a multichannel analyser (MCA).
 */
public interface IEpicsMCA extends Analyser, Configurable {
	/**
	 * Clears the mca, but does not return until the clear has been done.
	 *
	 * @throws DeviceException
	 */
	void clearWaitForCompletion() throws DeviceException;

	/**
	 * Gets the Dwell Time (DWEL).
	 *
	 * @return Dwell Time
	 * @throws DeviceException
	 */
	double getDwellTime() throws DeviceException;

	/**
	 * Sets the dwell time (DWEL)
	 *
	 * @param time
	 * @throws DeviceException
	 */
	void setDwellTime(double time) throws DeviceException;

	/**
	 * Activates the MCA using the Erase & Start button.
	 *
	 * @throws DeviceException
	 */
	void eraseStartAcquisition() throws DeviceException;
}