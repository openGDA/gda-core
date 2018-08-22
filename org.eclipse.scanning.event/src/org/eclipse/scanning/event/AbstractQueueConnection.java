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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IQueueConnection;
import org.eclipse.scanning.api.event.core.IQueueReader;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueueConnection<U extends StatusBean> extends AbstractConnection implements IQueueConnection<U>{

	private static final Logger logger = LoggerFactory.getLogger(AbstractQueueConnection.class);

	protected IEventService eservice;

	private Class<U> beanClass;

	AbstractQueueConnection(URI uri, String topic, IEventConnectorService service, IEventService eservice) {
		super(uri, topic, service);
		this.eservice = eservice;
	}

	AbstractQueueConnection(URI uri, String submitQueueName, String statusQueueName, String statusTopicName,
			String commandTopicName, IEventConnectorService service, IEventService eservice) {
		super(uri, submitQueueName, statusQueueName, statusTopicName, commandTopicName, service);
		this.eservice = eservice;
	}

	@Override
	public Class<U> getBeanClass() {
		return beanClass;
	}

	@Override
	public void setBeanClass(Class<U> beanClass) {
		this.beanClass = beanClass;
	}

	@Override
	public List<U> getQueue() throws EventException {
		return getQueue(getSubmitQueueName());
	}

	@Override
	public List<U> getQueue(String queueName) throws EventException {
		IQueueReader<U> reader = eservice.createQueueReader(uri, queueName);
		reader.setBeanClass(beanClass);
		return reader.getQueue();
	}

	protected Map<String, U> getMap(String queueName) throws EventException {

		final List<U> queue = getQueue(queueName);
		if (queue==null || queue.isEmpty()) return null;
		final HashMap<String, U> id = new HashMap<>(queue.size());
		for (U u : queue) id.put(u.getUniqueId(), u);
		return id;
	}

	public void clearQueue(String queueName) throws EventException {
		logger.info("Clearing queue {}", queueName);
		final String pauseMessage = "Pause to clear queue '" + queueName + "' ";
		doWhilePaused(queueName, pauseMessage, () -> doClearQueue(queueName));
	}

	private boolean doClearQueue(String queueName) throws EventException {
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
			return true;
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

	/**
	 * Performs a task while the queue is paused.
	 * TODO: <em>This method is massively flawed. It sends a {@link PauseBean} to pause the queue
	 * and then immediately perform the task without waiting for confirmation of any kind.
	 * This will most likely be performed before the queue is actually paused on the consuemer as the
	 * bean has to be sent over ActiveMQ (involving serializing and deserializing it) before the queue is actually paused.
	 * @param queueName
	 * @param pauseMessage
	 * @param task
	 * @return
	 * @throws EventException
	 */
	protected <T> T doWhilePaused(String queueName, String pauseMessage, Callable<T> task) throws EventException {
		IPublisher<QueueCommandBean> publisher = createPausePublisher();
		QueueCommandBean pauseBean = null;
		if (!isQueuePaused(queueName)) {
			pauseBean = new QueueCommandBean(queueName, Command.PAUSE);
			pauseBean.setMessage(pauseMessage);
		}

		// create a pause bean
		try {
			// send the pause bean if we're not already paused, to signal the queue consumer to pause
			if (pauseBean != null) {
				logger.info("Sending pause request for queue {}", queueName);
				publisher.broadcast(pauseBean);
			}

			return task.call();
		} catch (EventException e) {
			throw e;
		} catch (Exception e) {
			throw new EventException(e);
		} finally {
			// if we were paused, send a PauseBean with pause=false to signal the queue consumer to resume
			if (pauseBean != null) {
				logger.info("Sending resume request for queue {}", queueName);
				QueueCommandBean resumeBean = new QueueCommandBean(queueName, Command.RESUME);
				publisher.broadcast(resumeBean);
			}
			// disconnect the publisher
			publisher.disconnect();
		}
	}

	protected boolean reorder(U bean, String queueName, int amount) throws EventException {
		if (amount==0) return false; // Nothing to reorder, no exception required, order unchanged.

		final String pauseMessage = "Pause to reorder '" + bean.getName() + "' " + amount;
		return doWhilePaused(queueName, pauseMessage, () -> doReorder(bean, queueName, amount));
	}

	private boolean doReorder(U bean, String queueName, int amount) throws EventException {
		// We are paused, read the queue
		List<U> submitted = getQueue(queueName);
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

		clearQueue(queueName);

		U existing = submitted.get(index);
		if (amount>0) {
			submitted.add(index+amount+1, existing);
			submitted.remove(index);
		} else {
			submitted.add(index+amount, existing);
			submitted.remove(index+1);
		}

		Collections.reverse(submitted); // It goes back with the head at 0 and tail at size-1

		try (ISubmitter<U> submitter = eservice.createSubmitter(getUri(), queueName)) {
			for (U u : submitted)
				submitter.submit(u);
		}

		return true; // It was reordered
	}

	private IPublisher<QueueCommandBean> createPausePublisher() {
		IPublisher<QueueCommandBean> publisher = eservice.createPublisher(getUri(), EventConstants.CMD_TOPIC);
		publisher.setStatusSetName(EventConstants.CMD_SET);
		publisher.setStatusSetAddRequired(true);
		return publisher;
	}

	protected boolean remove(U bean, String queueName) throws EventException {
		final String pauseMessage = "Pause to remove '"+bean.getName()+"' ";
		return doWhilePaused(queueName, pauseMessage, () -> doRemove(bean, queueName));
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

	protected boolean replace(U bean, String queueName) throws EventException {
		final String pauseMessage = "Pause to replace '"+bean.getName()+"' ";
		return doWhilePaused(queueName, pauseMessage, () -> doReplace(bean, queueName));
	}

	private boolean doReplace(U bean, String queueName) throws EventException {
		// We are paused, read the queue
		List<U> submitted = getQueue(queueName);
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

		clearQueue(queueName);

		try (ISubmitter<U> submitter = eservice.createSubmitter(getUri(), queueName)) {
			for (U u : submitted) {
				submitter.submit(u);
			}
		}
		return true; // It was reordered
	}


	public boolean isQueuePaused(String submissionQueueName) {
		QueueCommandBean bean = getLastPauseResumeBean(submissionQueueName);
		return bean != null && bean.getCommand() == Command.PAUSE;
	}

	/**
	 * Finds and returns the most recent pause bean sent to the command queue.
	 * @param submissionQueueName name of submission queue
	 * @return pause bean
	 */
	protected QueueCommandBean getLastPauseResumeBean(String submissionQueueName) {
		IQueueReader<QueueCommandBean>   queueReader=null;
		try {
			queueReader = eservice.createQueueReader(getUri(), EventConstants.CMD_SET);
			queueReader.setBeanClass(QueueCommandBean.class);
		    List<QueueCommandBean> commandQueue = queueReader.getQueue();

		    // The most recent bean in the queue is the latest
		    for (QueueCommandBean commandBean : commandQueue) {
		    	Command command = commandBean.getCommand();
				if (submissionQueueName.equals(commandBean.getQueueName())
						&& (command == Command.PAUSE || command == Command.RESUME)) {
					return commandBean;
				}
			}
		} catch (Exception ne) {
			logger.error("Cannot get queue "+EventConstants.CMD_SET, ne);
			return null;
		} finally {
			try {
				if (queueReader!=null) queueReader.disconnect();
			} catch (EventException e) {
				logger.error("Cannot get disconnect "+EventConstants.CMD_SET, e);
			}
		}
		return null;
	}

}
