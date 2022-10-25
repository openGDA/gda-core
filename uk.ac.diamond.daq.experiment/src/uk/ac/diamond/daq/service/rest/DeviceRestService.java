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

package uk.ac.diamond.daq.service.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.diamond.daq.service.core.DeviceServiceCore;
import uk.ac.gda.common.entity.device.DeviceValue;

/**
 * Allows any client to communicate with GDA server side spring beans through a REST interface
 *
 * <p>
 * There are two assumptions
 * <ul>
 * <li>
 * the bean in the GDA server is named as {device}_{service}
 * </li>
 * <li>
 * any device is associated with any of the available services (still does not handle device with a subset of services)
 * </li>
 * </ul>
 * </p>
 *
 * @author Maurizio Nagni
 */
@RestController
@RequestMapping("/device")
public class DeviceRestService {

	@Autowired
	private DeviceServiceCore serviceCore;

	/**
	 * Returns a {@code List<String>} of the available services
	 * @param request
	 * @param response
	 */
	@GetMapping(value = "/detector/services")
	public void getServices(HttpServletRequest request, HttpServletResponse response) {
		serviceCore.getServices(response);
	}

	/**
	 * Returns the properties available for a specific service.
	 *
	 * The properties are returned as a DeviceMethods instance
	 *
	 * @param service the name of the service to analyse
	 * @param request
	 * @param response
	 */
	@GetMapping(value = "/detector/services/{service}/properties")
	public void getServiceProperties(@PathVariable String service, HttpServletRequest request, HttpServletResponse response) {
		serviceCore.getServiceProperties(service, response);
	}

	/**
	 * Return the value for a specific detector, service and property
	 *
	 * @param detectorName
	 * @param service
	 * @param propertyName
	 * @param request
	 * @param response
	 */
	@GetMapping(value = "/detector/{detectorName}/{serviceName}/{propertyName}")
	public void getDeviceValue(@PathVariable String detectorName, @PathVariable String serviceName, @PathVariable String propertyName,
			HttpServletRequest request, HttpServletResponse response) {
		var deviceRequest = serviceCore.createDeviceRequest(detectorName, serviceName, propertyName);
		serviceCore.getDeviceValue(deviceRequest, request, response);
	}

	/**
	 * Set the value for a specific detector, service and property.
	 * <p>
	 * The request body contains both the necessary parameters to identify detector, service and property and the value to set
	 * </p>
	 *
	 * @param deviceValue
	 * @param request
	 * @param response
	 */
	@PostMapping(value = "/detector")
	public void setDeviceValue(@RequestBody DeviceValue deviceValue, HttpServletRequest request, HttpServletResponse response) {
		var deviceRequest = serviceCore.createDeviceRequest(deviceValue);
		serviceCore.setDeviceValue(deviceRequest, request, response);
	}

}
