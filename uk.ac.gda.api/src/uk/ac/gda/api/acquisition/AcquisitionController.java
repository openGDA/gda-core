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

import java.net.URL;

import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;

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
	 * Runs the acquisition storing the result in the given {@code outputPath}
	 *
	 * @param outputPath where acquisition will be stored
	 * @throws AcquisitionControllerException if {@code outputPath} is {@code null}
	 */
	void runAcquisition(URL outputPath) throws AcquisitionControllerException;

	/**
	 * Sets the controller active acquisition parsing a file
	 *
	 * @param url
	 *            The location of the file to parse
	 * @throws AcquisitionControllerException
	 *             If the file is not found or cannot be parsed
	 */
	public void loadAcquisitionConfiguration(URL url) throws AcquisitionControllerException;

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
	 * Parse only an acquisition file. Similar to {@link #loadAcquisitionConfiguration(URL)} but does not set the parsed
	 * acquisition as the active one.
	 *
	 * @param url
	 *            The location of the file to parse
	 * @return an acquisition resource
	 * @throws AcquisitionControllerException
	 *             If the file is not found or cannot be parsed
	 */
	public AcquisitionConfigurationResource<T> parseAcquisitionConfiguration(URL url) throws AcquisitionControllerException;

	/**
	 * Sets the controller active acquisition using an {@link AcquisitionConfigurationResource} with the following
	 * priorities
	 * <ol>
	 * <li>If {@link AcquisitionConfigurationResource#getResource()} is not {@code null}, calls
	 * {@link #loadAcquisitionConfiguration(AcquisitionConfigurationResource)}</li>
	 * <li>If {@link AcquisitionConfigurationResource#getLocation()} is not {@code null}, calls
	 * {@link #loadAcquisitionConfiguration(URL)}</li>
	 * </ol>
	 *
	 * @param resource
	 *            the resource object
	 * @throws AcquisitionControllerException
	 *             If the resource cannot be used
	 */
	default void loadAcquisitionConfiguration(AcquisitionConfigurationResource<T> resource)
			throws AcquisitionControllerException {
		if (resource.getResource() == null) {
			loadAcquisitionConfiguration(resource.getLocation());
		} else {
			loadAcquisitionConfiguration(resource.getResource());
		}
	}

	/**
	 * Deletes an acquisition on a specified {@link URL}
	 *
	 * @param url
	 *            the configuration location
	 * @throws AcquisitionControllerException
	 */
	public void deleteAcquisitionConfiguration(URL url) throws AcquisitionControllerException;
}
