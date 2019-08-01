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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanClassListener;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queue.IQueueStatusBeanListener;
import org.eclipse.scanning.api.event.queue.QueueStatusBean;
import org.eclipse.scanning.api.event.queue.QueueStatusBeanEvent;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.scan.event.ILocationListener;
import org.eclipse.scanning.api.scan.event.Location;
import org.eclipse.scanning.api.scan.event.LocationEvent;
import org.eclipse.scanning.event.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
class SubscriberImpl<T extends EventListener> extends AbstractTopicConnection implements ISubscriber<T> {

	private static final Logger logger = LoggerFactory.getLogger(SubscriberImpl.class);

	private static final String DEFAULT_KEY = UUID.randomUUID().toString(); // Does not really matter what key is used for the default collection.

	private Map<String, Collection<T>> listeners; // Scan listeners
	private Map<Class, DisseminateHandler> handlers;
	private ExecutorService executor;

	private MessageConsumer messageConsumer;

	private boolean synchronous = true;

	private Class<?> beanClass;

	public SubscriberImpl(URI uri, String topic, IEventConnectorService service) {
		super(uri, topic, service);
		listeners = new ConcurrentHashMap<>(); // Concurrent overkill?
		handlers = createDisseminateHandlers();
	}

	@Override
	public void setTopicName(String topicName) {
		if (!listeners.isEmpty()) throw new IllegalStateException("Cannot set topic name once listeners have been added");
		super.setTopicName(topicName);
	}

	@Override
	public void addListener(T listener) throws EventException {
		addListener(DEFAULT_KEY, listener);
	}

	@Override
	public void addListener(String beanId, T listener) throws EventException {
		if (executor == null) {
			createExecutorService();
		}

		registerListener(beanId, listener);
		if (messageConsumer == null) {
			setBeanClass(getBeanClassForListener(listener));
			messageConsumer = createConsumer();
		}
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	private void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	private Class<?> getBeanClassForListener(T listener) {
		if (listener instanceof IBeanClassListener) {
			return ((IBeanClassListener) listener).getBeanClass();
		} else if (listener instanceof IScanListener) {
			return ScanBean.class;
		} else if (listener instanceof IQueueStatusBeanListener) {
			return QueueStatusBean.class;
		} else if (listener instanceof ILocationListener) {
			return Location.class;
		} else {
			throw new IllegalArgumentException("Unsupported listener type " + listener.getClass());
		}
	}

	private MessageConsumer createConsumer() throws EventException {
		try {
			final Topic topic = createTopic(getTopicName());

			final MessageConsumer consumer = getSession().createConsumer(topic);
			consumer.setMessageListener(this::handleMessage);
			return consumer;
		} catch (JMSException e) {
			throw new EventException("Cannot subscribe to topic "+getTopicName()+" with URI "+uri, e);
		}
	}

	private void handleMessage(Message message) {
		TextMessage txt = (TextMessage) message;
		try {
			String json = txt.getText();
			json = JsonUtil.removeProperties(json, properties);
			try {
				Object bean = service.unmarshal(json, getBeanClass());
				schedule(bean);
			} catch (Exception e) {
				// can't unmarshall this bean. This is only a warning not an error as we assume that
				// if we can't unmarshall a bean it's of a type we're not interested in
				logger.warn("Error processing message {} on topic {} with beanClass {}", message,
						getTopicName(), getBeanClass());
			}
		} catch (JMSException ne) {
			logger.error("Cannot get text from message " + txt, ne);
		}
	}

	private void schedule(Object bean) {
		if (executor != null) {
			executor.submit(() -> disseminate(bean));
		}
	}

	private void createExecutorService() {
		final ThreadFactory threadFactory = new ThreadFactory() {

			private final AtomicInteger threadNumber = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				final String name = getName();
				final Thread thread = new Thread(r, name);
				thread.setDaemon(true);
				thread.setPriority(Thread.NORM_PRIORITY + 1);
				return thread;
			}

			private String getName() {
				String name = getTopicName() + " subscriber";
				if (!synchronous) {
					name += ", thread " + threadNumber.getAndIncrement();
				}
				return name;
			}
		};

		if (synchronous) {
			executor = Executors.newSingleThreadExecutor(threadFactory);
		} else {
			executor = Executors.newCachedThreadPool(threadFactory);
		}
	}

