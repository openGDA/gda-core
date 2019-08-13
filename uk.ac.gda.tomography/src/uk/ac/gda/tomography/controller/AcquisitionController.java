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

import java.net.URL;

import org.eclipse.jface.dialogs.IDialogSettings;

import uk.ac.gda.tomography.model.Acquisition;
import uk.ac.gda.tomography.model.AcquisitionConfiguration;
import uk.ac.gda.tomography.model.AcquisitionParameters;

/**
 * A set of methods to load and save {@link AcquisitionConfiguration}
 *
 * @param <T>
 * @author Maurizio Nagni
 */
public interface AcquisitionController<T extends Acquisition<? extends AcquisitionConfiguration<? extends AcquisitionParameters>>> {

	public T getAcquisition();

	/**
	 * Saves the tomography scan parameters object associated with this controller
	 *
	 * @throws AcquisitionControllerException
	 *             if the object cannot be saved
	 */
	void saveAcquisitionAsFile(T acquisition, URL destination) throws AcquisitionControllerException;
	void saveAcquisitionAsIDialogSettings(T acquisition, IDialogSettings destination, String key) throws AcquisitionControllerException;
	void runAcquisition(T acquisition) throws AcquisitionControllerException;

	public void loadData(T data) throws AcquisitionControllerException;
	public void loadData(URL data) throws AcquisitionControllerException;
	public void loadData(String data) throws AcquisitionControllerException;
	public void loadData(IDialogSettings dialogSettings, String key) throws AcquisitionControllerException;

	/**
	 * Deletes the tomography scan parameters object associated with this controller
	 *
	 * @throws AcquisitionControllerException
	 *             if the object cannot be saved
	 */
	void deleteAcquisition(T acquisition) throws AcquisitionControllerException;
}
