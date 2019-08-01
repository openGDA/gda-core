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
package org.eclipse.scanning.api.event.queue;

import java.util.UUID;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.status.StatusBean;

/**
 * A {@link QueueCommandBean} can be used to control an {@link IJobQueue}. It can be sent
 * to the {@link IJobQueue}s command topic (as returned by {@link IJobQueue#getCommandTopicName()}).
 * A proxy to the {@link IJobQueue} created by calling {@link IEventService#createJobQueueProxy(java.net.URI, String)}
 * controls the real job queue on the server by sending {@link QueueCommandBean}s.
 * <p>
 * The action to be taken by the job queue is determined by the command, as set by calling
 * {@link QueueCommandBean#setCommand(Command)}. The command available can by split into three
 * categories:<ul>
 * <li>Some command, such as {@link Command#PAUSE_QUEUE} and {@link Command#RESUME_QUEUE}, affect the
 * job queue's consumer thread;</li>
 * <li>others, such as {@link Command#MOVE_FORWARD}, {@link Command#MOVE_BACKWARD},
 * {@link Command#REMOVE_FROM_QUEUE} and {@link Command#CLEAR_QUEUE}, affect the submission queue,
 * most of these methods require the bean to be set;</li>
 * <li>the remaining commands such as {@link Command#REMOVE_COMPLETED} and {@value Command#CLEAR_COMPLETED}
 * affect the set of running and completed jobs (the status set)</li>
 *</ul>
 * <p>
 * The {@link IJobQueue} can be identified either by calling {@link #setJobQueueId(UUID)} with the
 * UUID of the job queue, or by calling {@link #setQueueName(String)} with the name of the job
 * queue's submission queue.
 * <p>
 *
 * @author Matthew Gerring
 * @author Matthew Dickie
 *
 */
public class QueueCommandBean  extends IdBean {

	/**
	 * An enumeration of the commands that an {@link IJobQueue} can perform.
	 */
	public enum Command {

		/**
		 * A command to pause the {@link IJobQueue}'s consumer thread if it is running.
		 */
		PAUSE_QUEUE,

		/**
		 * A command to resume the {@link IJobQueue}'s consumer thread if it paused.
		 */
		RESUME_QUEUE,

		/**
		 * A command to stop {@link IJobQueue}'s consumer thread. It is not possible to restart a consumer thread
		 * that has been stopped.
		 */
		STOP_QUEUE,

		/**
		 * A command to clear the queue of submitted jobs waiting to be run.
		 */
		CLEAR_QUEUE,

		/**
		 * A command to clear the set of completed jobs.
		 */
		CLEAR_COMPLETED,

		/**
		 * A command to submit a bean for a job.
		 */
		SUBMIT_JOB,

		/**
		 * A command to pause the job for a bean.
		 */
		PAUSE_JOB,

		/**
		 * A command to resume the job for a bean.
		 */
		RESUME_JOB,

		/**
		 * A command to terminate the job for a bean.
		 */
		TERMINATE_JOB,

		/**
		 * A command to move a bean one place toward the head of the submission queue, i.e.
		 * it will be executed earlier.
		 */
		MOVE_FORWARD,

		/**
		 * A command to move a bean one place toward the tail of the submission queue, i.e.
		 * it will be executed later.
		 */
		MOVE_BACKWARD,

		/**
		 * A command to remove a bean from the submission queue.
		 */
		REMOVE_FROM_QUEUE,

		/**
		 * A command to remove a bean from the set of completed jobs.
		 */
		REMOVE_COMPLETED,

		/**
		 * A command to get the submission queue.
		 */
		GET_QUEUE,

		/**
		 * A command to get the beans for running and completed jobs.
		 */
		GET_RUNNING_AND_COMPLETED,

		/**
		 * A command to get the current state of the {@link IJobQueue}.
		 */
		GET_INFO;

	}

	/**
	 * The unique id of the {@link IJobQueue} the message is intended for.
	 * May be <code>null</code> if {@link #queueName} is set.
	 */
	private UUID jobQueueId;

	/**
	 * The name of the submission queue of the {@link IJobQueue} the message is intended for.
	 * May be set instead of {@link #jobQueueId}
	 */
	private String queueName;

	/**
	 * The message of the consumer
	 */
	private String message;

	/**
	 * The command to perform, e.g. pause/resume
	 */
	private Command command;

	/**
	 * The bean for the job that the command applies to. Only required for command
	 * that apply to a job rather than the queue as a whole.
	 */
	private StatusBean jobBean;

	/**
	 * The error message from the consumer. Set by the consumer when executing the command
	 * specified by the bean. If <code>null</code> then the command was executed successfully.
	 */
	private String errorMessage;

	private Object result;

	public QueueCommandBean() {
		super(); // make sure a unique id is set
		// no-arg constructor for json deserialization
	}

	public QueueCommandBean(UUID consumerId, Command command) {
		super(); // make sure a unique id is set
		this.jobQueueId = consumerId;
		this.command = command;
	}

	public QueueCommandBean(String queueName, Command command) {
		super(); // make sure a unique id is set
		this.queueName = queueName;
		this.command = command;
	}

	public QueueCommandBean(String queueName, Command command, StatusBean jobBean) {
		this(queueName, command);
		this.jobBean = jobBean;
	}

	public UUID getJobQueueId() {
		return jobQueueId;
	}

	public void setJobQueueId(UUID consumerId) {
		this.jobQueueId = consumerId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	public StatusBean getJobBean() {
		return jobBean;
	}

	public void setJobBean(StatusBean jobBean) {
		this.jobBean = jobBean;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		result = prime * result + ((jobQueueId == null) ? 0 : jobQueueId.hashCode());
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((jobBean == null) ? 0 : jobBean.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueueCommandBean other = (QueueCommandBean) obj;
		if (command != other.command)
			return false;
		if (jobQueueId == null) {
			if (other.jobQueueId != null)
				return false;
		} else if (!jobQueueId.equals(other.jobQueueId))
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (jobBean == null) {
			if (other.jobBean != null)
				return false;
		} else if (!jobBean.equals(other.jobBean))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (queueName == null) {
			if (other.queueName != null)
				return false;
		} else if (!queueName.equals(other.queueName))
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "QueueCommandBean [jobQueueId=" + jobQueueId + ", queueName=" + queueName + ", message=" + message
				+ ", command=" + command + ", jobBean=" + jobBean + ", errorMessage=" + errorMessage + ", result="
				+ result + "]";
	}

}
