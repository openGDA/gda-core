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
 * Interface to control ADCs, for example EpicsADC.
 */
public interface Adc extends Device {
	/**
	 * Get the current voltage for the specified channel.
	 * 
	 * @param channel
	 *            the ADC channel to read
	 * @return the current voltage (units are implementation specific)
	 * @throws DeviceException
	 */
	public double getVoltage(int channel) throws DeviceException;

	/**
	 * Get the current voltage from all open channels.
	 * 
	 * @return the current voltages (units are implementation specific)
	 * @throws DeviceException
	 */
	public double[] getVoltages() throws DeviceException;

	/**
	 * Set the voltage Range for the specified channel.
	 * 
	 * @param channel
	 *            the ADC channel to set
	 * @param range
	 *            the range to set (units are implementation specific)
	 * @see #getRange
	 * @throws DeviceException
	 */
	public void setRange(int channel, int range) throws DeviceException;

	/**
	 * Get the current voltage Range for the specified channel.
	 * 
	 * @param channel
	 *            the ADC channel to get
	 * @return the range (units are implementation specific)
	 * @see #setRange
	 * @throws DeviceException
	 */
	public int getRange(int channel) throws DeviceException;

	/**
	 * Set the unipolar or bipolar mode for a specified channel.
	 * 
	 * @see #isUniPolarSettable
	 * @param channel
	 *            the ADC channel to set
	 * @param polarity
	 *            unipolar mode either true or false
	 * @throws DeviceException
	 */
	public void setUniPolar(int channel, boolean polarity) throws DeviceException;

	/**
	 * Get the supported ranges for a particular ADC.
	 * 
	 * @return the supported voltage ranges (units are implementation specific)
	 * @throws DeviceException
	 */
	public int[] getRanges() throws DeviceException;

	/**
	 * Get the polarity capability of the ADC.
	 * 
	 * @return true if UNIPOLAR is supported else assume BIPOLAR
	 * @throws DeviceException
	 */
	public boolean isUniPolarSettable() throws DeviceException;

	/**
	 * Set the sample count for the ADC monitoring.
	 * 
	 * @param count
	 *            the number of samples to read
	 * @throws DeviceException
	 */
	public void setSampleCount(int count) throws DeviceException;
}
