/*
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

package uk.ac.diamond.daq.devices.mbs.api;

import java.util.List;

import gda.device.DeviceException;
import uk.ac.diamond.daq.pes.api.IElectronAnalyser;

public interface IMbsAnalyser extends IElectronAnalyser {

	/**
	 * Gets the acquisition period
	 *
	 * @return The acquisition period
	 * @throws DeviceException If there is a problem with communication
	 */
	double getAcquirePeriod() throws DeviceException;

	/**
	 * Sets the acquisition period
	 *
	 * @throws DeviceException If there is a problem with communication
	 */
	void setAcquirePeriod(double acquirePeriod) throws DeviceException;


	/**
	 * Gets the list of available acquisition modes
	 *
	 * @return the list of available acquisition modes
	 */
	public List<String> getAcquisitionModes();

	/**
	 * Gets the current acquisition mode
	 *
	 * @return The current acquisition mode
	 * @throws DeviceException If there is a problem with communication
	 */
	public String getAcquisitionMode() throws DeviceException;


	/**
	 * Gets the start energy
	 *
	 * @return The start energy
	 * @throws DeviceException If there is a problem with communication
	 */
	public double getStartEnergy() throws DeviceException;

	/**
	 * Sets the start energy
	 *
	 * @param startEnergy The start energy
	 * @throws DeviceException If there is a problem with communication
	 */
	public void setStartEnergy(double startEnergy) throws DeviceException;

	/**
	 * Gets the end energy
	 *
	 * @return The end energy
	 * @throws DeviceException If there is a problem with communication
	 */
	public double getEndEnergy() throws DeviceException;

	/**
	 * Sets the end energy
	 *
	 * @param endEnergy The end energy
	 * @throws DeviceException If there is a problem with communication
	 */
	public void setEndEnergy(double endEnergy) throws DeviceException;


	/**
	 * Gets the energy width
	 * @return The energy width
	 * @throws DeviceException
	 */
	public double getEnergyWidth() throws DeviceException;

	/**
	 * Gets the deflector X value
	 *
	 * @return The deflector X value
	 * @throws DeviceException If there is a problem with communication
	 */
	public double getDeflectorX() throws DeviceException;

	/**
	 * Sets the deflector X value
	 *
	 * @param deflectorX The deflector X value
	 * @throws DeviceException If there is a problem with communication
	 */
	public void setDeflectorX(double deflectorX) throws DeviceException;

	/**
	 * Gets the deflector Y value
	 *
	 * @return The deflector Y value
	 * @throws DeviceException If there is a problem with communication
	 */
	public double getDeflectorY() throws DeviceException;

	/**
	 * Sets the deflector Y value
	 *
	 * @param deflectorY The deflector Y value
	 * @throws DeviceException If there is a problem with communication
	 */
	public void setDeflectorY(double deflectorY) throws DeviceException;


	/**
	 * Sets the number of slices.
	 *
	 * @param slices The number of slices
	 * @throws DeviceException If there is a problem with communication
	 */
	public void setSlices(int slices) throws DeviceException;

	/**
	 * Gets the number of steps
	 *
	 * @return The number of steps
	 * @throws DeviceException If there is a problem with communication
	 */
	public int getNumberOfSteps() throws DeviceException;

	/**
	 * Sets the number of steps
	 *
	 * @param steps The number of steps
	 * @throws DeviceException If there is a problem with communication
	 */
	public void setNumberOfSteps(int steps) throws DeviceException;

	/**
	 * Gets the number of dither steps
	 *
	 * @return The number of dither steps
	 * @throws DeviceException If there is a problem with communication
	 */
	public int getNumberOfDitherSteps() throws DeviceException;

	/**
	 * Sets the number of dither steps
	 *
	 * @param ditherSteps The number of dither steps
	 * @throws DeviceException If there is a problem with communication
	 */
	public void setNumberOfDitherSteps(int ditherSteps) throws DeviceException;

	/**
	 * Gets the spin offset
	 *
	 * @return The spin offset
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public double getSpinOffset() throws DeviceException;

	/**
	 * Sets the spin offset
	 *
	 * @param spinOffset The spin offset
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setSpinOffset(double spinOffset) throws DeviceException;

	/**
	 * Gets the step size
	 *
	 * @return step size
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public double getStepSize() throws DeviceException;

	/**
	 * Sets the step size
	 *
	 * @param stepSize The step size
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setStepSize(double stepSize) throws DeviceException;

	public double[][] get2DImageArray() throws DeviceException;
}
