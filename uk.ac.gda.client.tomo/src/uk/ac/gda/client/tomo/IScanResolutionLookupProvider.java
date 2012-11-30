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

package uk.ac.gda.client.tomo;

import gda.device.DeviceException;

public interface IScanResolutionLookupProvider {

	/**
	 * @param resolution
	 * @return the number of projections for the given resolution
	 * @throws DeviceException 
	 */
	int getNumberOfProjections(int resolution) throws Exception;

	/**
	 * @param resolution
	 * @return the step size for the given resolution
	 * @throws Exception 
	 */
	double getStepSize(int resolution) throws Exception;

	/**
	 * @param resolution
	 * @return bin X value for the given resolution
	 * @throws Exception 
	 */
	int getBinX(int resolution) throws Exception;

	/**
	 * @param resolution
	 * @return bin Y value for the given resolution
	 * @throws Exception 
	 */
	int getBinY(int resolution) throws Exception;

}
