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

import uk.ac.gda.tomography.model.ITomographyScanParameters;

/**
 * A set of methods to load and save {@link ITomographyScanParameters}
 *
 * @param <T>
 *
 * @author Maurizio Nagni
 */
public interface ITomographyConfigurationController<T extends ITomographyScanParameters> {

	/**
	 * @return the tomography scan parameters object associated with this controller, otherwise <code>null</code>
	 */
	T getData();

	/**
	 * Saves the tomography scan parameters object associated with this controller
	 * @throws TomographyControllerException if the object cannot be saved
	 */
	void saveData() throws TomographyControllerException;
}
