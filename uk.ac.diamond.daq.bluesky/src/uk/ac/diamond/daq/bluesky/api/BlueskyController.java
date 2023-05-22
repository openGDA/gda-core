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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import uk.ac.diamond.daq.blueapi.model.DeviceModel;
import uk.ac.diamond.daq.blueapi.model.DeviceResponse;
import uk.ac.diamond.daq.blueapi.model.PlanModel;
import uk.ac.diamond.daq.blueapi.model.RunPlan;

public interface BlueskyController {
	/**
	 * Add a listener for {@link WorkerEvent}s emitted by the worker.
	 * @param listener Callback to process {@link WorkerEvent}s.
	 * @return Reference to the listener for chaining operations
	 */
	Consumer<WorkerEvent> addWorkerEventListener(Consumer<WorkerEvent> listener);

	/**
	 * Remove a listener for {@link WorkerEvent}s.
	 * @param listener The callback to remove
	 */
	void removeWorkerEventListener(Consumer<WorkerEvent> listener);

	/**
	 * Retrieve a list of plans that the worker can run.
	 * @return Information about plans as a list of {@link PlanModel}
	 * @throws BlueskyException If an error occurs while communicating with the worker.
	 */
	List<PlanModel> getPlans() throws BlueskyException;

	/**
	 * Retrieve a plan by name that the worker can run.
	 * @param name the name of the plan
	 * @return {@link PlanModel} containing information about the retrieved plan
	 * @throws BlueskyException if no such plan can be found
	 */
	PlanModel getPlan(String name) throws BlueskyException;

	/**
	 * Retrieve a list of devices that the worker can run.
	 * @return Information about devices in a {@link DeviceResponse}
	 * @throws BlueskyException If an error occurs while communicating with the worker.
	 */
	List<DeviceModel> getDevices() throws BlueskyException;

	/**
	 * Retrieve device by name that the worker can run.
	 * @param name the name of the device
	 * @return {@link DeviceModel} containing information about the retrieved device.
	 * @throws BlueskyException if no such device can be found
	 */
	DeviceModel getDevice(String name) throws BlueskyException;

	/**
	 * Submit a task for the worker to run, this method is asynchronous and
	 * will not block.
	 * @param task The task to be submitted to the Worker.
	 * @return A a task creation response
	 * @throws BlueskyException If an error occurs while communicating with the worker.
	 */
	String submitTask(RunPlan task) throws BlueskyException;

	/**
	 * Get the worker to run a task and return a future to synchronise on.
	 * @param task The task to be submitted to the Worker.
	 * @return A future representing the final state of the task
	 * @throws BlueskyException If an error occurs while communicating with the worker.
	 */
	CompletableFuture<WorkerEvent> runTask(RunPlan task) throws BlueskyException;
}
