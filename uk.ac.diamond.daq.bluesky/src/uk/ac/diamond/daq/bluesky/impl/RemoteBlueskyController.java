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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import javax.jms.JMSException;
import javax.jms.Topic;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;

import gda.configuration.properties.LocalProperties;
import gda.util.MultiTypedJsonAMQPMessageListener;
import gda.util.MultiTypedJsonMessageListener;
import io.blueskyproject.TaggedDocument;
import uk.ac.diamond.daq.blueapi.ApiClient;
import uk.ac.diamond.daq.blueapi.api.DefaultApi;
import uk.ac.diamond.daq.blueapi.model.DeviceModel;
import uk.ac.diamond.daq.blueapi.model.DeviceResponse;
import uk.ac.diamond.daq.blueapi.model.PlanModel;
import uk.ac.diamond.daq.blueapi.model.PlanResponse;
import uk.ac.diamond.daq.blueapi.model.RunPlan;
import uk.ac.diamond.daq.blueapi.model.StateChangeRequest;
import uk.ac.diamond.daq.blueapi.model.TaskResponse;
import uk.ac.diamond.daq.blueapi.model.WorkerState;
import uk.ac.diamond.daq.blueapi.model.WorkerTask;
import uk.ac.diamond.daq.bluesky.api.BlueskyController;
import uk.ac.diamond.daq.bluesky.api.BlueskyException;
import uk.ac.diamond.daq.bluesky.api.WorkerEvent;

@Component
public class RemoteBlueskyController implements BlueskyController {

	private static final Logger logger = LoggerFactory.getLogger(RemoteBlueskyController.class);

	private String clientBasePathUrl = "http://localhost:8000"; // NOSONAR

	private enum MessageBrokerImpl {
		ACTIVEMQ(MultiTypedJsonMessageListener.class),
		RABBITMQ(MultiTypedJsonAMQPMessageListener.class);

		private final Class<? extends MultiTypedJsonMessageListener> listenerClass;

		private MessageBrokerImpl(Class<? extends MultiTypedJsonMessageListener> listenerClass) {
			this.listenerClass = listenerClass;
		}

		public MultiTypedJsonMessageListener createListener() {
			try {
				return listenerClass.getConstructor().newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Could not create listener " + listenerClass); // NOSONAR, not possible as listener classes have no-arg constructors
			}
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}

		public static MessageBrokerImpl getCurrentMessageBrokerImpl() {
			final String brokerImplStr = LocalProperties.get(LocalProperties.GDA_MESSAGE_BROKER_IMPL, ACTIVEMQ.toString());
			return MessageBrokerImpl.valueOf(brokerImplStr.toUpperCase());
		}

	}

	private ApiClient client = new ApiClient();
	private DefaultApi api = null;

	private Set<Consumer<WorkerEvent>> workerEventListeners;
	private Set<Consumer<TaggedDocument>> taggedDocumentListeners;

	/**
	 * Constructor to setup the API connection
	 */
	@Activate
	public void init() throws JMSException {
		client.setBasePath(clientBasePathUrl);
		api = new DefaultApi(client);

		// CopyOnWriteArraySet used as set may be modified during iteration (e.g. in runTask via onWorkerEvent)
		workerEventListeners = new CopyOnWriteArraySet<>();
		taggedDocumentListeners = new CopyOnWriteArraySet<>();

		final var messageListener = getListener(BlueskyDestinations.WORKER_EVENT);
		messageListener.setHandler(WorkerEvent.class, this::onWorkerEvent);
		messageListener.setHandler(TaggedDocument.class, this::onObjectEvent);
		messageListener.configure();
	}

	private MultiTypedJsonMessageListener getListener(Topic topic) throws JMSException {
		final MessageBrokerImpl brokerImpl = MessageBrokerImpl.getCurrentMessageBrokerImpl();
		final MultiTypedJsonMessageListener messageListener = brokerImpl.createListener();
		messageListener.setTopic(topic.getTopicName());
		return messageListener;
	}

	private void onWorkerEvent(WorkerEvent event) {
		logger.trace("New Bluesky worker event: {}", event);
		workerEventListeners.forEach(listener -> listener.accept(event));
	}

	private void onObjectEvent(TaggedDocument event) {
		logger.trace("New Bluesky tagged document: {}", event);
		taggedDocumentListeners.forEach(listener -> listener.accept(event));
	}

	@SuppressWarnings("unchecked") // casts are safe due to generic type
	@Override
	public <T> Consumer<T> addEventListener(Class<T> cls, Consumer<T> listener) {
		if (cls == WorkerEvent.class) {
			workerEventListeners.add((Consumer<WorkerEvent>) listener);
		} else if (cls == TaggedDocument.class) {
			taggedDocumentListeners.add((Consumer<TaggedDocument>) listener);
		} else {
			throw new IllegalArgumentException("Class: " + cls + " is not supported");
		}
		return listener;
	}

	@Override
	public <T> void removeWorkerEventListener(Consumer<T> listener) {
		workerEventListeners.remove(listener);
		taggedDocumentListeners.remove(listener);
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
		final var done = waitForCompletionEvent();
		final TaskResponse response = api.submitTaskTasksPost(task);
		api.updateTaskWorkerTaskPut(new WorkerTask().taskId(response.getTaskId()));
		return done;
	}

	private boolean isComplete(WorkerEvent event) {
		return event.taskStatus() != null && event.taskStatus().taskComplete();
	}

	private boolean isError(WorkerEvent event) {
		return !event.errors().isEmpty() || (event.taskStatus() != null && event.taskStatus().taskFailed());
	}

	@Override
	public WorkerState getWorkerState() throws BlueskyException {
		return api.getStateWorkerStateGet();
	}

	@Override
	public void putWorkerState(StateChangeRequest request) throws BlueskyException {
		api.setStateWorkerStatePut(request);
	}

	@Override
	public CompletableFuture<Optional<WorkerEvent>> abort() throws BlueskyException {
		logger.info("Abort requested");
		final var done = waitForCompletionEvent();
		if (isWorkerRunning()) {
			putWorkerState(new StateChangeRequest().newState(WorkerState.ABORTING));
			return done.thenApply(Optional::ofNullable);
		} else {
			done.cancel(false);
			final var empty = new CompletableFuture<Optional<WorkerEvent>>();
			empty.complete(Optional.empty());
			return empty;
		}

	}

	private CompletableFuture<WorkerEvent> waitForCompletionEvent() {
		final var done = new CompletableFuture<WorkerEvent>();
		final Consumer<WorkerEvent> listener = event -> {
			if (isComplete(event)) {
				if (isError(event)) {
					done.completeExceptionally(new BlueskyException(event));
				} else {
					done.complete(event);
				}
			}
		};
		addEventListener(WorkerEvent.class, listener);
		return done.whenComplete((event, e) -> removeWorkerEventListener(listener));
	}

	@Override
	public boolean isWorkerRunning() throws BlueskyException {
		final var state = getWorkerState();
		return !Set.of(WorkerState.IDLE, WorkerState.PANICKED).contains(state);
	}

	public String getClientBasePathUrl() {
		return clientBasePathUrl;
	}

	public void setClientBasePathUrl(String clientBasePathUrl) {
		this.clientBasePathUrl = clientBasePathUrl;
	}

}
