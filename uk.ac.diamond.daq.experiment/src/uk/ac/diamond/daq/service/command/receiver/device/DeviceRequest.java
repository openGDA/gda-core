/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.service.command.receiver.device;

import uk.ac.gda.common.entity.device.DeviceValue;

/**
 * A utility class to move data for the device rest operations
 *
 * @author Maurizio Nagni
 *
 */
public class DeviceRequest {

	/**
	 * The instance of the service providing the property to get/set
	 */
	private final Object device;

	/**
	 * The client request document
	 */
	private final DeviceValue deviceValue;

	public DeviceRequest(Object device, DeviceValue deviceValue) {
		super();
		this.device = device;
		this.deviceValue = deviceValue;
	}

	public Object getDevice() {
		return device;
	}

	public DeviceValue getDeviceValue() {
		return deviceValue;
	}
}
