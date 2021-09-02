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

package uk.ac.gda.ui.tool.controller;

import java.util.UUID;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.gda.api.acquisition.Acquisition;
import uk.ac.gda.api.acquisition.configuration.AcquisitionConfiguration;
import uk.ac.gda.api.acquisition.parameters.AcquisitionParameters;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;
import uk.ac.gda.client.exception.AcquisitionControllerException;
import uk.ac.gda.client.properties.acquisition.AcquisitionKeys;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.ui.tool.document.DocumentFactory;
import uk.ac.gda.ui.tool.selectable.NamedCompositeFactory;

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
	 * Creates a new acquisition document then available through {@link #getAcquisition()}
	 *
	 * <p>
	 * Requiring the controller to create a new acquisition, the requester is preparing the controller for a specific acquisition type.
	 * While, earlier implementation, this action was delegated to specific gui component, with the adoption of the {@link DocumentFactory}
	 * is it possible for the controller to take this responsibility and leave to the gui components,
	 * typically classes implementing {@link NamedCompositeFactory} to specify only the [{@link AcquisitionPropertyType}, {@link AcquisitionTemplateType}] pair.
	 * </p>
	 *
	 * @param acquisitionKey
	 */
	void newScanningAcquisition(AcquisitionKeys acquisitionKey) throws AcquisitionControllerException;

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

	/**
	 * The actual controller acquisition keys.
	 *
	 * @return the acquisition keys, otherwise {@code (AcquisitionPropertyType.DEFAULT, AcquisitionTemplateType.STATIC_POINT)} if {@code #getAcquisition()} {@code null}
	 */
	public AcquisitionKeys getAcquisitionKeys();
}
