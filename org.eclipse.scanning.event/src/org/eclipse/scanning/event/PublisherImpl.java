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

import java.io.PrintStream;
import java.net.URI;
import java.util.Enumeration;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PublisherImpl<T> extends AbstractTopicConnection implements IPublisher<T> {

	private static final Logger logger = LoggerFactory.getLogger(PublisherImpl.class);

	private final long messageLifetime = Long.getLong("org.eclipse.scanning.event.publish.messageLifetime", 2000);

	// JMS things, these are null when not running and
	// are cleaned up at the end of a run.
	private MessageProducer messageProducer;
	private String statusSetName = null;

	private PrintStream out;

	public PublisherImpl(URI uri, String topic, IEventConnectorService service) {
		super(uri, topic, service);
	}

	@Override
	public synchronized void broadcast(T bean) throws EventException {
		try {
			if (messageProducer == null) {
				messageProducer = createProducer(getTopicName());
			}
			try {
				if (statusSetName != null) {
					updateStatusSet(bean);
				}
			} catch (Exception e) {
				logger.error("Could not update the status set", e);
			}

			send(messageProducer, bean, messageLifetime);
		} catch (JMSException ne) {
			throw new EventException("Unable to start the scan producer using uri " + uri + " and topic " + getTopicName(), ne);
		} catch (Exception neOther) {
			throw new EventException("Unable to prepare and send the event " + bean, neOther);
		}
	}

	private void send(MessageProducer producer, Object message, long messageLifetime)  throws Exception {
		int priority = message instanceof QueueCommandBean ? 8 : 4;

		String json = service.marshal(message);
		TextMessage msg = getSession().createTextMessage(json);

		producer.send(msg, DeliveryMode.NON_PERSISTENT, priority, messageLifetime);
		if (out!=null) out.println(json);
	}

	private boolean statusSetAddRequired = false;

	private MessageProducer createProducer(String topicName) throws JMSException {
		final Topic topic = createTopic(topicName);
		return getSession().createProducer(topic);
	}

	@Override
	public synchronized void disconnect() throws EventException {
		try {
			if (messageProducer!=null)      messageProducer.close();

			super.disconnect();

		} catch (JMSException ne) {
			throw new EventException("Internal error - unable to close connection!", ne);

		} finally {
			messageProducer = null;
		}
	}

	@Override
	public String getStatusSetName() {
		return statusSetName;
	}

	@Override
	public void setStatusSetName(String queueName) {
		this.statusSetName = queueName;
	}

	@Override
	public void setStatusSetAddRequired(boolean isRequired) {
		this.statusSetAddRequired  = isRequired;
	}

	/**
	 * Updates the given bean in the status set (a JMS queue). If {@link #setStatusSetAddRequired(boolean)}
	 * has been called with <code>true</code>, it is added to the status set if not present.
	 * @param bean bean to update
	 * @return <code>true</code> if the bean was updated or added, <code>false</code> otherwise
	 * @throws Exception
	 */
	private boolean updateStatusSet(T bean) throws Exception {
		final Queue queue = createQueue(getStatusSetName());
		// find the message id for the current bean
		String jMSMessageID = findMessageIdForBean(bean, queue);
		if (jMSMessageID != null) {
			boolean found = replaceBeanWithId(queue, jMSMessageID, bean);
			if (found) return true;
		}

		if (statusSetAddRequired) {
			// The bean was not found, so add it
			MessageProducer producer = getSession().createProducer(queue);
			try {
				producer.setDeliveryMode(DeliveryMode.PERSISTENT);
				String json = null;
				try {
					json = service.marshal(bean);
				} catch (Exception neother) {
					throw new EventException("Unable to marshall bean "+bean, neother);
				}

				TextMessage message = getSession().createTextMessage(json);
				producer.send(message);
			} finally {
				producer.close();
			}

			return true;
		}

		return false;
	}

	/**
	 * Finds the JMS message id for the given bean in the given queue,
	 * or <code>null</code> if none
	 * @param bean
	 * @param queue
	 * @return
	 * @throws JMSException
	 */
	private String findMessageIdForBean(T bean, final Queue queue) throws JMSException {
		final QueueBrowser qb = getQueueSession().createBrowser(queue);

		@SuppressWarnings("rawtypes")
		final Enumeration e = qb.getEnumeration();

		try {
			while (e.hasMoreElements()) {
				Message m = (Message)e.nextElement();
				if (m instanceof TextMessage) {
					TextMessage t = (TextMessage)m;

					final T qbean;
					try {
						@SuppressWarnings("unchecked")
						Class<T> beanClass = (Class<T>) bean.getClass();
						qbean = service.unmarshal(t.getText(), beanClass);
						if (isForSameObject(qbean, bean)) {
							return t.getJMSMessageID();
						}
					} catch (Exception ne) {
						// If we cannot deserialize to the type passed in, it certainly is
						// not going to be the bean which we are looking for.
					}
				}
			}
		} finally {
			qb.close();
		}

		return null;
	}

	private boolean replaceBeanWithId(Queue queue, String jmsMessageId, T bean) throws Exception {
		// consume the bean (removing it from the status set)
		MessageConsumer messageConsumer = getQueueSession().createConsumer(queue, "JMSMessageID = '"+jmsMessageId+"'");
		Message m = messageConsumer.receive(EventTimingsHelper.getReceiveTimeout());
		messageConsumer.close();
		if (m instanceof TextMessage) {
			MessageProducer producer = getQueueSession().createProducer(queue);
			try {
				TextMessage t = getQueueSession().createTextMessage(service.marshal(bean));
				t.setJMSMessageID(m.getJMSMessageID());
				t.setJMSExpiration(m.getJMSExpiration());
				t.setJMSTimestamp(m.getJMSTimestamp());
				t.setJMSPriority(m.getJMSPriority());
				t.setJMSCorrelationID(m.getJMSCorrelationID());

				producer.send(t);
			} finally {
				producer.close();
			}

			return true;
		}
		return false;
	}

	@Override
	public void setLoggingStream(PrintStream stream) {
		this.out = stream;
	}

}
