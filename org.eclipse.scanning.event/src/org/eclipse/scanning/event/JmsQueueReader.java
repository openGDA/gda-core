/*-
 * Copyright © 2019 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.event;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJmsQueueReader;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsQueueReader<U extends StatusBean> extends AbstractConnection implements IJmsQueueReader<U> {

	private static final Logger logger = LoggerFactory.getLogger(JmsQueueReader.class);

	private final IJobQueue<U> jobQueue;
	private final String queueName;
	private MessageConsumer messageConsumer;

	private boolean isRunning = false;

	private int waitTime = 0;

	public JmsQueueReader(URI uri, IEventService eventService, String queueName) throws EventException {
		super(uri, eventService.getEventConnectorService());
		this.queueName = queueName;

		@SuppressWarnings("unchecked")
		IJobQueue<U> jobQueue = (IJobQueue<U>) eventService.getJobQueue(queueName);
		this.jobQueue = jobQueue;
	}

	@Override
	public String getQueueName() {
		return queueName;
	}

	@Override
	public void start() {
		if (isRunning) throw new IllegalStateException("The queue reader for the queue '" + queueName + "' is already running");

		final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = Executors.defaultThreadFactory().newThread(runnable);
			thread.setName("JMS queue reader " + queueName);
			thread.setDaemon(true);
			return thread;
		});

		executor.submit(this::runAndHandleError);
	}

	protected void runAndHandleError() {
		try {
			run();
		} catch (Exception e) {
			logger.error("Fatal error in queue reader for queue '{}'", queueName, e);
		}

		try {
			disconnect();
		} catch (EventException e) {
			logger.error("Error disconnecting from queue '{}'", queueName, e);
		}
	}

	private void initialize() throws EventException {
		isRunning = true;

		try {
			Session session = getSession();
			Queue queue = createQueue(queueName);
			messageConsumer = session.createConsumer(queue);
		} catch (JMSException e) {
			throw new EventException("Could not connect to activemq queue: " + queueName, e);
		}
		logger.info("Reader for JMS queue '{}' successfully created", queueName);
	}

	@Override
	public void run() throws EventException {
		if (isRunning) throw new IllegalStateException("The queue reader for the queue '" + queueName + "' is already running");

		initialize();

		while (isRunning) {
			try {
				readNextMessage();
			} catch (Exception e) {
				isRunning = processException(e);
			}
		}
	}

	private void readNextMessage() throws Exception {
		final Message jmsMessage = messageConsumer.receive(EventTimingsHelper.getReceiveTimeout());
		if (jmsMessage != null) {
			TextMessage textMessage = (TextMessage) jmsMessage;
			final String jsonMessage = textMessage.getText();
			final U bean = getConnectorService().unmarshal(jsonMessage, null);
			jobQueue.submit(bean);
		}
	}

	private boolean processException(Exception e) {
		// if error occurred deserializing the bean, log a specific message and continue processing beans
		if (e.getClass().getSimpleName().contains("Json") || e.getClass().getSimpleName().endsWith("UnrecognizedPropertyException")) {
			logger.error("Could not deserialize bean.", e);
			return true;
		}

		logger.warn("{} ActiveMQ connection to {} lost.", queueName, uri, e);
		logger.warn("We will check every 2 seconds for 24 hours, until it comes back.");
		final long retryInterval = EventTimingsHelper.getConnectionRetryInterval();
		try {
			// Wait for 2 seconds (default time)
			Thread.sleep(retryInterval);
		} catch (InterruptedException ie) {
			logger.error("Interrupted while waiting to reconnect", e);
			Thread.currentThread().interrupt();
			return false;
		}

		waitTime += retryInterval;

		// Exits if wait time more than one day
		if (waitTime > EventTimingsHelper.DEFAULT_MAXIMUM_WAIT_TIME.toMillis()) {
			logger.warn("ActiveMQ permanently lost. Queue reader for queue '{}' will now exit", queueName);
			return false;
		}

		return true;
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public void stop() {
		if (!isRunning) throw new IllegalArgumentException("The queue reader for the queue '" + queueName + "' is not running");

		isRunning = false;
	}

	@Override
	public synchronized void disconnect() throws EventException {
		if (isRunning) stop();
		isRunning = false;

		super.disconnect();

		if (messageConsumer != null) {
			try {
				messageConsumer.close();
			} catch (JMSException e) {
				throw new EventException("Could not close JMS message consumer for queue: " + queueName, e);
			}
			messageConsumer = null;
		}
	}

}
