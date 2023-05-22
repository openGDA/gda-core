/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A document pertaining to the status of a task
 */
public record TaskStatus (
		/** The task Id */
		@JsonProperty("task_id")
		String taskId,
		/** Whether the task is complete */
		@JsonProperty("task_complete")
		boolean taskComplete,
		/** Whether the task has failed */
		@JsonProperty("task_failed")
		boolean taskFailed) {}
