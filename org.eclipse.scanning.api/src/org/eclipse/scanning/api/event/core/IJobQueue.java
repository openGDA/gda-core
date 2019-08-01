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

import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.queue.QueueCommandBean;
import org.eclipse.scanning.api.event.queue.QueueStatus;
import org.eclipse.scanning.api.event.queue.QueueStatusBean;
import org.eclipse.scanning.api.event.status.Status;

/**
 * A {@link IJobQueue} holds a queue of beans that represent tasks that can be run, together with
 * a consumer thread that reads beans from the queue, creating and running a process for each
 * according to the process factory that this queue has been configured with. It also contains
 * a set of beans for running and completed tasks. As tasks run, any change in their status
 * is published to the status topic, a JMS topic.
 * <p>
 * A job queue can be thought of a being composed of three components, and each method of this
 * interface is directed at exactly one of these components:
 * <ol>
 *
 * <li><em>The submission queue:</em> A queue of beans defining jobs to be run.
 * The method {@link #getSubmissionQueue()} returns a copy of the queue as a list.
 * A bean can be submitted to the tail of the queue using the {@link #submit(Object)} method.
 * The methods {@link #moveForward(Object)}
 * and {@link #moveBackward(Object)} and {@link #remove(Object)} can be used to move beans
 * up the queue, down the queue and to remove a bean from the queue, respectively.
 * {@link #clearQueue()} removes all beans from the queue</li>
 *
 * <li><em>The consumer thread:</em> A thread that runs in a loop, removing a job bean from
 * the submission queue, creating an {@link IBeanProcess} for it and running it
 * The process is run in the same thread if {@link IBeanProcess#isBlocking()} is <code>true</code>,
 * otherwise it is run in a new thread.
 * The method {@link #setRunner(IProcessCreator)} should be called by the code that
 * creates the job queue to set the {@link IProcessCreator} used to create the processes.
 * The methods {@link #start()}, {@link #stop()}, {@link #pause()}, {@link #resume()}
 * can be used to control the consumer thread.</li>
 * <li><em>The status set:</em> An ordered set of beans for running and completed jobs.
 * The consumer thread adds a bean to this set when it is removed from the submission queue.
 * The method {@link #removeCompleted(Object)} can be used to remove a particular bean
 * from this method</li>
 * </ol>
 * <p>
 * When the status of the bean changes, it is published to the status topic, a JMS topic
 * that can be listened to with an {@link ISubscriber}. The name of the status topic is returned by
 * {@link #getStatusTopicName()}. The beans status will change as the job is started and completed.
 * Also, the job queue passes an {@link IPublisher} to the {@link IBeanProcess} that runs
 * the job for the bean so that it can also send notifications to the status topic when necessary.
 * <p>
 * The job queue can also be controlled via a JMS topic called the command topic. The
 * method {@link #getCommandTopicName()} returns the name of this topic, and
 * {@link #getCommandAckTopicName()} the name of the topic used to return responses.
 * The command topic takes {@link QueueCommandBean} where the action to be taken
 * is determined by the command property
 *
 * This job queue is primarily intended to run solstice (GDA9) scans, however it can be used
 * as a generic way to run jobs with messaging.
 *
 * @author Matthew Gerring
 * @author Matthew Dickie
 *
 * @param <T> The bean class used for the queue
 */
public interface IJobQueue<T> extends IConnection, IBeanClass<T> {

	/**
	 * An instance of this interface can be used by clients to be notified of changes to the
	 * queue's status, specifically whether its consumer thread is running, paused or stopped.
	 */
	public interface IQueueStatusListener {

		/**
		 * Notifies the listener that the queue's status has changed.
		 * @param newStatus
		 */
		public void queueStatusChanged(QueueStatus newStatus);

	}

	/**
	 * The name job queue's submission queue. This name is expected to be unique to this instance.
	 *
	 * @return name of the submit queue
	 */
	public String getSubmitQueueName();

	/**
	 * Get a copy of the current submission queue as a list of beans.
	 *
	 * @return the submission queue
	 * @throws EventException if the submission queue cannot be returned for any reason
	 */
	public List<T> getSubmissionQueue() throws EventException;

	/**
	 * Removes all pending jobs from the submission queue.
	 *
	 * @throws EventException if the queue cannot be cleared for any reason
	 */
	void clearQueue() throws EventException;

