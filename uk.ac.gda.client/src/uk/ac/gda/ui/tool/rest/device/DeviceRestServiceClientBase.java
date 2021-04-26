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

package uk.ac.gda.ui.tool.rest.device;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.common.entity.device.DeviceValue;
import uk.ac.gda.ui.tool.rest.DeviceRestServiceClient;

/**
 * Utilities to use {@link DeviceRestServiceClient}
 *
 * @author Maurizio Nagni
 */
@Service
public class DeviceRestServiceClientBase {

	@Autowired
	private DeviceRestServiceClient deviceService;

	/**
	 * Retrieve the value of a service
	 *
	 * @param deviceName the detector to query
	 * @param serviceName the service to use to query the detector
	 * @param propertyName the service property to ask for
	 * @return the value of the service property for the required detector
	 * @throws GDAClientRestException
	 */
	public final DeviceValue getDeviceValue(String deviceName, String serviceName, String propertyName) throws GDAClientRestException {
		return deviceService.getProperty(deviceName, serviceName, propertyName);
	}

	/**
	 * Send a command to a service.
	 *
	 * @param deviceName the detector to query
	 * @param serviceName the service to use to query the detector
	 * @param method the command to request
	 * @throws GDAClientRestException
	 */
	public final void commandDevice(String deviceName, String serviceName, String method) throws GDAClientRestException {
		setDeviceValue(deviceName, serviceName, method, null);
	}

	/**
	 * Set the value of a service
	 *
	 * @param deviceName the detector to query
	 * @param serviceName the service to use to query the detector
	 * @param propertyName the service property to set
	 * @param value the new property value
	 *
	 * @throws GDAClientRestException
	 */
	public final void setDeviceValue(String deviceName, String serviceName, String propertyName, Object value) throws GDAClientRestException {
		DeviceValue.Builder newValue = new DeviceValue.Builder()
			.withProperty(propertyName)
			.withServiceName(serviceName);
		if (value != null) {
			newValue = newValue.withValue(value);
		}
		newValue.withName(deviceName);
		deviceService.setProperty(newValue.build());
	}
}
