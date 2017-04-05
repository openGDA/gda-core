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
import gda.device.DeviceException;

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
	 * Sets the lens mode
	 *
	 * @param lensMode The lens mode to set
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	void setLensMode(String lensMode) throws Exception;

	/**
	 * This can be called to change the number of iterations scheduled during a scan.
	 *
	 * @param newScheduledIterations
	 */
	void changeRequestedIterations(int newScheduledIterations);

	/**
	 * Get the energy axis for the current acquisition
	 *
	 * @return The energy axis
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	double[] getEnergyAxis() throws Exception;

	/**
	 * Gets the Y axis (usually angle) for the current acquisition
	 *
	 * @return The angle axis
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	double[] getAngleAxis() throws Exception;

	/**
	 * Get the current pass energy
	 *
	 * @return The current pass energy
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	// TODO This should probably be changed to double to become consistent with SPECS analysers
	Integer getPassEnergy() throws Exception;

	/**
	 * Sets the pass energy
	 *
	 * @param passEnergy The requested pass energy
	 * @throws Exception If the pass energy is invalid or if there is a problem with the EPICS communication
	 */
	// TODO This should probably be changed to double to become consistent with SPECS analysers
	void setPassEnergy(Integer passEnergy) throws Exception;

	/**
	 * Gets the current centre energy
	 *
	 * @return The current centre energy
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	Double getCentreEnergy() throws Exception;

	/**
	 * Sets the centre energy
	 *
	 * @param centreEnergy The requested centre energy
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	void setCentreEnergy(Double centreEnergy) throws Exception;

	/**
	 * Gets the collection time per point
	 *
	 * @return The collection time
	 * @throws DeviceException If there is a problem with the EPICS communication
	 */
	double getCollectionTime() throws DeviceException;

	/**
	 * Sets the collection time per point
	 *
	 * @throws DeviceException If there is a problem with the EPICS communication
	 */
	void setCollectionTime(double collectionTime) throws DeviceException;

	/**
	 * Starts the analyser acquiring in continuous mode, this is intended for use in alignment. This is non blocking
	 *
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	void startContinuious() throws Exception;

	/**
	 * Stops the analyser acquiring <b>Does NOT zero supplies</b>
	 *
	 * @see #zeroSupplies()
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	void stop() throws Exception;

	/**
	 * Stops the analyser and zeros the HV supplies. This puts the analyser into a safe state.
	 *
	 * @see #stop()
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	void zeroSupplies() throws Exception;

	/**
	 * Gets the currently set number of iterations
	 *
	 * @return The current number of iterations
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	int getIterations() throws Exception;

	/**
	 * Sets the number of iterations requested, not for use while in a scan
	 *
	 * @see #changeRequestedIterations(int)
	 * @param iterations The requested number of iterations
	 * @throws Exception If there is a problem with the EPICS communication
	 * @throws IllegalStateException If the analyser is performing a scan
	 */
	void setIterations(int iterations) throws Exception;

}