	/**
	 * Return a list of beans whose jobs are either running or completed.
	 * The list is ordered by submission time, not necessarily the ordering
	 * of the JMS queue.
	 *
	 * @return running and completed beans
	 * @throws EventException if the set of running and completed beans cannot be returned for any reason
	 */
	public List<T> getRunningAndCompleted() throws EventException;

	/**
	 * Clears the set of beans for running and completed jobs.
	 *
	 * @throws EventException if the set of running and completed
	 */
	void clearRunningAndCompleted() throws EventException;

	/**
	 * Cleans up the set of beans for running and completed job by removing certain beans.
	 * Specifically, the beans that are removed are those that meet one of the following criteria:
	 * <ul>
	 *   <li>have status {@link Status#FAILED} or {@link Status#NONE};</li>
	 *   <li>have a status indicating that they are running (i.e. {@link Status#isRunning()} is <code>true</code>)
	 *      and are older than the maximum running age (by default, two days);<li>
	 *   <li>have a status indicating that they are final (i.e. {@link Status#isFinal()} is <code>true</code>)
	 *      and are older than the maximum complete age (by default, one week);</li>
	 *   <li>Additionally jobs that are not started or paused will have their status set to {@link Status#FAILED};</li>
	 * </ul>
	 *<p>
	 * This method is intended to be called on starting the consumer.
	 *
	 * @throws EventException
	 */
	void cleanUpCompleted() throws EventException;

	/**
	 * Moves the given bean towards the head of the submission queue if possible,
	 * i.e. it will be processed sooner.
	 * @param bean
	 * @return <code>true</code> if the bean could be moved, <code>false</code> otherwise
	 * @throws EventException
	 */
	boolean moveForward(T bean) throws EventException;

	/**
	 * Moves the given bean towards the tail of the submission queue if possible,
	 * i.e. it will be processed later.
	 * @param bean
	 * @return <code>true</code> if the bean could be moved, <code>false</code> otherwise
	 * @throws EventException
	 */
	boolean moveBackward(T bean) throws EventException;

	/**
	 * Removes the bean from the submission queue if present. If the bean has been moved
	 * to the set of running and completed jobs, it will not be removed.
	 *
	 * @param bean bean to remove
	 * @return <code>true</code> if the bean was removed, <code>false</code> otherwise,
	 *    i.e. the bean was not present
	 * @throws EventException
	 */
	boolean remove(T bean) throws EventException;

	/**
	 * Remove the given bean from the set of beans for running
	 * and completed jobs (the status set).
	 *
	 * @param bean bean to remove
	 * @return <code>true</code> if the bean was removed, <code>false</code> otherwise,
	 *    i.e. the bean was not present
	 * @throws EventException
	 */
	public boolean removeCompleted(T bean) throws EventException;

	/**
	 * Replace the bean in the submission queue with the given bean, if present.
	 * If the bean has been moved to the set of running and completed jobs,
	 * it will not be removed. A bean will replace another if it has the same unique id
	 * as returned by {@link IdBean#getUniqueId()}
	 *
	 * @param bean bean to remove
	 * @return <code>true</code> if the bean was replaced, <code>false</code> otherwise,
	 *    i.e. the bean was not present
	 * @throws EventException
	 */
	boolean replace(T bean) throws EventException;

	/**
	 * The name of status topic, a JMS topic that receives updated versions of the bean
	 * as it changes while the task that it defines is run.
	 *
	 * @return status topic name
	 */
	public String getStatusTopicName();

	/**
	 * Set the name of the status topic.
	 * @param statusTopicName name of status topic
	 * @throws EventException
	 */
	public void setStatusTopicName(String statusTopicName) throws EventException;

	/**
	 * Set the consumer process to run for each job.
	 * @param process
	 * @throws EventException if the alive topic cannot be sent
	 */
	void setRunner(IProcessCreator<T> process) throws EventException;

	/**
	 * Starts the consumer in new thread and return. Similar to Thread.start()
	 * You must set the runner before calling this method
	 * @throws EventException
	 */
	void start() throws EventException;

	/**
	 * Ask the consumer to stop
	 * @throws EventException
	 */
	void stop() throws EventException;

	/**
	 * Awaits the start of the job queue's consumer thread. Mostly useful for testing.
	 * @throws InterruptedException
	 */
	void awaitStart() throws InterruptedException;

