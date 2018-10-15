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
 * An interface for a distributed PhotoEmission Electron Microscope (PEEM) end-station
 */
public interface PEEM extends Device {

	/**
	 * Connect to the PEEM.
	 * 
	 * @return the connection status true if connected
	 * @throws DeviceException
	 */
	public boolean connect() throws DeviceException;

	/**
	 * Disconnect from the PEEM
	 * 
	 * @return boolean the disconnection status true if disconnected false if already broken
	 * @throws DeviceException
	 */
	public boolean disconnect() throws DeviceException;

	/**
	 * Returns a display list of PEEM modules and info as a single String including header
	 * 
	 * @return String list of module info
	 * @throws DeviceException
	 */
	public String modules() throws DeviceException;

	/**
	 * Get the module index for the named PEEM module
	 * 
	 * @param moduleName
	 * @return the module index
	 * @throws DeviceException
	 */
	public int getModuleIndex(String moduleName) throws DeviceException;

	/**
	 * Get the PEEM module number
	 * 
	 * @return int = the module number
	 * @throws DeviceException
	 */
	public int getModuleNumber() throws DeviceException;

	/**
	 * Get the PSname
	 * 
	 * @param index
	 * @return the PSname
	 * @throws DeviceException
	 */
	public String getPSName(int index) throws DeviceException;

	/**
	 * Get the PS value
	 * 
	 * @param index
	 * @return double the PSvalue
	 * @throws DeviceException
	 */
	public double getPSValue(int index) throws DeviceException;

	/**
	 * Set the PSvalue
	 * 
	 * @param index
	 *            the index of the module to set
	 * @param value
	 *            double = the value of PSvalue
	 * @return boolean true = success else fail
	 * @throws DeviceException
	 */
	public boolean setPSValue(int index, double value) throws DeviceException;

	/**
	 * Get Preset
	 * 
	 * @return String value of Preset
	 * @throws DeviceException
	 */
	public String getPreset() throws DeviceException;

	/**
	 * Set Phi
	 * 
	 * @param angle
	 *            double
	 * @return int
	 * @throws DeviceException
	 */
	public int setPhi(double angle) throws DeviceException;

	/**
	 * Get Micrometer value
	 * 
	 * @return double micrometer value
	 * @throws DeviceException
	 */
	public double[] getMicrometerValue() throws DeviceException;

	/**
	 * Get VacuumGaugeValue
	 * 
	 * @return double vacuumGaugeValue
	 * @throws DeviceException
	 */
	public double getVacuumGaugeValue() throws DeviceException;

	/**
	 * Get VacuumGaugeLabel
	 * 
	 * @return String vacuumGaugeLabel
	 * @throws DeviceException
	 */
	public String getVacuumGaugeLabel() throws DeviceException;

	/**
	 * Get whether or not initialization of PEEM is done
	 * 
	 * @return boolean true if initialization done else false
	 * @throws DeviceException
	 */
	public boolean isInitDone() throws DeviceException;
}
