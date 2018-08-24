/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.event.core;

import java.util.List;

import org.eclipse.scanning.api.event.EventException;

/**
 * An interface representing a read only connection to a queue.
 * Note: this interface is a temporary measure as part of the work of DAQ-1464,
 * namely making it so that only the consumer can see the queue. It is easier
 * to first remove the methods that modify the queue, and make {@link ISubmitter}
 * implement this interface instead of {@link IQueueConnection}, leaving {@link IConsumer}
 * as the only interface that extends {@link IQueueConnection}
 * @param <T>
 */
public interface IReadOnlyQueueConnection<T> extends IQueueReader<T> {

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

}
