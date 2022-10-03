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
import javax.jms.QueueSession;
import javax.jms.Session;

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

	// set the queue and topic names to their defaults
	protected String submitQueueName  = EventConstants.SUBMISSION_QUEUE;
	protected String statusTopicName  = EventConstants.STATUS_TOPIC;

	protected IEventConnectorService service;

	private Session session;
	private QueueSession queueSession;
	private boolean connected = true;

	AbstractConnection(URI uri, IEventConnectorService service) {
		this.uri = uri;
		this.service = service;
	}

	AbstractConnection(URI uri, String submitQueueName, IEventConnectorService service) {
		this(uri, service);
		this.submitQueueName = submitQueueName;
	}

	AbstractConnection(URI uri, String submitQueueName, String statusTopicName, IEventConnectorService service) {
		this(uri, service);
		this.submitQueueName = submitQueueName;
		this.statusTopicName = statusTopicName;
	}

	@Override
	public IEventConnectorService getConnectorService() {
		return service;
	}

	protected synchronized Session getSession() throws JMSException {
		if (session == null) session = service.getSessionService().getSession(uri.toString(), false, Session.AUTO_ACKNOWLEDGE);
		return session;
	}

	protected synchronized QueueSession getQueueSession() throws JMSException {
		if (queueSession == null) queueSession = service.getSessionService().getQueueSession(uri.toString(), false, Session.AUTO_ACKNOWLEDGE);
		return queueSession;
	}

	/**
	 * Creates and returns a queue of the given name
	 * @param queueName
	 * @return
	 * @throws JMSException
	 */
	protected synchronized Queue createQueue(String queueName) throws JMSException {
		return getQueueSession().createQueue(queueName);
	}

	@Override
	public synchronized void disconnect() throws EventException {
		try {
			if (session != null) session.close();
			if (queueSession!=null) queueSession.close();
		} catch (JMSException ne) {
			logger.error("Internal error - unable to close connection!", ne);
		} finally {
			session = null;
			queueSession = null;
		}
		setConnected(false);
	}

	@Override
	public URI getUri() {
		return uri;
	}

	public String getSubmitQueueName() {
		return submitQueueName;
	}

	public String getStatusTopicName() {
		return statusTopicName;
	}

	public void setStatusTopicName(String statusTopicName) {
		this.statusTopicName = statusTopicName;
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
	protected boolean isForSameObject(Object qbean, Object bean) {
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
	public boolean isConnected() {
		return connected;
	}

	protected void setConnected(boolean connected) {
		this.connected = connected;
	}

}
