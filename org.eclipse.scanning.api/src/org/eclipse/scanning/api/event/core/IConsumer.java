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
package org.eclipse.scanning.api.event.core;

import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.alive.ConsumerStatus;
import org.eclipse.scanning.api.event.alive.ConsumerStatusBean;

/**
 *
 * A consumer consumes the submission queue. If a job appears on the queue, it
 * starts to run the job and moves the job to a status queue. As information is
 * run about the job, it publishes topics containing the current state of the bean
 * in the queue (JSON string).
 *
 * This consumer is intended to replace the consumer in the DAWN command server and
 * be a generic way to run jobs with messaging.
 *
 * @author Matthew Gerring
 *
 * @param <T> The bean class used for the Queue
 */
public interface IConsumer<T> extends IQueueConnection<T> {

	public interface IConsumerStatusListener {

		public void consumerStatusChanged(ConsumerStatus newStatus);

	}

	/**
	 * The string to define the queue for storing status of scans.
	 *
	 * @return
	 */
	public String getStatusTopicName();

	/**
	 * The string to define the queue for storing status of scans.
	 * @param topic
	 * @throws EventException
	 */
	public void setStatusTopicName(String queueName) throws EventException;

	/**
	 * Set the consumer process to run for each job.
	 * @param process
	 * @throws Exception if the alive topic cannot be sent
	 */
	void setRunner(IProcessCreator<T> process) throws EventException ;

	/**
	 * Starts the consumer in new thread and return. Similar to Thread.start()
	 * You must set the runner before calling this method
	 * @throws Exception
	 */
	void start() throws EventException;

	/**
	 * Ask the consumer to stop
	 * @throws EventException
	 */
	void stop() throws EventException;

	/**
	 * Restarts a running consumer.
	 * @throws EventException
	 */
	void restart() throws EventException;

	/**
	 * Awaits the start of the consumer. There are occasions
	 * when the consumer should start in its own thread but
	 * still provide the ability to await the startup process.
	 *
	 * @throws Exception
	 */
	void awaitStart() throws InterruptedException;

	/**
	 * Starts the consumer and block. Similar to Thread.run()
	 * You must set the runner before calling this method
	 * @throws Exception
	 */
	void run() throws EventException;

	/**
	 * Pauses the consumer, it will not process any more jobs
	 * until it resumes. Currently running jobs are not affected.
	 * @throws EventException
	 */
	void pause() throws EventException;

	/**
	 * Resumes the consumer.
	 * @throws EventException
	 */
	void resume() throws EventException;

	/**
	 * If the process for the given bean is running, pauses it. If the bean is queued,
	 * then when it is consumed from the queue, it's process will be started in a paused state.
	 * @param bean
	 * @throws EventException
	 */
	void pauseJob(T bean) throws EventException;

	/**
	 * Resumes the job for the given bean, if it is currently paused.
	 * @param bean
	 * @throws EventException
	 */
	void resumeJob(T bean) throws EventException;

	/**
	 * If the process for the given bean is running, terminates it. If the bean is queued,
	 * then when it is consumed from the queue, it's process will not be run and the
	 * bean's status set to {@link Status#TERMINATED}.
	 * @param bean
	 * @throws EventException
	 */
	void terminateJob(T bean) throws EventException;


	/**
	 *
	 * @return the current active process which will run jobs
	 */
	IProcessCreator<T> getRunner();

	/**
	 * Returns whether this consumer is paused.
	 *
	 * @return <code>true</code> if this consumer is paused, <code>false</code> otherwise
	 */
	boolean isPaused();

	/**
	 * The topic used to run commands like terminate the running process and get the consumer to stop.
	 * @return topic name
	 */
	public String getCommandTopicName();

	/**
	 * The topic used by the consumer to send acknowledgements for commands received on the command topic.
	 * @return command acknowledgement topic name
	 */
	public String getCommandAckTopicName();

	/**
	 * If set, the name of the topic that the consumer publishes {@link ConsumerStatusBean}s to, to indicate that it is running.
	 * @return the consumer status topic name, may be <code>null</code>
	 */
	public String getConsumerStatusTopicName();

    /**
     * The string UUID which denotes this consumer
     * @return
     */
	public UUID getConsumerId();

	/**
	 *
	 * @return the current status of the consumer.
	 */
	public ConsumerStatus getConsumerStatus();

	public void addConsumerStatusListener(IConsumerStatusListener listener);

	public void removeConsumerStatusListener(IConsumerStatusListener listener);

	public String getName();
	public void setName(String name);

	/**
	 * Call to disconnect all publishers and subscribers when the connection goes down.
	 * @throws EventException
	 */
	@Override
	public void disconnect() throws EventException;

	/**
	 *
	 * @return true if the consumer is active and actively running things from the queue.
	 */
	public boolean isActive();

	/**
	 * If the consumer should pause when it is started with
	 * jobs in the queue and wait until the user requires it to unpause.
	 *
	 * NOTE: setPauseOnStartup(...) must be called before the consumer is started!
	 *
	 * @return
	 */
	boolean isPauseOnStart();

	/**
	 * If the consumer should pause when it is started with
	 * jobs in the queue and wait until the user requires it to unpause.
	 *
	 * NOTE: setPauseOnStartup(...) must be called before the consumer is started!
	 *
	 * @param pauseOnStart
	 */
	void setPauseOnStart(boolean pauseOnStart);

}
