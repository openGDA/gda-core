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

package uk.ac.gda.client.tomo.alignment.view.handlers.simulator;

import gda.device.DeviceException;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraModuleController;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;

/**
 *
 */
public class CameraModuleControllerSimulator implements ICameraModuleController {

	@Override
	public CAMERA_MODULE getModule() throws DeviceException {
		return CAMERA_MODULE.ONE;
	}

	@Override
	public void moveModuleTo(CAMERA_MODULE module, IProgressMonitor monitor) throws Exception {

	}

	@Override
	public void stopModuleChange() throws DeviceException {

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String lookupHFOVUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double lookupObjectPixelSize(CAMERA_MODULE module) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String lookupObjectPixelSizeUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double lookupHFOV(CAMERA_MODULE module) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double lookupDefaultExposureTime(CAMERA_MODULE newModule) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getObjectPixelSizeInMM(CAMERA_MODULE cameraModule) {
		// TODO Auto-generated method stub
		return null;
	}

}