	private void disseminate(Object bean) {
		if (!isConnected()) {
			logger.warn("Subscriber to topic {} disconnected - ignoring bean {}", getTopicName(), bean);
			return;
		}

		try {
			disseminate(bean, listeners.get(DEFAULT_KEY)); // general listeners
			if (bean instanceof IdBean) {
				IdBean idBean = (IdBean) bean;
				disseminate(bean, listeners.get(idBean.getUniqueId())); // bean specific listeners, if any
			} else if (bean instanceof INameable) {
				INameable namedBean = (INameable) bean;
				disseminate(bean, listeners.get(namedBean.getName())); // bean specific listeners, if any
			}
		} catch (Exception e) {
			logger.error("Could not disseminate bean {}", bean, e);
		}
	}

	private void disseminate(Object bean, Collection<T> listeners) {
		if (listeners==null || listeners.isEmpty()) return;
		final EventListener[] ls = listeners.toArray(new EventListener[listeners.size()]);

		for (EventListener listener : ls) {
			for (Entry<Class, DisseminateHandler> entry : handlers.entrySet()) {
				if (entry.getKey().isInstance(listener)) {
					entry.getValue().disseminate(bean, listener);
				}
			}
		}
	}

	private Map<Class, DisseminateHandler> createDisseminateHandlers() {
		final Map<Class, DisseminateHandler> ret = Collections.synchronizedMap(new HashMap<Class, DisseminateHandler>());

		ret.put(IScanListener.class, (bean, listener) ->
				invokeScanListener((IScanListener) listener, (ScanBean) bean));
		ret.put(IQueueStatusBeanListener.class, (bean, listener) ->
				((IQueueStatusBeanListener) listener).queueStatusChanged(new QueueStatusBeanEvent((QueueStatusBean) bean)));
		ret.put(IBeanListener.class, (bean, listener) ->
				((IBeanListener<Object>) listener).beanChangePerformed(new BeanEvent<Object>(bean)));
		ret.put(ILocationListener.class, (bean, listener) ->
				((ILocationListener) listener).locationPerformed(new LocationEvent((Location) bean)));

		return ret;
	}

	protected void invokeScanListener(IScanListener scanListener, ScanBean scanBean) {
		// check if device state has changed
		boolean isStateChange = false;
		final DeviceState currentState = scanBean.getDeviceState();
		final DeviceState previousState = scanBean.getPreviousDeviceState();
		if (currentState != null && currentState != previousState) {
			isStateChange = true;
		}

		// check if status has changed
		final Status currentStatus = scanBean.getStatus();
		final Status previousStatus = scanBean.getPreviousStatus();
		if (currentStatus != null && previousStatus != null && currentStatus != previousStatus) {
			isStateChange = true;
		}

		if (isStateChange) {
			scanListener.scanStateChanged(new ScanEvent(scanBean));
		} else {
			scanListener.scanEventPerformed(new ScanEvent(scanBean));
		}
	}

	/**
	 * A {@link DisseminateHandler} knows how to invoke the listener for a particular bean,
	 * i.e. what method to call and how to construct an event from the bean to be passed to that method.
	 */
	@FunctionalInterface
	private interface DisseminateHandler {
		public void disseminate(Object bean, EventListener listener);
	}

	private void registerListener(String key, T listener) {
		Collection<T> ls = listeners.get(key);
		if (ls == null) {
			ls = new LinkedHashSet<>();
			listeners.put(key, ls);
		}
		ls.add(listener);
	}

	@Override
	public void removeListener(T listener) {
		removeListener(DEFAULT_KEY, listener);
	}

	@Override
	public void removeListener(String id, T listener) {
		if (listeners.containsKey(id)) {
			listeners.get(id).remove(listener);
		}
	}

	@Override
	public void removeListeners(String id) {
		listeners.remove(id);
	}

	@Override
	public void removeAllListeners() {
		listeners.clear();
	}

	@Override
	public synchronized void disconnect() throws EventException {
		try {
			removeAllListeners();
			if (messageConsumer!=null) messageConsumer.close();
			if (executor != null) {
				executor.shutdownNow();
			}
			super.disconnect();
		} catch (JMSException ne) {
			throw new EventException("Internal error - unable to close connection!", ne);
		} finally {
			messageConsumer = null;
			executor = null;
		}
	}

	@Override
	public boolean isSynchronous() {
		return synchronous;
	}

	@Override
	public void setSynchronous(boolean synchronous) {
		this.synchronous = synchronous;
	}

	private List<String> properties;


	@Override
	public void addProperty(String name, FilterAction... actions) {
		if (Arrays.stream(actions).anyMatch(action -> action != FilterAction.DELETE)) {
			throw new IllegalArgumentException("It is only possible to remove properties from the subscribed json right now");
		}

		if (properties == null) properties = new ArrayList<>();
		properties.add(name);
	}

	@Override
	public void removeProperty(String name) {
		if (properties == null) return;
		properties.remove(name);
	}

	@Override
	public List<String> getProperties() {
		return properties;
	}
}
