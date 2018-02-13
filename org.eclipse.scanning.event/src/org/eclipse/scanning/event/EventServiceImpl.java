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

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IDisconnectable;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IQueueReader;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.remote.RemoteServiceFactory;

public class EventServiceImpl implements IEventService {

	static {
		System.out.println("Started "+IEventService.class.getSimpleName());
	}

	private static IEventConnectorService eventConnectorService;

	public EventServiceImpl() {

	}

	// For tests
	public EventServiceImpl(IEventConnectorService serviceToUse) {
		Objects.requireNonNull(serviceToUse);
		eventConnectorService = serviceToUse;
	}

	public static void setEventConnectorService(IEventConnectorService eventService) {
		EventServiceImpl.eventConnectorService = eventService;
	}

	@Override
	public <T extends EventListener> ISubscriber<T> createSubscriber(URI uri, String topicName) {
		return new SubscriberImpl<>(uri, topicName, eventConnectorService);
	}


	@Override
	public <U> IPublisher<U> createPublisher(URI uri, String topicName) {
		return new PublisherImpl<>(uri, topicName, eventConnectorService);
	}

	@Override
	public <U extends StatusBean> ISubmitter<U> createSubmitter(URI uri, String queueName) {
		SubmitterImpl<U> submitter = new SubmitterImpl<U>(uri, queueName, eventConnectorService, this);
		submitter.setStatusTopicName(EventConstants.STATUS_TOPIC); // They may always change it later.
		return submitter;
	}

	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri) throws EventException {
		return createConsumer(uri, SUBMISSION_QUEUE, STATUS_SET, STATUS_TOPIC, HEARTBEAT_TOPIC, CMD_TOPIC);
	}

	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri, String submissionQueueName,
			String statusQueueName, String statusTopicName) throws EventException {
		return createConsumer(uri, submissionQueueName, statusQueueName, statusTopicName, HEARTBEAT_TOPIC, CMD_TOPIC);
	}


	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri, String submissionQName, String statusQueueName,
			String statusTopicName, String heartbeatTopicName, String commandTopicName) throws EventException {
		return new ConsumerImpl<>(uri, submissionQName, statusQueueName, statusTopicName, heartbeatTopicName,
				commandTopicName, eventConnectorService, this);

	}

	@Override
	public void checkHeartbeat(URI uri, String patientName, long listenTime) throws EventException, InterruptedException {
		HeartbeatChecker checker = new HeartbeatChecker(this, uri, patientName, listenTime);
		checker.checkPulse();
	}

	@Override
	public <T extends INameable> void checkTopic(URI uri, String patientName, long listenTime, String topicName, Class<T> beanClass) throws EventException, InterruptedException {
		TopicChecker<T> checker = new TopicChecker<>(this, uri, patientName, listenTime, topicName, beanClass);
		checker.checkPulse();
	}

	@Override
	public <T extends IdBean> IRequester<T> createRequestor(URI uri, String requestTopic, String responseTopic) throws EventException {
		return new RequesterImpl<>(uri, requestTopic, responseTopic, this);
	}

	@Override
	public <T extends IdBean> IResponder<T> createResponder(URI uri, String requestTopic, String responseTopic) throws EventException {
		return new ResponderImpl<>(uri, requestTopic, responseTopic, this);
	}

	@Override
	public IEventConnectorService getEventConnectorService() {
		return eventConnectorService;
	}

	@Override
	public <T> IQueueReader<T> createQueueReader(URI uri, String queueName) {
		return new QueueReaderImpl<>(uri, queueName, this);
	}

	private Map<String, SoftReference<?>> cachedServices;

	@Override
	public synchronized <T> T createRemoteService(URI uri, Class<T> serviceClass) throws EventException {
		if (cachedServices==null) cachedServices = new HashMap<>(7);
		try {
			String key = ""+uri+serviceClass.getName();
			if (cachedServices.containsKey(key)) {
				@SuppressWarnings("unchecked")
				SoftReference<T> ref = (SoftReference<T>)cachedServices.get(key);
				if (ref.get()!=null) {
					T service = ref.get();
					if (service instanceof IDisconnectable) {
						IDisconnectable discon = (IDisconnectable)service;
						if (discon.isDisconnected()) {
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
			T service = RemoteServiceFactory.getRemoteService(uri, serviceClass, this);
			cachedServices.put(key, new SoftReference<T>(service));
			return service;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new EventException("There problem creating service for "+serviceClass, e);
		}
	}

}
