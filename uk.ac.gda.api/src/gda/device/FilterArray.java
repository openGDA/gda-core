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
 * Interface to control an array of filters for beam attenuation, for example EpicsFilterArray.
 */
public interface FilterArray extends Device {
	/**
	 * Returns the current absorption level. This value is calculated from the filters currently in place and the energy
	 * this device is set to work with. The value is not necessarily the value given to the object via the setAbsorption
	 * method.
	 * 
	 * @return a double between 0 and 1
	 * @throws DeviceException
	 */
	public double getAbsorption() throws DeviceException;

	/**
	 * Tells the filter array to move so that, for the current energy used in its calculations, the absorption is as
	 * close as possible to the value supplied
	 * 
	 * @param absorption -
	 *            a double between 0 and 1
	 * @throws DeviceException
	 */
	public void setAbsorption(double absorption) throws DeviceException;

	/**
	 * Returns the current transmission level. This value is calculated from the filters currently in place and the
	 * energy this device is set to work with. The value is not necessarily the value given to the object via the
	 * setTransmission method.
	 * 
	 * @return the current transmission level
	 * @throws DeviceException
	 */
	public double getTransmission() throws DeviceException;

	/**
	 * Tells the filter array to move so that, for the current energy used in its calculations, the transmission is as
	 * close as possible to the value supplied.
	 * 
	 * @param transmission
	 *            the transmission value
	 * @throws DeviceException
	 */
	public void setTransmission(double transmission) throws DeviceException;

	/**
	 * Returns the energy value currently being used to calculate the absorption and transmission.
	 * 
	 * @return the energy value
	 * @throws DeviceException
	 */
	public double getCalculationEnergy() throws DeviceException;

	/**
	 * Sets the energy value which would be used to calculate the absorption and transmission if isUsingMonoEnergy
	 * return false.
	 * 
	 * @param energy
	 *            the energy value which would be used to calculate the absorption and transmission
	 * @throws DeviceException
	 */
	public void setCalculationEnergy(double energy) throws DeviceException;

	/**
	 * Returns the wavelength value currently being used to calculate the absorption and transmission.
	 * 
	 * @return the wavelength
	 * @throws DeviceException
	 */
	public double getCalculationWavelength() throws DeviceException;

	/**
	 * Sets the wavelength value which would be used to calculate the absorption and transmission if isUsingMonoEnergy
	 * return false.
	 * 
	 * @param wavelength
	 *            the wavelength value which would be used to calculate the absorption and transmission
	 * @throws DeviceException
	 */
	public void setCalculationWavelength(double wavelength) throws DeviceException;

	/**
	 * Returns true if a monochromator's setting is being used in the calculations rather than the energy supplied via
	 * the setCalculationEnergy method.
	 * 
	 * @return true if a monochromator's setting is being used in the calculations
	 * @throws DeviceException
	 */
	public boolean isUsingMonoEnergy() throws DeviceException;

	/**
	 * Tells the filter array to perform calculations using the current setting of a monochromator rather than a value
	 * supplied via its setCalculationEnergy method.
	 * <p>
	 * If the monochromator changes energy and this method is set to true, then the filter array is NOT expected to
	 * automatically adjust until another call of either setTransmission or setAbsorption.
	 * 
	 * @param useEnergy
	 *            perform calculations using the current setting of a monochromator
	 * @throws DeviceException
	 */
	public void setUseMonoEnergy(boolean useEnergy) throws DeviceException;
}
