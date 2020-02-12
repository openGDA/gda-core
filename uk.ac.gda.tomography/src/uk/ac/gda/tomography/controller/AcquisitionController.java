/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.controller;

import uk.ac.gda.tomography.model.Acquisition;
import uk.ac.gda.tomography.model.AcquisitionConfiguration;
import uk.ac.gda.tomography.model.AcquisitionParameters;

/**
 * A set of methods to load, save and delete {@link Acquisition}
 *
 * @param <T>
 * @author Maurizio Nagni
 */
public interface AcquisitionController<T extends Acquisition<? extends AcquisitionConfiguration<? extends AcquisitionParameters>>> {

	/**
	 * Returns the acquisition actually set in the controller
	 *
	 * @return an acquisition
	 */
	public T getAcquisition();

	/**
	 * Saves the acquisition actually set in the controller
	 *
	 * @throws AcquisitionControllerException
	 *             if the object cannot be saved
	 */
	void saveAcquisitionConfiguration() throws AcquisitionControllerException;

	/**
	 * Runs the controller acquisition associated {@link #getAcquisition()}
	 *
	 * @throws AcquisitionControllerException
	 */
	void runAcquisition() throws AcquisitionControllerException;

	/**
	 * Sets the controller active acquisition parsing a file
	 *
	 * @param filename
	 *            the name of the file to load
	 * @throws AcquisitionControllerException
	 *             if file is not found or readable
	 */
	public void loadAcquisitionConfiguration(String filename) throws AcquisitionControllerException;

	/**
	 * Deletes an acquisition file
	 *
	 * @param filename
	 *            the name of the file to delete
	 * @throws AcquisitionControllerException
	 *             if file is not found or readable
	 */
	public void deleteAcquisitionConfiguration(String filename) throws AcquisitionControllerException;
}
