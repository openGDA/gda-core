/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.syringepump;

import gda.device.Device;
import gda.device.DeviceException;
import gda.factory.Configurable;

public interface SyringePumpController extends Device, Configurable {

	boolean isBusy() throws DeviceException;

	boolean isEnabled();

	void stop() throws DeviceException;

	void setForce(double percent) throws DeviceException;
	double getForce() throws DeviceException;

	void setTargetTime(double seconds) throws DeviceException;
	double getTargetTime() throws DeviceException;

	void setDiameter(double millimeters) throws DeviceException;
	double getDiameter() throws DeviceException;

	void setInfuseRate(double mlps) throws DeviceException;
	double getInfuseRate() throws DeviceException;

	void setWithdrawRate(double mlps) throws DeviceException;
	double getWithdrawRate() throws DeviceException;

	void infuse(double ml) throws DeviceException;

	double getCapacity();

	double getVolume() throws DeviceException;

	void setVolume(double ml) throws DeviceException;

}
