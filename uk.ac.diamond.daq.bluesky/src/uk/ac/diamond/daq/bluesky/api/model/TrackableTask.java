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

package uk.ac.diamond.daq.bluesky.api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrackableTask(
		Object task,
		@JsonProperty("is_complete") boolean complete,
		@JsonProperty("is_pending") boolean pending,
		@JsonProperty("request_id") String requestId,
		@JsonProperty("task_id") String id,
		List<Object> errors
) {
	public boolean failed() {
		return !errors.isEmpty();
	}
}