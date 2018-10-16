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

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.status.Status;

public interface IQueueConnection<T> extends IConnection, IBeanClass<T> {

	/**
	 * The string to define the queue for storing status of scans.
	 *
	 * @return
	 */
	public String getStatusSetName();

	/**
	 * The string to define the queue for storing status of scans.
	 * @param topic
	 * @throws EventException
	 */
	public void setStatusSetName(String queueName) throws EventException;

	/**
	 * The string to define the queue for submitting scan objects to.
	 *
	 * @return
	 */
	public String getSubmitQueueName();

	/**
	 * The string to define the queue for submitting scan objects to.
	 * @throws EventException
	 */
	public void setSubmitQueueName(String queueName) throws EventException;

	/**
	 * Get a copy of the current submission queue as a list of beans, type T
	 * The list is ordered by submission time, not necessarily the ordering
	 * of the JMS queue.
	 *
	 * @return
	 */
	public List<T> getSubmissionQueue() throws EventException ;

	/**
	 * Removes all pending jobs from the consumer's submission queue.
	 * @throws EventException
	 */
	void clearQueue() throws EventException;

	/**
	 * Looks a the command queue to find out if a given queue with
	 * the same submission queue as this submitter is paused.
	 *
	 * @return
	 */
	boolean isQueuePaused();

	/**
	 * Return a list of beans whose jobs are either running or completed.
	 * The list is ordered by submission time, not necessarily the ordering
	 * of the JMS queue.
	 *
	 * @return running and completed beans
	 */
	public List<T> getRunningAndCompleted() throws EventException ;

	/**
	 * Removes all completed jobs from the consumer's status set.
	 * @throws EventException
	 */
	void clearRunningAndCompleted() throws EventException;

	/**
	 * Cleans up the status set by removing certain old jobs.
	 * Specifically, the jobs that are removed are those that meet one of the following criteria:
	 * <ul>
	 *   <li>jobs that have the status {@link Status#FAILED} or {@link Status#NONE};</li>
	 *   <li>jobs that are running (i.e. {@link Status#isRunning()} is <code>true</code>) and are older
	 *      than the maximum running age (by default, two days);<li>
	 *   <li>jobs that are final (i.e. {@link Status#isFinal()} is <code>true</code>) and are older than
	 *      the maximum complete age (by default, one week);</li>
	 *   <li>Additionally jobs that are not started or paused will have their status set to {@link Status#FAILED};</li>
	 * </ul>
	 *
	 * This method is intended to be called on starting the consumer.
	 *
	 * @throws EventException
	 */
	void cleanUpCompleted() throws EventException;

	/**
	 * Tries to reorder the bean in the submission queue if it is
	 * still there. If the bean has been moved to the status set,
	 * it will not be moved
	 *
	 * A pause will automatically be done while the bean
	 * is removed.
	 *
	 * @param bean
	 * @return
	 * @throws EventException
	 */
	boolean reorder(T bean, int amount) throws EventException;

	/**
	 * Tries to remove the bean from the submission queue if it is
	 * still there. If the bean has been moved to the status set,
	 * it will not be removed
	 *
	 * NOTE This method can end up reordering the items.
	 * A pause will automatically be done while the bean
	 * is removed.
	 *
	 * @param bean
	 * @return
	 * @throws EventException
	 */
	boolean remove(T bean) throws EventException;

	/**
	 * Remove the given bean from the set of beans corresponding to running
	 * and completed jobs (the status set).
	 *
	 * @param bean bean to remove
	 */
	public void removeCompleted(T bean) throws EventException;

	/**
	 * Tries to replace the bean from the submission queue if it is
	 * still there. If the bean has been moved to the status set,
	 * it will not be removed
	 *
	 * A pause will automatically be done while the bean
	 * is replace.
	 *
	 * @param bean
	 * @return
	 * @throws EventException
	 */
	boolean replace(T bean) throws EventException;

}
