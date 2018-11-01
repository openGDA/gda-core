/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.device.evaporator;

import gda.device.DeviceException;

/**
 * Interface exposing control of individual pockets in a multi-pocket evaporator.
 *
 * @see Evaporator
 * @see EvaporatorController
 */
public interface EvaporatorPocket {
	public enum Regulation {CURRENT, EMISSION, FLUX; }

	/**
	 * Set the label/name of this pocket
	 *
	 * @param label can be any arbitrary string
	 * @throws DeviceException if the device is unreachable
	 */
	void setLabel(String label) throws DeviceException;

	/** Get the label/name of this pocket */
	String getLabel() throws DeviceException;

	/**
	 * Set the regulation mode of this pocket.
	 *
	 * @param mode should be one of {@literal Regulation#values()}
	 * @throws DeviceException if the device is unreachable
	 */
	void setRegulation(String mode) throws DeviceException;

	/** Get the current regulation mode */
	String getRegulation() throws DeviceException;

	/**
	 * Set the current used for current based regulation. Has no effect until regulation is
	 * set to 'Current'
	 *
	 * @param current The current to use for regulation
	 * @throws DeviceException if the device is unreachable
	 */
	void setCurrent(double current) throws DeviceException;

	/** Get the current to be used for current based regulation */
	double getCurrent() throws DeviceException;

	/**
	 * Set the emission used for emission based regulation. Has no effect until regulation is
	 * set to 'Emission'
	 *
	 * @param emission The emission to use for regulation
	 * @throws DeviceException if the device is unreachable
	 */
	void setEmission(double emission) throws DeviceException;

	/** Get the emission used for emission based regulation */
	double getEmission() throws DeviceException;

	/**
	 * Set the flux used for flux based regulation. Has no effect until regulation is
	 * set to 'Flux'
	 *
	 * @param flux The flux to use for regulation
	 * @throws DeviceException if the device is unreachable
	 */
	void setFlux(double flux) throws DeviceException;

	/** Get the flux used for flux based regulation */
	double getFlux() throws DeviceException;
}
