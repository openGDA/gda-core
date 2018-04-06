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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a Queue of json beans from the activemq queue and deserializes the
 * json beans to a specific class.
 *
 * @author Matthew Gerring
 *
 * @param <T>
 */
public final class QueueReader<T> {

	private static final Logger logger = LoggerFactory.getLogger(QueueReader.class);

	private final IEventConnectorService service;

	public QueueReader(IEventConnectorService service) {
		this.service    = service;
	}

	/**
	 * Read beans from any queue.
	 * Returns a list of optionally date-ordered beans in the queue.
	 *
	 * @param uri
	 * @param queueName
	 * @param beanClass bean class
	 * @return list of beans in the queue
	 * @throws EventException
	 */
	public List<T> getBeans(final URI uri, final String queueName, final Class<T> beanClass) throws EventException {
		final Instant startTime = Instant.now();

		QueueConnection connection = null;
		try {
			QueueConnectionFactory connectionFactory = (QueueConnectionFactory) service.createConnectionFactory(uri);
			connection = connectionFactory.createQueueConnection(); // This times out when the server is not there.
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(queueName);
			connection.start();

			QueueBrowser qb = session.createBrowser(queue);
			@SuppressWarnings("rawtypes")
			Enumeration e  = qb.getEnumeration();

			final List<T> beans = new ArrayList<>();

			while (e.hasMoreElements()) {
				Message m = (Message)e.nextElement();
				if (m instanceof TextMessage) {
					processMessage(beanClass, session, queue, beans, (TextMessage) m);
				}
			}
			return beans;
		} catch (JMSException e) {
			throw new EventException("Could not read beans from queue", e);
		} finally {
			closeConnection(connection);

			final Instant timeNow = Instant.now();

			if (Duration.between(startTime, timeNow).toMillis() > 100) {
				logger.warn("getBeans() took {}ms, called from {} (abridged)", Duration.between(startTime, timeNow).toMillis(),
						Arrays.stream(Thread.currentThread().getStackTrace()).skip(2).limit(4).collect(Collectors.toList()));
			}
		}
	}

	private void closeConnection(QueueConnection connection) throws EventException {
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				throw new EventException("Could not close queue", e);
			}
		}
	}

	private void processMessage(final Class<T> beanClass, QueueSession qSes, Queue queue, final Collection<T> list,
			TextMessage message) throws JMSException {
		String json   = message.getText();
		@SuppressWarnings("unchecked")
		final Class<T> statusBeanClass = (Class<T>) StatusBean.class;
		try {
			final T bean = service.unmarshal(json, beanClass != null ? beanClass : statusBeanClass);
			list.add(bean);
		} catch (Exception unmarshallable) {
			logger.error("Removing old message {}", json, unmarshallable);
			String jMSMessageID = message.getJMSMessageID();
			if (jMSMessageID != null) {
				MessageConsumer consumer = qSes.createConsumer(queue, "JMSMessageID = '"+jMSMessageID+"'");
				Message ignored = consumer.receive(1000);
				consumer.close();
				logger.trace("Removed {}", ignored.getJMSMessageID());
			}
		}
	}

}
