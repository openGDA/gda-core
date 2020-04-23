/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.device;

import gda.factory.Findable;

/**
 * a LED light provides either On/Off switch, or brightness control, or both
 */
public interface ILEDLight extends Findable {
	/**
	 * weather the device has switch control or not
	 * @return true or false
	 */
	boolean hasSwitch();
	/**
	 * weather the device has brightness control or not
	 * @return true or false
	 */
	boolean hasBrightnessControl();
	/**
	 * switch light on
	 * @throws DeviceException
	 */
	void on() throws DeviceException;
	/**
	 * switch light off
	 * @throws DeviceException
	 */
	void off() throws DeviceException;
	/**
	 * set the brightness of the light
	 * @param v
	 * @throws DeviceException
	 */
	void setBrightness(double v) throws DeviceException;
	/**
	 * get the brightness of the light
	 * @return the brightness of the light
	 * @throws DeviceException
	 */
	double getBrightness() throws DeviceException;
}
