/*-
 * Copyright © 2026 Diamond Light Source Ltd.
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

import uk.ac.diamond.daq.bluesky.api.model.TaskOutcome.TaskError;

/**
 * The exception raised to signify a plan failing to complete successfully on
 * the blueAPI server
 * <p>
 * An overview of the exception is included in the form of exception type and
 * message. Further details are not available as exceptions are rarely
 * serializable.
 */
public class PlanFailedException extends BlueskyException {
	/** The UUID of the task that ran the plan */
	private final String taskId;
	/** The name of the exception type raised by the plan (eg "ValueError") */
	private final String errorType;
	/** The message of the exception raised by the plan */
	private final String errorMessage;

	public PlanFailedException(String task, TaskError err) {
		super("%s: %s".formatted(err.type(), err.message()));
		taskId = task;
		errorType = err.type();
		errorMessage = err.message();
	}
	public String getTaskId() {
		return taskId;
	}
	public String getErrorType() {
		return errorType;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
}
