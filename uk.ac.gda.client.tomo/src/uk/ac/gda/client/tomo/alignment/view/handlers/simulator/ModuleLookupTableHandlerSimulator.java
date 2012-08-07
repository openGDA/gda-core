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
import uk.ac.gda.client.tomo.alignment.view.handlers.IModuleLookupTableHandler;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;

/**
 *
 */
public class ModuleLookupTableHandlerSimulator implements IModuleLookupTableHandler {

	@Override
	public double getObjectPixelSizeInMM(CAMERA_MODULE module) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double lookupSs1X(CAMERA_MODULE module) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double lookupAcquisitionTimeForEnergyInterval(CAMERA_MODULE module, Integer energy) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double lookupCam1Z(CAMERA_MODULE module) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double lookupCam1Roll(CAMERA_MODULE module) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double lookupCam1X(CAMERA_MODULE module) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double lookupSs1Rz(CAMERA_MODULE module) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double lookupBallDiameter(CAMERA_MODULE module) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double lookupHFOV(CAMERA_MODULE module) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String lookupHFOVUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double lookupObjectPixelSize(CAMERA_MODULE module) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String lookupObjectPixelSizeUnits() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

}
