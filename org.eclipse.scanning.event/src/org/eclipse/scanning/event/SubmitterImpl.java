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
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SubmitterImpl<T extends StatusBean> extends AbstractReadOnlyQueueConnection<T> implements ISubmitter<T> {

	private static final Logger logger = LoggerFactory.getLogger(SubmitterImpl.class);

	// Properties to set for each message
	private int priority = 4; // Default priority is 4, see javadoc for MessageProducer.setPriority()
	private long lifeTime = TimeUnit.DAYS.toMillis(7); // default message time to live of 7 days

	SubmitterImpl(URI uri, String submitQueue, IEventConnectorService service, IEventService eservice) {
		super(uri, service, eservice);
		setSubmitQueueName(submitQueue);
	}

	private MessageProducer queueMessageProducer = null;
	private MessageProducer topicMessageProducer = null;

	private MessageProducer getQueueMessageProducer() throws JMSException {
		if (queueMessageProducer == null) {
			final Queue queue = createQueue(getSubmitQueueName());

			queueMessageProducer = getSession().createProducer(queue);
			queueMessageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
			queueMessageProducer.setPriority(priority);
			queueMessageProducer.setTimeToLive(lifeTime);
		}

		return queueMessageProducer;
	}

	private MessageProducer getTopicMessageProducer() throws JMSException {
		if (topicMessageProducer == null) {
			final Session session = getSession();
			final Topic topic = session.createTopic(getStatusTopicName());
			topicMessageProducer = session.createProducer(topic);
		}

		return topicMessageProducer;
	}

	@Override
	public void submit(T bean) throws EventException {
		try {
			logger.trace("submitting to queue {}: {}", getSubmitQueueName(), bean);

			if (bean.getSubmissionTime()<1) bean.setSubmissionTime(System.currentTimeMillis());
			if (bean.getUserName()==null) bean.setUserName(System.getProperty("user.name"));
			if (bean.getUniqueId()==null) bean.setUniqueId(UUID.randomUUID().toString());

			String json = null;
			try {
				json = service.marshal(bean);
			} catch (Exception e) {
				throw new EventException("Unable to marshall bean "+bean, e);
			}

			TextMessage message = getSession().createTextMessage(json);

			message.setJMSMessageID(bean.getUniqueId());
			message.setJMSExpiration(getLifeTime());
			getQueueMessageProducer().send(message);

			// Deals with paused consumers by publishing something directly after submission.
			// If there is a topic we tell everyone that we sent something to it in case the consumer is paused.
			if (getStatusTopicName() != null) {
				publishToStatusTopic(json);
			}

			logger.trace("submit({}) completed, closing...", bean);
		} catch (Exception e) {
			throw new EventException("Could not submit bean to queue " + getSubmitQueueName(), e);
		}
	}

	@Override
	public synchronized void disconnect() throws EventException{
		// Close and nullify the producer
		if (queueMessageProducer != null) {
			try {
				queueMessageProducer.close();
			} catch (JMSException e) {
				logger.error("Could not close messsage producer for queue " + getSubmitQueueName(), e);
			} finally {
				queueMessageProducer = null;
			}
		}

		super.disconnect();
	}

	/**
	 * Publishes the given message to the status topic.
	 * @param json
	 */
	private void publishToStatusTopic(String json) {
		try {
			final Session session = getSession();
			final MessageProducer producer = getTopicMessageProducer();
			final TextMessage msg = session.createTextMessage(json);
			producer.send(msg);
		} catch (JMSException e) {
			logger.error("Problem publishing to " + getStatusTopicName(), e);
		}
	}

	@Override
	public void blockingSubmit(T bean) throws EventException, InterruptedException, IllegalStateException {
		logger.trace("blockingSubmit(...)"); // Call to submit details the bean so no need to duplicate here

		String topic = getStatusTopicName();
		if (topic == null) {
			// We can't block if we don't know what topic to listen to.
			throw new IllegalStateException(
					"ISubmitter topicName must be set before blockingSubmit can be called.");
		}

		// We will listen for an event whose UID matches this bean
		// and which signals the scan is complete.
		ISubscriber<IScanListener> subscriber = eventService.createSubscriber(getUri(), topic);
		final String UID = bean.getUniqueId();
		final CountDownLatch latch = new CountDownLatch(1);

		subscriber.addListener(new IScanListener() {

			@Override
			public void scanStateChanged(ScanEvent evt) {
				ScanBean scanBean = evt.getBean();
				if (scanBean.getUniqueId().equals(UID)
						&& scanBean.getStatus().isFinal()) {
					latch.countDown();
				}
			}

			@Override
			public void scanEventPerformed(ScanEvent evt) {
				// We should only have to listen for state changes
				// but testing shows that we need to listen to all
				// scan events, like so. FIXME
				scanStateChanged(evt);
			}
		});

		submit(bean);
		latch.await();
		logger.trace("blockingSubmit({}) subscriber latch released. {}", bean, subscriber);
		subscriber.disconnect();
		logger.trace("blockingSubmit({}) subscriber disconnected.   {}", bean, subscriber);
	}

	@Override
	public int getPriority() {
		return priority;
	}


	@Override
	public void setPriority(int priority) {
		if (priority < 0) throw new IllegalArgumentException("Priority must be at least 0");
		this.priority = priority;
	}


	@Override
	public long getLifeTime() {
		return lifeTime;
	}


	@Override
	public void setLifeTime(long lifeTime) {
		if (lifeTime < 0) throw new IllegalArgumentException("Time to live must be at least 0ms");
		this.lifeTime = lifeTime;
	}

	@Override
	public boolean isQueuePaused() {
		// TODO: this method won't work if the pause command was sent via a ConsumerProxy
		// as this does not add the command bean to the command set.
		// This method will be soon be removed by a subsequent commit, and ISubmitter will no longer implement IReadOnlyQueueConnection
		QueueCommandBean bean = getLastPauseResumeBean(getSubmitQueueName());
		return bean != null && bean.getCommand() == Command.PAUSE;
	}

}
