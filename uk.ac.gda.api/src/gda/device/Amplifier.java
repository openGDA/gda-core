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
 * An interface for a distributed Amplifier class
 */
public interface Amplifier extends Device {
	/**
	 * Performs automatic current suppression.
	 * 
	 * @throws DeviceException
	 */
	public void autoCurrentSuppress() throws DeviceException;

	/**
	 * Performs automatic zero correct.
	 * 
	 * @throws DeviceException
	 */
	public void autoZeroCorrect() throws DeviceException;

	/**
	 * Gets the current suppress value.
	 * 
	 * @return the current suppress value
	 * @throws DeviceException
	 */
	public double getCurrentSuppressValue() throws DeviceException;

	/**
	 * Gets the Filter rise time.
	 * 
	 * @return filter rise time as a double (implementation specific)
	 * @throws DeviceException
	 */
	public double getFilterRiseTime() throws DeviceException;

	/**
	 * Gets the total gain.
	 * 
	 * @return current gain as double (implementation specific)
	 * @throws DeviceException
	 */
	public double getGain() throws DeviceException;

	/**
	 * Returns status of the amplifier
	 * 
	 * @return status string eg ready/invalid command error (implementation specific)
	 * @throws DeviceException
	 */
	public String getStatus() throws DeviceException;

	/**
	 * Gets the programmed voltage bias value
	 * 
	 * @return voltage bias as double
	 * @throws DeviceException
	 */
	public double getVoltageBias() throws DeviceException;

	/**
	 * To enable or disable auto filter operation
	 * 
	 * @param onOff
	 *            true to enable
	 * @throws DeviceException
	 */
	public void setAutoFilter(boolean onOff) throws DeviceException;

	/**
	 * Sets the current suppress on/off
	 * 
	 * @param onOff
	 *            true to enable
	 * @throws DeviceException
	 */
	public void setCurrentSuppress(boolean onOff) throws DeviceException;

	/**
	 * Sets the current suppression value and selects range automatically
	 * 
	 * @param value
	 *            currentSuppression value
	 * @throws DeviceException
	 */
	public void setCurrentSuppressionParams(double value) throws DeviceException;

	/**
	 * Sets the current suppression value and range
	 * 
	 * @param value
	 *            current suppression value
	 * @param range
	 * @throws DeviceException
	 */
	public void setCurrentSuppressionParams(double value, int range) throws DeviceException;

	/**
	 * Enable/disable x10 gain
	 * 
	 * @param onOff
	 *            true to enable
	 * @throws DeviceException
	 */
	public void setEnlargeGain(boolean onOff) throws DeviceException;

	/**
	 * Enable/disable filter
	 * 
	 * @param onOff
	 *            true to enable
	 * @throws DeviceException
	 */
	public void setFilter(boolean onOff) throws DeviceException;

	/**
	 * Sets the rise time of the filter
	 * 
	 * @param level
	 *            (implementation specific)
	 * @throws DeviceException
	 */
	public void setFilterRiseTime(int level) throws DeviceException;

	/**
	 * Sets instrument gain
	 * 
	 * @param level
	 *            (implementation specific)
	 * @throws DeviceException
	 */
	public void setGain(int level) throws DeviceException;

	/**
	 * Enable/disable voltage bias output
	 * 
	 * @param voltageBias
	 *            true specifies enable
	 * @throws DeviceException
	 */
	public void setVoltageBias(boolean voltageBias) throws DeviceException;

	/**
	 * Sets the voltage bias value
	 * 
	 * @param value
	 *            voltage bias setting
	 * @throws DeviceException
	 */
	public void setVoltageBias(double value) throws DeviceException;

	/**
	 * Enable/disable zero check
	 * 
	 * @param onOff
	 *            true to enable
	 * @throws DeviceException
	 */
	public void setZeroCheck(boolean onOff) throws DeviceException;
}