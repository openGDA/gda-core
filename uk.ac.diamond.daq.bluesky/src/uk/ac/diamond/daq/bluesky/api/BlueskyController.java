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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import uk.ac.diamond.daq.bluesky.api.model.Device;
import uk.ac.diamond.daq.bluesky.api.model.Plan;
import uk.ac.diamond.daq.bluesky.api.model.Task;
import uk.ac.diamond.daq.bluesky.api.model.WorkerState;
import uk.ac.diamond.daq.bluesky.event.WorkerEvent;


public interface BlueskyController {
	/**
	 * Add a listener for {@link WorkerEvent}s emitted by the worker.
	 * @param listener Callback to process {@link WorkerEvent}s.
	 * @return Reference to the listener for chaining operations
	 */
	<T> Consumer<T> addEventListener(Class<T> cls, Consumer<T> listener);

	/**
	 * Remove a listener for {@link WorkerEvent}s.
	 * @param listener The callback to remove
	 */
	<T> void removeWorkerEventListener(Consumer<T> listener);

	/**
	 * Retrieve a list of plans that the worker can run.
	 * @return Information about plans as a list of {@link Plan}
	 * @throws BlueskyException If an error occurs while communicating with the worker.
	 */
	List<Plan> getPlans() throws BlueskyException;

	/**
	 * Retrieve a plan by name that the worker can run.
	 * @param name the name of the plan
	 * @return {@link Plan} containing information about the retrieved plan
	 * @throws BlueskyException if no such plan can be found
	 */
	Plan getPlan(String name) throws BlueskyException;

	/**
	 * Checks if the worker is currently running.
	 * @return False if the worker is in an idle or crashed state, true otherwise
	 * @throws BlueskyException If there is a problem communicating with the worker
	 */
	boolean isWorkerRunning() throws BlueskyException;

	/**
	 * Get the current state of the worker
	 * @return The state of the worker
	 * @throws BlueskyException If there is a problem communicating with the worker
	 */
	WorkerState getWorkerState() throws BlueskyException;

	/**
	 * Request a change to the worker's state
	 * @param state The new state for the worker
	 * @throws BlueskyException If there is a problem communicating with the worker
	 */
	void setWorkerState(WorkerState state) throws BlueskyException;

	/**
	 * Abort the current plan if there is one running
	 * @return A future which yields the plan completion event if a plan was running
	 * @throws BlueskyException If there is a problem communicating with the worker
	 */
	CompletableFuture<Optional<WorkerEvent>> abort() throws BlueskyException;

	/**
	 * Retrieve a list of devices that the worker can run.
	 * @return Information about devices
	 * @throws BlueskyException If an error occurs while communicating with the worker.
	 */
	List<Device> getDevices() throws BlueskyException;

	/**
	 * Retrieve device by name that the worker can run.
	 * @param name the name of the device
	 * @return {@link Device} containing information about the retrieved device.
	 * @throws BlueskyException if no such device can be found
	 */
	Device getDevice(String name) throws BlueskyException;

	/**
	 * Submit a task for the worker to run, this method is asynchronous and
	 * will not block.
	 * @param task The task to be submitted to the Worker.
	 * @return A a task creation response
	 * @throws BlueskyException If an error occurs while communicating with the worker.
	 */
	String submitTask(Task task) throws BlueskyException;

	/**
	 * Get the worker to run a task and return a future to synchronise on.
	 * @param task The task to be submitted to the Worker.
	 * @throws BlueskyException If an error occurs while communicating with the worker.
	 */
	CompletableFuture<WorkerEvent> runTask(Task task) throws BlueskyException;
}
