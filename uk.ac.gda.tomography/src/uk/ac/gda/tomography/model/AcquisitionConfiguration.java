/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.model;

import java.util.Map;
import java.util.Set;

import gda.device.Device;

/**
 * An acquisition includes three components.
 * <ol>
 * <li>a set of devices involved in the execution, i.e. cameras, motors, beam</li>
 * <li>a class containing the parameters necessary to the required acquisition</li>
 * <li>a map of key, values which are not required for the acquisition process but are useful to be stored</li>
 * </ol>
 *
 * @author Maurizio Nagni
 */
public interface AcquisitionConfiguration<T extends AcquisitionParameters> {

	/**
	 * @return the devices involved in this acquisition
	 */
	public Set<Device> getDevices();

	/**
	 * @return the parameters defining the acquisition execution
	 */
	public T getAcquisitionParameters();

	/**
	 * @return a dictionary of text data
	 */
	public Map<String, String> getMetadata();

}
