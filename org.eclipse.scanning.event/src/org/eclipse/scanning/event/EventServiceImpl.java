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
import static org.eclipse.scanning.api.event.EventConstants.QUEUE_STATUS_TOPIC;
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
import org.eclipse.scanning.api.event.core.IJmsQueueReader;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.IPublisher;
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

	private final Map<String, IJobQueue<? extends StatusBean>> jobQueues = new HashMap<>();

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
	public <U extends StatusBean> IJobQueue<U> createJobQueue(URI uri) throws EventException {
		return createJobQueue(uri, SUBMISSION_QUEUE, STATUS_TOPIC, QUEUE_STATUS_TOPIC, CMD_TOPIC, ACK_TOPIC);
	}

	@Override
	public <U extends StatusBean> IJobQueue<U> createJobQueue(URI uri, String submissionQueueName,
			String statusTopicName) throws EventException {
		return createJobQueue(uri, submissionQueueName, statusTopicName, QUEUE_STATUS_TOPIC, CMD_TOPIC, ACK_TOPIC);
	}


	@Override
	public <U extends StatusBean> IJobQueue<U> createJobQueue(URI uri, String submissionQName,
			String statusTopicName, String queueStatusTopicName, String commandTopicName, String commandAckTopicName) throws EventException {
		logger.trace("createJobQueue({}, {}, {}, {}, {}, {}) using {} and {}", uri, submissionQName, statusTopicName,
				queueStatusTopicName, commandTopicName, commandAckTopicName, eventConnectorService, this);
		if (jobQueues.containsKey(submissionQName)) {
			throw new EventException("A job queue for the queue name '" + submissionQName + "' has already been created!");
		}
		final IJobQueue<U> jobQueue = new JobQueueImpl<>(uri, submissionQName, statusTopicName, queueStatusTopicName, commandTopicName,
				commandAckTopicName, eventConnectorService, this);
		jobQueues.put(submissionQName, jobQueue);
		return jobQueue;
	}

	@Override
	public IJobQueue<? extends StatusBean> getJobQueue(String queueName) throws EventException {
		IJobQueue<? extends StatusBean> jobQueue = jobQueues.get(queueName);
		if (jobQueue == null) throw new EventException("No job queue exists for queue '" + queueName + "'");
		return jobQueue;
	}

	@Override
	public <U extends StatusBean> IJmsQueueReader<U> createJmsQueueReader(URI uri, String queueName) throws EventException {
		return new JmsQueueReader<>(uri, this, queueName);
	}

	@Override
	public <U extends StatusBean> IJobQueue<U> createJobQueueProxy(URI uri, String submissionQueueName)
			throws EventException {
		return createJobQueueProxy(uri, submissionQueueName, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
	}

	@Override
	public <U extends StatusBean> IJobQueue<U> createJobQueueProxy(URI uri, String submissionQueueName,
			String commandTopicName, String commandAckTopicName) throws EventException {
		logger.trace("createJobQueueProxy({}, {}, {}, {}) using {} and {}", uri, submissionQueueName,
				commandTopicName, commandAckTopicName, eventConnectorService, this);
		return new JobQueueProxy<>(uri, submissionQueueName, commandTopicName, commandAckTopicName, eventConnectorService, this);
	}

	@Override
	public void disposeJobQueue() throws EventException {
		for (IJobQueue<? extends StatusBean> jobQueue : jobQueues.values()) {
			jobQueue.disconnect();
		}
		jobQueues.clear();
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
