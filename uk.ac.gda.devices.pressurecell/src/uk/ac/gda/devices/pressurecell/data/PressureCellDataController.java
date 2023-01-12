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

package uk.ac.gda.devices.pressurecell.data;

import gda.device.DeviceException;

public interface PressureCellDataController {

	void setFilePath(String directory, String filename) throws DeviceException;

	void setAcquire(boolean acquiring) throws DeviceException;

	void setDataWriter(boolean acquiring) throws DeviceException;

	String getLastFileName() throws DeviceException;

	void setTriggers(int before, int after) throws DeviceException;

}
