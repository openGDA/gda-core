/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.bluesky.client.error;

import java.util.Map;

/** The given path arguments or query parameters were not valid */
public class InvalidQuery extends ApiException {
	private final String endpoint;
	private final Map<String, String> query;
	private final transient Object data;

	public InvalidQuery(String endpoint, Map<String, String> query, Exception e) {
		super("Unable to build request for endpoint=%s and query=%s".formatted(endpoint, query), e);
		this.endpoint = endpoint;
		this.query = query;
		this.data = null;
	}

	public InvalidQuery(Object data, Exception e) {
		super("Unable to build request for data=" + data, e);
		this.endpoint = null;
		this.query = null;
		this.data = data;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public Map<String, String> getQuery() {
		return query;
	}

	public Object getData() {
		return data;
	}
}
