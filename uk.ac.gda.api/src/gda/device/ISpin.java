/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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
 * ISpin Interface
 */
public interface ISpin extends Scannable {
	/**
	 * enable spin or switch on spin
	 * 
	 * @throws DeviceException
	 */
	public abstract void on() throws DeviceException;

	/**
	 * disable spin or switch off spin
	 * 
	 * @throws DeviceException
	 */
	public abstract void off() throws DeviceException;

	/**
	 * set spin speed
	 * 
	 * @param speed
	 * @throws DeviceException
	 */
	public abstract void setSpeed(double speed) throws DeviceException;

	/**
	 * get spin speed
	 * 
	 * @return spin speed
	 * @throws DeviceException
	 */
	public abstract double getSpeed() throws DeviceException;

	/**
	 * check spin state - i.e. Enabled or Disabled
	 * 
	 * @return spin state
	 * @throws DeviceException
	 */
	public abstract String getState() throws DeviceException;

}