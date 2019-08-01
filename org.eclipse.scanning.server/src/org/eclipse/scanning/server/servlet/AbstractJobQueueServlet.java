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
package org.eclipse.scanning.server.servlet;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJmsQueueReader;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.servlet.IJobQueueServlet;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**

    Class used to register a servlet

    Spring config started servlets, for instance:
    <pre>

    {@literal <bean id="scanPerformer" class="org.eclipse.scanning.server.servlet.ScanServlet" init-method="connect">}
    {@literal    <property name="broker"      value="tcp://p45-control:61616" />}
    {@literal    <property name="submitQueue" value="uk.ac.diamond.p45.submitQueue" />}
    {@literal    <property name="statusSet"   value="uk.ac.diamond.p45.statusSet"   />}
    {@literal    <property name="statusTopic" value="uk.ac.diamond.p45.statusTopic" />}
    {@literal    <property name="durable"     value="true" />}
    {@literal </bean>}

    Use: property name="purgeQueue" value="false" to stop the startup phase purging old inactive jobs.

    </pre>
 *
 * @author Matthew Gerring
 *
 * @param <T> the type of bean consumed by this servlet
 */
public abstract class AbstractJobQueueServlet<T extends StatusBean> implements IJobQueueServlet<T> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractJobQueueServlet.class);

	protected IEventService eventService;
	protected String broker;

	// Property to specify if one scan at a time or more are completed.
	private boolean blocking = true;
	private boolean purgeQueue = true;
	private boolean pauseOnStart = false;

	// Recommended to configure these as
	protected String submitQueue = EventConstants.SUBMISSION_QUEUE;
	protected String statusTopic = EventConstants.STATUS_TOPIC;

	// Recommended not to change these because easier for UI to inspect job queue created
	protected String queueStatusTopic = EventConstants.QUEUE_STATUS_TOPIC;
	protected String commandTopic   = EventConstants.CMD_TOPIC;
	protected String commandAckTopic = EventConstants.ACK_TOPIC;

	protected IJobQueue<T> jobQueue;
	private IJmsQueueReader<T> jmsQueueReader;
	private boolean isConnected;

	protected AbstractJobQueueServlet() {
		this.eventService = Services.getEventService();
	}

	protected AbstractJobQueueServlet(String submitQueue, String statusTopic) {
		this();
		this.submitQueue = submitQueue;
		this.statusTopic = statusTopic;
	}

	@Override
	@PostConstruct // Requires spring 3 or better
	public void connect() throws EventException, URISyntaxException {
		jobQueue = eventService.createJobQueue(new URI(getBroker()), getSubmitQueue(), getStatusTopic(),
				getQueueStatusTopic(), getCommandTopic(), getCommandAckTopic());
		jobQueue.setName(getName());
		jobQueue.setRunner(AbstractJobQueueServlet.this::createProcess);
		jobQueue.setPauseOnStart(pauseOnStart);

		// Clean up the queue
		if (isPurgeQueue())
			jobQueue.cleanUpCompleted();

		jobQueue.start();

		// start a queue reader for the queue for beans that are still submitted to the JMS queue.
		// This reads the beans from the JMS queue and submits them to the IJobQueue.
		jmsQueueReader = eventService.createJmsQueueReader(new URI(getBroker()), getSubmitQueue());
		jmsQueueReader.start();

		isConnected = true;
		logger.info("Started {} for queue {}", getClass().getSimpleName(), getSubmitQueue());
	}

	protected abstract String getName();

	@Override
	@PreDestroy
	public void disconnect() throws EventException {
		if (!isConnected)
			return; // Nothing to disconnect
		eventService.disposeJobQueue();
		jmsQueueReader.disconnect();
	}

	public IJobQueue<T> getJobQueue() {
		return jobQueue;
	}

	public String getBroker() {
		return broker;
	}

	public void setBroker(String uri) {
		this.broker = uri;
	}

	public String getSubmitQueue() {
		return submitQueue;
	}

	public void setSubmitQueue(String submitQueue) {
		this.submitQueue = submitQueue;
	}

	public String getStatusTopic() {
		return statusTopic;
	}

	public void setStatusTopic(String statusTopic) {
		this.statusTopic = statusTopic;
	}

	public String getQueueStatusTopic() {
		return queueStatusTopic;
	}

	public void setQueueStatusTopic(String queueStatusTopic) {
		this.queueStatusTopic = queueStatusTopic;
	}

	public String getCommandTopic() {
		return commandTopic;
	}

	public void setCommandTopic(String commandTopic) {
		this.commandTopic = commandTopic;
	}

	public String getCommandAckTopic() {
		return commandAckTopic;
	}



	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	@Override
	public boolean isConnected() {
		return isConnected && (jobQueue!=null ? jobQueue.isConnected() : true);
	}

	public boolean isPurgeQueue() {
		return purgeQueue;
	}

	public void setPurgeQueue(boolean purgeQueue) {
		this.purgeQueue = purgeQueue;
	}

	public void setConsumer(IJobQueue<T> jobQueue) {
		this.jobQueue = jobQueue;
	}

	public boolean isPauseOnStart() {
		return pauseOnStart;
	}

	public void setPauseOnStart(boolean pauseOnStart) {
		this.pauseOnStart = pauseOnStart;
	}

}
