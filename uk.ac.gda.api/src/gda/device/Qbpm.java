/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import gda.device.DeviceException;

/**
 * Qbpm interface
 */
public interface Qbpm {

	/**
	 * @return BPM name
	 * @throws DeviceException
	 */
	public String getBpmName() throws DeviceException;

	/**
	 * @param name
	 * @throws DeviceException
	 */
	public void setBpmName(String name) throws DeviceException;

	/**
	 * @return Current Amp Quad Name
	 * @throws DeviceException
	 */
	public String getCurrAmpQuadName() throws DeviceException;

	/**
	 * @param name
	 * @throws DeviceException
	 */
	public void setCurrAmpQuadName(String name) throws DeviceException;

	/**
	 * @return current1
	 * @throws DeviceException
	 */
	public double getCurrent1() throws DeviceException;

	/**
	 * @return current2
	 * @throws DeviceException
	 */
	public double getCurrent2() throws DeviceException;

	/**
	 * @return current3
	 * @throws DeviceException
	 */
	public double getCurrent3() throws DeviceException;

	/**
	 * @return current4
	 * @throws DeviceException
	 */
	public double getCurrent4() throws DeviceException;

	/**
	 * @return range value
	 * @throws DeviceException
	 */
	public String getRangeValue() throws DeviceException;

	/**
	 * @return intensity total
	 * @throws DeviceException
	 */
	public double getIntensityTotal() throws DeviceException;

	/**
	 * @return X Position
	 * @throws DeviceException
	 */
	public double getXPosition() throws DeviceException;

	/**
	 * @return Y position
	 * @throws DeviceException
	 */
	public double getYPosition() throws DeviceException;

}