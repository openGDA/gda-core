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

import gda.device.attenuator.ClosestMatchTransmission;
import gda.factory.Configurable;
import gda.factory.Findable;

/**
 * Interface for a distributed object which operates an array of filters which attenuate the beam. All methods take
 * and return transmissions as decimal values (0 to 1), not percentages (0 to 100).
 */
public interface Attenuator extends Findable, Configurable {

	/**
	 * Changes the attenuation level to the closest achievable transmission to the given value. This uses the current
	 * beam energy in its calculations.
	 *
	 * @param transmission the desired transmission
	 * @return the actual transmission value achieved
	 */
	double setTransmission(double transmission) throws DeviceException;

	/**
	 * The current transmission, assuming the energy has not changed since the last filter adjustment.
	 *
	 * @return the current actual transmission
	 */
	double getTransmission() throws DeviceException;

	/**
	 * Returns the stored desired transmission value.
	 *
	 * @return the stored desired transmission value
	 */
	double getDesiredTransmission() throws DeviceException;

	/**
	 * @return double - the desired energy to use in calculations to determine the filter set to achieve the desired
	 *         transmission
	 */
	double getDesiredEnergy() throws DeviceException;

	/**
	 * @return double - the actual energy used in calculations to determine the filter set to achieve the desired
	 *         transmission
	 */
	double getClosestMatchEnergy() throws DeviceException;

	/**
	 * Using the current beam energy, calculates the closest achievable transmission value to the given value with the
	 * set of filters
	 *
	 * @param transmission
	 * @return double the closest transmission value that could be achieved
	 */
	double getClosestMatchTransmission(double transmission) throws DeviceException;

	/**
	 * Uses the given energy in the calculation
	 *
	 * @param transmission transmission (0 to 1)
	 * @param energyInKeV x-ray energy in the stated units
	 */
	ClosestMatchTransmission getClosestMatchTransmission(double transmission, double energyInKeV) throws DeviceException;

	/**
	 * Determines if correct filters are in place (as attenuator makes a change of transmission):
	 * Compares the results of {@link #getFilterPositions} with {@link #getDesiredFilterPositions}.
	 */
	boolean isReady() throws DeviceException;


	/**
	 * Returns array (indexing over filter elements) answering the question:
	 * <br><b>"Is filter (presently) in the beam?"</b>.</br>
	 */
	boolean[] getFilterPositions() throws DeviceException;

	/**
	 * Returns array (indexing over filter elements) answering the question:
	 * <br><b>"Is this filter required to be in the beam (to achieve the closest match transmission)?"</b>,</br>
	 * <br>as per the requested transmission most recently supplied to the {@link #getClosestMatchTransmission} method.</br>
	 */
	boolean[] getDesiredFilterPositions() throws DeviceException;

	/**
	 * Counts the filters available
	 * @return the number of filters in this attenuator
	 */
	int getNumberFilters() throws DeviceException;

	/**
	 * Names available filters
	 * @return the names of the filters in this attenuator
	 */
	String[] getFilterNames() throws DeviceException;

}
