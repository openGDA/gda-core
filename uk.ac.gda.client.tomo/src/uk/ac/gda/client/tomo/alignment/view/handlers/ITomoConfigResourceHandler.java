/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers;

import gda.device.DeviceException;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.edit.domain.EditingDomain;

import uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration;
import uk.ac.gda.tomography.parameters.TomoExperiment;

/**
 *
 */
public interface ITomoConfigResourceHandler extends ITomoHandler {

	/**
	 * Request to save the configuration
	 * 
	 * @param monitor
	 * @param saveableConfiguration
	 * @throws DeviceException
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 * @return id of the experiment configuration
	 * @throws Exception 
	 */
	String saveConfiguration(IProgressMonitor monitor, SaveableConfiguration saveableConfiguration)
			throws DeviceException, InvocationTargetException, InterruptedException, Exception;

	/**
	 * @param monitor
	 * @param shouldCreate
	 * @return the config resource
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 * @throws Exception 
	 */
	TomoExperiment getTomoConfigResource(IProgressMonitor monitor, boolean shouldCreate)
			throws InvocationTargetException, InterruptedException, Exception;

	/**
	 * @return the tomography parameters editing domain - this should be used to extract resourceset
	 * @throws Exception 
	 */
	EditingDomain getEditingDomain() throws Exception;

}
