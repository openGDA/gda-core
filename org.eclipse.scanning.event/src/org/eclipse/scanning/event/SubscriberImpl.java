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
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.commons.lang.ClassUtils;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.HeartbeatEvent;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanClassListener;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
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
class SubscriberImpl<T extends EventListener> extends AbstractConnection implements ISubscriber<T> {

	private static final Logger logger = LoggerFactory.getLogger(SubscriberImpl.class);

	private static final String DEFAULT_KEY = UUID.randomUUID().toString(); // Does not really matter what key is used for the default collection.

	private Map<String, Collection<T>> listeners; // Scan listeners
	private Map<Class, DiseminateHandler> handlers;
	private BlockingQueue<DiseminateEvent> queue;

	private MessageConsumer messageConsumer;

	private boolean synchronous = true;

	public SubscriberImpl(URI uri, String topic, IEventConnectorService service) {
		super(uri, topic, service);
		listeners = new ConcurrentHashMap<>(); // Concurrent overkill?
		handlers = createDiseminateHandlers();
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
	public void addListener(String scanID, T listener) throws EventException{
		setConnected(true);
		if (isSynchronous()) createDiseminateThread();
		registerListener(scanID, listener);
		if (messageConsumer == null) {
			try {
				Class<?> beanClass = listener instanceof IBeanClassListener ? ((IBeanClassListener)listener).getBeanClass() : null;
				messageConsumer = createConsumer(getTopicName(), beanClass);
			} catch (JMSException e) {
				throw new EventException("Cannot subscribe to topic "+getTopicName()+" with URI "+uri, e);
			}
		}
	}

	private MessageConsumer createConsumer(final String topicName, final Class<?> beanClass) throws JMSException {
		final Topic topic = createTopic(topicName);

		final MessageConsumer consumer = session.createConsumer(topic);
		consumer.setMessageListener(message -> handleMessage(topicName, beanClass, message));
		return consumer;
	}

	private void handleMessage(final String topicName, final Class<?> beanClass, Message message) {
		TextMessage txt = (TextMessage) message;
		try {
			String json = txt.getText();
			json = JsonUtil.removeProperties(json, properties);
			try {
				Object bean = service.unmarshal(json, beanClass);
				schedule(new DiseminateEvent(bean));
			} catch (Exception ne) {
				logger.error("Error processing message {} on topic {} with beanClass {}", message, topicName,
						beanClass, ne);
				ne.printStackTrace(); // Unit tests without log4j config show this one.
			}
		} catch (JMSException ne) {
			logger.error("Cannot get text from message " + txt, ne);
		}
	}

	private void schedule(DiseminateEvent event) {
		if (isSynchronous()) {
			if (queue!=null) queue.add(event);
		} else {
			if (event==DiseminateEvent.STOP) return;
			// TODO FIXME Might not be right...
			final Thread thread = new Thread("Execute event "+getTopicName()) {
				@Override
				public void run() {
					diseminate(event); // Use this JMS thread directly to do work.
				}
			};
			thread.setDaemon(true);
			thread.setPriority(Thread.NORM_PRIORITY+1);
			thread.start();
		}
	}

	private void createDiseminateThread() {
		if (!isSynchronous()) return; // If asynch we do not run events in order and wait until they return.
		if (queue!=null) return;
		queue = new LinkedBlockingQueue<>(); // Small, if they do work and things back-up, exceptions will occur.

		final Thread despatcher = new Thread(new Runnable() {
			@Override
			public void run() {
				while(isConnected()) {
					try {
						DiseminateEvent event = queue.take();
						if (event==DiseminateEvent.STOP) return;
						diseminate(event);
					} catch (RuntimeException e) {
						e.printStackTrace();
						logger.error("RuntimeException occured despatching event", e);
						continue;
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("Stopping event despatch thread ", e);
						return;
					}
				}
				System.out.println(Thread.currentThread().getName()+" disconnecting events.");
			}
		}, "Subscriber despatch thread "+ getTopicName());
		despatcher.setName("Subscriber despatcher");
		despatcher.setDaemon(true);
		despatcher.setPriority(Thread.NORM_PRIORITY+1);
		despatcher.start();
	}


	private static final class DiseminateEvent {

		public static final DiseminateEvent STOP = new DiseminateEvent("STOP");

		protected final Object bean;

		public DiseminateEvent(Object bean) {
			this.bean = bean;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bean == null) ? 0 : bean.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DiseminateEvent other = (DiseminateEvent) obj;
			if (bean == null) {
				if (other.bean != null)
					return false;
			} else if (!bean.equals(other.bean))
				return false;
			return true;
		}

	}

	private void diseminate(DiseminateEvent event) {
		Object bean = event.bean;
		diseminate(bean, listeners.get(DEFAULT_KEY));  // general listeners
		if (bean instanceof IdBean) {
			IdBean idBean = (IdBean)bean;
			diseminate(bean, listeners.get(idBean.getUniqueId())); // scan specific listeners, if any
		} else if (bean instanceof INameable) {
			INameable namedBean = (INameable)bean;
			diseminate(bean, listeners.get(namedBean.getName())); // scan specific listeners, if any
		}
	}

