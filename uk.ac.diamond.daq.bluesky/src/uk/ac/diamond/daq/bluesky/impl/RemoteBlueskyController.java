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

package uk.ac.diamond.daq.bluesky.impl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import javax.jms.JMSException;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;

import gda.configuration.properties.LocalProperties;
import gda.util.JsonMessageListener;
import uk.ac.diamond.daq.blueapi.ApiClient;
import uk.ac.diamond.daq.blueapi.api.DefaultApi;
import uk.ac.diamond.daq.blueapi.model.DeviceModel;
import uk.ac.diamond.daq.blueapi.model.DeviceResponse;
import uk.ac.diamond.daq.blueapi.model.PlanModel;
import uk.ac.diamond.daq.blueapi.model.PlanResponse;
import uk.ac.diamond.daq.blueapi.model.RunPlan;
import uk.ac.diamond.daq.blueapi.model.TaskResponse;
import uk.ac.diamond.daq.blueapi.model.WorkerTask;
import uk.ac.diamond.daq.bluesky.api.BlueskyController;
import uk.ac.diamond.daq.bluesky.api.BlueskyException;
import uk.ac.diamond.daq.bluesky.api.WorkerEvent;

@Component
public class RemoteBlueskyController implements BlueskyController {

	private ApiClient client = new ApiClient();
	private DefaultApi api = null;

	private static final Logger logger = LoggerFactory.getLogger(RemoteBlueskyController.class);

	private Set<Consumer<WorkerEvent>> taskEventListeners;

	/**
	 * Constructor to setup the API connection
	 */
	@Activate
	public void init() throws JMSException {
		this.client.setBasePath("http://localhost:8000");
		this.api = new DefaultApi(this.client);

		final var connectionFactory = new ActiveMQConnectionFactory();
		final var activeMqUrl = LocalProperties.get(LocalProperties.GDA_ACTIVEMQ_BROKER_URI);
		connectionFactory.setBrokerURL(activeMqUrl);

		// CopyOnWriteArraySet used as set may be modified during iteration (e.g. in runTask via onWorkerEvent)
		taskEventListeners = new CopyOnWriteArraySet<>();

		final var taskEventJsonListener = new JsonMessageListener<WorkerEvent>(WorkerEvent.class);
		taskEventJsonListener.setTopic(BlueskyDestinations.WORKER_EVENT.getTopicName());
		taskEventJsonListener.setHandler(this::onWorkerEvent);
		taskEventJsonListener.configure();
	}

	private void onWorkerEvent(WorkerEvent event) {
		logger.info("New Bluesky event: {}", event);
		taskEventListeners.forEach(listener -> listener.accept(event));
	}

	@Override
	public Consumer<WorkerEvent> addWorkerEventListener(Consumer<WorkerEvent> listener) {
		taskEventListeners.add(listener);
		return listener;
	}

	@Override
	public void removeWorkerEventListener(Consumer<WorkerEvent> listener) {
		taskEventListeners.remove(listener);
	}

	@Override
	public List<PlanModel> getPlans() throws RestClientException {
		PlanResponse response = api.getPlansPlansGet();

		return response.getPlans();
	}

	@Override
	public PlanModel getPlan(String name) throws BlueskyException {
		return api.getPlanByNamePlansNameGet(name);
	}

	@Override
	public List<DeviceModel> getDevices() throws BlueskyException {
		DeviceResponse response = api.getDevicesDevicesGet();

		return response.getDevices();
	}

	@Override
	public DeviceModel getDevice(String name) throws BlueskyException {
		return api.getDeviceByNameDevicesNameGet(name);
	}

	@Override
	public String submitTask(RunPlan task) throws BlueskyException {
		TaskResponse response = api.submitTaskTasksPost(task);
		api.updateTaskWorkerTaskPut(new WorkerTask().taskId(response.getTaskId()));
		return response.getTaskId();
	}

	@Override
	public CompletableFuture<WorkerEvent> runTask(RunPlan task) throws BlueskyException {
		final var done = new CompletableFuture<WorkerEvent>();
		final var taskIdFuture = new CompletableFuture<String>();
		final Consumer<WorkerEvent> listener = event -> {
			if (isComplete(event)) {
				if (isError(event)) {
					done.completeExceptionally(new BlueskyException(event));
				} else {
					done.complete(event);
				}
			}
		};
		addWorkerEventListener(listener);
		final TaskResponse response = api.submitTaskTasksPost(task);
		api.updateTaskWorkerTaskPut(new WorkerTask().taskId(response.getTaskId()));

		taskIdFuture.complete(response.getTaskId());
		return done.thenApply(event -> {
			removeWorkerEventListener(listener);
			return event;
		});
	}

	private boolean isComplete(WorkerEvent event) {
		return event.taskStatus() != null && event.taskStatus().taskComplete();
	}

	private boolean isError(WorkerEvent event) {
		return !event.errors().isEmpty() || (event.taskStatus() != null && event.taskStatus().taskFailed());
	}
}
