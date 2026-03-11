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

package uk.ac.diamond.daq.bluesky.event;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.diamond.daq.bluesky.api.model.TaskOutcome;
import uk.ac.diamond.daq.bluesky.api.model.TaskOutcome.TaskResult;

public record TaskStatus(
	@JsonProperty("task_id") String taskID,
	@JsonProperty("task_complete") boolean complete,
	@JsonProperty("task_failed") boolean failed,
	TaskOutcome result
) {

	/**
	 * Get the value returned by the plan this task represents
	 *
	 * @return the return value of the plan being run. If the plan failed
	 * or is not yet complete, this will return null whereas if the plan
	 * succeeded but returned None (or an unserializable type), this will
	 * return empty.
	 */
	public Optional<Object> returnValue() {
		if (result instanceof TaskResult res) {
			return Optional.ofNullable(res.result());
		}
		return null;
	}
}
