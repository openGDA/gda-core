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

import java.lang.reflect.Method;
import java.net.URI;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.core.IURIConnection;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractConnection implements IURIConnection {

	private static final Logger logger = LoggerFactory.getLogger(AbstractConnection.class);

	protected final URI uri;
	protected String topicName;

	// set the queue and topic names to their defaults
	protected String submitQueueName  = EventConstants.SUBMISSION_QUEUE;
	protected String statusQueueName  = EventConstants.STATUS_SET;
	protected String statusTopicName  = EventConstants.STATUS_TOPIC;
	protected String commandTopicName = EventConstants.CMD_TOPIC;

	protected IEventConnectorService service;

	protected QueueConnection connection;
	protected QueueSession queueSession;
	protected Session session;

	private boolean disconnected = false;

	AbstractConnection(URI uri, String topic, IEventConnectorService service) {
		this.uri = uri;
		this.topicName = topic;
		this.service = service;
	}

	AbstractConnection(URI uri, String submitQueueName, String statusQueueName, String statusTopicName,
			String commandTopicName, IEventConnectorService service) {
		this.uri = uri;
		this.submitQueueName = submitQueueName;
		this.statusQueueName = statusQueueName;
		this.statusTopicName = statusTopicName;
		this.commandTopicName = commandTopicName;
		this.service = service;
	}

	@Override
	public IEventConnectorService getConnectorService() {
		return service;
	}

	/**
	 * Creates and returns a topic of the given name
	 * @param topicName
	 * @return topic
	 * @throws JMSException
	 */
	protected Topic createTopic(String topicName) throws JMSException {
		if (connection==null) createConnection();
		if (session == null)  createSession();
		return session.createTopic(topicName);
	}

	/**
	 * Creates and returns a queue of the given name
	 * @param queueName
	 * @return
	 * @throws JMSException
	 */
	protected Queue createQueue(String queueName) throws JMSException {
		if (connection==null) createConnection();
		if (queueSession == null) createQueueSession();
		return queueSession.createQueue(queueName);
	}

	protected void createSession() throws JMSException {
		this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	private void createQueueSession() throws JMSException {
		this.queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	protected void createConnection() throws JMSException {
		Object factory = service.createConnectionFactory(uri);
		QueueConnectionFactory connectionFactory = (QueueConnectionFactory)factory;
		this.connection = connectionFactory.createQueueConnection();
		connection.start();
	}

	@Override
	public void disconnect() throws EventException {
		try {
			if (connection!=null) connection.close();
			if (session!=null) session.close();
			if (queueSession!=null) queueSession.close();
		} catch (JMSException ne) {
			logger.error("Internal error - unable to close connection!", ne);
		} finally {
			connection = null;
			session = null;
			queueSession = null;
		}
		setDisconnected(true);
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topic) {
		this.topicName = topic;
	}

	@Override
	public URI getUri() {
		return uri;
	}

	public String getSubmitQueueName() {
		return submitQueueName;
	}

	public void setSubmitQueueName(String submitQueueName) {
		this.submitQueueName = submitQueueName;
	}

	public String getStatusSetName() {
		return statusQueueName;
	}

	public void setStatusSetName(String statusQueueName) {
		this.statusQueueName = statusQueueName;
	}

	public String getStatusTopicName() {
		return statusTopicName;
	}

	public void setStatusTopicName(String statusTopicName) {
		this.statusTopicName = statusTopicName;
	}

	public String getCommandTopicName() {
		return commandTopicName;
	}

	public void setCommandTopicName(String commandTopicName) {
		this.commandTopicName = commandTopicName;
	}

	/**
	 * A utility method to test if a bean from the queue represents the same
	 * bean as that given . This is done by comparing their uniqueIds, if present,
	 * and falls back on using {@link Object#equals(Object)}
	 * @param qbean
	 * @param bean
	 * @return <code>true</code> if the two beans represent the same object,
	 *   <code>false</code> otherwise
	 */
	protected boolean isSame(Object qbean, Object bean) {
		Object id1 = getUniqueId(qbean);
		if (id1==null) return qbean.equals(bean); // Probably it won't because we are updating it but they might have transient fields.

		Object id2 = getUniqueId(bean);
		if (id2==null) return qbean.equals(bean); // Probably it won't because we are updating it but they might have transient fields.

		return id1.equals(id2);
	}

	private Object getUniqueId(Object bean) {
		if (bean instanceof StatusBean) {
			return ((StatusBean)bean).getUniqueId();
		}

		Object value = null;
		try {
			Method method = bean.getClass().getDeclaredMethod("getUniqueId");
			value = method.invoke(bean);
		} catch (Exception e) { // fall through
		}
		if (value == null) {
			try {
				Method method = bean.getClass().getDeclaredMethod("getName");
				value = method.invoke(bean);
			} catch (Exception e1) { // fall through
			}
		}

		return value;
	}

	@Override
	public boolean isDisconnected() {
		return disconnected;
	}

	private void setDisconnected(boolean disconnected) {
		this.disconnected = disconnected;
	}

}
