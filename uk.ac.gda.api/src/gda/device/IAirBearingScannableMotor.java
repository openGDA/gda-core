/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

/**
 * An extended {@link Scannable} interface to provide control of motor's air bearing,
 * i.e. switch on/off air supply to the motor. 
 */
public interface IAirBearingScannableMotor extends ScannableMotionUnits {
	/**
	 * switch on the air supply
	 * @throws DeviceException
	 */
	public abstract void on() throws DeviceException;
	/**
	 * switch off the air supply
	 * @throws DeviceException
	 */
	public abstract void off() throws DeviceException;
	/**
	 * check if the air supply to the motor is on or not.
	 * @return on - true, off -false
	 * @throws DeviceException
	 */
	public abstract boolean isOn() throws DeviceException;

}