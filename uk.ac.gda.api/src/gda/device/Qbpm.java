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

/**
 * Qbpm interface
 */
public interface Qbpm {

	/**
	 * @return BPM name
	 * @throws DeviceException
	 */
	String getBpmName() throws DeviceException;

	/**
	 * @param name
	 * @throws DeviceException
	 */
	void setBpmName(String name) throws DeviceException;

	/**
	 * @return Current Amp Quad Name
	 * @throws DeviceException
	 */
	String getCurrAmpQuadName() throws DeviceException;

	/**
	 * @param name
	 * @throws DeviceException
	 */
	void setCurrAmpQuadName(String name) throws DeviceException;

	/**
	 * @return current1
	 * @throws DeviceException
	 */
	double getCurrent1() throws DeviceException;

	/**
	 * @return current2
	 * @throws DeviceException
	 */
	double getCurrent2() throws DeviceException;

	/**
	 * @return current3
	 * @throws DeviceException
	 */
	double getCurrent3() throws DeviceException;

	/**
	 * @return current4
	 * @throws DeviceException
	 */
	double getCurrent4() throws DeviceException;

	/**
	 * @return range value
	 * @throws DeviceException
	 */
	String getRangeValue() throws DeviceException;

	/**
	 * @return intensity total
	 * @throws DeviceException
	 */
	double getIntensityTotal() throws DeviceException;

	/**
	 * @return X Position
	 * @throws DeviceException
	 */
	double getXPosition() throws DeviceException;

	/**
	 * @return Y position
	 * @throws DeviceException
	 */
	double getYPosition() throws DeviceException;
}