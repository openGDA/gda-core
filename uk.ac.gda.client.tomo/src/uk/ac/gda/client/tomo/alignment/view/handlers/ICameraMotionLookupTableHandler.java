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
import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;


/**
 *
 */
public interface ICameraMotionLookupTableHandler {

	/**
	 * @return array of index values that are defined for t3.m1z
	 * @throws DeviceException 
	 */
	double[] getT3M1zValues()throws DeviceException;

	/**
	 * @param module
	 * @param t3m1zValue
	 * @return double value looked up corresponding to the t3.m1y value and for the given module
	 * @throws DeviceException 
	 */
	double lookupT3M1Y(CAMERA_MODULE module, double t3m1zValue)throws DeviceException;

	/**
	 * @param module
	 * @param t3m1zValue
	 * @return double value looked up corresponding to the t3.x value and for the given module
	 * @throws DeviceException 
	 */
	double lookupT3X(CAMERA_MODULE module, double t3m1zValue) throws DeviceException;

}
