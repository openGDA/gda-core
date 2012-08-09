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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.device.DeviceException;
import gda.function.Lookup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.handlers.IModuleLookupTableHandler;
import uk.ac.gda.client.tomo.alignment.view.utils.ModuleLookupTableConstants;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;

/**
 *
 */
public class ModuleLookupTableHandler implements IModuleLookupTableHandler {
	private Lookup moduleTable;
	private Lookup moduleEnergyIntervalTable;

	private static final Logger logger = LoggerFactory.getLogger(ModuleLookupTableHandler.class);

	/**
	 * @param moduleTable
	 *            The moduleTable to set.
	 */
	public void setModuleTable(Lookup moduleTable) {
		this.moduleTable = moduleTable;
	}

	public void setModuleEnergyIntervalTable(Lookup moduleEnergyIntervalTable) {
		this.moduleEnergyIntervalTable = moduleEnergyIntervalTable;
	}

	/**
	 * gets the values from the lookup table and returns it in 'mm'
	 * 
	 * @return the object pixel size in 'mm'
	 */
	@Override
	public double getObjectPixelSizeInMM(CAMERA_MODULE selectedCameraModule) {
		if (!CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
			double lookupValue = 1.0;
			try {
				lookupValue = moduleTable.lookupValue(selectedCameraModule.getValue(),
						ModuleLookupTableConstants.OBJECT_PIXEL_SIZE);
				logger.info("lookupvalue for {} module is {}", selectedCameraModule.getValue(), lookupValue);
			} catch (Exception ex) {
				logger.error("Ex", ex);
			}

			return lookupValue / 1000;
		}
		return Double.NaN;
	}

	@Override
	public double lookupSs1X(CAMERA_MODULE selectedCameraModule) throws DeviceException {
		if (!CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
			return moduleTable.lookupValue(selectedCameraModule.getValue(), ModuleLookupTableConstants.SS1_X);
		}
		return Double.NaN;
	}

	@Override
	public double lookupAcquisitionTimeForEnergyInterval(CAMERA_MODULE selectedCameraModule, Integer energy)
			throws DeviceException {
		if (!CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
			return moduleEnergyIntervalTable.lookupValue(selectedCameraModule.getValue(), energy.toString());
		}
		return Double.NaN;
	}

	@Override
	public double lookupCam1Z(CAMERA_MODULE selectedCameraModule) throws DeviceException {
		if (!CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
			return moduleTable.lookupValue(selectedCameraModule.getValue(), ModuleLookupTableConstants.CAM1_Z);
		}
		return Double.NaN;
	}

	@Override
	public double lookupCam1Roll(CAMERA_MODULE module) throws DeviceException {
		if (!CAMERA_MODULE.NO_MODULE.equals(module)) {
			return moduleTable.lookupValue(module.getValue(), ModuleLookupTableConstants.CAM1_ROLL);
		}
		return Double.NaN;
	}

	@Override
	public double lookupCam1X(CAMERA_MODULE module) throws DeviceException {
		if (!CAMERA_MODULE.NO_MODULE.equals(module)) {
			return moduleTable.lookupValue(module.getValue(), ModuleLookupTableConstants.CAM1_X);
		}
		return Double.NaN;
	}

	@Override
	public double lookupSs1Rx(CAMERA_MODULE module) throws DeviceException {
		if (!CAMERA_MODULE.NO_MODULE.equals(module)) {
			return moduleTable.lookupValue(module.getValue(), ModuleLookupTableConstants.S2_XS);
		}
		return Double.NaN;
	}

	@Override
	public double lookupSs1Rz(CAMERA_MODULE selectedCameraModule) throws DeviceException {
		if (!CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
			return moduleTable.lookupValue(selectedCameraModule.getValue(), ModuleLookupTableConstants.S2_YS);
		}
		return Double.NaN;
	}

	@Override
	public double lookupBallDiameter(CAMERA_MODULE selectedCameraModule) throws DeviceException {
		if (!CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
			return moduleTable.lookupValue(selectedCameraModule.getValue(), ModuleLookupTableConstants.BALL_DIAMETER);
		}
		return Double.NaN;
	}

	@Override
	public double lookupHFOV(CAMERA_MODULE selectedCameraModule) throws DeviceException {
		if (!CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
			return moduleTable.lookupValue(selectedCameraModule.getValue(), ModuleLookupTableConstants.HFOV);
		}
		return Double.NaN;
	}

	@Override
	public String lookupHFOVUnit() throws DeviceException {
		return moduleTable.lookupUnitString(ModuleLookupTableConstants.HFOV);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Double lookupObjectPixelSize(CAMERA_MODULE module) throws DeviceException {
		if (!CAMERA_MODULE.NO_MODULE.equals(module)) {
			return Double.valueOf(moduleTable.lookupValue(module.getValue(),
					ModuleLookupTableConstants.OBJECT_PIXEL_SIZE));
		}
		return null;
	}

	@Override
	public String lookupObjectPixelSizeUnits() throws DeviceException {
		return moduleTable.lookupUnitString(ModuleLookupTableConstants.OBJECT_PIXEL_SIZE);
	}

	@Override
	public Double lookupDefaultExposureTime(CAMERA_MODULE module) throws DeviceException {
		if (!CAMERA_MODULE.NO_MODULE.equals(module)) {
			return Double.valueOf(moduleTable.lookupValue(module.getValue(),
					ModuleLookupTableConstants.DEFAULT_EXPOSURE_TIME));
		}
		return null;
	}
}