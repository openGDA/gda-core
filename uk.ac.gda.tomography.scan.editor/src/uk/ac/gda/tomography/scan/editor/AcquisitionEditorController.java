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

package uk.ac.gda.tomography.scan.editor;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Display;

import uk.ac.gda.tomography.controller.AcquisitionController;
import uk.ac.gda.tomography.controller.AcquisitionControllerException;
import uk.ac.gda.tomography.model.Acquisition;
import uk.ac.gda.tomography.model.AcquisitionConfiguration;

/**
 * Describes the operations to control a tomography related element.
 * @param <T>
 *
 * @author Maurizio Nagni
 */
public interface AcquisitionEditorController<T extends Acquisition<? extends AcquisitionConfiguration>> extends AcquisitionController<T> {

	/**
	 * Shows the tomography configuration dialog
	 * @param display
	 * @return the dialog return code
	 */
	int showConfigurationDialog(Display display);

	/**
	 * Loads the tomography scan parameters from a file
	 * @param data a json document file
	 * @throws AcquisitionControllerException if the object cannot be loaded or assigned	 *
	 */
	void loadData(File data) throws AcquisitionControllerException;

	/**
	 * Loads the tomography scan parameters as jsonDocument
	 * @param data a json document string
	 * @throws AcquisitionControllerException if the object cannot be loaded or assigned
	 */
	void loadData(String data) throws AcquisitionControllerException;

	/**
	 * Loads the tomography scan parameters from a {@link IDialogSettings} key/value element.
	 * @param dialogSettings an instance defining key/value for load tomography scan parameters
	 * @throws AcquisitionControllerException if the object cannot be loaded or assigned
	 */
	void loadData(IDialogSettings dialogSettings) throws AcquisitionControllerException;

	/**
	 * Creates a new tomography scan parameters and assigns it to this controller
	 */
	void createNewData() throws AcquisitionControllerException;

	/** Execute a tomography acquisition
	 */
	void runAcquisition() throws AcquisitionControllerException;

	/**
	 * Takes a flat image
	 * @return the acquired image as file
	 */
	Path takeFlatImage() throws AcquisitionControllerException;

	/**
	 * Takes a dark image
	 * @return the acquired image as file
	 */
	Path takeDarkImage() throws AcquisitionControllerException;
}