/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.beamline;

import gda.device.DeviceException;
import gda.factory.Findable;
import gda.observable.IObservable;

import java.io.Serializable;

/**
 *
 */
public interface BeamInfo extends Findable, IObservable, Serializable {

	/**
	 * sets the photon beam energy to the specified value and adds it to metadata list. Note this method is used by PBS
	 * to set beam energy parameter after energy calibration (which may be different from the DCM energy value), it does
	 * not actually change the DCM energy.
	 * 
	 * @param energy
	 */
	public abstract void setEnergy(double energy);

	/**
	 * gets the photon beam energy
	 * 
	 * @return the photon beam energy
	 */
	public abstract double getEnergy();

	/**
	 * sets the photon beam wavelength to the specified value and adds it to metadata list. Note this method is used by
	 * PBS to set beam wavelength parameter after wavelength calibration (which may be different from the DCM energy
	 * value), it does not actually change the DCM energy.
	 * 
	 * @param wavelength
	 */
	public abstract void setWavelength(double wavelength);

	/**
	 * sets the photon beam energy to the value of DCM energy from EPICS and adds it to metadata list
	 * 
	 * @throws DeviceException
	 */
	public abstract void setEnergy() throws DeviceException;

	/**
	 * gets the photon beam wavelength
	 * 
	 * @return photon beam wavelength
	 */
	public abstract double getWavelength();

	/**
	 * check if the wavelength/energy value is calibrated value or not.
	 * 
	 * @return boolean
	 */
	public abstract boolean isCalibrated();
}