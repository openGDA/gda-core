/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.pes.api;

import java.util.List;

import gda.device.Device;
import gda.device.DeviceException;

public interface IElectronAnalyser extends Device {

	AnalyserEnergyRangeConfiguration getEnergyRange();

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
	 * Gets the available PSU modes (also known as element set)
	 * @return The list of available PSU modes
	 */
	List<String> getPsuModes();

	/**
	 * Sets the PSU mode (aka element set)
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	void setPsuMode(String psuMode) throws Exception;

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
	 * Gets the list of available lens modes
	 */

	List<String> getLensModes();

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
	Integer getPassEnergy() throws Exception;

	/**
	 * Sets the pass energy
	 *
	 * @param passEnergy The requested pass energy
	 * @throws Exception If the pass energy is invalid or if there is a problem with the EPICS communication
	 */
	void setPassEnergy(Integer passEnergy) throws Exception;

	/**
	 * Gets the available pass energies
	 */
	List<String> getPassEnergies();

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
	void startContinuous() throws Exception;

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
	 * Stops the analyser after the current iteration and saves the data.
	 *
	 * @see #stop()
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	void stopAfterCurrentIteration() throws Exception;

	/**
	 * Gets the currently set number of iterations
	 *
	 * @return The current number of iterations
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	int getIterations() throws Exception;

	/**
	 * Gets the completed number of iterations
	 * from the most recent scan
	 *
	 * @return The number of complete iterations from previous scan
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	int getCompletedIterations() throws Exception;

	/**
	 * Gets the current iteration number in the scan
	 *
	 * @return The current iteration number in the scan
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	int getCurrentIteration() throws Exception;

	/**
	 * Sets the number of iterations requested, not for use while in a scan
	 *
	 * @see #changeRequestedIterations(int)
	 * @param iterations The requested number of iterations
	 * @throws Exception If there is a problem with the EPICS communication
	 * @throws IllegalStateException If the analyser is performing a scan
	 */
	void setIterations(int iterations) throws Exception;

	/**
	 * Gets the step size
	 *
	 * @return step size
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public double getEnergyStep() throws Exception;

	/**
	 * Sets the step size
	 *
	 * @param stepSize The step size
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setEnergyStep(double stepSize) throws Exception;

	/**
	 * Gets the list of supported acquisition mode, using AcquisitionMode enum. Allows UIs to support different
	 * models of electron analyser which may name their modes differently
	 *
	 * @return The list of supported acquisition modes
	 */
	List<AcquisitionMode> getSupportedAcquisitionModes();

	/**
	 * Gets the spectrum data acquired from the most recent acquisition
	 *
	 * @return The spectrum data
	 * @throws Exception If there is a problem getting the spectrum
	 */
	double[] getSpectrum() throws Exception;

	/**
	 * Gets the image acquired during the most recent acquisition
	 *
	 * @return  The image data
	 * @throws Exception If there is a problem getting the image
	 */
	double[] getImage() throws Exception;

	/**
	 * Gets the value of the slices parameter
	 *
	 * @return The number of slices
	 * @throws Exception If there is a problem getting the slices
	 */
	int getSlices() throws Exception;

	/**
	 * Gets the value of the frames parameter
	 *
	 * @return The number of slices
	 * @throws Exception If there is a problem getting the frames
	 */
	int getFrames() throws Exception;

	/**
	 * Gets data from an external I/O device
	 *
	 * @param length The number of data elements to return
	 * @return The data
	 * @throws Exception If there is a problem getting the data
	 */
	double[] getExtIO(int length) throws Exception;

	/**
	 * Sets the acquisition mode
	 *
	 * @param acquisitionMode The name of the acquisition mode
	 * @throws Exception If there is a problem setting acquisition mode
	 */
	void setAcquisitionMode(String acquisitionMode) throws Exception;

	/**
	 * Sets the acquisition mode to the one specified and performs any configuration necessary
	 * for the chosen mode, such as setting up ROIs
	 *
	 * @param acquisitionMode The requested acquisition mode
	 * @throws Exception If there is a problem setting up the acquisition mode
	 */
	void setupAcquisitionMode(AcquisitionMode acquisitionMode) throws Exception;

	/**
	 * Starts the analyser acquiring data with the current parameters
	 *
	 * @throws Exception
	 */
	void startAcquiring() throws Exception;

	/**
	 * Gets the current state of the analyser
	 *
	 * @return The current analyser state
	 * @throws Exception If there is a problem getting the current state
	 */
	short getDetectorState() throws Exception;

	/**
	 * Sets the analyser to single image mode
	 *
	 * @throws Exception If there is a problem setting single image mode
	 */
	void setSingleImageMode() throws Exception;

	/**
	 * Gets the excitation energy
	 *
	 * @return The excitation energy
	 * @throws Exception If there is a problem getting the value
	 * @deprecated This was deprecated on the old VG Scienta RMI interface so it's deprecated here too
	 */
	@Deprecated(since="9.20")
	double getExcitationEnergy() throws Exception;

	/**
	 * Returns the maximum number of steps the analyser can perform in a swept scan.
	 * Suggest returning Integer.MAX_VALUE if your analyser doesn't have a limit.
	 *
	 * @return The maximum number of steps
	 */
	int getMaximumNumberOfSteps();

	/**
	 * Returns sensor size along X direction.
	 * Declared as default to not change all implementations.
	 *
	 * @return Sensor size along X direction
	 * @throws DeviceException
	 */
	@SuppressWarnings("unused")
	default int getSensorSizeX() throws DeviceException {
		return 1;
	}

	/**
	 * Returns sensor size along Y direction.
	 * Declared as default to not change all implementations.
	 *
	 * @return Sensor size along Y direction
	 * @throws DeviceException
	 */
	@SuppressWarnings("unused")
	default int getSensorSizeY() throws DeviceException {
		return 1;
	}
	/**
	 * Returns region size along X direction.
	 * Declared as default to not change all implementations.
	 *
	 * @return Region size along X direction
	 * @throws DeviceException
	 */

	@SuppressWarnings("unused")
	default int getRegionSizeX() throws DeviceException {
		return 1;
	}

	/**
	 * Returns region size along Y direction.
	 * Declared as default to not change all implementations.
	 *
	 * @return Region size along Y direction
	 * @throws DeviceException
	 */
	@SuppressWarnings("unused")
	default int getRegionSizeY() throws DeviceException {
		return 1;
	}
}
