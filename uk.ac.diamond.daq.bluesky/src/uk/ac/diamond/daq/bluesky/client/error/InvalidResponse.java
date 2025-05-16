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

import com.fasterxml.jackson.core.JsonProcessingException;

/** Response from server was not in form we expected */
public class InvalidResponse extends ApiException {
	private final String content;
	private final Class<?> type;

	public InvalidResponse(String content, Class<?> type, JsonProcessingException cause) {
		super("Error reading %s from '%s'".formatted(type.getTypeName(), content), cause);
		this.content = content;
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public Class<?> getType() {
		return type;
	}
}
