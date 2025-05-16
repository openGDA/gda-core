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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.factory.ConfigurableAware;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.util.MultiTypedJsonAMQPMessageListener;
import gda.util.MultiTypedJsonMessageListener;
import io.blueskyproject.TaggedDocument;
import uk.ac.diamond.daq.bluesky.api.BlueskyController;
import uk.ac.diamond.daq.bluesky.api.BlueskyException;
import uk.ac.diamond.daq.bluesky.api.model.Device;
import uk.ac.diamond.daq.bluesky.api.model.Plan;
import uk.ac.diamond.daq.bluesky.api.model.Task;
import uk.ac.diamond.daq.bluesky.api.model.WorkerState;
import uk.ac.diamond.daq.bluesky.client.ApiClient;
import uk.ac.diamond.daq.bluesky.client.error.ApiException;
import uk.ac.diamond.daq.bluesky.event.WorkerEvent;
import uk.ac.diamond.daq.osgi.OsgiService;

@OsgiService(BlueskyController.class)
public class RemoteBlueskyController implements BlueskyController, ConfigurableAware, Findable {

	private static final Logger logger = LoggerFactory.getLogger(RemoteBlueskyController.class);

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


	private ApiClient api;

	private Set<Consumer<WorkerEvent>> workerEventListeners;
	private Set<Consumer<TaggedDocument>> taggedDocumentListeners;

	private String name;

	public RemoteBlueskyController(ApiClient api) {
		this.api = api;
	}

	/**
	 * Setup the API connection
	 */
	@Override
	public void postConfigure() throws FactoryException {
		try {
			// CopyOnWriteArraySet used as set may be modified during iteration (e.g. in runTask via onWorkerEvent)
			workerEventListeners = new CopyOnWriteArraySet<>();
			taggedDocumentListeners = new CopyOnWriteArraySet<>();

			final var messageListener = getListener(BlueskyDestinations.WORKER_EVENT);
			messageListener.setHandler(WorkerEvent.class, this::onWorkerEvent);
			messageListener.setHandler(TaggedDocument.class, this::onObjectEvent);
			messageListener.configure();
		} catch (Exception e) {
			logger.error("Error configuring blueapi client", e);
		}
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
	public List<Plan> getPlans() throws BlueskyException {
		return api.getPlans();
	}

	@Override
	public Plan getPlan(String name) throws BlueskyException {
		return api.getPlan(name);
	}

	@Override
	public List<Device> getDevices() throws BlueskyException {
		return api.getDevices();
	}

	@Override
	public Device getDevice(String name) throws BlueskyException {
		return api.getDevice(name);
	}

	@Override
	public String submitTask(Task task) throws BlueskyException {
		String response = api.submitTask(task);
		return api.setWorkerTask(response);
	}

	@Override
	public CompletableFuture<WorkerEvent> runTask(Task task) throws BlueskyException {
		final var done = waitForCompletionEvent();
		try {
			final String response = api.submitTask(task);
			api.setWorkerTask(response);
		} catch (ApiException e) {
			done.completeExceptionally(e);
		}
		return done;
	}

	@Override
	public WorkerState getWorkerState() throws BlueskyException {
		return api.getWorkerState();
	}

	@Override
	public void setWorkerState(WorkerState state) throws BlueskyException {
		api.setWorkerState(state);
	}

	public void pauseWorker() throws BlueskyException {
		api.setWorkerState(WorkerState.PAUSED);
	}

	public void resumeWorker() throws BlueskyException {
		api.setWorkerState(WorkerState.RUNNING);
	}

	@Override
	public CompletableFuture<Optional<WorkerEvent>> abort() throws BlueskyException {
		logger.info("Abort requested");
		final var done = waitForCompletionEvent();
		if (isWorkerRunning()) {
			setWorkerState(WorkerState.ABORTING);
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
			if (event.isComplete()) {
				if (event.isError()) {
					done.completeExceptionally(new BlueskyException(event));
				} else {
					done.complete(event);
				}
			}
		};
		addEventListener(WorkerEvent.class, listener);
		done.whenComplete((event, e) -> removeWorkerEventListener(listener));
		return done;
	}

	@Override
	public boolean isWorkerRunning() throws BlueskyException {
		return getWorkerState().isRunning();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
