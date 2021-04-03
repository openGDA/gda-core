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

package uk.ac.gda.api.acquisition;

import java.util.UUID;
import java.util.function.Supplier;

import uk.ac.gda.api.acquisition.configuration.AcquisitionConfiguration;
import uk.ac.gda.api.acquisition.parameters.AcquisitionParameters;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;

/**
 * Controls how create, load, save run an {@link Acquisition}
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
	T getAcquisition();

	/**
	 * Creates a new acquisition based on the function defined with {@link #createNewAcquisition()}. If the function is
	 * absent, returns an default instance of {@code T}
	 */
	void createNewAcquisition();

	/**
	 * Sets the controller function to create a new acquisition
	 *
	 * @param newAcquisitionSupplier
	 *            the provided function
	 */
	void setDefaultNewAcquisitionSupplier(Supplier<T> newAcquisitionSupplier);

	/**
	 * Saves the acquisition actually set in the controller
	 *
	 * @throws AcquisitionControllerException
	 *             if the object cannot be saved
	 */
	void saveAcquisitionConfiguration() throws AcquisitionControllerException;

	/**
	 * Runs the acquisition
	 * @return a response from the engine/service responsible to handle the request
	 * @throws AcquisitionControllerException
	 *             if the request fails
	 */
	RunAcquisitionResponse runAcquisition() throws AcquisitionControllerException;

	/**
	 * Sets the controller active acquisition using an existing instance
	 *
	 * @param acquisition
	 *            The acquisition to use
	 * @throws AcquisitionControllerException
	 *             If the acquisition cannot be used
	 */
	public void loadAcquisitionConfiguration(T acquisition) throws AcquisitionControllerException;

	/**
	 * Returns a document as {@link AcquisitionConfigurationResource}
	 *
	 * @param uuid The document id to retrieve
	 *
	 * @return an acquisition resource
	 * @throws AcquisitionControllerException
	 *             If the document is not found or cannot be parsed
	 */
	public AcquisitionConfigurationResource<T> createAcquisitionConfigurationResource(UUID uuid)
			throws AcquisitionControllerException;

	/**
	 * Deletes acquisition on its {@link Acquisition#getUuid()}
	 *
	 * @param uuid
	 *            the document id to delete
	 * @throws AcquisitionControllerException
	 */
	public void deleteAcquisitionConfiguration(UUID uuid) throws AcquisitionControllerException;

	/**
	 * Called before destroy the controller to release all the acquired resources or remove the used listeners
	 */
	public void releaseResources();
}
