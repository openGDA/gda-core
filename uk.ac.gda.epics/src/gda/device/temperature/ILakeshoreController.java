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

package gda.device.temperature;

import gda.device.DeviceException;
import gda.factory.ConditionallyConfigurable;
import gda.factory.Findable;
import gda.observable.IObservable;

public interface ILakeshoreController extends Findable, ConditionallyConfigurable, IObservable {


	public final double MAX_RAMP_RATE = 2.0; // Kevin/min
	public final double MIN_RAMP_RATE = 2.0; // K/min

	public default boolean isConnected() {
		return true;
	}

	public default String getConnectionState() {
		return "Enabled";
	}

	/**
	 * Sets the demand temperature . If ramping is enabled then the actual demand temperature will move
	 * towards this at the ramping rate.
	 *
	 * @param demandTemperature
	 *            The demanded temperature in K
	 * @throws DeviceException
	 */
	void setTargetTemp(double demandTemperature) throws DeviceException;

	/**
	 * Gets the current demand temperature in K.
	 *
	 * @return The current demand temperature from the readback value
	 * @throws DeviceException
	 */
	double getTargetTemp() throws DeviceException;

	/**
	 * gets current temperature
	 *
	 * @return temp
	 * @throws DeviceException
	 */
	double getTemp() throws DeviceException;

	/**
	 * gets channel 0 temperature
	 *
	 * @return channel 0 temperature
	 * @throws DeviceException
	 */
	double getChannel0Temp() throws DeviceException;

	/**
	 * gets channel 1 temperature
	 *
	 * @return channel 1 temperature
	 * @throws DeviceException
	 */
	double getChannel1Temp() throws DeviceException;

	/**
	 * gets channel 2 temperature
	 *
	 * @return channel 2 temperature
	 * @throws DeviceException
	 */
	double getChannel2Temp() throws DeviceException;

	/**
	 * gets channel 3 temperature
	 *
	 * @return channel 3 temperature
	 * @throws DeviceException
	 */
	double getChannel3Temp() throws DeviceException;

	int getReadbackChannel();

	void setReadbackChannel(int readbackChannel);

}