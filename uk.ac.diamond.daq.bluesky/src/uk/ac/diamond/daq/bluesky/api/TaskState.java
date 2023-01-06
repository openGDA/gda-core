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

import java.util.Set;

/**
 * State of a {@link Task} being processed by the worker
 */
public enum TaskState {
	PENDING, RUNNING, PAUSED, FAILED, COMPLETE;

	/* Set of states that correspond to a Task that the Worker has finished
	 * in one way or another (i.e. won't be doing any more of) */
	public static final Set<TaskState> TASK_FINISHED_STATES = Set.of(COMPLETE, FAILED);
}
