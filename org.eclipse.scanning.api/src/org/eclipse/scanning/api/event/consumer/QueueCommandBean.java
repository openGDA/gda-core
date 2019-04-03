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
package org.eclipse.scanning.api.event.consumer;

import java.util.UUID;

import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.status.StatusBean;

/**
 * This bean is designed to send commands to consumers such as terminate and
 * pause. The command may either be directed at a specific consumer, in which
 * case the user sets the consumerId, an example of this is terminating a consumer
 * from the 'Active Consumers' UI, or the user may set the queue name. If the
 * queue name is set all consumers looking a a given queue will respond to the
 * command.
 *  *
 * @author Matthew Gerring
 *
 */
public class QueueCommandBean  extends IdBean {

	/**
	 * An enumeration of the commands that a queue consumer can perform.
	 */
	public enum Command {

		/**
		 * A command to pause the consumer if it is running.
		 */
		PAUSE_QUEUE,

		/**
		 * A command to resume the consumer if it paused.
		 */
		RESUME_QUEUE,

		/**
		 * A command to stop the consumer. It is not possible to restart a consumer
		 * that has been stopped.
		 */
		STOP_QUEUE,

		/**
		 * A command to restart a running consumer.
		 */
		RESTART_QUEUE,

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
		 * A command to get the current state of the consumer.
		 */
		GET_INFO;

	}

	/**
	 * The unique id of the consumer the message is intended for.
	 * May be <code>null</code> if {@link #queueName} is set.
	 */
	private UUID consumerId;

	/**
	 * The name of the submission queue of the consumer the message is intended for.
	 * May be set instead of {@link #consumerId}
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
		this.consumerId = consumerId;
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

	public UUID getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(UUID consumerId) {
		this.consumerId = consumerId;
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
		result = prime * result + ((consumerId == null) ? 0 : consumerId.hashCode());
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
		if (consumerId == null) {
			if (other.consumerId != null)
				return false;
		} else if (!consumerId.equals(other.consumerId))
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
		return "QueueCommandBean [consumerId=" + consumerId + ", queueName=" + queueName + ", message=" + message
				+ ", command=" + command + ", jobBean=" + jobBean + ", errorMessage=" + errorMessage + ", result="
				+ result + "]";
	}

}
