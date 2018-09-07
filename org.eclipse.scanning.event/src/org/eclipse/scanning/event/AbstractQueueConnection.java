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
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IQueueConnection;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueueConnection<U extends StatusBean> extends AbstractReadOnlyQueueConnection<U> implements IQueueConnection<U> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractQueueConnection.class);

	AbstractQueueConnection(URI uri, String submitQueueName, String statusQueueName, String statusTopicName,
			IEventConnectorService service, IEventService eservice) {
		super(uri, submitQueueName, statusQueueName, statusTopicName, service, eservice);
	}

	@Override
	public void clearQueue() throws EventException {
		// we need to pause the consumer before we clear the submission queue
		final String queueName = getSubmitQueueName();
		doWhilePaused(() -> { doClearQueue(queueName); return null; });
	}

	@Override
	public void clearRunningAndCompleted() throws EventException {
		doClearQueue(getStatusSetName());
	}

	private void doClearQueue(String queueName) throws EventException {
		logger.info("Clearing queue {}", queueName);
		QueueConnection qCon = null;
		try {
			QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
			qCon  = connectionFactory.createQueueConnection(); // This times out when the server is not there.
			QueueSession    qSes  = qCon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue   = qSes.createQueue(queueName);
			qCon.start();

			QueueBrowser qb = qSes.createBrowser(queue);

			@SuppressWarnings("rawtypes")
			Enumeration  e  = qb.getEnumeration();
			while(e.hasMoreElements()) {
				Message msg = (Message)e.nextElement();
				MessageConsumer consumer = qSes.createConsumer(queue, "JMSMessageID = '" + msg.getJMSMessageID() + "'");
				Message rem = consumer.receive(EventTimingsHelper.getReceiveTimeout());
				if (rem != null) {
					logger.trace("Removed bean {}", rem);
				} else {
					logger.warn("Failed to remove next bean");
				}
				consumer.close();
			}
		} catch (Exception ne) {
			throw new EventException(ne);
		} finally {
			if (qCon!=null) {
				try {
					qCon.close();
				} catch (JMSException e) {
					logger.error("Cannot close connection to queue {}", queueName, e);
				}
			}
		}
	}

	protected abstract <T> T doWhilePaused(Callable<T> task) throws EventException;

	@Override
	public boolean reorder(U bean, int amount) throws EventException {
		if (amount==0) return false; // Nothing to reorder, no exception required, order unchanged.
		return doWhilePaused(() -> doReorder(bean, amount));
	}

	private boolean doReorder(U bean, int amount) throws EventException {
		// We are paused, read the queue
		List<U> submitted = getQueue();
		if (submitted==null || submitted.isEmpty())
			throw new EventException("There is nothing submitted waiting to be run\n\nPerhaps the job started to run.");

		Collections.reverse(submitted); // It comes out with the head at 0 and tail at size-1
		boolean found = false;
		int index = -1;
		for (U u : submitted) {
			index++;
			if (u.getUniqueId().equals(bean.getUniqueId())) {
				found=true;
				break;
			}
		}
		if (!found) throw new EventException("Cannot find bean '"+bean.getName()+"' in submission queue!\nIt might be running now.");

		if (index<1 && amount<0) throw new EventException("'"+bean.getName()+"' is already at the tail of the submission queue.");
		if (index+amount>submitted.size()-1) throw new EventException("'"+bean.getName()+"' is already at the head of the submission queue.");

		clearQueue();

		U existing = submitted.get(index);
		if (amount>0) {
			submitted.add(index+amount+1, existing);
			submitted.remove(index);
		} else {
			submitted.add(index+amount, existing);
			submitted.remove(index+1);
		}

		Collections.reverse(submitted); // It goes back with the head at 0 and tail at size-1

		try (ISubmitter<U> submitter = eventService.createSubmitter(getUri(), getSubmitQueueName())) {
			for (U u : submitted)
				submitter.submit(u);
		}

		return true; // It was reordered
	}

	@Override
	public boolean remove(U bean) throws EventException {
		return doWhilePaused(() -> doRemove(bean, getSubmitQueueName()));
	}

	@Override
	public void removeCompleted(U bean) throws EventException {
		doRemove(bean, getStatusSetName());
	}

	private boolean doRemove(U beanToRemove, String queueName) throws EventException {
		QueueConnection connection = null;
		QueueSession session = null;
		final QueueConnectionFactory connectionFactory = (QueueConnectionFactory) service.createConnectionFactory(uri);
		try {
			connection = connectionFactory.createQueueConnection(); // This times out when the server is not there.
			session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			final Queue queue = session.createQueue(queueName);
			connection.start();

			final QueueBrowser queueBrowser = session.createBrowser(queue);
			String jmsMessageId = null;
			@SuppressWarnings("rawtypes")
			final Enumeration e = queueBrowser.getEnumeration();

			while (jmsMessageId == null && e.hasMoreElements()) {
				Message message = (Message) e.nextElement();
				if (message instanceof TextMessage) {
					TextMessage textMsg = (TextMessage) message;

					final U beanFromQueue = service.unmarshal(textMsg.getText(), null);
					if (beanFromQueue != null && isForSameObject(beanFromQueue, beanToRemove)) {
						jmsMessageId = textMsg.getJMSMessageID();
					}
				}
			}

			queueBrowser.close();

			if (jmsMessageId != null) {
				MessageConsumer consumer = session.createConsumer(queue, "JMSMessageID = '" + jmsMessageId + "'");
				Message message = consumer.receive(1000);
				consumer.close();
				if (message == null) {
					// It might have been removed ok
					logger.warn("Could not remove bean (JMSMessageID={}) from queue {}. Bean: {}", jmsMessageId, queueName, beanToRemove);
					return false;
				} else {
					logger.info("Removed bean (JMSMessageId={} from queue", beanToRemove);
					return true;
				}
			} else {
				logger.warn("Could not find bean to remove in queue {}. Bean: {}", queueName, beanToRemove);
				return false;
			}
		} catch (Exception ne) {
			throw new EventException("Cannot remove item " + beanToRemove, ne);

		} finally {
			try {
				if (connection != null)
					connection.close();
				if (session != null)
					session.close();
			} catch (Exception e) {
				throw new EventException("Cannot close connection as expected!", e);
			}
		}
	}

	@Override
	public boolean replace(U bean) throws EventException {
		return doWhilePaused(() -> doReplace(bean));
	}

	private boolean doReplace(U bean) throws EventException {
		// We are paused, read the queue
		List<U> submitted = getQueue();
		if (submitted == null || submitted.isEmpty())
			throw new EventException("There is nothing submitted waiting to be run\n\nPerhaps the job started to run.");

		boolean found = false;
		for (int i = 0; i < submitted.size(); i++) {
			U u = submitted.get(i);
			if (u.getUniqueId().equals(bean.getUniqueId())) {
				found = true;
				submitted.set(i, bean);
				break;
			}
		}
		if (!found)
			throw new EventException("Cannot find bean '" + bean.getName() + "' in submission queue!\nIt might be running now.");

		clearQueue();

		try (ISubmitter<U> submitter = eventService.createSubmitter(getUri(), getSubmitQueueName())) {
			for (U u : submitted) {
				submitter.submit(u);
			}
		}
		return true; // It was reordered
	}

	public void cleanUpCompleted() throws EventException {
		try {
			QueueConnection qCon = null;

			try {
				QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
				qCon  = connectionFactory.createQueueConnection();
				QueueSession    qSes  = qCon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				Queue queue   = qSes.createQueue(getStatusSetName());
				qCon.start();

				QueueBrowser qb = qSes.createBrowser(queue);

				@SuppressWarnings("rawtypes")
				Enumeration  e  = qb.getEnumeration();

				Map<String, StatusBean> failIds = new LinkedHashMap<>();
				List<String>          removeIds = new ArrayList<>();
				while(e.hasMoreElements()) {
					Message m = (Message)e.nextElement();
					if (m==null) continue;
					if (m instanceof TextMessage) {
						TextMessage t = (TextMessage)m;

						try {
							final String     json  = t.getText();
							@SuppressWarnings("unchecked")
							final Class<U> statusBeanClass = (Class<U>) StatusBean.class;
							final StatusBean qbean = service.unmarshal(json, getBeanClass() != null ? getBeanClass() : statusBeanClass);
							if (qbean==null)               continue;
							if (qbean.getStatus()==null)   continue;
							if (!qbean.getStatus().isStarted() || qbean.getStatus()==Status.PAUSED) {
								failIds.put(t.getJMSMessageID(), qbean);
								continue;
							}

							// If it has failed, we clear it up
							if (qbean.getStatus()==Status.FAILED) {
								removeIds.add(t.getJMSMessageID());
								continue;
							}
							if (qbean.getStatus()==Status.NONE) {
								removeIds.add(t.getJMSMessageID());
								continue;
							}

							// If it is running and older than a certain time, we clear it up
							if (qbean.getStatus().isRunning()) {
								final long submitted = qbean.getSubmissionTime();
								final long current   = System.currentTimeMillis();
								if (current-submitted > EventTimingsHelper.getMaximumRunningAgeMs()) {
									removeIds.add(t.getJMSMessageID());
									continue;
								}
							}

							if (qbean.getStatus().isFinal()) {
								final long submitted = qbean.getSubmissionTime();
								final long current   = System.currentTimeMillis();
								if (current-submitted > EventTimingsHelper.getMaximumCompleteAgeMs()) {
									removeIds.add(t.getJMSMessageID());
								}
							}

						} catch (Exception ne) {
							logger.warn("Message "+t.getText()+" is not legal and will be removed.", ne);
							removeIds.add(t.getJMSMessageID());
						}
					}
				}

				// We fail the non-started jobs now - otherwise we could
				// actually start them late. TODO check this
				final List<String> ids = new ArrayList<>();
				ids.addAll(failIds.keySet());
				ids.addAll(removeIds);

				for (String jMSMessageID : ids) {
					MessageConsumer consumer = qSes.createConsumer(queue, "JMSMessageID = '"+jMSMessageID+"'");
					Message m = consumer.receive(EventTimingsHelper.getReceiveTimeout());
					consumer.close();
					if (removeIds.contains(jMSMessageID)) continue; // We are done

					if (m!=null && m instanceof TextMessage) {
						MessageProducer producer = qSes.createProducer(queue);
						final StatusBean    bean = failIds.get(jMSMessageID);
						bean.setStatus(Status.FAILED);
						producer.send(qSes.createTextMessage(service.marshal(bean)));

						logger.warn("Failed job {} messageid({})", bean.getName(), jMSMessageID);
					}
				}
			} finally {
				if (qCon!=null) qCon.close();
			}
		} catch (Exception ne) {
			throw new EventException("Problem connecting to "+statusQueueName+" in order to clean it!", ne);
		}
	}



}
