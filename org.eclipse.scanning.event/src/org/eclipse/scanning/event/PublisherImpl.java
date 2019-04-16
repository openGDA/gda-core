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

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.consumer.QueueCommandBean;
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
	public void setLoggingStream(PrintStream stream) {
		this.out = stream;
	}

}
