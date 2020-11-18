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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Provides basic functionalities for a rest service client
 *
 * @author Maurizio Nagni
 */
class ClientRestService {

	private ClientRestService() {
	}

	private static final RestTemplate restTemplate = new RestTemplate();

	/**
	 * Submit a request to a service and return only the response body
	 * @param <T> the expected response type
	 * @param url the URL
	 * @param method the HTTP method (GET, POST, etc)
	 * @param requestEntity the entity (headers and/or body) to write to the request, may be {@code null}
	 * @param responseType the type of the return value
	 * @return the response as entity
	 */
	public static <T> T returnBody(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) {
		ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, responseType);
		return response.getBody();
	}

	/**
	 * Append a rest path to its endpoint
	 * @param serviceEndpoint the service host URL
	 * @param restPath the service endpoint
	 * @return the service request URL
	 */
	public static String formatURL(String serviceEndpoint, String restPath) {
		return String.format("%s%s", serviceEndpoint, restPath);
	}

}
