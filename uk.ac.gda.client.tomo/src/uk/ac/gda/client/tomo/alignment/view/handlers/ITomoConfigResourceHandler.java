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

import java.lang.reflect.InvocationTargetException;

import gda.device.DeviceException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;

import uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration;

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
	 */
	void saveConfiguration(IProgressMonitor monitor, SaveableConfiguration saveableConfiguration)
			throws DeviceException, InvocationTargetException, InterruptedException;

	/**
	 * @param monitor
	 * @param shouldCreate
	 * @return the config resource
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 */
	Resource getTomoConfigResource(IProgressMonitor monitor, boolean shouldCreate) throws InvocationTargetException, InterruptedException;
}
