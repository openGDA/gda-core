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

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Client interface to Bluesky worker, can run plans, introspect plans and devices
 * and provide updates on running tasks.
 */
public interface BlueskyController {
	/**
	 * Retrieve a list of plans that the worker can run.
	 * @return Information about plans in a {@link PlanResponse}
	 * @throws BlueskyException If an error occurs while communicating with the worker.
	 */
	PlanResponse getPlans() throws BlueskyException;

	/**
	 * Retrieve a list of devices that the worker can run.
	 * @return Information about devices in a {@link DeviceResponse}
	 * @throws BlueskyException If an error occurs while communicating with the worker.
	 */
	DeviceResponse getDevices() throws BlueskyException;

	/**
	 * Add a listener for {@link WorkerEvent}s emitted by the worker.
	 * @param listener Callback to process {@link WorkerEvent}s.
	 * @return true if listener added
	 */
	boolean addWorkerEventListener(Consumer<WorkerEvent> listener);

	/**
	 * Remote a listener for {@link TaskStatus}s.
	 * @param listener The callback to remove
	 * @return true if listener removed
	 */
	boolean removeWorkerEventListener(Consumer<WorkerEvent> listener);

	/**
	 * Submit a task for the worker to run, this method is asynchronous and
	 * will not block.
	 * @param task The task to be submitted to the Worker.
	 * @return A a task creation response
	 * @throws BlueskyException If an error occurs while communicating with the worker.
	 */
	TaskResponse submitTask(Task task) throws BlueskyException;

	/**
	 * Get the worker to run a task and return a future to synchronise on.
	 * @param task The task to be submitted to the Worker.
	 * @return The event signifying that the task has completed or failed,
	 * 		   includes relevant information such as an error message.
	 * @throws BlueskyException If an error occurs while communicating with the worker.
	 */
	CompletableFuture<WorkerEvent> runTask(Task task) throws BlueskyException;
}
