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

import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;

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

	/**
	 * @return horizontal field of view units
	 * @throws DeviceException 
	 */
	String lookupHFOVUnit() throws DeviceException;
	/**
	 * @return horizontal field of view units
	 * @throws DeviceException 
	 */
	String lookupMagnificationUnit() throws DeviceException;
	/**
	 * @param module
	 * @return value of the object pixel size for the given module
	 */
	Double lookupObjectPixelSize(CAMERA_MODULE module) throws DeviceException;

	/**
	 * @return object pixel size units
	 * @throws DeviceException 
	 */
	String lookupObjectPixelSizeUnits() throws DeviceException;

	/**
	 * @param module
	 * @return horizontal field of view for the given module
	 * @throws DeviceException 
	 */
	Double lookupHFOV(CAMERA_MODULE module) throws DeviceException;
	/**
	 * @param module
	 * @return horizontal field of view for the given module
	 * @throws DeviceException 
	 */
	Double lookupMagnification(CAMERA_MODULE module) throws DeviceException;

	/**
	 * @param newModule
	 * @return default exposure time for a given module
	 * @throws DeviceException 
	 */
	Double lookupDefaultExposureTime(CAMERA_MODULE newModule) throws DeviceException;

	/**
	 * @param cameraModule
	 * @return the object pixel size in milli-meters
	 */
	Double getObjectPixelSizeInMM(CAMERA_MODULE cameraModule);

}