	/**
	 * Awaits the job queue's consumer thread having stopped. Mostly useful for testing.
	 * @throws InterruptedException
	 */
	void awaitStop() throws InterruptedException;

	/**
	 * Starts the job queue's consumer thread and blocks. Similar to Thread.run()
	 * You must set the runner by calling {@link #setRunner(IProcessCreator)} before calling this method.
	 * @throws EventException
	 */
	void run() throws EventException;

	/**
	 * Pauses the job queue's consumer thread if running. It will not process any more jobs
	 * until it resumes. Currently running jobs are not affected.
	 * @throws EventException
	 */
	void pause() throws EventException;

	/**
	 * Resumes the job queue's consumer thread if paused. It will resume processing jobs.
	 * @throws EventException
	 */
	void resume() throws EventException;

	/**
	 * Adds the given bean to the tail of the submission queue.
	 * @param bean bean to add
	 * @throws EventException
	 */
	void submit(T bean) throws EventException;

	/**
	 * If a process for the given bean exists and is running, pauses it. If the bean is still
	 * in the submission queue, then instead when the consumer thread removes it from queue,
	 * it will start its process in a paused state.
	 * @param bean bean whose process to pause.
	 * @throws EventException
	 */
	void pauseJob(T bean) throws EventException;

	/**
	 * If a process for the given bean exists and is paused, resumes it.
	 * @param bean bean whose process to resume.
	 * @throws EventException
	 */
	void resumeJob(T bean) throws EventException;

	/**
	 * If the process for the given bean exists and is running or paused, terminates it. If the bean
	 * is still in the submission queue then instead when the consumer thread removes it from
	 * the queue it will set its status to {@link Status#TERMINATED}, add the bean to the set of
	 * running and completed jobs. It will not create a process for it.
	 * @param bean bean whose process to terminate
	 * @throws EventException
	 */
	void terminateJob(T bean) throws EventException;

	/**
	 * Returns the {@link IProcessCreator} used as a factory to create {@link IBeanProcess} for
	 * each bean as they are removed from the submission queue by the consumer thread.
	 * @return the process creator
	 */
	IProcessCreator<T> getRunner();

	/**
	 * Returns whether the job queue's consumer thread is paused.
	 *
	 * @return <code>true</code> if the consumer thread is paused, <code>false</code> otherwise
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
	 * If set, the name of the topic that the job queue's consumer topic publishes {@link QueueStatusBean}s
	 * to, to indicate that it is running.
	 * @return the queue status topic name, may be <code>null</code>
	 */
	public String getQueueStatusTopicName();

    /**
     * The UUID which uniquely defines this job queue.
     * @return job queue id
     */
	public UUID getJobQueueId();

	/**
	 * Returns the {@link QueueStatus} indicating whether the job queue's consumer thread is
	 * {@link QueueStatus#RUNNING}, {@link QueueStatus#PAUSED} or {@link QueueStatus#STOPPED}.
	 * @return the current status of the queue.
	 */
	public QueueStatus getQueueStatus();

	public void addQueueStatusListener(IQueueStatusListener listener);

	public void removeQueueStatusListener(IQueueStatusListener listener);

	public String getName();
	public void setName(String name);

	/**
	 * Call to disconnect all publishers and subscribers when the connection goes down.
	 * @throws EventException
	 */
	@Override
	public void disconnect() throws EventException;

	/**
	 * Returns if the job queue is active. This is <code>true</code> as long as the consumer thread
	 * is not {@link QueueStatus#STOPPED}, i.e. it is {@link QueueStatus#RUNNING} or {@link QueueStatus#PAUSED}.
	 *
	 * @return true if the consumer is active
	 */
	public boolean isActive();

	/**
	 * Returns whether the job queue's consumer thread will start in a paused state if the submission queue
	 * is non-empty. If the submission queue is empty this flag will have no effect.
	 *
	 * @return <code>true</code> if the consumer thread should start paused with a non-empty submission queue,
	 *     <code>false</code> otherwise
	 */
	boolean isPauseOnStart();

	/**
	 * Sets whether the job queue's consumer thread should start in a paused state if the submission queue
	 * is non-empty. If the submission queue is empty this flag will have no effect. Note this must
	 * be called before the consumer thread is stared.
	 *
	 * @param pauseOnStart <code>true</code> if the consumer thread should start paused with a non-empty submission queue,
	 *     <code>false</code> otherwise
	 */
	void setPauseOnStart(boolean pauseOnStart);

}
