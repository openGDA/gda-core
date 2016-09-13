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
import gda.factory.FactoryException;
import gda.observable.IObserver;

public interface Syringe extends Device, IObserver {

	boolean isBusy() throws DeviceException;

	void stop() throws DeviceException;

	void configure() throws FactoryException;

	/**
	 * Infuse or withdraw liquid from/to the syringe
	 *
	 * @param ml volume to infuse
	 * <br>- positive is infusing (reducing volume remaining in syringe)
	 * <br>- can be negative (withdrawal)
	 */
	void infuse(double ml) throws DeviceException;

	double getVolume() throws DeviceException;

	/**
	 * Get capacity of current syringe. Capacity may also need to be configured
	 * in the device hardware.
	 * @return capacity in ml
	 */
	double getCapacity();

	double getInfuseRate() throws DeviceException;

	double getWithdrawRate() throws DeviceException;

	/**
	 * Get time to infuse current volume at the current infuse rate
	 * @return Time in seconds
	 * @throws DeviceException
	 */
	double getRemainingTime() throws DeviceException;

	/**
	 * Set the volume of fluid in the syringe - must be set when syringe is refilled.
	 * @param ml current volume
	 * @throws DeviceException if syringe is busy when called
	 */
	void setVolume(double ml) throws DeviceException;

	boolean isEnabled();
}