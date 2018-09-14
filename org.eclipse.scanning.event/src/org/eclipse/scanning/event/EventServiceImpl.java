/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.event;

import static org.eclipse.scanning.api.event.EventConstants.ACK_TOPIC;
import static org.eclipse.scanning.api.event.EventConstants.CMD_TOPIC;
import static org.eclipse.scanning.api.event.EventConstants.HEARTBEAT_TOPIC;
import static org.eclipse.scanning.api.event.EventConstants.STATUS_SET;
import static org.eclipse.scanning.api.event.EventConstants.STATUS_TOPIC;
import static org.eclipse.scanning.api.event.EventConstants.SUBMISSION_QUEUE;

import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IQueueReader;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.remote.RemoteServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventServiceImpl implements IEventService {

	private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);

	private static IEventConnectorService eventConnectorService;

	public EventServiceImpl() {
		System.out.println("Started "+IEventService.class.getSimpleName());
	}

	// For tests
	public EventServiceImpl(IEventConnectorService serviceToUse) {
		Objects.requireNonNull(serviceToUse);
		eventConnectorService = serviceToUse;
	}

	public void setEventConnectorService(IEventConnectorService eventService) {
		logger.trace("setEventConnectorService({})", eventService);
		EventServiceImpl.eventConnectorService = eventService;
	}

	@Override
	public <T extends EventListener> ISubscriber<T> createSubscriber(URI uri, String topicName) {
		logger.trace("createSubscriber({}, {}) using {} and {}", uri, topicName, eventConnectorService, this);
		return new SubscriberImpl<>(uri, topicName, eventConnectorService);
	}


	@Override
	public <U> IPublisher<U> createPublisher(URI uri, String topicName) {
		logger.trace("createPublisher({}, {}) using {} and {}", uri, topicName, eventConnectorService, this);
		return new PublisherImpl<>(uri, topicName, eventConnectorService);
	}

	@Override
	public <U extends StatusBean> ISubmitter<U> createSubmitter(URI uri, String queueName) {
		logger.trace("createSubmitter({}, {}) using {} and {}", uri, queueName, eventConnectorService, this);
		SubmitterImpl<U> submitter = new SubmitterImpl<>(uri, queueName, eventConnectorService, this);
		submitter.setStatusTopicName(EventConstants.STATUS_TOPIC); // They may always change it later.
		return submitter;
	}

	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri) throws EventException {
		return createConsumer(uri, SUBMISSION_QUEUE, STATUS_SET, STATUS_TOPIC, HEARTBEAT_TOPIC, CMD_TOPIC, ACK_TOPIC);
	}

	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri, String submissionQueueName,
			String statusQueueName, String statusTopicName) throws EventException {
		return createConsumer(uri, submissionQueueName, statusQueueName, statusTopicName, HEARTBEAT_TOPIC, CMD_TOPIC, ACK_TOPIC);
	}


	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri, String submissionQName, String statusQueueName,
			String statusTopicName, String heartbeatTopicName, String commandTopicName, String commandAckTopicName) throws EventException {
		logger.trace("createConsumer({}, {}, {}, {}, {}, {}, {}) using {} and {}", uri, submissionQName, statusQueueName, statusTopicName,
				heartbeatTopicName, commandTopicName, commandAckTopicName, eventConnectorService, this);
		return new ConsumerImpl<>(uri, submissionQName, statusQueueName, statusTopicName, heartbeatTopicName,
				commandTopicName, commandAckTopicName, eventConnectorService, this);
	}

	@Override
	public <U extends StatusBean> IConsumer<U> createConsumerProxy(URI uri, String submissionQueueName)
			throws EventException {
		return createConsumerProxy(uri, submissionQueueName, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
	}

	@Override
	public <U extends StatusBean> IConsumer<U> createConsumerProxy(URI uri, String submissionQueueName,
			String commandTopicName, String commandAckTopicName) throws EventException {
		logger.trace("createConsumerProxy({}, {}, {}, {}) using {} and {}", uri, submissionQueueName,
				commandTopicName, commandAckTopicName, eventConnectorService, this);
		return new ConsumerProxy<>(uri, submissionQueueName, commandTopicName, commandAckTopicName, eventConnectorService, this);
	}

	@Override
	public void checkHeartbeat(URI uri, String patientName, long listenTime) throws EventException, InterruptedException {
		logger.trace("checkHeartbeat({}, {}, {}) using {} and {}", uri, patientName, listenTime, eventConnectorService, this);
		HeartbeatChecker checker = new HeartbeatChecker(this, uri, patientName, listenTime);
		checker.checkPulse();
	}

	@Override
	public <T extends IdBean> IRequester<T> createRequestor(URI uri, String requestTopic, String responseTopic) throws EventException {
		logger.trace("createRequestor({}, {}, {}) using {} and {}", uri, requestTopic, responseTopic, eventConnectorService, this);
		return new RequesterImpl<>(uri, requestTopic, responseTopic, this);
	}

	@Override
	public <T extends IdBean> IResponder<T> createResponder(URI uri, String requestTopic, String responseTopic) throws EventException {
		logger.trace("createResponder({}, {}, {}) using {} and {}", uri, requestTopic, responseTopic, eventConnectorService, this);
		return new ResponderImpl<>(uri, requestTopic, responseTopic, this);
	}

	@Override
	public IEventConnectorService getEventConnectorService() {
		return eventConnectorService;
	}

	@Override
	public <T> IQueueReader<T> createQueueReader(URI uri, String queueName) {
		logger.trace("createQueueReader({}, {}) using {} and {}", uri, queueName, eventConnectorService, this);
		return new QueueReaderImpl<>(uri, queueName, this);
	}

	private Map<String, SoftReference<?>> cachedServices;

	@Override
	public synchronized <T> T createRemoteService(URI uri, Class<T> serviceClass) throws EventException {
		logger.trace("createRemoteService({}, {}) using {} and {}", uri, serviceClass, eventConnectorService, this);

		T service = null;
		if (cachedServices==null) cachedServices = new HashMap<>(7);
		try {
			String key = ""+uri+serviceClass.getName();
			if (cachedServices.containsKey(key)) {
				@SuppressWarnings("unchecked")
				SoftReference<T> ref = (SoftReference<T>)cachedServices.get(key);
				if (ref.get()!=null) {
					service = ref.get();
					if (service instanceof IConnection) {
						IConnection connection = (IConnection)service;
						if (!connection.isConnected()) {
							cachedServices.remove(key);
							// Drop out of these tests and make a new service.
						} else {
							return service;
						}
					} else {
						return service;
					}
				}
			}
			service = RemoteServiceFactory.getRemoteService(uri, serviceClass, this);
			cachedServices.put(key, new SoftReference<T>(service));
			logger.trace("createRemoteService({}, {}) returning", uri, serviceClass);
			return service;
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("createRemoteService({}, {}) failed", uri, serviceClass, e);
			throw new EventException("There problem creating service for "+serviceClass, e);
		} finally {
			// Also log in finally so that we see unhandled runtime errors, and also so we can see how service
			logger.trace("createRemoteService({}, {}) service = ", uri, serviceClass, service); // init got.
		}
	}

}
