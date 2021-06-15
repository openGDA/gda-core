/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool.rest;

import static uk.ac.gda.ui.tool.rest.ClientRestService.formatURL;
import static uk.ac.gda.ui.tool.rest.ClientRestService.submitRequest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.common.entity.device.DeviceMethods;
import uk.ac.gda.common.entity.device.DeviceValue;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;

/**
 * Provides to the GDA client access to the Device rest service
 *
 * <p>
 * <i>client.acquisition.service.endpoint</i> is the property which configures the URL endpoint to the service. The
 * default value is {@code http://127.0.0.1:8888/device}
 * </p>
 *
 * @author Maurizio Nagni
 */
@Service
public class DeviceRestServiceClient {

	@Autowired
	private ClientSpringContext clientContext;

	private String getServiceEndpoint() {
		return formatURL(clientContext.getRestServiceEndpoint(), "/device");
	}

	/**
	 * Returns the names of the available services - per device
	 *
	 * @return a collection of detectors names
	 *
	 * @throws GDAClientRestException
	 */
	public List<String> getServices() throws GDAClientRestException {
		String url = formatURL(getServiceEndpoint(), "/detector/services");
		ResponseEntity<List<String>> response = submitRequest(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
		return response.getBody();
	}

	/**
	 * Returns the getter/setter properties for a given service
	 *
	 * @param service the service to describe
	 *
	 * @return a description of the service
	 * @throws GDAClientRestException
	 */
	public DeviceMethods getProperties(String service) throws GDAClientRestException {
		String url = formatURL(getServiceEndpoint(), String.format("/detector/services/%s/properties", service));
		ResponseEntity<DeviceMethods> response = submitRequest(url, HttpMethod.GET, null, DeviceMethods.class);
		return response.getBody();
	}

	/**
	 * Given a device and service return a property value
	 *
	 * @param detectorName the detector to query
	 * @param serviceName the detector's service to use
	 * @param propertyName the service's property
	 *
	 * @return the property value
	 * @throws GDAClientRestException
	 */
	public DeviceValue getProperty(String detectorName, String serviceName, String propertyName) throws GDAClientRestException {
		String url = formatURL(getServiceEndpoint(), String.format("/detector/%s/%s/%s", detectorName, serviceName, propertyName));
		ResponseEntity<DeviceValue> response = submitRequest(url, HttpMethod.GET, null, new ParameterizedTypeReference<DeviceValue>() {});
		return response.getBody();
	}

	/**
	 * Sets a property value.
	 *
	 *  <p>
	 *  Submit a request to set a property value for a given device and service
	 *  </p>
	 * @param deviceValue
	 * @throws GDAClientRestException
	 */
	public void setProperty(DeviceValue deviceValue) throws GDAClientRestException {
		String url = formatURL(getServiceEndpoint(), "/detector");
		HttpEntity<DeviceValue> responseEntity = new HttpEntity<>(deviceValue);
		submitRequest(url, HttpMethod.POST, responseEntity, new ParameterizedTypeReference<DeviceValue>() {});
	}
}