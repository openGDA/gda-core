/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.bluesky.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An event pertaining to the overall status of the worker
 */
public record WorkerEvent(
		/** The task Id */
		@JsonProperty("task_id")
		String taskId,
		/** The state of the worker */
		@JsonProperty("state")
		WorkerState state,
		/** The status of the task the worker is running, if any */
		@JsonProperty("task_status")
		TaskStatus taskStatus,
		/** Errors with the worker if applicable, can be empty */
		@JsonProperty("errors")
		List<String> errors,
		/** Warnings with the worker if applicable, can be empty */
		@JsonProperty("warnings")
		List<String> warnings
		) {}