	private boolean diseminate(Object bean, Collection<T> listeners) {
		if (listeners==null || listeners.isEmpty()) return false;
		final EventListener[] ls = listeners.toArray(new EventListener[listeners.size()]);

		boolean ret = true;
		for (EventListener listener : ls) {
			List<Class<?>> types = getAllInterfaces(listener.getClass());
			boolean diseminated = false;
			for (Class<?> type : types) {
				DiseminateHandler handler = handlers.get(type);
				if (handler != null) {
					handler.diseminate(bean, listener);
					diseminated = true;
				}
			}
			ret =  ret && diseminated;
		}
		return ret;
	}

	private Map<Class<? extends EventListener>,List<Class<?>>> interfaces;

	/**
	 * Important to cache the interfaces. Getting them caused a bug where scannable
	 * values were slow to transmit to the client during a scan.
	 *
	 * @param class1
	 * @return
	 */
	private List<Class<?>> getAllInterfaces(Class<? extends EventListener> class1) {
		if (interfaces==null) interfaces = new HashMap<>();
		if (!interfaces.containsKey(class1)) {
			interfaces.put(class1, ClassUtils.getAllInterfaces(class1));
		}
		return interfaces.get(class1);
	}

	private Map<Class, DiseminateHandler> createDiseminateHandlers() {

		Map<Class, DiseminateHandler> ret = Collections.synchronizedMap(new HashMap<Class, DiseminateHandler>(3));

		ret.put(IScanListener.class, new DiseminateHandler() {
			@Override
			public void diseminate(Object bean, EventListener e) {

				if (!(bean instanceof ScanBean)) return;
				// This listener must be used with events publishing ScanBean
				// If your scan does not publish ScanBean events then you
				// may listen to it with a standard IBeanListener.

				// Used casting because generics got silly
				ScanBean sbean  = (ScanBean)bean;
				IScanListener l = (IScanListener)e;

				DeviceState now = sbean.getDeviceState();
				DeviceState was = sbean.getPreviousDeviceState();
				if (now != null && now != was) {
					execute(new DespatchEvent(l, new ScanEvent(sbean), true));
					return;
				} else {
					Status snow = sbean.getStatus();
					Status swas = sbean.getPreviousStatus();
					if (snow!=null && snow!=swas && swas!=null) {
						execute(new DespatchEvent(l, new ScanEvent(sbean), true));
						return;
					}
				}
				execute(new DespatchEvent(l, new ScanEvent(sbean), false));
			}
		});
		ret.put(IHeartbeatListener.class, new DiseminateHandler() {
			@Override
			public void diseminate(Object bean, EventListener e) {
				// Used casting because generics got silly
				HeartbeatBean hbean = (HeartbeatBean)bean;
				IHeartbeatListener l= (IHeartbeatListener)e;
				execute(new DespatchEvent(l, new HeartbeatEvent(hbean)));
			}
		});
		ret.put(IBeanListener.class, new DiseminateHandler() {
			@Override
			public void diseminate(Object bean, EventListener e) {
				// Used casting because generics got silly
				@SuppressWarnings("unchecked")
				IBeanListener<Object> l = (IBeanListener<Object>)e;
				execute(new DespatchEvent(l, new BeanEvent<Object>(bean)));
			}
		});
		ret.put(ILocationListener.class, new DiseminateHandler() {
			@Override
			public void diseminate(Object bean, EventListener e) {
				// Used casting because generics got silly
				ILocationListener l = (ILocationListener)e;
				execute(new DespatchEvent(l, new LocationEvent((Location)bean)));
			}
		});

		return ret;
	}

	@FunctionalInterface
	private interface DiseminateHandler {
		public void diseminate(Object bean, EventListener listener) throws ClassCastException;
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
	public void disconnect() throws EventException {
		try {
			removeAllListeners();
			if (messageConsumer!=null)     messageConsumer.close();

			super.disconnect();

		} catch (JMSException ne) {
			throw new EventException("Internal error - unable to close connection!", ne);

		} finally {
			messageConsumer = null;
			setConnected(false);
		}
		super.disconnect();
		schedule(DiseminateEvent.STOP);
	}

	private boolean connected;

	private void execute(DespatchEvent event) {
		if (event.listener instanceof IHeartbeatListener) ((IHeartbeatListener)event.listener).heartbeatPerformed((HeartbeatEvent)event.object);
		if (event.listener instanceof IBeanListener)      ((IBeanListener)event.listener).beanChangePerformed((BeanEvent)event.object);
		if (event.listener instanceof ILocationListener)  ((ILocationListener)event.listener).locationPerformed((LocationEvent)event.object);
		if (event.listener instanceof IScanListener){
			IScanListener l = (IScanListener)event.listener;
			ScanEvent     e = (ScanEvent)event.object;
			if (event.isStateChange()) {
				l.scanStateChanged(e);
			} else {
				l.scanEventPerformed(e);
			}
		}
	}

	/**
	 * Immutable event for queue.
	 *
	 * @author fcp94556
	 *
	 */
	private static class DespatchEvent {

		protected final EventListener listener;
		protected final EventObject   object;
		protected final boolean       isStateChange;

		public DespatchEvent(EventListener listener, EventObject object) {
			this(listener, object, false);
		}
		public DespatchEvent(EventListener listener, EventObject object, boolean isStateChange) {
			this.listener = listener;
			this.object   = object;
			this.isStateChange = isStateChange;
		}
		public boolean isStateChange() {
			return isStateChange;
		}

	}

	public boolean isConnected() {
		return connected;
	}

	private void setConnected(boolean connected) {
		this.connected = connected;
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
