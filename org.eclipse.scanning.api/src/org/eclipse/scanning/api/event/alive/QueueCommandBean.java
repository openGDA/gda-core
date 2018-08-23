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
package org.eclipse.scanning.api.event.alive;

import java.util.UUID;

import org.eclipse.scanning.api.event.IdBean;

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
		PAUSE,

		/**
		 * A command to resume the consumer if it paused.
		 */
		RESUME
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

	public QueueCommandBean() {
		// no-arg constructor for json deserialization
	}

	public QueueCommandBean(UUID consumerId, Command command) {
		this.consumerId = consumerId;
		this.command = command;
	}

	public QueueCommandBean(String queueName, Command command) {
		this.queueName = queueName;
		this.command = command;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		result = prime * result + ((consumerId == null) ? 0 : consumerId.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
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
		return true;
	}

}
