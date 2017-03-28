/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.vgscienta;

import gda.device.Device;

/**
 * This is intended to be the interface for the clients to interact with the analyser over RMI.
 *
 * The intension is to expand this interface over time, for now it just allows access to the energy range for validating KE in the GUI.
 *
 * @author James Mudd
 */
public interface IVGScientaAnalyserRMI extends Device {

	VGScientaAnalyserEnergyRange getEnergyRange();

	/**
	 * This is the energy covered by one pixel in pass energy = 1 in meV
	 * <p>
	 * To find the energy step per pixel this value should be multiplied by the pass energy. To find the fixed mode energy width this value should be multiplied
	 * by the pass energy and the number of energy channels.
	 * <p>
	 * This value should <b>not</b> be used to calculate energy scales.
	 */
	double getEnergyStepPerPixel();

	/**
	 * This is the fall-back maximum kinetic energy (KE) if the energyRange object can't provide a correct energy range
	 */
	double getMaxKE();

	/**
	 * This gets the number of energy channels in the fixed mode region, to allow the fixed mode energy width to be calculated.
	 *
	 * @return The number of energy channels in fixed mode
	 */
	int getFixedModeEnergyChannels();

	/**
	 * This gets the number of energy channels in the swept mode region, to allow the swept mode energy steps including pre-scan to be calculated
	 *
	 * @return The number of energy channels in swept mode
	 */
	int getSweptModeEnergyChannels();

	/**
	 * Gets the current PSU mode. (Also known as element set)
	 *
	 * @return The current power supply mode
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	String getPsuMode() throws Exception;

	/**
	 * Gets the current lens mode.
	 *
	 * @return The current lens mode
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	String getLensMode() throws Exception;

	/**
	 * This can be called to change the number of iterations scheduled during a scan.
	 *
	 * @param newScheduledIterations
	 */
	void changeRequestedIterations(int newScheduledIterations);

}
