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

package uk.ac.gda.bimorph;

import gda.device.Device;
import gda.device.DeviceException;

public interface BimorphMirrorController extends Device {
	public double getVoltage(int channel) throws DeviceException;
	public double[] getVoltages() throws DeviceException;

	public void setVoltage(int channel, double voltage) throws DeviceException;
	public void setVoltages(double... voltages) throws DeviceException;

	public int getNumberOfChannels() throws DeviceException;
	public boolean isBusy() throws DeviceException;
	public double getMaxDelta();
	public double getMaxVoltage();
	public double getMinVoltage();
}
