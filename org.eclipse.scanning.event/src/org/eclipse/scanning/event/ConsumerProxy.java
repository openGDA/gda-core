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

package org.eclipse.scanning.event;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.ConsumerStatus;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.status.StatusBean;

public final class ConsumerProxy<U extends StatusBean> extends AbstractConnection implements IConsumer<U> {

	private final String commandTopicName;
	private final String commandAckTopicName;

	private final IEventService eventService;

	private IRequester<QueueCommandBean> queueCommandRequestor;

	public ConsumerProxy(URI uri, String submissionQueueName, String commandTopicName, String commandAckTopicName, IEventConnectorService connectorService, IEventService eventService) {
		super(uri, connectorService);
		setSubmitQueueName(submissionQueueName);
		this.commandTopicName = commandTopicName;
		this.commandAckTopicName = commandAckTopicName;
		this.eventService = eventService;
	}

	private ConsumerInfo getConsumerInfo() {
		try {
			return (ConsumerInfo) sendCommand(Command.GET_INFO);
		} catch (EventException e) {
			throw new RuntimeException("Could not get consumer info", e);
		}
	}

	private Object sendCommand(Command command) throws EventException {
		return sendCommandBean(new QueueCommandBean(getSubmitQueueName(), command));
	}

	private Object sendCommand(Command command, U bean) throws EventException {
		QueueCommandBean commandBean = new QueueCommandBean(getSubmitQueueName(), command);
		commandBean.setBeanUniqueId(bean.getUniqueId());
		return sendCommandBean(commandBean);
	}

	private synchronized Object sendCommandBean(QueueCommandBean commandBean) throws EventException {
		if (queueCommandRequestor == null) {
			queueCommandRequestor = eventService.createRequestor(uri, getCommandTopicName(), getCommandAckTopicName());
			queueCommandRequestor.setTimeout(10, TimeUnit.SECONDS); // allow a very generous timeout, can take > 1 sec to get queue from ActiveMQ
		}

		try {
			QueueCommandBean response = queueCommandRequestor.post(commandBean);
			if (response.getErrorMessage() != null) {
				throw new EventException("The consumer could not process the command " + commandBean + ". Reason: " + response.getErrorMessage());
			}
			return response.getResult();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new EventException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<U> getRunningAndCompleted() throws EventException {
		return (List<U>) sendCommand(Command.GET_RUNNING_AND_COMPLETED);
	}

	@Override
	public void clearQueue() throws EventException {
		sendCommand(Command.CLEAR);
	}

	@Override
	public void clearRunningAndCompleted() throws EventException {
		sendCommand(Command.CLEAR_COMPLETED);
	}

	@Override
	public synchronized void disconnect() throws EventException {
		super.disconnect();
		queueCommandRequestor.disconnect();
	}

	@Override
	public boolean reorder(U bean, int amount) throws EventException {
		if (amount == 1) {
			return sendCommand(Command.MOVE_FORWARD, bean) == Boolean.TRUE;
		} else if (amount == -1) {
			return sendCommand(Command.MOVE_BACKWARD, bean) == Boolean.TRUE;
		} else {
			// TODO: replace reorder with moveUp and moveDown: see DAQ-1641
			throw new IllegalArgumentException("Beans can only be moved up or down by one place at a time.");
		}
	}

	@Override
	public boolean remove(U bean) throws EventException {
		return sendCommand(Command.REMOVE, bean) == Boolean.TRUE;
	}

	@Override
	public void removeCompleted(U bean) throws EventException {
		sendCommand(Command.REMOVE_COMPLETED, bean);
	}

	@Override
	public boolean replace(U bean) throws EventException {
		// This method is not yet supported by the proxy as it is not currently required to be.
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<U> getSubmissionQueue() throws EventException {
		return (List<U>) sendCommand(Command.GET_QUEUE);
	}

	@Override
	public void setRunner(IProcessCreator<U> process) throws EventException {
		// can't set the runner via the proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void start() throws EventException {
		// there is no command to start a consumer that isn't currently running.
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void stop() throws EventException {
		sendCommand(Command.STOP);
	}

	@Override
	public void restart() throws EventException {
		sendCommand(Command.RESTART);
	}

	@Override
	public void awaitStart() throws InterruptedException {
		// This method cannot be performed via a proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void run() throws EventException {
		// This method cannot be performed via a proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void pause() throws EventException {
		sendCommand(Command.PAUSE);
	}

	@Override
	public void resume() throws EventException {
		sendCommand(Command.RESUME);
	}

	@Override
	public boolean isPaused() {
		return getConsumerStatus() == ConsumerStatus.PAUSED;
	}

	@Override
	public void cleanUpCompleted() throws EventException {
		// The queue cannot be cleaned up via a proxy, clearCompleted is available however
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public IProcessCreator<U> getRunner() {
		// Cannot get the runner from a proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public String getCommandTopicName() {
		return commandTopicName;
	}

	@Override
	public String getCommandAckTopicName() {
		return commandAckTopicName;
	}

	@Override
	public String getHeartbeatTopicName() {
		// The proxy doesn't know the heartbeat topic name
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public UUID getConsumerId() {
		return getConsumerInfo().getConsumerId();
	}

	@Override
	public ConsumerStatus getConsumerStatus() {
		return getConsumerInfo().getStatus();
	}

	@Override
	public void addConsumerStatusListener(IConsumerStatusListener listener) {
		// Can't add or remove status listeners on the proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void removeConsumerStatusListener(IConsumerStatusListener listener) {
		// Can't add or remove status listeners on the proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public String getName() {
		return getConsumerInfo().getName();
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public boolean isActive() {
		return getConsumerStatus() != ConsumerStatus.STOPPED;
	}

	// TODO, move configuration methods to an interface that the proxy doesn't implement?

	@Override
	public boolean isPauseOnStart() {
		// This property is not visible on the proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void setPauseOnStart(boolean pauseOnStart) {
		// This property is not visible on the proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public Class<U> getBeanClass() {
		// This property is not visible on the proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void setBeanClass(Class<U> beanClass) {
		// This property is not visible on the proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

}
