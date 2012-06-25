/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;

/**
 *
 */
public interface ICameraModuleController extends ITomoHandler{

	/**
	 * @return the {@link CAMERA_MODULE} that the camera is currently in.
	 * @throws DeviceException
	 */
	CAMERA_MODULE getModule() throws DeviceException;

	/**
	 * @param module
	 *            - the module positions the camera should move into
	 * @param monitor
	 *            - the progress monitor on which operation is running
	 * @throws Exception
	 */
	void moveModuleTo(CAMERA_MODULE module, IProgressMonitor monitor) throws Exception;

	/**
	 * Invoked when the progress monitor is stopped, or any other interruption.
	 * 
	 * @throws DeviceException
	 */
	void stopModuleChange() throws DeviceException;

}
