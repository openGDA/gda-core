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

package uk.ac.diamond.daq.bluesky.impl;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import gda.configuration.properties.LocalProperties;
import gda.util.JsonMessageConversionException;
import gda.util.JsonMessageConverter;
import gda.util.JsonMessageListener;
import uk.ac.diamond.daq.bluesky.api.BlueskyController;
import uk.ac.diamond.daq.bluesky.api.BlueskyException;
import uk.ac.diamond.daq.bluesky.api.DeviceRequest;
import uk.ac.diamond.daq.bluesky.api.DeviceResponse;
import uk.ac.diamond.daq.bluesky.api.PlanRequest;
import uk.ac.diamond.daq.bluesky.api.PlanResponse;
import uk.ac.diamond.daq.bluesky.api.Task;
import uk.ac.diamond.daq.bluesky.api.TaskResponse;
import uk.ac.diamond.daq.bluesky.api.WorkerEvent;

/**
 * {@link BlueskyController} That communicates with the worker via a JMS-friendly message bus.
 */
@Component
public class JmsBlueskyController implements BlueskyController {

	private static final Logger logger = LoggerFactory.getLogger(JmsBlueskyController.class);

	private JmsTemplate jmsTemplate;
	private Set<Consumer<WorkerEvent>> taskEventListeners;
	private JsonMessageConverter messageConverter;

	@Activate
	public void init() throws JMSException {
		final var connectionFactory = new ActiveMQConnectionFactory();
		final var activeMqUrl = LocalProperties.get(LocalProperties.GDA_ACTIVEMQ_BROKER_URI);
		connectionFactory.setBrokerURL(activeMqUrl);

        messageConverter = new JsonMessageConverter();
		jmsTemplate = new JmsTemplate(connectionFactory);
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
	public PlanResponse getPlans() throws BlueskyException {
		final var response = jmsTemplate.sendAndReceive(
				BlueskyDestinations.WORKER_PLANS,
				session -> toMessage(new PlanRequest(), session));
		try {
			return messageConverter.fromMessage(response, PlanResponse.class);
		} catch (JMSException | JsonMessageConversionException e) {
			throw new BlueskyException("Error while decoding message", e);
		}
	}

	@Override
	public DeviceResponse getDevices() throws BlueskyException {
		final var response = jmsTemplate.sendAndReceive(
				BlueskyDestinations.WORKER_DEVICES,
				session -> toMessage(new DeviceRequest(), session));
		try {
			return messageConverter.fromMessage(response, DeviceResponse.class);
		} catch (JMSException | JsonMessageConversionException e) {
			throw new BlueskyException("Error while decoding message", e);
		}
	}

	@Override
	public boolean addWorkerEventListener(Consumer<WorkerEvent> listener) {
		return taskEventListeners.add(listener);
	}

	@Override
	public boolean removeWorkerEventListener(Consumer<WorkerEvent> listener) {
		return taskEventListeners.remove(listener);
	}

	@Override
	public TaskResponse submitTask(Task task) throws BlueskyException {
		logger.info("Submitting new task to Bluesky: {}", task);
		final var response = jmsTemplate.sendAndReceive(
				BlueskyDestinations.WORKER_RUN,
				session -> toMessage(task, session));
		try {
			final var acknowledgement = messageConverter.fromMessage(response, TaskResponse.class);
			logger.info("New task acknowledged by Bluesky: {}", acknowledgement.taskName());
			return acknowledgement;
		} catch (JMSException | JsonMessageConversionException e) {
			throw new BlueskyException("Error while decoding message", e);
		}
	}

	private Message toMessage(Object object, Session session) throws JMSException {
		try {
			return messageConverter.toMessage(object, session);
		} catch (JsonMessageConversionException e) {
			throw new JMSException(e.getMessage());
		}
	}

	@Override
	public CompletableFuture<WorkerEvent> runTask(Task task) throws BlueskyException {
		final var done = new CompletableFuture<WorkerEvent>();
		final var taskIdFuture = new CompletableFuture<String>();
		final Consumer<WorkerEvent> listener = event -> {
			try {
				// This is far from the best way to check this, maybe we should consider
				// some sort of transactional API. It is a reasonable first step though.
				final var taskId = taskIdFuture.get();

				if (isComplete(event)) {
					if (isError(event)) {
						done.completeExceptionally(new BlueskyException(event));
					} else {
						done.complete(event);
					}
				}
			} catch (InterruptedException | ExecutionException e) {
				throw new IllegalStateException("Error occured while evaluating task ID", e);
			}
		};
		addWorkerEventListener(listener);
		final var taskResponse = submitTask(task);
		taskIdFuture.complete(taskResponse.taskName());
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
