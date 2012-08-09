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
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;

/**
 * This looks up values from a lookup table. In i12 the lookup table is located in
 * "{SOFTWARE_LOCATION}/i12-config/lookupTables/tomo/module_lookup_table.txt"
 */
public interface IModuleLookupTableHandler extends ITomoHandler {

	/**
	 * @param module
	 * @return the object pixel size for the given module. If module is {@link CAMERA_MODULE}.NO_MODULE then returns
	 *         {@link Double}.Nan
	 */
	double getObjectPixelSizeInMM(CAMERA_MODULE module);

	/**
	 * @param module
	 * @return the ss1x for the given module. If module is {@link CAMERA_MODULE}.NO_MODULE then returns {@link Double}
	 *         .Nan
	 * @throws DeviceException
	 */
	double lookupSs1X(CAMERA_MODULE module) throws DeviceException;

	/**
	 * @param module
	 * @return the cam1.z for the given module. If module is {@link CAMERA_MODULE}.NO_MODULE then returns {@link Double}
	 *         .Nan
	 * @throws DeviceException
	 */
	double lookupCam1Z(CAMERA_MODULE module) throws DeviceException;

	/**
	 * @param module
	 * @return the cam1Roll for the given module. If module is {@link CAMERA_MODULE}.NO_MODULE then returns
	 *         {@link Double} .Nan
	 * @throws DeviceException
	 */
	double lookupCam1Roll(CAMERA_MODULE module) throws DeviceException;

	/**
	 * @param module
	 * @return the cam1X for the given module. If module is {@link CAMERA_MODULE}.NO_MODULE then returns {@link Double}
	 *         .Nan
	 * @throws DeviceException
	 */
	double lookupCam1X(CAMERA_MODULE module) throws DeviceException;

	/**
	 * @param module
	 * @return the ss1Rx for the given module. If module is {@link CAMERA_MODULE}.NO_MODULE then returns {@link Double}
	 *         .Nan
	 * @throws DeviceException
	 */
	double lookupSs1Rx(CAMERA_MODULE module) throws DeviceException;

	/**
	 * @param module
	 * @return the ss1Rz for the given module. If module is {@link CAMERA_MODULE}.NO_MODULE then returns {@link Double}
	 *         .Nan
	 * @throws DeviceException
	 */
	double lookupSs1Rz(CAMERA_MODULE module) throws DeviceException;

	/**
	 * @param module
	 * @return the ball diameter for the given module. If module is {@link CAMERA_MODULE}.NO_MODULE then returns
	 *         {@link Double} .Nan
	 * @throws DeviceException
	 */
	double lookupBallDiameter(CAMERA_MODULE module) throws DeviceException;

	/**
	 * @param module
	 * @return the horizontal field of view for the given module. If module is {@link CAMERA_MODULE}.NO_MODULE then
	 *         returns {@link Double} .Nan
	 * @throws DeviceException
	 */
	double lookupHFOV(CAMERA_MODULE module) throws DeviceException;

	/**
	 * This is picked from another lookup table. on i12 this is located in
	 * "{SOFTWARE_LOC}/i12-config/lookupTables/tomo/energy_interval_module.txt"
	 * 
	 * @param module
	 * @param energy
	 * @return the exposure time for a given module for a given energy level for the given module. If module is
	 *         {@link CAMERA_MODULE}.NO_MODULE then returns {@link Double} .Nan
	 * @throws DeviceException
	 */
	double lookupAcquisitionTimeForEnergyInterval(CAMERA_MODULE module, Integer energy) throws DeviceException;

	/**
	 * @return the units of the HFOV field in the lookup table
	 * @throws DeviceException
	 */
	String lookupHFOVUnit() throws DeviceException;

	/**
	 * @param module
	 * @return object pixel size of the detector for a given module
	 * @throws DeviceException
	 */
	Double lookupObjectPixelSize(CAMERA_MODULE module) throws DeviceException;

	/**
	 * @return units for the object pixel size
	 * @throws DeviceException
	 */
	String lookupObjectPixelSizeUnits() throws DeviceException;

	/**
	 * @param newModule
	 * @return the default exposure time for a given module
	 * @throws DeviceException 
	 */
	Double lookupDefaultExposureTime(CAMERA_MODULE newModule) throws DeviceException;

}
