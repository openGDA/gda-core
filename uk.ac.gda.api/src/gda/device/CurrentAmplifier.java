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
 * Interface for EPICS single channel current amplifier.
 */

public interface CurrentAmplifier extends Scannable {
	/**
	 * defines the overload status of the amplifier
	 * 
	 */
	public enum Status {
		/**
		 * amplifier is in working order
		 */
		NORMAL,
		/**
		 * amplifier is overloaded
		 */
		OVERLOAD;
		/**
		 * converts value to Status
		 * 
		 * @param value
		 * @return status
		 */
		public static Status from_int(int value) {
			Status s = Status.NORMAL;
			switch (value) {
			case 0:
				s = Status.NORMAL;
				break;
			case 1:
				s = Status.OVERLOAD;
				break;
			default:
				s = Status.NORMAL;
			}
			return s;

		}
	}

	/**
	 * Returns an array of all possible gain positions which this device can be moved to.
	 * 
	 * @return an array of gain positions
	 * @throws DeviceException
	 */
	public String[] getGainPositions() throws DeviceException;

	/**
	 * Returns an array of all possible gain units which this device can be moved to.
	 * 
	 * @return an array of gain units
	 * @throws DeviceException
	 */
	public String[] getGainUnits() throws DeviceException;

	/**
	 * Returns an array of all possible mode positions which this device can be set to.
	 * 
	 * @return an array of mode positions
	 * @throws DeviceException
	 */
	public String[] getModePositions() throws DeviceException;

	/**
	 * Moves amplifier's gain to the named position.
	 * 
	 * @param position
	 * @throws DeviceException
	 */
	public void setGain(String position) throws DeviceException;

	/**
	 * returns the current gain position
	 * 
	 * @return gain position
	 * @throws DeviceException
	 */
	public String getGain() throws DeviceException;

	/**
	 * Moves amplifier's gain unit to the named unit.
	 * 
	 * @param unit
	 * @throws DeviceException
	 */
	public void setGainUnit(String unit) throws DeviceException;

	/**
	 * returns the current gain unit
	 * 
	 * @return gain unit
	 * @throws DeviceException
	 */
	public String getGainUnit() throws DeviceException;

	/**
	 * returns the current value
	 * 
	 * @return current
	 * @throws DeviceException
	 */
	public double getCurrent() throws DeviceException;

	/**
	 * sets the amplifier's mode of operation
	 * 
	 * @param mode
	 * @throws DeviceException
	 */
	public void setMode(String mode) throws DeviceException;

	/**
	 * gets the amplifier's mode of operation
	 * 
	 * @return mode
	 * @throws DeviceException
	 */
	public String getMode() throws DeviceException;

	/**
	 * Returns the overload status
	 * 
	 * @return the status
	 * @throws DeviceException
	 */
	public Status getStatus() throws DeviceException;

	/**
	 * returns a parsed list of gains available for this amplifier.
	 * 
	 * @throws DeviceException
	 */
	public void listGains() throws DeviceException;

}
