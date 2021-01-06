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

package uk.ac.gda.ui.tool.spring;

import org.springframework.stereotype.Component;

import gda.configuration.properties.LocalProperties;

/**
 * Defines the context for a spring/rest enabled client
 *
 * @author Maurizio Nagni
 */
@Component
public class ClientSpringContext {

	public static final String REST_ENDPOINT = "client.rest.gda.service.endpoint";
	public static final String REST_ENDPOINT_DEFAULT = "http://127.0.0.1:8888";

	/**
	 * Returns the GDA rest service endpoint
	 *
	 * @return the service location
	 */
	public final String getRestServiceEndpoint() {
		return LocalProperties.get(REST_ENDPOINT, REST_ENDPOINT_DEFAULT);
	}

}
