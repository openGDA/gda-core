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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.device.DeviceException;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraModuleController;
import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;

public class PseudoModulesController implements ICameraModuleController {
	private Double objectPixelSize;

	private String objectPixelSizeUnits;

	private Double hfov;

	private Double defaultExposureTime;

	private Double objectPixelSizeInMm;

	@Override
	public void dispose() {

	}

	@Override
	public CAMERA_MODULE getModule() throws DeviceException {
		// This is just so that the aligment system will set the GUI up correctly.
		return CAMERA_MODULE.ONE;
	}

	@Override
	public void moveModuleTo(CAMERA_MODULE module, IProgressMonitor monitor) throws Exception {
		// Do nothing
	}

	@Override
	public void stopModuleChange() throws DeviceException {
		// Do nothing
	}

	private String hfovUnit;

	@Override
	public String lookupHFOVUnit() throws DeviceException {
		return hfovUnit;
	}

	public void setHfovUnit(String hfovUnit) {
		this.hfovUnit = hfovUnit;
	}

	public void setObjectPixelSize(Double objectPixelSize) {
		this.objectPixelSize = objectPixelSize;
	}

	@Override
	public Double lookupObjectPixelSize(CAMERA_MODULE module) throws DeviceException {
		return objectPixelSize;
	}

	public void setObjectPixelSizeUnits(String objectPixelSizeUnits) {
		this.objectPixelSizeUnits = objectPixelSizeUnits;
	}

	@Override
	public String lookupObjectPixelSizeUnits() throws DeviceException {
		return objectPixelSizeUnits;
	}

	public void setHfov(Double hfov) {
		this.hfov = hfov;
	}

	@Override
	public Double lookupHFOV(CAMERA_MODULE module) throws DeviceException {
		return hfov;
	}

	public void setDefaultExposureTime(Double defaultExposureTime) {
		this.defaultExposureTime = defaultExposureTime;
	}

	@Override
	public Double lookupDefaultExposureTime(CAMERA_MODULE newModule) throws DeviceException {
		return defaultExposureTime;
	}

	public void setObjectPixelSizeInMm(Double objectPixelSizeInMm) {
		this.objectPixelSizeInMm = objectPixelSizeInMm;
	}

	@Override
	public Double getObjectPixelSizeInMM(CAMERA_MODULE cameraModule) {
		return objectPixelSizeInMm;
	}

	@Override
	public String lookupMagnificationUnit() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double lookupMagnification(CAMERA_MODULE module) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

}
